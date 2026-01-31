package com.yoav_s.model;

import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class Settings extends BaseEntity implements Serializable {
    private String reminderTime;
    private int snoozeTime;
    private int advanceHours;
    private boolean notificationsEnabled;

    public Settings() {}

    public Settings(String reminderTime, int snoozeTime, int advanceHours, boolean notificationsEnabled) {
        this.reminderTime = reminderTime;
        this.snoozeTime = snoozeTime;
        this.advanceHours = advanceHours;
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public int getSnoozeTime() { return snoozeTime; }
    public void setSnoozeTime(int snoozeTime) { this.snoozeTime = snoozeTime; }

    public int getAdvanceHours() { return advanceHours; }
    public void setAdvanceHours(int advanceHours) { this.advanceHours = advanceHours; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return snoozeTime == settings.snoozeTime && advanceHours == settings.advanceHours && notificationsEnabled == settings.notificationsEnabled && Objects.equals(reminderTime, settings.reminderTime);
    }

}

