package com.example.bookworld.bookdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BorrowedBooks {
    private String bookName;
    private String borrowerName;
    private String returnDate; // Use return date instead of due date
    private long daysRemaining; // Number of days remaining

    public BorrowedBooks(String bookName, String borrowerName, String returnDate) {
        this.bookName = bookName;
        this.borrowerName = borrowerName;
        this.returnDate = returnDate;
        this.daysRemaining = calculateDaysRemaining();
    }

    public String getBookName() {
        return bookName;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    // Method to calculate days remaining
    private long calculateDaysRemaining() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date returnDateObj = dateFormat.parse(returnDate);
            Date currentDate = new Date();
            long diffInMillis = returnDateObj.getTime() - currentDate.getTime();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Return -1 or handle error case as per your app logic
        }
    }
}
