package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Build;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.DateUtil;
import com.yoav_s.helper.DateUtil_OLD;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.helper.inputValidators.EntryValidation;
import com.yoav_s.model.CareTask;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.CareTasksViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;


public class EditTaskActivity extends BaseActivity implements EntryValidation {

    public static final String EXTRA_SELECTED_TASK_ID = "SELECTED_TASK_ID";

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private TextView tvTaskType;
    private TextView tvDueAt;
    private TextView tvRepeatEvery;

    private MaterialCardView cardDueAt;
    private MaterialCardView cardRepeatEvery;

    private MaterialButton btnSave;
    private MaterialButton btnDelete;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private CareTasksViewModel careTasksViewModel;

    private CareTask selectedTask;
    private String selectedTaskId;
    private boolean taskLoaded = false;

    private LocalDate selectedDueDate = null;
    private int selectedEveryDays = 0;

    private enum ActionMode {
        NONE,
        SAVE,
        DELETE
    }

    private ActionMode actionMode = ActionMode.NONE;
    private boolean actionInProgress = false;

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
        setLayout(R.layout.activity_edit_task);
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
        readExtras();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        tvTaskType = drawerLayout.findViewById(R.id.tvTaskType);
        tvDueAt = drawerLayout.findViewById(R.id.tvDueAt);
        tvRepeatEvery = drawerLayout.findViewById(R.id.tvRepeatEvery);

        cardDueAt = drawerLayout.findViewById(R.id.cardDueAt);
        cardRepeatEvery = drawerLayout.findViewById(R.id.cardRepeatEvery);

