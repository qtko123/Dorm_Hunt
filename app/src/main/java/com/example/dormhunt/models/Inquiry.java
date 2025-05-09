package com.example.dormhunt.models;

import com.google.firebase.Timestamp;

public class Inquiry {
    private String id;
    private String userId;
    private String dormId;
    private String ownerId;
    private String status;
    private String userName;
    private String dormName;
    private Timestamp timestamp;
    private double price;
    private String paymentMethod;
    private String message;
    private String ownerName;

    public Inquiry() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDormId() { return dormId; }
    public void setDormId(String dormId) { this.dormId = dormId; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDormName() { return dormName; }
    public void setDormName(String dormName) { this.dormName = dormName; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public long getTimestampMillis() {
        return timestamp != null ? timestamp.toDate().getTime() : 0;
    }
}