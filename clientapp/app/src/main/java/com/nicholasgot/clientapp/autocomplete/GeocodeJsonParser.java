package com.nicholasgot.clientapp.autocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Parse Json String
 */
public class GeocodeJsonParser {


    /**
     * Converts JSON object to list
     * @param jObject JSON object
     * @return list
     */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        String PREDICTIONS = "predictions";
        JSONArray jPlaces = null;

        try {
            jPlaces = jObject.getJSONArray(PREDICTIONS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){

        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> place;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
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

        String DESCRIPTION = "description";
        String REFERENCE = "reference";
        String ID = "id";
        String _ID = "_id";

        HashMap<String, String> place = new HashMap<>();

        String id;
        String reference;
        String description;

        try {
            description = jPlace.getString(DESCRIPTION);
            id = jPlace.getString(ID);
            reference = jPlace.getString(REFERENCE);

            place.put(DESCRIPTION, description);
            place.put(_ID,id);
            place.put(REFERENCE,reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
