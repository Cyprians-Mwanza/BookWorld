package com.example.bookworld;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LendBooksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lend_books);

        Button btnChooseBook = findViewById(R.id.btnChooseBook);
        btnChooseBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement functionality to choose a book to lend
            }
        });
    }
}