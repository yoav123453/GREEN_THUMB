package com.yoav_s.tashtit.NOTIFICATIONS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Setting;
import com.yoav_s.model.User;

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

    public static void rescheduleAllForCurrentUser(Context context, User currentUser) {
        if (context == null || currentUser == null || currentUser.getIdFs() == null || currentUser.getIdFs().trim().isEmpty()) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = currentUser.getIdFs();

        Task<QuerySnapshot> settingsTask = db.collection("Settings")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> plantsTask = db.collection("Plants")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> careTasksTask = db.collection("CareTasks").get();

        Tasks.whenAllSuccess(settingsTask, plantsTask, careTasksTask)
                .addOnSuccessListener(results -> {
                    QuerySnapshot settingsSnapshot = (QuerySnapshot) results.get(0);
                    QuerySnapshot plantsSnapshot = (QuerySnapshot) results.get(1);
                    QuerySnapshot tasksSnapshot = (QuerySnapshot) results.get(2);

                    Setting setting = extractSetting(settingsSnapshot);

                    Set<String> plantIds = new HashSet<>();
                    Map<String, String> plantNicknameById = new HashMap<>();
                    Map<String, Plant> plantById = new HashMap<>();

                    for (DocumentSnapshot doc : plantsSnapshot.getDocuments()) {
                        Plant plant = doc.toObject(Plant.class);
                        if (plant == null) continue;

                        plant.setIdFs(doc.getId());
                        plantIds.add(doc.getId());
                        plantNicknameById.put(doc.getId(), plant.getNickname() != null ? plant.getNickname() : "-");
                        plantById.put(doc.getId(), plant);
                    }

                    cancelAllForPlants(context, plantIds, tasksSnapshot);

                    if (setting == null) {
                        setting = buildDefaultSetting(userId);
                    }

                    if (!setting.isNotificationsEnabled()) {
                        return;
                    }

                    for (DocumentSnapshot doc : tasksSnapshot.getDocuments()) {
                        CareTask task = doc.toObject(CareTask.class);
                        if (task == null) continue;

                        task.setIdFs(doc.getId());

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
                                currentUser.getDisplayName(),
                                setting.getSnoozeTime(),
                                triggerAtMillis
                        );
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private static Setting extractSetting(QuerySnapshot snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return null;
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        Setting setting = doc.toObject(Setting.class);
        if (setting != null) {
            setting.setIdFs(doc.getId());
        }
        return setting;
    }

    private static Setting buildDefaultSetting(String userId) {
        Setting setting = new Setting();
        setting.setUserId(userId);
        setting.setReminderTime("09:00");
        setting.setSnoozeTime(10);
        setting.setNotificationsEnabled(true);
        return setting;
    }

    private static void cancelAllForPlants(Context context, Set<String> plantIds, QuerySnapshot tasksSnapshot) {
        if (tasksSnapshot == null || plantIds == null || plantIds.isEmpty()) {
            return;
        }

        for (DocumentSnapshot doc : tasksSnapshot.getDocuments()) {
            CareTask task = doc.toObject(CareTask.class);
            if (task == null) continue;
            if (task.getPlantId() == null || !plantIds.contains(task.getPlantId())) continue;

            cancelReminder(context, doc.getId());
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
