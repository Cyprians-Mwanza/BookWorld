package com.example.bookworld;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Group extends AppCompatActivity {

    private List<String> friendsList = new ArrayList<>();
    private LinearLayout friendsLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotButton = findViewById(R.id.three_dotButton);
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout);
        LinearLayout searchLayout = findViewById(R.id.searchlayout);
        LinearLayout moreLayout = findViewById(R.id.morelayout);
        TextView findFriendsTextView = findViewById(R.id.findFriends);
        friendsLayout = findViewById(R.id.friendsLayout);

        // Set click listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click (e.g., go back to previous activity)
                onBackPressed();
            }
        });

        threeDotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle three dot button click (e.g., open menu)
                // Implement your logic here
            }
        });

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle home layout click (e.g., navigate to HomeActivity)
                Intent intent = new Intent(Group.this, Home.class);
                startActivity(intent);
            }
        });

        myBooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle my books layout click (e.g., navigate to MyBooksActivity)
                Intent intent = new Intent(Group.this, MyBooks.class);
                startActivity(intent);
            }
        });

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle search layout click (e.g., navigate to SearchActivity)
                Intent intent = new Intent(Group.this, search_discovery.class);
                startActivity(intent);
            }
        });

        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inflate the layout for the pop-up window
                View popupView = getLayoutInflater().inflate(R.layout.activity_more, null);

                PopupWindow popupWindow = new PopupWindow(popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                // Set background drawable with semi-transparent color to create overlay effect
                Drawable background = new ColorDrawable(Color.BLACK);
                background.setAlpha(150); // Set alpha (transparency) level (0-255)
                popupWindow.setBackgroundDrawable(background);

                // Set focusable and outside touchable to true to allow dismissal of the pop-up window when touched outside
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);

                // Show the pop-up window at the center of the screen
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // Add click listeners to the pop-up window items
                popupView.findViewById(R.id.group).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Group.this, Group.class);
                        startActivity(intent);
                        popupWindow.dismiss();
                    }
                });

                popupView.findViewById(R.id.borrow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Group.this, BorrowPage.class);
                        startActivity(intent);
                        popupWindow.dismiss();
                    }
                });
            }
        });

        findFriendsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle find friends text view click
                fetchAndShowFriendsPopup(v);
            }
        });
    }

    private void fetchAndShowFriendsPopup(final View anchorView) {
        // Fetch usernames from Firestore
        CollectionReference usersRef = db.collection("users");
        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> usernames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String username = document.getString("username");
                        if (username != null) {
                            usernames.add(username);
                        }
                    }
                    showFriendsPopup(anchorView, usernames);
                } else {
                    Toast.makeText(Group.this, "Error fetching usernames", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showFriendsPopup(View anchorView, List<String> usernames) {
        // Inflate the layout for the pop-up window
        View popupView = getLayoutInflater().inflate(R.layout.popup_friends, null);

        // Create the pop-up window
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Set background drawable with semi-transparent color to create overlay effect
        Drawable background = new ColorDrawable(Color.BLACK);
        background.setAlpha(150); // Set alpha (transparency) level (0-255)
        popupWindow.setBackgroundDrawable(background);

        // Set focusable and outside touchable to true to allow dismissal of the pop-up window when touched outside
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        // Show the pop-up window at the center of the screen
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // Get the layout to add usernames dynamically
        LinearLayout usersLayout = popupView.findViewById(R.id.usersLayout);

        for (final String username : usernames) {
            TextView usernameTextView = new TextView(this);
            usernameTextView.setText(username);
            usernameTextView.setTextSize(18);
            usernameTextView.setPadding(10, 10, 10, 10);
            usernameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle adding friend
                    addFriend(username);
                    popupWindow.dismiss();
                }
            });
            usersLayout.addView(usernameTextView);
        }
    }

    private void addFriend(String username) {
        if (!friendsList.contains(username)) {
            friendsList.add(username);
            TextView friendTextView = new TextView(this);
            friendTextView.setText(username);
            friendTextView.setTextSize(18);
            friendTextView.setPadding(10, 10, 10, 10);
            friendsLayout.addView(friendTextView);
            Toast.makeText(this, username + " added as friend", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, username + " is already your friend", Toast.LENGTH_SHORT).show();
        }
    }
}
