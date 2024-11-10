package com.example.bookworld.bookdata;

import android.os.Parcel;
import android.os.Parcelable;

public class BorrowedBooks implements Parcelable {
    private String bookTitle;
    private String name; // Borrower's name
    private long borrowStartTime; // Timestamp for when the book was borrowed
    private int days; // Number of days the book is borrowed for (stored as int)
    private String thumbnailUrl;
    private int borrowCount; // Count of times borrowed
    private String countdown; // Countdown string

    // Default constructor
    public BorrowedBooks() {
    }

    // Constructor with parameters
    public BorrowedBooks(String bookTitle, String name, long borrowStartTime, int days, String thumbnailUrl, int borrowCount) {
        this.bookTitle = bookTitle;
        this.name = name;
        this.borrowStartTime = borrowStartTime;
        this.days = days;
        this.thumbnailUrl = thumbnailUrl;
        this.borrowCount = borrowCount;
    }

    protected BorrowedBooks(Parcel in) {
        bookTitle = in.readString();
        name = in.readString();
        borrowStartTime = in.readLong();
        days = in.readInt(); // Read as int
        thumbnailUrl = in.readString();
        borrowCount = in.readInt();
    }

    public static final Creator<BorrowedBooks> CREATOR = new Creator<BorrowedBooks>() {
        @Override
        public BorrowedBooks createFromParcel(Parcel in) {
            return new BorrowedBooks(in);
        }

        @Override
        public BorrowedBooks[] newArray(int size) {
            return new BorrowedBooks[size];
        }
    };

    // Getters
    public String getBookTitle() {
        return bookTitle;
    }

    public String getName() {
        return name;
    }

    public long getBorrowStartTime() {
        return borrowStartTime;
    }

    public int getDays() {
        return days;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public String getCountdown() {
        return countdown;
    }

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    // Get remaining days as a long
    public long getRemainingDays() {
        long currentTime = System.currentTimeMillis();
        long endTime = borrowStartTime + (days * 24 * 60 * 60 * 1000L); // Convert days to milliseconds
        long remainingTime = endTime - currentTime;

        if (remainingTime > 0) {
            return remainingTime / (24 * 60 * 60 * 1000L); // Convert milliseconds to days
        } else {
            return 0; // Handle as expired or return appropriate value
        }
    }

    // Setters
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBorrowStartTime(long borrowStartTime) {
        this.borrowStartTime = borrowStartTime;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookTitle);
        dest.writeString(name);
        dest.writeLong(borrowStartTime);
        dest.writeInt(days); // Write as int
        dest.writeString(thumbnailUrl);
        dest.writeInt(borrowCount);
    }
}
