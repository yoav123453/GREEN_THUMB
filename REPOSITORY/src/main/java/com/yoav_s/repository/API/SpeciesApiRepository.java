package com.yoav_s.repository.API;

import android.util.Log;

import androidx.annotation.Nullable;

import com.yoav_s.model.Specie;
import com.yoav_s.repository.API.perenual.PerenualSpeciesDetailsDto;
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
        void onSuccess(List<Specie> results, int page, int lastPage);
        void onError(String message);
    }

    public interface DetailsCallback {
        void onSuccess(Specie specie);
        void onError(String message);
    }

    public void fetchSpeciesPage(int page, @Nullable String query, CallbackResult cb) {
        String q = (query == null || query.trim().isEmpty()) ? null : query.trim();

        Log.d("PERENUAL_CALL", "speciesList page=" + page + " q=" + q);

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
                            } catch (Exception ignored) {
                            }
                            cb.onError("API error: " + response.code() + (details.isEmpty() ? "" : ("\n" + details)));
                            return;
                        }

                        PerenualSpeciesListResponse body = response.body();

                        List<Specie> mapped = new ArrayList<>();
                        if (body.data != null) {
                            for (PerenualSpeciesDto dto : body.data) {
                                Specie s = mapDtoToSpecie(dto);
                                if (s != null) mapped.add(s);
                            }
                        }

                        int lastPage = (body.lastPage != null) ? body.lastPage : Integer.MAX_VALUE;
                        cb.onSuccess(mapped, page, lastPage);
                    }

                    @Override
                    public void onFailure(Call<PerenualSpeciesListResponse> call, Throwable t) {
                        cb.onError(t.getMessage() == null ? "Network error" : t.getMessage());
                    }
                });
    }

    public void fetchSpeciesDetails(Specie baseSpecie, DetailsCallback cb) {
        if (baseSpecie == null || baseSpecie.getApiId() <= 0) {
            cb.onError("Invalid specie id");
            return;
        }
        Log.d("PERENUAL_CALL", "speciesDetails id=" + baseSpecie.getApiId());
        PerenualApiClient.getService()
                .speciesDetails(baseSpecie.getApiId(), BuildConfig.PERENUAL_API_KEY)
                .enqueue(new Callback<PerenualSpeciesDetailsDto>() {
                    @Override
                    public void onResponse(Call<PerenualSpeciesDetailsDto> call,
                                           Response<PerenualSpeciesDetailsDto> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            String details = "";
                            try {
                                if (response.errorBody() != null) details = response.errorBody().string();
                            } catch (Exception ignored) {
                            }
                            cb.onError("API error: " + response.code() + (details.isEmpty() ? "" : ("\n" + details)));
                            return;
                        }

                        PerenualSpeciesDetailsDto dto = response.body();

                        baseSpecie.setLight(mapLight(dto.sunlight));
                        baseSpecie.setBaselineCarewateringDays(guessWaterDays(dto.watering));

                        cb.onSuccess(baseSpecie);
                    }

                    @Override
                    public void onFailure(Call<PerenualSpeciesDetailsDto> call, Throwable t) {
                        cb.onError(t.getMessage() == null ? "Network error" : t.getMessage());
                    }
                });
    }

    private Specie mapDtoToSpecie(PerenualSpeciesDto dto) {
        String name = pickName(dto);
        if (name == null) return null;

        Specie.Category category = mapCategory(dto);

        // list endpoint = basic data only
        Specie.Light light = null;
        int waterDays = 7;

        int fertilizeDays = 30;
        int sprayDays = 14;
        int pruneDays = 60;
        int repotDays = 180;

        Specie s = new Specie(name, category, waterDays, fertilizeDays, sprayDays, pruneDays, repotDays, light);
        s.setApiId(dto.id);
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

    private Specie.Category mapCategory(PerenualSpeciesDto dto) {
        String type = (dto.type == null) ? "" : dto.type.toLowerCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder();
        if (dto.commonName != null) sb.append(dto.commonName).append(" ");
        if (dto.scientificName != null) {
            for (String s : dto.scientificName) {
                if (s != null) sb.append(s).append(" ");
            }
        }
        String nameText = sb.toString().toLowerCase(Locale.ROOT);

        if (type.contains("tree") || type.contains("palm")) return Specie.Category.TREE;
        if (type.contains("shrub") || type.contains("bush")) return Specie.Category.SHRUB;
        if (type.contains("grass") || type.contains("bamboo")) return Specie.Category.GRASS;
        if (type.contains("flower") || type.contains("rose") || type.contains("orchid")
                || type.contains("lily") || type.contains("daisy") || type.contains("tulip")) {
            return Specie.Category.FLOWER;
        }

        if (containsAny(nameText, "tree", "oak", "maple", "pine", "cedar", "olive", "palm")) {
            return Specie.Category.TREE;
        }
        if (containsAny(nameText, "shrub", "bush", "azalea", "hydrangea", "boxwood", "hibiscus")) {
            return Specie.Category.SHRUB;
        }
        if (containsAny(nameText, "grass", "bamboo", "reed", "fescue")) {
            return Specie.Category.GRASS;
        }
        if (containsAny(nameText, "flower", "rose", "tulip", "lily", "orchid", "daisy", "sunflower")) {
            return Specie.Category.FLOWER;
        }

        return Specie.Category.OTHER;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) return false;

        for (String k : keywords) {
            if (k != null && !k.isEmpty() && text.contains(k)) return true;
        }

        return false;
    }

    private Specie.Light mapLight(List<String> sunlightValues) {
        if (sunlightValues == null || sunlightValues.isEmpty()) return null;

        for (String raw : sunlightValues) {
            Specie.Light parsed = Specie.Light.fromApi(raw);
            if (parsed != null) return parsed;
        }

        String joined = sunlightValues.toString().toLowerCase(Locale.ROOT);

        if (joined.contains("full sun") || joined.contains("full_sun")) return Specie.Light.FULL_SUN;
        if (joined.contains("sun part shade") || joined.contains("sun-part_shade") || joined.contains("sun_part_shade")) {
            return Specie.Light.SUN_PART_SHADE;
        }
        if (joined.contains("part shade") || joined.contains("part_shade")) return Specie.Light.PART_SHADE;
        if (joined.contains("full shade") || joined.contains("full_shade")) return Specie.Light.FULL_SHADE;

        return null;
    }

    private int guessWaterDays(String wateringValue) {
        if (wateringValue == null) return 7;

        String w = wateringValue.toLowerCase(Locale.ROOT);

        if (w.contains("frequent")) return 3;
        if (w.contains("average")) return 7;
        if (w.contains("minimum")) return 14;
        if (w.contains("none")) return 30;

        return 7;
    }
}
