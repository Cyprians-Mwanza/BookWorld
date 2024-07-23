package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class BookDetails extends AppCompatActivity {

    private CollectionReference cartRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Initialize Firebase components
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        cartRef = db.collection("users").document(userId).collection("cartItems");

        // Initialize views
        TextView titleTextView = findViewById(R.id.bookTitle);
        TextView authorTextView = findViewById(R.id.bookAuthor);
        TextView descriptionTextView = findViewById(R.id.bookDescription);
        TextView priceTextView = findViewById(R.id.bookPrice);
        ImageView thumbnailImageView = findViewById(R.id.bookThumbnail);
        Button addcartButton = findViewById(R.id.add_to_cart_button);
        Button borrowButton = findViewById(R.id.borrowButton);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotsButton = findViewById(R.id.three_dotButton);

        // Set back button click listener
        backButton.setOnClickListener(v -> finish());

        // Set three dots button click listener
        threeDotsButton.setOnClickListener(v -> {
            // Navigate to the "three dots" activity
            Intent intent = new Intent(BookDetails.this, three_dots.class);
            startActivity(intent);
        });

        borrowButton.setOnClickListener(v -> {
            // Navigate to the "three dots" activity
            Intent intent = new Intent(BookDetails.this, BorrowPage1.class);
            startActivity(intent);
        });


        // Retrieve book details from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("BOOK_ID", "default_id");
            String title = extras.getString("BOOK_TITLE", "Default Title");
            String author = extras.getString("BOOK_AUTHOR", "Unknown Author");
            String description = extras.getString("BOOK_DESCRIPTION", "No Description");
            String price = extras.getString("BOOK_PRICE", "0.0"); // Retrieve price as a string
            String thumbnailUrl = extras.getString("BOOK_THUMBNAIL");
            float rating = extras.getFloat("BOOK_RATING", 0.0f);

            // Set retrieved data to TextViews and ImageView
            titleTextView.setText(title);
            authorTextView.setText(author);
            descriptionTextView.setText(description);
            priceTextView.setText("Ksh " + price);
            Picasso.get().load(thumbnailUrl).into(thumbnailImageView);

            // Make description and price visible
            descriptionTextView.setVisibility(View.VISIBLE);
            priceTextView.setVisibility(View.VISIBLE);

            // Set add to cart button click listener
            addcartButton.setOnClickListener(v -> {
                // Create a Book object
                Book book = new Book(id, thumbnailUrl, title, author, description, price, rating);

                // Create a map for the book data
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", book.getId());
                bookData.put("thumbnailUrl", book.getThumbnailUrl());
                bookData.put("title", book.getTitle());
                bookData.put("author", book.getAuthor());
                bookData.put("description", book.getDescription());
                bookData.put("price", book.getPrice());
                bookData.put("rating", book.getRating());

                // Save the book to the user's cart in Firestore
                cartRef.add(bookData).addOnSuccessListener(documentReference -> {
                    Toast.makeText(BookDetails.this, "Book added to cart", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(BookDetails.this, "Error adding to cart", Toast.LENGTH_SHORT).show();
                });

                // Pass book details to CartActivity
                Intent intent = new Intent(BookDetails.this, CartActivity.class);
                intent.putExtra("BOOK", book);  // Pass the Book object directly
                startActivity(intent);
            });
        }
    }
}
