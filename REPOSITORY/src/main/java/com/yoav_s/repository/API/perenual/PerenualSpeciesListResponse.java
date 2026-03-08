package com.yoav_s.repository.API.perenual;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PerenualSpeciesListResponse {
    @SerializedName("data")
    public List<PerenualSpeciesDto> data;
}
