package com.example.bookworld;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookworld.Model.STKCallbackResponse;
import com.example.bookworld.Services.DarajaApiClient;
import com.example.bookworld.databinding.ActivityBuyBinding;
import com.example.bookworld.Model.AccessToken;
import com.example.bookworld.Model.STKPush;
import com.example.bookworld.Model.MpesaRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class BuyActivity extends AppCompatActivity implements View.OnClickListener {

    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    private ActivityBuyBinding binding;
    private String token, encodedPassword, timestamp;
    private EditText amount;
    private Button readButton;
    private String bookId, bookTitle, pdfUrl, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true);

        binding.btnPay.setOnClickListener(this);

        // Initialize read button and make it invisible at the start
        readButton = binding.readButton;
        readButton.setVisibility(View.GONE);

        // Retrieve book details from intent extras
        Intent intent = getIntent();
        bookId = intent.getStringExtra("BOOK_ID");
        bookTitle = intent.getStringExtra("BOOK_TITLE");
        pdfUrl = intent.getStringExtra("PDF_URL");
        price = intent.getStringExtra("BOOK_PRICE");

        // Log book details for debugging
        Log.d("BuyActivity", "Book ID: " + bookId);
        Log.d("BuyActivity", "Book Title: " + bookTitle);
        Log.d("BuyActivity", "PDF URL: " + pdfUrl);
        Log.d("BuyActivity", "Price: " + price);

        // Set the price in the amount EditText
        amount = binding.amount;
        amount.setText(price);

        // Set up the read button to open the PDF URL in a new activity
        readButton.setOnClickListener(v -> {
            if (pdfUrl != null) {
                Intent readIntent = new Intent(BuyActivity.this, ContentActivity.class);
                readIntent.putExtra("PDF_URL", pdfUrl);
                startActivity(readIntent);
            } else {
                Toast.makeText(BuyActivity.this, "PDF URL not available", Toast.LENGTH_SHORT).show();
            }
        });

        getAccessToken();
    }

    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mApiClient.setAuthToken(response.body().accessToken);
                    token = response.body().accessToken;
                } else {
                    Timber.e("Failed to get access token: %s", response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                Timber.e(t, "Failed to get access token");
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == binding.btnPay) {
            String phone_number = binding.phone.getText().toString().trim();
            String amount = binding.amount.getText().toString().trim();

            if (phone_number.isEmpty()) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            performSTKPush(phone_number, amount);
        }
    }

    public void performSTKPush(String phone_number, String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        timestamp = Utils.getTimestamp();
        String toEncode = "174379" + "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919" + timestamp;
        byte[] byteArray = toEncode.getBytes(StandardCharsets.UTF_8);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encodedPassword = Base64.getEncoder().encodeToString(byteArray);
        } else {
            encodedPassword = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
        }

        STKPush stkPush = new STKPush(
                "174379",
                encodedPassword,
                timestamp,
                "CustomerPayBillOnline",
                Integer.parseInt(amount),
                "254115036785",
                "174379",
                Utils.sanitizePhoneNumber(phone_number),
                "https://mydomain.com/path",
                "BookWorld",
                "Payment of " + bookTitle
        );

        mApiClient.setGetAccessToken(false);
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Timber.d("post submitted to API. %s", response.body());
                        checkTransactionStatus(response.body().getCheckoutRequestID());
                        Toast.makeText(BuyActivity.this, "Request sent. Please complete payment on your phone.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (response.errorBody() != null) {
                            Timber.e("Response %s", response.errorBody().string());
                            Toast.makeText(BuyActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e, "Error processing response");
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Timber.e(t, "Request failed");
            }
        });
    }

    public void checkTransactionStatus(String checkoutRequestID) {
        new Handler().postDelayed(() -> {
            MpesaRequest mpesaRequest = new MpesaRequest();
            mpesaRequest.sendRequest("174379", encodedPassword, timestamp, checkoutRequestID, token, new MpesaRequest.MpesaRequestCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("Transaction status response", response);
                    Gson gson = new Gson();
                    STKCallbackResponse stkCallbackResponse = gson.fromJson(response, STKCallbackResponse.class);

                    if (stkCallbackResponse.getResultCode().equals("0")) {
                        // Transaction successful
                        Toast.makeText(BuyActivity.this, "Transaction successful!", Toast.LENGTH_SHORT).show();

                        // Show the "Read" button after a successful transaction
                        readButton.setVisibility(View.VISIBLE);

                        // Save purchase details to Firestore
                        savePurchaseDetails();
                    } else {
                        // Transaction failed
                        Toast.makeText(BuyActivity.this, "Transaction failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String error) {
                    Timber.e("Failed to get transaction status: %s", error);
                    Toast.makeText(BuyActivity.this, "Failed to confirm transaction. Please check your network connection.", Toast.LENGTH_SHORT).show();
                }
            });
        }, 30000); // Check after 30 seconds
    }


    private void savePurchaseDetails() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userBooks = db.collection("Users").document(userId).collection("BoughtBooks");

        Map<String, Object> bookDetails = new HashMap<>();
        bookDetails.put("bookId", bookId);
        bookDetails.put("bookTitle", bookTitle);
        bookDetails.put("pdfUrl", pdfUrl);
        bookDetails.put("price", price);

        userBooks.add(bookDetails)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BuyActivity", "Book purchase details saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("BuyActivity", "Failed to save book purchase details", e);
                });
    }
}
