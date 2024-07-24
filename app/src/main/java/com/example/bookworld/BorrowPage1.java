package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class BorrowPage1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_page1);

        // Retrieve the book ID from the intent
        String bookId = getIntent().getStringExtra("BOOK_ID");

        // Initialize views
        Button borrowFullBookButton = findViewById(R.id.btnFullBook);
        Button borrowChapterButton = findViewById(R.id.btnBorrowChapter);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);

        // Set back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set three-dot button click listener
        threeDotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(BorrowPage1.this, three_dots.class);
                startActivity(intent);
            }
        });

        // Set borrow full book button click listener
        borrowFullBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle borrow full book functionality
                // For example, you might want to show a message or start a new activity
                // based on your app's flow
                Intent intent = new Intent(BorrowPage1.this, BorrowPage.class);
                intent.putExtra("BOOK_ID", bookId);
                startActivity(intent);
            }
        });

        // Set borrow chapter button click listener
        borrowChapterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BorrowPage1.this, ChapterList.class);
                intent.putExtra("BOOK_ID", bookId);
                startActivity(intent);
            }
        });
    }
}
