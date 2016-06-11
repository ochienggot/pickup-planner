package com.nicholasgot.clientapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ContentHandler;
import java.util.Calendar;

/**
 * Created by ngot on 21/04/2016.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String LOG_TAG = DatePickerFragment.class.getSimpleName();
    private String mDay;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of datepickerdialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with date chosen
        mDay = "Date: " + day + "/" + month + "/" + year + "    ";

        TextView textView = (TextView) getActivity().findViewById(R.id.text_view_date);
        textView.setText(mDay);
    }
}
