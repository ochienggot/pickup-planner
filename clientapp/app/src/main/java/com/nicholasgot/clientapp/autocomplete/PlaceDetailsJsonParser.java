package com.nicholasgot.clientapp.autocomplete;

import android.util.Log;

import com.nicholasgot.clientapp.utils.LocationDetails;

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

    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        Log.v(LOG_TAG, "Json object: " + jObject);

        Double lat = 0.0;
        Double lng = 0.0;

        HashMap<String, String> hm = new HashMap<>();
        List<HashMap<String, String>> list = new ArrayList<>();

        try {
            lat = LocationDetails.getLatitude(jObject);
            lng = LocationDetails.getLongitude(jObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        hm.put(LocationDetails.LATITUDE, Double.toString(lat));
        hm.put(LocationDetails.LONGITUDE, Double.toString(lng));
        list.add(hm);

        return list;
    }
}
