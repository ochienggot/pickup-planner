package com.nicholasgot.clientapp.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for obtaining location details
 */
public class LocationDetails {

    public static final String RESULT = "result";
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";

    public static Double getLatitude(JSONObject jsonObject)
            throws JSONException {

        return (Double)jsonObject.getJSONObject(RESULT)
                .getJSONObject(GEOMETRY)
                .getJSONObject(LOCATION)
                .get(LATITUDE);
    }

    public static Double getLongitude(JSONObject jsonObject)
            throws JSONException {

        return (Double) jsonObject.getJSONObject(RESULT)
                .getJSONObject(GEOMETRY)
                .getJSONObject(LOCATION)
                .get(LONGITUDE);
    }
}
