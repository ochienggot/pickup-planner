package com.nicholasgot.clientapp.autocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ngot on 29/06/2016.
 */
public class GeocodeJsonParser {


    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("predictions");
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
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

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

        HashMap<String, String> place = new HashMap<>();

        String id;
        String reference;
        String description;

        try {
            description = jPlace.getString(DESCRIPTION);
            id = jPlace.getString(ID);
            reference = jPlace.getString(REFERENCE);

            place.put(DESCRIPTION, description);
            place.put("_id",id);
            place.put(REFERENCE,reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
