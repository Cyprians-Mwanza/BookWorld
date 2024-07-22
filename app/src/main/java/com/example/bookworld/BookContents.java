package com.example.bookworld;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class BookContents extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_contents);

        // Initialize WebView
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        // Get the chapter title from the intent
        String chapterTitle = getIntent().getStringExtra("chapter_title");

        // Load the chapter content based on chapterTitle
        loadChapterContent(chapterTitle);
    }

    private void loadChapterContent(String chapterTitle) {
        // Assuming chapter content URL is stored in Firestore
        // Replace with actual logic to fetch chapter content from Firestore or any other source
        if (chapterTitle != null) {
            String chapterUrl = "https://your-book-service.com/chapters/" + chapterTitle + ".pdf";
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + chapterUrl);
        }
    }
}
