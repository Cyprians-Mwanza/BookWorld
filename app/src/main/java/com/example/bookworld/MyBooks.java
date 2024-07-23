package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.BookAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyBooks extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> bookList;
    private DatabaseReference dbRef;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList, this); // Pass this activity as the OnBookClickListener
        recyclerView.setAdapter(adapter);
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        // Retrieve books from Firestore
        fetchBooksFromFirestore();

        // Initialize welcomeTextView
        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Fetch and display username
        fetchUsernameAndDisplay();

        // Set onClick listeners for bottom navigation
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout myBooksLayout = findViewById(R.id.myBooksLayout);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        LinearLayout moreLayout = findViewById(R.id.moreLayout);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);
        ImageView backButton = findViewById(R.id.backButton);
        viewCartButton = findViewById(R.id.view_button); // Correct initialization

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        myBooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Already in MyBooks activity, no action needed
            }
        });

        viewCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to CartActivity
                Intent intent = new Intent(MyBooks.this, CartActivity.class);
                startActivity(intent);
            }
        });

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyBooks.this, Home.class);
                startActivity(intent);
            }
        });

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyBooks.this, search_discovery.class);
                startActivity(intent);
            }
        });

        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyBooks.this, More.class);
                startActivity(intent);
            }
        });

        threeDotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyBooks.this, three_dots.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyBooks.this, Home.class);
                startActivity(intent);
            }
        });
    }

    private void fetchBooksFromFirestore() {
        db.collection("Fantasy")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            bookList.clear(); // Clear the list before adding new items
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId(); // Assuming the document ID can be used as the book ID
                                String thumbnailUrl = document.getString("thumbnailUrl");
                                String title = document.getString("title");
                                String author = document.getString("author");
                                String description = document.getString("description");
                                String price = document.getString("price");

                                float rating = 0.0f; // Default value if not found or conversion fails
                                Object ratingObj = document.get("rating");
                                if (ratingObj instanceof Double) {
                                    rating = ((Double) ratingObj).floatValue();
                                } else if (ratingObj instanceof Float) {
                                    rating = (Float) ratingObj;
                                }

                                // Create a Book object and add it to the list
                                Book book = new Book(id, thumbnailUrl, title, author, description, price, rating);
                                bookList.add(book);
                            }

                            // Notify the adapter that the data set has changed
                            adapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            Toast.makeText(MyBooks.this, "Failed to fetch books: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchUsernameAndDisplay() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            fetchUsernameFromRealtimeDatabase(userId);
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUsernameFromRealtimeDatabase(String userId) {
        dbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null) {
                        updateWelcomeMessage(username);
                    } else {
                        fetchUsernameFromFirestore(userId);
                    }
                } else {
                    fetchUsernameFromFirestore(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyBooks.this, "Failed to fetch username from Realtime Database.", Toast.LENGTH_SHORT).show();
            }
        });
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
        String welcomeMessage = "Hi " + username + ", Welcome back to your reading, check your cart to see the books that you wish to read.";
        welcomeTextView.setText(welcomeMessage);
    }

    public void onBookClick(Book book) {
        // Handle click events on books here (if needed)
        // For example, open a detailed view of the book
        Intent intent = new Intent(MyBooks.this, BookDetails.class);
        intent.putExtra("book_id", book.getId());
        startActivity(intent);
    }
}
