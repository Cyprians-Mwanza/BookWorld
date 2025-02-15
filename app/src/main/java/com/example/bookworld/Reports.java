package com.example.bookworld;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Reports extends AppCompatActivity {

    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private EditText fromDateInput, toDateInput;
    private TextView totalRevenueText, numBooksSoldText, numActiveUsersText;
    private ListView salesTrendList, topBooksList;
    private Button downloadReportButton;
    private List<BorrowedBook> borrowedBooks;
    private List<BoughtBook> boughtBooks;
    private List<String> salesTrends;
    private List<String> topBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = FirebaseFirestore.getInstance();
        borrowedBooks = new ArrayList<>();
        boughtBooks = new ArrayList<>();
        salesTrends = new ArrayList<>();
        topBooks = new ArrayList<>();

        fromDateInput = findViewById(R.id.from_date);
        toDateInput = findViewById(R.id.to_date);
        totalRevenueText = findViewById(R.id.totalRevenue);
        numBooksSoldText = findViewById(R.id.numBooksSold);
        numActiveUsersText = findViewById(R.id.numActiveUsers);
        salesTrendList = findViewById(R.id.salesTrendList);
        topBooksList = findViewById(R.id.topBooksList);
        downloadReportButton = findViewById(R.id.download_report);

        fetchReportData();
        fetchBorrowedBooks();
        fetchBoughtBooks();

        fromDateInput.setOnClickListener(v -> showDatePicker(fromDateInput));
        toDateInput.setOnClickListener(v -> showDatePicker(toDateInput));
        downloadReportButton.setOnClickListener(v -> downloadReport());

        Log.d("Reports", "Activity Initialized Successfully");
    }

    private void fetchReportData() {
    }

    private void fetchBorrowedBooks() {
        db.collection("borrowedBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BorrowedBook borrowedBook = document.toObject(BorrowedBook.class);
                    borrowedBooks.add(borrowedBook);
                    Log.d("BorrowedBooks", "Fetched: " + borrowedBook.getBookTitle());
                }
            } else {
                Log.e("BorrowedBooks", "Error fetching borrowed books");
            }
        });
    }

    private void fetchBoughtBooks() {
        db.collection("boughtBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BoughtBook boughtBook = document.toObject(BoughtBook.class);
                    boughtBooks.add(boughtBook);
                    Log.d("BoughtBooks", "Fetched: " + boughtBook.getBookTitle());
                }
            } else {
                Log.e("BoughtBooks", "Error fetching bought books");
            }
        });
    }

    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dateInput.setText(date);
            Log.d("DatePicker", "Selected date: " + date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateListView(ListView listView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
    }

    private void downloadReport() {
        try {
            File file = new File(getExternalFilesDir(null), "report.csv");
            FileWriter writer = new FileWriter(file);
            writer.append("Total Revenue,Books Sold,Active Users\n");
            writer.append(totalRevenueText.getText().toString().replace("Total Revenue: $", ""));
            writer.append(",");
            writer.append(numBooksSoldText.getText().toString().replace("Books Sold: ", ""));
            writer.append(",");
            writer.append(numActiveUsersText.getText().toString().replace("Active Users: ", ""));
            writer.append("\n");
            writer.flush();
            writer.close();
            Toast.makeText(this, "Report saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("Reports", "Error writing report file", e);
        }
    }
}
