package com.yoav_s.viewmodel;

import android.app.Application;

import com.google.firebase.Timestamp;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.CareTasks;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Plants;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.CareTasksRepository;
import com.yoav_s.repository.DB.PlantsRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

import java.util.List;

public class CareTasksViewModel extends BaseViewModel<CareTask, CareTasks> {
    private CareTasksRepository repository;

    public CareTasksViewModel(Application application) {
        super(CareTask.class, CareTasks.class, application);
    }

    @Override
    protected BaseRepository<CareTask, CareTasks> createRepository(
            Application application) {
        repository = new CareTasksRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "nextDueAt");
    }


    public void skipOverdueScheduledTasksForPlants(List<String> plantIds, Timestamp startOfToday) {
        repository.skipOverdueScheduledTasksForPlants(plantIds, startOfToday);
    }
}
