package com.example.bookworld;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ContentActivity extends AppCompatActivity {
    private static final String TAG = "ContentActivity";
    private TextView contentTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        contentTextView = findViewById(R.id.contentTextView);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        // Get the PDF URL and chapter index from the intent
        String pdfUrl = getIntent().getStringExtra("PDF_URL");
        int chapterIndex = getIntent().getIntExtra("CHAPTER_INDEX", -1);

        if (pdfUrl != null && chapterIndex != -1) {
            // Allow network operation on the main thread for simplicity (not recommended for production)
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            loadChapterContentFromPdf(pdfUrl, chapterIndex);
        } else {
            contentTextView.setText("Chapter content could not be loaded.");
        }
    }



    private void loadChapterContentFromPdf(String pdfUrl, int chapterIndex) {
        new Thread(() -> {
            InputStream inputStream = null;
            try {
                // Open the PDF file from the URL
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                inputStream = connection.getInputStream();
                PdfReader reader = new PdfReader(inputStream);
                PdfDocument pdfDoc = new PdfDocument(reader);

                StringBuilder chapterContent = new StringBuilder();
                boolean isChapterFound = false;
                int currentChapter = 0;
                boolean isNextChapterDetected = false;

                // Loop through each page in the PDF
                for (int page = 1; page <= pdfDoc.getNumberOfPages(); page++) {
                    // Extract text from the current page
                    String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(page));

                    // Split page content by lines to detect chapter sections
                    String[] lines = pageContent.split("\n");
                    for (String line : lines) {
                        // Detect the start of a new chapter
                        if (line.contains("Chapter")) {
                            if (currentChapter == chapterIndex) {
                                // Start appending the content of this chapter
                                isChapterFound = true;
                            } else if (isChapterFound) {
                                // Stop collecting content once the next chapter starts
                                isNextChapterDetected = true;
                                break;
                            }
                            currentChapter++;
                        }

                        // Append content to the chapter if we are inside the target chapter
                        if (isChapterFound) {
                            chapterContent.append(line).append("\n");
                        }
                    }

                    // Stop collecting content once the next chapter is detected
                    if (isNextChapterDetected) {
                        break;
                    }
                }

                // Update UI with the chapter content in the TextView
                runOnUiThread(() -> {
                    if (chapterContent.length() > 0) {
                        // Set the extracted chapter content to the TextView
                        contentTextView.setText(chapterContent.toString());
                    } else {
                        contentTextView.setText("Chapter content not found.");
                    }
                });

                pdfDoc.close();
            } catch (Exception e) {
                Log.e(TAG, "Error loading PDF content: ", e);
                runOnUiThread(() -> {
                    // Show an error message if the PDF cannot be loaded
                    Toast.makeText(ContentActivity.this, "Error loading chapter content: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}