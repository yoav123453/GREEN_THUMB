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
import java.util.Locale;

public class SpeciesApiViewModel extends AndroidViewModel {

    private final SpeciesApiRepository apiRepo = new SpeciesApiRepository();

    private final MutableLiveData<Specie> lvSelectedSpecie = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> lvLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> lvError = new MutableLiveData<>(null);
    private final MutableLiveData<List<Specie>> lvSpecies = new MutableLiveData<>(new ArrayList<>());

    private final List<Specie> allItems = new ArrayList<>();
    private final HashSet<Integer> seenApiIds = new HashSet<>();
    private final HashSet<String> seenNameKeys = new HashSet<>();

    private String userQuery = "";
    private Specie.Category selectedCategory = null;

    private int currentPage = 1;
    private boolean isLastPage = false;

    public SpeciesApiViewModel(@NonNull Application application) {
        super(application);
    }
    public LiveData<Specie> getSelectedSpecie() { return lvSelectedSpecie; }
    public LiveData<Boolean> getLoading() { return lvLoading; }
    public LiveData<String> getError() { return lvError; }
    public LiveData<List<Specie>> getSpecies() { return lvSpecies; }

    public boolean isLastPage() { return isLastPage; }

    public void setCategory(@NonNull String spinnerValue) {
        switch (spinnerValue) {
            case "Tree": selectedCategory = Specie.Category.TREE; break;
            case "Shrub": selectedCategory = Specie.Category.SHRUB; break;
            case "Flower": selectedCategory = Specie.Category.FLOWER; break;
            case "Grass": selectedCategory = Specie.Category.GRASS; break;
            case "Other": selectedCategory = Specie.Category.OTHER; break;
            default: selectedCategory = null; // All
        }

        // ✅ Like your original: changing category triggers a re-search
        searchSpecies(userQuery);
    }

    public void searchSpecies(String query) {
        if (Boolean.TRUE.equals(lvLoading.getValue())) return;

        userQuery = (query == null) ? "" : query.trim();

        currentPage = 1;
        isLastPage = false;

        allItems.clear();
        seenApiIds.clear();
        seenNameKeys.clear();
        lvSpecies.setValue(new ArrayList<>());

        loadPage(currentPage);
    }
    public void loadSpecieDetails(Specie specie) {
        if (specie == null) return;

        lvLoading.setValue(true);
        lvError.setValue(null);

        apiRepo.fetchSpeciesDetails(specie, new SpeciesApiRepository.DetailsCallback() {
            @Override
            public void onSuccess(Specie result) {
                lvLoading.postValue(false);
                lvSelectedSpecie.postValue(result);
            }

            @Override
            public void onError(String message) {
                lvLoading.postValue(false);
                lvError.postValue(message);
            }
        });
    }
    public void loadNextPage() {
        if (Boolean.TRUE.equals(lvLoading.getValue())) return;
        if (isLastPage) return;

        loadPage(currentPage + 1);
    }

    private String buildEffectiveQuery() {
        // ✅ category keyword fetch to ensure the API returns that category
        String keyword = null;

        if (selectedCategory != null) {
            switch (selectedCategory) {
                case TREE: keyword = "tree"; break;
                case SHRUB: keyword = "shrub"; break;
                case FLOWER: keyword = "flower"; break;
                case GRASS: keyword = "grass"; break;
                case OTHER: keyword = ""; break; // Other can't be searched reliably
            }
        }

        if (selectedCategory == null) return userQuery; // All
        if (selectedCategory == Specie.Category.OTHER) return userQuery; // Other = show "unclassified" locally? (see below)

        if (keyword == null) return userQuery;
        if (userQuery.isEmpty()) return keyword;
        return userQuery + " " + keyword;
    }

    private void loadPage(int page) {
        lvLoading.setValue(true);
        lvError.setValue(null);

        String effectiveQuery = buildEffectiveQuery();

        apiRepo.fetchSpeciesPage(page, effectiveQuery, new SpeciesApiRepository.CallbackResult() {
            @Override
            public void onSuccess(List<Specie> results, int returnedPage, int lastPage) {
                lvLoading.postValue(false);

                if (results == null || results.isEmpty()) {
                    isLastPage = true;
                    updateDisplayedList(); // still update (especially for Other)
                    return;
                }

                currentPage = returnedPage;
                if (lastPage != Integer.MAX_VALUE && currentPage >= lastPage) isLastPage = true;

                boolean dedupeByName = !effectiveQuery.trim().isEmpty();

                for (Specie s : results) {
                    if (s == null) continue;

                    // ✅ If user is filtering by a category (except Other),
                    // force the category label so UI shows it consistently
                    if (selectedCategory != null && selectedCategory != Specie.Category.OTHER) {
                        s.setCategory(selectedCategory);
                    }

                    int id = s.getApiId();
                    if (id != 0) {
                        if (seenApiIds.contains(id)) continue;
                        seenApiIds.add(id);
                    }

                    if (dedupeByName) {
                        String nameKey = (s.getName() == null ? "" : s.getName().trim().toLowerCase(Locale.ROOT));
                        String catKey = (s.getCategory() == null ? "null" : s.getCategory().name());
                        String key = nameKey + "|" + catKey;

                        if (seenNameKeys.contains(key)) continue;
                        seenNameKeys.add(key);
                    }

                    allItems.add(s);
                }

                updateDisplayedList();
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

        // ✅ Filtering:
        // - For Tree/Shrub/Flower/Grass: show what we fetched (already forced category)
        // - For Other: show items where repository classified as OTHER
        if (selectedCategory == null) {
            out.addAll(allItems);
        } else if (selectedCategory == Specie.Category.OTHER) {
            for (Specie s : allItems) {
                if (s != null && s.getCategory() == Specie.Category.OTHER) out.add(s);
            }
        } else {
            out.addAll(allItems);
        }

        out.sort(Comparator.comparing(
                s -> s.getName() == null ? "" : s.getName().toLowerCase(Locale.ROOT),
                String::compareTo
        ));

        lvSpecies.postValue(out);
    }
}
