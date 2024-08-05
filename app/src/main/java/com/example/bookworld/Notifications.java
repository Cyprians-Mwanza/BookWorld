package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

    private View messagesIndicator;
    private View requestsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        notificationsLayout = findViewById(R.id.notificationsLayout);
        messagesIndicator = findViewById(R.id.messagesIndicator);
        requestsIndicator = findViewById(R.id.requestsIndicator);

        // Set up the back button
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Set up the "MESSAGES" tab click listener
        findViewById(R.id.messagesTab).setOnClickListener(v -> {
            fetchMessages();
            showMessagesTab();
        });

        // Set up the "REQUESTS" tab click listener
        findViewById(R.id.requestsTab).setOnClickListener(v -> {
            fetchFriendRequests();
            showRequestsTab();
        });

        // Initially, show messages tab
        showMessagesTab();
        fetchMessages();
    }

    private void showMessagesTab() {
        messagesIndicator.setVisibility(View.VISIBLE);
        requestsIndicator.setVisibility(View.GONE);
    }

    private void showRequestsTab() {
        messagesIndicator.setVisibility(View.GONE);
        requestsIndicator.setVisibility(View.VISIBLE);
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

        for (Message message : messagesList) {
            String senderId = message.getSenderId();

            if (senderId != null) {
                db.collection("users").document(senderId)
                        .get()
                        .addOnCompleteListener(task -> {
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

                            checkIfAllUsernamesFetched();
                        });
            } else {
                // If senderId is null, handle it as 'Unknown'
                message.setSenderUsername("Unknown");
                checkIfAllUsernamesFetched();
            }
        }
    }

    private void checkIfAllUsernamesFetched() {
        pendingUsernamesCount--;
        if (pendingUsernamesCount == 0) {
            displayMessages();
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
            messageTextView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color to black
            notificationsLayout.addView(messageTextView);
        }
    }

    private void fetchFriendRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid(); // Get current user's ID

        CollectionReference friendRequestsRef = db.collection("users").document(currentUserId)
                .collection("notifications").document("FriendRequests").collection("requests");

        friendRequestsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    notificationsLayout.removeAllViews(); // Clear existing views
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot.isEmpty()) {
                        // No friend requests
                        TextView noRequestsTextView = new TextView(Notifications.this);
                        noRequestsTextView.setText("No friend requests.");
                        noRequestsTextView.setPadding(8, 8, 8, 8);
                        noRequestsTextView.setTextSize(18); // Set the text size to 18sp or any desired size
                        noRequestsTextView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color to black
                        notificationsLayout.addView(noRequestsTextView);
                    } else {
                        // Display friend requests
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            FriendRequest friendRequest = document.toObject(FriendRequest.class);

                            // Create a layout for each friend request
                            LinearLayout requestLayout = new LinearLayout(Notifications.this);
                            requestLayout.setOrientation(LinearLayout.VERTICAL);
                            requestLayout.setPadding(8, 8, 8, 8);
                            requestLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));

                            // Add request details
                            TextView requestTextView = new TextView(Notifications.this);
                            requestTextView.setText("Friend request from: " + friendRequest.getSenderUsername());
                            requestTextView.setTextSize(18); // Set the text size to 18sp or any desired size
                            requestTextView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color to black
                            requestLayout.addView(requestTextView);

                            // Create a horizontal layout for the buttons
                            LinearLayout buttonsLayout = new LinearLayout(Notifications.this);
                            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                            buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            buttonsLayout.setGravity(Gravity.END); // Align buttons to the right

                            // Add Accept button
                            Button acceptButton = new Button(Notifications.this);
                            acceptButton.setText("Accept");
                            acceptButton.setOnClickListener(v -> acceptRequest(friendRequest));
                            buttonsLayout.addView(acceptButton);

                            // Add Cancel button
                            Button cancelButton = new Button(Notifications.this);
                            cancelButton.setText("Cancel");
                            cancelButton.setOnClickListener(v -> cancelRequest(friendRequest));
                            buttonsLayout.addView(cancelButton);

                            // Add the buttons layout to the request layout
                            requestLayout.addView(buttonsLayout);

                            // Add the request layout to the notifications layout
                            notificationsLayout.addView(requestLayout);
                        }
                    }
                } else {
                    Toast.makeText(Notifications.this, "Error fetching friend requests.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void acceptRequest(FriendRequest friendRequest) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();
        String senderId = friendRequest.getSenderId();
        String requestId = friendRequest.getRequestId();

        // Add sender as a friend
        CollectionReference friendsRef = db.collection("users").document(currentUserId).collection("friends");
        friendsRef.document(senderId).set(new HashMap<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Add current user as a friend in sender's friends list
                CollectionReference senderFriendsRef = db.collection("users").document(senderId).collection("friends");
                senderFriendsRef.document(currentUserId).set(new HashMap<>()).addOnCompleteListener(senderTask -> {
                    if (senderTask.isSuccessful()) {
                        // Move request to accepted requests
                        Map<String, Object> acceptedRequestData = new HashMap<>();
                        acceptedRequestData.put("senderUsername", friendRequest.getSenderUsername());
                        acceptedRequestData.put("senderId", senderId);
                        acceptedRequestData.put("status", "accepted"); // Status to indicate the request is accepted

                        // Add to accepted requests
                        db.collection("users").document(currentUserId)
                                .collection("notifications").document("FriendRequests")
                                .collection("accepted").document(requestId)
                                .set(acceptedRequestData)
                                .addOnCompleteListener(acceptedTask -> {
                                    if (acceptedTask.isSuccessful()) {
                                        // Remove request from incoming requests
                                        db.collection("users").document(currentUserId)
                                                .collection("notifications").document("FriendRequests")
                                                .collection("requests").document(requestId)
                                                .delete().addOnCompleteListener(deleteTask -> {
                                                    if (deleteTask.isSuccessful()) {
                                                        Toast.makeText(Notifications.this, "Friend request accepted.", Toast.LENGTH_SHORT).show();
                                                        fetchFriendRequests(); // Refresh friend requests
                                                        updateGroupActivity(); // Notify Group activity to update friends list
                                                    } else {
                                                        Toast.makeText(Notifications.this, "Error removing friend request.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(Notifications.this, "Error moving request to accepted list.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(Notifications.this, "Error adding friend to sender's list.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Notifications.this, "Error adding friend.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRequest(FriendRequest friendRequest) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();
        String requestId = friendRequest.getRequestId();

        // Remove the request from the current user's friend requests
        db.collection("users").document(currentUserId)
                .collection("notifications").document("FriendRequests")
                .collection("requests").document(requestId)
                .delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Notifications.this, "Friend request canceled.", Toast.LENGTH_SHORT).show();
                        fetchFriendRequests(); // Refresh friend requests
                    } else {
                        Toast.makeText(Notifications.this, "Error canceling friend request.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateGroupActivity() {
        // Notify Group activity to update friends list
        Intent intent = new Intent(this, Group.class);
        intent.putExtra("updateFriendsList", true);
        startActivity(intent);
    }
}
