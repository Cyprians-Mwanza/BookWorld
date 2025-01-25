package com.example.bookworld;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.bookdata.BorrowedBook;
import com.example.bookworld.bookdata.BoughtBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Reports extends AppCompatActivity {

    private TableLayout borrowedBooksTable, boughtBooksTable;
    private ImageView filterButtonFrom, filterButtonTo;
    private EditText fromDateInput, toDateInput;
    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        borrowedBooksTable = findViewById(R.id.borrowed_books_table);
        boughtBooksTable = findViewById(R.id.bought_books_table);
        filterButtonFrom = findViewById(R.id.filter_from_button);
        filterButtonTo = findViewById(R.id.filter_to_button);
        fromDateInput = findViewById(R.id.from_date);
        toDateInput = findViewById(R.id.to_date);

        // Add headers to the tables
        addHeaders(borrowedBooksTable, new String[]{"Borrower", "Book Title", "Days", "Author"});
        addHeaders(boughtBooksTable, new String[]{"Book Title", "Author", "Buyer", "Price"});

        // Fetch all books initially
        fetchBooksDataForAllUsers();

        // Set up date picker buttons
        filterButtonFrom.setOnClickListener(v -> showDatePicker(fromDateInput));
        filterButtonTo.setOnClickListener(v -> showDatePicker(toDateInput));

        // Trigger filtering when "To Date" is modified
        toDateInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String fromDateString = fromDateInput.getText().toString();
                String toDateString = editable.toString();

                if (!fromDateString.isEmpty() && !toDateString.isEmpty()) {
                    filterBooksByDateRange(fromDateString, toDateString);
                }
            }
        });
    }

    private void fetchBooksDataForAllUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            String userId = userDocument.getId();
                            fetchUserBorrowedBooks(userId);
                            fetchUserBoughtBooks(userId);
                        }
                    } else {
                        Toast.makeText(Reports.this, "Error fetching users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserBorrowedBooks(String userId) {
        db.collection("users")
                .document(userId)
                .collection("borrowedBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BorrowedBook borrowedBook = document.toObject(BorrowedBook.class);
                            addBorrowedBookRow(borrowedBook);
                        }
                    } else {
                        Toast.makeText(Reports.this, "Error fetching borrowed books for user: " + userId, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserBoughtBooks(String userId) {
        db.collection("users")
                .document(userId)
                .collection("BoughtBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BoughtBook boughtBook = document.toObject(BoughtBook.class);
                            addBoughtBookRow(boughtBook);
                        }
                    } else {
                        Toast.makeText(Reports.this, "Error fetching bought books for user: " + userId, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterBooksByDateRange(String fromDateString, String toDateString) {
        try {
            Date fromDate = dateFormat.parse(fromDateString);
            Date toDate = dateFormat.parse(toDateString);

            borrowedBooksTable.removeAllViews();
            boughtBooksTable.removeAllViews();
            addHeaders(borrowedBooksTable, new String[]{"Borrower", "Book Title", "Days", "Author"});
            addHeaders(boughtBooksTable, new String[]{"Buyer", "Book Title", "Price", "Author"});

            db.collection("users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot userDocument : task.getResult()) {
                                String userId = userDocument.getId();
                                fetchFilteredBorrowedBooks(userId, fromDate, toDate);
                                fetchFilteredBoughtBooks(userId, fromDate, toDate);
                            }
                        } else {
                            Toast.makeText(Reports.this, "Error fetching users.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Please use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchFilteredBorrowedBooks(String userId, Date fromDate, Date toDate) {
        db.collection("users")
                .document(userId)
                .collection("borrowedBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BorrowedBook borrowedBook = document.toObject(BorrowedBook.class);
                            try {
                                Date bookDate = dateFormat.parse(borrowedBook.getDateBorrowed());
                                if (bookDate != null && !bookDate.before(fromDate) && !bookDate.after(toDate)) {
                                    addBorrowedBookRow(borrowedBook);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(Reports.this, "Error fetching filtered borrowed books.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchFilteredBoughtBooks(String userId, Date fromDate, Date toDate) {
        db.collection("users")
                .document(userId)
                .collection("BoughtBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BoughtBook boughtBook = document.toObject(BoughtBook.class);

                            // Ensure dateBought is not null or empty
                            String dateBought = boughtBook.getDateBought();
                            if (dateBought != null && !dateBought.isEmpty()) {
                                try {
                                    // Parse the date
                                    Date bookDate = dateFormat.parse(dateBought);

                                    // Filter books within the date range
                                    if (bookDate != null && !bookDate.before(fromDate) && !bookDate.after(toDate)) {
                                        addBoughtBookRow(boughtBook);
                                    }
                                } catch (ParseException e) {
                                    Log.e("DateParseError", "Error parsing date: " + dateBought, e);
                                }
                            } else {
                                Log.e("DateError", "dateBought is null or empty for document ID: " + document.getId());
                            }
                        }
                    } else {
                        Toast.makeText(Reports.this, "Error fetching filtered bought books.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addBorrowedBookRow(BorrowedBook borrowedBook) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView borrowerTextView = createStyledCell(borrowedBook.getName());
        tableRow.addView(borrowerTextView);

        TextView titleTextView = createStyledCell(borrowedBook.getBookTitle());
        tableRow.addView(titleTextView);

        TextView daysTextView = createStyledCell(String.valueOf(borrowedBook.getDays()));
        tableRow.addView(daysTextView);

        TextView authorTextView = createStyledCell(borrowedBook.getAuthor());
        tableRow.addView(authorTextView);

        borrowedBooksTable.addView(tableRow);
    }

    private void addBoughtBookRow(BoughtBook boughtBook) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView titleTextView = createStyledCell(boughtBook.getBookTitle());
        tableRow.addView(titleTextView);

        TextView authorTextView = createStyledCell(boughtBook.getAuthor());
        tableRow.addView(authorTextView);

        TextView buyerTextView = createStyledCell(boughtBook.getName());
        tableRow.addView(buyerTextView);

        TextView priceTextView = createStyledCell(String.valueOf(boughtBook.getPrice()));
        tableRow.addView(priceTextView);

        boughtBooksTable.addView(tableRow);
    }

    private void addHeaders(TableLayout tableLayout, String[] headers) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        for (String header : headers) {
            TextView headerTextView = createStyledCell(header);
            headerTextView.setTypeface(Typeface.DEFAULT_BOLD);
            headerTextView.setBackgroundColor(Color.LTGRAY);
            headerRow.addView(headerTextView);
        }

        tableLayout.addView(headerRow);
    }

    private TextView createStyledCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(10, 16, 10, 16);
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateInput.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
