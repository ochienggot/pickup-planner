package com.nicholasgot.citypulse.androidapp;

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

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import citypulse.commons.data.Coordinate;
import citypulse.commons.reasoning_request.ARType;
import citypulse.commons.reasoning_request.ReasoningRequest;
import citypulse.commons.reasoning_request.User;
import citypulse.commons.reasoning_request.concrete.FunctionalConstraintValueAdapter;
import citypulse.commons.reasoning_request.concrete.FunctionalParameterValueAdapter;
import citypulse.commons.reasoning_request.concrete.IntegerFunctionalConstraintValue;
import citypulse.commons.reasoning_request.concrete.StringFunctionalParameterValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraint;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintName;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintOperator;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraints;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalDetails;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameter;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameterName;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameterValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameters;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalPreference;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalPreferenceOperation;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalPreferences;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nicholasgot.citypulse.androidapp.autocompletetext.GeocodeJSONParser;
import com.nicholasgot.citypulse.androidapp.autocompletetext.PlaceDetailsJSONParser;
import com.nicholasgot.citypulse.androidapp.autocompletetext.SimpleGeocodeJSONParser;
import com.nicholasgot.citypulse.androidapp.common.ApplicationExecutionConditions;
import com.nicholasgot.citypulse.androidapp.common.DefaultValues;
import com.nicholasgot.citypulse.webSockets.WebSocketBasicClient;

