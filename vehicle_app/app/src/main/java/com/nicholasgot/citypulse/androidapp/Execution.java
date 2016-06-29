package com.nicholasgot.citypulse.androidapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import citypulse.commons.contextual_filtering.city_event_ontology.CityEvent;
import citypulse.commons.contextual_filtering.city_event_ontology.CriticalEventResults;
import citypulse.commons.contextual_filtering.contextual_event_request.ContextualEventRequest;
import citypulse.commons.contextual_filtering.contextual_event_request.Place;
import citypulse.commons.contextual_filtering.contextual_event_request.PlaceAdapter;
import citypulse.commons.contextual_filtering.contextual_event_request.Route;
import citypulse.commons.data.Coordinate;
import citypulse.commons.event_request.DataFederationRequest;
import citypulse.commons.event_request.DataFederationRequest.DataFederationPropertyType;
import citypulse.commons.event_request.DataFederationResult;
import citypulse.commons.event_request.QosVector;
import citypulse.commons.event_request.WeightVector;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nicholasgot.citypulse.androidapp.common.DefaultValues;
import com.nicholasgot.citypulse.androidapp.common.MessageConverters;

public class Execution extends Activity implements LocationListener {

	public static final String EXECUTION_DETAILS = "execution details";
	public static final String STARTING_POINT = "starting point";
	public static final String DESTINATION_POINT = "destination point";
	public static final String INTEREST_POINT = "interest point";
	public static final String PARKING_CONTEXTUAL_EVENT_REQUESTS = "parking contextual event request";
	public static final String ROUTE_CONTEXTUAL_EVENT_REQUESTS = "route contextual event request";

	public static final String DECISION_SUPPORT_PARKING_PLANNER_RESONSE = "decision support parking planner response";
	public static final String DECISION_SUPPORT_PARKING_PLANNER_REQUEST = "decision support parking planner request";

	public static final String DECISION_SUPPORT_TRAVEL_PLANNER_RESONSE = "decision support travel planner response";
	public static final String DECISION_SUPPORT_TRAVEL_PLANNER_REQUEST = "decision support travel planner request";

	public static final String TRAVEL_STATUS_REQUEST = "travel status request";
	public static final String TRAVEL_STATUS_EVENT = "travel status event";
	public static final String TRAVEL_STATUS_EVENT_PAYLOAD = "travel status event payload";

	private String parkingContextualEventRequestString = null;
	private String routeContextualEventRequestString = null;
	private Intent parkingServiceIntent = null;
	private Intent routeServiceIntent = null;
	private Intent travelStatusServiceIntent = null;
	private Coordinate startingPoint = null;
	private Coordinate destinationPoint = null;
	private Coordinate interestPoint = null;
	private GoogleMap map;
	private boolean locationAvailable;
	private Marker userPositionMarker;
	private ContextualEventRequest parkingContextualEventRequest = null;
	private ContextualEventRequest routeContextualEventRequest = null;
	private LocationManager locationManager;
	private Activity currentActivity = this;
	private BroadcastReceiver alertsBroadcastReceiver;
	private BroadcastReceiver statusBroadcastReceiver;
	private BroadcastReceiver errorBroadcastReceiver;
	private String travelStatusEventRequest = null;
	private TextView trafficStatusTextView;
	private TextView pollutionStatusTextView;

