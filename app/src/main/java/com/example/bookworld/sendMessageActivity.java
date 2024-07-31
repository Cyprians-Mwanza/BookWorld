package com.example.bookworld;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class sendMessageActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText messageEditText;
    private Button sendButton;
    private String senderUsername;
    private String recipientUsername;
    private String senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Retrieve recipientUsername from intent
        recipientUsername = getIntent().getStringExtra("recipientUsername");

        // Fetch sender's ID and username
        fetchSenderDetails();

        sendButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString().trim();
            if (!messageText.isEmpty() && senderId != null && senderUsername != null) {
                sendMessage(senderUsername, recipientUsername, messageText, senderId);
            } else {
                Toast.makeText(sendMessageActivity.this, "Message cannot be empty or user details not fetched", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSenderDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            senderId = currentUser.getUid();
            db.collection("users").document(senderId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            senderUsername = document.getString("username");
                        } else {
                            Toast.makeText(sendMessageActivity.this, "Error fetching username", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(sendMessageActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendMessage(String senderUsername, String recipientUsername, String messageContent, String senderId) {
        Message message = new Message(senderUsername, recipientUsername, messageContent, senderId);

        // Assuming you have the recipient's user ID
        String recipientUserId = getRecipientUserId(recipientUsername); // Implement this method based on your logic

        db.collection("users")
                .document(recipientUserId)
                .collection("notifications")
                .add(message)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(sendMessageActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity
                        } else {
                            Toast.makeText(sendMessageActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Dummy method to get recipient user ID based on recipientUsername
    private String getRecipientUserId(String recipientUsername) {
        // Implement logic to retrieve recipient user ID from Firestore
        return "recipientUserId"; // Placeholder
    }
}
