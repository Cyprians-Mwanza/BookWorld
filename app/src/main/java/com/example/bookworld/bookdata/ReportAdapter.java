package com.example.bookworld.bookdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import com.bumptech.glide.Glide;
import com.example.bookworld.R;
import java.util.List;

public class ReportAdapter extends BaseAdapter {
    private final Context context;
    private final List<ReportClass> reportList;

    public ReportAdapter(Context context, List<ReportClass> reportList) {
        this.context = context;
        this.reportList = reportList;

    }

    @Override
    public int getCount() {
        return reportList.size();
    }

    @Override
    public Object getItem(int position) {
        return reportList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_reports_cards, parent, false);
            holder = new ViewHolder();
            holder.bookTitle = convertView.findViewById(R.id.bookTitle);
            holder.days = convertView.findViewById(R.id.days);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate the data
        ReportClass report = reportList.get(position);
        holder.bookTitle.setText(report.getBookTitle() != null ? report.getBookTitle() : "Unknown Title");
        holder.days.setText("Days: " + report.getDays());

        return convertView;
    }

    // Method to clear the data in the list and refresh the view
    public void clear() {
        if (reportList != null) {
            reportList.clear();
            notifyDataSetChanged();
        }
    }

    // ViewHolder pattern for caching view references
    private static class ViewHolder {
        TextView bookTitle;
        TextView days;
        ImageView thumbnail;
    }
}
