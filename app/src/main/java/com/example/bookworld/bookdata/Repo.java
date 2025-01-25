package com.example.bookworld.bookdata;

public class Repo {

    String itemCount, paymentMethod, productName, totalPrice;

    public Repo() {}

    public Repo(String itemCount, String paymentMethod, String productName, String totalPrice) {
        this.itemCount = itemCount;
        this.paymentMethod = paymentMethod;
        this.productName = productName;
        this.totalPrice = totalPrice;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
