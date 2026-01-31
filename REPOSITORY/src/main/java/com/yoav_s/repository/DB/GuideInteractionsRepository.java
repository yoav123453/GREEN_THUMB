package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.Guide;
import com.yoav_s.model.GuideInteraction;
import com.yoav_s.model.GuideInteractions;
import com.yoav_s.model.Guides;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class GuideInteractionsRepository extends BaseRepository<GuideInteraction, GuideInteractions> {
    public GuideInteractionsRepository(Context context) {
        super(GuideInteraction.class, GuideInteractions.class, context);
    }

    @Override
    protected Query getQueryForExist(GuideInteraction entity) {
        return getCollection().
                whereEqualTo("userId", entity.getUserId()).
                whereEqualTo("guideId", entity.getGuideId());
    }
}
