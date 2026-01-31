package com.yoav_s.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class CareTask extends BaseEntity implements Serializable {

    public enum Type { WATER, FERTILIZE, SPRAY, PRUNE, REPOT }
    public enum State { SCHEDULED, DONE, SKIPPED }

    private String plantId;
    private Type type;
    private int everyDays;

    private State state;

    private String text;
    @Exclude
    private String photo;
    private String photoUrl;

    private Timestamp nextDueAt;
    private Timestamp doneAt;

    public CareTask() {
        this.state = State.SCHEDULED;
    }

    public CareTask(String plantId, Type type, int everyDays, State state,
                    String text, String photo, String photoUrl, Timestamp nextDueAt, Timestamp doneAt) {
        this.plantId = plantId;
        this.type = type;
        this.everyDays = everyDays;
        this.state = state;
        this.text = text;
        this.photo = photo;
        this.photoUrl = photoUrl;
        this.nextDueAt = nextDueAt;
        this.doneAt = doneAt;
    }

    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public int getEveryDays() { return everyDays; }
    public void setEveryDays(int everyDays) { this.everyDays = everyDays; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    @Exclude
    public String getPhoto() { return photo; }
    @Exclude
    public void setPhoto(String photo) { this.photo = photo; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Timestamp getNextDueAt() { return nextDueAt; }
    public void setNextDueAt(Timestamp nextDueAt) { this.nextDueAt = nextDueAt; }

    public Timestamp getDoneAt() { return doneAt; }
    public void setDoneAt(Timestamp doneAt) { this.doneAt = doneAt; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CareTask careTask = (CareTask) o;
        return everyDays == careTask.everyDays && Objects.equals(plantId, careTask.plantId) && type == careTask.type && state == careTask.state && Objects.equals(text, careTask.text) && Objects.equals(photo, careTask.photo) && Objects.equals(photoUrl, careTask.photoUrl) && Objects.equals(nextDueAt, careTask.nextDueAt) && Objects.equals(doneAt, careTask.doneAt);
    }
}
