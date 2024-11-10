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

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {


    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<ReportClass> reportList;
    private final Runnable updateCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            handler.postDelayed(this, 60000); // Update every minute
        }
    };

    public ReportAdapter(List<ReportClass> borrowedBooksList) {
        this.reportList = borrowedBooksList;
        handler.post(updateCountdownRunnable); // Start updating countdown
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reports_cards, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportClass borrowedBook = reportList.get(position);
        holder.bookTitleTextView.setText(borrowedBook.getBookTitle());
        holder.borrowerNameTextView.setText("Name: " + borrowedBook.getName());
        holder.daysTextView.setText(String.valueOf("Days Borrowed = " + borrowedBook.getDays()));
        holder.dateTextView.setText(String.valueOf("Date Borrowed = " + borrowedBook.getDateBorrowed()));
        Picasso.get().load(borrowedBook.getThumbnailUrl()).into(holder.bookThumbnailImageView);

        // Calculate countdown
        long remainingDays = borrowedBook.getDays(); // Adjust as needed
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
        return reportList.size();
    }

    public void updateData(List<ReportClass> newBorrowedBooksList) {
        this.reportList.clear();
        this.reportList.addAll(newBorrowedBooksList);
        notifyDataSetChanged();
    }

    private void removeBookFromUser(ReportClass borrowedBook) {
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
        public TextView dateTextView;
        public TextView countdownTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            bookThumbnailImageView = itemView.findViewById(R.id.bookThumbnailImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            borrowerNameTextView = itemView.findViewById(R.id.borrowerNameTextView); // Added TextView for borrower name
            daysTextView = itemView.findViewById(R.id.daysTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            countdownTextView = itemView.findViewById(R.id.countdownTextView); // Added TextView for countdown
        }
    }

    // Getters and Setters for the fields

    public List<ReportClass> getBorrowedBooksList() {
        return reportList;
    }

    public void setBorrowedBooksList(List<ReportClass> borrowedBooksList) {
        this.reportList = borrowedBooksList;
    }
}
