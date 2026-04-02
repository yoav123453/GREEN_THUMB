package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
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
import com.google.firebase.Timestamp;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Guide;
import com.yoav_s.model.User;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.GuidesViewModel;

public class CreateGuideActivity extends BaseActivity {

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private EditText etGuideTitle;
    private EditText etGuideBody;

    private MaterialButton btnPublish;
    private MaterialButton btnCancel;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private GuidesViewModel guidesViewModel;

    private boolean publishInProgress = false;

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
            Toast.makeText(this, "Only content creators can create guides", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_create_guide);
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
        configureBodyInputScrolling();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        etGuideTitle = drawerLayout.findViewById(R.id.etGuideTitle);
        etGuideBody = drawerLayout.findViewById(R.id.etGuideBody);

        btnPublish = drawerLayout.findViewById(R.id.btnPublish);
        btnCancel = drawerLayout.findViewById(R.id.btnCancel);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }
    private void configureBodyInputScrolling() {
        etGuideBody.setVerticalScrollBarEnabled(true);
        etGuideBody.setMovementMethod(new ScrollingMovementMethod());

        etGuideBody.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }

            return false;
        });
    }
    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnPublish.setOnClickListener(v -> publishGuide());

        btnCancel.setOnClickListener(v -> finish());

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

        guidesViewModel.getSuccess().observe(this, success -> {
            if (!publishInProgress) return;

            hideProgressDialog();
            publishInProgress = false;

            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Guide published", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Could not publish guide", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publishGuide() {
        String title = etGuideTitle.getText() != null
                ? etGuideTitle.getText().toString().trim()
                : "";

        String body = etGuideBody.getText() != null
                ? etGuideBody.getText().toString().trim()
                : "";

        etGuideTitle.setError(null);
        etGuideBody.setError(null);

        if (title.isEmpty()) {
            etGuideTitle.setError("Enter a title");
            Toast.makeText(this, "Guide title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (body.isEmpty()) {
            etGuideBody.setError("Enter guide content");
            Toast.makeText(this, "Guide body is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Guide guide = new Guide();
        guide.setContentCreatorId(currentUser.getIdFs());
        guide.setTitle(title);
        guide.setText(body);
        guide.setViewsCount(0);
        guide.setCommentsCount(0);
        guide.setAvgRating(0.0);
        guide.setPublishedAt(Timestamp.now());

        publishInProgress = true;
        showProgressDialog(null, "Publishing guide...");
        guidesViewModel.add(guide);
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