        btnSave = drawerLayout.findViewById(R.id.btnSave);
        btnDelete = drawerLayout.findViewById(R.id.btnDelete);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void readExtras() {
        Intent intent = getIntent();

        if (intent == null) {
            Toast.makeText(this, "Task data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedTaskId = intent.getStringExtra(EXTRA_SELECTED_TASK_ID);

        if (selectedTaskId == null || selectedTaskId.trim().isEmpty()) {
            Toast.makeText(this, "Task data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        cardDueAt.setOnClickListener(v -> openDatePicker());
        tvDueAt.setOnClickListener(v -> openDatePicker());

        cardRepeatEvery.setOnClickListener(v -> openRepeatEveryDialog());
        tvRepeatEvery.setOnClickListener(v -> openRepeatEveryDialog());

        btnSave.setOnClickListener(v -> {
            if (!validate()) return;
            saveTaskChanges();
        });

        btnDelete.setOnClickListener(v -> {
            AlertDialogHelper.showDelete(
                    this,
                    "Are you sure you want to remove this care task type from the plant schedule?",
                    this::deleteTask,
                    () -> {
                    }
            );
        });

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

        navSettings.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(SettingsActivity.class);
            finish();
        });

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

        navLogout.setOnClickListener(v -> logout());
    }

    @Override
    protected void setViewModel() {
        careTasksViewModel = new ViewModelProvider(this).get(CareTasksViewModel.class);

        showProgressDialog(null, "Loading task...");

        careTasksViewModel.getLiveDataEntity().observe(this, task -> {
            hideProgressDialog();

            if (task == null) {
                Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            selectedTask = task;
            selectedDueDate = getTaskDueDate(selectedTask);
            selectedEveryDays = Math.max(selectedTask.getEveryDays(), 1);
            taskLoaded = true;

            showTaskData();
        });

        careTasksViewModel.getSuccess().observe(this, success -> {
            if (!actionInProgress) return;

            ActionMode completedAction = actionMode;

            hideProgressDialog();

            if (!Boolean.TRUE.equals(success)) {
                actionInProgress = false;
                actionMode = ActionMode.NONE;

                if (completedAction == ActionMode.DELETE) {
                    Toast.makeText(this, "Could not delete task", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Could not save task changes", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (completedAction == ActionMode.SAVE) {
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            } else if (completedAction == ActionMode.DELETE) {
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
            }

            actionInProgress = false;
            actionMode = ActionMode.NONE;

            if (currentUser != null) {
                TaskNotificationScheduler.rescheduleAllForCurrentUser(this, currentUser);
            }

            setResult(RESULT_OK);
            finish();
        });

        careTasksViewModel.get(selectedTaskId);
    }

    private void showTaskData() {
        if (!taskLoaded || selectedTask == null) return;

        tvTaskType.setText(formatTaskType(selectedTask.getType()));
        tvDueAt.setText(formatDate(selectedDueDate));
        tvRepeatEvery.setText(formatEveryDays(selectedEveryDays));
    }

    private void openDatePicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Date picker requires Android O or newer", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate openAt = selectedDueDate != null ? selectedDueDate : today;

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Choose due date")
                .setSelection(DateUtil_OLD.localDateToLong(openAt))
                .setCalendarConstraints(
                        DateUtil_OLD.buidCalendarConstrains(
                                today,
                                today.plusYears(10),
                                openAt
                        )
                )
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDueDate = DateUtil_OLD.longToLocalDate(selection);
            tvDueAt.setText(formatDate(selectedDueDate));
        });

        picker.show(getSupportFragmentManager(), "DUE_DATE_PICKER");
    }

    private void openRepeatEveryDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(selectedEveryDays > 0 ? String.valueOf(selectedEveryDays) : "");
        input.setSelection(input.getText().length());

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Repeats every how many days?")
                .setView(input)
                .setCancelable(true)
                .setPositiveButton("Save", (dialog, which) -> {
                    String value = input.getText().toString().trim();

                    if (value.isEmpty()) {
                        Toast.makeText(this, "Enter repeat days", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int days = Integer.parseInt(value);

                        if (days <= 0) {
                            Toast.makeText(this, "Repeat days must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        selectedEveryDays = days;
                        tvRepeatEvery.setText(formatEveryDays(selectedEveryDays));

                    } catch (Exception e) {
                        Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void setValidation() {
        tvDueAt.setError(null);
        tvRepeatEvery.setError(null);
    }

    @Override
    public boolean validate() {
        setValidation();

        if (selectedTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedDueDate == null) {
            tvDueAt.setError("Choose a due date");
            Toast.makeText(this, "Choose a valid due date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && selectedDueDate.isBefore(LocalDate.now())) {
            tvDueAt.setError("Date cannot be in the past");
            Toast.makeText(this, "Due date cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedEveryDays <= 0) {
            tvRepeatEvery.setError("Invalid repeat days");
            Toast.makeText(this, "Repeat days must be greater than 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveTaskChanges() {
        if (!taskLoaded || selectedTask == null) return;

        selectedTask.setEveryDays(selectedEveryDays);
        selectedTask.setNextDueAt(localDateToTimestamp(selectedDueDate));

        actionMode = ActionMode.SAVE;
        actionInProgress = true;

        showProgressDialog(null, "Saving task...");
        careTasksViewModel.update(selectedTask);
    }

    private void deleteTask() {
        if (!taskLoaded || selectedTask == null) return;

        actionMode = ActionMode.DELETE;
        actionInProgress = true;

        showProgressDialog(null, "Deleting task...");
        careTasksViewModel.delete(selectedTask);
    }

    private void logout() {
        drawerLayout.closeDrawer(GravityCompat.END);
        currentUser = null;
        launcherHelper.launchActivity(SignInActivity.class);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }

    private LocalDate getTaskDueDate(CareTask task) {
        if (task == null || task.getNextDueAt() == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }

        return Instant.ofEpochMilli(task.getNextDueAt().toDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Timestamp localDateToTimestamp(LocalDate date) {
        if (date == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }

        return new Timestamp(
                java.util.Date.from(
                        date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
        );
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return DateUtil.localDateToString(date, DateUtil.FORMAT_DD_MM_YYYY);
    }

    private String formatEveryDays(int days) {
        if (days <= 0) return "-";
        return "Every " + days + " days";
    }

    private String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}