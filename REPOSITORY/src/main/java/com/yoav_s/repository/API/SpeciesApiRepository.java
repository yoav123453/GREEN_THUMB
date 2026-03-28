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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

                        try {
                            PerenualSpeciesDetailsDto dto = response.body();

                            baseSpecie.setLight(mapLight(asStringList(dto.sunlight)));
                            baseSpecie.setBaselineCarewateringDays(guessWaterDays(dto.watering));
                            baseSpecie.setBaselineCarefertilizeDays(deriveFertilizeDays(dto, baseSpecie));
                            baseSpecie.setBaselineCaresprayDays(deriveSprayDays(dto, baseSpecie));
                            baseSpecie.setBaselineCarepruneDays(derivePruneDays(dto, baseSpecie));
                            baseSpecie.setBaselineCarerepotDays(deriveRepotDays(dto, baseSpecie));

                            cb.onSuccess(baseSpecie);
                        } catch (Exception e) {
                            Log.e("PERENUAL_EXCEPTION", "details mapping failed", e);
                            cb.onError(e.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<PerenualSpeciesDetailsDto> call, Throwable t) {
                        Log.e("PERENUAL_EXCEPTION", "speciesDetails failed", t);
                        cb.onError(t.toString());
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

        int fertilizeDays = -1;
        int sprayDays = -1;
        int pruneDays = -1;
        int repotDays = -1;

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

    private int deriveFertilizeDays(PerenualSpeciesDetailsDto dto, Specie specie) {
        String typeText = detailsText(dto);
        String cycle = normalize(dto.cycle);
        String growth = normalize(dto.growthRate);
        boolean indoor = asBoolean(dto.indoor);

        int days;

        if (isSucculentOrCactus(typeText)) {
            days = 60;
        } else if (containsAny(typeText, "herb", "vegetable")) {
            days = 21;
        } else if (cycle.contains("annual")) {
            days = 21;
        } else if (indoor && isTropicalOrIndoorFoliage(typeText, specie)) {
            days = 30;
        } else if (cycle.contains("perennial")) {
            days = 45;
        } else if (specie.getCategory() == Specie.Category.TREE || specie.getCategory() == Specie.Category.SHRUB) {
            days = 60;
        } else {
            days = 30;
        }

        if (containsAny(growth, "high", "fast", "rapid")) {
            days = Math.round(days * 0.75f);
        } else if (containsAny(growth, "low", "slow")) {
            days = Math.round(days * 1.5f);
        }

        return clamp(days, 14, 90);
    }

    private int deriveSprayDays(PerenualSpeciesDetailsDto dto, Specie specie) {
        String typeText = detailsText(dto);
        boolean indoor = asBoolean(dto.indoor);
        boolean pestRisk = !asStringList(dto.pestSusceptibility).isEmpty();

        String soilText = joinedNormalizedList(asStringList(dto.soil));
        String sunText = joinedNormalizedList(asStringList(dto.sunlight));

        if (isSucculentOrCactus(typeText)) {
            return 0;
        }

        boolean humidPlant =
                isTropicalOrIndoorFoliage(typeText, specie)
                        || containsAny(soilText, "peat", "humus", "loam", "rich")
                        || containsAny(sunText, "part shade", "part_shade", "full shade", "full_shade");

        if (indoor && humidPlant) {
            return pestRisk ? 3 : 4;
        }

        if (indoor) {
            return pestRisk ? 5 : 7;
        }

        if (humidPlant) {
            return pestRisk ? 7 : 10;
        }

        if (specie.getCategory() == Specie.Category.FLOWER
                || specie.getCategory() == Specie.Category.OTHER) {
            return 14;
        }

        return 0;
    }

    private int derivePruneDays(PerenualSpeciesDetailsDto dto, Specie specie) {
        int pruningAmount = getPruningAmount(dto.pruningCount);
        String pruningInterval = getPruningInterval(dto.pruningCount);

        if (pruningAmount > 0) {
            int intervalDays = intervalToDays(pruningInterval);
            if (intervalDays > 0) {
                return Math.max(1, Math.round((float) intervalDays / pruningAmount));
            }
        }

        int pruningMonthsCount = countDistinctNonEmpty(asStringList(dto.pruningMonth));
        if (pruningMonthsCount > 0) {
            return Math.max(30, Math.round(365f / pruningMonthsCount));
        }

        if (isSucculentOrCactus(detailsText(dto))) {
            return 180;
        }

        switch (specie.getCategory()) {
            case TREE:
                return 365;
            case SHRUB:
                return 180;
            case FLOWER:
                return 120;
            case GRASS:
                return 90;
            case OTHER:
            default:
                return asBoolean(dto.indoor) ? 180 : 0;
        }
    }

    private int deriveRepotDays(PerenualSpeciesDetailsDto dto, Specie specie) {
        String typeText = detailsText(dto);
        String cycle = normalize(dto.cycle);
        String growth = normalize(dto.growthRate);
        boolean indoor = asBoolean(dto.indoor);

        if (cycle.contains("annual")) {
            return 0;
        }

        if (isSucculentOrCactus(typeText)) {
            return 730;
        }

        int days;
        if (containsAny(growth, "high", "fast", "rapid")) {
            days = 240;
        } else if (containsAny(growth, "low", "slow")) {
            days = 540;
        } else {
            days = 365;
        }

        if (indoor) {
            return clamp(days, 180, 730);
        }

        if (specie.getCategory() == Specie.Category.TREE
                || specie.getCategory() == Specie.Category.SHRUB) {
            return clamp(Math.max(days, 730), 180, 730);
        }

        if (specie.getCategory() == Specie.Category.GRASS) {
            return clamp(Math.max(days, 540), 180, 730);
        }

        return clamp(days, 180, 730);
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String detailsText(PerenualSpeciesDetailsDto dto) {
        StringBuilder sb = new StringBuilder();

        if (dto.type != null) sb.append(dto.type).append(" ");
        if (dto.commonName != null) sb.append(dto.commonName).append(" ");
        if (dto.scientificName != null) {
            for (String s : dto.scientificName) {
                if (s != null) sb.append(s).append(" ");
            }
        }

        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isSucculentOrCactus(String text) {
        return containsAny(text, "succulent", "cactus", "cacti");
    }

    private boolean isTropicalOrIndoorFoliage(String text, Specie specie) {
        if (containsAny(text,
                "tropical", "houseplant", "indoor", "foliage", "fern", "calathea",
                "philodendron", "monstera", "maranta", "alocasia", "anthurium",
                "palm", "dracaena", "begonia", "orchid")) {
            return true;
        }

        return specie.getCategory() == Specie.Category.OTHER || specie.getCategory() == Specie.Category.FLOWER;
    }

    private int intervalToDays(String interval) {
        String v = normalize(interval);

        if (containsAny(v, "year", "yearly", "annual")) return 365;
        if (containsAny(v, "season", "seasonal")) return 90;
        if (containsAny(v, "month", "monthly")) return 30;
        if (containsAny(v, "week", "weekly")) return 7;
        if (containsAny(v, "day", "daily")) return 1;

        return 0;
    }

    private int countDistinctNonEmpty(List<String> values) {
        if (values == null || values.isEmpty()) return 0;

        Set<String> set = new HashSet<>();
        for (String s : values) {
            String v = normalize(s);
            if (!v.isEmpty()) set.add(v);
        }
        return set.size();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<String> asStringList(JsonElement element) {
        List<String> out = new ArrayList<>();

        if (element == null || element.isJsonNull()) return out;

        try {
            if (element.isJsonArray()) {
                for (JsonElement item : element.getAsJsonArray()) {
                    if (item != null && !item.isJsonNull()) {
                        String v = item.getAsString();
                        if (v != null && !v.trim().isEmpty()) {
                            out.add(v.trim());
                        }
                    }
                }
            } else if (element.isJsonPrimitive()) {
                String v = element.getAsString();
                if (v != null && !v.trim().isEmpty()) {
                    out.add(v.trim());
                }
            }
        } catch (Exception ignored) {
        }

        return out;
    }

    private boolean asBoolean(JsonElement element) {
        if (element == null || element.isJsonNull()) return false;

        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isBoolean()) {
                    return element.getAsBoolean();
                }
                if (element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsInt() != 0;
                }
                if (element.getAsJsonPrimitive().isString()) {
                    String v = normalize(element.getAsString());
                    return v.equals("true") || v.equals("1") || v.equals("yes");
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private int getPruningAmount(JsonElement element) {
        if (element == null || element.isJsonNull()) return 0;

        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement amountEl = obj.get("amount");
                if (amountEl != null && !amountEl.isJsonNull()) {
                    return amountEl.getAsInt();
                }
            } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }
        } catch (Exception ignored) {
        }

        return 0;
    }

    private String getPruningInterval(JsonElement element) {
        if (element == null || element.isJsonNull()) return "";

        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement intervalEl = obj.get("interval");
                if (intervalEl != null && !intervalEl.isJsonNull()) {
                    return intervalEl.getAsString();
                }
            } else if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
        } catch (Exception ignored) {
        }

        return "";
    }
    private String joinedNormalizedList(List<String> values) {
        if (values == null || values.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                sb.append(v.trim()).append(" ");
            }
        }

        return normalize(sb.toString());
    }
}
