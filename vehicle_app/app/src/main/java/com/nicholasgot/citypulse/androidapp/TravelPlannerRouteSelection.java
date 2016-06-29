package com.nicholasgot.citypulse.androidapp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import citypulse.commons.contextual_filtering.contextual_event_request.ContextualEventRequest;
import citypulse.commons.contextual_filtering.contextual_event_request.FilteringFactor;
import citypulse.commons.contextual_filtering.contextual_event_request.FilteringFactorName;
import citypulse.commons.contextual_filtering.contextual_event_request.FilteringFactorValue;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingElement;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingElementName;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingElementValue;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingFactor;
import citypulse.commons.contextual_filtering.contextual_event_request.RankingFactorType;
import citypulse.commons.contextual_filtering.contextual_event_request.Route;
import citypulse.commons.data.Coordinate;
import citypulse.commons.reasoning_request.Answer;
import citypulse.commons.reasoning_request.Answers;
import citypulse.commons.reasoning_request.concrete.AnswerTravelPlanner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.nicholasgot.citypulse.androidapp.common.MessageConverters;

public class TravelPlannerRouteSelection extends Activity implements
		GoogleMap.OnMapClickListener {

    public static final String LOG_TAG = TravelPlannerRouteSelection.class.getSimpleName();
	private final Integer ROUTE_NUMBER_NOT_SELECTED = -1;

	private String routeReasoningResponse;
	private String routeReasoningRequest;

	private GoogleMap map;
	private HashMap<Integer, Polyline> routesPolylines = new HashMap<Integer, Polyline>();
	private HashMap<Integer, Answer> answersHashMap = new HashMap<Integer, Answer>();
	private int selectedRouteNumber = ROUTE_NUMBER_NOT_SELECTED;
	private Activity currentActivity = this;

	private boolean locationAvailable;

	private LatLng destinationPoint = null;
	private LatLng startingPoint = null;
	private LatLng interestPoint = null;

	private Bundle executionBundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_travel_planner_route_selection);

		Intent intent = getIntent();
		executionBundle = intent.getBundleExtra(Execution.EXECUTION_DETAILS);

		routeReasoningResponse = executionBundle
				.getString(Execution.DECISION_SUPPORT_TRAVEL_PLANNER_RESONSE);
		routeReasoningRequest = executionBundle
				.getString(Execution.DECISION_SUPPORT_TRAVEL_PLANNER_REQUEST);

        Log.v(LOG_TAG, "Reasoning response: " + routeReasoningResponse);
        Log.v(LOG_TAG, "Reasoning request: " + routeReasoningRequest);

		startingPoint = new Gson().fromJson(
				executionBundle.getString(Execution.STARTING_POINT),
				LatLng.class);
		destinationPoint = new Gson().fromJson(
				executionBundle.getString(Execution.DESTINATION_POINT),
				LatLng.class);

		if (executionBundle.containsKey(Execution.INTEREST_POINT)) {
			interestPoint = new Gson().fromJson(
					executionBundle.getString(Execution.INTEREST_POINT),
					LatLng.class);
		}

		FragmentManager myFragmentManager = getFragmentManager();
		MapFragment myMapFragment = (MapFragment) myFragmentManager
				.findFragmentById(R.id.travelPlannerSelectionMap);
		map = myMapFragment.getMap();

		loadRoutesOnMap();

		map.setOnMapClickListener(this);

		Button selectButton = (Button) findViewById(R.id.travelPlannerSelectionSelectButton);

		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (selectedRouteNumber == ROUTE_NUMBER_NOT_SELECTED) {
					Toast.makeText(currentActivity,
							"Please select a route first!", Toast.LENGTH_SHORT)
							.show();
				} else {

					System.out
							.println("Start the ROUTE_CONTEXTUAL_EVENT_REQUESTS creation");

					Answer selectedAnswer = answersHashMap
							.get(selectedRouteNumber);

					AnswerTravelPlanner answerTravelPlanner = (AnswerTravelPlanner) selectedAnswer;

					Route place = new Route();
					place.setPlaceId(answerTravelPlanner.getId() + "");
					place.setRoute(answerTravelPlanner.getRoute());
					place.setLength(answerTravelPlanner.getLength());

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
						    place, filteringFactors, rankingFactor);
					

					String contextualEventRequestString = MessageConverters
							.contextualEventRequestToJSON(contextualEventRequest);

					System.out.println("contextualEventRequestString "
							+ contextualEventRequestString);

					Intent intent = new Intent(currentActivity, Execution.class);

					executionBundle.putString(
							Execution.ROUTE_CONTEXTUAL_EVENT_REQUESTS,
							contextualEventRequestString);

					intent.putExtra(Execution.EXECUTION_DETAILS,
							executionBundle);

					startActivity(intent);
					finish();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		locationAvailable = false;

		// if (map == null) {
		// map = travelPlannerSupportMapFragment.getMap();
		//
		//
		// travelPlannerFragmentMap.clear();
		//
		// }
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

	private String createContextualEventRequest() {

		return "request";
	}

	private void loadRoutesOnMap() {

		Answers answers = MessageConverters
				.decisionSupportResponsefronJson(routeReasoningResponse);

		// List<Answer> ansersList = new ArrayList<Answer>();
		//
		// List<Coordinate> coordinateList1 = new ArrayList<Coordinate>();
		// coordinateList1.add(new Coordinate(10.1542546, 56.2105575));
		// coordinateList1.add(new Coordinate(10.1552546, 56.2115575));
		//
		// AnswerTravelPlanner answerTravelPlanner1 = new AnswerTravelPlanner();
		// answerTravelPlanner1.setRoute(coordinateList1);
		// ansersList.add(answerTravelPlanner1);
		//
		// List<Coordinate> coordinateList2 = new ArrayList<Coordinate>();
		// coordinateList2.add(new Coordinate(10.1642546, 56.2105575));
		// coordinateList2.add(new Coordinate(10.1652546, 56.2115575));
		//
		// AnswerTravelPlanner answerTravelPlanner2 = new AnswerTravelPlanner();
		// answerTravelPlanner2.setRoute(coordinateList2);
		// ansersList.add(answerTravelPlanner2);
		//
		// Answers answers = new Answers();
		// answers.setAnswers(ansersList);

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		Integer routeCounter = 0;

		for (Answer answer : answers.getAnswers()) {

			answersHashMap.put(routeCounter, answer);

			AnswerTravelPlanner answerTravelPlanner = (AnswerTravelPlanner) answer;

			// TODO: markers on map can be added here
			List<Coordinate> route = answerTravelPlanner.getRoute();

			PolylineOptions routePolyline = new PolylineOptions();

			for (Coordinate coordinate : route) {
				routePolyline.add(new LatLng(coordinate.getLatitude(),
						coordinate.getLongitude()));

				builder.include(new LatLng(coordinate.getLatitude(), coordinate
						.getLongitude()));
			}

			routePolyline.color(Color.RED);

			Polyline poliPolyline = map.addPolyline(routePolyline);

			routesPolylines.put(routeCounter, poliPolyline);
			routeCounter++;
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
				.title("Starting point")
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

		map.addMarker(new MarkerOptions()
                .position(destinationPoint)
				.title("Destination point"));

		if (interestPoint != null) {
			map.addMarker(new MarkerOptions()
					.title("Point of interest")
					.position(interestPoint)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		}
	}

	private float minDistanceBetweenPointAndPolyline(LatLng point,
			Polyline poliPolyline) {

		List<LatLng> polylinePoints = poliPolyline.getPoints();

		float[] distanceTemp = new float[10];

		Location.distanceBetween(point.latitude, point.longitude,
				polylinePoints.get(0).latitude,
				polylinePoints.get(0).longitude, distanceTemp);

		float minDistance = distanceTemp[0];

		for (LatLng currentPoint : polylinePoints) {

			Location.distanceBetween(point.latitude, point.longitude,
					currentPoint.latitude, currentPoint.longitude, distanceTemp);

			if (distanceTemp[0] < minDistance) {
				minDistance = distanceTemp[0];
			}
		}

		return minDistance;
	}

	@Override
	public void onMapClick(LatLng point) {

		float minDistance = minDistanceBetweenPointAndPolyline(point,
				routesPolylines.get(0));
		Polyline minDistancePolyline = routesPolylines.get(0);
		selectedRouteNumber = 0;

		for (Integer key : routesPolylines.keySet()) {

			Polyline polyline = routesPolylines.get(key);
			float currentDistance = minDistanceBetweenPointAndPolyline(point,
					polyline);

			if (minDistance > currentDistance) {
				minDistance = currentDistance;
				minDistancePolyline = polyline;
				selectedRouteNumber = key;
			}
		}

		for (Polyline polyline : routesPolylines.values()) {
			polyline.setColor(Color.RED);
		}

		minDistancePolyline.setColor(Color.BLUE);

	}
}
