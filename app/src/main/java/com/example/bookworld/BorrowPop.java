package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class BorrowPop extends AppCompatActivity {

    private static final String TAG = "BorrowPop"; // Tag for logging

    private TextView nameEditText;
    private EditText daysEditText;
    private Button borrowButton;
    private Button readButton;

    private FirebaseFirestore db;
    private String userId;
    private String bookId;
    private String bookTitle;
    private String pdfUrl;
    private String thumbnailUrl;
    private String author;
    private String description;
    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_pop);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        daysEditText = findViewById(R.id.daysEditText);
        borrowButton = findViewById(R.id.borrowButton);
        readButton = findViewById(R.id.readButton);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotsButton = findViewById(R.id.three_dotButton);

        // Retrieve book details from intent extras
        Intent intent = getIntent();
        bookId = intent.getStringExtra("BOOK_ID");
        bookTitle = intent.getStringExtra("BOOK_TITLE");
        pdfUrl = intent.getStringExtra("PDF_URL");
        thumbnailUrl = intent.getStringExtra("BOOK_THUMBNAIL_URL");  // Corrected key
        author = intent.getStringExtra("BOOK_AUTHOR");  // Corrected key
        description = intent.getStringExtra("BOOK_DESCRIPTION");  // Corrected key
        price = intent.getStringExtra("BOOK_PRICE");  // Corrected key

        Log.d(TAG, "Book ID: " + bookId);
        Log.d(TAG, "Book Title: " + bookTitle);
        Log.d(TAG, "PDF URL: " + pdfUrl);
        Log.d(TAG, "Thumbnail URL: " + thumbnailUrl);
        Log.d(TAG, "Author: " + author);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "Price: " + price);

        // Set borrow button click listener
        borrowButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String daysStr = daysEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(daysStr)) {
                Toast.makeText(BorrowPop.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            int days = Integer.parseInt(daysStr);
            if (days < 1 || days > 5) {
                Toast.makeText(BorrowPop.this, "Maximum Number of Days Allowed is 5", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the book is already borrowed or the user has borrowed more than 5 books
            checkBorrowConditions(name, days);
        });

        // Fetch the current user's username and set it in the nameEditText
        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username"); // Assume "username" field holds the user's name
                            if (username != null) {
                                nameEditText.setText(username); // Set the username in nameEditText
                            }
                        } else {
                            Log.d(TAG, "User document does not exist.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching username: ", e));
        }


        // Set read button click listener
        readButton.setOnClickListener(v -> {
            if (pdfUrl != null) {
                Intent intent1 = new Intent(BorrowPop.this, ContentActivity.class);
                intent1.putExtra("PDF_URL", pdfUrl); // Pass PDF URL to ContentActivity
                startActivity(intent1);
            } else {
                Toast.makeText(BorrowPop.this, "PDF URL not available", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkBorrowConditions(String name, int days) {
        db.collection("users").document(userId).collection("borrowedBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int borrowedBookCount = 0;
                        boolean isAlreadyBorrowed = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getString("bookId").equals(bookId)) {
                                isAlreadyBorrowed = true;
                                break;
                            }
                            borrowedBookCount++;
                        }

                        if (isAlreadyBorrowed) {
                            Toast.makeText(BorrowPop.this, "You have already borrowed this book", Toast.LENGTH_SHORT).show();
                        } else if (borrowedBookCount >= 5) {
                            Toast.makeText(BorrowPop.this, "You cannot borrow more than 5 books", Toast.LENGTH_SHORT).show();
                        } else {
                            storeBorrowingDetails(name, days);
                        }
                    } else {
                        Log.e(TAG, "Error getting borrowed books: ", task.getException());
                    }
                });
    }

    // Your existing storeBorrowingDetails method with the date added
    private void storeBorrowingDetails(String name, int days) {
        // Create a date formatter to get the current date in "dd-MM-yy" format
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yy");
        String currentDate = dateFormatter.format(new Date()); // Get the current date as a string

        // Create a map to store borrowing details
        Map<String, Object> borrowData = new HashMap<>();
        borrowData.put("name", name);
        borrowData.put("days", days);  // Store as integer directly
        borrowData.put("bookId", bookId);
        borrowData.put("bookTitle", bookTitle);
        borrowData.put("pdfUrl", pdfUrl);
        borrowData.put("thumbnailUrl", thumbnailUrl);
        borrowData.put("author", author);
        borrowData.put("description", description);
        borrowData.put("price", price);
        borrowData.put("dateBorrowed", currentDate); // Add the formatted current date

        // Add the borrowData to Firestore
        db.collection("users").document(userId).collection("borrowedBooks").add(borrowData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BorrowPop.this, "Book borrowed successfully", Toast.LENGTH_SHORT).show();
                    readButton.setVisibility(View.VISIBLE);  // Show read button
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                        if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Toast.makeText(BorrowPop.this, "You do not have permission to perform this operation", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BorrowPop.this, "Error borrowing book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BorrowPop.this, "Error borrowing book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
