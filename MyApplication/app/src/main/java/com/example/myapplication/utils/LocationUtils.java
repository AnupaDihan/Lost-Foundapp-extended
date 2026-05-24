package com.example.myapplication.utils;

import android.location.Location;

import com.example.myapplication.models.Item;

public class LocationUtils {

    // Calculate distance between two coordinates in kilometers
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    // Check if an item is within radius of user location
    public static boolean isWithinRadius(Item item, double userLat, double userLon, double radiusKm) {
        if (item.getLatitude() == 0 && item.getLongitude() == 0) {
            return false; // Item has no location coordinates
        }
        double distance = calculateDistance(userLat, userLon, item.getLatitude(), item.getLongitude());
        return distance <= radiusKm;
    }
}