package com.example.bookworld;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.BorrowedBook;
import com.example.bookworld.bookdata.BoughtBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Reports extends AppCompatActivity {

    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private EditText fromDateInput, toDateInput;
    private TextView totalRevenueText, numBooksSoldText, numActiveUsersText;
    private ListView salesTrendList, topBooksList;
    private Button downloadReportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = FirebaseFirestore.getInstance();

        fromDateInput = findViewById(R.id.from_date);
        toDateInput = findViewById(R.id.to_date);
        totalRevenueText = findViewById(R.id.totalRevenue);
        numBooksSoldText = findViewById(R.id.numBooksSold);
        numActiveUsersText = findViewById(R.id.numActiveUsers);
        salesTrendList = findViewById(R.id.salesTrendList);
        topBooksList = findViewById(R.id.topBooksList);
        downloadReportButton = findViewById(R.id.download_report);

        fetchReportData();

        fromDateInput.setOnClickListener(v -> showDatePicker(fromDateInput));
        toDateInput.setOnClickListener(v -> showDatePicker(toDateInput));
        downloadReportButton.setOnClickListener(v -> downloadReport());
    }

    private void fetchReportData() {
        db.collection("reportData").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalRevenue = 0;
                int booksSold = 0;
                int activeUsers = 0;
                List<String> salesTrends = new ArrayList<>();
                List<String> topBooks = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    totalRevenue += document.getLong("totalRevenue");
                    booksSold += document.getLong("booksSold");
                    activeUsers += document.getLong("activeUsers");
                    salesTrends.add(document.getString("salesTrend"));
                    topBooks.add(document.getString("topBook"));
                }

                totalRevenueText.setText("Total Revenue: $" + totalRevenue);
                numBooksSoldText.setText("Books Sold: " + booksSold);
                numActiveUsersText.setText("Active Users: " + activeUsers);
                updateListView(salesTrendList, salesTrends);
                updateListView(topBooksList, topBooks);
            } else {
                Toast.makeText(Reports.this, "Error fetching report data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dateInput.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateListView(ListView listView, List<String> data) {
        CustomListAdapter adapter = new CustomListAdapter(this, data);
        listView.setAdapter(adapter);
    }

    private void downloadReport() {
        Toast.makeText(this, "Downloading report...", Toast.LENGTH_SHORT).show();
        // Logic to generate and download report file
    }
}
