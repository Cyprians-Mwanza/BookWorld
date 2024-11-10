package com.example.bookworld.bookdata;

import android.os.CountDownTimer;
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

import com.example.bookworld.R;
import com.example.bookworld.ReturnBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReturnBooksAdapter extends RecyclerView.Adapter<ReturnBooksAdapter.CartViewHolder> {

    private final Context mContext;
    private final List<Book> mCartItems;
    private final FirebaseFirestore mFirestore;
    private final String userId;

    public ReturnBooksAdapter(Context context, List<Book> cartItems, String userId) {
        this.mContext = context;
        this.mCartItems = cartItems;
        this.mFirestore = FirebaseFirestore.getInstance();
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
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {

        private final ImageView thumbnailImageView;
        private final TextView titleTextView;
        private final TextView authorTextView;
        private final TextView priceTextView;
        private final TextView daysCountTextView;
        private final ImageButton deleteButton;
        private CountDownTimer countDownTimer;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.bookThumbnail);
            titleTextView = itemView.findViewById(R.id.bookTitle);
            authorTextView = itemView.findViewById(R.id.bookAuthor);
            priceTextView = itemView.findViewById(R.id.bookPrice);
            daysCountTextView = itemView.findViewById(R.id.daysCountTextView);
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

                    // Initialize countdown timer using daysToBorrow as an int
                    startCountdownTimer(book.getDaysToBorrow());

                    // Adding click listener to navigate to ReturnBook activity
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

        // Updated method to accept an int
        private void startCountdownTimer(int daysToBorrow) {
            if (countDownTimer != null) {
                countDownTimer.cancel(); // Cancel any existing timer to prevent multiple timers on the same view
            }

            // Ensure daysToBorrow is positive, else no countdown
            if (daysToBorrow > 0) {
                long countdownMillis = daysToBorrow * 24L * 60 * 60 * 1000; // Convert days to milliseconds

                countDownTimer = new CountDownTimer(countdownMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                        long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                        // Display the countdown as days, hours, minutes, and seconds left
                        daysCountTextView.setText(days + " days, " + hours + " hrs, " + minutes + " mins, " + seconds + " secs left");
                    }

                    @Override
                    public void onFinish() {
                        daysCountTextView.setText("Expired");
                    }
                }.start();
            } else {
                daysCountTextView.setText("Expired"); // If daysToBorrow is 0 or negative, display expired immediately
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
