package com.nicholasgot.clientapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class TravelActivityFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    public final int MY_PERMISSIONS_ACCESS_LOCATION = 123;
    public static final String PREF_LOCATION = "pref_location";
    public static final String LOG_TAG = TravelActivity.class.getSimpleName();
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private LocationManager mLocationManager = null;

    public TravelActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get location from Shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(PREF_LOCATION, null);

        // Geocode location from preference
        //GeocodeLocationTask geocode = new GeocodeLocationTask();
        //geocode.doInBackground();

        // Presents a list of travel times: now, or arrive earliest
        View view = inflater.inflate(R.layout.fragment_travel, container, false);
        setSpinner(view);

        // DOING: handle selection of airport and library destinations

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();

        return view;
    }

    /**
     * Presents the options in a spinner view
     * @param view the root view
     */
    protected void setSpinner(View view) {
        Spinner spinner = (Spinner) view.findViewById(R.id.events_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                if (item.equals("Shopping")) {
                    Toast.makeText(getContext(), "Spinner item selected", Toast.LENGTH_SHORT).show();
                }
                // Do something when item is selected
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do something
            }
        });

        final String SPECIFIC_DATE = "I want to go on a specific date";
        Spinner spinnerDates = (Spinner) view.findViewById(R.id.dates_spinner);
        spinnerDates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = (String) parent.getItemAtPosition(position);
                if (selectedDate.equals(SPECIFIC_DATE)) {
                    Toast.makeText(getContext(), "Call Date chooser", Toast.LENGTH_SHORT).show();
                    // Present option to choose date
                    showDatePickerDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.dst_airport, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterDates = ArrayAdapter.createFromResource(getActivity(),
                R.array.dates_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterDates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinnerDates.setAdapter(adapterDates);
    }

    /**
     * Present a dialog to the user to enable them pick the date of travel
     */
    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getActivity().getFragmentManager(), "datePicker");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //ActivityCompat.requestPermissions(getActivity(),
         //       new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          //      MY_PERMISSIONS_ACCESS_LOCATION);
        try {
            mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.v(LOG_TAG, "Last location: " + mLastLocation);
        } catch (SecurityException se) {
            Log.e(LOG_TAG, "SE " + se);
        }
    }

    private void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Log.v(LOG_TAG, "Permission not granted");

        ActivityCompat.requestPermissions(activity,
                permissions,
                requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int resultCode, String permissions[], int[] grantResults) {
        Log.e(LOG_TAG, "here");
        switch (resultCode) {
            case MY_PERMISSIONS_ACCESS_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: perm granted. This does not fall through
                }
            default:
                Log.e(LOG_TAG, "Default case");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getConnectionResult() = " +
                connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
}
