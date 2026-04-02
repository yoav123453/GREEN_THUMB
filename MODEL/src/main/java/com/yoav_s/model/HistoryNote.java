package com.yoav_s.model;

import com.google.firebase.Timestamp;
import com.yoav_s.model.BASE.BaseEntity;

import java.io.Serializable;

public class HistoryNote extends BaseEntity implements Serializable {

    public enum EntryType {
        TEXT,
        TASK
    }

    private EntryType entryType;

    private String plantId;
    private String text;
    private String photo;
    private String photoUrl;
    private Timestamp createdAt;

    public HistoryNote() {
        this.entryType = EntryType.TEXT;
        this.plantId = "";
        this.text = "";
        this.photo = "";
        this.photoUrl = "";
        this.createdAt = null;
    }
    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}