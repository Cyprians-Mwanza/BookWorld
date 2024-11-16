package com.example.bookworld.bookdata;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportClass implements Parcelable {
    private String bookTitle;
    private int days;
    private Object dateBorrowed;
    private String genre;
    private String cart;

    // Default constructor
    public ReportClass() {
        this.bookTitle = "";
        this.days = 0;
        this.dateBorrowed = "Unknown Date";
    }

    // Constructor with parameters
    public ReportClass(String bookTitle, Object dateBorrowed, int days) {
        this.bookTitle = (bookTitle != null) ? bookTitle : "Unknown Title";
        this.dateBorrowed = (dateBorrowed != null) ? dateBorrowed : "Unknown Date";
        this.days = days;
    }

    // Parcelable constructor
    protected ReportClass(Parcel in) {
        bookTitle = in.readString();
        dateBorrowed = in.readSerializable();
        days = in.readInt();
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

    // Firebase methods for data fetching

    public static void fetchFavouriteGenre(FirebaseFirestore db, FirebaseUser currentUser, final OnDataFetchedListener listener) {
        if (currentUser == null) {
            Log.e("ReportClass", "User is not logged in.");
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("Favourite genre")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> favouriteGenres = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String genre = document.getString("genreName");
                            if (genre != null) {
                                favouriteGenres.add(genre);
                            }
                        }
                        listener.onDataFetched(favouriteGenres);
                    } else {
                        Log.e("ReportClass", "Error fetching favourite genres", task.getException());
                    }
                });
    }

    public static void getCartItems(FirebaseFirestore db, FirebaseUser currentUser, final OnDataFetchedListener listener) {
        if (currentUser == null) {
            Log.e("ReportClass", "User is not logged in.");
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("cartItems")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> cartItems = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String itemName = document.getString("itemName");
                            if (itemName != null) {
                                cartItems.add(itemName);
                            }
                        }
                        listener.onDataFetched(cartItems);
                    } else {
                        Log.e("ReportClass", "Error fetching cart items", task.getException());
                    }
                });
    }

    // Interface for the data fetch callback
    public interface OnDataFetchedListener {
        void onDataFetched(List<String> data);
    }

    // Other getters and setters...

    public String getBookTitle() {
        return (bookTitle != null && !bookTitle.isEmpty()) ? bookTitle : "Unknown Title";
    }

    public int getDays() {
        return days;
    }

    // Getter for the genre
    public String getGenre() {
        return genre != null ? genre : "Unknown Genre";
    }
    public void setBookTitle(String bookTitle) {
        this.bookTitle = (bookTitle != null) ? bookTitle : "Unknown Title";
    }

    public void setDays(int days) {
        this.days = days;
    }



    // Setter for the genre
    public void setGenre(String genre) {
        this.genre = genre;
    }

    // Getter for the cart
    public String getCart() {
        return cart != null ? cart : "No Cart Info";
    }

    // Setter for the cart
    public void setCart(String cart) {
        this.cart = cart;
    }
    public String getDateBorrowed() {
        if (dateBorrowed instanceof Long) {
            Long timestamp = (Long) dateBorrowed;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = new Date(timestamp);
            return sdf.format(date);
        } else if (dateBorrowed instanceof String) {
            return (String) dateBorrowed;
        }
        return "Unknown Date";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookTitle);
        dest.writeSerializable((Serializable) dateBorrowed);
        dest.writeInt(days);
    }

    @Override
    public String toString() {
        return "ReportClass{" +
                "bookTitle='" + bookTitle + '\'' +
                ", days=" + days +
                ", dateBorrowed=" + dateBorrowed +
                '}';
    }
}
