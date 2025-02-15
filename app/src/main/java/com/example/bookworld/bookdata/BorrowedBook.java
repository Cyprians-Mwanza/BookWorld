package com.example.bookworld.bookdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BorrowedBook {
    private String name;  // Borrower's name
    private String bookTitle;  // Title of the borrowed book
    private int days;
    private String author;
    private String dateBorrowed; // Stored as String in Firestore

    // Default constructor needed for Firestore
    public BorrowedBook() {
    }

    // Parameterized constructor
    public BorrowedBook(String name, String bookTitle, int days, String author, String dateBorrowed) {
        this.name = name;
        this.bookTitle = bookTitle;
        this.days = days;
        this.author = author;
        this.dateBorrowed = dateBorrowed;
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

    public String getDateBorrowed() {
        return dateBorrowed;
    }

    public void setDateBorrowed(String dateBorrowed) {
        this.dateBorrowed = dateBorrowed;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    // Convert dateBorrowed from String to Date
    public Date getDateBorrowedAsDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); // Adjust format to match your Firestore string
        try {
            return sdf.parse(dateBorrowed);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    // Utility method to set dateBorrowed as String from Date
    public void setDateBorrowedAsDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); // Adjust format to match your Firestore format
        this.dateBorrowed = sdf.format(date);
    }

    // New method to return borrower name
    public String getBorrower() {
        return name;
    }

    // New method to return days borrowed
    public int getDaysBorrowed() {
        return days;
    }
}
