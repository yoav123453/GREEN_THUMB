package com.yoav_s.repository.API.perenual;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PerenualSpeciesDetailsDto {
    @SerializedName("id") public int id;
    @SerializedName("common_name") public String commonName;
    @SerializedName("scientific_name") public List<String> scientificName;
    @SerializedName("type") public String type;

    @SerializedName("sunlight") public JsonElement sunlight;
    @SerializedName("watering") public String watering;

    @SerializedName("cycle") public String cycle;
    @SerializedName("indoor") public JsonElement indoor;
    @SerializedName("growth_rate") public String growthRate;
    @SerializedName("flowering_season") public String floweringSeason;

    @SerializedName("soil") public JsonElement soil;
    @SerializedName("pest_susceptibility") public JsonElement pestSusceptibility;

    @SerializedName("pruning_month") public JsonElement pruningMonth;
    @SerializedName("pruning_count") public JsonElement pruningCount;
}