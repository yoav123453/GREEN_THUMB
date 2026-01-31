package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.Specie;
import com.yoav_s.model.Species;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class SpeciesRepository extends BaseRepository<Specie, Species> {
    public SpeciesRepository(Context context) {
        super(Specie.class, Species.class, context);
    }

    @Override
    protected Query getQueryForExist(Specie entity) {
        return getCollection().whereEqualTo("name", entity.getName());
    }
}
