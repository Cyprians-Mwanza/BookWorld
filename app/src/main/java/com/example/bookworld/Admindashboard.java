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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        threeDotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(Admindashboard.this, three_dots.class);
                startActivity(intent);
            }
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
        long currentTimeMillis = System.currentTimeMillis();
        for (BorrowedBooks borrowedBook : borrowedBooksList) {
            try {
                int days = borrowedBook.getDays(); // Directly get the int value
                long borrowEndTimeMillis = borrowedBook.getBorrowStartTime() + TimeUnit.DAYS.toMillis(days);
                long remainingMillis = borrowEndTimeMillis - currentTimeMillis;

                if (remainingMillis > 0) {
                    long daysLeft = TimeUnit.MILLISECONDS.toDays(remainingMillis);
                    long hoursLeft = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
                    long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
                    long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;

                    borrowedBook.setCountdown(String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft));
                }
            } catch (NumberFormatException e) {
                borrowedBook.setCountdown("Invalid Days");
                Log.e(TAG, "Error parsing days: " + borrowedBook.getDays(), e);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateCharts() {
        // Prepare data for the line chart
        List<BorrowedBooks> topBorrowedBooks = getTopBorrowedBooks(borrowedBooksList);

        // Clear existing series from the line chart
        lineChart.removeAllSeries();

        // Create LineGraphSeries for each top borrowed book
        int colorIndex = 0;
        for (BorrowedBooks book : topBorrowedBooks) {
            LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>();
            int index = 0;

            // Example data: Use actual data points from your data
            for (int i = 0; i < 10; i++) { // Replace with actual data
                lineSeries.appendData(new DataPoint(index++, Math.random() * 10), true, 10);
            }

            // Set a different color for each series
            lineSeries.setColor(getLineColor(colorIndex));
            lineSeries.setTitle(book.getBookTitle()); // Ensure this method exists
            lineChart.addSeries(lineSeries);
            colorIndex++;
        }

        lineChart.setTitle("Top Borrowed Books");
        lineChart.getLegendRenderer().setVisible(true);
        lineChart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private List<BorrowedBooks> getTopBorrowedBooks(List<BorrowedBooks> borrowedBooksList) {
        // Example logic: sort books by borrow count and return top N
        borrowedBooksList.sort((b1, b2) -> Integer.compare(b2.getBorrowCount(), b1.getBorrowCount()));
        return borrowedBooksList.subList(0, Math.min(5, borrowedBooksList.size()));
    }

    private int getLineColor(int index) {
        int[] colors = {
                android.graphics.Color.RED,
                android.graphics.Color.GREEN,
                android.graphics.Color.BLUE,
                android.graphics.Color.YELLOW,
                android.graphics.Color.CYAN
        };
        return colors[index % colors.length];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateCountdownRunnable); // Stop countdown updates when activity is destroyed
    }
}
