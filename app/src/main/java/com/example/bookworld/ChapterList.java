package com.example.bookworld;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ChapterList extends AppCompatActivity {

    private ListView chapterListView;
    private final List<String> chapters = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        chapterListView = findViewById(R.id.chapterListView);
        Button borrowSelectedChaptersButton = findViewById(R.id.borrowSelectedChaptersButton);

        // Get the book ID from the intent
        String bookId = getIntent().getStringExtra("BOOK_ID");

        // Set up the ListView and its adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, chapters);
        chapterListView.setAdapter(adapter);
        chapterListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Load chapters from Firestore
        FirestoreChapterExtractor.extractChapters(this, bookId, new FirestoreChapterExtractor.OnChaptersExtractedListener() {
            @Override
            public void onChaptersExtracted(List<String> extractedChapters) {
                chapters.clear();
                chapters.addAll(extractedChapters);
                adapter.notifyDataSetChanged(); // Ensure the adapter is notified of data changes
            }
        });

        // Set up the button click listener
        borrowSelectedChaptersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrowSelectedChapters();
            }
        });
    }

    private void borrowSelectedChapters() {
        // Get selected chapters
        SparseBooleanArray checkedItems = chapterListView.getCheckedItemPositions();
        List<String> selectedChapters = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            if (checkedItems.valueAt(i)) {
                selectedChapters.add(chapters.get(position));
            }
        }

        if (selectedChapters.isEmpty()) {
            Toast.makeText(this, "No chapters selected", Toast.LENGTH_SHORT).show();
        } else {
            // Process the borrowing of selected chapters (e.g., save to Firestore or local storage)
            Toast.makeText(this, "Borrowed " + selectedChapters.size() + " chapters", Toast.LENGTH_SHORT).show();
            // Add your borrowing logic here
        }
    }
}
