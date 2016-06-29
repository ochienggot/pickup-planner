package com.nicholasgot.citypulse.androidapp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import citypulse.commons.contextual_filtering.contextual_event_request.ContextualEventRequest;
import citypulse.commons.contextual_filtering.contextual_event_request.FilteringFactor;
import citypulse.commons.contextual_filtering.contextual_event_request.FilteringFactorName;
import citypulse.commons.contextual_filtering.contextual_event_request.FilteringFactorValue;
import citypulse.commons.contextual_filtering.contextual_event_request.Point;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingElement;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingElementName;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingElementValue;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingFactor;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingFactorType;
import citypulse.commons.data.Coordinate;
import citypulse.commons.reasoning_request.Answer;
import citypulse.commons.reasoning_request.Answers;
import citypulse.commons.reasoning_request.concrete.AnswerParkingSpaces;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.nicholasgot.citypulse.androidapp.common.DefaultValues;
import com.nicholasgot.citypulse.androidapp.common.MessageConverters;

public class ParkingPlaceSelectionActivity extends Activity implements
		OnMarkerClickListener {

	public final static String GO_TO_ROUTE_SELECTION = "go to route selection";
	// public final static String PARKING_SELECTION_DETAILS =
	// "parking selection details";

	private final Integer PARKING_PLACE_NUMBER_NOT_SELECTED = -1;

	private String parkingReasoningResponse;
	private String parkingReasoningRequest;
	private LatLng startingPoint;
	private LatLng interestPoint;
	private LatLng destinationPoint;
	private GoogleMap map;
	private HashMap<Integer, Answer> answersHashMap = new HashMap<Integer, Answer>();
	private HashMap<Marker, Integer> parkingPlacesMarkers = new HashMap<Marker, Integer>();
	private int selectedParkingNumber = PARKING_PLACE_NUMBER_NOT_SELECTED;
	private Marker selectedMarker;
	private Activity currentActivity = this;
	private Bundle requestBundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_place_selection);

		Intent intent = getIntent();
		requestBundle = intent.getBundleExtra(Execution.EXECUTION_DETAILS);

		parkingReasoningResponse = requestBundle
				.getString(Execution.DECISION_SUPPORT_PARKING_PLANNER_RESONSE);
		parkingReasoningRequest = requestBundle
				.getString(Execution.DECISION_SUPPORT_PARKING_PLANNER_REQUEST);

		startingPoint = new Gson()
				.fromJson(requestBundle.getString(Execution.STARTING_POINT),
						LatLng.class);
		interestPoint = new Gson()
				.fromJson(requestBundle.getString(Execution.INTEREST_POINT),
						LatLng.class);

		FragmentManager myFragmentManager = getFragmentManager();
		MapFragment myMapFragment = (MapFragment) myFragmentManager
				.findFragmentById(R.id.parkingPlaceSelectionMap);
		map = myMapFragment.getMap();

		loadParkingPlacesOnMap();

		map.setOnMarkerClickListener(this);

		Button selectButton = (Button) findViewById(R.id.parkingPlaceselectionSelectButton);

		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (selectedParkingNumber == PARKING_PLACE_NUMBER_NOT_SELECTED) {
					Toast.makeText(currentActivity,
							"Please select a parking place first!",
							Toast.LENGTH_SHORT).show();
				} else {

//					ReasoningRequest reasoningRequest = MessageConverters
//							.resoningRequestFronJson(parkingReasoningRequest);

					Answer selectedAnswer = answersHashMap
							.get(selectedParkingNumber);

//					ContextualEventRequest contextualEventRequest = new ContextualEventRequest(
//							reasoningRequest, selectedAnswer);

					
					AnswerParkingSpaces answerParkingSpaces = (AnswerParkingSpaces) selectedAnswer;

					Point point = new Point();
					point.setPoint(answerParkingSpaces.getPosition());

					Set<FilteringFactor> filteringFactors = new HashSet<FilteringFactor>();

					Set<FilteringFactorValue> filteringFactorValueActivity = new HashSet<FilteringFactorValue>();
					filteringFactorValueActivity.add(new FilteringFactorValue(
							"CarCommute"));
					FilteringFactor filteringFactor = new FilteringFactor(
							FilteringFactorName.ACTIVITY,
							filteringFactorValueActivity);
					filteringFactors.add(filteringFactor);

					Set<RankingElement> rankingElements = new HashSet<RankingElement>();
					rankingElements.add(new RankingElement(
							RankingElementName.DISTANCE,
							new RankingElementValue(70)));
					rankingElements.add(new RankingElement(
							RankingElementName.EVENT_LEVEL,
							new RankingElementValue(30)));
					
					RankingFactor rankingFactor = new RankingFactor(
						    RankingFactorType.LINEAR, rankingElements);
					
					ContextualEventRequest contextualEventRequest = new ContextualEventRequest(
						    point, filteringFactors, rankingFactor);
					
					
					
					
					
					String contextualEventRequestString = MessageConverters
							.contextualEventRequestToJSON(contextualEventRequest);

					Intent intent = new Intent(currentActivity, Execution.class);

					requestBundle.putString(
							Execution.PARKING_CONTEXTUAL_EVENT_REQUESTS,
							contextualEventRequestString);

					requestBundle.putString(Execution.DESTINATION_POINT,
							new Gson().toJson(destinationPoint));

					intent.putExtra(Execution.EXECUTION_DETAILS, requestBundle);

					startActivity(intent);

					finish();

				}

			}
		});

		Button routeButton = (Button) findViewById(R.id.parkingPlaceSelectionGoToRoute);

		routeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (selectedParkingNumber == PARKING_PLACE_NUMBER_NOT_SELECTED) {
					Toast.makeText(currentActivity,
							"Please select a parking place first!",
							Toast.LENGTH_SHORT).show();
				} else {

//					ReasoningRequest reasoningRequest = MessageConverters
//							.resoningRequestFronJson(parkingReasoningRequest);

					Answer selectedAnswer = answersHashMap
							.get(selectedParkingNumber);

//					ContextualEventRequest contextualEventRequest = new ContextualEventRequest(
//							reasoningRequest, selectedAnswer);



					AnswerParkingSpaces answerParkingSpaces = (AnswerParkingSpaces) selectedAnswer;

					Point point = new Point();
					point.setPoint(answerParkingSpaces.getPosition());

					Set<FilteringFactor> filteringFactors = new HashSet<FilteringFactor>();

					Set<FilteringFactorValue> filteringFactorValueActivity = new HashSet<FilteringFactorValue>();
					filteringFactorValueActivity.add(new FilteringFactorValue(
							"CarCommute"));
					FilteringFactor filteringFactor = new FilteringFactor(
							FilteringFactorName.ACTIVITY,
							filteringFactorValueActivity);
					filteringFactors.add(filteringFactor);

					Set<RankingElement> rankingElements = new HashSet<RankingElement>();
					rankingElements.add(new RankingElement(
							RankingElementName.DISTANCE,
							new RankingElementValue(70)));
					rankingElements.add(new RankingElement(
							RankingElementName.EVENT_LEVEL,
							new RankingElementValue(30)));
					
					RankingFactor rankingFactor = new RankingFactor(
						    RankingFactorType.LINEAR, rankingElements);
					
					ContextualEventRequest contextualEventRequest = new ContextualEventRequest(
						    point, filteringFactors, rankingFactor);
					
					
					
					
					
					
					
					
					
					
					
					String contextualEventRequestString = MessageConverters
							.contextualEventRequestToJSON(contextualEventRequest);

					requestBundle.putString(
							Execution.PARKING_CONTEXTUAL_EVENT_REQUESTS,
							contextualEventRequestString);

					requestBundle.putString(Execution.DESTINATION_POINT,
							new Gson().toJson(destinationPoint));

					Intent intent = new Intent();

					intent.setAction(GO_TO_ROUTE_SELECTION);
					intent.putExtra(DefaultValues.COMMMAND_BUNDLE,
							requestBundle);

					sendBroadcast(intent);
					finish();

				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parking_place_selection, menu);
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

	private void loadParkingPlacesOnMap() {

		Answers answers = MessageConverters
				.decisionSupportResponsefronJson(parkingReasoningResponse);

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		Integer markerCounter = 0;

		for (Answer answer : answers.getAnswers()) {

			answersHashMap.put(markerCounter, answer);

			AnswerParkingSpaces answerTravelPlanner = (AnswerParkingSpaces) answer;

			Coordinate coordinate = answerTravelPlanner.getPosition();

			LatLng location = new LatLng(coordinate.getLatitude(),
					coordinate.getLongitude());

			builder.include(location);

			Marker marker = map.addMarker(new MarkerOptions()
					.position(location).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.parking_place)));

			parkingPlacesMarkers.put(marker, markerCounter);
			markerCounter++;

		}

		LatLngBounds bounds = builder.build();

		int width = getResources().getDisplayMetrics().widthPixels;
		int height = getResources().getDisplayMetrics().heightPixels;
		int padding = (int) (width * 0.12);

		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width,
				height, padding);

		map.moveCamera(cu);

		map.addMarker(new MarkerOptions()
				.position(startingPoint)
				.title("my position")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.user_position_marker)));

		map.addMarker(new MarkerOptions()
		.title("Point of interest")
		.position(interestPoint)
		.icon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

	}

	@Override
	public boolean onMarkerClick(Marker arg0) {

		selectedMarker = arg0;
		
		selectedMarker.showInfoWindow();

		if (parkingPlacesMarkers.containsKey(selectedMarker)) {

			selectedParkingNumber = parkingPlacesMarkers.get(arg0);

			Coordinate coordinate = ((AnswerParkingSpaces) answersHashMap
					.get(selectedParkingNumber)).getPosition();

			destinationPoint = new LatLng(coordinate.getLatitude(),
					coordinate.getLongitude());

			for (Marker currentMarker : parkingPlacesMarkers.keySet()) {
				currentMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.parking_place));
			}

			selectedMarker.setIcon(BitmapDescriptorFactory
					.fromResource(R.drawable.selected_parking_place));
		}

		return false;
	}
}
