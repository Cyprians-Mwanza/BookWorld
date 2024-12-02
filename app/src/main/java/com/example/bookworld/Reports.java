package com.example.bookworld;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookworld.bookdata.CartItem;
import com.example.bookworld.bookdata.ReportClass;
import com.example.bookworld.bookdata.ReportAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Reports extends AppCompatActivity {

    private static final String TAG = "Reports";

    private FirebaseFirestore db;
    private RelativeLayout bookLayout;
    private ImageView back;
    private TextView dateText, favourite, books_in_cart_text, cartItems;
    private ImageButton previousDay, currentDay;
    private String date;
    private ReportAdapter adapter;
    private Dialog dialog;
    private List<ReportClass> reportList;
    private ArrayList<String> orderList;
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
        ListView listView = findViewById(R.id.recyclerBooks); // Use ListView
        back = findViewById(R.id.backButton);

        // Initialize report list and adapter
        reportList = new ArrayList<>();
        adapter = new ReportAdapter(Reports.this, reportList);
        listView.setAdapter(adapter); // Set adapter for ListView

        // Dialog setup
        dialog = new MaterialAlertDialogBuilder(this)
                .setView(new ProgressBar(this))
                .setTitle("Fetching Data...")
                .setMessage("Please wait")
                .create();
        dialog.show();

        back.setOnClickListener(v -> finish());

        bookLayout.setOnClickListener(v -> {
            //navigate to low stock activity
            startActivity(new Intent(Reports.this, search_discovery.class));
        });

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
        fetchBorrowedBooks(currentDate); // Pass the current date initially

        // Set onclick listeners
        previousDay.setOnClickListener(v -> {
            count++; // Increment count for previous days
            if (count < 6) {
                // Get the base date and calculate the previous date
                LocalDate currentDate1 = LocalDate.now(); // Use today's date as the base
                LocalDate previousDate = currentDate1.minusDays(count); // Subtract count days

                // Update the date text to show the previous date
                String month = previousDate.getMonth().toString();
                month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
                int previousDay = previousDate.getDayOfMonth();
                date = previousDate.toString(); // Update the global date variable

                if (count == 1) {
                    dateText.setText(MessageFormat.format("Yesterday: {0} {1}", previousDay, month));
                } else {
                    dateText.setText(MessageFormat.format("{0} {1}", previousDay, month));
                }

                // Clear previous report data
                reportList.clear();
                adapter.clear();
                adapter.notifyDataSetChanged();

                // Fetch data for the selected previous date
                fetchBorrowedBooks(previousDate); // Pass the previous date as LocalDate
            } else {
                count = 6; // Prevent exceeding the limit
            }
        });

        currentDay.setOnClickListener(v -> {
            if (count > 0) { // Ensure count stays within bounds
                count--; // Decrement count for moving forward
                // Get the base date and calculate the next date
                LocalDate currentDate1 = LocalDate.now(); // Use today's date as the base
                LocalDate nextDate = currentDate1.minusDays(count); // Subtract count days

                // Update the date text to show the next date
                String month = nextDate.getMonth().toString();
                month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
                int nextDay = nextDate.getDayOfMonth();
                date = nextDate.toString(); // Update the global date variable

                if (count == 0) {
                    dateText.setText(MessageFormat.format("Today: {0} {1}", nextDay, month));
                } else if (count == 1) {
                    dateText.setText(MessageFormat.format("Yesterday: {0} {1}", nextDay, month));
                } else {
                    dateText.setText(MessageFormat.format("{0} {1}", nextDay, month));
                }

                // Clear previous report data
                reportList.clear();
                adapter.clear();
                adapter.notifyDataSetChanged();

                // Fetch data for the selected next date
                fetchBorrowedBooks(nextDate); // Pass the next date as LocalDate
            } else {
                count = 0; // Prevent exceeding the limit
            }
        });




    }

    private void fetchFavouriteGenre() {
        // Get the current date or any specific date for the query
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        db.collection("users")
                .document(userId)
                .collection("Favourite genre")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting favourite genres: ", error);
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        Toast.makeText(Reports.this, "No favourite genres available", Toast.LENGTH_SHORT).show();
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
                        // Create a string with numbered genres
                        StringBuilder genresList = new StringBuilder("Favourite Genres:\n");
                        for (int i = 0; i < favouriteGenres.size(); i++) {
                            genresList.append(i + 1).append(". ").append(favouriteGenres.get(i)).append("\n");
                        }

                        favourite.setText(genresList.toString());
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

                    if (!cartItemList.isEmpty()) {
                        // Create a string with numbered cart items
                        StringBuilder itemsList = new StringBuilder("Cart Items:\n");
                        for (int i = 0; i < cartItemList.size(); i++) {
                            itemsList.append(i + 1).append(". ").append(cartItemList.get(i)).append("\n");
                        }

                        cartItems.setText(itemsList.toString()); // Update the TextView with the formatted list
                    } else {
                        cartItems.setText("No items in the cart.");
                    }
                });
    }


    private void getData() {
        fetchBorrowedBooks(LocalDate.parse(date)); // Pass the selected or current date
        // Clear the list before adding new items
        reportList.clear();

        db.collection(userId)
                .document("users")
                .collection("borrowedBooks")
                .document(date)
                .collection("borrowedBooks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        // Handle the error
                        Log.e(TAG, "Error getting data: ", error);
                        return;
                    }

                    assert value != null;
                    if (value.isEmpty()) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        // Display a message if no sales data is available
                        Toast.makeText(Reports.this, "No data available for the selected date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Clear the lists before fetching data
                    reportList.clear();

                    // Add retrieved items to the list
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ReportClass reportClass = doc.toObject(ReportClass.class);
                        if (reportClass != null) {
                            reportList.add(reportClass);
                            // Add the item to the listview
                            orderList.add(Objects.requireNonNull(reportClass).getGenre() + " \n" +
                                    "Count: " + reportClass.getCart());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    // Variables to track genre and cart items count
                    int favouriteGenreCount = 0;
                    int cartItems = 0;

                    // Loop through reportList to calculate totals and count items for favourite genre and cart
                    for (ReportClass reportClass : reportList) {
                        favouriteGenreCount++;  // Increment the count of favourite genres
                        cartItems += Integer.parseInt(reportClass.getCart());  // Accumulate cart items count
                    }

                    // Update the TextViews with the accumulated totals
                    favourite.setText(MessageFormat.format("{0} Favourite Genres", favouriteGenreCount));
                    books_in_cart_text.setText(MessageFormat.format("{0} Items in Cart", cartItems));

                    // Now, fetch the favourite genres and cart items again and display them
                    fetchFavouriteGenre();  // Refresh the favourite genres list
                    getCartItems();  // Refresh the cart items list

                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });
    }



    private void fetchBorrowedBooks(LocalDate selectedDate) {
        // Convert the selected date to the "dd-MM-yyyy" format
        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()));

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
                                            // Retrieve the dateBorrowed as a String
                                            String dateBorrowed = borrowedBookSnapshot.getString("dateBorrowed");

                                            if (dateBorrowed != null) {
                                                // Compare the borrowedDate string with the selected date string
                                                if (dateBorrowed.equals(formattedDate)) {
                                                    // Only add books that match the selected date
                                                    ReportClass report = borrowedBookSnapshot.toObject(ReportClass.class);
                                                    if (report != null) {
                                                        report.setDays(borrowedBookSnapshot.getLong("days") != null ? borrowedBookSnapshot.getLong("days").intValue() : 0);
                                                        report.setBookTitle(borrowedBookSnapshot.getString("name") != null ? borrowedBookSnapshot.getString("name") : "Unknown");
                                                        report.setBookTitle(borrowedBookSnapshot.getString("bookTitle") != null ? borrowedBookSnapshot.getString("bookTitle") : "No Title");
                                                        reportList.add(report);

                                                        borrowerCounts.put(report.getBookTitle(), borrowerCounts.getOrDefault(report.getBookTitle(), 0) + 1);
                                                    }
                                                }
                                            }
                                        }

                                        if (adapter != null) {
                                            adapter.notifyDataSetChanged(); // Notify adapter of data change
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




    private void updateDateText(LocalDate date, String relativeDay) {
        String month = date.getMonth().toString();
        month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        dateText.setText(MessageFormat.format("{0}: {1} {2}", relativeDay, date.getDayOfMonth(), month));
    }

    private void resetViews() {
        reportList.clear();
        borrowerCounts.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
