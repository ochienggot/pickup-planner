package com.nicholasgot.clientapp.utils;

import com.nicholasgot.clientapp.TravelActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Geocodes addresses into lat/lon pairs by querying Google Geocoding API
 */
public class GeocodeLocationTask implements Callback {
    public final String LOG_TAG = GeocodeLocationTask.class.getSimpleName();

    private String location;

    public GeocodeLocationTask(String location) {
        this.location = location;
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        String mResponseData;
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code: " + response);
        }
        else {
            mResponseData = response.body().string();
            TravelActivity.getLocationFromJson(location, mResponseData);
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    /**
     * Background task to geocode address stored in SharedPreferences
     */
    public void doInBackground() {
        final String API_KEY = "key";
        final String MAP = "maps";
        final String API = "api";
        final String GEOCODE = "geocode";
        final String JSON = "json";
        final String ADDRESS = "address";
        final String GOOGLE_MAPS_URL = "https://maps.googleapis.com";

        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_MAPS_URL).newBuilder();
        urlBuilder.addPathSegment(MAP)
                .addPathSegment(API)
                .addPathSegment(GEOCODE)
                .addPathSegment(JSON)
                .addQueryParameter(ADDRESS, location)
                .addQueryParameter(API_KEY, "AIzaSyBAIZEm2AXkw4Wv5P4y5QzMxv9rFlX7i0Y");

        String okUrl = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(okUrl)
                .build();

        httpClient.newCall(request).enqueue(this);
    }
}