package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class adminLogin extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private CheckBox showPasswordCheckBox;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        editTextEmail = findViewById(R.id.textEmail);
        editTextPassword = findViewById(R.id.Password);
        buttonLogin = findViewById(R.id.buttonLogin);
        showPasswordCheckBox = findViewById(R.id.checkBoxShowPassword);

        dbRef = FirebaseDatabase.getInstance().getReference("users");

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                login(email, password);
            }
        });

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Show password
                    editTextPassword.setTransformationMethod(null);
                } else {
                    // Hide password
                    editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
                // Move cursor to the end of the text
                editTextPassword.setSelection(editTextPassword.getText().length());
            }
        });
    }

    private void login(String email, String password) {
        dbRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String dbPassword = snapshot.child("password").getValue(String.class);
                        String role = snapshot.child("role").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(password)) {
                            if ("admin".equals(role)) {
                                navigateToAdminDashboard();
                                Toast.makeText(adminLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(adminLogin.this, "User is not an admin", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(adminLogin.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(adminLogin.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(adminLogin.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(adminLogin.this, Admindashboard.class);
        startActivity(intent);
        finish();
    }
}
