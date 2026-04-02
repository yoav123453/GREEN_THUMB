package com.yoav_s.tashtit.NOTIFICATIONS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.yoav_s.model.Plant;

public class TaskSnoozeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra(TaskNotificationScheduler.EXTRA_TASK_ID);
        String taskType = intent.getStringExtra(TaskNotificationScheduler.EXTRA_TASK_TYPE);
        String plantNickname = intent.getStringExtra(TaskNotificationScheduler.EXTRA_PLANT_NICKNAME);
        int snoozeMinutes = intent.getIntExtra(TaskNotificationScheduler.EXTRA_SNOOZE_MINUTES, 10);
        Plant plant = (Plant) intent.getSerializableExtra(TaskNotificationScheduler.EXTRA_PLANT);
        String userDisplayName = intent.getStringExtra(TaskNotificationScheduler.EXTRA_USER_DISPLAY_NAME);

        if (taskId != null) {
            NotificationManagerCompat.from(context).cancel(taskId.hashCode());
        }

        Intent reminderIntent = new Intent(context, TaskReminderReceiver.class);
        reminderIntent.putExtra(TaskNotificationScheduler.EXTRA_TASK_ID, taskId);
        reminderIntent.putExtra(TaskNotificationScheduler.EXTRA_TASK_TYPE, taskType);
        reminderIntent.putExtra(TaskNotificationScheduler.EXTRA_PLANT_NICKNAME, plantNickname);
        reminderIntent.putExtra(TaskNotificationScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes);
        reminderIntent.putExtra(TaskNotificationScheduler.EXTRA_PLANT, plant);
        reminderIntent.putExtra(TaskNotificationScheduler.EXTRA_USER_DISPLAY_NAME, userDisplayName);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId != null ? taskId.hashCode() : 0,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = System.currentTimeMillis() + (Math.max(snoozeMinutes, 1) * 60L * 1000L);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
            );
        }
    }
}