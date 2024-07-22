package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LendBookActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lend_book);


        Button ViewBorrowedBooks = findViewById(R.id.btnViewBorrowedBooks);

        // Initialize layouts and button
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout);
        LinearLayout searchLayout = findViewById(R.id.searchbutton);
        LinearLayout moreLayout = findViewById(R.id.morelayout);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);
        ImageView backButton = findViewById(R.id.backButton);
        // Ensure this ID matches your XML

        // Set onClick listeners
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LendBookActivity.this, Home.class);
                startActivity(intent);
            }
        });

        myBooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LendBookActivity.this, MyBooks.class);
                startActivity(intent);
            }
        });

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LendBookActivity.this, search_discovery.class);
                startActivity(intent);
            }
        });

        // Set onClick listener for the "More" button to show pop-up window
        // Set onClick listener for the "More" button to show pop-up window
        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LendBookActivity.this, More.class);
                startActivity(intent);
            }
        });

        threeDotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LendBookActivity.this, three_dots.class);
                startActivity(intent);
            }
        });


        // Set onClick listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the current activity
            }
        });


        ViewBorrowedBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LendBookActivity.this, LendBookActivity.class);
                startActivity(intent);
            }
        });






    }
}