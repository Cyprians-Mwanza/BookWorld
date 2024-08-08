package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class More extends AppCompatActivity {

    private static final String TAG = "More";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        // Initialize views
        LinearLayout groupCard = findViewById(R.id.group);
        LinearLayout helpCard = findViewById(R.id.help);
        LinearLayout borrowCard = findViewById(R.id.borrow);
        LinearLayout myProfileCard = findViewById(R.id.ic_MyProfile);

        // Set click listeners
        groupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Group card clicked");
                Intent intent = new Intent(More.this, Group.class);
                startActivity(intent);
            }
        });

        helpCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Help card clicked");
                Intent intent = new Intent(More.this, Account_Settings.class);
                startActivity(intent);
            }
        });

        borrowCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Borrow card clicked");
                Intent intent = new Intent(More.this, BookPrice.class);
                startActivity(intent);
            }
        });

        myProfileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "My Profile card clicked");
                Intent intent = new Intent(More.this, Account_Settings.class);
                startActivity(intent);
            }
        });
    }
}
