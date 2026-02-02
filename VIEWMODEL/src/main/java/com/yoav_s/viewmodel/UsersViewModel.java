package com.yoav_s.viewmodel;

import android.app.Application;

import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.UsersRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class UsersViewModel extends BaseViewModel<User, Users> {
    private UsersRepository repository;

    public UsersViewModel(Application application) {
        super(User.class, Users.class, application);
    }

    @Override
    protected BaseRepository<User, Users> createRepository(
            Application application) {
        repository = new UsersRepository(application);
        return repository;
    }

    public void getAll() {
        getAllAscending(null, "displayName");
    }
    public void logIn(String email, String password) {
        repository.getCollection().whereEqualTo("email", email).whereEqualTo("password", password).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        lvEntity.setValue(user);
                    }
                })
                .addOnFailureListener(e -> {
                    lvEntity.setValue(null);
                });
    }
}
