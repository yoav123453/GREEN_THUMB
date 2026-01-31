package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.Reminder;
import com.yoav_s.model.Reminders;
import com.yoav_s.repository.BASE.DB.BaseRepository;

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
}
