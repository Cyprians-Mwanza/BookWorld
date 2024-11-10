package com.example.bookworld;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddBookActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_PDF_REQUEST = 2;
    private ImageView thumbnailImageView;
    private EditText genreEditText;
    private EditText priceEditText;
    private ProgressBar progressBar;
    private Switch saleSwitch; // Declare the Switch

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri thumbnailUri;
    private Uri pdfUri;

    private String selectedGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Button selectThumbnailButton = findViewById(R.id.thumbnail_button);
        selectThumbnailButton.setOnClickListener(v -> openImagePicker());
        thumbnailImageView = findViewById(R.id.thumbnail_image);

        genreEditText = findViewById(R.id.genreEditText);
        priceEditText = findViewById(R.id.book_price);
        progressBar = findViewById(R.id.progress_bar);
        saleSwitch = findViewById(R.id.sale_switch); // Initialize the Switch

        // Set up the Switch to control visibility of the price EditText
        saleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                priceEditText.setVisibility(View.VISIBLE); // Show price EditText
            } else {
                priceEditText.setVisibility(View.GONE); // Hide price EditText
                priceEditText.setText(""); // Clear the price field if hidden
            }
        });

        setupGenreEditText();

        Button selectPdfButton = findViewById(R.id.book_file_button);
        selectPdfButton.setOnClickListener(v -> openPdfPicker());

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> uploadData());

        ImageView threeDotButton = findViewById(R.id.three_dotButton);
        ImageView backButton = findViewById(R.id.backButton);

        threeDotButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddBookActivity.this, three_dots.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Thumbnail Image"), PICK_IMAGE_REQUEST);
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF Book File"), PICK_PDF_REQUEST);
    }

    private void uploadData() {
        String title = ((EditText) findViewById(R.id.book_title)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.book_desc)).getText().toString().trim();
        String author = ((EditText) findViewById(R.id.book_author)).getText().toString().trim();
        String genre = genreEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();

        // Check if required fields are filled
        if (title.isEmpty() || description.isEmpty() || author.isEmpty() || thumbnailUri == null || pdfUri == null) {
            Toast.makeText(this, "Please fill in all fields and select both thumbnail image and PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the book is for sale, ensure the price is provided
        if (saleSwitch.isChecked() && priceString.isEmpty()) {
            Toast.makeText(this, "Please enter a price for the book", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the loading icon
        progressBar.setVisibility(View.VISIBLE);

        // Check for duplicates
        db.collection(genre)
                .whereEqualTo("title", title)
                .whereEqualTo("author", author)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Book already exists
                        Toast.makeText(AddBookActivity.this, "Book title with this author already exists", Toast.LENGTH_SHORT).show();
                        // Hide the loading icon
                        progressBar.setVisibility(View.GONE);
                    } else {
                        // Book does not exist, proceed to upload
                        uploadThumbnailToFirebase(genre, thumbnailUri, title, author, description, priceString);
                    }
                });
    }

    private void uploadThumbnailToFirebase(String genre, Uri thumbnailUri, String title, String author, String description, String price) {
        StorageReference thumbnailRef = storageRef.child(genre + "/" + title + "_thumbnail.jpg");
        UploadTask uploadTask = thumbnailRef.putFile(thumbnailUri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                thumbnailRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String thumbnailUrl = uri.toString();
                    uploadPdfToFirebase(genre, pdfUri, title, author, description, thumbnailUrl, price);
                });
            } else {
                Toast.makeText(AddBookActivity.this, "Failed to upload thumbnail image", Toast.LENGTH_SHORT).show();
                // Hide the loading icon
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void uploadPdfToFirebase(String genre, Uri pdfUri, String title, String author, String description, String thumbnailUrl, String price) {
        StorageReference pdfRef = storageRef.child(genre + "/" + title + ".pdf");
        UploadTask uploadTask = pdfRef.putFile(pdfUri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String pdfUrl = uri.toString();
                    saveBookDetails(genre, title, author, description, thumbnailUrl, pdfUrl, price);
                });
            } else {
                Toast.makeText(AddBookActivity.this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
                // Hide the loading icon
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveBookDetails(String genre, String title, String author, String description, String thumbnailUrl, String pdfUrl, String price) {
        Map<String, Object> book = new HashMap<>();
        book.put("title", title);
        book.put("author", author);
        book.put("description", description);
        book.put("thumbnailUrl", thumbnailUrl);
        book.put("pdfUrl", pdfUrl);
        book.put("price", saleSwitch.isChecked() ? price : "Not for sale"); // Save price only if book is for sale

        db.collection(genre)  // Store book details in a genre-specific collection
                .add(book)
                .addOnCompleteListener(task -> {
                    // Hide the loading icon
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(AddBookActivity.this, "Book added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddBookActivity.this, "Failed to add book", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupGenreEditText() {
        genreEditText.setOnClickListener(view -> {
            showGenrePopupMenu();
        });
    }

    private void showGenrePopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, genreEditText);
        String[] genres = getResources().getStringArray(R.array.genre_array);
        for (String genre : genres) {
            popupMenu.getMenu().add(genre);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            selectedGenre = item.getTitle().toString();
            genreEditText.setText(selectedGenre);
            return true;
        });
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                thumbnailUri = data.getData();
                Glide.with(this).load(thumbnailUri).into(thumbnailImageView);
            } else if (requestCode == PICK_PDF_REQUEST) {
                pdfUri = data.getData();
            }
        }
    }
}
