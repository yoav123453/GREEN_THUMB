package com.yoav_s.tashtit.ACTIVITIES;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.Timestamp;
import com.yoav_s.helper.DateUtil;
import com.yoav_s.helper.DateUtil_OLD;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.helper.inputValidators.EntryValidation;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Specie;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.CareTasksViewModel;
import com.yoav_s.viewmodel.PlantsViewModel;
import com.yoav_s.viewmodel.SpeciesApiViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleSetupActivity extends BaseActivity implements EntryValidation {

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private TextView tvPlantNickname;
    private TextView tvLightValue;
    private TextView tvStartDate;

    private MaterialCardView cardStartDate;

    private EditText etWaterDays;
    private EditText etFertilizeDays;
    private EditText etSprayDays;
    private EditText etPruneDays;
    private EditText etRepotDays;

    private CheckBox cbWater;
    private CheckBox cbFertilize;
    private CheckBox cbSpray;
    private CheckBox cbPrune;
    private CheckBox cbRepot;

    private MaterialButton btnSaveSchedule;
    private MaterialButton btnBack;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;

    private SpeciesApiViewModel speciesApiViewModel;
    private PlantsViewModel plantsViewModel;
    private CareTasksViewModel careTasksViewModel;

    // Data received from AddPlantActivity.
    private Specie selectedSpecie;
    private String plantNickname;
    private String plantLocation;

    // The date chosen by the user from the date picker.
    private LocalDate selectedStartDate = null;

    // Simpler save state than before:
    // first save the plant, then save its selected tasks.
    private boolean waitingForPlantSave = false;
    private boolean waitingForTaskSave = false;

    // Temporary data used while saving.
    private Plant pendingPlant = null;
    private final List<CareTask> pendingTasks = new ArrayList<>();
    private int taskSaveIndex = 0;

    // Lets the screen know when specie details are ready to use.
    private boolean specieDetailsReady = false;

    // Creates the screen, checks login, applies layout and window insets.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (currentUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_schedule_setup);
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

    // Runs the whole setup flow of the activity.
    @Override
    protected void initializeActivity() {
        launcherHelper = new LauncherHelper(this);
        initializeViews();
        readExtras();
        setListeners();
        setViewModel();

        tvPlantNickname.setText("\"" + plantNickname + "\"");

        if (selectedSpecie != null && selectedSpecie.getApiId() > 0) {
            speciesApiViewModel.loadSpecieDetails(selectedSpecie);
        } else {
            specieDetailsReady = true;
            showSpecieData();
        }
    }

    // Connects the XML views to Java fields and prepares the read-only day fields.
    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        tvPlantNickname = drawerLayout.findViewById(R.id.tvPlantNickname);
        tvLightValue = drawerLayout.findViewById(R.id.tvLightValue);
        tvStartDate = drawerLayout.findViewById(R.id.tvStartDate);

        cardStartDate = drawerLayout.findViewById(R.id.cardStartDate);

        etWaterDays = drawerLayout.findViewById(R.id.etWaterDays);
        etFertilizeDays = drawerLayout.findViewById(R.id.etFertilizeDays);
        etSprayDays = drawerLayout.findViewById(R.id.etSprayDays);
        etPruneDays = drawerLayout.findViewById(R.id.etPruneDays);
        etRepotDays = drawerLayout.findViewById(R.id.etRepotDays);

        cbWater = drawerLayout.findViewById(R.id.cbWater);
        cbFertilize = drawerLayout.findViewById(R.id.cbFertilize);
        cbSpray = drawerLayout.findViewById(R.id.cbSpray);
        cbPrune = drawerLayout.findViewById(R.id.cbPrune);
        cbRepot = drawerLayout.findViewById(R.id.cbRepot);

        btnSaveSchedule = drawerLayout.findViewById(R.id.btnSaveSchedule);
        btnBack = drawerLayout.findViewById(R.id.btnBack);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);

        setupReadOnlyField(etWaterDays);
        setupReadOnlyField(etFertilizeDays);
        setupReadOnlyField(etSprayDays);
        setupReadOnlyField(etPruneDays);
        setupReadOnlyField(etRepotDays);
    }

    // Sets all click listeners for menu, drawer actions, date picker, save and back.
    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        navMyPlants.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(MyPlantsActivity.class);
            finish();
        });

        navCalendar.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            Toast.makeText(this, "Calendar activity not implemented yet", Toast.LENGTH_SHORT).show();
        });

        navSettings.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            Toast.makeText(this, "Settings activity not implemented yet", Toast.LENGTH_SHORT).show();
        });

        navGuides.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            Toast.makeText(this, "Guides activity not implemented yet", Toast.LENGTH_SHORT).show();
        });

        navAi.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            Toast.makeText(this, "AI activity not implemented yet", Toast.LENGTH_SHORT).show();
        });

        navLogout.setOnClickListener(v -> logout());

        // Only the card opens the date picker. No need for the TextView click too.
        cardStartDate.setOnClickListener(v -> openDatePicker());

        btnBack.setOnClickListener(v -> finish());

        btnSaveSchedule.setOnClickListener(v -> {
            if (!validate()) return;
            saveSchedule();
        });
    }

    // Creates the ViewModels and observes specie loading plus the save flow.
    @Override
    protected void setViewModel() {
        speciesApiViewModel = new ViewModelProvider(this).get(SpeciesApiViewModel.class);
        plantsViewModel = new ViewModelProvider(this).get(PlantsViewModel.class);
        careTasksViewModel = new ViewModelProvider(this).get(CareTasksViewModel.class);

        speciesApiViewModel.getLoading().observe(this, loading -> {
            if (Boolean.TRUE.equals(loading)) {
                showProgressDialog(null, "Loading species care details...");
            } else if (!waitingForPlantSave && !waitingForTaskSave) {
                hideProgressDialog();
            }
        });

        speciesApiViewModel.getSelectedSpecie().observe(this, specie -> {
            if (specie != null) {
                selectedSpecie = specie;
                specieDetailsReady = true;
                showSpecieData();
                hideProgressDialog();
            }
        });

        speciesApiViewModel.getError().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                hideProgressDialog();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();

                // If loading fails, still try to use the specie object already passed from AddPlantActivity.
                specieDetailsReady = true;
                showSpecieData();
            }
        });

        // Handles the first step of saving: the plant document itself.
        plantsViewModel.getSuccess().observe(this, success -> {
            if (!waitingForPlantSave) return;

            waitingForPlantSave = false;

            if (!Boolean.TRUE.equals(success)) {
                hideProgressDialog();
                resetSaveState();
                Toast.makeText(this, "Could not save plant", Toast.LENGTH_SHORT).show();
                return;
            }

            pendingTasks.clear();
            pendingTasks.addAll(buildSelectedTasks(pendingPlant.getIdFs(), selectedStartDate));

            if (pendingTasks.isEmpty()) {
                hideProgressDialog();
                resetSaveState();
                Toast.makeText(this, "Select at least one valid task", Toast.LENGTH_SHORT).show();
                return;
            }

            waitingForTaskSave = true;
            taskSaveIndex = 0;

            showProgressDialog(null, "Saving care tasks...");
            saveNextTask();
        });

        // Handles the second step of saving: the selected care tasks.
        careTasksViewModel.getSuccess().observe(this, success -> {
            if (!waitingForTaskSave) return;

            if (!Boolean.TRUE.equals(success)) {
                waitingForTaskSave = false;
                hideProgressDialog();
                resetSaveState();
                Toast.makeText(this, "Could not save care tasks", Toast.LENGTH_SHORT).show();
                return;
            }

            taskSaveIndex++;

            if (taskSaveIndex < pendingTasks.size()) {
                saveNextTask();
            } else {
                waitingForTaskSave = false;
                hideProgressDialog();
                Toast.makeText(this, "Plant schedule saved", Toast.LENGTH_SHORT).show();
                resetSaveState();

                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    // Reads the specie, nickname and location sent from AddPlantActivity.
    private void readExtras() {
        Intent intent = getIntent();

        if (intent == null) {
            Toast.makeText(this, "Missing plant data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Object specieObj = intent.getSerializableExtra("SELECTED_SPECIE");
        plantNickname = intent.getStringExtra("PLANT_NICKNAME");
        plantLocation = intent.getStringExtra("PLANT_LOCATION");

        if (!(specieObj instanceof Specie) || plantNickname == null || plantLocation == null) {
            Toast.makeText(this, "Plant data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedSpecie = (Specie) specieObj;
    }

    // Shows the loaded specie light and baseline task intervals on screen.
    private void showSpecieData() {
        if (selectedSpecie == null) return;

        tvLightValue.setText(formatLight(selectedSpecie.getLight()));

        applyTaskRow(etWaterDays, cbWater, selectedSpecie.getBaselineCarewateringDays());
        applyTaskRow(etFertilizeDays, cbFertilize, selectedSpecie.getBaselineCarefertilizeDays());
        applyTaskRow(etSprayDays, cbSpray, selectedSpecie.getBaselineCaresprayDays());
        applyTaskRow(etPruneDays, cbPrune, selectedSpecie.getBaselineCarepruneDays());
        applyTaskRow(etRepotDays, cbRepot, selectedSpecie.getBaselineCarerepotDays());
    }

    // Fills one task row and disables it if the specie does not support that task.
    private void applyTaskRow(EditText etDays, CheckBox cbTask, int days) {
        if (days <= 0) {
            etDays.setText("-");
            cbTask.setChecked(false);
            cbTask.setEnabled(false);
            cbTask.setAlpha(0.45f);
        } else {
            etDays.setText(String.valueOf(days));
            cbTask.setEnabled(true);
            cbTask.setChecked(true);
            cbTask.setAlpha(1f);
        }
    }

    // Makes one EditText behave like a read-only display field.
    private void setupReadOnlyField(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setClickable(false);
        editText.setLongClickable(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
    }

    // Opens the date picker and stores the chosen date in selectedStartDate.
    private void openDatePicker() {
        LocalDate today = LocalDate.now();
        LocalDate openAt = selectedStartDate != null ? selectedStartDate : today;

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Choose start date")
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
            selectedStartDate = DateUtil_OLD.longToLocalDate(selection);
            tvStartDate.setText(DateUtil.localDateToString(selectedStartDate, DateUtil.FORMAT_DD_MM_YYYY));
        });

        picker.show(getSupportFragmentManager(), "START_DATE_PICKER");
    }

    // Clears previous validation messages and prepares validation state.
    @Override
    public void setValidation() {
        cbWater.setError(null);
        cbFertilize.setError(null);
        cbSpray.setError(null);
        cbPrune.setError(null);
        cbRepot.setError(null);
    }

    // Validates the screen before saving.
    // Uses the date infrastructure helper directly because the Validator DATE rule
    // only supports EditText and uses a hardcoded pattern that does not fit this screen well.
    @Override
    public boolean validate() {
        setValidation();

        if (!specieDetailsReady || selectedSpecie == null) {
            Toast.makeText(this, "Specie details are still loading", Toast.LENGTH_SHORT).show();
            return false;
        }

        LocalDate validDate = validateStartDate();
        if (validDate == null) {
            return false;
        }

        if (!hasAtLeastOneSelectedTask()) {
            cbWater.setError("Select at least one task");
            Toast.makeText(this, "Select at least one task type", Toast.LENGTH_SHORT).show();
            return false;
        }

        String speciesId = resolveSpeciesId(selectedSpecie);
        if (speciesId == null || speciesId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid species id", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Validates the chosen date using the helper infrastructure and prevents past dates.
    private LocalDate validateStartDate() {
        // If the picker already provided the date, that is the most reliable source.
        if (selectedStartDate != null) {
            if (selectedStartDate.isBefore(LocalDate.now())) {
                Toast.makeText(this, "Start date cannot be in the past", Toast.LENGTH_SHORT).show();
                return null;
            }
            return selectedStartDate;
        }

        // Fallback: if needed, parse the text with the date helper infrastructure.
        String dateText = tvStartDate.getText().toString().trim();

        if (dateText.isEmpty() || dateText.equalsIgnoreCase("Tap to choose a date")) {
            Toast.makeText(this, "Choose a valid start date", Toast.LENGTH_SHORT).show();
            return null;
        }

        LocalDate parsedDate = DateUtil_OLD.stringToLocalDate(dateText, "dd/MM/uuuu");

        if (parsedDate == null) {
            Toast.makeText(this, "Date must be in dd/MM/yyyy format", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            Toast.makeText(this, "Start date cannot be in the past", Toast.LENGTH_SHORT).show();
            return null;
        }

        return parsedDate;
    }

    // Starts the save flow after validation succeeds.
    private void saveSchedule() {
        pendingPlant = new Plant(
                resolveSpeciesId(selectedSpecie),
                selectedSpecie != null ? selectedSpecie.getName() : null,
                plantNickname,
                plantLocation,
                currentUser.getIdFs()
        );

        waitingForPlantSave = true;
        waitingForTaskSave = false;

        showProgressDialog(null, "Saving plant...");
        plantsViewModel.add(pendingPlant);
    }

    // Checks whether the user selected at least one valid task row.
    private boolean hasAtLeastOneSelectedTask() {
        return isSelectedValidTask(etWaterDays, cbWater)
                || isSelectedValidTask(etFertilizeDays, cbFertilize)
                || isSelectedValidTask(etSprayDays, cbSpray)
                || isSelectedValidTask(etPruneDays, cbPrune)
                || isSelectedValidTask(etRepotDays, cbRepot);
    }

    // Checks whether one task row is enabled, checked, and has a positive day interval.
    private boolean isSelectedValidTask(EditText etDays, CheckBox cbTask) {
        return cbTask.isEnabled() && cbTask.isChecked() && getTaskDays(etDays) > 0;
    }

    // Reads the day interval number from a task row safely.
    private int getTaskDays(EditText etDays) {
        try {
            return Integer.parseInt(etDays.getText().toString().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    // Decides which specie id should be saved inside Plant.
    // This is not a simple getter because it prefers apiId when available.
    private String resolveSpeciesId(Specie specie) {
        if (specie == null) return null;

        if (specie.getApiId() > 0) {
            return String.valueOf(specie.getApiId());
        }

        return specie.getIdFs();
    }

    // Builds all selected task objects for this plant.
    private List<CareTask> buildSelectedTasks(String plantId, LocalDate startDate) {
        List<CareTask> tasks = new ArrayList<>();

        maybeAddTask(tasks, plantId, CareTask.Type.WATER, etWaterDays, cbWater, startDate);
        maybeAddTask(tasks, plantId, CareTask.Type.FERTILIZE, etFertilizeDays, cbFertilize, startDate);
        maybeAddTask(tasks, plantId, CareTask.Type.SPRAY, etSprayDays, cbSpray, startDate);
        maybeAddTask(tasks, plantId, CareTask.Type.PRUNE, etPruneDays, cbPrune, startDate);
        maybeAddTask(tasks, plantId, CareTask.Type.REPOT, etRepotDays, cbRepot, startDate);

        return tasks;
    }

    // Adds one task to the save list only if it is selected and valid.
    private void maybeAddTask(List<CareTask> out,
                              String plantId,
                              CareTask.Type type,
                              EditText etDays,
                              CheckBox cbTask,
                              LocalDate startDate) {
        int everyDays = getTaskDays(etDays);

        if (!cbTask.isEnabled() || !cbTask.isChecked() || everyDays <= 0) {
            return;
        }

        out.add(buildTask(plantId, type, everyDays, startDate));
    }

    // Creates one CareTask object ready to save.
    private CareTask buildTask(String plantId, CareTask.Type type, int everyDays, LocalDate startDate) {
        CareTask task = new CareTask();

        String taskLabel = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        taskLabel = taskLabel.substring(0, 1).toUpperCase(Locale.ROOT) + taskLabel.substring(1);

        LocalDate dueDate = startDate.plusDays(everyDays);

        task.setPlantId(plantId);
        task.setType(type);
        task.setEveryDays(everyDays);
        task.setState(CareTask.State.SCHEDULED);
        task.setText(taskLabel);
        task.setPhoto(null);
        task.setPhotoUrl(null);
        task.setNextDueAt(new Timestamp(DateUtil_OLD.localDateToDate(dueDate)));
        task.setDoneAt(null);

        return task;
    }

    // Saves the next task in the list.
    private void saveNextTask() {
        if (taskSaveIndex < 0 || taskSaveIndex >= pendingTasks.size()) {
            return;
        }

        careTasksViewModel.add(pendingTasks.get(taskSaveIndex));
    }

    // Clears temporary save state after success or failure.
    private void resetSaveState() {
        waitingForPlantSave = false;
        waitingForTaskSave = false;
        pendingPlant = null;
        pendingTasks.clear();
        taskSaveIndex = 0;
    }

    // Uses the same light formatting style as SpeciesDetailsGuestActivity.
    private String formatLight(Specie.Light light) {
        if (light == null) return "-";
        return light.getApiValue();
    }

    // Logs the user out and returns to SignInActivity.
    private void logout() {
        drawerLayout.closeDrawer(GravityCompat.END);
        currentUser = null;
        launcherHelper.launchActivity(SignInActivity.class);
        finish();
    }

    // Closes the drawer first when the system back button is pressed.
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }
}