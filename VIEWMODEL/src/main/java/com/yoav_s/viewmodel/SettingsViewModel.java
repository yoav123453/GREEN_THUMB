package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.model.Setting;
import com.yoav_s.model.Settings;
import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.SettingsRepository;
import com.yoav_s.repository.DB.UsersRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class SettingsViewModel extends BaseViewModel<Setting, Settings> {
    private SettingsRepository repository;

    public SettingsViewModel(Application application) {
        super(Setting.class, Settings.class, application);
    }

    @Override
    protected BaseRepository<Setting, Settings> createRepository(
            Application application) {
        repository = new SettingsRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "userId");
    }
}
