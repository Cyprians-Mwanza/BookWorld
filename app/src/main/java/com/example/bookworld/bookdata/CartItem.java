package com.example.bookworld.bookdata;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String id;
    private String thumbnailUrl;
    private String title;
    private String author;
    private String description;
    private String price;
    private float rating;

    public CartItem() {
        // Default constructor required for Firestore
    }

    protected CartItem(Parcel in) {
        id = in.readString();
        thumbnailUrl = in.readString();
        title = in.readString();
        author = in.readString();
        description = in.readString();
        price = in.readString();
        rating = in.readFloat();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

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

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(thumbnailUrl);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeFloat(rating);
    }
}
