package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
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
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Guide;
import com.yoav_s.model.GuideInteraction;
import com.yoav_s.model.GuideInteractions;
import com.yoav_s.model.Guides;
import com.yoav_s.model.User;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.GuideInteractionsViewModel;
import com.yoav_s.viewmodel.GuidesViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends BaseActivity {

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private TextView tvTotalGuidesValue;
    private TextView tvAvgRatingValue;
    private TextView tvTotalViewsValue;
    private TextView tvTotalCommentsValue;
    private TextView tvMostLikedGuideValue;

    private MaterialButton btnBack;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private GuidesViewModel guidesViewModel;
    private GuideInteractionsViewModel guideInteractionsViewModel;

    private final List<Guide> authoredGuides = new ArrayList<>();
    private final List<GuideInteraction> allInteractions = new ArrayList<>();
    private final Map<String, Guide> authoredGuideById = new HashMap<>();

    private boolean guidesLoaded = false;
    private boolean interactionsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (currentUser == null) {
            Toast.makeText(this, "For signed-in/registered users only", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        if (currentUser.getRole() != User.Role.CONTENT_CREATOR) {
            Toast.makeText(this, "Only content creators can access analytics", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_analytics);
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
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        tvTotalGuidesValue = drawerLayout.findViewById(R.id.tvTotalGuidesValue);
        tvAvgRatingValue = drawerLayout.findViewById(R.id.tvAvgRatingValue);
        tvTotalViewsValue = drawerLayout.findViewById(R.id.tvTotalViewsValue);
        tvTotalCommentsValue = drawerLayout.findViewById(R.id.tvTotalCommentsValue);
        tvMostLikedGuideValue = drawerLayout.findViewById(R.id.tvMostLikedGuideValue);

        btnBack = drawerLayout.findViewById(R.id.btnBack);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnBack.setOnClickListener(v -> finish());

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
        navLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            currentUser = null;
            launcherHelper.launchActivity(SignInActivity.class);
            finish();
        });
    }

    @Override
    protected void setViewModel() {
        guidesViewModel = new ViewModelProvider(this).get(GuidesViewModel.class);
        guideInteractionsViewModel = new ViewModelProvider(this).get(GuideInteractionsViewModel.class);

        showProgressDialog(null, "Loading analytics...");

        // important with your infrastructure: load first, then observe
        guidesViewModel.getAll();
        guideInteractionsViewModel.getAll();

        guidesViewModel.getLiveDataCollection().observe(this, this::handleGuidesChanged);
        guideInteractionsViewModel.getLiveDataCollection().observe(this, this::handleInteractionsChanged);
    }

    private void handleGuidesChanged(Guides guides) {
        guidesLoaded = true;

        authoredGuides.clear();
        authoredGuideById.clear();

        if (guides != null && currentUser != null && currentUser.getIdFs() != null) {
            for (Guide guide : guides) {
                if (guide == null || guide.getContentCreatorId() == null) continue;

                if (currentUser.getIdFs().equals(guide.getContentCreatorId())) {
                    authoredGuides.add(guide);
                    if (guide.getIdFs() != null) {
                        authoredGuideById.put(guide.getIdFs(), guide);
                    }
                }
            }
        }

        updateAnalyticsUi();
        hideLoadingIfReady();
    }

    private void handleInteractionsChanged(GuideInteractions interactions) {
        interactionsLoaded = true;

        allInteractions.clear();
        if (interactions != null) {
            allInteractions.addAll(interactions);
        }

        updateAnalyticsUi();
        hideLoadingIfReady();
    }

    private void updateAnalyticsUi() {
        if (!guidesLoaded || !interactionsLoaded) return;

        int totalGuides = authoredGuides.size();
        int totalViews = 0;
        int totalComments = 0;

        double ratingsSum = 0.0;
        int ratingsCount = 0;

        Map<String, Integer> likesByGuideId = new HashMap<>();

        for (Guide guide : authoredGuides) {
            if (guide == null) continue;
            totalViews += Math.max(guide.getViewsCount(), 0);
            if (guide.getIdFs() != null) {
                likesByGuideId.put(guide.getIdFs(), 0);
            }
        }

        for (GuideInteraction interaction : allInteractions) {
            if (interaction == null || interaction.getGuideId() == null) continue;
            if (!authoredGuideById.containsKey(interaction.getGuideId())) continue;

            if (interaction.getBody() != null && !interaction.getBody().trim().isEmpty()) {
                totalComments++;
            }

            if (interaction.getRating() > 0) {
                ratingsSum += interaction.getRating();
                ratingsCount++;
            }

            if (interaction.isLike()) {
                int currentLikes = likesByGuideId.containsKey(interaction.getGuideId())
                        ? likesByGuideId.get(interaction.getGuideId())
                        : 0;
                likesByGuideId.put(interaction.getGuideId(), currentLikes + 1);
            }
        }

        double avgRating = ratingsCount > 0 ? (ratingsSum / ratingsCount) : 0.0;

        String mostLikedTitle = "-";
        int maxLikes = 0;

        for (Map.Entry<String, Integer> entry : likesByGuideId.entrySet()) {
            String guideId = entry.getKey();
            int likes = entry.getValue();

            if (likes > maxLikes) {
                maxLikes = likes;

                Guide guide = authoredGuideById.get(guideId);
                mostLikedTitle = guide != null ? safeText(guide.getTitle()) : "-";
            }
        }

        tvTotalGuidesValue.setText(String.valueOf(totalGuides));
        tvAvgRatingValue.setText(String.format(Locale.getDefault(), "★%.1f", avgRating));
        tvTotalViewsValue.setText(String.valueOf(totalViews));
        tvTotalCommentsValue.setText(String.valueOf(totalComments));
        tvMostLikedGuideValue.setText(mostLikedTitle);
    }

    private void hideLoadingIfReady() {
        if (guidesLoaded && interactionsLoaded) {
            hideProgressDialog();
        }
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