package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
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
    private BorrowedBooksAdapter adapter;
    private List<BorrowedBooks> borrowedBooksList;
    private Map<String, Integer> borrowerCounts = new HashMap<>();
    private GraphView lineChart;
    private GraphView barChart;
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
        recyclerBorrowedBooks.setAdapter(adapter);

        // Initialize GraphView
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.pieChart);
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
                int days = Integer.parseInt(borrowedBook.getDays()); // Convert string to int
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

        // Prepare data for the bar chart
        updateBarChart();
    }

    private void updateBarChart() {
        // Clear existing series from the bar chart
        barChart.removeAllSeries();

        // Create BarGraphSeries
        BarGraphSeries<DataPoint> barSeries = new BarGraphSeries<>();
        int index = 0;

        // Populate BarGraphSeries with borrower counts
        for (Map.Entry<String, Integer> entry : borrowerCounts.entrySet()) {
            String borrower = entry.getKey();
            int count = entry.getValue();
            barSeries.appendData(new DataPoint(index++, count), true, borrowerCounts.size());
        }

        barSeries.setSpacing(50); // Set spacing between bars
        barChart.addSeries(barSeries);

        barChart.setTitle("Top Borrowers");
        barChart.getLegendRenderer().setVisible(true);
        barChart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
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
