package com.nicholasgot.citypulse.androidapp.common;

import com.google.android.gms.maps.model.LatLng;
import com.nicholasgot.citypulse.androidapp.TravelPlannerActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodeLocationTask implements Callback {
    public final String LOG_TAG = GeocodeLocationTask.class.getSimpleName();

    private String mResponseData;

    private LatLng location;

    public GeocodeLocationTask(LatLng location) {
        this.location = location;
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code: " + response);
        } else {
            mResponseData = response.body().string();
            TravelPlannerActivity.geocodePickupPoints(location, mResponseData);
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    /**
     * Background task to geocode
     */
    public void doInBackground() {
        String API_KEY = "key";

        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://maps.googleapis.com").newBuilder();
        urlBuilder.addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("geocode")
                .addPathSegment("json")
                .addQueryParameter("latlng", location.latitude + "," + location.longitude)
                .addQueryParameter(API_KEY, "AIzaSyDJM5AGR7UchF-LuIPcwFtIWLoSgMA2NKE");

        String okUrl = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(okUrl)
                .build();

        httpClient.newCall(request).enqueue(this);
    }
}

