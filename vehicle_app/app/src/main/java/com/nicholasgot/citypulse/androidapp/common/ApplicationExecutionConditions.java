package com.nicholasgot.citypulse.androidapp.common;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class ApplicationExecutionConditions {

    public static final String LOG_TAG = ApplicationExecutionConditions.class.getSimpleName();

	public final static Boolean isGPSandInternetSignal(Activity activity) {

		LocationManager locationManager = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(
					activity,
					"GPS is disabled. Please enable it and try again.",
					Toast.LENGTH_SHORT).show();
			return false;
		}

        return isInternetSignal(activity);
	}

	public final static Boolean isInternetSignal(Activity activity) {

		ConnectivityManager conn = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
        if (activeNetwork != null) {
            boolean wifiNetwork = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            boolean mobileNetwork = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiNetwork || mobileNetwork) {
                return true;
            }
        }

		return false;
	}
}
