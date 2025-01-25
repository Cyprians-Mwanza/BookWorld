package com.example.bookworld.bookdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BoughtBook {
    private String name;
    private String bookTitle;
    private String author;
    private String price;
    private String dateBought; // Store date as String for Firestore

    // Empty constructor needed for Firestore
    public BoughtBook() {
    }

    // Constructor with parameters
    public BoughtBook(String name, String bookTitle, String author, String price, String dateBought) {
        this.name = name;
        this.bookTitle = bookTitle;
        this.author = author;
        this.price = price;
        this.dateBought = dateBought;
    }

    // Getters and setters for Firestore mapping
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDateBought() {
        return dateBought;
    }

    public void setDateBought(String dateBought) {
        this.dateBought = dateBought;
    }

    // Convert dateBorrowed from String to Date
    public Date getDateBoughtAsDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Match Firestore date format
        try {
            return sdf.parse(dateBought);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    // Convert Date back to String
    public void setDateBoughtAsDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Adjust to match Firestore date format
        this.dateBought = sdf.format(date);
    }
}
