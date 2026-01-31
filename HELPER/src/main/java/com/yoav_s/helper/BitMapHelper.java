package com.yoav_s.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitMapHelper {

    public static String encodeTobase64(Bitmap image) {
        if (image != null) {
            Bitmap immagex = image;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            immagex.compress(Bitmap.CompressFormat.PNG, 90, baos);
            byte[] b = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
            return imageEncoded;
        } else
            return null;
    }

    public static String encodeTobase64(byte[] bytes){
        return encodeTobase64(byteArrayToBitmap(bytes));
    }

    public static Bitmap decodeBase64(String input) {
        if (input != null && !input.isEmpty()) {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } else
            return null;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }

    //region Resize
    /**
     * Resizes an original Bitmap to precisely match the dimensions of a given ImageView.
     *
     * @param originalBitmap The Bitmap image obtained from the camera.
     * @param imageView The ImageView whose dimensions the Bitmap should match.
     * @return A new Bitmap scaled to the ImageView's width and height, or the original Bitmap
     * if the ImageView's dimensions are not yet available (width or height is 0).
     */
    public static Bitmap resizeBitmapToImageViewSize(Bitmap originalBitmap, ImageView imageView) {
        // Get the actual measured width and height of the ImageView.
        // IMPORTANT: imageView.getWidth() and imageView.getHeight() will be 0 if the ImageView
        // has not yet been laid out on the screen. See the note below for how to handle this.
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();

        /*
        // Check if the ImageView has valid dimensions.
        // If not, it means the view hasn't been measured yet, so we return the original bitmap.
        if (targetWidth <= 0 || targetHeight <= 0) {
            Log.w("ImageResizer", "ImageView dimensions are not yet available (" + targetWidth + " x " + targetHeight + "). Returning original bitmap.");
            return originalBitmap;
        }


            // Create a new scaled bitmap using the ImageView's dimensions.
        // The 'filter' parameter (true) provides better quality scaling.
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);
         */

        return resizeBitmapToImageViewSize(originalBitmap, targetWidth, targetHeight);
    }


    /**
     * Resizes an original Bitmap to precisely match the dimensions of a given ImageView.
     *
     * @param originalBitmap The Bitmap image obtained from the camera.
     * @param targetWidth The width whose dimensions the Bitmap should match.
     * @param targetHeight The height whose dimensions the Bitmap should match.
     * @return A new Bitmap scaled to the ImageView's width and height, or the original Bitmap
     * if the ImageView's dimensions are not yet available (width or height is 0).
     */
    public static Bitmap resizeBitmapToImageViewSize(Bitmap originalBitmap, int targetWidth, int targetHeight) {
        // Check if the targetWidth and targetHeight has valid dimensions.
        if (targetWidth <= 0 || targetHeight <= 0) {
            Log.w("ImageResizer", "ImageView dimensions are not yet available (" + targetWidth + " x " + targetHeight + "). Returning original bitmap.");
            return originalBitmap;
        }

        // Create a new scaled bitmap using the ImageView's dimensions.
        // The 'filter' parameter (true) provides better quality scaling.
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);
    }


    /**
     * Rotates a Bitmap based on its EXIF orientation data.
     *
     * @param context The application context.
     * @param bitmap The Bitmap to be potentially rotated.
     * @param imageUri The Uri of the original image file, used to read EXIF data.
     * @return The rotated Bitmap, or the original Bitmap if no rotation is needed or an error occurs.
     */
    public static Bitmap rotateBitmapIfRequired(Context context, Bitmap bitmap, Uri imageUri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) {
                Log.e("ImageRotation", "Could not open input stream for URI: " + imageUri);
                return bitmap; // Return original if stream cannot be opened
            }

            ExifInterface ei = new ExifInterface(inputStream);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateBitmap(bitmap, 270);
                // Handle mirrored cases if necessary, though less common for simple rotation fixes
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    return flipBitmap(bitmap, true, false);
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return flipBitmap(bitmap, false, true);
                case ExifInterface.ORIENTATION_TRANSPOSE: // Rotate 90 and flip horizontal
                    return flipBitmap(rotateBitmap(bitmap, 90), true, false);
                case ExifInterface.ORIENTATION_TRANSVERSE: // Rotate 270 and flip horizontal
                    return flipBitmap(rotateBitmap(bitmap, 270), true, false);
                default:
                    return bitmap; // No rotation needed
            }
        } catch (IOException e) {
            Log.e("ImageRotation", "Error reading EXIF data or rotating bitmap: " + e.getMessage());
            return bitmap; // Return original if an error occurs
        }
    }

    /**
     * Helper method to perform the actual bitmap rotation.
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        // Create a new bitmap with the rotated pixels
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Helper method to perform bitmap flipping.
     */
    private static Bitmap flipBitmap(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        if (horizontal) {
            matrix.preScale(-1.0f, 1.0f);
        }
        if (vertical) {
            matrix.preScale(1.0f, -1.0f);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //endregion Resize

    //region URI
    // --- Helper Methods for URI to Bitmap Conversion ---
    // These methods perform the downsampling to prevent OutOfMemoryError.

    public static Bitmap getBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return bitmap;
        } catch (Exception e) {
            Log.e("BitmapHelper", "Error decoding bitmap from Uri: " + uri.toString(), e);
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    //endregion URI
}