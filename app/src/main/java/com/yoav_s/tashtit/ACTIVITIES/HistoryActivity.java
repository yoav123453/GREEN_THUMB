package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.HistoryNote;
import com.yoav_s.model.HistoryNotes;
import com.yoav_s.model.Plant;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.HistoryEntriesAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.HistoryNotesViewModel;


import java.util.ArrayList;
import com.yoav_s.helper.BitMapHelper;

public class HistoryActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_PLANT = "SELECTED_PLANT";

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private MaterialCardView cardNoteComposer;
    private EditText etNote;
    private ImageButton btnAddNote;
    private ImageButton btnCancelNote;
    private ImageButton btnAddPhoto;

    private RecyclerView rvHistory;
    private MaterialButton btnBack;
    private FloatingActionButton fabAddHistoryNote;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;
    private HistoryNotesViewModel historyNotesViewModel;

    private HistoryEntriesAdapter adapter;

    private Plant selectedPlant;

    private boolean notesLoaded = false;
    private boolean addNoteInProgress = false;

    private Bitmap pendingPhotoBitmap = null;
    private String pendingPhotoBase64 = null;

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
        setLayout(R.layout.activity_history);
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
        setRecyclerView();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        cardNoteComposer = drawerLayout.findViewById(R.id.cardNoteComposer);
        etNote = drawerLayout.findViewById(R.id.etNote);
        btnAddNote = drawerLayout.findViewById(R.id.btnAddNote);
        btnCancelNote = drawerLayout.findViewById(R.id.btnCancelNote);
        btnAddPhoto = drawerLayout.findViewById(R.id.btnAddPhoto);

        rvHistory = drawerLayout.findViewById(R.id.rvHistory);
        btnBack = drawerLayout.findViewById(R.id.btnBack);
        fabAddHistoryNote = drawerLayout.findViewById(R.id.fabAddHistoryNote);

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

    private void setRecyclerView() {
        adapter = new HistoryEntriesAdapter(new ArrayList<>());
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnBack.setOnClickListener(v -> finish());

        fabAddHistoryNote.setOnClickListener(v -> {
            if (cardNoteComposer.getVisibility() == android.view.View.VISIBLE) {
                etNote.requestFocus();
            } else {
                cardNoteComposer.setVisibility(android.view.View.VISIBLE);
            }
        });

        btnCancelNote.setOnClickListener(v -> clearComposer(true));

        btnAddPhoto.setOnClickListener(v ->
                launcherHelper.takePhotoWithPermissionCheck(bitmap -> {
                    if (bitmap == null) {
                        Toast.makeText(this, "Could not take photo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pendingPhotoBitmap = bitmap;
                    pendingPhotoBase64 = BitMapHelper.encodeTobase64(bitmap);

                    updatePhotoButtonState();
                    Toast.makeText(this, "Photo attached", Toast.LENGTH_SHORT).show();
                })
        );

        btnAddNote.setOnClickListener(v -> addHistoryNote());

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
        historyNotesViewModel = new ViewModelProvider(this).get(HistoryNotesViewModel.class);

        if (selectedPlant == null || selectedPlant.getIdFs() == null || selectedPlant.getIdFs().trim().isEmpty()) {
            Toast.makeText(this, "Plant data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgressDialog(null, "Loading history...");

        historyNotesViewModel.getByPlant(selectedPlant.getIdFs());

        historyNotesViewModel.getLiveDataCollection().observe(this, this::handleNotesChanged);

        historyNotesViewModel.getSuccess().observe(this, success -> {
            if (!addNoteInProgress) return;

            hideProgressDialog();
            addNoteInProgress = false;

            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
                clearComposer(true);

                historyNotesViewModel.getByPlant(selectedPlant.getIdFs());
            } else {
                Toast.makeText(this, "Could not add note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotesChanged(HistoryNotes notes) {
        notesLoaded = true;

        if (notes == null) {
            adapter.setItems(new ArrayList<>());
            hideLoadingIfReady();
            return;
        }

        adapter.setItems(notes);
        hideLoadingIfReady();
    }

    private void addHistoryNote() {
        if (selectedPlant == null) return;

        String text = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        if (text.isEmpty() && (pendingPhotoBase64 == null || pendingPhotoBase64.trim().isEmpty())) {
            Toast.makeText(this, "Write a note or add a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        HistoryNote note = new HistoryNote();
        note.setPlantId(selectedPlant.getIdFs());
        note.setEntryType(HistoryNote.EntryType.TEXT);
        note.setText(text);
        note.setPhoto(pendingPhotoBase64);
        note.setCreatedAt(Timestamp.now());

        addNoteInProgress = true;
        showProgressDialog(null, "Adding note...");
        historyNotesViewModel.addHistoryNote(note);
    }

    private void clearComposer(boolean hideComposer) {
        etNote.setText("");
        pendingPhotoBitmap = null;
        pendingPhotoBase64 = null;
        updatePhotoButtonState();

        if (hideComposer) {
            cardNoteComposer.setVisibility(android.view.View.GONE);
        }
    }

    private void updatePhotoButtonState() {
        boolean hasPhoto = pendingPhotoBase64 != null && !pendingPhotoBase64.trim().isEmpty();

        btnAddPhoto.setImageTintList(null);
        btnAddPhoto.clearColorFilter();

        if (hasPhoto) {
            btnAddPhoto.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#E8F3E9"))
            );
        } else {
            btnAddPhoto.setBackgroundTintList(
                    ColorStateList.valueOf(Color.TRANSPARENT)
            );
        }
    }

    private void hideLoadingIfReady() {
        if (notesLoaded && !addNoteInProgress) {
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

}