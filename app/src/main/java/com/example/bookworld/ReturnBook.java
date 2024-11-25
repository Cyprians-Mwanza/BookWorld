package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReturnBook extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference borrowedBooksRef;
    private RecyclerView chaptersRecyclerView;
    private List<String> chaptersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
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
        chaptersRecyclerView = findViewById(R.id.RecyclerView);

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

            readButton.setOnClickListener(v -> {
                if (pdfUrl != null) {
                    extractChaptersFromPdf(pdfUrl);
                } else {
                    Toast.makeText(ReturnBook.this, "PDF URL not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void extractChaptersFromPdf(String pdfUrl) {
        new Thread(() -> {
            InputStream inputStream = null;
            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                inputStream = connection.getInputStream();
                PdfReader reader = new PdfReader(inputStream);
                PdfDocument pdfDoc = new PdfDocument(reader);

                int totalPages = pdfDoc.getNumberOfPages();
                chaptersList = new ArrayList<>();
                for (int page = 1; page <= totalPages; page++) {
                    String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(page));
                    if (pageContent.contains("Chapter")) {
                        String[] lines = pageContent.split("\n");
                        for (String line : lines) {
                            if (line.contains("Chapter")) {
                                chaptersList.add(line.trim());
                            }
                        }
                    }
                }

                runOnUiThread(() -> {
                    chaptersRecyclerView.setLayoutManager(new LinearLayoutManager(ReturnBook.this));
                    ChapterAdapter adapter = new ChapterAdapter(chaptersList, position -> {
                        Intent intent = new Intent(ReturnBook.this, ContentActivity.class);
                        intent.putExtra("PDF_URL", pdfUrl);
                        intent.putExtra("CHAPTER_INDEX", position);
                        startActivity(intent);
                    });
                    chaptersRecyclerView.setAdapter(adapter);
                    Toast.makeText(ReturnBook.this, "Chapters/topics extracted!", Toast.LENGTH_SHORT).show();
                });

                pdfDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ReturnBook.this, "Error extracting PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
        private List<String> chapters;
        private OnChapterClickListener listener;

        public interface OnChapterClickListener {
            void onChapterClick(int position);
        }

        public ChapterAdapter(List<String> chapters, OnChapterClickListener listener) {
            this.chapters = chapters;
            this.listener = listener;
        }

        @Override
        public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ChapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChapterViewHolder holder, int position) {
            String chapter = chapters.get(position);
            holder.chapterTextView.setText(chapter);
            holder.itemView.setOnClickListener(v -> listener.onChapterClick(position));
        }

        @Override
        public int getItemCount() {
            return chapters.size();
        }

        public static class ChapterViewHolder extends RecyclerView.ViewHolder {
            TextView chapterTextView;

            public ChapterViewHolder(View itemView) {
                super(itemView);
                chapterTextView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