public class ParkingPlacePlannerActivity extends Fragment implements
		LocationListener {

	private String parkingReasoningRequest = "";
	private AutoCompleteTextView poiTextField;
	private SupportMapFragment parkingPlaceSupportMapFragment;
	private GoogleMap map;

	AutocompleteGeoLocationDownloadTask placesDownloadTask;
	AutocompleteGeoLocationDownloadTask placeDetailsDownloadTask;
	AutocompleteGeoLocationParserTask placesParserTask;
	AutocompleteGeoLocationParserTask placeDetailsParserTask;

	final static int PLACES = 0;
	final static int PLACES_DETAILS = 1;

	private LatLng pointOfInterestLocation;
	private Location lastLocation;
	private boolean locationAvailable;
	private Marker userPositionMarker;
	private LocationManager locationManager;
	private ParkingPlacePlannerActivity thisActivity = this;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View parkingPlacePlannerView = inflater.inflate(
				R.layout.parking_place_planner_frag, container, false);

		Button parkingPlacePlannerConstraintButton = (Button) parkingPlacePlannerView
				.findViewById(R.id.parkingPlaceconstraintsButton);
		Button parkingPlcacePlannerOKButton = (Button) parkingPlacePlannerView
				.findViewById(R.id.parkingPlacePlannerOKButton);

		poiTextField = (AutoCompleteTextView) parkingPlacePlannerView
				.findViewById(R.id.parkingPlaceSelectionTextField);

		poiTextField.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				placesDownloadTask = new AutocompleteGeoLocationDownloadTask(
						PLACES);

				// Getting url to the Google Places Autocomplete api
				String url = getAutoCompleteUrl(s.toString());

				// Start downloading Google Places
				// This causes to execute doInBackground() of DownloadTask class
				placesDownloadTask.execute(url);

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		poiTextField.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ListView lv = (ListView) parent;
				SimpleAdapter adapter = (SimpleAdapter) lv.getAdapter();

				HashMap<String, String> hm = (HashMap<String, String>) adapter
						.getItem(position);

				// Creating a DownloadTask to download Places details of the
				// selected place
				placeDetailsDownloadTask = new AutocompleteGeoLocationDownloadTask(
						PLACES_DETAILS);

				// Getting url to the Google Places details api
				String url = getPlaceDetailsUrl(hm.get("reference"));

				// Start downloading Google Place Details
				// This causes to execute doInBackground() of DownloadTask class
				placeDetailsDownloadTask.execute(url);

			}
		});

		poiTextField.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {

				if (!ApplicationExecutionConditions
						.isInternetSignal(getActivity())) {
					return false;
				}

				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {

					String location = poiTextField.getText().toString();

					if (location == null || location.equals("")) {
						Toast.makeText(getActivity(), "No Place is entered",
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

					// url , from where the geocoding data is fetched
					url = url + address + "&" + sensor;

					SimpleGeolocationDownloadTask simpleGeolocationDownloadTask = new SimpleGeolocationDownloadTask();

					simpleGeolocationDownloadTask.execute(url);

				}
				return false;
			}
		});

		parkingPlcacePlannerOKButton
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						if (!ApplicationExecutionConditions
								.isGPSandInternetSignal(getActivity())) {
							return;
						}

						if (pointOfInterestLocation == null) {
							Toast.makeText(getActivity(),
									"Please select a point of interest.",
									Toast.LENGTH_SHORT).show();
							return;
						}

						if (lastLocation == null) {
							Toast.makeText(getActivity(),
									"There is no GPS coverage.",
									Toast.LENGTH_SHORT).show();
							return;
						}

						SendMessageToServer sendMessageTask = new SendMessageToServer();
						sendMessageTask.execute();
						
						Toast.makeText(getActivity(),
								"Please wait until the system determines the best parking spots.",
								Toast.LENGTH_LONG).show();

					}
				});

		parkingPlacePlannerConstraintButton
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						Intent intent = new Intent(getActivity(),
								ParkingPlaceConstraintsActivity.class);
						startActivityForResult(
								intent,
								DefaultValues.PARKING_PLACE_PLANNER_CONSTRAINTS_REQUEST_CODE);

					}
				});

		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 10, this);

		return parkingPlacePlannerView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		parkingPlaceSupportMapFragment = (SupportMapFragment) fm
				.findFragmentById(R.id.ParkingPlacePlannerMap);
		if (parkingPlaceSupportMapFragment == null) {
			parkingPlaceSupportMapFragment = SupportMapFragment.newInstance();
			fm.beginTransaction()
					.replace(R.id.ParkingPlacePlannerMap,
							parkingPlaceSupportMapFragment).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		locationAvailable = false;

		if (map == null) {
			map = parkingPlaceSupportMapFragment.getMap();

		}
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {

	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {

	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;

		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());

		if (locationAvailable == false) {

			map.clear();

			userPositionMarker = map.addMarker(new MarkerOptions()
					.position(latLng)
					.title("my position")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.user_position_marker)));

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

			if (pointOfInterestLocation != null) {

				map.addMarker(new MarkerOptions()
						.title("Point of interest")
						.position(pointOfInterestLocation)
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

			}

		} else {
			userPositionMarker.setPosition(latLng);
		}

		locationAvailable = true;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	private String generateParkingRequest() {

		// create the request object
		FunctionalParameters requestFunctionalParameters = new FunctionalParameters();

		Coordinate poiCoordinate = new Coordinate(
				pointOfInterestLocation.longitude,
				pointOfInterestLocation.latitude);

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.POINT_OF_INTEREST,
						new StringFunctionalParameterValue(poiCoordinate
								.toString())));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.STARTING_POINT,
						new StringFunctionalParameterValue(new Coordinate(
								lastLocation.getLongitude(), lastLocation
										.getLatitude()).toString())));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.STARTING_DATETIME,
						new StringFunctionalParameterValue(new Long(System
								.currentTimeMillis()).toString())));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.DISTANCE_RANGE,
						new StringFunctionalParameterValue("1000")));

		requestFunctionalParameters
				.addFunctionalParameter(new FunctionalParameter(
						FunctionalParameterName.TIME_OF_STAY,
						new StringFunctionalParameterValue("100")));

		FunctionalConstraints requestFunctionalConstraints = new FunctionalConstraints();

		requestFunctionalConstraints
				.addFunctionalConstraint(new FunctionalConstraint(
						FunctionalConstraintName.COST,
						FunctionalConstraintOperator.LESS_THAN,
						new IntegerFunctionalConstraintValue(100)));

		FunctionalPreferences requestFunctionalPreferences = new FunctionalPreferences();

		SharedPreferences parkingPlaceConstraintActivityPreferences = getActivity()
				.getSharedPreferences(
						"ParkingPlaceConstraintActivityPreferences",
						getActivity().MODE_PRIVATE);

		boolean cheapestRestoredCheckBox = parkingPlaceConstraintActivityPreferences
				.getBoolean("cheapestCheckBox", false);
		boolean shortestWalkRestoredCheckBox = parkingPlaceConstraintActivityPreferences
				.getBoolean("shortestWalkCheckBox", false);

		if (cheapestRestoredCheckBox)
			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(1,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.COST));

		if (shortestWalkRestoredCheckBox)
			requestFunctionalPreferences
					.addFunctionalPreference(new FunctionalPreference(2,
							FunctionalPreferenceOperation.MINIMIZE,
							FunctionalConstraintName.DISTANCE));

		ReasoningRequest reasoningRequest = new ReasoningRequest(new User(),
				ARType.PARKING_SPACES, new FunctionalDetails(
						requestFunctionalParameters,
						requestFunctionalConstraints,
						requestFunctionalPreferences));

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(FunctionalConstraintValue.class,
				new FunctionalConstraintValueAdapter());
		builder.registerTypeAdapter(FunctionalParameterValue.class,
				new FunctionalParameterValueAdapter());
		Gson gson = builder.create();

		return gson.toJson(reasoningRequest);

	}

	private class SendMessageToServer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			parkingReasoningRequest = generateParkingRequest();

			SharedPreferences settingsPreferences = getActivity()
					.getSharedPreferences("SettingsPreferences",
							Context.MODE_PRIVATE);

			String requestEndpoint = DefaultValues.getEndPointForIP(
					DefaultValues.REASONING_REQUEST_WEB_SOCKET_END_POINT,
					settingsPreferences.getString("serverLocation",
							DefaultValues.WEB_SOCKET_SERVER_IP));

			System.out
					.println("Parking module: the following request was sent to the decision support ( "
							+ requestEndpoint + " )" + parkingReasoningRequest);

			WebSocketBasicClient webSocketBasicClient = new WebSocketBasicClient(
					requestEndpoint, parkingReasoningRequest);

			String parkingReasoningResponse = webSocketBasicClient
					.sendWebsocketRequest();

			System.out
					.println("Parking module: the following respose was received from the decision support "
							+ parkingReasoningResponse);

			if (parkingReasoningResponse == null) {
				System.out
						.println("The parkingReasoningResponse message is null");

				Intent intent = new Intent(getActivity(), ErrorPanel.class);

				intent.putExtra(
						"Error",
						"The parking recomendation message is null. Please check if "
								+ "the IP of the CityPulse framework is correct (in Settings tab) and if "
								+ "the decisions support component is running. The current decision "
								+ "support endpoint is " + requestEndpoint);
				startActivity(intent);

			} else if (parkingReasoningResponse.equals("{\"answers\":[]}")) {
				System.out
						.println("The decision support provided an empty answer.");

				Intent intent = new Intent(getActivity(), ErrorPanel.class);

				intent.putExtra(
						"Error",
						"The decision support component is working but is not able to provide a recomendation. The current decision "
								+ "support endpoint is " + requestEndpoint);
				startActivity(intent);
			} else {

				Intent intent = new Intent(getActivity(),
						ParkingPlaceSelectionActivity.class);

				Bundle bundle = new Bundle();
				bundle.putString(
						Execution.DECISION_SUPPORT_PARKING_PLANNER_RESONSE,
						parkingReasoningResponse);
				bundle.putString(
						Execution.DECISION_SUPPORT_PARKING_PLANNER_REQUEST,
						parkingReasoningRequest);

				bundle.putString(Execution.STARTING_POINT, new Gson()
						.toJson(new LatLng(lastLocation.getLatitude(),
								lastLocation.getLongitude())));
				bundle.putString(Execution.INTEREST_POINT,
						new Gson().toJson(pointOfInterestLocation));
				intent.putExtra(Execution.EXECUTION_DETAILS, bundle);
				startActivity(intent);

			}
			System.out.println("ready");

			return null;
		}
	}

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

			String line = "";
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

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key=AIzaSyBZnohTsdOvK3WEuS3C549ml43v48bG5YI";

		// place to be be searched
		String input = "input=" + place;

		// place type to be searched
		String types = "types=geocode";

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = input + "&" + types + "&" + sensor + "&" + key;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"
				+ output + "?" + parameters;

		return url;
	}

	private String getPlaceDetailsUrl(String ref) {

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key=AIzaSyBZnohTsdOvK3WEuS3C549ml43v48bG5YI";

		// reference of place
		String reference = "reference=" + ref;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = reference + "&" + sensor + "&" + key;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/details/"
				+ output + "?" + parameters;

		return url;
	}

	/** A class, to download Places from Geocoding webservice */
	private class AutocompleteGeoLocationDownloadTask extends
			AsyncTask<String, Void, String> {

		private int downloadType = 0;

		// Constructor
		public AutocompleteGeoLocationDownloadTask(int type) {
			this.downloadType = type;
		}

		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
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
				// Creating ParserTask for parsing Google Places
				placesParserTask = new AutocompleteGeoLocationParserTask(PLACES);

				// Start parsing google places json data
				// This causes to execute doInBackground() of ParserTask class
				if (result != "")
					placesParserTask.execute(result);
				else
					System.out.println("The result is empty");

				break;

			case PLACES_DETAILS:
				// Creating ParserTask for parsing Google Places
				placeDetailsParserTask = new AutocompleteGeoLocationParserTask(
						PLACES_DETAILS);

				// Starting Parsing the JSON string
				// This causes to execute doInBackground() of ParserTask class
				placeDetailsParserTask.execute(result);

				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);

				inputManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

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
					GeocodeJSONParser placeJsonParser = new GeocodeJSONParser();
					// Getting the parsed data as a List construct
					list = placeJsonParser.parse(jObject);
					break;
				case PLACES_DETAILS:
					PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
					// Getting the parsed data as a List construct
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

				// Creating a SimpleAdapter for the AutoCompleteTextView
				SimpleAdapter adapter = new SimpleAdapter(getActivity()
						.getBaseContext(), result,
						android.R.layout.simple_list_item_1, from, to);

				// Setting the adapter
				poiTextField.setAdapter(adapter);
				break;
			case PLACES_DETAILS:
				HashMap<String, String> hm = result.get(0);

				// Getting latitude from the parsed data
				double latitude = Double.parseDouble(hm.get("lat"));

				// Getting longitude from the parsed data
				double longitude = Double.parseDouble(hm.get("lng"));

				pointOfInterestLocation = new LatLng(latitude, longitude);

				map.addMarker(new MarkerOptions()
						.title("Point of interest")
						.position(pointOfInterestLocation)
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						pointOfInterestLocation, 10));

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

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			SimpleGeocodeJSONParser parser = new SimpleGeocodeJSONParser();

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

			locations = new ArrayList<String>();

			if (responseList != null) {

				for (HashMap<String, String> item : responseList) {

					locations.add(item.get("formatted_address"));

				}

				if (locations.size() == 0) {
					Toast.makeText(getActivity(),
							"No result found. Please make another selection",
							Toast.LENGTH_SHORT).show();

				} else if (locations.size() == 1) {

					char[] location = locations.get(0).toCharArray();

					poiTextField.setText(location, 0, location.length);

					// Getting latitude from the parsed data
					double endPointLatitude = Double.parseDouble(responseList
							.get(0).get("lat"));

					// Getting longitude from the parsed data
					double endPointLongitude = Double.parseDouble(responseList
							.get(0).get("lng"));

					pointOfInterestLocation = new LatLng(endPointLatitude,
							endPointLongitude);

					map.addMarker(new MarkerOptions()
							.title("Point of interest")
							.position(pointOfInterestLocation)
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							pointOfInterestLocation, 10));

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

									poiTextField.setText(location, 0,
											location.length);

									// Getting latitude from the parsed data
									double endPointLatitude = Double
											.parseDouble(responseList
													.get(which).get("lat"));

									// Getting longitude from the parsed data
									double endPointLongitude = Double
											.parseDouble(responseList
													.get(which).get("lng"));

									pointOfInterestLocation = new LatLng(
											endPointLatitude, endPointLongitude);

									map.addMarker(new MarkerOptions()
											.title("Point of interest")
											.position(pointOfInterestLocation)
											.icon(BitmapDescriptorFactory
													.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
									map.moveCamera(CameraUpdateFactory
											.newLatLngZoom(
													pointOfInterestLocation, 10));

								}
							});
					builder.show();
				}

			} else {
				Toast.makeText(
						getActivity(),
						"Unable to sugest any location. Most probably you are not connected to the internet.",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	/** A class, to download Places from Geocoding webservice */
	private class SimpleGeolocationDownloadTask extends
			AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {

			// Instantiating ParserTask which parses the json data from
			// Geocoding webservice
			// in a non-ui thread
			SimpleGeolocationParserTask parserTask = new SimpleGeolocationParserTask();

			// Start parsing the places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}
	}

}