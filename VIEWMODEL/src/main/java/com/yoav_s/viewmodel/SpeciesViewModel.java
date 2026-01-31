package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.model.Specie;
import com.yoav_s.model.Species;
import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.SpeciesRepository;
import com.yoav_s.repository.DB.UsersRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class SpeciesViewModel extends BaseViewModel<Specie, Species> {
    private SpeciesRepository repository;

    public SpeciesViewModel(Application application) {
        super(Specie.class, Species.class, application);
    }

    @Override
    protected BaseRepository<Specie, Species> createRepository(
            Application application) {
        repository = new SpeciesRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "name");
    }
}
