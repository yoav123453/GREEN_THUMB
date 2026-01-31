package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.repository.BASE.DB.BaseRepository;

    public class UsersRepository extends BaseRepository<User, Users> {
    public UsersRepository(Context context) {
        super(User.class, Users.class, context);
    }

    @Override
    protected Query getQueryForExist(User entity) {
        return getCollection().
                whereEqualTo("displayName", entity.getDisplayName())
                .whereEqualTo("email", entity.getEmail());
    }
}


