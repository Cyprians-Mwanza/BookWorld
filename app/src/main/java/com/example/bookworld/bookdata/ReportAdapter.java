package com.example.bookworld.bookdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.BookViewHolder> {

    private Context context;
    private List<ReportClass> bookItems;

    public ReportAdapter(Context context, List<ReportClass> bookItems) {
        this.context = context;
        this.bookItems = bookItems;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        ReportClass bookItem = bookItems.get(position);
        holder.nameTextView.setText(bookItem.getName());
        holder.titleTextView.setText(bookItem.getTitle());
        holder.priceTextView.setText("Price: " + bookItem.getPrice());
    }

    @Override
    public int getItemCount() {
        return bookItems.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, titleTextView, priceTextView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }
    }
}
