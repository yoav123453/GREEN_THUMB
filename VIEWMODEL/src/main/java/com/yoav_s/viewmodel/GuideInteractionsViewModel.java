package com.yoav_s.viewmodel;

import android.app.Application;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.GuideInteraction;
import com.yoav_s.model.GuideInteractions;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.GuideInteractionsRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class GuideInteractionsViewModel extends BaseViewModel<GuideInteraction, GuideInteractions> {
    private GuideInteractionsRepository repository;

    public GuideInteractionsViewModel(Application application) {
        super(GuideInteraction.class, GuideInteractions.class, application);
    }

    @Override
    protected BaseRepository<GuideInteraction, GuideInteractions> createRepository(
            Application application) {
        repository = new GuideInteractionsRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "guideId");
    }

    public void getByGuide(String guideId) {
        Query query = repository.getCollection()
                .whereEqualTo("guideId", guideId);
        getAll(null, null, query);
    }

    public void getByGuideAndUser(String guideId, String userId) {
        Query query = repository.getCollection()
                .whereEqualTo("guideId", guideId)
                .whereEqualTo("userId", userId);
        get(query);
    }
}