package com.example.bookworld;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.BorrowedBook;
import com.example.bookworld.bookdata.BoughtBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private TableLayout borrowedBooksTable, boughtBooksTable;
    private Button downloadReportButton;
    private List<String> salesTrends;
    private List<String> topBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = FirebaseFirestore.getInstance();
        salesTrends = new ArrayList<>();
        topBooks = new ArrayList<>();

        fromDateInput = findViewById(R.id.from_date);
        toDateInput = findViewById(R.id.to_date);
        totalRevenueText = findViewById(R.id.totalRevenue);
        numBooksSoldText = findViewById(R.id.numBooksSold);
        numActiveUsersText = findViewById(R.id.numActiveUsers);
        salesTrendList = findViewById(R.id.salesTrendList);
        topBooksList = findViewById(R.id.topBooksList);
        borrowedBooksTable = findViewById(R.id.borrowedBooksTable);
        boughtBooksTable = findViewById(R.id.boughtBooksTable);
        downloadReportButton = findViewById(R.id.download_report);

        fetchReportData();
        fetchBorrowedBooks();
        fetchBoughtBooks();
        fetchSalesTrends();
        fetchTopBooks();

        fromDateInput.setOnClickListener(v -> showDatePicker(fromDateInput));
        toDateInput.setOnClickListener(v -> showDatePicker(toDateInput));
        downloadReportButton.setOnClickListener(v -> downloadReport());

        Log.d("Reports", "Activity Initialized Successfully");
    }

    private void fetchReportData() {
        db.collection("reportData").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalRevenue = 0;
                int booksSold = 0;
                int activeUsers = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    totalRevenue += document.getLong("totalRevenue") != null ? document.getLong("totalRevenue").intValue() : 0;
                    booksSold += document.getLong("booksSold") != null ? document.getLong("booksSold").intValue() : 0;
                    activeUsers += document.getLong("activeUsers") != null ? document.getLong("activeUsers").intValue() : 0;
                }
                totalRevenueText.setText("Total Revenue: $" + totalRevenue);
                numBooksSoldText.setText("Books Sold: " + booksSold);
                numActiveUsersText.setText("Active Users: " + activeUsers);
            }
        });
    }

    private void fetchBorrowedBooks() {
        db.collection("borrowedBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BorrowedBook book = document.toObject(BorrowedBook.class);
                    addTableRow(borrowedBooksTable, new String[]{book.getBorrower(), book.getBookTitle(), book.getDateBorrowed(), String.valueOf(book.getDaysBorrowed())});
                }
            }
        });
    }

    private void fetchBoughtBooks() {
        db.collection("boughtBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BoughtBook book = document.toObject(BoughtBook.class);
                    addTableRow(boughtBooksTable, new String[]{book.getBuyer(), book.getBookTitle(), book.getDateOfPurchase(), String.valueOf(book.getPrice())});
                }
            }
        });
    }

    private void fetchSalesTrends() {
        db.collection("salesTrends").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    salesTrends.add(document.getString("trend"));
                }
                updateListView(salesTrendList, salesTrends);
            }
        });
    }

    private void fetchTopBooks() {
        db.collection("topBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    topBooks.add(document.getString("title"));
                }
                updateListView(topBooksList, topBooks);
            }
        });
    }

    private void addTableRow(TableLayout tableLayout, String[] data) {
        TableRow row = new TableRow(this);
        for (String item : data) {
            TextView textView = new TextView(this);
            textView.setText(item);
            textView.setPadding(10, 10, 10, 10);
            row.addView(textView);
        }
        tableLayout.addView(row);
    }

    private void updateListView(ListView listView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
    }

    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dateInput.setText(date);
            Log.d("DatePicker", "Selected date: " + date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void downloadReport() {
        Toast.makeText(this, "Downloading report...", Toast.LENGTH_SHORT).show();
    }
}
