package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.model.Plant;
import com.yoav_s.model.Plants;
import com.yoav_s.model.Specie;
import com.yoav_s.model.Species;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.PlantsRepository;
import com.yoav_s.repository.DB.SpeciesRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class PlantsViewModel extends BaseViewModel<Plant, Plants> {
    private PlantsRepository repository;

    public PlantsViewModel(Application application) {
        super(Plant.class, Plants.class, application);
    }

    @Override
    protected BaseRepository<Plant, Plants> createRepository(
            Application application) {
        repository = new PlantsRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "nickname");
    }
}
