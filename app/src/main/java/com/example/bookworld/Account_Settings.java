package com.example.bookworld;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Account_Settings extends AppCompatActivity {

    private EditText etCurrentEmail, etCurrentPassword, etUsername, etNewPhone, etNewEmail, etNewPassword, etConfirmPassword;
    private Button btnSaveChanges;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        etCurrentEmail = findViewById(R.id.et_current_email);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etUsername = findViewById(R.id.et_username);
        etNewPhone = findViewById(R.id.et_new_phone);
        etNewEmail = findViewById(R.id.et_new_email);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.confirm_password);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Fetch current user information and populate the fields
        fetchUserData();

        // Set up click listener for Save Changes button
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUserData(); // Refresh user data
                swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
            }
        });
    }

    private void fetchUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("users").child(userId);

            userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("username").getValue(String.class);
                            String phone = dataSnapshot.child("phone").getValue(String.class);
                            String email = dataSnapshot.child("email").getValue(String.class);

                            etUsername.setText(username);
                            etNewPhone.setText(phone);
                            etCurrentEmail.setText(email);
                        } else {
                            Toast.makeText(Account_Settings.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Account_Settings.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        String currentEmail = etCurrentEmail.getText().toString().trim();
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        String newPhone = etNewPhone.getText().toString().trim();
        String newEmail = etNewEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentEmail.isEmpty() || currentPassword.isEmpty()) {
            Toast.makeText(this, "Current email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.signInWithEmailAndPassword(currentEmail, currentPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userId = user.getUid();
                                DatabaseReference userRef = mDatabase.child("users").child(userId);

                                // Update email if provided
                                if (!newEmail.isEmpty() && !newEmail.equals(currentEmail)) {
                                    user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> emailTask) {
                                            if (emailTask.isSuccessful()) {
                                                // Update the email in the database
                                                userRef.child("email").setValue(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> dbTask) {
                                                        if (dbTask.isSuccessful()) {
                                                            Toast.makeText(Account_Settings.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Account_Settings.this, "Failed to update email in database", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(Account_Settings.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                                // Update password if provided and matches confirmation
                                if (!newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
                                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> passwordTask) {
                                            if (passwordTask.isSuccessful()) {
                                                Toast.makeText(Account_Settings.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Account_Settings.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                                // Update other user information (username and phone)
                                Map<String, Object> updates = new HashMap<>();
                                if (!newUsername.isEmpty()) {
                                    updates.put("username", newUsername);
                                }
                                if (!newPhone.isEmpty()) {
                                    updates.put("phone", newPhone);
                                }

                                if (!updates.isEmpty()) {
                                    userRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> updateTask) {
                                            if (updateTask.isSuccessful()) {
                                                // Refresh the UI with the updated details
                                                etUsername.setText(newUsername);
                                                etNewPhone.setText(newPhone);
                                                etCurrentEmail.setText(newEmail.isEmpty() ? currentEmail : newEmail);

                                                Toast.makeText(Account_Settings.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Account_Settings.this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(Account_Settings.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
