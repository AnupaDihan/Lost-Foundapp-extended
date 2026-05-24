package com.example.myapplication.models;

public class Item {
    private long id;
    private String title;
    private String description;
    private String category;
    private String type; // "lost" or "found"
    private String location;
    private String contactInfo;
    private String date;
    private String imagePath;
    private long timestamp;
    private boolean isResolved;

    // New fields for geo features
    private double latitude;
    private double longitude;

    // Full constructor
    public Item(long id, String title, String description, String category, String type,
                String location, String contactInfo, String date, String imagePath,
                long timestamp, boolean isResolved, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.location = location;
        this.contactInfo = contactInfo;
        this.date = date;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
        this.isResolved = isResolved;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Constructor without ID (for new items)
    public Item(String title, String description, String category, String type,
                String location, String contactInfo, String date, String imagePath,
                double latitude, double longitude) {
        this(0, title, description, category, type, location, contactInfo, date, imagePath,
                System.currentTimeMillis(), false, latitude, longitude);
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}