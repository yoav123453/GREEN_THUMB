package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.CareTasks;
import com.yoav_s.repository.BASE.DB.BaseRepository;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CareTasksRepository extends BaseRepository<CareTask, CareTasks> {
    public CareTasksRepository(Context context) {
        super(CareTask.class, CareTasks.class, context);
    }

    @Override
    protected Query getQueryForExist(CareTask entity) {
        return getCollection()
                .whereEqualTo("plantId", entity.getPlantId())
                .whereEqualTo("type", entity.getType().name())
                .whereEqualTo("nextDueAt", entity.getNextDueAt());
    }

    public Task<Boolean> skipOverdueScheduledTasksForPlants(List<String> plantIds, Timestamp startOfToday) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        if (plantIds == null || plantIds.isEmpty() || startOfToday == null) {
            taskCompletionSource.setResult(true);
            return taskCompletionSource.getTask();
        }

        Set<String> plantIdSet = new HashSet<>(plantIds);

        getCollection()
                .whereEqualTo("state", CareTask.State.SCHEDULED.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = getCollection().getFirestore().batch();
                    int operationsCount = 0;
                    Timestamp skippedAt = Timestamp.now();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        CareTask currentTask = document.toObject(CareTask.class);

                        if (currentTask == null) continue;
                        if (currentTask.getPlantId() == null) continue;
                        if (!plantIdSet.contains(currentTask.getPlantId())) continue;
                        if (currentTask.getNextDueAt() == null) continue;
                        if (!currentTask.getNextDueAt().toDate().before(startOfToday.toDate())) continue;

                        currentTask.setIdFs(document.getId());
                        currentTask.setState(CareTask.State.SKIPPED);
                        currentTask.setDoneAt(skippedAt);

                        DocumentReference currentTaskRef = getCollection().document(document.getId());
                        batch.set(currentTaskRef, currentTask);
                        operationsCount++;

                        CareTask nextTask = buildNextOccurrenceAfterToday(currentTask, startOfToday);
                        if (nextTask != null) {
                            DocumentReference nextTaskRef = getCollection().document();
                            nextTask.setIdFs(nextTaskRef.getId());
                            batch.set(nextTaskRef, nextTask);
                            operationsCount++;
                        }
                    }

                    if (operationsCount == 0) {
                        taskCompletionSource.setResult(true);
                        return;
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> taskCompletionSource.setResult(true))
                            .addOnFailureListener(e -> taskCompletionSource.setResult(false));
                })
                .addOnFailureListener(e -> taskCompletionSource.setResult(false));

        return taskCompletionSource.getTask();
    }

    private CareTask buildNextOccurrenceAfterToday(CareTask currentTask, Timestamp startOfToday) {
        if (currentTask == null || currentTask.getNextDueAt() == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTask.getNextDueAt().toDate());

        int intervalDays = Math.max(currentTask.getEveryDays(), 1);
        long startOfTodayMillis = startOfToday.toDate().getTime();

        do {
            calendar.add(Calendar.DAY_OF_YEAR, intervalDays);
        } while (calendar.getTimeInMillis() < startOfTodayMillis);

        CareTask nextTask = new CareTask();
        nextTask.setPlantId(currentTask.getPlantId());
        nextTask.setType(currentTask.getType());
        nextTask.setEveryDays(currentTask.getEveryDays());
        nextTask.setState(CareTask.State.SCHEDULED);
        nextTask.setNextDueAt(new Timestamp(calendar.getTime()));
        nextTask.setDoneAt(null);

        return nextTask;
    }
}