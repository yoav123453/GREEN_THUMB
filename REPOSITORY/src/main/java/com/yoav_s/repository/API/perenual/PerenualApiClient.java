package com.yoav_s.repository.API.perenual;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerenualApiClient {

    private static final String BASE_URL = "https://perenual.com/";
    private static PerenualApiService service;

    public static PerenualApiService getService() {
        if (service == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(PerenualApiService.class);
        }
        return service;
    }
}
