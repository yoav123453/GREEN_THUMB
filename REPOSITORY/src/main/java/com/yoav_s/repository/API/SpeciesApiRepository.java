package com.yoav_s.repository.API;

import androidx.annotation.Nullable;

import com.yoav_s.model.Specie;
import com.yoav_s.repository.BuildConfig;
import com.yoav_s.repository.API.perenual.PerenualApiClient;
import com.yoav_s.repository.API.perenual.PerenualSpeciesDto;
import com.yoav_s.repository.API.perenual.PerenualSpeciesListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeciesApiRepository {
    public interface CallbackResult {
        void onSuccess(List<Specie> results);
        void onError(String message);
    }

    public void fetchSpeciesPage(int page, @Nullable String query, CallbackResult cb) {
        String q = (query == null || query.trim().isEmpty()) ? null : query.trim();

        PerenualApiClient.getService()
                .speciesList(BuildConfig.PERENUAL_API_KEY, page, q)
                .enqueue(new Callback<PerenualSpeciesListResponse>() {
                    @Override
                    public void onResponse(Call<PerenualSpeciesListResponse> call,
                                           Response<PerenualSpeciesListResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            String details = "";
                            try {
                                if (response.errorBody() != null) details = response.errorBody().string();
                            } catch (Exception ignored) {}

                            cb.onError("API error: " + response.code() + (details.isEmpty() ? "" : ("\n" + details)));
                            return;
                        }

                        List<Specie> mapped = new ArrayList<>();
                        if (response.body().data != null) {
                            for (PerenualSpeciesDto dto : response.body().data) {
                                Specie s = mapDtoToSpecie(dto);
                                if (s != null) mapped.add(s);
                            }
                        }
                        cb.onSuccess(mapped);
                    }

                    @Override
                    public void onFailure(Call<PerenualSpeciesListResponse> call, Throwable t) {
                        cb.onError(t.getMessage() == null ? "Network error" : t.getMessage());
                    }
                });
    }

    private Specie mapDtoToSpecie(PerenualSpeciesDto dto) {
        String name = pickName(dto);
        if (name == null) return null;

        Specie.Category category = mapCategoryFromType(dto.type);
        Specie.Light light = mapLight(dto);

        int waterDays = guessWaterDays(dto);

        // Defaults (you said we’ll fix later)
        int fertilizeDays = 30;
        int sprayDays = 14;
        int pruneDays = 60;
        int repotDays = 180;

        Specie s = new Specie(name, category, waterDays, fertilizeDays, sprayDays, pruneDays, repotDays, light);
        s.setApiId(dto.id); //for dedupe
        return s;
    }

    private String pickName(PerenualSpeciesDto dto) {
        if (dto.commonName != null && !dto.commonName.trim().isEmpty()) return dto.commonName.trim();
        if (dto.scientificName != null && !dto.scientificName.isEmpty() && dto.scientificName.get(0) != null) {
            String v = dto.scientificName.get(0).trim();
            return v.isEmpty() ? null : v;
        }
        return null;
    }

    private Specie.Category mapCategoryFromType(String typeRaw) {
        String type = (typeRaw == null) ? "" : typeRaw.toLowerCase(Locale.ROOT);

        if (type.contains("tree")) return Specie.Category.TREE;
        if (type.contains("shrub")) return Specie.Category.SHRUB;
        if (type.contains("grass")) return Specie.Category.GRASS;
        if (type.contains("climber") || type.contains("vine")) return Specie.Category.CLIMBER;
        if (type.contains("succulent") || type.contains("cactus")) return Specie.Category.SUCCULENT;
        if (type.contains("flower")) return Specie.Category.FLOWER;

        return Specie.Category.FLOWER;
    }

    private Specie.Light mapLight(PerenualSpeciesDto dto) {
        if (dto.sunlight == null || dto.sunlight.isEmpty()) return Specie.Light.PART_SHADE;

        // pickes the “strongest” value if multiple exist
        String joined = dto.sunlight.toString().toLowerCase(Locale.ROOT);

        if (joined.contains("full_sun")) return Specie.Light.FULL_SUN;
        if (joined.contains("sun-part_shade")) return Specie.Light.SUN_PART_SHADE;
        if (joined.contains("part_shade")) return Specie.Light.PART_SHADE;
        if (joined.contains("full_shade")) return Specie.Light.FULL_SHADE;

        return Specie.Light.PART_SHADE;
    }

    private int guessWaterDays(PerenualSpeciesDto dto) {
        if (dto.watering == null) return 7;
        String w = dto.watering.toLowerCase(Locale.ROOT);

        // docs: frequent/average/minimum/none :contentReference[oaicite:3]{index=3}
        if (w.contains("frequent")) return 3;
        if (w.contains("average")) return 7;
        if (w.contains("minimum")) return 14;
        if (w.contains("none")) return 30;
        return 7;
    }
}
