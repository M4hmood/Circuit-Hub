package com.tekup.circuithub.models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private List<CartItem> items = new ArrayList<>();
    private double total;
    private String date;
    private String status;

    public Order() {}

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
    public double getTotal() { return total; }
    public String getDate() { return date; }
    public String getStatus() { return status; }

    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public void setTotal(double total) { this.total = total; }
    public void setDate(String date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }
}
