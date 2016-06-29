package com.nicholasgot.clientapp.autocomplete;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Parse Json result into Latitude and Longitude
 */
public class PlaceDetailsJsonParser {

    public static final String LOG_TAG = PlaceDetailsJsonParser.class.getSimpleName();

    public static final String RESULT = "result";
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";

    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){
        Log.v(LOG_TAG, "Json object: " + jObject);

        Double lat = Double.valueOf(0);
        Double lng = Double.valueOf(0);

        HashMap<String, String> hm = new HashMap<>();
        List<HashMap<String, String>> list = new ArrayList<>();

        try {
            lat = getLatitude(jObject);
            lng = getLongitude(jObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        hm.put(LATITUDE, Double.toString(lat));
        hm.put(LONGITUDE, Double.toString(lng));
        list.add(hm);

        return list;
    }

    private Double getLatitude(JSONObject jsonObject) throws JSONException {

        return (Double)jsonObject.getJSONObject(RESULT)
                .getJSONObject(GEOMETRY)
                .getJSONObject(LOCATION)
                .get(LATITUDE);
    }

    private Double getLongitude(JSONObject jsonObject) throws JSONException {

        return (Double) jsonObject.getJSONObject(RESULT)
                .getJSONObject(GEOMETRY)
                .getJSONObject(LOCATION)
                .get(LONGITUDE);
    }
}
