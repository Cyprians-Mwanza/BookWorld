package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button buttonSignUp, buttonLogin;
    private TextView AdminText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSignUp = findViewById(R.id.buttonSignup);
        buttonLogin = findViewById(R.id.buttonLogin);
        AdminText = findViewById(R.id.AdminLogin);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open SignUpActivity
                Intent intent = new Intent(MainActivity.this, sign_in.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open LoginActivity
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
            }
        });
        AdminText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open LoginActivity
                Intent intent = new Intent(MainActivity.this, adminLogin.class);
                startActivity(intent);
            }
        });
    }
}
