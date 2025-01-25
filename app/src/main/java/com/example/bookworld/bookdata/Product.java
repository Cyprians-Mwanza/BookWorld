package com.example.bookworld.bookdata;

public class Product {
    private String name, image, price, quantity, barcode, itemId, category, itemTotal, totalAmount, itemCount, remainingQuantity, soldQuantity;

    public Product() {}

    public Product(String name, String image, String price, String quantity, String barcode, String itemId, String category, String itemTotal, String totalAmount, String itemCount, String remainingQuantity, String soldQuantity) {
        this.name = name;
        this.image = image;
        this.price = price;
        this.quantity = quantity;
        this.barcode = barcode;
        this.itemId = itemId;
        this.category = category;
        this.itemTotal = itemTotal;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.remainingQuantity = remainingQuantity;
        this.soldQuantity = soldQuantity;
    }

    // Public getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(String itemTotal) {
        this.itemTotal = itemTotal;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

    public String getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(String remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(String soldQuantity) {
        this.soldQuantity = soldQuantity;
    }
}
