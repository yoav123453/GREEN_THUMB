package com.yoav_s.repository.DB;

import com.google.firebase.firestore.Query;

import com.yoav_s.model.BASE.BaseEntity;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class Xrepository extends BaseRepository {
    @Override
    protected Query getQueryForExist(BaseEntity entity) {
        return null;
    }
}
