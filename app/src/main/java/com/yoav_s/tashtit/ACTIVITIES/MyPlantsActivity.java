package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.yoav_s.tashtit.NOTIFICATIONS.TaskNotificationRescheduler;
import com.yoav_s.viewmodel.RemindersViewModel;




import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.CareTasks;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Plants;
import com.yoav_s.model.Specie;
import com.yoav_s.model.Species;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.MyPlantsAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.CareTasksViewModel;
import com.yoav_s.viewmodel.PlantsViewModel;
import com.yoav_s.viewmodel.SpeciesViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.yoav_s.model.HistoryNote;
import com.yoav_s.viewmodel.HistoryNotesViewModel;
import com.yoav_s.helper.NetworkUtils;

public class MyPlantsActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvMyPlants;
    private FloatingActionButton fabAddPlant;
    private ImageButton btnMenu;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;

    private PlantsViewModel plantsViewModel;
    private CareTasksViewModel careTasksViewModel;
    private SpeciesViewModel speciesViewModel;

    private MyPlantsAdapter adapter;

    private HistoryNotesViewModel historyNotesViewModel;
    private HistoryNote pendingHistoryNote = null;

    private final List<Plant> userPlants = new ArrayList<>();
    private final List<CareTask> allTasks = new ArrayList<>();

    private final Map<String, String> speciesNamesByIdFs = new HashMap<>();
    private final Map<String, String> speciesNamesByApiId = new HashMap<>();

    private final Map<String, String> speciesNameByPlantId = new HashMap<>();
    private final Map<String, CareTask> nextTaskByPlantId = new HashMap<>();

    private boolean plantsLoaded = false;
    private boolean tasksLoaded = false;
    private boolean speciesLoaded = false;
    private boolean emptyToastShown = false;
    private boolean overdueTasksCheckStarted = false;

    private enum ActionMode {
        NONE,
        FINISH_CURRENT_TASK,
        CREATE_NEXT_TASK,
        ADD_HISTORY_NOTE,
        DELETE_TASKS,
        DELETE_PLANT
    }

    private ActionMode actionMode = ActionMode.NONE;

    private boolean actionInProgress = false;
    private String actionSuccessMessage = "Done";
    private String actionFailMessage = "Action failed";
    private String actionPlantId = null;

    private CareTask pendingNextTask = null;

    private final List<CareTask> pendingTaskDeletes = new ArrayList<>();
    private int deleteTaskIndex = 0;
    private Plant pendingPlantDelete = null;

    private RemindersViewModel remindersViewModel;

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
        setLayout(R.layout.activity_my_plants);
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
        setViewModel();
        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);
        rvMyPlants = drawerLayout.findViewById(R.id.rvMyPlants);
        fabAddPlant = drawerLayout.findViewById(R.id.fabAddPlant);
        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void setRecyclerView() {
        adapter = new MyPlantsAdapter(new ArrayList<>());

        adapter.setListener(new MyPlantsAdapter.Listener() {
            @Override
            public void onOpen(Plant plant) {
                if (plant == null) {
                    Toast.makeText(MyPlantsActivity.this, "Plant not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MyPlantsActivity.this, PlantDetailsActivity.class);
                intent.putExtra(PlantDetailsActivity.EXTRA_SELECTED_PLANT, plant);
                startActivity(intent);
            }

            @Override
            public void onMarkDone(Plant plant) {
                handleTaskAction(plant, true);
            }

            @Override
            public void onSkip(Plant plant) {
                handleTaskAction(plant, false);
            }
        });

        adapter.setOnItemLongClickListener((plant, position) -> {
            if (plant == null) return false;

            AlertDialogHelper.showDelete(
                    this,
                    "Delete \"" + safeText(plant.getNickname()) + "\"?",
                    () -> startDeletePlantFlow(plant)
            );
            return true;
        });

        rvMyPlants.setLayoutManager(new LinearLayoutManager(this));
        rvMyPlants.setAdapter(adapter);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        fabAddPlant.setOnClickListener(v -> launcherHelper.launchActivity(AddPlantActivity.class));

        navMyPlants.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

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
        plantsViewModel = new ViewModelProvider(this).get(PlantsViewModel.class);
        careTasksViewModel = new ViewModelProvider(this).get(CareTasksViewModel.class);
        historyNotesViewModel = new ViewModelProvider(this).get(HistoryNotesViewModel.class);
        speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
        remindersViewModel = new ViewModelProvider(this).get(RemindersViewModel.class);

        showProgressDialog(null, "Loading your plants...");

        plantsViewModel.getAll();
        careTasksViewModel.getAll();
        speciesViewModel.getAll();

        plantsViewModel.getLiveDataCollection().observe(this, this::handlePlantsChanged);
        careTasksViewModel.getLiveDataCollection().observe(this, this::handleTasksChanged);
        speciesViewModel.getLiveDataCollection().observe(this, this::handleSpeciesChanged);

        plantsViewModel.getSuccess().observe(this, success -> {
            if (!actionInProgress || actionMode != ActionMode.DELETE_PLANT) return;

            if (!Boolean.TRUE.equals(success)) {
                finishActionFailure("Could not delete plant");
                return;
            }

            finishActionSuccess("Plant deleted");
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

            if (actionMode == ActionMode.DELETE_TASKS) {
                deleteTaskIndex++;

                if (deleteTaskIndex < pendingTaskDeletes.size()) {
                    careTasksViewModel.delete(pendingTaskDeletes.get(deleteTaskIndex));
                } else {
                    actionMode = ActionMode.DELETE_PLANT;
                    plantsViewModel.delete(pendingPlantDelete);
                }
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

    private void handleSpeciesChanged(Species species) {
        speciesLoaded = true;
        speciesNamesByIdFs.clear();
        speciesNamesByApiId.clear();

        if (species != null) {
            for (Specie specie : species) {
                if (specie == null) continue;

                if (specie.getIdFs() != null && !specie.getIdFs().trim().isEmpty()) {
                    speciesNamesByIdFs.put(specie.getIdFs(), safeText(specie.getName()));
                }

                if (specie.getApiId() > 0) {
                    speciesNamesByApiId.put(String.valueOf(specie.getApiId()), safeText(specie.getName()));
                }
            }
        }

        refreshRecyclerData();
        hideLoadingIfReady();
    }

    private void refreshRecyclerData() {
        if (adapter == null) return;

        buildPlantUiMaps();

        adapter.setSpeciesNameByPlantId(new HashMap<>(speciesNameByPlantId));
        adapter.setNextTaskByPlantId(new HashMap<>(nextTaskByPlantId));
        adapter.setItems(new ArrayList<>(userPlants));

        if (plantsLoaded && userPlants.isEmpty() && !emptyToastShown) {
            emptyToastShown = true;
            Toast.makeText(this,
                    "No plants yet. Tap + to add your first plant",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void buildPlantUiMaps() {
        speciesNameByPlantId.clear();
        nextTaskByPlantId.clear();

        for (Plant plant : userPlants) {
            if (plant == null || plant.getIdFs() == null) continue;

            String speciesName = safeText(plant.getSpeciesName());
            if ("-".equals(speciesName)) {
                speciesName = resolveSpeciesName(plant.getSpeciesId());
            }

            speciesNameByPlantId.put(plant.getIdFs(), speciesName);

            CareTask nextTask = findNextScheduledTask(plant.getIdFs());
            if (nextTask != null) {
                nextTaskByPlantId.put(plant.getIdFs(), nextTask);
            }
        }
    }

    private CareTask findNextScheduledTask(String plantId) {
        CareTask nextTask = null;

        for (CareTask task : allTasks) {
            if (task == null || task.getPlantId() == null || !task.getPlantId().equals(plantId)) {
                continue;
            }

            if (task.getState() != CareTask.State.SCHEDULED || task.getNextDueAt() == null) {
                continue;
            }

            if (nextTask == null) {
                nextTask = task;
            } else {
                long currentTime = task.getNextDueAt().toDate().getTime();
                long bestTime = nextTask.getNextDueAt().toDate().getTime();

                if (currentTime < bestTime) {
                    nextTask = task;
                }
            }
        }

        return nextTask;
    }

    private String resolveSpeciesName(String speciesId) {
        if (speciesId == null || speciesId.trim().isEmpty()) {
            return "-";
        }

        String name = speciesNamesByIdFs.get(speciesId);
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }

        name = speciesNamesByApiId.get(speciesId);
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }

        return speciesId;
    }

    private void handleTaskAction(Plant plant, boolean isMarkDone) {
        if (plant == null || plant.getIdFs() == null) {
            Toast.makeText(this, "Plant not found", Toast.LENGTH_SHORT).show();
            return;
        }

        CareTask currentTask = nextTaskByPlantId.get(plant.getIdFs());
        if (currentTask == null) {
            Toast.makeText(this, "No upcoming task for this plant", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.requireInternet(MyPlantsActivity.this)) {
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

        actionPlantId = plant.getIdFs();
        adapter.setHiddenNextTaskPlantId(actionPlantId);

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

    private void startDeletePlantFlow(Plant plant) {
        if (plant == null || plant.getIdFs() == null) return;
        if (!NetworkUtils.requireInternet(MyPlantsActivity.this)) {
            return;
        }

        pendingPlantDelete = plant;
        pendingTaskDeletes.clear();

        for (CareTask task : allTasks) {
            if (task != null && plant.getIdFs().equals(task.getPlantId())) {
                pendingTaskDeletes.add(task);
            }
        }

        actionInProgress = true;
        actionSuccessMessage = "Plant deleted";
        actionFailMessage = "Could not delete plant";

        showProgressDialog(null, "Deleting plant...");

        if (pendingTaskDeletes.isEmpty()) {
            actionMode = ActionMode.DELETE_PLANT;
            plantsViewModel.delete(pendingPlantDelete);
        } else {
            actionMode = ActionMode.DELETE_TASKS;
            deleteTaskIndex = 0;
            careTasksViewModel.delete(pendingTaskDeletes.get(deleteTaskIndex));
        }
    }

    private void finishActionSuccess(String message) {
        hideProgressDialog();
        resetActionState();
        adapter.setHiddenNextTaskPlantId(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (currentUser != null) {
            TaskNotificationRescheduler.rescheduleAllForCurrentUser(this, currentUser, remindersViewModel);        }
    }

    private void finishActionFailure(String message) {
        hideProgressDialog();
        resetActionState();
        adapter.setHiddenNextTaskPlantId(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void resetActionState() {
        actionInProgress = false;
        actionMode = ActionMode.NONE;
        pendingNextTask = null;
        pendingHistoryNote = null;
        actionPlantId = null;

        pendingTaskDeletes.clear();
        deleteTaskIndex = 0;
        pendingPlantDelete = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            TaskNotificationRescheduler.rescheduleAllForCurrentUser(this, currentUser, remindersViewModel);        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    2001
            );
        }
    }

    private void hideLoadingIfReady() {
        if (plantsLoaded && tasksLoaded && speciesLoaded && !actionInProgress) {
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