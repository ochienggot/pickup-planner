package com.nicholasgot.clientapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Allows the user to pick time
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private static int mHour;
    private static int mMinute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default time for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with time chosen by the user
        mHour = hourOfDay;
        mMinute = minute;

        // Display the time picked
        TextView textView = (TextView) getActivity().findViewById(R.id.text_view_time);
        String selectedTime = "Time constraint:  " + mHour + "." + mMinute;
        textView.setText(selectedTime);
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }
}
