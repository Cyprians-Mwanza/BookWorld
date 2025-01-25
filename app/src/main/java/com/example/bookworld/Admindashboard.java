package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bookworld.bookdata.BorrowedBooks;
import com.example.bookworld.bookdata.BorrowedBooksAdapter;
import com.example.bookworld.bookdata.ReportAdmin;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Admindashboard extends AppCompatActivity {

    private static final String TAG = "Admindashboard";
    private FirebaseFirestore db;
    private List<BorrowedBooks> borrowedBooksList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private BorrowedBooksAdapter adapter;
    private ProgressBar progressBar;
    private ImageView threeDotsButton, generateReportButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView); // Ensure RecyclerView is initialized first
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        threeDotsButton = findViewById(R.id.threeDotButton);
        Button generateReportButton = findViewById(R.id.generate);  // Add the generate report button

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize borrowedBooksList and RecyclerView Adapter
        borrowedBooksList = new ArrayList<>();
        adapter = new BorrowedBooksAdapter(borrowedBooksList);

        // Set up RecyclerView with LayoutManager and Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        // Add Book button
        generateReportButton.setOnClickListener(v -> generateReport());
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

  private void generateReport() {
        double totalPrice = 0;
        int borrowedBooksCount = 0;
        Set<String> uniqueBorrowers = new HashSet<>();

        // Maps to store the frequencies of books and favorite genres
        Map<String, Integer> bookBorrowCount = new HashMap<>();
        Map<String, Integer> userBorrowCount = new HashMap<>();
        Map<String, Integer> genreSelectionCount = new HashMap<>();

        // Process borrowed books list
        for (BorrowedBooks borrowedBook : borrowedBooksList) {
            // Sum up the prices of all borrowed books
            try {
                totalPrice += Double.parseDouble(borrowedBook.getPrice());
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Handle invalid price format
            }

            // Count the number of borrowed books
            borrowedBooksCount++;

            // Track unique borrowers
            uniqueBorrowers.add(borrowedBook.getName());

            // Track book borrow frequency
            bookBorrowCount.put(borrowedBook.getBookTitle(),
                    bookBorrowCount.getOrDefault(borrowedBook.getBookTitle(), 0) + 1);

            // Track user borrow count
            userBorrowCount.put(borrowedBook.getName(),
                    userBorrowCount.getOrDefault(borrowedBook.getName(), 0) + 1);
        }

        // Fetch user IDs from Firestore
      double finalTotalPrice = totalPrice;
      int finalBorrowedBooksCount = borrowedBooksCount;
      db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> userIdsList = new ArrayList<>();
                for (DocumentSnapshot userDoc : task.getResult()) {
                    userIdsList.add(userDoc.getId());  // Add user ID to the list
                }

                // After fetching user IDs, fetch favorite genres for each user
                for (String userId : userIdsList) {
                    db.collection("users").document(userId).collection("Favourite genre")
                            .get()
                            .addOnCompleteListener(favoriteGenresTask -> {
                                if (favoriteGenresTask.isSuccessful()) {
                                    for (DocumentSnapshot documentSnapshot : favoriteGenresTask.getResult()) {
                                        String genre = documentSnapshot.getString("genreName");
                                        genreSelectionCount.put(genre,
                                                genreSelectionCount.getOrDefault(genre, 0) + 1);
                                    }
                                }
                            });
                }

                                // After all data is processed, generate the report
                final int uniqueBorrowersCount = uniqueBorrowers.size(); // Make this variable final
                generateReportContent(bookBorrowCount, userBorrowCount, genreSelectionCount, finalTotalPrice, finalBorrowedBooksCount, uniqueBorrowersCount);
            }
        });
    }


    private void generateReportContent(Map<String, Integer> bookBorrowCount, Map<String, Integer> userBorrowCount,
                                       Map<String, Integer> genreSelectionCount, double totalPrice, int borrowedBooksCount,
                                       int uniqueBorrowersCount) {

        // Convert the report data to strings or lists for passing through Intent
        String totalAmount = String.format("Total Amount: %.2f", totalPrice);
        String totalBooksBorrowed = "Total Books Borrowed: " + borrowedBooksCount;
        String totalBorrowers = "Total Borrowers: " + uniqueBorrowersCount;

        // Format top borrowed books
        StringBuilder topBooks = new StringBuilder("\n");
        bookBorrowCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(entry -> topBooks.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));

        // Format top users
        StringBuilder topUsers = new StringBuilder("\n");
        userBorrowCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .forEach(entry -> topUsers.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));

        // Format top genres
        StringBuilder topGenres = new StringBuilder("\n");
        genreSelectionCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .forEach(entry -> topGenres.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));

        // Pass data to ReportAdmin via Intent
        Intent intent = new Intent(Admindashboard.this, ReportAdmin.class);
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        intent.putExtra("TOTAL_BOOKS_BORROWED", totalBooksBorrowed);
        intent.putExtra("TOTAL_BORROWERS", totalBorrowers);
        intent.putExtra("TOP_BOOKS", topBooks.toString());
        intent.putExtra("TOP_USERS", topUsers.toString());
        intent.putExtra("TOP_GENRES", topGenres.toString());
        startActivity(intent);
    }
    
}