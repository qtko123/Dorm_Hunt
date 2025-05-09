package com.example.dormhunt.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final String IMAGE_DIRECTORY = "dorm_images";
    
    public static String saveImage(Context context, Uri imageUri) {
        try {
            // Generate unique filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "DORM_" + timeStamp + ".jpg";

            // Create directory if it doesn't exist
            File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file
            File imageFile = new File(directory, imageFileName);

            // Copy image from Uri to our app's private storage
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            FileOutputStream fos = new FileOutputStream(imageFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            
            fos.close();
            inputStream.close();

            return imageFileName;
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    public static Bitmap loadImage(Context context, String fileName) {
        if (fileName == null) return null;

        try {
            File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
            File imageFile = new File(directory, fileName);

            if (!imageFile.exists()) {
                Log.e(TAG, "Image file not found: " + fileName);
                return null;
            }

            return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            return null;
        }
    }

    public static boolean deleteImage(Context context, String fileName) {
        if (fileName == null) return false;

        try {
            File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
            File imageFile = new File(directory, fileName);
            return imageFile.delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + e.getMessage());
            return false;
        }
    }

    public static String getImagePath(Context context, String fileName) {
        if (fileName == null) return null;
        
        File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
        File imageFile = new File(directory, fileName);
        return imageFile.exists() ? imageFile.getAbsolutePath() : null;
    }

    public static boolean imageExists(Context context, String fileName) {
        if (fileName == null) return false;
        
        File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
        File imageFile = new File(directory, fileName);
        return imageFile.exists();
    }

    public static Bitmap resizeBitmap(Bitmap source, int maxWidth, int maxHeight) {
        if (source == null) return null;

        int width = source.getWidth();
        int height = source.getHeight();
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int targetWidth = Math.round(width * ratio);
        int targetHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }
}