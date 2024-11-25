package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.ReturnBooksAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyBooks extends AppCompatActivity implements ReturnBooksAdapter.OnBookClickListener {

    private RecyclerView recyclerView;
    private ReturnBooksAdapter adapter;
    private List<Book> bookList;
    private TextView welcomeTextView;
    private Button viewCartButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String userId;
    private CollectionReference borrowedBooksRef;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_Layout);
        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bookList = new ArrayList<>();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        viewCartButton = findViewById(R.id.view_button);

        // User data
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            adapter = new ReturnBooksAdapter(this, bookList, userId);
            recyclerView.setAdapter(adapter);

            // Initialize Firestore reference
            borrowedBooksRef = db.collection("users").document(userId).collection("borrowedBooks");

            // Fetch books and display user info
            fetchBooksFromFirestore();
            fetchUsernameAndDisplay();
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }

        // Bottom Navigation
        initializeBottomNavigation();

        // Swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshCart);
    }

    private void fetchBooksFromFirestore() {
        if (userId == null) {
            Toast.makeText(MyBooks.this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        borrowedBooksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                bookList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String bookId = document.getString("bookId");
                    String bookTitle = document.getString("bookTitle");
                    String thumbnailUrl = document.getString("thumbnailUrl");
                    String author = document.getString("author");
                    String description = document.getString("description");
                    String price = document.getString("price");
                    Long returnDateMillis = document.getLong("returnDateMillis"); // Fetch as Long

                    if (bookId != null && bookTitle != null && price != null) {
                        // Create Book object and set fields
                        Book book = new Book(bookId, thumbnailUrl, bookTitle, author, description, price, 0, "");
                        if (returnDateMillis != null) {
                            book.setReturnDateMillis(returnDateMillis); // Set return date
                        } else {
                            book.setReturnDateMillis(0L); // Default value for missing date
                        }
                        bookList.add(book);
                    }
                }
                updateCountdowns(); // Update countdown strings
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MyBooks.this, "Failed to fetch borrowed books: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for real-time updates
        borrowedBooksRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(MyBooks.this, "Error fetching books: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                bookList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Book book = doc.toObject(Book.class);
                    book.setId(doc.getId());  // Set the document ID to the book
                    bookList.add(book);
                }
                updateCountdowns(); // Update countdowns for live data changes
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateCountdowns() {
        for (Book book : bookList) {
            if (book.getReturnDateMillis() > 0) {
                long returnDateMillis = book.getReturnDateMillis();
                long currentMillis = System.currentTimeMillis();

                long diffMillis = returnDateMillis - currentMillis;

                // Calculate days and hours
                long daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis);
                long remainingMillisAfterDays = diffMillis - TimeUnit.DAYS.toMillis(daysLeft);
                long hoursLeft = TimeUnit.MILLISECONDS.toHours(remainingMillisAfterDays);

                // Set countdown text
                String countdown;
                if (diffMillis >= 0) {
                    countdown = daysLeft + " days " + hoursLeft + " hours remaining";
                } else {
                    countdown = "Overdue by " + Math.abs(daysLeft) + " days " + Math.abs(hoursLeft) + " hours";
                }

                book.setCountdown(countdown); // Update book countdown
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void refreshCart() {
        fetchBooksFromFirestore();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void fetchUsernameAndDisplay() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    if (username != null) {
                        welcomeTextView.setText(Html.fromHtml("Hi, <b><font color='#FF6F00'>" + username + "</font></b>, welcome back to your reading, check your cart to see the books that you wish to read."));
                    }
                } else {
                    Toast.makeText(MyBooks.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(MyBooks.this, "Error fetching username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(MyBooks.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeBottomNavigation() {
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        LinearLayout moreLayout = findViewById(R.id.moreLayout);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);
        ImageView backButton = findViewById(R.id.backButton);

        viewCartButton.setOnClickListener(v -> startActivity(new Intent(MyBooks.this, CartActivity.class)));
        homeLayout.setOnClickListener(view -> startActivity(new Intent(MyBooks.this, Home.class)));
        searchLayout.setOnClickListener(view -> startActivity(new Intent(MyBooks.this, search_discovery.class)));
        moreLayout.setOnClickListener(view -> startActivity(new Intent(MyBooks.this, More.class)));
        threeDotButton.setOnClickListener(view -> startActivity(new Intent(MyBooks.this, three_dots.class)));
        backButton.setOnClickListener(v -> startActivity(new Intent(MyBooks.this, Home.class)));
    }


    public void onBookClick(Book book) {
        Intent intent = new Intent(MyBooks.this, ReturnBook.class);
        intent.putExtra("BOOK_ID", book.getId());
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
        intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
        intent.putExtra("BOOK_PRICE", book.getPrice());
        intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
        intent.putExtra("BOOK_RATING", book.getRating());
        intent.putExtra("PDF_URL", book.getPdfUrl());
        startActivity(intent);
    }
}
