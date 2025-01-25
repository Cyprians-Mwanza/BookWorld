package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowPop extends AppCompatActivity {
    private static final String TAG = "BorrowPop";
    private TextView nameEditText;
    private EditText daysEditText;
    private Button borrowButton;
    private Button readButton;
    private RecyclerView chaptersRecyclerView;
    private List<String> chaptersList;

    private FirebaseFirestore db;
    private String userId;
    private String bookId;
    private String bookTitle;
    private String pdfUrl, price;
    private String thumbnailUrl;
    private String author;
    private String description;

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
        chaptersRecyclerView = findViewById(R.id.chapterRecyclerView);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotsButton = findViewById(R.id.three_dotButton);

        // Retrieve book details from intent extras
        Intent intent = getIntent();
        bookId = intent.getStringExtra("BOOK_ID");
        bookTitle = intent.getStringExtra("BOOK_TITLE");
        pdfUrl = intent.getStringExtra("PDF_URL");
        thumbnailUrl = intent.getStringExtra("BOOK_THUMBNAIL_URL");
        author = intent.getStringExtra("BOOK_AUTHOR");
        description = intent.getStringExtra("BOOK_DESCRIPTION");
        price = intent.getStringExtra("BOOK_PRICE");

        // Set borrow button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(BorrowPop.this, BookDetails.class);
                startActivity(intent);
            }
        });

        threeDotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(BorrowPop.this, three_dots.class);
                startActivity(intent);
            }
        });

        borrowButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String daysStr = daysEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(daysStr)) {
                Toast.makeText(BorrowPop.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            int days = Integer.parseInt(daysStr);
            if (days < 1 || days > 10) {
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
                            String username = documentSnapshot.getString("username");
                            if (username != null) {
                                nameEditText.setText(username);
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
                extractChaptersFromPdf(pdfUrl);
            } else {
                Toast.makeText(BorrowPop.this, "PDF URL not available", Toast.LENGTH_SHORT).show();
            }
        });
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
                    chaptersRecyclerView.setLayoutManager(new LinearLayoutManager(BorrowPop.this));
                    ChapterAdapter adapter = new ChapterAdapter(chaptersList, position -> {
                        Intent intent = new Intent(BorrowPop.this, ContentActivity.class);
                        intent.putExtra("PDF_URL", pdfUrl);
                        intent.putExtra("CHAPTER_INDEX", position);
                        startActivity(intent);
                    });
                    chaptersRecyclerView.setAdapter(adapter);
                    Toast.makeText(BorrowPop.this, "Chapters/topics extracted!", Toast.LENGTH_SHORT).show();
                });

                pdfDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(BorrowPop.this, "Error extracting PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        } else if (borrowedBookCount >= 10) {
                            Toast.makeText(BorrowPop.this, "You cannot borrow more than 5 books", Toast.LENGTH_SHORT).show();
                        } else {
                            storeBorrowingDetails(name, days);
                        }
                    } else {
                        Log.e(TAG, "Error getting borrowed books: ", task.getException());
                    }
                });
    }

    private void storeBorrowingDetails(String name, int days) {
        // Get the current time in milliseconds (timestamp)
        long currentDateMillis = System.currentTimeMillis(); // Get the current date as a long (timestamp)

        // Calculate the return date by adding the number of days to the current date
        long returnDateMillis = currentDateMillis + (days * 24L * 60 * 60 * 1000); // Convert days to milliseconds

        // Format the current date and return date into a readable format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String borrowedDate = sdf.format(new Date(currentDateMillis)); // Current date as string
        String returnDate = sdf.format(new Date(returnDateMillis)); // Return date as string

        // Create a map to store borrowing details
        Map<String, Object> borrowData = new HashMap<>();
        borrowData.put("name", name);
        borrowData.put("days", days);  // Store the number of days directly as an integer
        borrowData.put("bookId", bookId);
        borrowData.put("bookTitle", bookTitle);
        borrowData.put("pdfUrl", pdfUrl);
        borrowData.put("thumbnailUrl", thumbnailUrl);
        borrowData.put("author", author);
        borrowData.put("description", description);
        borrowData.put("price", price);
        borrowData.put("dateBorrowed", borrowedDate); // Store the formatted borrowing date
        borrowData.put("returnDateMillis", returnDateMillis); // Store the return date as a timestamp
        borrowData.put("returnDate", returnDate); // Store the formatted return date

        // Add the borrowData to Firestore
        db.collection("users").document(userId).collection("borrowedBooks").add(borrowData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BorrowPop.this, "Book borrowed successfully", Toast.LENGTH_SHORT).show();
                    readButton.setVisibility(View.VISIBLE);  // Show the read button
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