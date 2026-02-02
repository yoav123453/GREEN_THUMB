package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.Reminder;
import com.yoav_s.model.Reminders;
import com.yoav_s.model.Setting;
import com.yoav_s.model.Settings;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class SettingsRepository extends BaseRepository<Setting, Settings> {
    public SettingsRepository(Context context) {
        super(Setting.class, Settings.class, context);
    }

    @Override
    protected Query getQueryForExist(Setting entity) {
        return getCollection().whereEqualTo("userId", entity.getUserId());
    }
}
