package com.yoav_s.model;

import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class Setting extends BaseEntity implements Serializable {
    private String userId;
    private String reminderTime;
    private int snoozeTime;
    private boolean notificationsEnabled;

    public Setting() {}

    public Setting(String reminderTime, int snoozeTime, boolean notificationsEnabled) {
        this.reminderTime = reminderTime;
        this.snoozeTime = snoozeTime;
        this.notificationsEnabled = notificationsEnabled;
        this.userId = userId;
    }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public int getSnoozeTime() { return snoozeTime; }
    public void setSnoozeTime(int snoozeTime) { this.snoozeTime = snoozeTime; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Setting setting = (Setting) o;
        return snoozeTime == setting.snoozeTime && notificationsEnabled == setting.notificationsEnabled && Objects.equals(userId, setting.userId) && Objects.equals(reminderTime, setting.reminderTime);
    }
}

