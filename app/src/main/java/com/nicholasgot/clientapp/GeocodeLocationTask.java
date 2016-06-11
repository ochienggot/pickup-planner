package com.nicholasgot.clientapp;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private String mResponseData;
    private LatLng mCodedLocation;

    public GeocodeLocationTask(String location) {
        this.location = location;
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
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
        String API_KEY = "key";

        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://maps.googleapis.com").newBuilder();
        urlBuilder.addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("geocode")
                .addPathSegment("json")
                .addQueryParameter("address", location)
                .addQueryParameter(API_KEY, "AIzaSyBAIZEm2AXkw4Wv5P4y5QzMxv9rFlX7i0Y");

        String okUrl = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(okUrl)
                .build();

        httpClient.newCall(request).enqueue(this);
    }

    /**
     * Uses JSON response data to obtain the location latitude/longitude
     * @param responseData response data from Google Geocoding API
     */
    protected void getLocationFromJson(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject obj = jsonArray.getJSONObject(0);
            JSONObject jsonGeometry = obj.getJSONObject("geometry");
            JSONObject jsonLocation = jsonGeometry.getJSONObject("location");

            String lat = jsonLocation.getString("lat");
            String lon = jsonLocation.getString("lng");
            mCodedLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

        } catch (JSONException je) {
            Log.v(LOG_TAG, "Json Exception " + je);
        }
    }

    @Override
    public String toString() {
        return "Lat/Lng: " + mCodedLocation.latitude + "/" + mCodedLocation.longitude;
    }
}