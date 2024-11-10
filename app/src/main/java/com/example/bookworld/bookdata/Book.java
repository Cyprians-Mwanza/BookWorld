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
    private String pdfUrl; // Ensure pdfUrl is included

    // No-argument constructor required for Firestore
    public Book() {
    }

    // Constructor with arguments
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

}
