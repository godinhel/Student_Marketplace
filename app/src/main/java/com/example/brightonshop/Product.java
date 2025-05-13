package com.example.brightonshop;
public class Product {
    private String title;
    private String description;
    private String image_url;
    private String price;
    private String user;

    public Product() {} // Required for Firestore

    public Product(String title, String description, String image_url, String price, String user) {
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.price = price;
        this.user = user;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImage_url() { return image_url; }
    public String getPrice() { return price; }
    public String getUser() { return user; }
}
