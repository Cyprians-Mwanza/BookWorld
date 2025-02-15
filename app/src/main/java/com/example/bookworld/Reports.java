package com.example.bookworld;

import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.BorrowedBook;
import com.example.bookworld.bookdata.BoughtBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    }

    private void showDatePicker(EditText fromDateInput) {
    }

    private void fetchTopBooks() {
    }

    private void fetchSalesTrends() {
    }

    private void fetchReportData() {
        db.collection("reportData").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    totalRevenueText.setText("Total Revenue: $" + document.getString("totalRevenue"));
                    numBooksSoldText.setText("Books Sold: " + document.getString("booksSold"));
                    numActiveUsersText.setText("Active Users: " + document.getString("activeUsers"));
                }
            }
        });
    }

    private void fetchBorrowedBooks() {
        db.collection("borrowedBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                borrowedBooksTable.removeAllViews();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BorrowedBook book = document.toObject(BorrowedBook.class);
                    addTableRow(borrowedBooksTable, new String[]{book.getBorrower(), book.getBookTitle(), book.getDateBorrowed(), String.valueOf(book.getDaysBorrowed())});
                }
            }
        });
    }

    private void addTableRow(TableLayout borrowedBooksTable, String[] strings) {
    }

    private void fetchBoughtBooks() {
        db.collection("boughtBooks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boughtBooksTable.removeAllViews();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BoughtBook book = document.toObject(BoughtBook.class);
                    addTableRow(boughtBooksTable, new String[]{book.getBuyer(), book.getBookTitle(), book.getDateOfPurchase(), String.valueOf(book.getPrice())});
                }
            }
        });
    }

    private void updateListView(ListView listView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
    }

    private void downloadReport() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Report.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("Report Summary\n".getBytes());
            fos.write(("Total Revenue: " + totalRevenueText.getText().toString() + "\n").getBytes());
            fos.write(("Books Sold: " + numBooksSoldText.getText().toString() + "\n").getBytes());
            fos.write(("Active Users: " + numActiveUsersText.getText().toString() + "\n").getBytes());
            fos.close();
            Toast.makeText(this, "Report saved in Downloads folder", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
