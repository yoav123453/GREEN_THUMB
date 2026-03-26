package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class GuestHomeActivity extends BaseActivity {
    private SpeciesApiViewModel vm;
    private SpeciesAdapter adapter;

    private EditText etSearchSpecies;
    private Spinner spCategory;
    private RecyclerView rvPlants;

    private MaterialButton btnGuides, btnSignIn, btnRegister;

    private MaterialButton btnSearchSpecies;

    private LauncherHelper launcherHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_guest_home);
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
        setRecyclerView();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        etSearchSpecies = findViewById(R.id.etSearchSpecies);
        spCategory = findViewById(R.id.spCategory);
        rvPlants = findViewById(R.id.rvPlants);

        btnSearchSpecies = findViewById(R.id.btnSearchSpecies);
        btnGuides = findViewById(R.id.btnGuides);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);


        String[] cats = new String[]{"All", "Tree", "Shrub", "Flower", "Grass", "Other"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cats);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(spAdapter);

    }
    private void setRecyclerView() {
        adapter = new SpeciesAdapter(null);

        rvPlants.setAdapter(adapter);
        rvPlants.setLayoutManager(new LinearLayoutManager(this));
        rvPlants.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return; // only when user scrolls down

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

    }
    @Override
    protected void setListeners() {

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, SignInActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnGuides.setOnClickListener(v ->
                Toast.makeText(this, "Guides not implemented yet", Toast.LENGTH_SHORT).show());

        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = (String) spCategory.getSelectedItem();
                if (vm != null) vm.setCategory(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnSearchSpecies.setOnClickListener(v -> {
            String q = etSearchSpecies.getText().toString();
            vm.searchSpecies(q);
        });


        adapter.setOnItemClickListener(new com.uri_r.tashtit.ADPTERS.BASE.GenericAdapter.OnItemClickListener<Specie>() {
            @Override
            public void onItemClick(Specie item, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("SPECIE", item);
                launcherHelper.launchActivity(SpeciesDetailsGuestActivity.class, bundle);
            }
        });
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

        vm.getSpecies().observe(this, list -> adapter.setItems(list));

        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        vm.searchSpecies("");
    }
}
