package com.example.bookworld.bookdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context mContext;
    private List<Book> mCartItems;
    private Set<Book> selectedBooks;
    private boolean selectAllChecked;

    public CartAdapter(Context context, List<Book> cartItems, Set<Book> selectedBooks) {
        mContext = context;
        mCartItems = cartItems;
        this.selectedBooks = this.selectedBooks != null ? this.selectedBooks : new HashSet<>();
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
        private CheckBox selectCheckBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.bookThumbnail);
            titleTextView = itemView.findViewById(R.id.bookTitle);
            authorTextView = itemView.findViewById(R.id.bookAuthor);
            priceTextView = itemView.findViewById(R.id.bookPrice);
            selectCheckBox = itemView.findViewById(R.id.select_checkbox);

            // Handle checkbox state changes
            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Book book = mCartItems.get(adapterPosition);
                    if (isChecked) {
                        selectedBooks.add(book);
                    } else {
                        selectedBooks.remove(book);
                    }
                }
            });

            // Set up click listener to toggle selection
            itemView.setOnClickListener(v -> {
                selectCheckBox.setChecked(!selectCheckBox.isChecked());
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Book book = mCartItems.get(adapterPosition);
                    if (selectCheckBox.isChecked()) {
                        selectedBooks.add(book);
                    } else {
                        selectedBooks.remove(book);
                    }
                }
            });
        }

        public void bind(int position) {
            Book book = mCartItems.get(position);
            if (book != null) {
                Picasso.get().load(book.getThumbnailUrl()).into(thumbnailImageView);
                titleTextView.setText(book.getTitle());
                authorTextView.setText("by " + book.getAuthor());
                priceTextView.setText("Ksh " + book.getPrice());

                if (selectAllChecked) {
                    selectCheckBox.setChecked(true);
                    selectedBooks.add(book);
                } else {
                    selectCheckBox.setChecked(selectedBooks.contains(book));
                }
            }
        }
    }

    // Update the selectedBooks set
    public void setSelectedBooks(Set<Book> selectedBooks) {
        this.selectedBooks = selectedBooks != null ? selectedBooks : new HashSet<>();
        notifyDataSetChanged(); // Notify adapter to refresh view
    }

    // Update selectAllChecked state
    public void setSelectAllChecked(boolean selectAllChecked) {
        this.selectAllChecked = selectAllChecked;
        notifyDataSetChanged(); // Notify adapter to refresh view
    }
}
