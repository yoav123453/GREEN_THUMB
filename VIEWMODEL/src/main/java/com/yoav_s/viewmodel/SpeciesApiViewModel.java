package com.yoav_s.viewmodel;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yoav_s.model.Specie;
import com.yoav_s.repository.API.SpeciesApiRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class SpeciesApiViewModel extends AndroidViewModel {

    private final SpeciesApiRepository apiRepo = new SpeciesApiRepository();

    private final MutableLiveData<Boolean> lvLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> lvError = new MutableLiveData<>(null);
    private final MutableLiveData<List<Specie>> lvSpecies = new MutableLiveData<>(new ArrayList<>());

    private final List<Specie> allItems = new ArrayList<>();
    private final HashSet<Integer> seenApiIds = new HashSet<>();

    private String lastQuery = "";
    private Specie.Category selectedCategory = null; // null = All
    private int currentPage = 1;
    private boolean isLastPage = false;

    public SpeciesApiViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getLoading() { return lvLoading; }
    public LiveData<String> getError() { return lvError; }
    public LiveData<List<Specie>> getSpecies() { return lvSpecies; }

    public void setCategory(@NonNull String spinnerValue) {
        switch (spinnerValue) {
            case "Tree": selectedCategory = Specie.Category.TREE; break;
            case "Shrub": selectedCategory = Specie.Category.SHRUB; break;
            case "Flower": selectedCategory = Specie.Category.FLOWER; break;
            case "Succulent": selectedCategory = Specie.Category.SUCCULENT; break;
            case "Climber": selectedCategory = Specie.Category.CLIMBER; break;
            case "Grass": selectedCategory = Specie.Category.GRASS; break;
            default: selectedCategory = null; // All
        }
        updateDisplayedList();
    }

    public void searchSpecies(String query) {
        if (Boolean.TRUE.equals(lvLoading.getValue())) return;

        lastQuery = (query == null) ? "" : query.trim();
        currentPage = 1;
        isLastPage = false;

        allItems.clear();
        seenApiIds.clear();
        lvSpecies.setValue(new ArrayList<>());

        loadPage(currentPage);
    }

    public void loadNextPage() {
        if (Boolean.TRUE.equals(lvLoading.getValue())) return;
        if (isLastPage) return;

        loadPage(currentPage + 1);
    }

    private void loadPage(int page) {
        lvLoading.setValue(true);
        lvError.setValue(null);

        apiRepo.fetchSpeciesPage(page, lastQuery, new SpeciesApiRepository.CallbackResult() {
            @Override
            public void onSuccess(List<Specie> results) {
                lvLoading.postValue(false);

                if (results == null || results.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                currentPage = page;

                for (Specie s : results) {
                    if (s == null) continue;

                    int id = s.getApiId();
                    if (id != 0) {
                        if (seenApiIds.contains(id)) continue;
                        seenApiIds.add(id);
                    }
                    allItems.add(s);
                }

                // if less than typical page size -> last page
                if (results.size() < 30) isLastPage = true;

                updateDisplayedList(); // ✅ filter + sort every time
            }

            @Override
            public void onError(String message) {
                lvLoading.postValue(false);
                lvError.postValue(message);
            }
        });
    }
    private void updateDisplayedList() {
        List<Specie> out = new ArrayList<>();

        if (selectedCategory == null) {
            out.addAll(allItems);
        } else {
            for (Specie s : allItems) {
                if (s != null && s.getCategory() == selectedCategory) out.add(s);
            }
        }
        out.sort(Comparator.comparing(
                s -> s.getName() == null ? "" : s.getName().toLowerCase(),
                String::compareTo
        ));

        lvSpecies.postValue(out);
    }
}