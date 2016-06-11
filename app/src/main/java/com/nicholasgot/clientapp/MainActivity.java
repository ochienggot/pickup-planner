package com.nicholasgot.clientapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mRadioTravel;
    private boolean mRadioEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up support action toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        /*
        Spinner spinner = (Spinner) findViewById(R.id.events_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.events_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        */

        // Set click listener for Button
//        Button button = (Button) findViewById(R.id.button_confirm_events);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mRadioTravel) {
//                    Intent travelIntent = new Intent(getApplicationContext(), TravelActivity.class);
//                    startActivity(travelIntent);
//                }
//                if (mRadioEvent) {
//                    Intent eventIntent = new Intent(getApplicationContext(), EventsActivity.class);
//                    startActivity(eventIntent);
//                }
//            }
//        });
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

        // Determine which button was clicked
        switch (view.getId()) {
            case R.id.radio_travel:
                if (checked) {
                    Intent travelIntent = new Intent(getApplicationContext(), TravelActivity.class);
                    startActivity(travelIntent);
                    mRadioTravel = true;
                    mRadioEvent = false;
                }
                break;
            case R.id.radio_attend_event:
                if (checked) {
                    Intent eventIntent = new Intent(getApplicationContext(), EventsActivity.class);
                    startActivity(eventIntent);
                    mRadioEvent = true;
                    mRadioTravel = false; // TODO: handle this better
                }
        }
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

}