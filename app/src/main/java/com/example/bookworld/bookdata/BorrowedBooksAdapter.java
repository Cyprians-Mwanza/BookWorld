package com.example.bookworld.bookdata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookworld.R;
import java.util.List;

public class BorrowedBooksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW_TYPE = 0;
    private static final int ITEM_VIEW_TYPE = 1;

    private List<BorrowedBooks> borrowedBooksList;

    // Constructor
    public BorrowedBooksAdapter(List<BorrowedBooks> borrowedBooksList) {
        this.borrowedBooksList = borrowedBooksList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_VIEW_TYPE;  // The first item is the header
        } else {
            return ITEM_VIEW_TYPE;  // Other items are the list items
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_VIEW_TYPE) {
            // Inflate header layout
            View headerView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.table_row_header, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            // Inflate item layout
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_borrowed_book, parent, false);
            return new BorrowedBookViewHolder(itemView);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BorrowedBookViewHolder) {
            // Adjust position for header row
            BorrowedBooks book = borrowedBooksList.get(position - 1);

            BorrowedBookViewHolder itemHolder = (BorrowedBookViewHolder) holder;
            itemHolder.borrowerTextView.setText(book.getName());
            itemHolder.bookTitleTextView.setText(book.getBookTitle());
            itemHolder.priceTextView.setText(book.getPrice());
            itemHolder.returnDateTextView.setText(book.getReturnDate());
        }
    }


    // Return the size of the dataset (including the header)
    @Override
    public int getItemCount() {
        return borrowedBooksList.size() + 1;  // +1 for the header row
    }

    // Update the data in the adapter
    public void updateData(List<BorrowedBooks> newBorrowedBooksList) {
        borrowedBooksList.clear();
        borrowedBooksList.addAll(newBorrowedBooksList);
        notifyDataSetChanged();
    }

    // ViewHolder for the header
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        // You can initialize header views here if needed
        public HeaderViewHolder(View view) {
            super(view);
            // Example: view.findViewById(R.id.header_item);
        }
    }

    // ViewHolder class for each item in the RecyclerView
    public static class BorrowedBookViewHolder extends RecyclerView.ViewHolder {
        public TextView borrowerTextView;
        public TextView bookTitleTextView;
        public TextView priceTextView;
        public TextView returnDateTextView;

        public BorrowedBookViewHolder(View view) {
            super(view);
            borrowerTextView = view.findViewById(R.id.borrower);
            bookTitleTextView = view.findViewById(R.id.title);
            priceTextView = view.findViewById(R.id.price);
            returnDateTextView = view.findViewById(R.id.returnDate);
        }
    }
}
