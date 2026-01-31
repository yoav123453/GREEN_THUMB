package com.yoav_s.helper.inputValidators;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import java.util.function.Supplier;

/**
 * ImageRule checks the current bitmap supplied by bitmapSupplier.
 * Valid if and only if bitmapSupplier.get() != null.
 */
public class ImageRule extends Rule {

    private final Supplier<Bitmap> bitmapSupplier;
    //private final int badgeImageResId;
    private ImageView ivError;

    /**
     * @param view the ImageView associated with this rule (UI anchor)
     * @param bitmapSupplier supplies the current Bitmap (null if no image selected)
     * @param message error message to show (Validator handles UI)
     //* @param badgeImageResId icon shown in the badge overlay
     * @param ivError icon shown next to the ImageView
     */
    public ImageRule(View view, Supplier<Bitmap> bitmapSupplier, String message, /*int badgeImageResId*/ ImageView ivError) {
        super(view, RuleOperation.REQUIRED, message);

        if (bitmapSupplier == null)
            throw new IllegalArgumentException("bitmapSupplier cannot be null");

        this.bitmapSupplier = bitmapSupplier;
        //this.badgeImageResId = badgeImageResId;
        this.ivError = ivError;
    }

    /**
     * Valid only when the user picked an image.
     */
    @Override
    public boolean getIsValid() {
        Bitmap b = bitmapSupplier.get();
        return b != null;
    }

    public void showError(){
        ivError.setVisibility(VISIBLE);
    }

    public void removeError(){
        ivError.setVisibility(GONE);
    }

    /**
     * Badge icon for UI indication (Validator will draw it)
     */
    /*
    public int getBadgeImage() {
        //return badgeImageResId;
    }

    public void onError(ImageView imageView) {
        showTooltip(imageView, message);
        addBadge(imageView);
    }

    public void onSuccess(ImageView imageView) {
        removeBadge(imageView);
    }

    // --------------------------
    // TooltipCompat (NO LAYOUT CHANGES!)
    // --------------------------
    public void showTooltip(View anchor, String message) {
        TooltipCompat.setTooltipText(anchor, message);

        // Force show tooltip
        anchor.post(() -> anchor.performLongClick());
    }
     */

    // --------------------------
    // Badge overlay (safe for ConstraintLayout)
    // --------------------------
//    public void addBadge(ImageView imageView) {
//        imageView.setForeground(
//               imageView.getContext().getDrawable(/*android.R.drawable.stat_notify_error*/ badgeImageResId)
//        );
//    }

//    public void removeBadge(ImageView imageView) {
//        imageView.setForeground(null);
//    }
}