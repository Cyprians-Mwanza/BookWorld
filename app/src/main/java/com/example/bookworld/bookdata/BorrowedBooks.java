package com.example.bookworld.bookdata;

public class BorrowedBooks {

    private String name;
    private int days;
    private String bookId;
    private String bookTitle;
    private String pdfUrl;
    private String thumbnailUrl;
    private String author;
    private String description;
    private String price;
    private String dateBorrowed;  // Formatted date as a string
    private long returnDateMillis; // Return date as a timestamp
    private String returnDate; // Formatted return date as a string
    private String countdown; // Field to store the countdown string
    private int borrowCount; // Add a borrow count field

    // Default constructor required for Firestore
    public BorrowedBooks() {
    }

    // Constructor for initialization
    public BorrowedBooks(String name, int days, String bookId, String bookTitle, String pdfUrl,
                         String thumbnailUrl, String author, String description, String price,
                         String dateBorrowed, long returnDateMillis, String returnDate, int borrowCount) {
        this.name = name;
        this.days = days;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.pdfUrl = pdfUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.description = description;
        this.price = price;
        this.dateBorrowed = dateBorrowed;
        this.returnDateMillis = returnDateMillis;
        this.returnDate = returnDate;
        this.borrowCount = borrowCount; // Initialize borrow count
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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

    public String getDateBorrowed() {
        return dateBorrowed;
    }

    public void setDateBorrowed(String dateBorrowed) {
        this.dateBorrowed = dateBorrowed;
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

    // Getter and Setter for borrowCount
    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    // Getter and Setter for countdown
    public String getCountdown() {
        return countdown;
    }

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }
}