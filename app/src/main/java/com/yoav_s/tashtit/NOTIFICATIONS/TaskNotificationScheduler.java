package com.yoav_s.tashtit.NOTIFICATIONS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.yoav_s.model.CareTask;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Setting;
import com.yoav_s.model.TaskNotificationSchedule;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TaskNotificationScheduler {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String EXTRA_PLANT_NICKNAME = "plant_nickname";
    public static final String EXTRA_SNOOZE_MINUTES = "snooze_minutes";
    public static final String EXTRA_PLANT = "plant";
    public static final String EXTRA_USER_DISPLAY_NAME = "user_display_name";

    private TaskNotificationScheduler() {}

    public static void rescheduleAll(Context context, TaskNotificationSchedule schedule) {
        if (context == null || schedule == null) {
            return;
        }

        Setting setting = schedule.getSetting();

        if (setting == null) {
            return;
        }

        Set<String> plantIds = new HashSet<>();
        Map<String, String> plantNicknameById = new HashMap<>();
        Map<String, Plant> plantById = new HashMap<>();

        if (schedule.getPlants() != null) {
            for (Plant plant : schedule.getPlants()) {
                if (plant == null || plant.getIdFs() == null) continue;

                plantIds.add(plant.getIdFs());
                plantNicknameById.put(
                        plant.getIdFs(),
                        plant.getNickname() != null ? plant.getNickname() : "-"
                );
                plantById.put(plant.getIdFs(), plant);
            }
        }

        cancelAllForTasks(context, schedule);

        if (!setting.isNotificationsEnabled()) {
            return;
        }

        if (schedule.getCareTasks() == null) {
            return;
        }

        for (CareTask task : schedule.getCareTasks()) {
            if (task == null) continue;

            if (task.getPlantId() == null || !plantIds.contains(task.getPlantId())) continue;
            if (task.getState() != CareTask.State.SCHEDULED) continue;
            if (task.getNextDueAt() == null) continue;

            long triggerAtMillis = calculateTriggerTime(
                    task.getNextDueAt().toDate(),
                    setting.getReminderTime()
            );

            if (triggerAtMillis <= System.currentTimeMillis()) {
                continue;
            }

            scheduleTaskNotification(
                    context,
                    task.getIdFs(),
                    formatTaskType(task.getType()),
                    plantNicknameById.get(task.getPlantId()),
                    plantById.get(task.getPlantId()),
                    schedule.getUserDisplayName(),
                    setting.getSnoozeTime(),
                    triggerAtMillis
            );
        }
    }

    private static void cancelAllForTasks(Context context, TaskNotificationSchedule schedule) {
        if (schedule == null || schedule.getCareTasks() == null) {
            return;
        }

        for (CareTask task : schedule.getCareTasks()) {
            if (task == null) continue;
            cancelReminder(context, task.getIdFs());
        }
    }

    private static long calculateTriggerTime(Date dueDate, String reminderTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dueDate);

        int hour = 9;
        int minute = 0;

        try {
            if (reminderTime != null && reminderTime.contains(":")) {
                String[] parts = reminderTime.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private static void scheduleTaskNotification(Context context,
                                                 String taskId,
                                                 String taskType,
                                                 String plantNickname,
                                                 Plant plant,
                                                 String userDisplayName,
                                                 int snoozeMinutes,
                                                 long triggerAtMillis) {
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_TYPE, taskType);
        intent.putExtra(EXTRA_PLANT_NICKNAME, plantNickname != null ? plantNickname : "-");
        intent.putExtra(EXTRA_PLANT, plant);
        intent.putExtra(EXTRA_USER_DISPLAY_NAME, userDisplayName != null ? userDisplayName : "-");
        intent.putExtra(EXTRA_SNOOZE_MINUTES, Math.max(snoozeMinutes, 1));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                buildRequestCode(taskId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
        );
    }

    public static void cancelReminder(Context context, String taskId) {
        Intent intent = new Intent(context, TaskReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                buildRequestCode(taskId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private static int buildRequestCode(String taskId) {
        return taskId != null ? taskId.hashCode() : 0;
    }

    private static String formatTaskType(CareTask.Type type) {
        if (type == null) return "Care task";

        String value = type.name().toLowerCase().replace('_', ' ');
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}