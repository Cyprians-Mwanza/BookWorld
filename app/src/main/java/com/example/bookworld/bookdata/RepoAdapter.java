package com.example.bookworld.bookdata;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.Product;
import com.example.bookworld.R;

import java.util.ArrayList;

public class RepoAdapter extends RecyclerView.Adapter<RepoAdapter.ProductViewHolder> {

    private Context context;
    private ArrayList<Product> productArrayList;
    private LayoutInflater inflater;

    public RepoAdapter(Context context, ArrayList<Product> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.reports_card, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productArrayList.get(position);

        // Use getter methods instead of directly accessing the fields
        holder.productName.setText(product.getName());
        holder.productPrice.setText("Ksh " + product.getPrice());
        holder.productQuantity.setText(product.getQuantity());
        holder.productId.setText(product.getItemId());
        holder.productCategory.setText(product.getCategory());


    }


    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView productName, productPrice, productQuantity, productId, productCategory;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.name);
            productPrice = itemView.findViewById(R.id.price);
            productQuantity = itemView.findViewById(R.id.quantity);
            productId = itemView.findViewById(R.id.id);
            productCategory = itemView.findViewById(R.id.category);
        }
    }
}
