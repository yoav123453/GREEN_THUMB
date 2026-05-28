package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Reminder;
import com.yoav_s.model.Reminders;
import com.yoav_s.model.Setting;
import com.yoav_s.model.TaskNotificationSchedule;
import com.yoav_s.model.User;
import com.yoav_s.repository.BASE.DB.BaseRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RemindersRepository extends BaseRepository<Reminder, Reminders> {
    public RemindersRepository(Context context) {
        super(Reminder.class, Reminders.class, context);
    }

    @Override
    protected Query getQueryForExist(Reminder entity) {
        return getCollection()
                .whereEqualTo("userId", entity.getUserId())
                .whereEqualTo("taskId", entity.getTaskId())
                .whereEqualTo("scheduledAt", entity.getScheduledAt());
    }

    public Task<TaskNotificationSchedule> getNotificationScheduleForUser(User currentUser) {
        TaskCompletionSource<TaskNotificationSchedule> taskCompletionSource = new TaskCompletionSource<>();

        if (currentUser == null || currentUser.getIdFs() == null || currentUser.getIdFs().trim().isEmpty()) {
            taskCompletionSource.setResult(null);
            return taskCompletionSource.getTask();
        }

        String userId = currentUser.getIdFs();

        Task<QuerySnapshot> settingsTask = getCollection().getFirestore()
                .collection("Settings")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> plantsTask = getCollection().getFirestore()
                .collection("Plants")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> careTasksTask = getCollection().getFirestore()
                .collection("CareTasks")
                .get();

        Tasks.whenAllSuccess(settingsTask, plantsTask, careTasksTask)
                .addOnSuccessListener(results -> {
                    QuerySnapshot settingsSnapshot = (QuerySnapshot) results.get(0);
                    QuerySnapshot plantsSnapshot = (QuerySnapshot) results.get(1);
                    QuerySnapshot tasksSnapshot = (QuerySnapshot) results.get(2);

                    Setting setting = extractSetting(settingsSnapshot);

                    if (setting == null) {
                        setting = buildDefaultSetting(userId);
                    }

                    ArrayList<Plant> userPlants = new ArrayList<>();
                    Set<String> plantIds = new HashSet<>();

                    for (DocumentSnapshot doc : plantsSnapshot.getDocuments()) {
                        Plant plant = doc.toObject(Plant.class);

                        if (plant == null) continue;

                        plant.setIdFs(doc.getId());
                        userPlants.add(plant);
                        plantIds.add(doc.getId());
                    }

                    ArrayList<CareTask> userCareTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : tasksSnapshot.getDocuments()) {
                        CareTask careTask = doc.toObject(CareTask.class);

                        if (careTask == null) continue;
                        if (careTask.getPlantId() == null) continue;
                        if (!plantIds.contains(careTask.getPlantId())) continue;

                        careTask.setIdFs(doc.getId());
                        userCareTasks.add(careTask);
                    }

                    TaskNotificationSchedule schedule = new TaskNotificationSchedule(
                            userId,
                            currentUser.getDisplayName(),
                            setting,
                            userPlants,
                            userCareTasks
                    );

                    taskCompletionSource.setResult(schedule);
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    private Setting extractSetting(QuerySnapshot snapshot) {
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

    private Setting buildDefaultSetting(String userId) {
        Setting setting = new Setting();
        setting.setUserId(userId);
        setting.setReminderTime("09:00");
        setting.setSnoozeTime(10);
        setting.setNotificationsEnabled(true);
        return setting;
    }
}