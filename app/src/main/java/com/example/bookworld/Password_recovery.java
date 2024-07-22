package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.Random;

public class Password_recovery extends AppCompatActivity {

    private EditText emailEditText;
    private Button recoverPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.EmailText);
        recoverPasswordButton = findViewById(R.id.buttonpass);

        recoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(Password_recovery.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                } else {
                    // Generate OTP
                    String otp = generateOTP();

                    // Send OTP via email (replace with your actual sending logic)
                    sendOtpByEmail(email, otp);
                }
            }
        });
    }

    private String generateOTP() {
        // Generate a random 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void sendOtpByEmail(String email, String otp) {
        // Here, you would send the OTP to the user's email using your backend or a third-party service
        // Replace this with your actual sending logic

        // For demonstration purposes, we'll just log the OTP
        Log.d("PasswordRecoveryActivity", "Generated OTP: " + otp);

        // Navigate to RecoveryCodeActivity or LoginActivity
        Intent intent = new Intent(Password_recovery.this, Recovery_code.class);
        intent.putExtra("email", email);
        intent.putExtra("otp", otp);
        startActivity(intent);
        finish(); // Finish current activity
    }
}
