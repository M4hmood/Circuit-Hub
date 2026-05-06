package com.tekup.circuithub.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Product {
    private String id;
    private String name;
    private String category;
    private double price;
    private String description;
    private Map<String, String> specs = new LinkedHashMap<>();
    private String imageUrl;
    private byte[] imageData;
    private int stock;

    public Product() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public Map<String, String> getSpecs() { return specs; }
    public String getImageUrl() { return imageUrl; }
    public byte[] getImageData() { return imageData; }
    public int getStock() { return stock; }

    public void setId(String id) { this.id = id; }
    public void setName(String n) { this.name = n; }
    public void setCategory(String c) { this.category = c; }
    public void setPrice(double p) { this.price = p; }
    public void setDescription(String d) { this.description = d; }
    public void setSpecs(Map<String, String> s) { this.specs = s; }
    public void setImageUrl(String u) { this.imageUrl = u; }
    public void setImageData(byte[] d) { this.imageData = d; }
    public void setStock(int s) { this.stock = s; }
}
