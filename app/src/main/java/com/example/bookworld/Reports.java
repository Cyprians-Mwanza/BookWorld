package com.example.bookworld;

import static com.example.bookworld.More.TAG;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.ReportAdapter;
import com.example.bookworld.bookdata.ReportClass;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reports extends AppCompatActivity {

    private static final String TAG = "Reports";

    private FirebaseFirestore db;
    private RelativeLayout bookLayout;
    private ImageView back;
    private TextView dateText, favourite, cartItems;
    private ImageButton previousDay, currentDay;
    private String date;
    private ReportAdapter adapter;
    private Dialog dialog;
    private List<ReportClass> reportList;
    private Map<String, Integer> borrowerCounts = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private FirebaseUser currentUser;
    private String userId;
    private int count = 0; // Keeps track of the date navigation state (for previous/next day)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid(); // Get the current user's ID
        } else {
            // Handle the case where the user is not logged in
            Log.e(TAG, "User not logged in");
            finish(); // Close the activity if necessary
        }

        // Initialize views
        previousDay = findViewById(R.id.next_date);
        currentDay = findViewById(R.id.previous_date);
        dateText = findViewById(R.id.date);
        bookLayout = findViewById(R.id.book_layout);
        favourite = findViewById(R.id.favourite_genres_text);
        cartItems = findViewById(R.id.books_in_cart_text);
        RecyclerView recyclerBorrowedBooks = findViewById(R.id.recyclerBooks);
        back = findViewById(R.id.backButton);

        if (recyclerBorrowedBooks != null) {
            recyclerBorrowedBooks.setLayoutManager(new GridLayoutManager(this, 2));
        }

        reportList = new ArrayList<>();
        adapter = new ReportAdapter(reportList);
        if (recyclerBorrowedBooks != null) {
            recyclerBorrowedBooks.setAdapter(adapter);
        }

        // Dialog setup
        dialog = new MaterialAlertDialogBuilder(this)
                .setView(new ProgressBar(this))
                .setTitle("Fetching Data...")
                .setMessage("Please wait")
                .create();
        dialog.show();

        back.setOnClickListener(v -> finish());

        // Get current date
        LocalDate currentDate = LocalDate.now();
        date = currentDate.toString();

        // Display the current date
        String month1 = currentDate.getMonth().toString();
        month1 = month1.substring(0, 1).toUpperCase() + month1.substring(1).toLowerCase();
        int day = currentDate.getDayOfMonth();
        dateText.setText(MessageFormat.format("Today: {0} {1}", day, month1));

        // Fetch data
        fetchFavouriteGenre();
        getCartItems();
        getData();
        fetchBorrowedBooks();

        // Set onclick listeners for navigating days
        previousDay.setOnClickListener(v -> {
            count++;
            if (count < 6) {
                LocalDate currentDate1 = LocalDate.now();
                dateText.setText("");
                for (int i = 1; i <= count; i++) {
                    LocalDate previousDate = currentDate1.minusDays(i);
                    String month = previousDate.getMonth().toString();
                    month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
                    int previousDay = previousDate.getDayOfMonth();

                    date = previousDate.toString();

                    if (i == 1) {
                        dateText.setText(MessageFormat.format("Yesterday: {0} {1}", previousDay, month));
                    } else {
                        dateText.setText(MessageFormat.format("{0} {1}", previousDay, month));
                    }
                }

                // Reset and fetch new data for the selected date
                resetViews();
                getData(); // Fetch data for the new selected date
            } else {
                count = 6;
            }
        });

        currentDay.setOnClickListener(v -> {
            count--;
            if (count >= 0) {
                LocalDate currentDate1 = LocalDate.now();
                dateText.setText("");
                for (int i = 0; i <= count; i++) {
                    LocalDate previousDate = currentDate1.minusDays(i);
                    String month = previousDate.getMonth().toString();
                    month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
                    int previousDay = previousDate.getDayOfMonth();

                    date = previousDate.toString();

                    if (i == 0) {
                        dateText.setText(MessageFormat.format("Today: {0} {1}", previousDay, month));
                    } else if (i == 1) {
                        dateText.setText(MessageFormat.format("Yesterday: {0} {1}", previousDay, month));
                    } else {
                        dateText.setText(MessageFormat.format("{0} {1}", previousDay, month));
                    }
                }

                // Reset and fetch new data for the selected date
                resetViews();
                getData(); // Fetch data for the new selected date
            } else {
                count = 0;
            }
        });
    }

    private void fetchFavouriteGenre() {
        db.collection("users")
                .document(userId)
                .collection("Favourite genre")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting favourite genres: ", error);
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        Toast.makeText(Reports.this, "No favourite genre available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> favouriteGenres = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String genre = doc.getString("genreName");
                        if (genre != null) {
                            favouriteGenres.add(genre);
                        }
                    }

                    Log.d(TAG, "Favourite genres: " + favouriteGenres);
                    if (!favouriteGenres.isEmpty()) {
                        favourite.setText(MessageFormat.format("Favourite Genres: {0}", favouriteGenres));
                    }
                });
    }

    private void getCartItems() {
        db.collection("users")
                .document(userId)
                .collection("cartItems")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting cart items: ", error);
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        Toast.makeText(Reports.this, "No cart items available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> cartItemList = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String itemName = doc.getString("itemName");
                        if (itemName != null) {
                            cartItemList.add(itemName);
                        }
                    }

                    Log.d(TAG, "Cart items: " + cartItemList);
                    cartItems.setText(MessageFormat.format("Cart Items: {0}", cartItemList.size()));
                });
    }

    private void getData() {
        db.collection("users")
                .document(userId)
                .collection("borrowedBooks" + "cartItems" + "Favourite genre")
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d(TAG, "Data for date " + date + ": " + document.getData());
                            Toast.makeText(Reports.this, "Data for " + date + " retrieved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "No data found for " + date);
                            Toast.makeText(Reports.this, "No data available for " + date, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error fetching data for " + date, task.getException());
                    }
                });
    }

    private void fetchBorrowedBooks() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    reportList.clear(); // Clear existing data
                    borrowerCounts.clear(); // Clear borrower counts

                    for (DocumentSnapshot userSnapshot : querySnapshot.getDocuments()) {
                        DocumentReference userDocRef = userSnapshot.getReference();
                        if (userDocRef != null) {
                            userDocRef.collection("borrowedBooks").get().addOnCompleteListener(borrowedBooksTask -> {
                                if (borrowedBooksTask.isSuccessful()) {
                                    QuerySnapshot borrowedBooksSnapshot = borrowedBooksTask.getResult();
                                    if (borrowedBooksSnapshot != null) {
                                        for (DocumentSnapshot borrowedBookSnapshot : borrowedBooksSnapshot.getDocuments()) {
                                            ReportClass report = borrowedBookSnapshot.toObject(ReportClass.class);  // Using ReportClass
                                            if (report != null) {
                                                // Retrieve and set the missing fields
                                                Long daysLong = borrowedBookSnapshot.getLong("days");
                                                report.setDays(daysLong != null ? daysLong.intValue() : 0); // Set days

                                                String name = borrowedBookSnapshot.getString("name");
                                                report.setName(name != null ? name : "Unknown"); // Set borrower name

                                                String title = borrowedBookSnapshot.getString("bookTitle");
                                                report.setBookTitle(title != null ? title : "No Title"); // Set book title

                                                String thumbnailUrl = borrowedBookSnapshot.getString("thumbnailUrl");
                                                report.setThumbnailUrl(thumbnailUrl != null ? thumbnailUrl : ""); // Set thumbnail URL

                                                String date = borrowedBookSnapshot.getString("dateBorrowed");
                                                report.setDateBorrowed(date != null ? date : ""); // Set borrower name

                                                reportList.add(report); // Add to report list

                                                // Update borrower counts
                                                if (name != null) {
                                                    borrowerCounts.put(name, borrowerCounts.getOrDefault(name, 0) + 1);
                                                }
                                            }
                                        }

                                        // Notify adapter of data change
                                        if (adapter != null) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Error getting borrowed books.", borrowedBooksTask.getException());
                                }
                            });
                        }
                    }
                }
            } else {
                Log.w(TAG, "Error getting users.", task.getException());
            }
        });
    }

    private void resetViews() {
        // Clear and reset all data views
        reportList.clear();
        adapter.notifyDataSetChanged();
        favourite.setText("");
        cartItems.setText("");
    }
}
