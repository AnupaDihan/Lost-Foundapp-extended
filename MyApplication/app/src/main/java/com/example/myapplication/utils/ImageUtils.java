package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(context.getFilesDir(), fileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: " + imageUri);
                return "";
            }

            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            Log.d(TAG, "Image saved successfully to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return "";
        }
    }

    public static Bitmap loadImageFromPath(String path) {
        try {
            if (path == null || path.isEmpty()) return null;
            File file = new File(path);
            if (file.exists()) {
                return BitmapFactory.decodeFile(path);
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap: " + e.getMessage());
            return null;
        }
    }
}
