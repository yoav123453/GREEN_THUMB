package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.helper.NetworkUtils;
import com.yoav_s.model.Guide;
import com.yoav_s.model.Guides;
import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.GuidesAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.GuidesViewModel;
import com.yoav_s.viewmodel.UsersViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GuidesActivity extends BaseActivity {

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private EditText etGuideTitleSearch;
    private MaterialButton btnSearchGuides;

    private RecyclerView rvGuides;

    private MaterialCardView cardCreatorActions;
    private MaterialButton btnAnalytics;
    private MaterialButton btnCreateGuide;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;

    private GuidesViewModel guidesViewModel;
    private UsersViewModel usersViewModel;

    private GuidesAdapter adapter;

    private final List<Guide> allGuides = new ArrayList<>();
    private final Map<String, String> authorNameByUserId = new HashMap<>();

    private boolean guidesLoaded = false;
    private boolean usersLoaded = false;

    private boolean deleteInProgress = false;
    private Guide pendingDeleteGuide = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_guides);
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
        updateCreatorActionsVisibility();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        etGuideTitleSearch = drawerLayout.findViewById(R.id.etGuideTitleSearch);
        btnSearchGuides = drawerLayout.findViewById(R.id.btnSearchGuides);

        rvGuides = drawerLayout.findViewById(R.id.rvGuides);

        cardCreatorActions = drawerLayout.findViewById(R.id.cardCreatorActions);
        btnAnalytics = drawerLayout.findViewById(R.id.btnAnalytics);
        btnCreateGuide = drawerLayout.findViewById(R.id.btnCreateGuide);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void setRecyclerView() {
        adapter = new GuidesAdapter(new ArrayList<>());

        adapter.setListener(new GuidesAdapter.Listener() {
            @Override
            public void onOpen(Guide guide) {
                if (guide == null || guide.getIdFs() == null || guide.getIdFs().trim().isEmpty()) {
                    Toast.makeText(GuidesActivity.this, "Guide not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(GuideDetailsActivity.EXTRA_SELECTED_GUIDE_ID, guide.getIdFs());
                launcherHelper.launchActivity(GuideDetailsActivity.class, bundle);
            }

            @Override
            public void onItemClicked(Guide guide) {
                if (!NetworkUtils.requireInternet(GuidesActivity.this)) {
                    return;
                }
                if (guide == null || currentUser == null || currentUser.getIdFs() == null) {
                    return;
                }

                if (currentUser.getRole() != User.Role.CONTENT_CREATOR) {
                    return;
                }

                if (guide.getContentCreatorId() == null) {
                    return;
                }

                if (!currentUser.getIdFs().equals(guide.getContentCreatorId())) {
                    return;
                }

                AlertDialogHelper.showDelete(
                        GuidesActivity.this,
                        "Do you want to delete your guide?",
                        () -> deleteGuide(guide),
                        () -> {
                            // pressed No
                        }
                );
            }
        });

        rvGuides.setLayoutManager(new LinearLayoutManager(this));
        rvGuides.setAdapter(adapter);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnSearchGuides.setOnClickListener(v -> applyGuideFilter(true));

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

        navGuides.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

        navAi.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(AIAssistantActivity.class);
            finish();
        });

        navLogout.setOnClickListener(v -> logout());

        btnAnalytics.setOnClickListener(v -> {
            if (currentUser == null || currentUser.getRole() != User.Role.CONTENT_CREATOR) {
                Toast.makeText(this, "Only content creators can access analytics", Toast.LENGTH_SHORT).show();
                return;
            }

            launcherHelper.launchActivity(AnalyticsActivity.class);
        });

        btnCreateGuide.setOnClickListener(v -> {
            if (currentUser == null || currentUser.getRole() != User.Role.CONTENT_CREATOR) {
                Toast.makeText(this, "Only content creators can create guides", Toast.LENGTH_SHORT).show();
                return;
            }

            launcherHelper.launchActivity(CreateGuideActivity.class, result -> {
                if (LauncherHelper.isResultOk(result)) {
                    Toast.makeText(this, "Guide published", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void setViewModel() {
        guidesViewModel = new ViewModelProvider(this).get(GuidesViewModel.class);
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        showProgressDialog(null, "Loading guides...");

        guidesViewModel.getAll();
        usersViewModel.getAll();

        guidesViewModel.getLiveDataCollection().observe(this, this::handleGuidesChanged);
        usersViewModel.getLiveDataCollection().observe(this, this::handleUsersChanged);

        guidesViewModel.getSuccess().observe(this, success -> {
            if (!deleteInProgress) return;

            hideProgressDialog();

            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Guide deleted", Toast.LENGTH_SHORT).show();

                if (pendingDeleteGuide != null) {
                    allGuides.remove(pendingDeleteGuide);
                    applyGuideFilter(false);
                }
            } else {
                Toast.makeText(this, "Could not delete guide", Toast.LENGTH_SHORT).show();
            }

            deleteInProgress = false;
            pendingDeleteGuide = null;
        });
    }

    private void handleGuidesChanged(Guides guides) {
        guidesLoaded = true;
        allGuides.clear();

        if (guides != null) {
            allGuides.addAll(guides);
        }

        applyGuideFilter(false);
        hideLoadingIfReady();
    }

    private void handleUsersChanged(Users users) {
        usersLoaded = true;
        authorNameByUserId.clear();

        if (users != null) {
            for (User user : users) {
                if (user == null || user.getIdFs() == null) continue;
                authorNameByUserId.put(user.getIdFs(), safeText(user.getDisplayName()));
            }
        }

        adapter.setAuthorNameByUserId(new HashMap<>(authorNameByUserId));
        hideLoadingIfReady();
    }

    private void applyGuideFilter(boolean showNoResultsToast) {
        String query = etGuideTitleSearch.getText() != null
                ? etGuideTitleSearch.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";

        List<Guide> filtered = new ArrayList<>();

        for (Guide guide : allGuides) {
            if (guide == null) continue;

            String title = guide.getTitle() != null ? guide.getTitle().trim() : "";

            if (query.isEmpty() || title.toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(guide);
            }
        }

        adapter.setAuthorNameByUserId(new HashMap<>(authorNameByUserId));
        adapter.setItems(filtered);

        if (showNoResultsToast && filtered.isEmpty()) {
            Toast.makeText(this, "No guides found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCreatorActionsVisibility() {
        boolean isContentCreator = currentUser != null
                && currentUser.getRole() == User.Role.CONTENT_CREATOR;

        cardCreatorActions.setVisibility(
                isContentCreator ? android.view.View.VISIBLE : android.view.View.GONE
        );
    }
    private void deleteGuide(Guide guide) {
        if (guide == null || guide.getIdFs() == null || guide.getIdFs().trim().isEmpty()) {
            Toast.makeText(this, "Guide not found", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingDeleteGuide = guide;
        deleteInProgress = true;

        showProgressDialog(null, "Deleting guide...");
        guidesViewModel.delete(guide);
    }
    private void hideLoadingIfReady() {
        if (guidesLoaded && usersLoaded && !deleteInProgress) {
            hideProgressDialog();
        }
    }

    private void logout() {
        drawerLayout.closeDrawer(GravityCompat.END);
        currentUser = null;
        launcherHelper.launchActivity(SignInActivity.class);
        finish();
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