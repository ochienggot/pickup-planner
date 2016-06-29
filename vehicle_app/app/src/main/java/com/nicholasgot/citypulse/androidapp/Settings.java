package com.nicholasgot.citypulse.androidapp;

import com.nicholasgot.citypulse.androidapp.common.DefaultValues;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class Settings extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View settings = inflater.inflate(R.layout.settings_frag, container,
				false);

		final CheckBox latencySmallerThanCheckBox = (CheckBox) settings
				.findViewById(R.id.latecySmallerThanCheckBox);
		final EditText latencySmallerThanText = (EditText) settings
				.findViewById(R.id.latecySmallerThanEditText);

		final CheckBox priceSmallerThanCheckBox = (CheckBox) settings
				.findViewById(R.id.priceSmallerThanCheckBox);
		final EditText priceSmallerThanText = (EditText) settings
				.findViewById(R.id.priceSmallerThanTextBox);

		final CheckBox securityLevelCheckBox = (CheckBox) settings
				.findViewById(R.id.securityLevelCheckBox);
		final EditText securityLevelText = (EditText) settings
				.findViewById(R.id.securityLevelTextBox);

		final CheckBox accuracyBiggerThanCheckBox = (CheckBox) settings
				.findViewById(R.id.accuracyBiggerThanCheckBox);
		final EditText accuracyBiggerThanText = (EditText) settings
				.findViewById(R.id.accuracyBiggerThanTextBox);

		final CheckBox completnessBiggerThanCheckBox = (CheckBox) settings
				.findViewById(R.id.completnessBiggerThanCheckBox);
		final EditText completnessBiggerThanText = (EditText) settings
				.findViewById(R.id.completnessBiggerThanTextBox);

		final CheckBox bandwithBiggerThanCheckBox = (CheckBox) settings
				.findViewById(R.id.bandwithBiggerThanCheckBox);
		final EditText bandwithBiggerThanText = (EditText) settings
				.findViewById(R.id.bandwithBiggerThanTextBox);

		final EditText serverLocationTextField = (EditText) settings
				.findViewById(R.id.serverLocation);

		Button saveSettingsBtn = (Button) settings
				.findViewById(R.id.saveSettingsButton);

		latencySmallerThanText.setEnabled(false);
		priceSmallerThanText.setEnabled(false);
		securityLevelText.setEnabled(false);
		accuracyBiggerThanText.setEnabled(false);
		accuracyBiggerThanText.setEnabled(false);
		completnessBiggerThanText.setEnabled(false);
		bandwithBiggerThanText.setEnabled(false);

		latencySmallerThanCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						latencySmallerThanText
								.setEnabled(latencySmallerThanCheckBox
										.isChecked());
					}
				});

		priceSmallerThanCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						priceSmallerThanText
								.setEnabled(priceSmallerThanCheckBox
										.isChecked());
					}
				});

		securityLevelCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						securityLevelText.setEnabled(securityLevelCheckBox
								.isChecked());

					}
				});

		accuracyBiggerThanCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						accuracyBiggerThanText
								.setEnabled(accuracyBiggerThanCheckBox
										.isChecked());

					}
				});

		completnessBiggerThanCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						completnessBiggerThanText
								.setEnabled(completnessBiggerThanCheckBox
										.isChecked());

					}
				});

		bandwithBiggerThanCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						bandwithBiggerThanText
								.setEnabled(bandwithBiggerThanCheckBox
										.isChecked());

					}
				});

		SharedPreferences settingsPreferences = this.getActivity()
				.getSharedPreferences("SettingsPreferences",
						Context.MODE_PRIVATE);
		final Editor settingsEditor = settingsPreferences.edit();

		int latencySmallerThanRestoredValue = settingsPreferences.getInt(
				"latencySmallerThanValue", 5000);
		boolean latencySmallerThanRestoredCheckBox = settingsPreferences
				.getBoolean("latencySmallerThanCheckBox", true);

		int priceSmallerThanRestoredValue = settingsPreferences.getInt(
				"priceSmallerThanValue", 5000);
		boolean priceSmallerThanRestoredCheckBox = settingsPreferences
				.getBoolean("priceSmallerThanCheckBox", true);

		int securityLevelRestoredValue = settingsPreferences.getInt(
				"securityLevelValue", 1);
		boolean securityLevelRestoredCheckBox = settingsPreferences.getBoolean(
				"securityLevelCheckBox", true);

		int accuracyBiggerThanRestoredValue = settingsPreferences.getInt(
				"accuracyBiggerThanValue", 0);
		boolean accuracyBiggerThanRestoredCheckBox = settingsPreferences
				.getBoolean("accuracyBiggerThanCheckBox", true);

		int completnessBiggerThanRestoredValue = settingsPreferences.getInt(
				"completnessBiggerThanValue", 0);
		boolean completnessBiggerThanRestoredCheckBox = settingsPreferences
				.getBoolean("completnessBiggerThanCheckBox", true);

		int bandwithBiggerThanRestoredValue = settingsPreferences.getInt(
				"bandwithBiggerThanValue", 500);
		boolean bandwithBiggerThanRestoredCheckBox = settingsPreferences
				.getBoolean("bandwithBiggerThanCheckBox", true);

		String serverLocationString = settingsPreferences.getString(
				"serverLocation", DefaultValues.WEB_SOCKET_SERVER_IP);

		latencySmallerThanText.setText("" + latencySmallerThanRestoredValue);
		latencySmallerThanCheckBox
				.setChecked(latencySmallerThanRestoredCheckBox);

		priceSmallerThanText.setText("" + priceSmallerThanRestoredValue);
		priceSmallerThanCheckBox.setChecked(priceSmallerThanRestoredCheckBox);

		securityLevelText.setText("" + securityLevelRestoredValue);
		securityLevelCheckBox.setChecked(securityLevelRestoredCheckBox);

		accuracyBiggerThanText.setText("" + accuracyBiggerThanRestoredValue);
		accuracyBiggerThanCheckBox
				.setChecked(accuracyBiggerThanRestoredCheckBox);

		completnessBiggerThanText.setText(""
				+ completnessBiggerThanRestoredValue);
		completnessBiggerThanCheckBox
				.setChecked(completnessBiggerThanRestoredCheckBox);

		bandwithBiggerThanText.setText("" + bandwithBiggerThanRestoredValue);
		bandwithBiggerThanCheckBox
				.setChecked(bandwithBiggerThanRestoredCheckBox);

		serverLocationTextField.setText(serverLocationString);

		saveSettingsBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				settingsEditor.putBoolean("latencySmallerThanCheckBox",
						latencySmallerThanCheckBox.isChecked());

				if (latencySmallerThanCheckBox.isChecked()) {
					settingsEditor.putInt("latencySmallerThanValue", Integer
							.parseInt(latencySmallerThanText.getText()
									.toString()));
				}

				settingsEditor.putBoolean("priceSmallerThanCheckBox",
						priceSmallerThanCheckBox.isChecked());

				if (priceSmallerThanCheckBox.isChecked()) {
					settingsEditor.putInt("priceSmallerThanValue",
							Integer.parseInt(priceSmallerThanText.getText()
									.toString()));
				}

				settingsEditor.putBoolean("securityLevelCheckBox",
						securityLevelCheckBox.isChecked());

				if (securityLevelCheckBox.isChecked()) {
					settingsEditor.putInt("securityLevelValue", Integer
							.parseInt(securityLevelText.getText().toString()));
				}

				settingsEditor.putBoolean("accuracyBiggerThanCheckBox",
						accuracyBiggerThanCheckBox.isChecked());

				if (accuracyBiggerThanCheckBox.isChecked()) {
					settingsEditor.putInt("accuracyBiggerThanValue", Integer
							.parseInt(accuracyBiggerThanText.getText()
									.toString()));
				}

				settingsEditor.putBoolean("completnessBiggerThanCheckBox",
						completnessBiggerThanCheckBox.isChecked());

				if (completnessBiggerThanCheckBox.isChecked()) {
					settingsEditor.putInt("completnessBiggerThanValue", Integer
							.parseInt(completnessBiggerThanText.getText()
									.toString()));
				}

				settingsEditor.putBoolean("bandwithBiggerThanCheckBox",
						bandwithBiggerThanCheckBox.isChecked());

				if (bandwithBiggerThanCheckBox.isChecked()) {
					settingsEditor.putInt("bandwithBiggerThanValue", Integer
							.parseInt(bandwithBiggerThanText.getText()
									.toString()));
				}
				
				settingsEditor.putString("serverLocation", serverLocationTextField.getText().toString());

				settingsEditor.commit();
			}
		});

		return settings;
	}
}