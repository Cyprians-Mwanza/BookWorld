package com.example.bookworld.bookdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context mContext;
    private List<Book> mCartItems;

    public CartAdapter(Context context, List<Book> cartItems) {
        mContext = context;
        mCartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cart_item, parent, false);
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

    public class CartViewHolder extends RecyclerView.ViewHolder {

        private ImageView thumbnailImageView;
        private TextView titleTextView;
        private TextView authorTextView;
        private TextView priceTextView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.bookThumbnail);
            titleTextView = itemView.findViewById(R.id.bookTitle);
            authorTextView = itemView.findViewById(R.id.bookAuthor);
            priceTextView = itemView.findViewById(R.id.bookPrice);
        }

        public void bind(int position) {
            if (position < mCartItems.size()) {
                Book book = mCartItems.get(position);
                if (book != null) {
                    Picasso.get().load(book.getThumbnailUrl()).into(thumbnailImageView);
                    if (titleTextView != null) {
                        titleTextView.setText(book.getTitle());
                    }
                    if (authorTextView != null) {
                        authorTextView.setText("by " + book.getAuthor());
                    }
                    if (priceTextView != null) {
                        priceTextView.setText("Ksh " + book.getPrice());
                    }
                }
            }
        }
    }
}
