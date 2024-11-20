package com.example.bookworld.bookdata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private List<String> chapters;
    private OnChapterClickListener listener;

    // Interface to handle chapter click events
    public interface OnChapterClickListener {
        void onChapterClick(String chapter);
    }

    // Constructor for the adapter
    public ChapterAdapter(List<String> chapters, OnChapterClickListener listener) {
        this.chapters = chapters;
        this.listener = listener;
    }

    @Override
    public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item view for each chapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChapterViewHolder holder, int position) {
        // Get the chapter name from the list and bind it to the view
        String chapter = chapters.get(position);
        holder.chapterText.setText(chapter);

        // Set an onClickListener to handle chapter selection
        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    // ViewHolder to hold the chapter item views
    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView chapterText;

        public ChapterViewHolder(View itemView) {
            super(itemView);
            chapterText = itemView.findViewById(R.id.chapterText); // Reference to the TextView
        }
    }
}