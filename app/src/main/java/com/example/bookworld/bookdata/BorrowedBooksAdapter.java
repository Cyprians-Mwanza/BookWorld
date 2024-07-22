package com.example.bookworld.bookdata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;

import java.util.List;

public class BorrowedBooksAdapter extends RecyclerView.Adapter<BorrowedBooksAdapter.BookViewHolder> {

    private List<BorrowedBooks> borrowedBooks;

    public BorrowedBooksAdapter(List<BorrowedBooks> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrowed_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BorrowedBooks book = borrowedBooks.get(position);
        holder.textBookName.setText(book.getBookName());
        holder.textBorrower.setText("Borrower: " + book.getBorrowerName());
        holder.textDaysRemaining.setText(book.getDaysRemaining() + " days remaining");
    }

    @Override
    public int getItemCount() {
        return borrowedBooks.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView textBookName;
        TextView textBorrower;
        TextView textDaysRemaining;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            textBookName = itemView.findViewById(R.id.textBookTittle);
            textBorrower = itemView.findViewById(R.id.textBorrower);
            textDaysRemaining = itemView.findViewById(R.id.textDaysRemaining);
        }
    }
}
