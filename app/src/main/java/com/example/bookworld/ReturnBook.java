package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.Book;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ReturnBook extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference borrowedBooksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        userId = currentUser != null ? currentUser.getUid() : null;
        borrowedBooksRef = db.collection("users").document(userId).collection("borrowedBooks");

        // Initialize views
        TextView titleTextView = findViewById(R.id.bookTitle);
        TextView authorTextView = findViewById(R.id.bookAuthor);
        TextView descriptionTextView = findViewById(R.id.bookDescription);
        TextView priceTextView = findViewById(R.id.bookPrice);
        ImageView thumbnailImageView = findViewById(R.id.bookThumbnail);
        Button readButton = findViewById(R.id.borrowButton);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotsButton = findViewById(R.id.three_dotButton);

        // Set back button click listener
        backButton.setOnClickListener(v -> finish());

        // Set three dots button click listener
        threeDotsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReturnBook.this, three_dots.class);
            startActivity(intent);
        });

        // Retrieve book details from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("BOOK_ID");
            String title = extras.getString("BOOK_TITLE");
            String author = extras.getString("BOOK_AUTHOR");
            String description = extras.getString("BOOK_DESCRIPTION");
            String price = extras.getString("BOOK_PRICE");
            String thumbnailUrl = extras.getString("BOOK_THUMBNAIL");
            String pdfUrl = extras.getString("PDF_URL");

            // Set retrieved data to TextViews and ImageView
            titleTextView.setText(title);
            authorTextView.setText("by " + author);
            descriptionTextView.setText(description);
            priceTextView.setText("Ksh " + price);
            Picasso.get().load(thumbnailUrl).into(thumbnailImageView);


            // Set "Read" button click listener
            readButton.setOnClickListener(v -> {
                Intent intent = new Intent(ReturnBook.this, ContentActivity.class);
                intent.putExtra("PDF_URL", pdfUrl); // Pass the PDF URL to ContentActivity
                startActivity(intent);
            });
        }
    }


    // No need for refreshData method in this case
}
