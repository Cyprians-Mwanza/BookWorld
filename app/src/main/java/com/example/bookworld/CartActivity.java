package com.example.bookworld;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.CartAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartActivity extends AppCompatActivity {
    private String userId;
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<Book> cartItemList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView checkouttext;
    private CheckBox selectAllCheckBox;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference cartRef;

    private Set<Book> selectedBooks;
    private boolean selectAllChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        checkouttext = findViewById(R.id.checkout_text);
        selectAllCheckBox = findViewById(R.id.select_checkbox);

        // Set GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        cartRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize cartItemList and selectedBooks
        cartItemList = new ArrayList<>();
        selectedBooks = new HashSet<>();

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            handleUserNotAuthenticated();
            return;
        }
        cartRef = db.collection("users").document(userId).collection("cartItems");

        // Initialize and set up CartAdapter
        cartAdapter = new CartAdapter(CartActivity.this, cartItemList, selectedBooks);
        cartRecyclerView.setAdapter(cartAdapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshCart);

        // Listen for changes in the cart collection
        cartRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    cartItemList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Book book = doc.toObject(Book.class);
                        cartItemList.add(book);
                    }
                    cartAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                }
            }
        });

        // Checkout selected books on button click
        checkouttext.setOnClickListener(v -> checkoutSelectedBooks());

        // Select all or deselect all books in cart
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedBooks.clear(); // Clear current selections
            if (isChecked) {
                selectedBooks.addAll(cartItemList); // Add all items to selectedBooks
                selectAllChecked = true; // Mark select all as checked
            } else {
                selectAllChecked = false; // Mark select all as unchecked
            }
            cartAdapter.setSelectAllChecked(selectAllChecked); // Update the adapter's select all state
            cartAdapter.notifyDataSetChanged(); // Notify the adapter to update UI
        });
    }

    // Refresh the cart data
    private void refreshCart() {
        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    cartItemList.clear();
                    cartItemList.addAll(querySnapshot.toObjects(Book.class)); // Update list with new data
                    cartAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                }
                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            } else {
                Toast.makeText(CartActivity.this, "Failed to refresh cart.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            }
        });
    }

    // Handle checkout for selected books
    private void checkoutSelectedBooks() {
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No items selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a list to hold IDs of books to be deleted
        List<String> bookIdsToDelete = new ArrayList<>();

        // Prepare the list of IDs to delete
        for (Book book : selectedBooks) {
            bookIdsToDelete.add(book.getId());
        }

        // Delete all selected books from Firestore in one batch operation
        db.runBatch(batch -> {
            for (String bookId : bookIdsToDelete) {
                // Reference to each cart item document
                batch.delete(cartRef.document(bookId));
            }
        }).addOnSuccessListener(aVoid -> {
            // Clear selected books after successful deletion
            selectedBooks.clear();
            selectAllChecked = false; // Reset select all state
            cartAdapter.notifyDataSetChanged();
            Toast.makeText(CartActivity.this, "Books checked out", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(CartActivity.this, "Error during checkout", Toast.LENGTH_SHORT).show();
        });
    }

    // Handle the case where the user is not authenticated
    private void handleUserNotAuthenticated() {
        // Implement your logic to handle user not being authenticated
        Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity if the user is not authenticated
    }
}
