package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Plant;
import com.yoav_s.model.Plants;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.AI.AskAiViewModel;
import com.yoav_s.viewmodel.PlantsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AIAssistantActivity extends BaseActivity {

    private static final String GENERAL_OPTION = "General";

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private Spinner spPlant;
    private EditText etPrompt;
    private MaterialButton btnAnswer;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private PlantsViewModel plantsViewModel;
    private AskAiViewModel aiViewModel;

    private final List<Plant> userPlants = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

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
        setLayout(R.layout.activity_aiassistant);
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
        configurePromptScrolling();
        setupSpinner();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        spPlant = drawerLayout.findViewById(R.id.spPlant);
        etPrompt = drawerLayout.findViewById(R.id.etPrompt);
        btnAnswer = drawerLayout.findViewById(R.id.btnAnswer);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void configurePromptScrolling() {
        etPrompt.setVerticalScrollBarEnabled(true);
        etPrompt.setMovementMethod(new ScrollingMovementMethod());

        etPrompt.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }

            return false;
        });
    }

    private void setupSpinner() {
        List<String> items = new ArrayList<>();
        items.add(GENERAL_OPTION);

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPlant.setAdapter(spinnerAdapter);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnAnswer.setOnClickListener(v -> askAi());

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

        navAi.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

        navLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            currentUser = null;
            launcherHelper.launchActivity(SignInActivity.class);
            finish();
        });
    }

    @Override
    protected void setViewModel() {
        plantsViewModel = new ViewModelProvider(this).get(PlantsViewModel.class);
        aiViewModel = new ViewModelProvider(this).get(AskAiViewModel.class);

        showProgressDialog(null, "Loading AI assistant...");

        plantsViewModel.getAll();

        plantsViewModel.getLiveDataCollection().observe(this, this::handlePlantsChanged);

        aiViewModel.isLoading.observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showProgressDialog(null, "Thinking...");
            } else {
                hideProgressDialog();
            }
        });

        aiViewModel.successResult.observe(this, answer -> {
            hideProgressDialog();

            if (answer == null || answer.trim().isEmpty()) {
                AlertDialogHelper.showError(this, "AI returned an empty answer");
                return;
            }

            AlertDialogHelper.showInfo(this, "AI Answer", answer.trim());
        });

        aiViewModel.errorResult.observe(this, errorMessage -> {
            hideProgressDialog();
            AlertDialogHelper.showError(
                    this,
                    "System error: " + (errorMessage != null ? errorMessage : "Unknown AI error")
            );
        });
    }

    private void handlePlantsChanged(Plants plants) {
        userPlants.clear();

        if (plants != null && currentUser != null && currentUser.getIdFs() != null) {
            for (Plant plant : plants) {
                if (plant == null || plant.getUserId() == null) continue;

                if (currentUser.getIdFs().equals(plant.getUserId())) {
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

        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add(GENERAL_OPTION);

        for (Plant plant : userPlants) {
            spinnerItems.add(safeText(plant.getNickname()));
        }

        spinnerAdapter.clear();
        spinnerAdapter.addAll(spinnerItems);
        spinnerAdapter.notifyDataSetChanged();

        hideProgressDialog();
    }

    private void askAi() {
        String userPrompt = etPrompt.getText() != null
                ? etPrompt.getText().toString().trim()
                : "";

        if (userPrompt.isEmpty()) {
            etPrompt.setError("Enter a prompt");
            Toast.makeText(this, "You must enter something in the prompt area", Toast.LENGTH_SHORT).show();
            return;
        }

        Plant selectedPlant = getSelectedPlant();
        String finalPrompt = buildAiPrompt(userPrompt, selectedPlant);

        aiViewModel.generateText(finalPrompt);
    }

    private Plant getSelectedPlant() {
        int position = spPlant.getSelectedItemPosition();

        if (position <= 0) {
            return null;
        }

        int plantIndex = position - 1;
        if (plantIndex < 0 || plantIndex >= userPlants.size()) {
            return null;
        }

        return userPlants.get(plantIndex);
    }

    private String buildAiPrompt(String userPrompt, Plant plant) {
        StringBuilder builder = new StringBuilder();

        builder.append("You are a helpful plant care assistant inside an Android app. ");
        builder.append("Give practical, clear, safe plant-care advice. ");
        builder.append("Answer in the same language as the user's question. ");
        builder.append("If you are not sure, say what to check next and avoid pretending to know facts you do not know. ");
        builder.append("Return plain text only. ");
        builder.append("Do not use markdown. ");
        builder.append("Do not use *, **, ***, _, #, bullet points, numbered lists, or bold formatting. ");

        if (plant == null) {
            builder.append("This is a general question about plants. ");
        } else {
            builder.append("This question is about the user's specific plant. ");
            builder.append("Plant nickname: ").append(safeText(plant.getNickname())).append(". ");
            builder.append("Species: ").append(safeText(plant.getSpeciesName())).append(". ");
            builder.append("Location in the house: ").append(safeText(plant.getLocation())).append(". ");
        }

        builder.append("User question: ").append(userPrompt);

        return builder.toString();
    }

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
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