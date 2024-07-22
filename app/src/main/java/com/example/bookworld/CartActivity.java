package com.example.bookworld;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.CartAdapter;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
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


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        cartRecyclerView = findViewById(R.id.cart_recycler_view);

        // Set GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        cartRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize cartItemList
        cartItemList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();


        db.collection("users").document(userId).collection("cartItems")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            return;
                        }
                        if (value != null){
                            cartItemList.clear();
                            for (QueryDocumentSnapshot doc : value) {
                                Book book = doc.toObject(Book.class);
                                cartItemList.add(book);
                            }
                        }

                        // Initialize and set up CartAdapter
                        cartAdapter = new CartAdapter(CartActivity.this, cartItemList);
                        cartRecyclerView.setAdapter(cartAdapter);
                    }
                });
    }
}
