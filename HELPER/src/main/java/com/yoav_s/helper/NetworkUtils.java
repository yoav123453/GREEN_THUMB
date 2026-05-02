package com.yoav_s.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class NetworkUtils {

    public static boolean hasInternetConnection(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();

        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork);

        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public static boolean requireInternet(Activity activity) {
        if (hasInternetConnection(activity)) {
            return true;
        }

        Toast.makeText(
                activity,
                "No internet connection. Please connect to Wi-Fi or mobile data.",
                Toast.LENGTH_LONG
        ).show();

        return false;
    }

    public static void showNoInternetDialog(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("No internet connection")
                .setMessage("GreenThumb needs internet connection. Please connect to Wi-Fi or mobile data and try again.")
                .setPositiveButton("Open settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    activity.startActivity(intent);
                })
                .setNegativeButton("OK", null)
                .show();
    }
}