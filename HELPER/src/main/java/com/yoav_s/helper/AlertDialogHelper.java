package com.yoav_s.helper;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;


public class AlertDialogHelper {

    public static void alertOk(Context context, String title, String message, boolean cancelAble, int icon){
        alert(context, title, message, cancelAble, icon, "OK", null, null, null, null, null);
    }

    public static void showAccept(Context context, String message ){
        alert(context, "OK", message, true, R.drawable.ok, "Accept", null, null, null, null, null);
    }

    // Simple error dialog
    public static void showError(Context context, String message) {
        alert(context, "Error", message, true, R.drawable.error, "OK", null, null, null, null, null);
    }

    // Error dialog with callback
    public static void showError(Context context, String message, Runnable onDismiss) {
        alert(context, "Error", message, true, R.drawable.error, "OK", null, null, onDismiss, null, null);
    }

    // Simple confirmation dialog
    public static void showConfirmation(Context context, String message, Runnable onConfirm) {
        alert(context, "Confirmation", message, true, R.drawable.confirmation, "OK", null, null, onConfirm, null, null);
    }

    // Confirmation with both callbacks
    public static void showConfirmation(Context context, String message,
                                        Runnable onConfirm, Runnable onCancel) {
        alert(context, "Confirmation", message, true, R.drawable.confirmation, "Yes", "No", null, onConfirm, onCancel, null);
    }

    // Info dialog
    public static void showInfo(Context context, String title, String message) {
        alert(context, title, message, true, R.drawable.information, "OK", null, null, null, null, null);
    }

    // Delete dialog
    public static void showDelete(Context context, String message, Runnable onDelete) {
        alert(context, "Delete", message, true, R.drawable.trash_can, "Yes", "No", null, onDelete, null, null);
    }

    // Delete dialog
    public static void showDelete(Context context, String message, Runnable onDelete, Runnable onCancel) {
        alert(context, "Delete", message, true, R.drawable.trash_can, "Yes", "No", null, onDelete, onCancel, null);
    }

    public static void showTakePicture(Context context, Runnable onTakePicture, Runnable onPickPicture) {
        alert(context,
                "Take picture",
                "Take picture from camera or gallery",
                true,
                R.drawable.camera,
                "Camera",
                "Gallery",
                "Cancel",
                onTakePicture,
                onPickPicture,
                null);
    }

    public static void showTakePicture(Context context, Runnable onTakePicture, Runnable onPickPicture, Runnable onCancel) {
        alert(context,
                "Take picture",
                "Take picture from camera or gallery",
                true,
                R.drawable.camera,
                "Camera",
                "Gallery",
                "Cancel",
                onTakePicture,
                onPickPicture,
                onCancel);
    }

    public static void alert(
            Context  context,
            String   title,
            String   message,
            boolean  cancelAble,
            int      icon,
            String   positiveButtonText,
            String   negativeButtonText,
            String   neutralButtonText,
            Runnable onPositiveClick,
            Runnable onNegativeClick,
            Runnable onNeutralClick) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelAble);

        if (icon != 0) {
            builder.setIcon(icon);
        }

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                if (onPositiveClick != null) {
                    onPositiveClick.run();
                }
            });
        }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> {
                if (onNegativeClick != null) {
                    onNegativeClick.run();
                }
            });
        }

        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, (dialog, which) -> {
                if (onNeutralClick != null) {
                    onNeutralClick.run();
                }
            });
        }

        builder.show();
    }

    /*
    USAGE:
        // Example 1: Dialog with all three buttons
        AlertDialogHelper.alert(
            context,
            "Confirm Action",
            "Do you want to save changes?",
            false,
            R.drawable.ic_warning,
            "Save",           // positive button
            "Don't Save",     // negative button
            "Cancel",         // neutral button
            () -> saveChanges(),              // positive action
            () -> discardChanges(),           // negative action
            () -> cancelOperation()           // neutral action
        );

        // Example 2: Dialog with only positive and negative buttons
        AlertDialogHelper.alert(
            context,
            "Delete Item",
            "Are you sure you want to delete this item?",
            false,
            R.drawable.ic_delete,
            "Yes",            // positive button
            "No",             // negative button
            null,             // no neutral button
            () -> deleteItem(),               // positive action
            () -> cancelDeletion(),           // negative action
            null                              // no neutral action
        );

        // Example 3: Dialog with only positive button and custom text
        AlertDialogHelper.alert(
            context,
            "Success",
            "Operation completed successfully",
            true,
            R.drawable.ic_success,
            "Great!",         // positive button
            null,             // no negative button
            null,             // no neutral button
            () -> refreshScreen(),            // positive action
            null,                             // no negative action
            null                              // no neutral action
        );
     */

    public static void alert(
            Context        context,
            String         title,
            String         message,
            boolean        cancelable,
            int            icon,
            List<String>   items,
            List<Runnable> actions){

        if (items.size() != actions.size()) {
            throw new IllegalArgumentException("Items and actions lists must have the same size");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                //.setMessage(message)  // don't work with list
                .setCancelable(cancelable)
                .setItems(items.toArray(new String[0]), (dialog, which) -> {
                    if (actions.get(which) != null) {
                        actions.get(which).run();
                    }
                });

        if (icon != 0) {
            builder.setIcon(icon);
        }

        builder.show();
    }

    // USAGE
    //    List<String> options = Arrays.asList(
    //            "Save Document",
    //            "Share Document",
    //            "Print Document",
    //            "Delete Document"
    //    );
    //
    //    List<Runnable> actions = Arrays.asList(
    //            () -> saveDocument(),
    //            () -> shareDocument(),
    //            () -> printDocument(),
    //            () -> deleteDocument()
    //    );
    //
    //  AlertDialogHelper.alert(
    //      context,
    //      true,
    //      R.drawable.ic_document
    //      options,
    //      actions,
    //  );
}
