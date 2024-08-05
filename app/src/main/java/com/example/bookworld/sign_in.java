package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class sign_in extends AppCompatActivity {

    private EditText emailEditText, usernameEditText, passwordEditText, confirmPasswordEditText, phoneEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailEditText = findViewById(R.id.Email);
        usernameEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        confirmPasswordEditText = findViewById(R.id.ConPassword);
        phoneEditText = findViewById(R.id.Phone);
        signUpButton = findViewById(R.id.buttonLogin);
        loginTextView = findViewById(R.id.CreateAccount);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    signUp();
                }
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(sign_in.this, login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email address is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            isValid = false;
        } else if (username.length() < 4 || username.length() > 10) {
            usernameEditText.setError("Username must be between 4 and 10 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
            isValid = false;
        } else if (!isValidPassword(password)) {
            passwordEditText.setError("Password should be a combination of numbers and letters");
            isValid = false;
            Toast.makeText(sign_in.this, "Password should be a combination of numbers and letters", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm password is required");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError("Passwords do not match");
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone contact is required");
            isValid = false;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            phoneEditText.setError("Invalid phone number");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidPassword(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return false;
    }

    private void signUp() {
        final String email = emailEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String phone = phoneEditText.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserDetails(email, username, phone);
                        } else {
                            Toast.makeText(sign_in.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDetails(final String email, final String username, final String phone) {
        final String userId = mAuth.getCurrentUser().getUid();
        final Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);
        user.put("phone", phone);

        // Save to Firestore
        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Save to Realtime Database
                            mDatabase.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(sign_in.this, "Sign Up Successful.", Toast.LENGTH_SHORT).show();
                                                navigateToLogin();
                                            } else {
                                                Toast.makeText(sign_in.this, "Failed to save user details: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(sign_in.this, "Failed to save user details: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(sign_in.this, login.class);
        startActivity(intent);
        finish();
    }
}
