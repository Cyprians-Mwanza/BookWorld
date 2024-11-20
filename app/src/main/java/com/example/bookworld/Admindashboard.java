package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.LegendRenderer;
import com.example.bookworld.bookdata.BorrowedBooks;
import com.example.bookworld.bookdata.BorrowedBooksAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Admindashboard extends AppCompatActivity {

    private static final String TAG = "Admindashboard";

    private FirebaseFirestore db;
    private ImageView threeDotsButton;
    private BorrowedBooksAdapter adapter;
    private List<BorrowedBooks> borrowedBooksList;
    private Map<String, Integer> borrowerCounts = new HashMap<>();
    private GraphView lineChart;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountdowns();
            handler.postDelayed(this, 60000); // Update every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        RecyclerView recyclerBorrowedBooks = findViewById(R.id.recyclerBorrowedBooks);
        recyclerBorrowedBooks.setLayoutManager(new GridLayoutManager(this, 2));

        borrowedBooksList = new ArrayList<>();
        adapter = new BorrowedBooksAdapter(borrowedBooksList);
        threeDotsButton = findViewById(R.id.threeDotButton);
        recyclerBorrowedBooks.setAdapter(adapter);

        // Initialize GraphView
        lineChart = findViewById(R.id.lineChart);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Add Book button
        Button buttonAddBook = findViewById(R.id.addBookButton);
        buttonAddBook.setOnClickListener(v -> {
            // Navigate to AddBookActivity
            Intent intent = new Intent(Admindashboard.this, AddBookActivity.class);
            startActivity(intent);
        });

        // Set up swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> fetchBorrowedBooks());

        threeDotsButton.setOnClickListener(v -> {
            // Navigate to the "three dots" activity
            Intent intent = new Intent(Admindashboard.this, three_dots.class);
            startActivity(intent);
        });

        // Fetch borrowed books immediately after login
        fetchBorrowedBooks();

        // Start the countdown update loop
        handler.post(updateCountdownRunnable);
    }

    private void fetchBorrowedBooks() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    borrowedBooksList.clear(); // Clear existing data
                    borrowerCounts.clear(); // Clear borrower counts

                    for (DocumentSnapshot userSnapshot : querySnapshot.getDocuments()) {
                        DocumentReference userDocRef = userSnapshot.getReference();
                        userDocRef.collection("borrowedBooks").get().addOnCompleteListener(borrowedBooksTask -> {
                            if (borrowedBooksTask.isSuccessful()) {
                                QuerySnapshot borrowedBooksSnapshot = borrowedBooksTask.getResult();
                                if (borrowedBooksSnapshot != null) {
                                    for (DocumentSnapshot borrowedBookSnapshot : borrowedBooksSnapshot.getDocuments()) {
                                        BorrowedBooks borrowedBook = borrowedBookSnapshot.toObject(BorrowedBooks.class);
                                        if (borrowedBook != null) {
                                            // Ensure days is retrieved as an int
                                            Long daysLong = borrowedBookSnapshot.getLong("days");
                                            if (daysLong != null) {
                                                borrowedBook.setDays(daysLong.intValue());
                                            } else {
                                                borrowedBook.setDays(0); // Default value if not present
                                            }
                                            borrowedBooksList.add(borrowedBook);

                                            // Update borrower counts
                                            String borrower = borrowedBook.getName(); // Ensure this method exists
                                            borrowerCounts.put(borrower, borrowerCounts.getOrDefault(borrower, 0) + 1);
                                        }
                                    }

                                    // Notify adapter of data change
                                    adapter.notifyDataSetChanged();

                                    // Update charts after data is fetched
                                    updateCharts();
                                }
                            } else {
                                Log.w(TAG, "Error getting borrowed books.", borrowedBooksTask.getException());
                            }
                        });
                    }

                    swipeRefreshLayout.setRefreshing(false); // Stop the refresh indicator
                }
            } else {
                Toast.makeText(Admindashboard.this, "Error getting users.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error getting users.", task.getException());
            }
        });
    }

    private void updateCountdowns() {
        LocalDate currentDate = LocalDate.now();
        for (BorrowedBooks book : borrowedBooksList) {
            if (book.getReturnDateMillis() > 0) {
                LocalDate returnDate = LocalDate.ofEpochDay(book.getReturnDateMillis() / (24 * 60 * 60 * 1000));
                long daysLeft = ChronoUnit.DAYS.between(currentDate, returnDate);
                String countdown = "Due in " + daysLeft + " days";
                book.setCountdown(countdown);
            }
        }
        adapter.notifyDataSetChanged(); // Notify adapter about countdown changes
    }

    private void updateCharts() {
        // Setup for graph chart
        List<DataPoint> dataPoints = new ArrayList<>();
        int i = 0;
        for (String borrower : borrowerCounts.keySet()) {
            dataPoints.add(new DataPoint(i++, borrowerCounts.get(borrower)));
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
        lineChart.addSeries(series);

        // Set chart properties
        lineChart.getViewport().setScalable(true);
        lineChart.getViewport().setScrollable(true);
        lineChart.getLegendRenderer().setVisible(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateCountdownRunnable); // Remove countdown update when activity is destroyed
    }
}