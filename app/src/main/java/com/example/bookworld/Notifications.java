package com.example.bookworld;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notifications extends AppCompatActivity {

    private LinearLayout notificationsLayout;
    private FirebaseFirestore db;
    private List<Message> messagesList = new ArrayList<>();
    private Map<String, String> senderUsernames = new HashMap<>();
    private int pendingUsernamesCount; // Track pending usernames to fetch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        notificationsLayout = findViewById(R.id.notificationsLayout);

        // Set up the back button
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Fetch and display messages
        fetchMessages();
    }

    private void fetchMessages() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid(); // Get current user's ID

        CollectionReference messagesRef = db.collection("users").document(currentUserId)
                .collection("notifications");

        messagesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    messagesList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Message message = document.toObject(Message.class);
                        messagesList.add(message);
                    }
                    // Fetch usernames for all messages
                    fetchSenderUsernames();
                } else {
                    Toast.makeText(Notifications.this, "Error fetching messages.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchSenderUsernames() {
        if (messagesList.isEmpty()) {
            // If there are no messages, just update the UI
            displayMessages();
            return;
        }

        pendingUsernamesCount = messagesList.size(); // Set the count of pending usernames

        // Loop through each message and fetch the sender's username
        for (Message message : messagesList) {
            String senderId = message.getSenderId();

            if (senderId != null) {
                db.collection("users").document(senderId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String senderUsername = document.getString("username");
                                        message.setSenderUsername(senderUsername);
                                    } else {
                                        message.setSenderUsername("Unknown");
                                    }
                                } else {
                                    message.setSenderUsername("Unknown");
                                    Toast.makeText(Notifications.this, "Error fetching sender's username.", Toast.LENGTH_SHORT).show();
                                }

                                // Check if all usernames have been fetched
                                pendingUsernamesCount--;
                                if (pendingUsernamesCount == 0) {
                                    displayMessages();
                                }
                            }
                        });
            } else {
                // If senderId is null, decrease the pending count
                pendingUsernamesCount--;
                if (pendingUsernamesCount == 0) {
                    displayMessages();
                }
            }
        }
    }

    private void displayMessages() {
        notificationsLayout.removeAllViews(); // Clear existing views
        for (Message message : messagesList) {
            TextView messageTextView = new TextView(this);
            String senderUsername = message.getSenderUsername();
            messageTextView.setText("From: " + senderUsername + "\n" + message.getMessageContent());
            messageTextView.setPadding(8, 8, 8, 8);
            messageTextView.setTextSize(18); // Set the text size to 18sp or any desired size
            notificationsLayout.addView(messageTextView);
        }
    }
}
