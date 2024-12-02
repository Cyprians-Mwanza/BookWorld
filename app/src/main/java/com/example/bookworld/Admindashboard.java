package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bookworld.bookdata.BorrowedBooks;
import com.example.bookworld.bookdata.BorrowedBooksAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Admindashboard extends AppCompatActivity {

    private static final String TAG = "Admindashboard";
    private FirebaseFirestore db;
    private List<BorrowedBooks> borrowedBooksList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private BorrowedBooksAdapter adapter;
    private ProgressBar progressBar;
    private ImageView threeDotsButton;
    private GraphView lineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView); // Ensure RecyclerView is initialized first
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        threeDotsButton = findViewById(R.id.threeDotButton);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize borrowedBooksList and RecyclerView Adapter
        borrowedBooksList = new ArrayList<>();
        adapter = new BorrowedBooksAdapter(borrowedBooksList);

        // Set up RecyclerView with LayoutManager and Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        // Add Book button
        Button buttonAddBook = findViewById(R.id.addBookButton);
        buttonAddBook.setOnClickListener(v -> {
            // Navigate to AddBookActivity
            Intent intent = new Intent(Admindashboard.this, AddBookActivity.class);
            startActivity(intent);
        });

        threeDotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(Admindashboard.this, three_dots.class);
                startActivity(intent);
            }
        });

        // Set up swipe-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener(() -> fetchBorrowedBooks());

        // Fetch borrowed books when the activity starts
        fetchBorrowedBooks();


    }

    private void fetchBorrowedBooks() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")  // Fetch all users
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BorrowedBooks> allBooksList = new ArrayList<>();

                        // Loop through each user
                        for (DocumentSnapshot userDoc : task.getResult()) {
                            String userId = userDoc.getId();

                            // Fetch borrowedBooks for each user
                            db.collection("users")
                                    .document(userId)
                                    .collection("borrowedBooks")
                                    .get()
                                    .addOnCompleteListener(bookTask -> {
                                        if (bookTask.isSuccessful()) {
                                            // Loop through borrowed books
                                            for (DocumentSnapshot borrowedBookSnapshot : bookTask.getResult()) {
                                                String borrowerName = borrowedBookSnapshot.getString("name");
                                                String bookTitle = borrowedBookSnapshot.getString("bookTitle");
                                                String price = borrowedBookSnapshot.getString("price");
                                                String returnDate = borrowedBookSnapshot.getString("returnDate");

                                                // Create BorrowedBooks object
                                                BorrowedBooks borrowedBook = new BorrowedBooks(borrowerName, bookTitle, price, returnDate);
                                                allBooksList.add(borrowedBook);
                                            }

                                            // Update RecyclerView with new list of borrowed books
                                            adapter.updateData(allBooksList);
                                        } else {
                                            Toast.makeText(Admindashboard.this, "Error fetching borrowed books.", Toast.LENGTH_SHORT).show();
                                        }

                                        // Stop refreshing the SwipeRefreshLayout and hide progress bar
                                        swipeRefreshLayout.setRefreshing(false);
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    } else {
                        Toast.makeText(Admindashboard.this, "Error fetching users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void displayTopBorrowedBooks(Map<String, Integer> bookBorrowCount) {
        // Get top 5 books sorted by borrow count
        List<Map.Entry<String, Integer>> topBooks = bookBorrowCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        // Prepare data points for the graph
        DataPoint[] dataPoints = new DataPoint[topBooks.size()];
        for (int i = 0; i < topBooks.size(); i++) {
            Map.Entry<String, Integer> entry = topBooks.get(i);
            dataPoints[i] = new DataPoint(i + 1, entry.getValue());
        }

        // Display data on the chart
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
        lineChart.removeAllSeries(); // Clear previous data
        lineChart.addSeries(series);

        // Set graph labels
        lineChart.getGridLabelRenderer().setHorizontalAxisTitle("Books");
        lineChart.getGridLabelRenderer().setVerticalAxisTitle("Borrow Count");
        lineChart.getViewport().setMinX(0);
        lineChart.getViewport().setMaxX(6);
        lineChart.getViewport().setYAxisBoundsManual(true);
    }
}