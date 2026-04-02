package com.yoav_s.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.HistoryNote;
import com.yoav_s.model.HistoryNotes;
import com.yoav_s.repository.BASE.DB.BaseRepository;
import com.yoav_s.repository.DB.HistoryNotesRepository;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

public class HistoryNotesViewModel extends BaseViewModel<HistoryNote, HistoryNotes> {

    public HistoryNotesViewModel(@NonNull Application application) {
        super(HistoryNote.class, HistoryNotes.class, application);
    }

    @Override
    protected BaseRepository<HistoryNote, HistoryNotes> createRepository(Application application) {
        return new HistoryNotesRepository(application);
    }

    public void getByPlant(String plantId) {
        com.google.firebase.firestore.Query query =
                repository.getCollection().whereEqualTo("plantId", plantId);
        getAll("photo", "photoUrl", query);
    }

    public void addHistoryNote(HistoryNote note) {
        add(note, "photo", "photoUrl");
    }
}