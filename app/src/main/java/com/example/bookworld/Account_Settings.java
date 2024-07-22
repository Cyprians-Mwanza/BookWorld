package com.example.bookworld;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Account_Settings extends AppCompatActivity {
    private EditText etCurrentEmail;
    private EditText etFullName;
    private EditText etUsername;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        ImageView backButton = findViewById(R.id.backButton);
        LinearLayout homeLayout = findViewById(R.id.homelayout1);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout1);
        LinearLayout searchLayout = findViewById(R.id.searchbutton1);
        LinearLayout moreLayout = findViewById(R.id.morelayout1);
        ImageView threeDotsButton = findViewById(R.id.logoutButton);
        etCurrentEmail = findViewById(R.id.et_current_email);
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        EditText etCurrentPassword = findViewById(R.id.et_current_password);
        EditText etNewPassword = findViewById(R.id.et_new_password);
        EditText etConfirmPassword = findViewById(R.id.confirm_password);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(Account_Settings.this, three_dots.class);
                startActivity(intent);
            }
        });
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account_Settings.this, Home.class);
                startActivity(intent);
            }
        });

        myBooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account_Settings.this, MyBooks.class);
                startActivity(intent);
            }
        });

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account_Settings.this, search_discovery.class);
                startActivity(intent);
            }
        });

        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account_Settings.this, More.class);
                startActivity(intent);
            }
        });

        threeDotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(Account_Settings.this,three_dots.class);
                startActivity(intent);
            }
        });

        loadUserInfo();
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();
            etCurrentEmail.setText(email);

            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        etFullName.setText(fullName);
                        etUsername.setText(username);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        }
    }
}
