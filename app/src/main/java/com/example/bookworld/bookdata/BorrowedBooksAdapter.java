package com.example.bookworld.bookdata;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BorrowedBooksAdapter extends RecyclerView.Adapter<BorrowedBooksAdapter.ViewHolder> {

    private List<BorrowedBooks> borrowedBooksList;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            handler.postDelayed(this, 60000); // Update every minute
        }
    };

    public BorrowedBooksAdapter(List<BorrowedBooks> borrowedBooksList) {
        this.borrowedBooksList = borrowedBooksList;
        handler.post(updateCountdownRunnable); // Start updating countdown
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrowed_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BorrowedBooks borrowedBook = borrowedBooksList.get(position);
        holder.bookTitleTextView.setText(borrowedBook.getBookTitle());
        holder.borrowerNameTextView.setText(borrowedBook.getName());
        holder.daysTextView.setText(String.valueOf(borrowedBook.getDays()));
        Picasso.get().load(borrowedBook.getThumbnailUrl()).into(holder.bookThumbnailImageView);

        // Calculate countdown
        long remainingDays = borrowedBook.getRemainingDays();
        if (remainingDays > 0) {
            holder.countdownTextView.setText(String.format("%d days remaining", remainingDays));
        } else {
            holder.countdownTextView.setText("Expired");
            // Optionally remove the book from the user's collection
            removeBookFromUser(borrowedBook);
        }
    }

    @Override
    public int getItemCount() {
        return borrowedBooksList.size();
    }

    public void updateData(List<BorrowedBooks> newBorrowedBooksList) {
        this.borrowedBooksList.clear();
        this.borrowedBooksList.addAll(newBorrowedBooksList);
        notifyDataSetChanged();
    }

    private void removeBookFromUser(BorrowedBooks borrowedBook) {
        // Implement logic to remove the book from the user's collection in Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userDoc = firestore.collection("users").document("userId"); // replace "userId" with actual user ID
        userDoc.collection("borrowedBooks").document(borrowedBook.getBookTitle()).delete()
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView bookThumbnailImageView;
        public TextView bookTitleTextView;
        public TextView borrowerNameTextView;
        public TextView daysTextView;
        public TextView countdownTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            bookThumbnailImageView = itemView.findViewById(R.id.bookThumbnailImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            borrowerNameTextView = itemView.findViewById(R.id.borrowerNameTextView);
            daysTextView = itemView.findViewById(R.id.daysTextView);
            countdownTextView = itemView.findViewById(R.id.countdownTextView);
        }
    }
}
