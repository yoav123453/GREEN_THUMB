package com.yoav_s.model;

import com.google.firebase.Timestamp;
import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class    Reminder extends BaseEntity implements Serializable {
    private String userId;
    private String plantId;
    private String taskId;

    private String title;
    private String body;

    private Timestamp scheduledAt;

    public Reminder() {}

    public Reminder(String userId, String plantId, String taskId, String title, String body, Timestamp scheduledAt) {
        this.userId = userId;
        this.plantId = plantId;
        this.taskId = taskId;
        this.title = title;
        this.body = body;
        this.scheduledAt = scheduledAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(userId, reminder.userId) && Objects.equals(plantId, reminder.plantId) && Objects.equals(taskId, reminder.taskId) && Objects.equals(title, reminder.title) && Objects.equals(body, reminder.body) && Objects.equals(scheduledAt, reminder.scheduledAt);
    }
}

