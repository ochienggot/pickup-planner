package com.nicholasgot.clientapp;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TravelActivity extends AppCompatActivity {
    public static final String LOG_TAG = TravelActivity.class.getSimpleName();
    public final int MY_PERMISSIONS_ACCESS_LOCATION = 123;

    public final String MY_LOCATION = "Use my location";
    public final String DEFAULT_LOCATION = "Uppsala";

    private String sourceLocation;
    private String destLocation;

    private EditText mEditText;
    private Spinner mSpinner;
    private GeocodeLocationTask geoCode;

    private static Map<String, LatLng> locations = new HashMap<>();

    private Button mDestinationAirport;
    private Button mDestinationShopping;
    private Button mDestinationLibrary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEditText = (EditText) findViewById(R.id.user_location_edit_text);
        mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(getApplicationContext(), "Entered pickup location", Toast.LENGTH_SHORT).show();

                    String locationPref = mEditText.getText().toString(); // Heed compiler warnings always!
                    if (locationPref.equals(MY_LOCATION)) sourceLocation = DEFAULT_LOCATION;
                    else sourceLocation = locationPref;
                    geoCodeLocation(sourceLocation);

                    return true;
                }
                return false;
            }
        });

        mSpinner = (Spinner) findViewById(R.id.events_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                if (item.equals("Shopping")) {
                    Toast.makeText(getApplicationContext(), "Spinner item selected", Toast.LENGTH_SHORT).show();
                }

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

        // Initialize to airports option
        Spinner spinner = (Spinner) findViewById(R.id.events_spinner);
        updateSpinnerAdapter(spinner, R.array.dst_airport);
        RadioButton buttonAirport = (RadioButton) findViewById(R.id.radio_airport);
        buttonAirport.toggle();
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
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Present a dialog to the user to enable them pick the date of travel
     * @param view
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
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject obj = jsonArray.getJSONObject(0);
            JSONObject jsonGeometry = obj.getJSONObject("geometry");
            JSONObject jsonLocation = jsonGeometry.getJSONObject("location");

            String lat = jsonLocation.getString("lat");
            String lon = jsonLocation.getString("lng");

            LatLng loc1 = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            locations.put(loc, loc1);

        } catch (JSONException je) {
            Log.v(LOG_TAG, "Json Exception " + je);
        }
    }

    public void geoCodeLocation(String loc) {
        geoCode = new GeocodeLocationTask(loc);
        geoCode.doInBackground();
    }

    /**
     * Write travel request to database
     */
    public void sendRequestToDatabase(View view) {
        if (locations != null) {
            postRequestToDatabase();
        }
        else {
            Log.e(LOG_TAG, "Null location from geocode.");
        }
    }

    /**
     * Send to the database
     */
    private void postRequestToDatabase() {
        DatabaseConnection db = new DatabaseConnection();
        LatLng src = locations.get(sourceLocation);
        LatLng dst = locations.get(destLocation);

        if (src == null || dst == null) {
            Toast.makeText(this, "Source and destination must not be null", Toast.LENGTH_SHORT).show();
            return;
        }

        String source = "(" + src.latitude + "," + src.longitude + ")";
        String destination = "(" + dst.latitude + "," + dst.longitude + ")";
        db.postLocation(source, destination);

        Toast.makeText(this, "Request sent. You will receive a notification on your travel details.", Toast.LENGTH_LONG).show();
    }
}
