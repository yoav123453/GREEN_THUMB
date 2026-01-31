package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.Guide;
import com.yoav_s.model.Guides;
import com.yoav_s.model.Reminder;
import com.yoav_s.model.Reminders;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class GuidesRepository extends BaseRepository<Guide, Guides> {
    public GuidesRepository(Context context) {
        super(Guide.class, Guides.class, context);
    }

    @Override
    protected Query getQueryForExist(Guide entity) {
        return getCollection()
                .whereEqualTo("contentCreatorId", entity.getContentCreatorId())
                .whereEqualTo("title", entity.getTitle());
    }
}
