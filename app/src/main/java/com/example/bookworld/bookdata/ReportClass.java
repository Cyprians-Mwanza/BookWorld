package com.example.bookworld.bookdata;

import android.os.Parcel;
import android.os.Parcelable;

public class ReportClass implements Parcelable {
    private String bookTitle;
    private String name; // Borrower's name
    private int days; // Number of days the book is borrowed for (stored as int)
    private String thumbnailUrl;
    private String dateBorrowed; // Date when the book was borrowed

    // Default constructor
    public ReportClass() {
    }

    // Constructor with parameters
    public ReportClass(String bookTitle, String name, String dateBorrowed, int days, String thumbnailUrl) {
        this.bookTitle = bookTitle;
        this.name = name;
        this.dateBorrowed = dateBorrowed;
        this.days = days;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Parcelable constructor
    protected ReportClass(Parcel in) {
        bookTitle = in.readString();
        name = in.readString();
        dateBorrowed = in.readString(); // Read date as String
        days = in.readInt(); // Read as int
        thumbnailUrl = in.readString();
    }

    public static final Creator<ReportClass> CREATOR = new Creator<ReportClass>() {
        @Override
        public ReportClass createFromParcel(Parcel in) {
            return new ReportClass(in);
        }

        @Override
        public ReportClass[] newArray(int size) {
            return new ReportClass[size];
        }
    };

    // Getters
    public String getBookTitle() {
        return bookTitle;
    }

    public String getName() {
        return name;
    }

    public String getDateBorrowed() {
        return dateBorrowed; // Return date as String
    }

    public int getDays() {
        return days;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    // Setters
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDateBorrowed(String dateBorrowed) {
        this.dateBorrowed = dateBorrowed; // Set the date
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookTitle);
        dest.writeString(name);
        dest.writeString(dateBorrowed); // Write dateBorrowed as String
        dest.writeInt(days); // Write as int
        dest.writeString(thumbnailUrl);
    }
}
