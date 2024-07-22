package com.example.bookworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class three_dots extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_dots);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        LinearLayout accountLayout = findViewById(R.id.accountLayout);
        LinearLayout notificationsLayout = findViewById(R.id.NotificationsLayout);
        LinearLayout logoutLayout = findViewById(R.id.LogoutLayout);
        LinearLayout deleteLayout = findViewById(R.id.DeleteLayout);
        ImageView backButton = findViewById(R.id.backButton);

        accountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(three_dots.this, Account_Settings.class);
                startActivity(intent);
            }
        });

        notificationsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(three_dots.this, Notifications.class);
                startActivity(intent);
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteAccountDialog();
            }
        });

        // Add onClick listener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(three_dots.this, Home.class);
                startActivity(intent);
            }
        });
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes button
                        mAuth.signOut();
                        Intent intent = new Intent(three_dots.this, login.class);
                        startActivity(intent);
                        finish(); // Close this activity
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes button
                        deleteAccount();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    private void deleteAccount() {
        // Delete account details from Firebase Authentication
        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Account deleted successfully, now delete from Firestore
                        deleteAccountFromFirestore();
                    } else {
                        // Handle account deletion failure
                        // You can show a Toast or handle it as per your app's requirement
                    }
                });
    }

    private void deleteAccountFromFirestore() {
        // Delete account details from Firestore
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Firestore document deleted successfully
                    // Navigate user back to login screen or any appropriate screen
                    Intent intent = new Intent(three_dots.this, sign_in.class);
                    startActivity(intent);
                    finish(); // Close this activity
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore deletion failure
                    // You can show a Toast or handle it as per your app's requirement
                });
    }
}
