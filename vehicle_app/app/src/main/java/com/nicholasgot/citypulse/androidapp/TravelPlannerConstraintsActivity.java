package com.nicholasgot.citypulse.androidapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class TravelPlannerConstraintsActivity extends Activity {

	CheckBox fastestCheckBox;
	CheckBox shortestCheckBox;
	CheckBox cleanerCheckBox;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.travel_planner_constraints);

		
		Button travelPlaneerConstraintsOkButton = (Button) findViewById(R.id.travelPlannerConstraintsSaveButton);
		
		fastestCheckBox = (CheckBox) findViewById(R.id.trafficPlannerConstraintsFastestCheckBox);
		shortestCheckBox = (CheckBox) findViewById(R.id.trafficPlannerConstraintsShortestCheckBox);
		cleanerCheckBox = (CheckBox) findViewById(R.id.trafficPlannerConstraintsCleannerCheckBox);
		
		SharedPreferences routeConstraintsPreferences = getSharedPreferences(
				"RouteConstraintsPreferences", MODE_PRIVATE);
		final Editor routeConstraintsEditor = routeConstraintsPreferences
				.edit();
		
		boolean fastestRestoredRestoredCheckBox = routeConstraintsPreferences
				.getBoolean("fastestCheckBox", false);
		boolean shortestRestoredCheckBox = routeConstraintsPreferences
				.getBoolean("shortestCheckBox", false);
		boolean cleanerRestoredCheckBox = routeConstraintsPreferences
				.getBoolean("cleanerCheckBox", false);
		

		fastestCheckBox.setChecked(fastestRestoredRestoredCheckBox);
		shortestCheckBox.setChecked(shortestRestoredCheckBox);
		cleanerCheckBox.setChecked(cleanerRestoredCheckBox);
		

		travelPlaneerConstraintsOkButton
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						boolean trafficFastest;
						boolean trafficShortest;
					    boolean pollutionCleaner;

						trafficFastest = fastestCheckBox.isChecked();
						trafficShortest = shortestCheckBox.isChecked();
						pollutionCleaner = cleanerCheckBox.isChecked();
						
						
						routeConstraintsEditor.putBoolean(
								"fastestCheckBox", trafficFastest);
						routeConstraintsEditor.putBoolean(
								"shortestCheckBox", trafficShortest);
						routeConstraintsEditor.putBoolean(
								"cleanerCheckBox", pollutionCleaner);
						
						routeConstraintsEditor.commit();
						finish();

					}
				});

	}

	

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_constraints, menu);
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
}