package com.yoav_s.repository.API.perenual;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PerenualApiService {

    @GET("/api/v2/species-list")
    Call<PerenualSpeciesListResponse> speciesList(
            @Query("key") String apiKey,
            @Query("page") int page,
            @Query("q") String query
    );

    @GET("/api/v2/species/details/{id}")
    Call<PerenualSpeciesDetailsDto> speciesDetails(
            @Path("id") int id,
            @Query("key") String apiKey
    );
}
