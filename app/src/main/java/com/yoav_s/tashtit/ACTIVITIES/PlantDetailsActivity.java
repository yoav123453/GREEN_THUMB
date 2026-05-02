package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yoav_s.helper.NetworkUtils;
import com.yoav_s.tashtit.NOTIFICATIONS.TaskNotificationScheduler;



import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.CareTasks;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Specie;
import com.yoav_s.model.Species;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.FutureCareTasksAdapter;
import com.yoav_s.tashtit.ADPTERS.UpcomingTasksAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.CareTasksViewModel;
import com.yoav_s.viewmodel.SpeciesApiViewModel;
import com.yoav_s.viewmodel.SpeciesViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.yoav_s.model.HistoryNote;
import com.yoav_s.viewmodel.HistoryNotesViewModel;

public class PlantDetailsActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_PLANT = "SELECTED_PLANT";

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private TextView tvPlantNickname;

    private TextView tvSpeciesValue;
    private TextView tvLocationValue;
    private TextView tvLightValue;

    private RecyclerView rvUpcomingTasks;
    private RecyclerView rvFutureTasks;

    private MaterialButton btnHistory;
    private MaterialButton btnBack;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private CareTasksViewModel careTasksViewModel;
    private SpeciesViewModel speciesViewModel;
    private SpeciesApiViewModel speciesApiViewModel;

    private UpcomingTasksAdapter upcomingAdapter;
    private FutureCareTasksAdapter futureAdapter;

    private Plant selectedPlant;
    private Specie selectedSpecie;

    private final List<CareTask> upcomingTasks = new ArrayList<>();
    private final List<CareTask> futureTasks = new ArrayList<>();

    private boolean tasksLoaded = false;
    private boolean speciesLoaded = false;
    private boolean emptyUpcomingToastShown = false;
    private boolean emptyFutureToastShown = false;
    private boolean overdueTasksCheckStarted = false;

    private HistoryNotesViewModel historyNotesViewModel;
    private HistoryNote pendingHistoryNote = null;

    private enum ActionMode {
        NONE,
        FINISH_CURRENT_TASK,
        CREATE_NEXT_TASK,
        ADD_HISTORY_NOTE
    }

    private ActionMode actionMode = ActionMode.NONE;

    private boolean actionInProgress = false;
    private CareTask pendingNextTask = null;

    private String actionSuccessMessage = "Done";
    private String actionFailMessage = "Action failed";
    private String busyTaskId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_plant_details);
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
        setRecyclerViews();
        setListeners();
        setViewModel();
        showPlantBaseInfo();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        tvPlantNickname = drawerLayout.findViewById(R.id.tvPlantNickname);

        tvSpeciesValue = drawerLayout.findViewById(R.id.tvSpeciesValue);
        tvLocationValue = drawerLayout.findViewById(R.id.tvLocationValue);
        tvLightValue = drawerLayout.findViewById(R.id.tvLightValue);

        rvUpcomingTasks = drawerLayout.findViewById(R.id.rvUpcomingTasks);
        rvFutureTasks = drawerLayout.findViewById(R.id.rvFutureTasks);

        btnHistory = drawerLayout.findViewById(R.id.btnHistory);
        btnBack = drawerLayout.findViewById(R.id.btnBack);

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
            Toast.makeText(this, "Plant data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Object plantObj = intent.getSerializableExtra(EXTRA_SELECTED_PLANT);

        if (!(plantObj instanceof Plant)) {
            Toast.makeText(this, "Plant data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedPlant = (Plant) plantObj;
    }

    private void showPlantBaseInfo() {
        if (selectedPlant == null) return;

        tvPlantNickname.setText("\"" + safeText(selectedPlant.getNickname()) + "\"");
        tvSpeciesValue.setText(safeText(selectedPlant.getSpeciesName()));
        tvLocationValue.setText(safeText(selectedPlant.getLocation()));
        tvLightValue.setText("-");
    }

    private void setRecyclerViews() {
        upcomingAdapter = new UpcomingTasksAdapter(new ArrayList<>());
        futureAdapter = new FutureCareTasksAdapter(new ArrayList<>());

        upcomingAdapter.setListener(new UpcomingTasksAdapter.Listener() {
            @Override
            public void onEdit(CareTask task) {
                if (task == null || task.getIdFs() == null || task.getIdFs().trim().isEmpty()) {
                    Toast.makeText(PlantDetailsActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(EditTaskActivity.EXTRA_SELECTED_TASK_ID, task.getIdFs());

                launcherHelper.launchActivity(EditTaskActivity.class, bundle, result -> {
                    if (LauncherHelper.isResultOk(result)) {
                        careTasksViewModel.getAll();
                        Toast.makeText(PlantDetailsActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
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

        futureAdapter.setListener(task -> {
            if (task == null || task.getIdFs() == null || task.getIdFs().trim().isEmpty()) {
                Toast.makeText(PlantDetailsActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString(EditTaskActivity.EXTRA_SELECTED_TASK_ID, task.getIdFs());

            launcherHelper.launchActivity(EditTaskActivity.class, bundle, result -> {
                if (LauncherHelper.isResultOk(result)) {
                    careTasksViewModel.getAll();
                    Toast.makeText(PlantDetailsActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                }
            });
        });

        rvUpcomingTasks.setLayoutManager(new LinearLayoutManager(this));
        rvUpcomingTasks.setAdapter(upcomingAdapter);

        rvFutureTasks.setLayoutManager(new LinearLayoutManager(this));
        rvFutureTasks.setAdapter(futureAdapter);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnBack.setOnClickListener(v -> finish());

        btnHistory.setOnClickListener(v -> {
            if (selectedPlant == null) {
                Toast.makeText(this, "Plant not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable(HistoryActivity.EXTRA_SELECTED_PLANT, selectedPlant);
            launcherHelper.launchActivity(HistoryActivity.class, bundle);
        });

        navMyPlants.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
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
        historyNotesViewModel = new ViewModelProvider(this).get(HistoryNotesViewModel.class);
        speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
        speciesApiViewModel = new ViewModelProvider(this).get(SpeciesApiViewModel.class);

        showProgressDialog(null, "Loading plant details...");

        careTasksViewModel.getAll();
        speciesViewModel.getAll();
        trySkipOverdueTasksForSelectedPlant();

        careTasksViewModel.getLiveDataCollection().observe(this, this::handleTasksChanged);
        speciesViewModel.getLiveDataCollection().observe(this, this::handleSpeciesChanged);

        speciesApiViewModel.getSelectedSpecie().observe(this, specie -> {
            if (specie != null) {
                selectedSpecie = specie;
                showResolvedPlantInfo();
            }
        });

        speciesApiViewModel.getError().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

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
                    actionMode = ActionMode.ADD_HISTORY_NOTE;
                    historyNotesViewModel.addHistoryNote(pendingHistoryNote);
                    return;
                }

                finishActionSuccess(actionSuccessMessage);
            }
        });
        historyNotesViewModel.getSuccess().observe(this, success -> {
            if (!actionInProgress || actionMode != ActionMode.ADD_HISTORY_NOTE) return;

            if (!Boolean.TRUE.equals(success)) {
                finishActionFailure("Task updated, but could not save history");
                return;
            }

            finishActionSuccess(actionSuccessMessage);
        });
    }

    private void trySkipOverdueTasksForSelectedPlant() {
        if (overdueTasksCheckStarted) return;
        if (careTasksViewModel == null) return;
        if (selectedPlant == null || selectedPlant.getIdFs() == null || selectedPlant.getIdFs().trim().isEmpty()) return;

        ArrayList<String> plantIds = new ArrayList<>();
        plantIds.add(selectedPlant.getIdFs());

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

    private void handleSpeciesChanged(Species species) {
        speciesLoaded = true;

        selectedSpecie = findMatchingSpecie(species);

        if (selectedSpecie == null) {
            selectedSpecie = buildSpecieFromPlant();

            if (selectedSpecie != null && selectedSpecie.getApiId() > 0) {
                speciesApiViewModel.loadSpecieDetails(selectedSpecie);
            }
        }

        showResolvedPlantInfo();
        hideLoadingIfReady();
    }

    private Specie findMatchingSpecie(Species species) {
        if (species == null || selectedPlant == null) return null;

        String plantSpeciesId = selectedPlant.getSpeciesId();
        String plantSpeciesName = selectedPlant.getSpeciesName();
        int plantApiId = parseSpeciesApiId(plantSpeciesId);

        for (Specie specie : species) {
            if (specie == null) continue;

            if (plantApiId > 0 && specie.getApiId() == plantApiId) {
                return specie;
            }

            if (plantSpeciesId != null
                    && specie.getIdFs() != null
                    && plantSpeciesId.trim().equals(specie.getIdFs().trim())) {
                return specie;
            }

            if (plantSpeciesName != null
                    && specie.getName() != null
                    && plantSpeciesName.trim().equalsIgnoreCase(specie.getName().trim())) {
                return specie;
            }
        }

        return null;
    }

    private Specie buildSpecieFromPlant() {
        if (selectedPlant == null) return null;

        Specie specie = new Specie();
        specie.setName(selectedPlant.getSpeciesName());

        int apiId = parseSpeciesApiId(selectedPlant.getSpeciesId());
        if (apiId > 0) {
            specie.setApiId(apiId);
        } else {
            specie.setIdFs(selectedPlant.getSpeciesId());
        }

        return specie;
    }

    private int parseSpeciesApiId(String speciesId) {
        if (speciesId == null || speciesId.trim().isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(speciesId.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void showResolvedPlantInfo() {
        if (selectedPlant == null) return;

        tvLocationValue.setText(safeText(selectedPlant.getLocation()));

        if (selectedSpecie != null) {
            tvSpeciesValue.setText(safeText(selectedSpecie.getName()));
            tvLightValue.setText(formatLight(selectedSpecie.getLight()));
        } else {
            tvSpeciesValue.setText(safeText(selectedPlant.getSpeciesName()));
            tvLightValue.setText("-");
        }
    }

    private void handleTasksChanged(CareTasks tasks) {
        tasksLoaded = true;
        upcomingTasks.clear();
        futureTasks.clear();

        if (selectedPlant != null && selectedPlant.getIdFs() != null && tasks != null) {
            long startOfToday = getStartOfTodayMillis();
            long endOfUpcomingWindow = getEndOfWindowMillis(30);

            for (CareTask task : tasks) {
                if (task == null) continue;
                if (task.getPlantId() == null) continue;
                if (!selectedPlant.getIdFs().equals(task.getPlantId())) continue;
                if (task.getState() != CareTask.State.SCHEDULED) continue;
                if (task.getNextDueAt() == null) continue;

                long dueTime = task.getNextDueAt().toDate().getTime();

                if (dueTime >= startOfToday && dueTime <= endOfUpcomingWindow) {
                    upcomingTasks.add(task);
                } else if (dueTime > endOfUpcomingWindow) {
                    futureTasks.add(task);
                }
            }
        }

        sortTasksByDueDate(upcomingTasks);
        sortTasksByDueDate(futureTasks);

        upcomingAdapter.setItems(new ArrayList<>(upcomingTasks));
        futureAdapter.setItems(new ArrayList<>(futureTasks));

        hideLoadingIfReady();

        if (tasksLoaded && upcomingTasks.isEmpty() && !emptyUpcomingToastShown) {
            emptyUpcomingToastShown = true;
            Toast.makeText(this,
                    "No upcoming tasks for the next 30 days",
                    Toast.LENGTH_SHORT).show();
        }

        if (tasksLoaded && futureTasks.isEmpty() && !emptyFutureToastShown) {
            emptyFutureToastShown = true;
            Toast.makeText(this,
                    "No future care plan tasks found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sortTasksByDueDate(List<CareTask> tasks) {
        Collections.sort(tasks, new Comparator<CareTask>() {
            @Override
            public int compare(CareTask t1, CareTask t2) {
                long d1 = t1 != null && t1.getNextDueAt() != null ? t1.getNextDueAt().toDate().getTime() : Long.MAX_VALUE;
                long d2 = t2 != null && t2.getNextDueAt() != null ? t2.getNextDueAt().toDate().getTime() : Long.MAX_VALUE;
                return Long.compare(d1, d2);
            }
        });
    }

    private long getStartOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfWindowMillis(int daysAhead) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        calendar.add(Calendar.DAY_OF_YEAR, daysAhead);
        return calendar.getTimeInMillis();
    }

    private void handleTaskAction(CareTask currentTask, boolean isMarkDone) {
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.requireInternet(PlantDetailsActivity.this)) {
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
        upcomingAdapter.setBusyTaskId(busyTaskId);

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
        upcomingAdapter.setBusyTaskId(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (currentUser != null) {
            TaskNotificationScheduler.rescheduleAllForCurrentUser(this, currentUser);
        }
    }

    private void finishActionFailure(String message) {
        hideProgressDialog();
        resetActionState();
        upcomingAdapter.setBusyTaskId(null);
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
        if (tasksLoaded && speciesLoaded && !actionInProgress) {
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

    private String formatLight(Specie.Light light) {
        if (light == null) return "-";
        return light.getApiValue();
    }
    private String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}