	private List<AlertDialog> alerDialogList = new ArrayList<AlertDialog>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_travel_planner_execution);

		trafficStatusTextView = (TextView) findViewById(R.id.executionTrafficStatus);
		pollutionStatusTextView = (TextView) findViewById(R.id.executionPollutionStatus);

		System.out.println("Starting execution with the following details:");

		Intent intent = getIntent();

		Bundle bundle = intent.getBundleExtra(EXECUTION_DETAILS);

		if (bundle.containsKey(PARKING_CONTEXTUAL_EVENT_REQUESTS)) {
			parkingContextualEventRequestString = bundle
					.getString(PARKING_CONTEXTUAL_EVENT_REQUESTS);

			System.out.println("PARKING_CONTEXTUAL_EVENT_REQUESTS: "
					+ parkingContextualEventRequestString);
		}

		if (bundle.containsKey(ROUTE_CONTEXTUAL_EVENT_REQUESTS)) {
			routeContextualEventRequestString = bundle
					.getString(ROUTE_CONTEXTUAL_EVENT_REQUESTS);
			System.out.println("ROUTE_CONTEXTUAL_EVENT_REQUESTS: "
					+ routeContextualEventRequestString);
		}

		if (bundle.containsKey(STARTING_POINT)) {
			startingPoint = new Gson().fromJson(
					bundle.getString(STARTING_POINT), Coordinate.class);

			System.out.println("STARTING_POINT: " + startingPoint);
		}

		if (bundle.containsKey(DESTINATION_POINT)) {
			destinationPoint = new Gson().fromJson(
					bundle.getString(DESTINATION_POINT), Coordinate.class);
			System.out.println("DESTINATION_POINT: " + destinationPoint);
		}

		if (bundle.containsKey(INTEREST_POINT)) {
			interestPoint = new Gson().fromJson(
					bundle.getString(INTEREST_POINT), Coordinate.class);
			System.out.println("INTEREST_POINT: " + interestPoint);
		}

		if (parkingContextualEventRequestString != null) {
			parkingServiceIntent = new Intent(this,
					ParkingNotificationService.class);
			parkingServiceIntent.putExtra(PARKING_CONTEXTUAL_EVENT_REQUESTS,
					parkingContextualEventRequestString);
			startService(parkingServiceIntent);

			// Dan please check
			parkingContextualEventRequest = MessageConverters
					.contextualEventRequestFromJSON(parkingContextualEventRequestString);
		}

		if (routeContextualEventRequestString != null) {
			routeServiceIntent = new Intent(this,
					TravelNotificationService.class);
			routeServiceIntent.putExtra(ROUTE_CONTEXTUAL_EVENT_REQUESTS,
					routeContextualEventRequestString);
			startService(routeServiceIntent);

			// Dan please check
			routeContextualEventRequest = MessageConverters
					.contextualEventRequestFromJSON(routeContextualEventRequestString);

			launchTravelStatusService();

		}

		FragmentManager myFragmentManager = getFragmentManager();
		MapFragment myMapFragment = (MapFragment) myFragmentManager
				.findFragmentById(R.id.travelPlannerSelectionMap);
		map = myMapFragment.getMap();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 10, this);

		alertsBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String criticalEventResultsString = intent
						.getStringExtra(DefaultValues.EVENT_ALERT_MESSAGE_PAYLOAD);

				System.out.println("critical event: "
						+ criticalEventResultsString);

				GsonBuilder builder = new GsonBuilder();
				builder.registerTypeAdapter(Place.class, new PlaceAdapter());
				Gson gson = builder.create();

				CriticalEventResults criticalEventResults = gson.fromJson(
						criticalEventResultsString, CriticalEventResults.class);

				StringBuilder messageStringBuilder = new StringBuilder(
						"The following events have been received: ");

				Boolean eventOK = true;

				for (CityEvent contextualEvent : criticalEventResults
						.getContextualEvents()) {

					if (contextualEvent.getEventLevel() > 0) {
						messageStringBuilder.append(contextualEvent
								.getEventCategory()
								+ "[level = "
								+ contextualEvent.getEventLevel()
								+ ", coordinates("
								+ contextualEvent.getEventPlace()
										.getCentreCoordinate().toString()
								+ ")]; ");
					} else {
						eventOK = false;
					}
				}

				if (eventOK) {

					for (AlertDialog alertDialog : alerDialogList) {
						if (alertDialog.isShowing())
							alertDialog.cancel();
					}

					AlertDialog alertDialog = new AlertDialog.Builder(
							currentActivity)
							.setTitle("Event notification")
							.setMessage(messageStringBuilder.toString())
							.setPositiveButton("Go to parking selection",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {

											Intent intent = new Intent();
											intent.setAction(DefaultValues.COMMAND_GO_TO_PARKING_RECOMANDATION);
											currentActivity
													.sendBroadcast(intent);
											currentActivity.finish();
										}
									})
							.setNegativeButton("Go to route selection",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											Intent intent = new Intent();
											intent.setAction(DefaultValues.COMMAND_GO_TO_TRAVEL_RECOMANDATION);
											currentActivity
													.sendBroadcast(intent);
											currentActivity.finish();
										}
									})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setNeutralButton("Continue",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {

											System.out
													.println("The user has decided to continue.");

										}
									}).show();

					alerDialogList.add(alertDialog);
				} else {
					System.out.println("The event was not displayed!");
				}
			}
		};

		IntentFilter alertIntentFilter = new IntentFilter();
		alertIntentFilter.addAction(DefaultValues.EVENT_ALERT_MESSAGE);
		registerReceiver(alertsBroadcastReceiver, alertIntentFilter);

		errorBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String errorMessage = intent
						.getStringExtra(DefaultValues.ERROR_MESSAGE_PAYLOAD);
				
				Intent intentError = new Intent(currentActivity, ErrorPanel.class);

				intentError.putExtra(
						"Error",
						errorMessage);
				startActivity(intentError);

			}
		};

		IntentFilter errorIntentFilter = new IntentFilter();
		errorIntentFilter.addAction(DefaultValues.ERROR_MESSAGE);
		registerReceiver(errorBroadcastReceiver, errorIntentFilter);

		statusBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String statusEvent = intent
						.getStringExtra(Execution.TRAVEL_STATUS_EVENT_PAYLOAD);

				System.out.println("received event from data federation "
						+ statusEvent);

				if (!statusEvent.contains("FAULT:")) {

					DataFederationResult dataFederationRequest = (DataFederationResult) new Gson()
							.fromJson(statusEvent, DataFederationResult.class);

					if (dataFederationRequest.getResult().containsKey(
							"http://ict-citypulse.eu/city#AirPollutionIndex")) {
						char[] pollutionstatusMessage = ("Air qualit index: " + dataFederationRequest
								.getResult()
								.get("http://ict-citypulse.eu/city#AirPollutionIndex")
								.get(0)).toCharArray();

						pollutionStatusTextView.setText(pollutionstatusMessage,
								0, pollutionstatusMessage.length);
					}

					if (dataFederationRequest.getResult().containsKey(
							"http://ict-citypulse.eu/city#AverageSpeed")) {

						char[] trafficstatusMessage = ("Average speed: "
								+ ((Double) Double
										.parseDouble(dataFederationRequest
												.getResult()
												.get("http://ict-citypulse.eu/city#AverageSpeed")
												.get(0))).intValue() + " km/h")
								.toCharArray();

						trafficStatusTextView.setText(trafficstatusMessage, 0,
								trafficstatusMessage.length);

					}
				} else {
					System.out
							.println("The data federation event cannot be parsed!");

					Intent errorIntent = new Intent(currentActivity,
							ErrorPanel.class);

					errorIntent
							.putExtra(
									"Error",
									"Invalid message received from data federation component. The application will not display values for air quality index and average speed. The message is: "
											+ statusEvent);
					startActivity(errorIntent);
				}

			}
		};

		IntentFilter statusIntentFilter = new IntentFilter();
		statusIntentFilter.addAction(Execution.TRAVEL_STATUS_EVENT);
		registerReceiver(statusBroadcastReceiver, statusIntentFilter);

	}

	@Override
	protected void onDestroy() {

		for (AlertDialog alertDialog : alerDialogList) {
			if (alertDialog.isShowing())
				alertDialog.cancel();
		}

		if (parkingServiceIntent != null) {
			stopService(parkingServiceIntent);
		}

		if (routeServiceIntent != null) {
			stopService(routeServiceIntent);
		}

		if (travelStatusServiceIntent != null) {
			stopService(travelStatusServiceIntent);
		}

		
		System.out.println("1");
		unregisterReceiver(alertsBroadcastReceiver);
		System.out.println("2");
		unregisterReceiver(statusBroadcastReceiver);
		System.out.println("3");
		
		super.onDestroy();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.travel_planner_execution, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationAvailable = false;

	}

	@Override
	public void onLocationChanged(Location location) {

		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());

		if (locationAvailable == false) {

			map.clear();

			userPositionMarker = map.addMarker(new MarkerOptions()
					.position(latLng)
					.title("my position")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.user_position_marker)));

			if (startingPoint != null)
				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(startingPoint.getLatitude(),
										startingPoint.getLongitude()))
						.title("Starting point")
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

			if (destinationPoint != null)
				map.addMarker(new MarkerOptions().title("Destination point")
						.position(
								new LatLng(destinationPoint.getLatitude(),
										destinationPoint.getLongitude())));

			if (interestPoint != null) {
				map.addMarker(new MarkerOptions()
						.title("Point of interest")
						.position(
								new LatLng(interestPoint.getLatitude(),
										interestPoint.getLongitude()))
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
			}

			if (routeContextualEventRequest != null) {

				List<Coordinate> route = ((Route) routeContextualEventRequest
						.getPlace()).getRoute();

				PolylineOptions routePolyline = new PolylineOptions();

				for (Coordinate coordinate : route) {
					routePolyline.add(new LatLng(coordinate.getLatitude(),
							coordinate.getLongitude()));
				}

				routePolyline.color(Color.RED);

				map.addPolyline(routePolyline);

				// map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,
				// 12));
			}

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

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

	private void launchTravelStatusService() {
		List<DataFederationPropertyType> dataFederationPropertyTypes = new ArrayList<DataFederationRequest.DataFederationPropertyType>();
		dataFederationPropertyTypes.add(DataFederationPropertyType.air_quality);
		dataFederationPropertyTypes
				.add(DataFederationPropertyType.average_speed);

		SharedPreferences settingsPreferences = getSharedPreferences(
				"SettingsPreferences", Context.MODE_PRIVATE);
		final Editor settingsEditor = settingsPreferences.edit();

		int latencySmallerThanRestoredValue = settingsPreferences.getInt(
				"latencySmallerThanValue", 0);
		boolean latencySmallerThanRestoredCheckBox = settingsPreferences
				.getBoolean("latencySmallerThanCheckBox", false);

		int priceSmallerThanRestoredValue = settingsPreferences.getInt(
				"priceSmallerThanValue", 0);
		boolean priceSmallerThanRestoredCheckBox = settingsPreferences
				.getBoolean("priceSmallerThanCheckBox", false);

		int securityLevelRestoredValue = settingsPreferences.getInt(
				"securityLevelValue", 0);
		boolean securityLevelRestoredCheckBox = settingsPreferences.getBoolean(
				"securityLevelCheckBox", false);

		int accuracyBiggerThanRestoredValue = settingsPreferences.getInt(
				"accuracyBiggerThanValue", 0);
		boolean accuracyBiggerThanRestoredCheckBox = settingsPreferences
				.getBoolean("accuracyBiggerThanCheckBox", false);

		int completnessBiggerThanRestoredValue = settingsPreferences.getInt(
				"completnessBiggerThanValue", 0);
		boolean completnessBiggerThanRestoredCheckBox = settingsPreferences
				.getBoolean("completnessBiggerThanCheckBox", false);

		int bandwithBiggerThanRestoredValue = settingsPreferences.getInt(
				"bandwithBiggerThanValue", 0);
		boolean bandwithBiggerThanRestoredCheckBox = settingsPreferences
				.getBoolean("bandwithBiggerThanCheckBox", false);

		QosVector qosVector = new QosVector();

		if (latencySmallerThanRestoredCheckBox)
			qosVector.setLatency(latencySmallerThanRestoredValue);

		if (priceSmallerThanRestoredCheckBox)
			qosVector.setPrice(priceSmallerThanRestoredValue);

		if (securityLevelRestoredCheckBox)
			qosVector.setSecurity(securityLevelRestoredValue);

		if (accuracyBiggerThanRestoredCheckBox)
			qosVector.setAccuracy(new Double(accuracyBiggerThanRestoredValue));

		if (completnessBiggerThanRestoredCheckBox)
			qosVector.setReliability(new Double(
					completnessBiggerThanRestoredValue));

		if (bandwithBiggerThanRestoredCheckBox)
			qosVector.setTraffic(new Double(bandwithBiggerThanRestoredValue));

		WeightVector weightVector = new WeightVector();

		// Dan Please check
		DataFederationRequest dataFederationRequest = new DataFederationRequest(
				dataFederationPropertyTypes,
				((Route) routeContextualEventRequest.getPlace()).getRoute(),
				true, qosVector, weightVector);

		travelStatusEventRequest = new Gson().toJson(dataFederationRequest);

		System.out.println("dataFederationRequest " + travelStatusEventRequest);

		travelStatusServiceIntent = new Intent(this, TravelStatusService.class);
		travelStatusServiceIntent.putExtra(TRAVEL_STATUS_REQUEST,
				travelStatusEventRequest);
		startService(travelStatusServiceIntent);

	}
}
