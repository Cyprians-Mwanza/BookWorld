package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Recovery_code extends AppCompatActivity {

    private EditText otpEditText1, otpEditText2, otpEditText3, otpEditText4, otpEditText5, otpEditText6;
    private String email, otp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_code);

        mAuth = FirebaseAuth.getInstance();

        otpEditText1 = findViewById(R.id.otpDigit1);
        otpEditText2 = findViewById(R.id.otpDigit2);
        otpEditText3 = findViewById(R.id.otpDigit3);
        otpEditText4 = findViewById(R.id.otpDigit4);
        otpEditText5 = findViewById(R.id.otpDigit5);
        otpEditText6 = findViewById(R.id.otpDigit6);

        Button verifyButton = findViewById(R.id.buttonVerify);
       TextView resendButton = findViewById(R.id.resend);

        email = getIntent().getStringExtra("email");
        otp = getIntent().getStringExtra("otp");

        // Setup listeners for OTP EditTexts to move focus automatically
        setupOtpEditTextListeners();

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOtp = getEnteredOtp();
                if (enteredOtp.length() < 4) {
                    Toast.makeText(Recovery_code.this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
                } else {
                    verifyOTP(enteredOtp);
                }
            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resend OTP functionality (generate new OTP and resend)
                otp = generateOTP();
                sendOtpByEmail(email, otp);
            }
        });
    }

    private void setupOtpEditTextListeners() {
        otpEditText1.addTextChangedListener(new OtpTextWatcher(otpEditText1));
        otpEditText2.addTextChangedListener(new OtpTextWatcher(otpEditText2));
        otpEditText3.addTextChangedListener(new OtpTextWatcher(otpEditText3));
        otpEditText4.addTextChangedListener(new OtpTextWatcher(otpEditText4));
        otpEditText5.addTextChangedListener(new OtpTextWatcher(otpEditText5));
        otpEditText6.addTextChangedListener(new OtpTextWatcher(otpEditText6));
    }

    private class OtpTextWatcher implements TextWatcher {
        private final EditText currentEditText;

        OtpTextWatcher(EditText currentEditText) {
            this.currentEditText = currentEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1) {
                if (currentEditText == otpEditText1) {
                    otpEditText2.requestFocus();
                } else if (currentEditText == otpEditText2) {
                    otpEditText3.requestFocus();
                } else if (currentEditText == otpEditText3) {
                    otpEditText4.requestFocus();
                } else if (currentEditText == otpEditText4) {
                    // Last digit entered, verify OTP
                    String enteredOtp = getEnteredOtp();
                    if (enteredOtp.length() == 4) {
                        verifyOTP(enteredOtp);
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private String getEnteredOtp() {
        StringBuilder sb = new StringBuilder();
        sb.append(otpEditText1.getText().toString().trim());
        sb.append(otpEditText2.getText().toString().trim());
        sb.append(otpEditText3.getText().toString().trim());
        sb.append(otpEditText4.getText().toString().trim());
        return sb.toString();
    }

    private void verifyOTP(String enteredOtp) {
        if (enteredOtp.equals(otp)) {
            // OTP is correct, navigate to ResetPasswordActivity or similar
            Intent intent = new Intent(Recovery_code.this, reset_password.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish(); // Finish current activity
        } else {
            Toast.makeText(Recovery_code.this, "Incorrect OTP, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateOTP() {
        // Generate a new OTP
        // This is a simple example, replace with your actual OTP generation logic
        // Random 4-digit OTP
        return String.valueOf((int) ((Math.random() * (9999 - 1000)) + 1000));
    }

    private void sendOtpByEmail(String email, String otp) {
        // Here, you would send the OTP to the user's email using your backend or a third-party service
        // Replace this with your actual sending logic
        // For demonstration purposes, we'll just log the OTP and show a Toast
        Toast.makeText(Recovery_code.this, "OTP resent to " + email + ": " + otp, Toast.LENGTH_SHORT).show();
    }
}
