package com.example.bookworld;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Group extends AppCompatActivity {

    private static final String PREFS_NAME = "FriendsPrefs";
    private static final String KEY_FRIENDS_LIST = "FriendsList";
    private static final int REQUEST_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_SEND_SMS_PERMISSION = 2;

    private List<String> friendsList = new ArrayList<>();
    private LinearLayout friendsLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private PopupWindow popupWindow;
    private String pendingPhoneNumber;
    private String senderUsername;
    private String senderId;
    private String currentuserUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            senderId = currentUser.getUid();
            fetchCurrentUserUsername();
        } else {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize views
        ImageView backButton = findViewById(R.id.backButton);
        ImageView threeDotButton = findViewById(R.id.three_dotButton);
        LinearLayout homeLayout = findViewById(R.id.homelayout);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout);
        LinearLayout searchLayout = findViewById(R.id.searchlayout);
        LinearLayout moreLayout = findViewById(R.id.morelayout);
        TextView findFriendsTextView = findViewById(R.id.findFriends);
        TextView inviteTextView = findViewById(R.id.invite);
        friendsLayout = findViewById(R.id.friendsLayout);

        // Debug log for senderUsername
        Log.d("GroupActivity", "Sender Username: " + currentuserUsername);

        // Load saved friends list
        loadFriendsList();

        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());

        threeDotButton.setOnClickListener(v -> {
            Intent intent = new Intent(Group.this, three_dots.class);
            startActivity(intent);
        });

        homeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Group.this, Home.class);
            startActivity(intent);
        });

        myBooksLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Group.this, MyBooks.class);
            startActivity(intent);
        });

        searchLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Group.this, search_discovery.class);
            startActivity(intent);
        });

        moreLayout.setOnClickListener(view -> {
            View popupView = getLayoutInflater().inflate(R.layout.activity_more, null);
            popupWindow = new PopupWindow(popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            Drawable background = new ColorDrawable(Color.BLACK);
            background.setAlpha(150);
            popupWindow.setBackgroundDrawable(background);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            popupView.findViewById(R.id.group).setOnClickListener(v -> {
                Intent intent = new Intent(Group.this, Group.class);
                startActivity(intent);
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.borrow).setOnClickListener(v -> {
                Intent intent = new Intent(Group.this, BookPrice.class);
                startActivity(intent);
                popupWindow.dismiss();
            });
        });

        findFriendsTextView.setOnClickListener(v -> fetchAndShowFriendsPopup(v));

        inviteTextView.setOnClickListener(v -> checkContactsPermission());
    }

    private void fetchCurrentUserUsername() {
        db.collection("users")
                .document(senderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentuserUsername = document.getString("username");
                            // Use the currentuserUsername for other operations as needed
                            Log.d("GroupActivity", "Current User's Username: " + currentuserUsername);
                        } else {
                            Log.d("GroupActivity", "No such document");
                        }
                    } else {
                        Log.d("GroupActivity", "Get failed with ", task.getException());
                    }
                });
    }

    private void checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS_PERMISSION);
        } else {
            showContactsPopup();
        }
    }

    private void showContactsPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_contacts, null);
        popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Drawable background = new ColorDrawable(Color.BLACK);
        background.setAlpha(150);
        popupWindow.setBackgroundDrawable(background);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(friendsLayout, Gravity.CENTER, 0, 0);
        LinearLayout contactsLayout = popupView.findViewById(R.id.contactsLayout);
        loadContacts(contactsLayout);
    }

    private void loadContacts(LinearLayout contactsLayout) {
        Cursor cursor = getContentResolver().query(
                CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));

                View contactItemView = LayoutInflater.from(this).inflate(R.layout.item_contact, contactsLayout, false);
                TextView contactNameTextView = contactItemView.findViewById(R.id.contactName);
                Button inviteButton = contactItemView.findViewById(R.id.inviteButton);

                contactNameTextView.setText(name);
                inviteButton.setOnClickListener(v -> checkSendSmsPermission(phoneNumber));

                contactsLayout.addView(contactItemView);
            }
            cursor.close();
        }
    }

    private void checkSendSmsPermission(String phoneNumber) {
        pendingPhoneNumber = phoneNumber;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
        } else {
            sendSmsInvitation(phoneNumber);
        }
    }

    private void sendSmsInvitation(String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = "Check out this amazing app!";
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "Invitation sent", Toast.LENGTH_SHORT).show();
    }

    private void fetchAndShowFriendsPopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_friends, null);
        popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Drawable background = new ColorDrawable(Color.BLACK);
        background.setAlpha(150);
        popupWindow.setBackgroundDrawable(background);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchorView);
        LinearLayout friendsLayout = popupView.findViewById(R.id.popup_friends);
        loadFriendsFromFirestore(friendsLayout);
    }

    private void loadFriendsFromFirestore(LinearLayout friendsLayout) {
        CollectionReference usersRef = db.collection("users");
        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String username = document.getString("username");
                        addFriendToPopup(username, friendsLayout);
                    }
                } else {
                    Toast.makeText(Group.this, "Error fetching friends", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addFriendToPopup(String friendUsername, LinearLayout friendsLayout) {
        View friendItemView = LayoutInflater.from(this).inflate(R.layout.item_friend_no_message, friendsLayout, false);
        TextView friendNameTextView = friendItemView.findViewById(R.id.friendName);
        Button addFriendButton = friendItemView.findViewById(R.id.addFriendButton);

        friendNameTextView.setText(friendUsername);
        addFriendButton.setOnClickListener(v -> {
            addFriendToLayout(friendUsername); // Add friend to the layout
            saveFriendToFirebase(friendUsername); // Save the friend to Firebase
        });

        friendsLayout.addView(friendItemView);
    }

    private void showMessageDialog(String recipientUsername) {
        View dialogView = getLayoutInflater().inflate(R.layout.popup_send_message, null);
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        android.app.AlertDialog messageDialog = dialogBuilder.create();
        Button sendButton = dialogView.findViewById(R.id.sendButton);
        TextView messageEditText = dialogView.findViewById(R.id.messageInput);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                fetchUserIdAndSendMessage(recipientUsername, message);
                messageDialog.dismiss();
            } else {
                Toast.makeText(Group.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        messageDialog.show();
    }

    private void fetchUserIdAndSendMessage(String recipientUsername, String message) {
        CollectionReference usersRef = db.collection("users");
        usersRef.whereEqualTo("username", recipientUsername).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String recipientId = document.getId(); // Assuming userId is the document ID
                        sendMessage(recipientUsername, recipientId, message);
                    }
                } else {
                    Toast.makeText(Group.this, "Error fetching user ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessage(String recipientUsername, String recipientId, String message) {
        Message messageObject = new Message(senderUsername, recipientUsername, message, senderId);

        db.collection("users")
                .document(recipientId) // Use recipient ID to address the recipient
                .collection("notifications")
                .add(messageObject)
                .addOnSuccessListener(documentReference -> Toast.makeText(Group.this, "Message sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Group.this, "Error sending message", Toast.LENGTH_SHORT).show());
    }


    private void addFriendToLayout(String friendUsername) {
        View friendItemView = LayoutInflater.from(this).inflate(R.layout.item_friend_with_message, friendsLayout, false);
        TextView friendNameTextView = friendItemView.findViewById(R.id.friendName);
        ImageView deleteFriendImageView = friendItemView.findViewById(R.id.deleteIcon);
        ImageView messageFriendImageView = friendItemView.findViewById(R.id.messageIcon);

        friendNameTextView.setText(friendUsername);

        // Set up the delete icon click listener
        deleteFriendImageView.setOnClickListener(v -> {
            removeFriend(friendUsername);
        });

        // Set up the message icon click listener
        messageFriendImageView.setOnClickListener(v -> {
            showMessageDialog(friendUsername);
        });

        friendsLayout.addView(friendItemView);
    }

    private void removeFriend(String friendUsername) {
        // Remove from local list
        friendsList.remove(friendUsername);

        // Remove from Firebase Database
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("users/friends").child(senderId);
        friendsRef.orderByValue().equalTo(friendUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    childSnapshot.getRef().removeValue();
                }
                // Save the updated friends list
                saveFriendsList();
                // Remove from UI
                loadFriendsList();
                Toast.makeText(Group.this, "Friend removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Group.this, "Error removing friend", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveFriendToFirebase(String friendUsername) {
        // Add the friend to the local list
        friendsList.add(friendUsername);

        // Optionally, you can also save the friend to Firebase Database
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("users/friends").child(senderId);
        friendsRef.push().setValue(friendUsername);

        // Save the friends list to SharedPreferences
        saveFriendsList();

        Toast.makeText(this, "Friend added to list", Toast.LENGTH_SHORT).show();
    }

    private void saveFriendsList() {
        Set<String> friendsSet = new HashSet<>(friendsList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_FRIENDS_LIST, friendsSet);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContactsPopup();
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SEND_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingPhoneNumber != null) {
                    sendSmsInvitation(pendingPhoneNumber);
                }
            } else {
                Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadFriendsList() {
        // Retrieve the saved friends list from SharedPreferences
        Set<String> friendsSet = sharedPreferences.getStringSet(KEY_FRIENDS_LIST, new HashSet<>());

        // Clear the existing friendsList
        friendsList.clear();

        // Add all retrieved friends to the friendsList
        friendsList.addAll(friendsSet);

        // Update the UI to display the friends
        friendsLayout.removeAllViews(); // Clear existing views

        for (String friendUsername : friendsList) {
            addFriendToLayout(friendUsername);
        }
    }

}