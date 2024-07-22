package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BorrowPage extends AppCompatActivity {

    private EditText nameEditText;
    private Spinner daysSpinner;
    private Spinner chapterSpinner;
    private FirebaseFirestore db;
    private List<String> chapterList;
    private List<String> bookChapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_page);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotButton = findViewById(R.id.threeDotButton);
        nameEditText = findViewById(R.id.nameEditText);
        daysSpinner = findViewById(R.id.daysSpinner);
        chapterSpinner = findViewById(R.id.chapterSpinner);
        Button readButton = findViewById(R.id.readButton);

        // Set up the Spinner with the days array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(adapter);

        // Set click listeners for the buttons
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity to go back to the previous one
                finish();
            }
        });

        threeDotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BorrowPage.this, three_dots.class);
                startActivity(intent);
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();

                if (name.isEmpty()) {
                    nameEditText.setError("Name cannot be empty");
                    nameEditText.requestFocus();
                    return;
                }

                String days = daysSpinner.getSelectedItem().toString();
                Toast.makeText(BorrowPage.this, "Name: " + name + ", Days: " + days, Toast.LENGTH_SHORT).show();

                // Fetch book chapters and scan
                fetchBookChapters();
            }
        });

        // Set listener for the chapter spinner selection
        chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedChapterTitle = chapterList.get(position);
                if (selectedChapterTitle != null) {
                    // Launch BookContents activity with selected chapter
                    Intent intent = new Intent(BorrowPage.this, BookContents.class);
                    intent.putExtra("chapter_title", selectedChapterTitle);
                    startActivity(intent);
                } else {
                    Toast.makeText(BorrowPage.this, "Please select a chapter to read", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected
            }
        });
    }

    private void fetchBookChapters() {
        String bookId = "your_book_id"; // Replace with actual book ID or logic to fetch book ID
        db.collection("Books").document(bookId).collection("Chapters")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            bookChapters = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String chapterTitle = document.getString("chapterTitle");
                                bookChapters.add(chapterTitle);
                            }
                            ArrayAdapter<String> chapterAdapter = new ArrayAdapter<>(BorrowPage.this, android.R.layout.simple_spinner_item, bookChapters);
                            chapterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            chapterSpinner.setAdapter(chapterAdapter);
                        } else {
                            Toast.makeText(BorrowPage.this, "Error fetching book chapters", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
