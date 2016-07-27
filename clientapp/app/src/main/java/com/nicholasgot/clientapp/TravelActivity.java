package com.nicholasgot.clientapp;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.nicholasgot.clientapp.utils.DatabaseConnection;
import com.nicholasgot.clientapp.utils.GeocodeLocationTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Travel Details Activity
 */
public class TravelActivity extends AppCompatActivity {
    public static final String LOG_TAG = TravelActivity.class.getSimpleName();
    public final int MY_PERMISSIONS_ACCESS_LOCATION = 123;

    private String destLocation;

    private Spinner mSpinner;
    private GeocodeLocationTask geoCode;

    private static Map<String, LatLng> locations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: program in terms of the problem domain, not the low-level implementation detail
//        mEditText = (EditText) findViewById(R.id.user_location_edit_text);
//        mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    Toast.makeText(getApplicationContext(), "Pickup location entered.", Toast.LENGTH_SHORT).show();
//
//                    String locationPref = mEditText.getText().toString(); // Heed compiler warnings always!
//                    if (locationPref.equals(MY_LOCATION)) sourceLocation = DEFAULT_LOCATION;
//                    else sourceLocation = locationPref;
//                    geoCodeLocation(sourceLocation);
//
//                    return true;
//                }
//                else {
//                    geoCodeLocation(DEFAULT_LOCATION);
//                    return true;
//                }
//            }
//        });

        mSpinner = (Spinner) findViewById(R.id.events_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                destLocation = (String) mSpinner.getSelectedItem();
                geoCode = new GeocodeLocationTask(destLocation);
                geoCode.doInBackground();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do something
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Request sent; Your bus comes in x minutes", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_ACCESS_LOCATION);

    }

    @Override
    public void onResume() {
        super.onResume();

        // Initialize destination to airports option
        Spinner spinner = (Spinner) findViewById(R.id.events_spinner);
        updateSpinnerAdapter(spinner, R.array.dst_airport);
        RadioButton buttonAirport = (RadioButton) findViewById(R.id.radio_airport);
        if (buttonAirport != null) {
            buttonAirport.toggle();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the spinner view items depending the user selection
     * @param view chosen button
     */
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        Spinner spinner = (Spinner) findViewById(R.id.events_spinner);

        switch (view.getId()) {
            case R.id.radio_airport:
                if (checked) {
                    updateSpinnerAdapter(spinner, R.array.dst_airport);
                }
                break;
            case R.id.radio_library:
                if (checked) {
                    updateSpinnerAdapter(spinner, R.array.dst_library);
                }
                break;
            case R.id.radio_shopping_mall:
                if (checked) {
                    updateSpinnerAdapter(spinner, R.array.dst_shopping_mall);
                }
                break;
        }
    }

    /**
     * Update the spinner adapter depending on the chosen radio button
     * @param spinner Spinner
     * @param id: view id
     */
    protected void updateSpinnerAdapter(Spinner spinner, int id) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                id,
                R.layout.spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Present a dialog to the user to enable them pick the date of travel
     * @param view view
     */
    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Calls fragment defined to allow the user to pick the time
     */
    public void showTimePickerDialog(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getFragmentManager(), "timePicker");

    }

    /**
     * Uses JSON response data to obtain the location latitude/longitude
     * @param responseData response data from Google Geocoding API
     */
    public static void getLocationFromJson(String loc, String responseData) {
        final String RESULTS = "results";
        final String GEOMETRY = "geometry";
        final String LOCATION = "location";
        final String LATITUDE = "lat";
        final String LONGITUDE = "lng";

        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray(RESULTS);
            JSONObject obj = jsonArray.getJSONObject(0);
            JSONObject jsonGeometry = obj.getJSONObject(GEOMETRY);
            JSONObject jsonLocation = jsonGeometry.getJSONObject(LOCATION);

            String lat = jsonLocation.getString(LATITUDE);
            String lon = jsonLocation.getString(LONGITUDE);

            LatLng loc1 = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            locations.put(loc, loc1);

        } catch (JSONException je) {
            Log.v(LOG_TAG, "Json Exception: " + je);
        }
    }

    /**
     * Write travel request to database
     */
    public void sendRequestToDatabase(View view) {
        if (locations != null) {
            postRequestToDatabase();
        }
        else {
            Log.e(LOG_TAG, "Null location from Geocoding API.");
        }
    }

    /**
     * Send to the database
     */
    protected void postRequestToDatabase() {
        DatabaseConnection db = new DatabaseConnection(this);
//        LatLng src = locations.get(sourceLocation);
        LatLng dst = locations.get(destLocation);
        LatLng src = TravelActivityFragment.destinationPoint;

        if (src == null || dst == null) {
            Toast.makeText(this, "Error in Geocoding Pickup/destination location.", Toast.LENGTH_LONG).show();
            return;
        }

        String source = "(" + src.latitude + "," + src.longitude + ")";
        String destination = "(" + dst.latitude + "," + dst.longitude + ")";
        db.postLocation(source, destination);
    }
}
