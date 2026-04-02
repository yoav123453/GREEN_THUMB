package com.yoav_s.tashtit.NOTIFICATIONS;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.yoav_s.model.Plant;
import com.yoav_s.tashtit.ACTIVITIES.PlantDetailsActivity;

public class TaskReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "care_task_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String taskId = intent.getStringExtra(TaskNotificationScheduler.EXTRA_TASK_ID);
        String taskType = intent.getStringExtra(TaskNotificationScheduler.EXTRA_TASK_TYPE);
        String plantNickname = intent.getStringExtra(TaskNotificationScheduler.EXTRA_PLANT_NICKNAME);
        String userDisplayName = intent.getStringExtra(TaskNotificationScheduler.EXTRA_USER_DISPLAY_NAME);
        int snoozeMinutes = intent.getIntExtra(TaskNotificationScheduler.EXTRA_SNOOZE_MINUTES, 10);

        createChannel(context);

        Plant plant = (Plant) intent.getSerializableExtra(TaskNotificationScheduler.EXTRA_PLANT);

        Intent openIntent = new Intent(context, PlantDetailsActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (plant != null) {
            openIntent.putExtra(PlantDetailsActivity.EXTRA_SELECTED_PLANT, plant);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                (taskId != null ? taskId.hashCode() : 0) + 100,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, TaskSnoozeReceiver.class);
        snoozeIntent.putExtra(TaskNotificationScheduler.EXTRA_TASK_ID, taskId);
        snoozeIntent.putExtra(TaskNotificationScheduler.EXTRA_TASK_TYPE, taskType);
        snoozeIntent.putExtra(TaskNotificationScheduler.EXTRA_PLANT_NICKNAME, plantNickname);
        snoozeIntent.putExtra(TaskNotificationScheduler.EXTRA_PLANT, plant);
        snoozeIntent.putExtra(TaskNotificationScheduler.EXTRA_USER_DISPLAY_NAME, userDisplayName);
        snoozeIntent.putExtra(TaskNotificationScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (taskId != null ? taskId.hashCode() : 0) + 200,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String safeDisplayName = (userDisplayName != null && !userDisplayName.trim().isEmpty())
                ? userDisplayName.trim()
                : "User";

        String title = "Hi " + safeDisplayName + "!";

        String body = "Time to " + (taskType != null ? taskType.toLowerCase() : "care for your plant")
                + " \"" + (plantNickname != null ? plantNickname : "-") + "\"";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .addAction(0, "Snooze", snoozePendingIntent);

        NotificationManagerCompat.from(context)
                .notify(taskId != null ? taskId.hashCode() : (int) System.currentTimeMillis(), builder.build());
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Care task reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for plant care tasks");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
