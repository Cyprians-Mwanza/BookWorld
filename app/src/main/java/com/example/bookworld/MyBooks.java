package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.BookAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyBooks extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView welcomeTextView;
    private Button viewCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        // Initialize Firestore and Firebase Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList, this); // Pass this activity as the OnBookClickListener
        recyclerView.setAdapter(adapter);

        // Retrieve books from Firestore
        fetchBooksFromFirestore();

        // Initialize welcomeTextView
        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Fetch and display username
        fetchUsernameAndDisplay();

        // Set onClick listeners for bottom navigation
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        LinearLayout moreLayout = findViewById(R.id.moreLayout);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);
        ImageView backButton = findViewById(R.id.backButton);
        viewCartButton = findViewById(R.id.view_button); // Correct initialization

        viewCartButton.setOnClickListener(v -> {
            // Navigate to CartActivity
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
    }

    private void fetchBooksFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MyBooks.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("borrowedBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear(); // Clear the list before adding new items

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String bookId = document.getString("bookId");
                            String bookTitle = document.getString("bookTitle");
                            String thumbnailUrl = document.getString("thumbnailUrl");
                            String author = document.getString("author");
                            String description = document.getString("description");
                            String price = document.getString("price");
                            String pdfUrl = document.getString("pdfUrl");

                            // Retrieve "days" as a String and convert to Integer
                            String daysStr = document.getString("days");

                            if (bookId != null && bookTitle != null && price != null) {
                                // Create a Book object and add it to the list
                                Book book = new Book(bookId, thumbnailUrl, bookTitle, author, description, price, 0, pdfUrl);
                                bookList.add(book);
                            }
                        }

                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(MyBooks.this, "Failed to fetch borrowed books: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUsernameAndDisplay() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            fetchUsernameFromFirestore(userId);
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUsernameFromFirestore(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String username = document.getString("username");
                    if (username != null) {
                        updateWelcomeMessage(username);
                    } else {
                        Toast.makeText(MyBooks.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyBooks.this, "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MyBooks.this, "Failed to fetch username from Firestore.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWelcomeMessage(String username) {
        String welcomeMessage = "Hi <font color='#FF6F00'>" + username + "</font>, Welcome back to your reading, check your cart to see the books that you wish to read.";
        welcomeTextView.setText(Html.fromHtml(welcomeMessage));
    }


    @Override
    public void onBookClick(Book book) {
        // Handle click events on books here
        Intent intent = new Intent(MyBooks.this, ContentActivity.class);
        intent.putExtra("PDF_URL", book.getPdfUrl()); // Pass the PDF URL to ContentActivity
        startActivity(intent);
    }

}
