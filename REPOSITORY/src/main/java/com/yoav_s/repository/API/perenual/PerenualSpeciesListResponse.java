package com.yoav_s.repository.API.perenual;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PerenualSpeciesListResponse {
    @SerializedName("data")
    public List<PerenualSpeciesDto> data;

    @SerializedName("current_page") public Integer currentPage;
    @SerializedName("last_page") public Integer lastPage;
    @SerializedName("per_page") public Integer perPage;
    @SerializedName("total") public Integer total;
}
