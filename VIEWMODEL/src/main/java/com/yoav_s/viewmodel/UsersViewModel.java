package com.yoav_s.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yoav_s.helper.PasswordUtil;
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

    private final MutableLiveData<Boolean> lvEmailExists = new MutableLiveData<>();

    public LiveData<Boolean> getEmailExists() {
        return lvEmailExists;
    }

    public void checkEmailExists(String email) {
        repository.getCollection()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    boolean exists = (qs != null && !qs.isEmpty());
                    lvEmailExists.setValue(exists);
                })
                .addOnFailureListener(e -> lvEmailExists.setValue(null));
    }

    public void logIn(String email, String password) {
        repository.getCollection()
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {

                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);

                        if (user != null && PasswordUtil.verifyPassword(password, user.getPassword())) {
                            lvEntity.setValue(user);
                        } else {
                            lvEntity.setValue(null);
                        }
                    } else {
                        lvEntity.setValue(null);
                    }
                })
                .addOnFailureListener(e -> lvEntity.setValue(null));
    }
}
