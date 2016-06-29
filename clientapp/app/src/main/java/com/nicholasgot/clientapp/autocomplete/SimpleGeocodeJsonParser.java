package com.nicholasgot.clientapp.autocomplete;

import com.nicholasgot.clientapp.utils.LocationDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Parse Json object TODO: better documentation
 */
public class SimpleGeocodeJsonParser {

    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> place;

        /** Taking each place, parses and adds to list object */
        for(int i = 0; i<placesCount;i++){
            try {
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlace(JSONObject jPlace){

        String FORMATTED_ADDRESS = "formatted_address";
        String GEOMETRY = "geometry";
        String LOCATION = "location";

        HashMap<String, String> place = new HashMap<>();
        String formatted_address = "-NA-";
        String lat;
        String lng;

        try {
            // Extracting formatted address, if available
            if(!jPlace.isNull(FORMATTED_ADDRESS)){
                formatted_address = jPlace.getString(FORMATTED_ADDRESS);
            }

            lat = jPlace.getJSONObject(GEOMETRY)
                    .getJSONObject(LOCATION)
                    .getString(LocationDetails.LATITUDE);
            lng = jPlace.getJSONObject(GEOMETRY)
                    .getJSONObject(LOCATION)
                    .getString(LocationDetails.LONGITUDE);

            place.put(FORMATTED_ADDRESS, formatted_address);
            place.put(LocationDetails.LATITUDE, lat);
            place.put(LocationDetails.LONGITUDE, lng);

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
