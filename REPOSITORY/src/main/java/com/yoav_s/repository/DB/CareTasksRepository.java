package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.CareTasks;
import com.yoav_s.repository.BASE.DB.BaseRepository;

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
}
