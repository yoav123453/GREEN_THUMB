package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.model.Reminder;
import com.yoav_s.model.Reminders;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.RemindersRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class RemindersViewModel extends BaseViewModel<Reminder, Reminders> {
    private RemindersRepository repository;

    public RemindersViewModel(Application application) {
        super(Reminder.class, Reminders.class, application);
    }

    @Override
    protected BaseRepository<Reminder, Reminders> createRepository(
            Application application) {
        repository = new RemindersRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "scheduledAt");
    }
}
