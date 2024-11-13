package com.example.bookworld.bookdata;

import android.os.Parcel;
import android.os.Parcelable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class BorrowedBooks implements Parcelable {
    private String bookTitle;
    private String userId;  // Assuming you have a userId field
    private String name; // Borrower's name
    private long borrowStartDate; // Timestamp for when the book was borrowed in milliseconds
    private int days; // Number of days the book is borrowed for (stored as int)
    private String thumbnailUrl;
    private int borrowCount; // Count of times borrowed
    private String countdown; // Countdown string

    // Default constructor
    public BorrowedBooks() {
    }

    // Constructor with parameters
    public BorrowedBooks(String bookTitle, String name, long borrowStartDate, int days, String thumbnailUrl, int borrowCount) {
        this.bookTitle = bookTitle;
        this.name = name;
        this.borrowStartDate = borrowStartDate;
        this.days = days;
        this.thumbnailUrl = thumbnailUrl;
        this.borrowCount = borrowCount;
    }

    protected BorrowedBooks(Parcel in) {
        bookTitle = in.readString();
        name = in.readString();
        borrowStartDate = in.readLong();
        days = in.readInt(); // Read as int
        thumbnailUrl = in.readString();
        borrowCount = in.readInt();
    }

    // Method to return borrow start date as LocalDate
    public LocalDate getBorrowStartDate() {
        return Instant.ofEpochMilli(borrowStartDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    // Calculate the remaining days based on the borrow start date and total allowed days
    public String calculateRemainingDays() {
        LocalDate currentDate = LocalDate.now();
        LocalDate borrowStartDateLocal = getBorrowStartDate();
        long daysPassed = ChronoUnit.DAYS.between(borrowStartDateLocal, currentDate);
        long daysLeft = days - daysPassed;

        if (daysLeft > 0) {
            return String.format("%d day%s remaining", daysLeft, daysLeft > 1 ? "s" : "");
        } else {
            return "Expired";
        }
    }

    // Getter methods
    public String getBookTitle() {
        return bookTitle;
    }

    public String getName() {
        return name;
    }

    public long getBorrowStartDateMillis() {
        return borrowStartDate;
    }

    public int getDays() {
        return days;
    }
    // Getters and Setters for all fields, including userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    // Setter methods
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBorrowStartDate(long borrowStartDate) {
        this.borrowStartDate = borrowStartDate;
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

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    // Parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookTitle);
        dest.writeString(name);
        dest.writeLong(borrowStartDate);
        dest.writeInt(days); // Write as int
        dest.writeString(thumbnailUrl);
        dest.writeInt(borrowCount);
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
}
