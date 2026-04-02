package com.yoav_s.tashtit.ACTIVITIES;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Specie;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.SpeciesAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.SpeciesApiViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddPlantActivity extends BaseActivity {

    private SpeciesApiViewModel vm;
    private SpeciesAdapter adapter;

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;
    private Spinner spCategory;
    private RecyclerView rvSpeciesResults;

    private View cardSpeciesResults;

    private EditText etSpeciesSearch;
    private EditText etNickname;
    private EditText etLocation;

    private MaterialButton btnContinue;
    private MaterialButton btnCancel;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;

    private final List<Specie> currentSpeciesResults = new ArrayList<>();
    private Specie selectedSpecie = null;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private boolean ignoreSpeciesTextChanges = false;

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
        setLayout(R.layout.activity_add_plant);
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
        setCategorySpinner();
        setRecyclerView();
        setListeners();
        setViewModel();
        readExtras();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);
        spCategory = drawerLayout.findViewById(R.id.spCategory);

        etSpeciesSearch = drawerLayout.findViewById(R.id.etSpeciesSearch);
        rvSpeciesResults = drawerLayout.findViewById(R.id.rvSpeciesResults);
        cardSpeciesResults = drawerLayout.findViewById(R.id.cardSpeciesResults);

        etNickname = drawerLayout.findViewById(R.id.etNickname);
        etLocation = drawerLayout.findViewById(R.id.etLocation);

        btnContinue = drawerLayout.findViewById(R.id.btnContinue);
        btnCancel = drawerLayout.findViewById(R.id.btnCancel);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void setCategorySpinner() {
        String[] cats = new String[]{"All", "Tree", "Shrub", "Flower", "Grass", "Other"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cats);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(spAdapter);
    }

    private void setRecyclerView() {
        adapter = new SpeciesAdapter(new ArrayList<>());

        rvSpeciesResults.setAdapter(adapter);
        rvSpeciesResults.setLayoutManager(new LinearLayoutManager(this));
        rvSpeciesResults.setNestedScrollingEnabled(true);

        rvSpeciesResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;

                int total = lm.getItemCount();
                int lastVisible = lm.findLastVisibleItemPosition();

                boolean loading = Boolean.TRUE.equals(vm.getLoading().getValue());
                if (!loading && !vm.isLastPage() && lastVisible >= total - 4) {
                    vm.loadNextPage();
                }
            }
        });

        hideSpeciesResults();
    }

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

        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) spCategory.getSelectedItem();
                if (vm != null) vm.setCategory(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        etSpeciesSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!ignoreSpeciesTextChanges) {
                    selectedSpecie = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ignoreSpeciesTextChanges) return;

                String query = s.toString().trim();

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (vm != null) {
                        vm.searchSpecies(query);
                    }
                };

                searchHandler.postDelayed(searchRunnable, 350);
            }
        });

        etSpeciesSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && adapter.getItemCount() > 0) {
                showSpeciesResults();
            }
        });

        adapter.setOnItemClickListener((item, position) -> {
            if (item == null) return;

            selectedSpecie = item;

            ignoreSpeciesTextChanges = true;
            etSpeciesSearch.setText(item.getName());
            etSpeciesSearch.setSelection(etSpeciesSearch.getText().length());
            ignoreSpeciesTextChanges = false;

            hideSpeciesResults();
        });

        btnContinue.setOnClickListener(v -> onContinueClicked());

        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void setViewModel() {
        vm = new ViewModelProvider(this).get(SpeciesApiViewModel.class);

        String selected = (String) spCategory.getSelectedItem();
        if (selected != null) vm.setCategory(selected);

        vm.getLoading().observe(this, loading -> {
            if (Boolean.TRUE.equals(loading)) showProgressDialog(null, "Loading species...");
            else hideProgressDialog();
        });

        vm.getSpecies().observe(this, list -> {
            currentSpeciesResults.clear();
            if (list != null) currentSpeciesResults.addAll(list);

            if (list == null) {
                adapter.setItems(new ArrayList<>());
            } else {
                adapter.setItems(list);
            }

            boolean shouldShowResults =
                    etSpeciesSearch.hasFocus()
                            && list != null
                            && !list.isEmpty();

            if (shouldShowResults) {
                showSpeciesResults();
            } else {
                hideSpeciesResults();
            }
        });

        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        vm.searchSpecies("");
    }

    private void readExtras() {
        Intent intent = getIntent();
        if (intent == null) return;

        Object specieObj = intent.getSerializableExtra("SELECTED_SPECIE");
        String nickname = intent.getStringExtra("PLANT_NICKNAME");
        String location = intent.getStringExtra("PLANT_LOCATION");

        if (specieObj instanceof Specie) {
            selectedSpecie = (Specie) specieObj;

            ignoreSpeciesTextChanges = true;
            etSpeciesSearch.setText(selectedSpecie.getName());
            etSpeciesSearch.setSelection(etSpeciesSearch.getText().length());
            ignoreSpeciesTextChanges = false;

            if (selectedSpecie.getCategory() != null) {
                spCategory.setSelection(getSpinnerPositionForCategory(selectedSpecie.getCategory()));
            }
        }

        if (nickname != null) {
            etNickname.setText(nickname);
        }

        if (location != null) {
            etLocation.setText(location);
        }
    }

    private void onContinueClicked() {
        clearFieldErrors();

        Specie resolvedSpecie = resolveSelectedSpecieFromInput();
        String nickname = etNickname.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (resolvedSpecie == null) {
            etSpeciesSearch.setError("Choose a species from the results");
            etSpeciesSearch.requestFocus();
            return;
        }

        if (nickname.isEmpty()) {
            etNickname.setError("Enter a nickname");
            etNickname.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Enter a location");
            etLocation.requestFocus();
            return;
        }

        selectedSpecie = resolvedSpecie;

        Bundle bundle = new Bundle();
        bundle.putSerializable("SELECTED_SPECIE", selectedSpecie);
        bundle.putString("PLANT_NICKNAME", nickname);
        bundle.putString("PLANT_LOCATION", location);

        launcherHelper.launchActivity(ScheduleSetupActivity.class, bundle, result -> {
            if (LauncherHelper.isResultOk(result)) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void clearFieldErrors() {
        etSpeciesSearch.setError(null);
        etNickname.setError(null);
        etLocation.setError(null);
    }

    private Specie resolveSelectedSpecieFromInput() {
        String typedName = etSpeciesSearch.getText().toString().trim();

        if (typedName.isEmpty()) {
            return null;
        }

        if (selectedSpecie != null
                && selectedSpecie.getName() != null
                && selectedSpecie.getName().trim().equalsIgnoreCase(typedName)) {
            return selectedSpecie;
        }

        for (Specie specie : currentSpeciesResults) {
            if (specie != null
                    && specie.getName() != null
                    && specie.getName().trim().equalsIgnoreCase(typedName)) {
                return specie;
            }
        }

        return null;
    }

    private int getSpinnerPositionForCategory(Specie.Category category) {
        if (category == null) return 0;

        switch (category) {
            case TREE:
                return 1;
            case SHRUB:
                return 2;
            case FLOWER:
                return 3;
            case GRASS:
                return 4;
            case OTHER:
            default:
                return 5;
        }
    }

    private void showSpeciesResults() {
        cardSpeciesResults.setVisibility(View.VISIBLE);
        rvSpeciesResults.setVisibility(View.VISIBLE);
    }

    private void hideSpeciesResults() {
        cardSpeciesResults.setVisibility(View.GONE);
        rvSpeciesResults.setVisibility(View.GONE);
    }

    private void logout() {
        drawerLayout.closeDrawer(GravityCompat.END);
        currentUser = null;
        launcherHelper.launchActivity(SignInActivity.class);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
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