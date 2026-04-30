package com.yoav_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Specie;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.SpeciesApiViewModel;

public class SpeciesDetailsGuestActivity extends BaseActivity {

    private TextView tvSpeciesNameValue;
    private TextView tvCategoryValue;
    private TextView tvLightValue;
    private TextView tvWaterDays;
    private TextView tvFertilizeDays;
    private TextView tvSprayDays;
    private TextView tvPruneDays;
    private TextView tvRepotDays;

    private MaterialButton btnSignInToAdd;
    private MaterialButton btnRegisterToAdd;
    private MaterialButton btnBack;

    private Specie specie;
    private LauncherHelper launcherHelper;
    private SpeciesApiViewModel vm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_species_details_guest);
        setBottomNavigationVisibility(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        setViewModel();
        getExtras();
        showSpecieData();
        if (specie != null && specie.getApiId() > 0) {
            vm.loadSpecieDetails(specie);
        }
    }

    @Override
    protected void initializeViews() {
        tvSpeciesNameValue = findViewById(R.id.tvSpeciesNameValue);
        tvCategoryValue = findViewById(R.id.tvCategoryValue);
        tvLightValue = findViewById(R.id.tvLightValue);

        tvWaterDays = findViewById(R.id.tvWaterDays);
        tvFertilizeDays = findViewById(R.id.tvFertilizeDays);
        tvSprayDays = findViewById(R.id.tvSprayDays);
        tvPruneDays = findViewById(R.id.tvPruneDays);
        tvRepotDays = findViewById(R.id.tvRepotDays);

        btnSignInToAdd = findViewById(R.id.btnSignInToAdd);
        btnRegisterToAdd = findViewById(R.id.btnRegisterToAdd);
        btnBack = findViewById(R.id.btnBack);

        setListeners();
    }

    @Override
    protected void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSignInToAdd.setOnClickListener(v -> {
            if (specie == null) {
                Toast.makeText(this, "Specie not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putBoolean("OPEN_ADD_PLANT_AFTER_AUTH", true);
            bundle.putSerializable("SELECTED_SPECIE", specie);

            launcherHelper.launchActivity(SignInActivity.class, bundle);
        });

        btnRegisterToAdd.setOnClickListener(v -> {
            if (specie == null) {
                Toast.makeText(this, "Specie not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putBoolean("OPEN_ADD_PLANT_AFTER_AUTH", true);
            bundle.putSerializable("SELECTED_SPECIE", specie);

            launcherHelper.launchActivity(RegisterActivity.class, bundle);
        });
    }

    @Override
    protected void setViewModel() {
        vm = new ViewModelProvider(this).get(SpeciesApiViewModel.class);

        vm.getSelectedSpecie().observe(this, updatedSpecie -> {
            if (updatedSpecie != null) {
                specie = updatedSpecie;
                showSpecieData();
            }
        });

        vm.getError().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getExtras() {
        if (getIntent().hasExtra("SPECIE")) {
            specie = (Specie) getIntent().getSerializableExtra("SPECIE");
        }

        if (specie == null) {
            Toast.makeText(this, "Specie data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showSpecieData() {
        if (specie == null) return;

        tvSpeciesNameValue.setText(getSafeText(specie.getName()));
        tvCategoryValue.setText(formatCategory(specie.getCategory()));
        tvLightValue.setText(formatLight(specie.getLight()));

        tvWaterDays.setText(formatDays(specie.getBaselineCarewateringDays()));
        tvFertilizeDays.setText(formatDays(specie.getBaselineCarefertilizeDays()));
        tvSprayDays.setText(formatDays(specie.getBaselineCaresprayDays()));
        tvPruneDays.setText(formatDays(specie.getBaselineCarepruneDays()));
        tvRepotDays.setText(formatDays(specie.getBaselineCarerepotDays()));
    }

    private String getSafeText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }

    private String formatCategory(Specie.Category category) {
        if (category == null) return "-";

        switch (category) {
            case TREE:
                return "Tree";
            case SHRUB:
                return "Shrub";
            case FLOWER:
                return "Flower";
            case GRASS:
                return "Grass";
            case OTHER:
            default:
                return "Other";
        }
    }

    private String formatLight(Specie.Light light) {
        if (light == null) return "-";
        return light.getApiValue();
    }

    private String formatDays(int days) {
        if (days < 0) return "-";
        return String.valueOf(days);
    }
}