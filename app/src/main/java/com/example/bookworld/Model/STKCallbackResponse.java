package com.example.bookworld.Model;

import com.google.gson.annotations.SerializedName;

public class STKCallbackResponse {

    @SerializedName("ResultCode")
    private String resultCode;
    @SerializedName("ResultDesc")
    private String resultDesc;
    @SerializedName("CheckoutRequestID")
    private String checkoutRequestID;
    // Add more like responseCode, responseDescription,merchantRequestID,
    @SerializedName("ResponseCode")
    private String responseCode;
    @SerializedName("ResponseDescription")
    private String responseDescription;
    @SerializedName("MerchantRequestID")
    private String merchantRequestID;



    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    public String getCheckoutRequestID() {
        return checkoutRequestID;
    }

    public void setCheckoutRequestID(String checkoutRequestID) {
        this.checkoutRequestID = checkoutRequestID;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getMerchantRequestID() {
        return merchantRequestID;
    }

    public void setMerchantRequestID(String merchantRequestID) {
        this.merchantRequestID = merchantRequestID;
    }


}
