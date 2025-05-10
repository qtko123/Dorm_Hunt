package com.example.dormhunt.models;

import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Dorm {
    private String id;
    private String ownerId;
    private String name;
    private String description;
    private String location;
    private double price;
    private List<String> amenities;
    private List<String> inclusions;
    private boolean isAvailable;
    private int maxOccupants;
    private int currentOccupants;
    private int viewCount;
    private String imagePath;
    private String imageResourceName;
    private String gender;
    private String occupancyStatus;
    private int pendingCount;
    private int approvedCount;
    private String ownerName;
    private String imageUrl;

    public Dorm() {
        this.amenities = new ArrayList<>();
        this.inclusions = new ArrayList<>();
    }

    public Dorm(String ownerId, String name, String description, String location,
                double price, List<String> amenities, String imagePath, String imageUrl) {
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
        this.price = price;
        this.amenities = amenities;
        this.imagePath = imagePath;
        this.isAvailable = true;
        this.maxOccupants = 4;
        this.currentOccupants = 0;
        this.viewCount = 0;
        this.inclusions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public boolean isAvailable() {
        return currentOccupants < maxOccupants;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public int getMaxOccupants() {
        return maxOccupants;
    }

    public void setMaxOccupants(int maxOccupants) {
        this.maxOccupants = maxOccupants;
    }

    public int getCurrentOccupants() {
        return currentOccupants;
    }

    public void setCurrentOccupants(int currentOccupants) {
        this.currentOccupants = currentOccupants;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
        this.approvedCount = approvedCount;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupancyStatus() {
        return String.format("%d/%d", currentOccupants, maxOccupants);
    }

    public String getAvailabilityStatus() {
        if (currentOccupants < maxOccupants) {
            int spotsLeft = maxOccupants - currentOccupants;
            return spotsLeft + " spot" + (spotsLeft > 1 ? "s" : "") + " available";
        }
        return "Full";
    }

    public List<String> getInclusions() {
        return inclusions;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}