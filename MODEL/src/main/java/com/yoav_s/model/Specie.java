package com.yoav_s.model;

import com.yoav_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Specie extends BaseEntity implements Serializable {

    public enum Category { HOUSE, SUCCULENT, HERB, ORNAMENTAL }
    public enum Light { LOW, MED, HIGH }

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
        Specie specie = (Specie) o;
        return baselineCarewateringDays == specie.baselineCarewateringDays && baselineCarefertilizeDays == specie.baselineCarefertilizeDays && baselineCaresprayDays == specie.baselineCaresprayDays && baselineCarepruneDays == specie.baselineCarepruneDays && baselineCarerepotDays == specie.baselineCarerepotDays && Objects.equals(name, specie.name) && category == specie.category && light == specie.light;
    }
}

