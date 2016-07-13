package com.nicholasgot.clientapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nicholasgot.clientapp.autocomplete.GeocodeJsonParser;
import com.nicholasgot.clientapp.autocomplete.PlaceDetailsJsonParser;
import com.nicholasgot.clientapp.autocomplete.SimpleGeocodeJsonParser;
import com.nicholasgot.clientapp.utils.NetworkAndLocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TravelActivityFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    public final int MY_PERMISSIONS_ACCESS_LOCATION = 123;
    public static final String PREF_LOCATION = "pref_location";
    public static final String LOG_TAG = TravelActivity.class.getSimpleName();
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private LocationManager mLocationManager = null;

    private AutoCompleteTextView endPointTextField;
    private AutocompleteGeoLocationDownloadTask placesDownloadTask;
    private AutocompleteGeoLocationDownloadTask placeDetailsDownloadTask;
    private AutocompleteGeoLocationParserTask placesParserTask;
    private AutocompleteGeoLocationParserTask placeDetailsParserTask;

    final static int PLACES = 0;
    final static int PLACES_DETAILS = 1;

    protected static LatLng destinationPoint = null;

    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";

    public TravelActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_travel, container, false);

        endPointTextField = (AutoCompleteTextView) view
                .findViewById(R.id.pickupLocationTextField);

        endPointTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                placesDownloadTask = new AutocompleteGeoLocationDownloadTask(PLACES);
                String url = getAutoCompleteUrl(s.toString());
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        endPointTextField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                ListView lv = (ListView) parent;
                SimpleAdapter adapter = (SimpleAdapter) lv.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter
                        .getItem(position);

                placeDetailsDownloadTask = new AutocompleteGeoLocationDownloadTask(PLACES_DETAILS);
                String url = getPlaceDetailsUrl(hm.get("reference"));
                placeDetailsDownloadTask.execute(url);
            }
        });

        endPointTextField
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {

                        if (!NetworkAndLocationServices
                                .isInternetSignal(getActivity())) {
                            return false;
                        }

                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                                || (actionId == EditorInfo.IME_ACTION_DONE)) {

                            String location = endPointTextField.getText()
                                    .toString();

                            if (location == null || location.equals("")) {
                                Toast.makeText(getActivity(),
                                        "No Place is entered",
                                        Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            String url = "https://maps.googleapis.com/maps/api/geocode/json?";

                            try {
                                // encoding special characters like space in the
                                // user input
                                // place
                                location = URLEncoder.encode(location, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            String address = "address=" + location;

                            String sensor = "sensor=false";

                            url = url + address + "&" + sensor;
                            SimpleGeolocationDownloadTask simpleGeolocationDownloadTask = new SimpleGeolocationDownloadTask();
                            simpleGeolocationDownloadTask.execute(url);
                        }
                        return false;
                    }
                });

        // get location from Shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(PREF_LOCATION, null);

        // Presents a list of travel times: now, or arrive earliest
        setSpinner(view);

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

        Spinner spinnerDates = (Spinner) view.findViewById(R.id.dates_spinner);
        spinnerDates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = (String) parent.getItemAtPosition(position);
                TextView textView = (TextView) getActivity().findViewById(R.id.text_view_date);
                if (selectedDate.equals(TravelDate.SPECIFIC_DATE.getDate())) {
                    showDatePickerDialog();
                }
                else if (selectedDate.equals(TravelDate.EARLIEST_DATE.getDate())){
                    textView.setText(R.string.date_earliest_date);
                }
                else if (selectedDate.equals(TravelDate.TODAY.getDate())) {
                    textView.setText(R.string.date_today);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.dst_airport, R.layout.spinner_item);
        ArrayAdapter<CharSequence> adapterDates = ArrayAdapter.createFromResource(getActivity(),
                R.array.dates_array, R.layout.spinner_item);

//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterDates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinnerDates.setAdapter(adapterDates);
    }

    public enum TravelDate {
        SPECIFIC_DATE("I want to go on a specific date"),
        EARLIEST_DATE("I want to go at the earliest"),
        TODAY("I want to go today");

        private String chosenDate;

        TravelDate(String date) {
            chosenDate = date;
        }

        String getDate() {
            return chosenDate;
        }
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

    // Autocomplete
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));

            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private String getAutoCompleteUrl(String place) {
        String PATH = "https://maps.googleapis.com/maps/api/place/autocomplete/";
        String INPUT = "input=" + place;
        String TYPES = "types=geocode";
        String SENSOR = "sensor=false";
        String KEY = "key=";
        String OUTPUT = "json";

        // Building the parameters to the web service
        String PARAMETERS = INPUT + "&" +
                TYPES + "&" +
                SENSOR + "&" +
                KEY + BuildConfig.GOOGLE_AUTOCOMPLETE_API_KEY;

        return PATH + OUTPUT + "?" + PARAMETERS;
    }

    private String getPlaceDetailsUrl(String ref) {
        String PATH = "https://maps.googleapis.com/maps/api/place/details/";
        String REFERENCE = "reference=" + ref;
        String SENSOR = "sensor=false";
        String KEY = "key=";
        String OUTPUT = "json";

        // Building the parameters to the web service
        String PARAMETERS = REFERENCE + "&" +
                SENSOR + "&" +
                KEY + BuildConfig.GOOGLE_MAPS_API_KEY;

        return PATH + OUTPUT + "?" + PARAMETERS;
    }

    /** A class, to download Places from Geocoding webservice */
    private class AutocompleteGeoLocationDownloadTask extends
            AsyncTask<String, Void, String> {

        private int downloadType = 0;

        public AutocompleteGeoLocationDownloadTask(int type) {
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            switch (downloadType) {
                case PLACES:
                    placesParserTask = new AutocompleteGeoLocationParserTask(PLACES);

                    if (!result.equals(""))
                        placesParserTask.execute(result);
                    else
                        System.out.println("The result is empty");
                    break;

                case PLACES_DETAILS:
                    placeDetailsParserTask = new AutocompleteGeoLocationParserTask(PLACES_DETAILS);
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    /** A class to parse the Geocoding Places in non-ui thread */
    private class AutocompleteGeoLocationParserTask extends
            AsyncTask<String, Integer, List<HashMap<String, String>>> {

        int parserType = 0;

        public AutocompleteGeoLocationParserTask(int type) {
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try {
                jObject = new JSONObject(jsonData[0]);

                switch (parserType) {
                    case PLACES:
                        GeocodeJsonParser placeJsonParser = new GeocodeJsonParser();
                        list = placeJsonParser.parse(jObject);
                        break;
                    case PLACES_DETAILS:
                        PlaceDetailsJsonParser placeDetailsJsonParser = new PlaceDetailsJsonParser();
                        list = placeDetailsJsonParser.parse(jObject);
                }

            } catch (Exception e) {
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            switch (parserType) {
                case PLACES:
                    String[] from = new String[] { "description" };
                    int[] to = new int[] { android.R.id.text1 };

                    SimpleAdapter adapter = new SimpleAdapter(getActivity()
                            .getBaseContext(), result,
                            android.R.layout.simple_list_item_1, from, to);

                    endPointTextField.setAdapter(adapter);
                    break;

                case PLACES_DETAILS:
                    HashMap<String, String> hm = result.get(0);

                    double endPointLatitude = Double.parseDouble(hm.get(LATITUDE));
                    double endPointLongitude = Double.parseDouble(hm.get(LONGITUDE));

                    destinationPoint = new LatLng(endPointLatitude,
                            endPointLongitude);
                    // TODO: Call database and write these details
//                    displayMarkerAtLocationAndZoomIn(destinationPoint);

                    InputMethodManager inputManager = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity()
                                    .getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
            }
        }
    }

    /** A class to parse the Geocoding Places in non-ui thread */
    class SimpleGeolocationParserTask extends
            AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;
        List<String> locations;
        List<HashMap<String, String>> responseList;

        @Override
        protected List<HashMap<String, String>> doInBackground(
                String... jsonData) {

            List<HashMap<String, String>> places = null;
            SimpleGeocodeJsonParser parser = new SimpleGeocodeJsonParser();

            try {
                jObject = new JSONObject(jsonData[0]);
                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);

            } catch (Exception e) {
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            responseList = list;
            if (responseList != null) {
                locations = new ArrayList<>();
                for (HashMap<String, String> item : responseList) {
                    locations.add(item.get("formatted_address"));
                }

                if (locations.size() == 0) {
                    Toast.makeText(getActivity(),
                            "No result found. Please make another selection",
                            Toast.LENGTH_SHORT).show();

                } else if (locations.size() == 1) {

                    char[] location = locations.get(0).toCharArray();

                    endPointTextField.setText(location, 0, location.length);

                    double endPointLatitude = Double.parseDouble(responseList
                            .get(0).get(LATITUDE));
                    double endPointLongitude = Double.parseDouble(responseList
                            .get(0).get(LONGITUDE));

                    destinationPoint = new LatLng(endPointLatitude, endPointLongitude);
//                    displayMarkerAtLocationAndZoomIn(destinationPoint);

                } else {

                    CharSequence locationsCharSequence[] = locations
                            .toArray(new CharSequence[locations.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            getActivity());
                    builder.setTitle("Pick a location");
                    builder.setItems(locationsCharSequence,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    char[] location = locations.get(which)
                                            .toCharArray();

                                    endPointTextField.setText(location, 0,
                                            location.length);

                                    double endPointLatitude = Double
                                            .parseDouble(responseList
                                                    .get(which).get(LATITUDE));

                                    double endPointLongitude = Double
                                            .parseDouble(responseList
                                                    .get(which).get(LONGITUDE));

                                    destinationPoint = new LatLng(endPointLatitude, endPointLongitude);
//                                    displayMarkerAtLocationAndZoomIn(destinationPoint);

                                }
                            });
                    builder.show();
                }
            } else {
                Toast.makeText(getActivity(),
                    "Unable to suggest any location. Check internet connection.",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** A class, to download Places from Geocoding webservice */
    private class SimpleGeolocationDownloadTask extends
            AsyncTask<String, Integer, String> {

        String data = null;

        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                // TODO: change error handling
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            SimpleGeolocationParserTask parserTask = new SimpleGeolocationParserTask();
            parserTask.execute(result);
        }
    }
}
