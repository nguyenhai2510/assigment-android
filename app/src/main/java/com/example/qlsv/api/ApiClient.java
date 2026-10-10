package com.example.qlsv.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://upbound-antonetta-undefrauded.ngrok-free.dev/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor headerInterceptor = chain -> {
                Request originalRequest = chain.request();
                Request requestWithHeaders = originalRequest.newBuilder()
                        .header("ngrok-skip-browser-warning", "true")
                        .header("Content-Type", "application/json")
                        .build();
                return chain.proceed(requestWithHeaders);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
