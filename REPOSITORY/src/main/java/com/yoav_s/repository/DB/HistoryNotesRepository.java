package com.yoav_s.repository.DB;

import android.content.Context;

import com.google.firebase.firestore.Query;
import com.yoav_s.model.HistoryNote;
import com.yoav_s.model.HistoryNotes;
import com.yoav_s.repository.BASE.DB.BaseRepository;

public class HistoryNotesRepository extends BaseRepository<HistoryNote, HistoryNotes> {

    public HistoryNotesRepository(Context context) {
        super(HistoryNote.class, HistoryNotes.class, context);
    }

    @Override
    protected Query getQueryForExist(HistoryNote entity) {
        return null;
    }
}
