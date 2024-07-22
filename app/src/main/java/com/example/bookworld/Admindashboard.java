package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.BorrowedBooks;
import com.example.bookworld.bookdata.BorrowedBooksAdapter;

import java.util.ArrayList;
import java.util.List;

public class Admindashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

        // Initialize views
        RecyclerView recyclerBorrowedBooks = findViewById(R.id.recyclerBorrowedBooks);
        recyclerBorrowedBooks.setLayoutManager(new LinearLayoutManager(this));

        // Example data for borrowed books (replace with actual data retrieval)
        List<BorrowedBooks> borrowedBooks = new ArrayList<>();
        borrowedBooks.add(new BorrowedBooks("Book 1", "John Doe", "2024-07-15"));
        borrowedBooks.add(new BorrowedBooks("Book 2", "Jane Smith", "2024-07-20"));

        // Set up adapter
        BorrowedBooksAdapter adapter = new BorrowedBooksAdapter(borrowedBooks);
        recyclerBorrowedBooks.setAdapter(adapter);

        // Button to add a book
        Button addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start AddBookActivity
                Intent intent = new Intent(Admindashboard.this, AddBookActivity.class);
                startActivity(intent);
            }
        });
    }
}
