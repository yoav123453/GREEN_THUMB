package com.yoav_s.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.util.function.Consumer;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class LauncherHelper {

    private ComponentActivity activity;

    // Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String> mediaPickerLauncher;
    private ActivityResultLauncher<Intent> generalActivityLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    // Callbacks
    private Consumer<Bitmap> cameraCallback;
    private Consumer<Boolean> permissionCallback;
    private Consumer<Uri> mediaCallback;
    private Consumer<ActivityResult> generalActivityCallback;
    private Consumer<Uri> takePictureCallback;
    private Uri currentTakePictureUri;

    public LauncherHelper(ComponentActivity activity) {
        this.activity = activity;
        setupLaunchers();
    }

    private void setupLaunchers() {
        // Camera Launcher
        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (cameraCallback != null) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                            cameraCallback.accept(bitmap);
                        } else {
                            cameraCallback.accept(null);
                        }
                    }
                }
        );

        // Permission Launcher
        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (permissionCallback != null) {
                        permissionCallback.accept(isGranted);
                    }
                }
        );

        // Media Picker Launcher
        mediaPickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (mediaCallback != null) {
                        mediaCallback.accept(uri);
                    }
                }
        );

        // General Activity Launcher
        generalActivityLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (generalActivityCallback != null) {
                        generalActivityCallback.accept(result);
                    }
                }
        );

        // Take Picture Launcher
        takePictureLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (takePictureCallback != null) {
                        // If successful, return the URI; if failed, return null
                        takePictureCallback.accept(success ? currentTakePictureUri : null);
                        currentTakePictureUri = null; // Clear the stored URI
                    }
                }
        );
    }

    // Public methods
    public void requestCameraPermission(Consumer<Boolean> callback) {
        this.permissionCallback = callback;
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    public void takePhoto(Consumer<Bitmap> callback) {
        this.cameraCallback = callback;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    // Helper method - בדיקה ולקיחת תמונה
    public void takePhotoWithPermissionCheck(Consumer<Bitmap> callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            takePhoto(callback);
        } else {
            requestCameraPermission(granted -> {
                if (granted) {
                    takePhoto(callback);
                } else {
                    callback.accept(null); // הרשאה נדחתה
                }
            });
        }
    }

    /**
     * Take a full resolution picture and save it to the provided URI
     * @param outputUri The URI where the picture should be saved
     * @param callback Callback with the URI if successful, null if failed
     */
    public void takePicture(Uri outputUri, Consumer<Uri> callback) {
        this.takePictureCallback = callback;
        this.currentTakePictureUri = outputUri;
        takePictureLauncher.launch(outputUri);
    }

    /**
     * Take a full resolution picture with permission check
     * @param outputUri The URI where the picture should be saved
     * @param callback Callback with the URI if successful, null if failed
     */
    public void takePictureWithPermissionCheck(Uri outputUri, Consumer<Uri> callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            takePicture(outputUri, callback);
        } else {
            requestCameraPermission(granted -> {
                if (granted) {
                    takePicture(outputUri, callback);
                } else {
                    callback.accept(null); // הרשאה נדחתה
                }
            });
        }
    }

    public void pickImage(Consumer<Uri> callback) {
        this.mediaCallback = callback;
        mediaPickerLauncher.launch("image/*");
    }

    //region General Activity Launcher method
    /**
     * Launch an activity by class with optional callback
     */
    public void launchActivity(Class<?> activityClass, Consumer<ActivityResult> callback) {
        Intent intent = new Intent(activity, activityClass);
        launchActivity(intent, callback);
    }

    /**
     * Launch an activity by class with data and optional callback
     */
    public void launchActivity(Class<?> activityClass, Bundle extras, Consumer<ActivityResult> callback) {
        Intent intent = new Intent(activity, activityClass);
        if (extras != null) {
            intent.putExtras(extras);
        }
        launchActivity(intent, callback);
    }

    /**
     * Launch an activity with custom intent and optional callback
     */
    public void launchActivity(Intent intent, Consumer<ActivityResult> callback) {
        this.generalActivityCallback = callback;
        generalActivityLauncher.launch(intent);
    }

    /**
     * Launch an activity without expecting a result (fire and forget)
     */
    public void launchActivity(Class<?> activityClass) {
        launchActivity(activityClass, (Consumer<ActivityResult>) null);
    }

    /**
     * Launch an activity with data without expecting a result
     */
    public void launchActivity(Class<?> activityClass, Bundle extras) {
        launchActivity(activityClass, extras, null);
    }

    // Helper methods for common result handling

    /**
     * Check if the activity result was successful
     */
    public static boolean isResultOk(ActivityResult result) {
        return result.getResultCode() == Activity.RESULT_OK;
    }

    /**
     * Get data from activity result safely
     */
    public static Intent getResultData(ActivityResult result) {
        return result.getData();
    }

    /**
     * Get extras from activity result safely
     */
    public static Bundle getResultExtras(ActivityResult result) {
        Intent data = result.getData();
        return data != null ? data.getExtras() : null;
    }
    //endregion General Activity Launcher method
}