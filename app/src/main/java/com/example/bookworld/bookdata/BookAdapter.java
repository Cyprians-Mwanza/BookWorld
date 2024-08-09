package com.example.bookworld.bookdata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.BookDetails;
import com.example.bookworld.Home;
import com.example.bookworld.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;

    public BookAdapter(List<Book> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText("by " + book.getAuthor());
        holder.price.setText("Ksh " + book.getPrice()); // Set price directly as a string
        Picasso.get().load(book.getThumbnailUrl()).into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to start BookDetailsActivity
                Intent intent = new Intent(context, BookDetails.class);
                intent.putExtra("BOOK_ID", book.getId());
                intent.putExtra("BOOK_TITLE", book.getTitle());
                intent.putExtra("BOOK_AUTHOR", book.getAuthor());
                intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
                intent.putExtra("BOOK_PRICE", book.getPrice());
                intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
                intent.putExtra("BOOK_RATING", book.getRating());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void setOnBookClickListener(Home home) {

    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView author;
        TextView price;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.bookThumbnail);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            price = itemView.findViewById(R.id.bookPrice); // Reference to book price TextView in item layout
        }
    }
}