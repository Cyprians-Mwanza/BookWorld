package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ContentActivity extends AppCompatActivity {

    private WebView webView;
    private String pdfUrl;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        webView = findViewById(R.id.webView);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(ContentActivity.this, Home.class);
                startActivity(intent);
            }
        });
        // Retrieve PDF URL from the intent
        pdfUrl = getIntent().getStringExtra("PDF_URL");

        if (pdfUrl != null) {
            setupWebView();
            // Use Google Docs Viewer to load the PDF
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + pdfUrl);
        } else {
            // Handle the error if PDF URL is not provided
            webView.loadData("Error: No PDF URL provided", "text/html", "UTF-8");
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);  // Enable zoom controls
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);  // Hide the zoom controls

        webView.setHorizontalScrollBarEnabled(true);  // Enable horizontal scrollbar
        webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Optional: Handle actions after the page is fully loaded
            }
        });
    }
}