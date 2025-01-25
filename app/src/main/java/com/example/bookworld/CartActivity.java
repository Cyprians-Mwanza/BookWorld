package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<Book> cartItemList;
    private ImageView backButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference cartRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        backButton = findViewById(R.id.backButton);

        // Set GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        cartRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize cartItemList
        cartItemList = new ArrayList<>();

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        cartRef = db.collection("users").document(userId).collection("cartItems");

        // Initialize and set up CartAdapter
        cartAdapter = new CartAdapter(CartActivity.this, cartItemList, this::onBookClick, userId);
        cartRecyclerView.setAdapter(cartAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(CartActivity.this, MyBooks.class);
                startActivity(intent);
            }
        });

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
                        book.setId(doc.getId());  // Set the document ID to the book
                        cartItemList.add(book);
                    }
                    cartAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                }
            }
        });

        // Initial data load
        refreshCart();
    }

    // Method to handle book click functionality
      public void onBookClick(Book book) {
        if ("Not for Sale".equalsIgnoreCase(book.getPrice())) {
            // Navigate to BookDetails activity if the book is not for sale
            Intent intent = new Intent(CartActivity.this, BorrowPop.class);
            intent.putExtra("BOOK_ID", book.getId());
            intent.putExtra("BOOK_TITLE", book.getTitle());
            intent.putExtra("BOOK_AUTHOR", book.getAuthor());
            intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
            intent.putExtra("BOOK_PRICE", book.getPrice());
            intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
            intent.putExtra("BOOK_RATING", book.getRating());
            intent.putExtra("PDF_URL", book.getPdfUrl());
            startActivity(intent);
        } else {
            // Navigate to BuyBook activity if the book has a price
            Intent intent = new Intent(CartActivity.this, BuyActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            intent.putExtra("BOOK_TITLE", book.getTitle());
            intent.putExtra("BOOK_AUTHOR", book.getAuthor());
            intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
            intent.putExtra("BOOK_PRICE", book.getPrice());
            intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
            intent.putExtra("BOOK_RATING", book.getRating());
            intent.putExtra("PDF_URL", book.getPdfUrl());
            startActivity(intent);
        }
    }

    // Refresh the cart data
    private void refreshCart() {
        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    cartItemList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Book book = doc.toObject(Book.class);
                        book.setId(doc.getId());  // Set the document ID to the book
                        cartItemList.add(book);
                    }
                    cartAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                }
                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            } else {
                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            }
        });
    }
}
