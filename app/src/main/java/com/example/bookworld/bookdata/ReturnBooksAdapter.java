package com.example.bookworld.bookdata;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.ContentActivity;
import com.example.bookworld.R;
import com.example.bookworld.ReturnBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ReturnBooksAdapter extends RecyclerView.Adapter<ReturnBooksAdapter.CartViewHolder> {

    private Context mContext;
    private List<Book> mCartItems;
    private FirebaseFirestore mFirestore;
    private String userId;

    public ReturnBooksAdapter(Context context, List<Book> cartItems, String userId) {
        mContext = context;
        mCartItems = cartItems;
        mFirestore = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_borrow, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mCartItems.size();
    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {

        private ImageView thumbnailImageView;
        private TextView titleTextView;
        private TextView authorTextView;
        private TextView priceTextView;
        private ImageButton deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.bookThumbnail);
            titleTextView = itemView.findViewById(R.id.bookTitle);
            authorTextView = itemView.findViewById(R.id.bookAuthor);
            priceTextView = itemView.findViewById(R.id.bookPrice);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(int position) {
            if (position < mCartItems.size()) {
                Book book = mCartItems.get(position);
                if (book != null) {
                    Picasso.get().load(book.getThumbnailUrl()).into(thumbnailImageView);
                    titleTextView.setText(book.getTitle());
                    authorTextView.setText("by " + book.getAuthor());
                    priceTextView.setText("Ksh " + book.getPrice());

                    deleteButton.setOnClickListener(v -> {
                        removeItem(position, book);
                    });

                    // Adding click listener to the whole item view to navigate to ReturnBooks activity
                    itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(mContext, ReturnBook.class);
                        intent.putExtra("BOOK_ID", book.getId());
                        intent.putExtra("BOOK_TITLE", book.getTitle());
                        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
                        intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
                        intent.putExtra("BOOK_PRICE", book.getPrice());
                        intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
                        intent.putExtra("BOOK_RATING", book.getRating());
                        intent.putExtra("PDF_URL", book.getPdfUrl());
                        mContext.startActivity(intent);
                    });
                }
            }
        }
    }

    private void removeItem(int position, Book book) {
        mCartItems.remove(position);
        notifyItemRemoved(position);

        // Remove the book from Firestore using the book's ID
        mFirestore.collection("users")
                .document(userId)
                .collection("borrowedBooks")
                .document(book.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Item successfully removed from Firestore
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}
