package com.example.bookworld.bookdata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;

import java.util.List;

import retrofit2.http.Part;

public  class PartAdapter extends RecyclerView.Adapter<PartAdapter.PartViewHolder> {
    private List<String> parts;
    private OnPartClickListener listener;

    public interface OnPartClickListener {
        void onPartClick(int position);
    }

    public PartAdapter(List<String> parts, OnPartClickListener listener) {
        this.parts = parts;
        this.listener = listener;
    }

    @Override
    public PartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PartViewHolder holder, int position) {
        String part = parts.get(position);
        holder.partTextView.setText(part);
        holder.itemView.setOnClickListener(v -> listener.onPartClick(position));
    }

    @Override
    public int getItemCount() {
        return parts.size();
    }

    public static class PartViewHolder extends RecyclerView.ViewHolder {
        TextView partTextView;

        public PartViewHolder(View itemView) {
            super(itemView);
            partTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
