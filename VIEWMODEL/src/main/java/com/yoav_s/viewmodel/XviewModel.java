package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class XviewModel extends BaseViewModel {
    @Override
    protected BaseRepository createRepository(Application application) {
        return null;
    }

    public XviewModel(Class tEntity, Class tCollection, Application application) {
        super(tEntity, tCollection, application);
    }
}
