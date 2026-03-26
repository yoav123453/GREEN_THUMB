package com.yoav_s.model;

import com.yoav_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class Specie extends BaseEntity implements Serializable {

    public enum Category { TREE, SHRUB, FLOWER, GRASS, OTHER }

    public enum Light {
        FULL_SUN("full_sun"),
        SUN_PART_SHADE("sun-part_shade"),
        PART_SHADE("part_shade"),
        FULL_SHADE("full_shade");
        private final String apiValue;

        Light(String apiValue) {
            this.apiValue = apiValue;
        }

        public String getApiValue() {
            return apiValue;
        }

        @Override
        public String toString() {
            return apiValue;
        }

        public static Light fromApi(String value) {
            if (value == null || value.trim().isEmpty()) return null;

            String v = value.trim()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[\\s-]+", "_");

            switch (v) {
                case "full_sun":
                    return FULL_SUN;
                case "sun_part_shade":
                    return SUN_PART_SHADE;
                case "part_shade":
                    return PART_SHADE;
                case "full_shade":
                    return FULL_SHADE;
                default:
                    return null;
            }
        }
    }

    private int apiId = 0;

    private String name;
    private Category category;

    private int baselineCarewateringDays;
    private int baselineCarefertilizeDays;
    private int baselineCaresprayDays;
    private int baselineCarepruneDays;
    private int baselineCarerepotDays;

    private Light light;

    public Specie() {}

    public Specie(String name, Category category,
                  int baselineCarewateringDays, int baselineCarefertilizeDays, int baselineCaresprayDays,
                  int baselineCarepruneDays, int baselineCarerepotDays, Light light) {
        this.name = name;
        this.category = category;
        this.baselineCarewateringDays = baselineCarewateringDays;
        this.baselineCarefertilizeDays = baselineCarefertilizeDays;
        this.baselineCaresprayDays = baselineCaresprayDays;
        this.baselineCarepruneDays = baselineCarepruneDays;
        this.baselineCarerepotDays = baselineCarerepotDays;
        this.light = light;
    }

    public int getApiId() { return apiId; }
    public void setApiId(int apiId) { this.apiId = apiId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getBaselineCarewateringDays() { return baselineCarewateringDays; }
    public void setBaselineCarewateringDays(int v) { this.baselineCarewateringDays = v; }

    public int getBaselineCarefertilizeDays() { return baselineCarefertilizeDays; }
    public void setBaselineCarefertilizeDays(int v) { this.baselineCarefertilizeDays = v; }

    public int getBaselineCaresprayDays() { return baselineCaresprayDays; }
    public void setBaselineCaresprayDays(int v) { this.baselineCaresprayDays = v; }

    public int getBaselineCarepruneDays() { return baselineCarepruneDays; }
    public void setBaselineCarepruneDays(int v) { this.baselineCarepruneDays = v; }

    public int getBaselineCarerepotDays() { return baselineCarerepotDays; }
    public void setBaselineCarerepotDays(int v) { this.baselineCarerepotDays = v; }

    public Light getLight() { return light; }
    public void setLight(Light light) { this.light = light; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Specie specie = (Specie) o;
        return apiId == specie.apiId && baselineCarewateringDays == specie.baselineCarewateringDays && baselineCarefertilizeDays == specie.baselineCarefertilizeDays && baselineCaresprayDays == specie.baselineCaresprayDays && baselineCarepruneDays == specie.baselineCarepruneDays && baselineCarerepotDays == specie.baselineCarerepotDays && Objects.equals(name, specie.name) && category == specie.category && light == specie.light;
    }
}

