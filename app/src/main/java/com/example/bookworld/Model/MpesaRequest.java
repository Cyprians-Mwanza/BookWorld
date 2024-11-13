package com.example.bookworld.Model;


import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class MpesaRequest {

    public interface MpesaRequestCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public void sendRequest(String businessShortCode, String password, String timestamp, String checkoutRequestID, String token, MpesaRequestCallback callback) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject json = new JSONObject();
        try {
            json.put("BusinessShortCode", businessShortCode);
            json.put("Password", password);
            json.put("Timestamp", timestamp);
            json.put("CheckoutRequestID", checkoutRequestID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(mediaType, json.toString());
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Handle the response
                    callback.onSuccess(responseData);


                } else {
                    // Handle the error
                    callback.onFailure("Request failed: " + response.message());
                }
            }
        });
    }
}

