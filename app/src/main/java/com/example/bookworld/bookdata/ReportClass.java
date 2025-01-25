package com.example.bookworld.bookdata;

public class ReportClass {
    private String name;
    private String title;
    private String price;

    public ReportClass(String name, String title, String price) {
        this.name = name;
        this.title = title;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }
}
