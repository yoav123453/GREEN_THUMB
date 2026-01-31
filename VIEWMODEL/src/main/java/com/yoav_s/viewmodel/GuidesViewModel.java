package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.model.Guide;
import com.yoav_s.model.Guides;
import com.yoav_s.model.Reminder;
import com.yoav_s.model.Reminders;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.GuidesRepository;
import com.yoav_s.repository.DB.RemindersRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class GuidesViewModel extends BaseViewModel<Guide, Guides> {
    private GuidesRepository repository;

    public GuidesViewModel(Application application) {
        super(Guide.class, Guides.class, application);
    }

    @Override
    protected BaseRepository<Guide, Guides> createRepository(
            Application application) {
        repository = new GuidesRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "title");
    }
}
