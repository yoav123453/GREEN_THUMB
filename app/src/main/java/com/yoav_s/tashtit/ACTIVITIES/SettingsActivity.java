package com.yoav_s.tashtit.ACTIVITIES;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.yoav_s.tashtit.NOTIFICATIONS.TaskNotificationScheduler;



import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Setting;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.SettingsViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SettingsActivity extends BaseActivity {

    private static final String DELETED_USER_ID = "DELETED_USER";
    private static final String DELETED_CREATOR_ID = "DELETED_CREATOR";

    private static final int[] SNOOZE_MINUTES_OPTIONS = {1,5, 10, 15, 30};

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private MaterialCardView cardReminderTime;
    private MaterialCardView cardSnooze;

    private EditText etReminderTime;
    private EditText etSnoozeMinutes;

    private SwitchMaterial switchNotifications;

    private MaterialButton btnSaveSettings;
    private MaterialButton btnDeleteAccount;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private SettingsViewModel settingsViewModel;

    private Setting currentSetting = null;

    private boolean createDefaultInProgress = false;
    private boolean saveInProgress = false;
    private boolean deleteInProgress = false;
    private boolean defaultCreationAttempted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (currentUser == null) {
            Toast.makeText(this, "For signed-in/registered users only", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_settings);
        setBottomNavigationVisibility(false);

        android.view.View contentFrame = findViewById(R.id.content_frame);
        DrawerLayout rootDrawer = contentFrame.findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootDrawer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeActivity();
    }

    @Override
    protected void initializeActivity() {
        launcherHelper = new LauncherHelper(this);
        initializeViews();
        configureSelectionFields();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        cardReminderTime = drawerLayout.findViewById(R.id.cardReminderTime);
        cardSnooze = drawerLayout.findViewById(R.id.cardSnooze);

        etReminderTime = drawerLayout.findViewById(R.id.etReminderTime);
        etSnoozeMinutes = drawerLayout.findViewById(R.id.etSnoozeMinutes);

        switchNotifications = drawerLayout.findViewById(R.id.switchNotifications);

        btnSaveSettings = drawerLayout.findViewById(R.id.btnSaveSettings);
        btnDeleteAccount = drawerLayout.findViewById(R.id.btnDeleteAccount);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void configureSelectionFields() {
        makeSelectionField(etReminderTime);
        makeSelectionField(etSnoozeMinutes);
    }

    private void makeSelectionField(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);
        editText.setLongClickable(false);
        editText.setTextIsSelectable(false);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        cardReminderTime.setOnClickListener(v -> openReminderTimePicker());
        etReminderTime.setOnClickListener(v -> openReminderTimePicker());

        cardSnooze.setOnClickListener(v -> openSnoozeChooser());
        etSnoozeMinutes.setOnClickListener(v -> openSnoozeChooser());

        btnSaveSettings.setOnClickListener(v -> saveSettings());

        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());

        navMyPlants.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(MyPlantsActivity.class);
            finish();
        });

        navCalendar.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(CalendarActivity.class);
            finish();
        });

        navSettings.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

        navGuides.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(GuidesActivity.class);
            finish();
        });

        navAi.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(AIAssistantActivity.class);
            finish();
        });

        navLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            currentUser = null;
            launcherHelper.launchActivity(SignInActivity.class);
            finish();
        });
    }

    @Override
    protected void setViewModel() {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        showProgressDialog(null, "Loading settings...");

        settingsViewModel.getLiveDataEntity().observe(this, setting -> {
            if (deleteInProgress) return;

            if (setting != null) {
                currentSetting = setting;
                populateFields(setting);
                hideProgressDialog();
                return;
            }

            if (!defaultCreationAttempted) {
                defaultCreationAttempted = true;

                currentSetting = buildDefaultSetting();
                populateFields(currentSetting);

                createDefaultInProgress = true;
                settingsViewModel.add(currentSetting);
                return;
            }

            hideProgressDialog();
            Toast.makeText(this, "Loaded default settings", Toast.LENGTH_SHORT).show();
        });

        settingsViewModel.getSuccess().observe(this, success -> {
            if (createDefaultInProgress) {
                createDefaultInProgress = false;

                if (Boolean.TRUE.equals(success)) {
                    settingsViewModel.getByUserId(currentUser.getIdFs());
                } else {
                    hideProgressDialog();
                    Toast.makeText(this, "Could not create default settings", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (saveInProgress) {
                hideProgressDialog();
                saveInProgress = false;

                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                    if (currentUser != null) {
                        TaskNotificationScheduler.rescheduleAllForCurrentUser(this, currentUser);
                    }
                } else {
                    Toast.makeText(this, "Could not save settings", Toast.LENGTH_SHORT).show();
                }
            }
        });

        settingsViewModel.getByUserId(currentUser.getIdFs());
    }

    private void populateFields(Setting setting) {
        etReminderTime.setText(safeReminderTime(setting.getReminderTime()));
        etSnoozeMinutes.setText(String.valueOf(setting.getSnoozeTime()));
        switchNotifications.setChecked(setting.isNotificationsEnabled());
    }

    private Setting buildDefaultSetting() {
        Setting setting = new Setting();
        setting.setUserId(currentUser.getIdFs());
        setting.setReminderTime("09:00");
        setting.setSnoozeTime(10);
        setting.setNotificationsEnabled(true);
        return setting;
    }

    private void openReminderTimePicker() {
        String value = etReminderTime.getText() != null ? etReminderTime.getText().toString().trim() : "09:00";

        int hour = 9;
        int minute = 0;

        try {
            String[] parts = value.split(":");
            if (parts.length == 2) {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {
        }

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) ->
                        etReminderTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)),
                hour,
                minute,
                true
        );

        dialog.show();
    }
    private void openSnoozeChooser() {
        List<String> items = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        for (int value : SNOOZE_MINUTES_OPTIONS) {
            items.add(String.valueOf(value));
            actions.add(() -> etSnoozeMinutes.setText(String.valueOf(value)));
        }

        AlertDialogHelper.alert(
                this,
                "Snooze after (min)",
                "",
                true,
                R.drawable.information,
                items,
                actions
        );
    }

    private void saveSettings() {
        if (currentUser == null || currentUser.getIdFs() == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String reminderTime = etReminderTime.getText() != null ? etReminderTime.getText().toString().trim() : "";
        String snoozeText = etSnoozeMinutes.getText() != null ? etSnoozeMinutes.getText().toString().trim() : "";

        if (!isValidReminderTime(reminderTime)) {
            etReminderTime.setError("Choose a valid time");
            Toast.makeText(this, "Choose a valid reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        int snoozeMinutes;

        try {
            snoozeMinutes = Integer.parseInt(snoozeText);
        } catch (Exception e) {
            Toast.makeText(this, "Choose valid settings values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAllowedValue(snoozeMinutes, SNOOZE_MINUTES_OPTIONS)) {
            Toast.makeText(this, "Choose a valid snooze value", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentSetting == null) {
            currentSetting = buildDefaultSetting();
        }

        currentSetting.setUserId(currentUser.getIdFs());
        currentSetting.setReminderTime(reminderTime);
        currentSetting.setSnoozeTime(snoozeMinutes);
        currentSetting.setNotificationsEnabled(switchNotifications.isChecked());

        saveInProgress = true;
        showProgressDialog(null, "Saving settings...");

        if (currentSetting.getIdFs() == null || currentSetting.getIdFs().trim().isEmpty()) {
            settingsViewModel.add(currentSetting);
        } else {
            settingsViewModel.update(currentSetting);
        }
    }

    private void confirmDeleteAccount() {
        AlertDialogHelper.alert(
                this,
                "Delete",
                "Are you sure?\nThis action cannot be undone",
                true,
                R.drawable.trashcan,
                "Yes",
                "No",
                null,
                this::deleteAccount,
                null,
                null
        );
    }

    private void deleteAccount() {
        if (currentUser == null || currentUser.getIdFs() == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        deleteInProgress = true;
        showProgressDialog(null, "Deleting account...");

        String userId = currentUser.getIdFs();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> plantsTask = db.collection("Plants")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> settingsTask = db.collection("Settings")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> remindersTask = db.collection("Reminders")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> guideInteractionsTask = db.collection("GuideInteractions")
                .whereEqualTo("userId", userId)
                .get();

        Task<QuerySnapshot> guidesTask = db.collection("Guides")
                .whereEqualTo("contentCreatorId", userId)
                .get();

        Task<QuerySnapshot> careTasksTask = db.collection("CareTasks").get();
        Task<QuerySnapshot> historyNotesTask = db.collection("HistoryNotes").get();

        Tasks.whenAllSuccess(
                        plantsTask,
                        settingsTask,
                        remindersTask,
                        guideInteractionsTask,
                        guidesTask,
                        careTasksTask,
                        historyNotesTask
                )
                .addOnSuccessListener(results -> {
                    QuerySnapshot plantsSnapshot = (QuerySnapshot) results.get(0);
                    QuerySnapshot settingsSnapshot = (QuerySnapshot) results.get(1);
                    QuerySnapshot remindersSnapshot = (QuerySnapshot) results.get(2);
                    QuerySnapshot guideInteractionsSnapshot = (QuerySnapshot) results.get(3);
                    QuerySnapshot guidesSnapshot = (QuerySnapshot) results.get(4);
                    QuerySnapshot careTasksSnapshot = (QuerySnapshot) results.get(5);
                    QuerySnapshot historyNotesSnapshot = (QuerySnapshot) results.get(6);

                    Set<String> plantIds = new HashSet<>();
                    WriteBatch batch = db.batch();

                    if (plantsSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : plantsSnapshot.getDocuments()) {
                            plantIds.add(doc.getId());
                            batch.delete(doc.getReference());
                        }
                    }

                    if (settingsSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : settingsSnapshot.getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                    }

                    if (remindersSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : remindersSnapshot.getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                    }

                    if (careTasksSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : careTasksSnapshot.getDocuments()) {
                            String plantId = doc.getString("plantId");
                            if (plantId != null && plantIds.contains(plantId)) {
                                batch.delete(doc.getReference());
                            }
                        }
                    }

                    if (historyNotesSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : historyNotesSnapshot.getDocuments()) {
                            String plantId = doc.getString("plantId");
                            if (plantId != null && plantIds.contains(plantId)) {
                                batch.delete(doc.getReference());
                            }
                        }
                    }

                    if (guideInteractionsSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : guideInteractionsSnapshot.getDocuments()) {
                            batch.update(doc.getReference(), "userId", DELETED_USER_ID);
                        }
                    }

                    if (guidesSnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : guidesSnapshot.getDocuments()) {
                            batch.update(doc.getReference(), "contentCreatorId", DELETED_CREATOR_ID);
                        }
                    }

                    batch.delete(db.collection("Users").document(userId));

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                hideProgressDialog();
                                deleteInProgress = false;

                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

                                currentUser = null;
                                launcherHelper.launchActivity(SignInActivity.class);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                hideProgressDialog();
                                deleteInProgress = false;
                                Toast.makeText(this, "Could not delete account", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    deleteInProgress = false;
                    Toast.makeText(this, "Could not delete account", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidReminderTime(String value) {
        return value != null && value.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    private boolean isAllowedValue(int value, int[] allowedValues) {
        for (int allowed : allowedValues) {
            if (allowed == value) return true;
        }
        return false;
    }

    private String safeReminderTime(String value) {
        return isValidReminderTime(value) ? value : "09:00";
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }
}