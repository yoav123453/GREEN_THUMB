package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yoav_s.helper.NetworkUtils;
import com.yoav_s.tashtit.NOTIFICATIONS.TaskNotificationScheduler;



import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.Timestamp;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.CareTasks;
import com.yoav_s.model.HistoryNote;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Plants;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.CalendarTasksAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.CareTasksViewModel;
import com.yoav_s.viewmodel.HistoryNotesViewModel;
import com.yoav_s.viewmodel.PlantsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends BaseActivity {

    private enum SelectedRange {
        DAY,
        WEEK,
        MONTH
    }

    private enum ActionMode {
        NONE,
        FINISH_CURRENT_TASK,
        CREATE_NEXT_TASK,
        ADD_HISTORY_NOTE
    }

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private MaterialButtonToggleGroup toggleGroupRange;
    private MaterialButton btnDay;
    private MaterialButton btnWeek;
    private MaterialButton btnMonth;

    private TextView tvSelectedRangeTitle;
    private TextView tvSelectedRangeDates;

    private RecyclerView rvCalendarTasks;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;

    private PlantsViewModel plantsViewModel;
    private CareTasksViewModel careTasksViewModel;
    private HistoryNotesViewModel historyNotesViewModel;
    private HistoryNote pendingHistoryNote = null;

    private CalendarTasksAdapter adapter;

    private final List<Plant> userPlants = new ArrayList<>();
    private final List<CareTask> allTasks = new ArrayList<>();

    private final Map<String, String> plantNicknameByPlantId = new HashMap<>();

    private boolean plantsLoaded = false;
    private boolean tasksLoaded = false;

    private SelectedRange selectedRange = SelectedRange.DAY;

    private ActionMode actionMode = ActionMode.NONE;
    private boolean actionInProgress = false;

    private CareTask pendingNextTask = null;

    private String actionSuccessMessage = "Done";
    private String actionFailMessage = "Action failed";
    private String busyTaskId = null;

    private boolean overdueTasksCheckStarted = false;

    private final SimpleDateFormat headerDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        setLayout(R.layout.activity_calendar);
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
        setRecyclerView();
        setListeners();
        updateRangeHeader();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        toggleGroupRange = drawerLayout.findViewById(R.id.toggleGroupRange);
        btnDay = drawerLayout.findViewById(R.id.btnDay);
        btnWeek = drawerLayout.findViewById(R.id.btnWeek);
        btnMonth = drawerLayout.findViewById(R.id.btnMonth);

        tvSelectedRangeTitle = drawerLayout.findViewById(R.id.tvSelectedRangeTitle);
        tvSelectedRangeDates = drawerLayout.findViewById(R.id.tvSelectedRangeDates);

        rvCalendarTasks = drawerLayout.findViewById(R.id.rvCalendarTasks);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);

        toggleGroupRange.check(R.id.btnDay);
    }

    private void setRecyclerView() {
        adapter = new CalendarTasksAdapter(new ArrayList<>());

        adapter.setListener(new CalendarTasksAdapter.Listener() {
            @Override
            public void onEdit(CareTask task) {
                if (task == null || task.getIdFs() == null || task.getIdFs().trim().isEmpty()) {
                    Toast.makeText(CalendarActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(EditTaskActivity.EXTRA_SELECTED_TASK_ID, task.getIdFs());

                launcherHelper.launchActivity(EditTaskActivity.class, bundle, result -> {
                    if (LauncherHelper.isResultOk(result)) {
                        Toast.makeText(CalendarActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMarkDone(CareTask task) {
                handleTaskAction(task, true);
            }

            @Override
            public void onSkip(CareTask task) {
                handleTaskAction(task, false);
            }
        });

        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(this));
        rvCalendarTasks.setAdapter(adapter);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        toggleGroupRange.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnDay) {
                selectedRange = SelectedRange.DAY;
            } else if (checkedId == R.id.btnWeek) {
                selectedRange = SelectedRange.WEEK;
            } else if (checkedId == R.id.btnMonth) {
                selectedRange = SelectedRange.MONTH;
            }

            updateRangeHeader();
            refreshRecyclerData();
        });

        navMyPlants.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(MyPlantsActivity.class);
            finish();
        });

        navCalendar.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

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
        plantsViewModel = new ViewModelProvider(this).get(PlantsViewModel.class);
        careTasksViewModel = new ViewModelProvider(this).get(CareTasksViewModel.class);
        historyNotesViewModel = new ViewModelProvider(this).get(HistoryNotesViewModel.class);


        showProgressDialog(null, "Loading calendar...");

        plantsViewModel.getAll();
        careTasksViewModel.getAll();

        plantsViewModel.getLiveDataCollection().observe(this, this::handlePlantsChanged);
        careTasksViewModel.getLiveDataCollection().observe(this, this::handleTasksChanged);

        careTasksViewModel.getSuccess().observe(this, success -> {
            if (!actionInProgress) return;

            if (!Boolean.TRUE.equals(success)) {
                finishActionFailure(actionFailMessage);
                return;
            }

            if (actionMode == ActionMode.FINISH_CURRENT_TASK) {
                actionMode = ActionMode.CREATE_NEXT_TASK;
                careTasksViewModel.add(pendingNextTask);
                return;
            }

            if (actionMode == ActionMode.CREATE_NEXT_TASK) {
                if (pendingHistoryNote != null) {
                    actionMode = CalendarActivity.ActionMode.ADD_HISTORY_NOTE;
                    historyNotesViewModel.addHistoryNote(pendingHistoryNote);
                    return;
                }
                finishActionSuccess(actionSuccessMessage);
            }
        });
        historyNotesViewModel.getSuccess().observe(this, success -> {
            if (!actionInProgress || actionMode != CalendarActivity.ActionMode.ADD_HISTORY_NOTE) return;

            if (!Boolean.TRUE.equals(success)) {
                finishActionFailure("Task updated, but could not save history");
                return;
            }

            finishActionSuccess(actionSuccessMessage);
        });
    }

    private void handlePlantsChanged(Plants plants) {
        plantsLoaded = true;
        userPlants.clear();

        if (plants != null) {
            for (Plant plant : plants) {
                if (plant != null
                        && currentUser != null
                        && currentUser.getIdFs() != null
                        && currentUser.getIdFs().equals(plant.getUserId())) {
                    userPlants.add(plant);
                }
            }
        }

        Collections.sort(userPlants, new Comparator<Plant>() {
            @Override
            public int compare(Plant p1, Plant p2) {
                String n1 = p1 != null && p1.getNickname() != null ? p1.getNickname() : "";
                String n2 = p2 != null && p2.getNickname() != null ? p2.getNickname() : "";
                return n1.compareToIgnoreCase(n2);
            }
        });

        buildPlantNicknameMap();
        trySkipOverdueTasksForCurrentUser();
        refreshRecyclerData();
        hideLoadingIfReady();
    }

    private void trySkipOverdueTasksForCurrentUser() {
        if (overdueTasksCheckStarted) return;
        if (careTasksViewModel == null) return;
        if (userPlants.isEmpty()) return;

        ArrayList<String> plantIds = new ArrayList<>();

        for (Plant plant : userPlants) {
            if (plant != null && plant.getIdFs() != null && !plant.getIdFs().trim().isEmpty()) {
                plantIds.add(plant.getIdFs());
            }
        }

        if (plantIds.isEmpty()) return;

        overdueTasksCheckStarted = true;
        careTasksViewModel.skipOverdueScheduledTasksForPlants(plantIds, getStartOfTodayTimestamp());
    }

    private Timestamp getStartOfTodayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return new Timestamp(calendar.getTime());
    }

    private void handleTasksChanged(CareTasks tasks) {
        tasksLoaded = true;
        allTasks.clear();

        if (tasks != null) {
            allTasks.addAll(tasks);
        }

        refreshRecyclerData();
        hideLoadingIfReady();
    }

    private void buildPlantNicknameMap() {
        plantNicknameByPlantId.clear();

        for (Plant plant : userPlants) {
            if (plant == null || plant.getIdFs() == null) continue;
            plantNicknameByPlantId.put(plant.getIdFs(), safeText(plant.getNickname()));
        }
    }

    private void refreshRecyclerData() {
        if (adapter == null) return;

        List<CareTask> filteredTasks = new ArrayList<>();

        long startMillis = getRangeStartMillis();
        long endMillis = getRangeEndMillis();

        for (CareTask task : allTasks) {
            if (task == null) continue;
            if (task.getPlantId() == null) continue;
            if (!plantNicknameByPlantId.containsKey(task.getPlantId())) continue;
            if (task.getState() != CareTask.State.SCHEDULED) continue;
            if (task.getNextDueAt() == null) continue;

            long dueTime = task.getNextDueAt().toDate().getTime();
            if (dueTime >= startMillis && dueTime <= endMillis) {
                filteredTasks.add(task);
            }
        }

        Collections.sort(filteredTasks, new Comparator<CareTask>() {
            @Override
            public int compare(CareTask t1, CareTask t2) {
                long d1 = t1 != null && t1.getNextDueAt() != null ? t1.getNextDueAt().toDate().getTime() : Long.MAX_VALUE;
                long d2 = t2 != null && t2.getNextDueAt() != null ? t2.getNextDueAt().toDate().getTime() : Long.MAX_VALUE;
                return Long.compare(d1, d2);
            }
        });

        adapter.setPlantNicknameByPlantId(new HashMap<>(plantNicknameByPlantId));
        adapter.setItems(filteredTasks);
    }

    private void updateRangeHeader() {
        long startMillis = getRangeStartMillis();
        long endMillis = getRangeEndMillis();

        if (selectedRange == SelectedRange.DAY) {
            tvSelectedRangeTitle.setText("Tasks for today");
            tvSelectedRangeDates.setText(headerDateFormat.format(startMillis));
            return;
        }

        if (selectedRange == SelectedRange.WEEK) {
            tvSelectedRangeTitle.setText("Tasks for this week");
            tvSelectedRangeDates.setText(
                    headerDateFormat.format(startMillis) + " - " + headerDateFormat.format(endMillis)
            );
            return;
        }

        tvSelectedRangeTitle.setText("Tasks for this month");
        tvSelectedRangeDates.setText(
                headerDateFormat.format(startMillis) + " - " + headerDateFormat.format(endMillis)
        );
    }

    private long getRangeStartMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getRangeEndMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        if (selectedRange == SelectedRange.WEEK) {
            calendar.add(Calendar.DAY_OF_YEAR, 6);
        } else if (selectedRange == SelectedRange.MONTH) {
            calendar.add(Calendar.DAY_OF_YEAR, 29);
        }

        return calendar.getTimeInMillis();
    }

    private void handleTaskAction(CareTask currentTask, boolean isMarkDone) {
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.requireInternet(CalendarActivity.this)) {
            return;
        }

        Timestamp actionTime = Timestamp.now();
        Timestamp nextDueAt = calculateNextDueAt(currentTask);

        pendingNextTask = buildNextOccurrence(currentTask, nextDueAt);

        if (isMarkDone) {
            pendingHistoryNote = new HistoryNote();
            pendingHistoryNote.setPlantId(currentTask.getPlantId());
            pendingHistoryNote.setEntryType(HistoryNote.EntryType.TASK);
            pendingHistoryNote.setText(formatTaskType(currentTask.getType()));
            pendingHistoryNote.setCreatedAt(actionTime);
        } else {
            pendingHistoryNote = null;
        }

        currentTask.setState(isMarkDone ? CareTask.State.DONE : CareTask.State.SKIPPED);
        currentTask.setDoneAt(actionTime);

        actionInProgress = true;
        actionMode = ActionMode.FINISH_CURRENT_TASK;

        busyTaskId = currentTask.getIdFs();
        adapter.setBusyTaskId(busyTaskId);

        actionSuccessMessage = isMarkDone ? "Task marked as done" : "Task skipped";
        actionFailMessage = isMarkDone ? "Could not mark task as done" : "Could not skip task";

        showProgressDialog(null, isMarkDone ? "Saving task..." : "Skipping task...");
        careTasksViewModel.update(currentTask);
    }

    private Timestamp calculateNextDueAt(CareTask task) {
        long now = System.currentTimeMillis();
        long baseTime = now;

        if (task.getNextDueAt() != null) {
            baseTime = Math.max(now, task.getNextDueAt().toDate().getTime());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.add(Calendar.DAY_OF_YEAR, Math.max(task.getEveryDays(), 1));

        return new Timestamp(calendar.getTime());
    }

    private CareTask buildNextOccurrence(CareTask currentTask, Timestamp nextDueAt) {
        CareTask nextTask = new CareTask();
        nextTask.setPlantId(currentTask.getPlantId());
        nextTask.setType(currentTask.getType());
        nextTask.setEveryDays(currentTask.getEveryDays());
        nextTask.setState(CareTask.State.SCHEDULED);
        nextTask.setNextDueAt(nextDueAt);
        nextTask.setDoneAt(null);
        return nextTask;
    }

    private void finishActionSuccess(String message) {
        hideProgressDialog();
        resetActionState();
        adapter.setBusyTaskId(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (currentUser != null) {
            TaskNotificationScheduler.rescheduleAllForCurrentUser(this, currentUser);
        }
    }

    private void finishActionFailure(String message) {
        hideProgressDialog();
        resetActionState();
        adapter.setBusyTaskId(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void resetActionState() {
        actionInProgress = false;
        actionMode = ActionMode.NONE;
        pendingNextTask = null;
        pendingHistoryNote = null;
        busyTaskId = null;
    }

    private void hideLoadingIfReady() {
        if (plantsLoaded && tasksLoaded && !actionInProgress) {
            hideProgressDialog();
        }
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

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
    }
    private String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}