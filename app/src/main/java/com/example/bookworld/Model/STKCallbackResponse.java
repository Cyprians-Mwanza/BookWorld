package com.example.bookworld.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class STKCallbackResponse {

    @SerializedName("ResultCode")
    private String resultCode;
    @SerializedName("ResultDesc")
    private String resultDesc;
    @SerializedName("CheckoutRequestID")
    private String checkoutRequestID;
    @SerializedName("ResponseCode")
    private String responseCode;
    @SerializedName("ResponseDescription")
    private String responseDescription;
    @SerializedName("MerchantRequestID")
    private String merchantRequestID;
    @SerializedName("CallbackMetadata")
    private CallbackMetadata callbackMetadata;

    // Getters and Setters
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

    public CallbackMetadata getCallbackMetadata() {
        return callbackMetadata;
    }

    public void setCallbackMetadata(CallbackMetadata callbackMetadata) {
        this.callbackMetadata = callbackMetadata;
    }

    // Utility Method to Check Payment Status
    public boolean isPaymentSuccessful() {
        return "0".equals(resultCode);
    }

    // CallbackMetadata Class
    public static class CallbackMetadata {
        @SerializedName("Item")
        private List<MetadataItem> items;

        public List<MetadataItem> getItems() {
            return items;
        }

        public void setItems(List<MetadataItem> items) {
            this.items = items;
        }

        public static class MetadataItem {
            @SerializedName("Name")
            private String name;
            @SerializedName("Value")
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
