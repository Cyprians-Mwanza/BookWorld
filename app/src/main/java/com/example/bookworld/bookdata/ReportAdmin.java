package com.example.bookworld.bookdata;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookworld.R;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReportAdmin extends AppCompatActivity {

    private TextView reportTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_admin);

        // Initialize TextViews
        TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
        TextView totalBooksBorrowedTextView = findViewById(R.id.totalBooksBorrowedTextView);
        TextView totalBorrowersTextView = findViewById(R.id.totalBorrowersTextView);
        TextView topBooksTextView = findViewById(R.id.topBooksTextView);
        TextView topUsersTextView = findViewById(R.id.topUsersTextView);
        TextView topGenresTextView = findViewById(R.id.topGenresTextView);

        // Retrieve data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            totalAmountTextView.setText(intent.getStringExtra("TOTAL_AMOUNT"));
            totalBooksBorrowedTextView.setText(intent.getStringExtra("TOTAL_BOOKS_BORROWED"));
            totalBorrowersTextView.setText(intent.getStringExtra("TOTAL_BORROWERS"));
            topBooksTextView.setText(intent.getStringExtra("TOP_BOOKS"));
            topUsersTextView.setText(intent.getStringExtra("TOP_USERS"));
            topGenresTextView.setText(intent.getStringExtra("TOP_GENRES"));
        }
    }


    // Method to save the report as a text file
    private void downloadReport(String reportContent) {
        try {
            // Create a file in the Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File reportFile = new File(downloadsDir, "report.txt");

            // Write the report content to the file
            FileOutputStream fos = new FileOutputStream(reportFile);
            fos.write(reportContent.getBytes());
            fos.close();

            // Notify the user
            Toast.makeText(this, "Report downloaded successfully", Toast.LENGTH_SHORT).show();

            // Optionally, open the file using an Intent
            Uri uri = Uri.fromFile(reportFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error downloading report", Toast.LENGTH_SHORT).show();
        }
    }
}
