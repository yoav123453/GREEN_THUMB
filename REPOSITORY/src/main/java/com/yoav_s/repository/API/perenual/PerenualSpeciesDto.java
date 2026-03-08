package com.yoav_s.repository.API.perenual;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PerenualSpeciesDto {
    @SerializedName("id") public int id;
    @SerializedName("common_name") public String commonName;
    @SerializedName("scientific_name") public List<String> scientificName;
    @SerializedName("type") public String type;
    @SerializedName("sunlight") public List<String> sunlight;
    @SerializedName("watering") public String watering;
}