package com.example.bookworld.bookdata;// ChapterAdapter.java
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {
    private List<String> chapters;
    private List<String> selectedChapters;
    private OnChapterClickListener onChapterClickListener;

    public interface OnChapterClickListener {
        void onChapterClick(String chapter);
    }

    public ChapterAdapter(List<String> chapters, OnChapterClickListener onChapterClickListener) {
        this.chapters = chapters;
        this.onChapterClickListener = onChapterClickListener;
        this.selectedChapters = new ArrayList<>();
    }

    public void setSelectedChapters(List<String> selectedChapters) {
        this.selectedChapters = selectedChapters;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String chapter = chapters.get(position);
        holder.chapterTitle.setText("Chapter " + (position + 1));
        holder.itemView.setOnClickListener(v -> onChapterClickListener.onChapterClick(chapter));
        holder.itemView.setBackgroundColor(selectedChapters.contains(chapter) ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chapterTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            chapterTitle = itemView.findViewById(android.R.id.text1);
        }
    }
}
