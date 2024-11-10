package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.Book;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class search_discovery extends AppCompatActivity {

    private TextView searchButton;
    private FirebaseFirestore db;
    private EditText searchEditText;
    private TextView messageTextView;
    private LinearLayout bookGenresLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_discovery);

        // Initialize layouts and button
        bookGenresLayout = findViewById(R.id.Book_Genres);
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout);
        LinearLayout moreLayout = findViewById(R.id.morelayout);
        ImageView threeDotButton = findViewById(R.id.three_dotButton);
        ImageView backButton = findViewById(R.id.backButton);
        LinearLayout artLayout = findViewById(R.id.art);
        searchButton = findViewById(R.id.searchButton);
        messageTextView = findViewById(R.id.messageTextView);
        searchEditText = findViewById(R.id.searchEditText);
        LinearLayout technologyLayout = findViewById(R.id.technologyLayout);
        LinearLayout businessLayout = findViewById(R.id.businessLayout);
        LinearLayout animationLayout = findViewById(R.id.animationLayout);
        LinearLayout healthLayout = findViewById(R.id.health_sciencesLayout);
        LinearLayout comicsLayout = findViewById(R.id.comicsLayout);
        LinearLayout fantasyLayout = findViewById(R.id.fantasyLayout);
        LinearLayout historyLayout = findViewById(R.id.historyLayout);
        LinearLayout nonfictionLayout = findViewById(R.id.non_fiction);
        LinearLayout fictionLayout = findViewById(R.id.fictionLayout);
        db = FirebaseFirestore.getInstance();

        searchButton.setOnClickListener(view -> {
            String query = searchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                searchBooks(query);
            } else {
                Toast.makeText(search_discovery.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

        // Set onClick listeners for navigation
        homeLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Home.class)));
        myBooksLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, MyBooks.class)));
        moreLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, More.class)));
        threeDotButton.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, three_dots.class)));
        backButton.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Home.class)));
        artLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Fiction.class)));
        technologyLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Technology.class)));
        businessLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Business.class)));
        animationLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, animation.class)));
        healthLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, HealthSciences.class)));
        comicsLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, comics.class)));
        fantasyLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, fantasy.class)));
        historyLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, History.class)));
        nonfictionLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Non_Fiction.class)));
        fictionLayout.setOnClickListener(view -> startActivity(new Intent(search_discovery.this, Art.class)));
    }

    private void searchBooks(String query) {
        List<String> collections = List.of("Fiction", "Technology", "Fantasy", "Animation", "Comics", "Art", "Non-fiction", "Business", "Health Sciences", "History");
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Query each collection
        for (String collection : collections) {
            tasks.add(db.collection(collection)
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + "\uf8ff")
                    .get());
        }

        // Wait for all tasks to complete
        Tasks.whenAllSuccess(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                bookGenresLayout.removeAllViews();
                boolean hasResults = false;

                for (Object result : task.getResult()) {
                    QuerySnapshot querySnapshot = (QuerySnapshot) result;
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String id = document.getId();
                        String thumbnailUrl = document.getString("thumbnailUrl");
                        String title = document.getString("title");
                        String author = document.getString("author");
                        String description = document.getString("description");
                        String pdfUrl = document.getString("pdfUrl");
                        String price = document.getString("price");
                        int daysToBorrow = document.getLong("daysToBorrow").intValue();  // Use intValue() to convert Long to int


                        float rating = 0.0f;
                        Object ratingObj = document.get("rating");
                        if (ratingObj instanceof Double) {
                            rating = ((Double) ratingObj).floatValue();
                        } else if (ratingObj instanceof Float) {
                            rating = (Float) ratingObj;
                        }

                        // Create a Book object and add it to the layout
                        Book book = new Book(id, thumbnailUrl, title, author, description, price, rating, pdfUrl, daysToBorrow);
                        // Assuming you have a method to create a view for the book and add it to the layout
                        View bookView = createBookView(book);
                        bookGenresLayout.addView(bookView);

                        hasResults = true;
                    }
                }

                // Show/hide UI components based on search result
                if (hasResults) {
                    messageTextView.setVisibility(View.GONE);
                    bookGenresLayout.setVisibility(View.VISIBLE);
                } else {
                    messageTextView.setText("Book not available");
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_empty_book, 0, 0, 0);

                    // Hide the book genres layout
                    bookGenresLayout.setVisibility(View.GONE);
                }
            } else {
                Log.e("FirestoreError", "Error getting documents: ", task.getException());
                Toast.makeText(getApplicationContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to create a view for a book (implement according to your layout)
    private View createBookView(Book book) {
        // Create and return a view for the book
        // This could involve inflating a layout and setting the book details
        return null; // Replace with actual implementation
    }
}
