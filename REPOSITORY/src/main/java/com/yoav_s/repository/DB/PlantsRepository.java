package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Plants;
import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class PlantsRepository extends BaseRepository<Plant, Plants> {
    public PlantsRepository(Context context) {
        super(Plant.class, Plants.class, context);
    }

    @Override
    protected Query getQueryForExist(Plant entity) {
        return getCollection().whereEqualTo("nickname", entity.getNickname());
    }
}
