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

        // Initialize Firestore and Firebase Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and adapter
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_Layout);
        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bookList = new ArrayList<>();

        // Initialize other UI components
        welcomeTextView = findViewById(R.id.welcomeTextView);
        viewCartButton = findViewById(R.id.view_button);

        // Fetch the current user and initialize userId
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            adapter = new ReturnBooksAdapter(this, bookList, userId);
            recyclerView.setAdapter(adapter);

            // Initialize borrowedBooksRef
            borrowedBooksRef = db.collection("users").document(userId).collection("borrowedBooks");

            // Retrieve books from Firestore
            fetchBooksFromFirestore();

            // Fetch and display username
            fetchUsernameAndDisplay();
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }

        // Set onClick listeners for bottom navigation
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        LinearLayout moreLayout = findViewById(R.id.moreLayout);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);
        ImageView backButton = findViewById(R.id.backButton);

        viewCartButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyBooks.this, CartActivity.class);
            startActivity(intent);
        });

        homeLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MyBooks.this, Home.class);
            startActivity(intent);
        });

        searchLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MyBooks.this, search_discovery.class);
            startActivity(intent);
        });

        moreLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MyBooks.this, More.class);
            startActivity(intent);
        });

        threeDotButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyBooks.this, three_dots.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyBooks.this, Home.class);
            startActivity(intent);
        });

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshCart);
    }

    private void fetchBooksFromFirestore() {
        if (userId == null) {
            Toast.makeText(MyBooks.this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        borrowedBooksRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String bookId = document.getString("bookId");
                            String bookTitle = document.getString("bookTitle");
                            String thumbnailUrl = document.getString("thumbnailUrl");
                            String author = document.getString("author");
                            String description = document.getString("description");
                            String price = document.getString("price");
                            String pdfUrl = document.getString("pdfUrl");

                            // Fetch the daysToBorrow value and ensure it's not null
                            Long daysToBorrowLong = document.getLong("daysToBorrow");
                            int daysToBorrow = (daysToBorrowLong != null) ? daysToBorrowLong.intValue() : 0;  // Default to 0 if null


                            if (bookId != null && bookTitle != null && price != null) {
                                Book book = new Book(bookId, thumbnailUrl, bookTitle, author, description, price, 0, pdfUrl, daysToBorrow);
                                bookList.add(book);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MyBooks.this, "Failed to fetch borrowed books: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Listen for changes in the borrowedBooks collection
        borrowedBooksRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
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
                    adapter.notifyDataSetChanged(); // Notify the adapter of data changes
                }
            }
        });
    }

    private void refreshCart() {
        fetchBooksFromFirestore(); // Re-fetch books data
        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
    }

    private void fetchUsernameAndDisplay() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null) {
                                welcomeTextView.setText(Html.fromHtml("Hi, <b> <font color='#FF6F00'>" + username + "</font> </b> welcome back to your reading, check your cart to see the books that you wish to read."));
                            }
                        } else {
                            Toast.makeText(MyBooks.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyBooks.this, "Error fetching username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(MyBooks.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
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
