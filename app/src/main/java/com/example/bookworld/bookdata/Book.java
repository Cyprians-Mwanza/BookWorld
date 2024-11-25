package com.example.bookworld.bookdata;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private String id;
    private String thumbnailUrl;
    private String title;
    private String author;
    private String description;
    private String price;
    private float rating;
    private int daysToBorrow;
    private String pdfUrl;
    private long returnDateMillis;
    private String returnDate;
    private String countdown;
    private int borrowCount;

    // No-argument constructor required for Firestore
    public Book() {
    }

    // Constructor with all parameters
    public Book(String id, String thumbnailUrl, String title, String author, String description, String price, float rating, String pdfUrl, int daysToBorrow) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.author = author;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.pdfUrl = pdfUrl;
        this.daysToBorrow = daysToBorrow;
    }

    // **New Constructor**
    public Book(String id, String thumbnailUrl, String title, String author, String description, String price, int daysToBorrow, String pdfUrl) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.author = author;
        this.description = description;
        this.price = price;
        this.daysToBorrow = daysToBorrow;
        this.pdfUrl = pdfUrl;
    }

    // Parcelable implementation
    protected Book(Parcel in) {
        id = in.readString();
        thumbnailUrl = in.readString();
        title = in.readString();
        author = in.readString();
        description = in.readString();
        price = in.readString();
        rating = in.readFloat();
        pdfUrl = in.readString();
        daysToBorrow = in.readInt();
        returnDateMillis = in.readLong();
        returnDate = in.readString();
        countdown = in.readString();
        borrowCount = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(thumbnailUrl);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeFloat(rating);
        dest.writeString(pdfUrl);
        dest.writeInt(daysToBorrow);
        dest.writeLong(returnDateMillis);
        dest.writeString(returnDate);
        dest.writeString(countdown);
        dest.writeInt(borrowCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public int getDaysToBorrow() {
        return daysToBorrow;
    }

    public void setDaysToBorrow(int daysToBorrow) {
        this.daysToBorrow = daysToBorrow;
    }

    public long getReturnDateMillis() {
        return returnDateMillis;
    }

    public void setReturnDateMillis(long returnDateMillis) {
        this.returnDateMillis = returnDateMillis;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getCountdown() {
        return countdown;
    }

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }
}
