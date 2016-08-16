package com.nicholasgot.clientapp.utils;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * GPS and Network conditions for application
 */
public class NetworkAndLocationServices {

    public final static Boolean isGPSandInternetSignal(Activity activity) {

        LocationManager locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(
                    activity,
                    "Your GPS seems to be disabled. Please enable it and try again.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return isInternetSignal(activity);
    }

    /**
     * Check Internet connectivity status for mobile and WiFi
     *
     * @param activity containing activity
     * @return true if either WiFi or mobile Internet is connected and false otherwise
     */
    public static Boolean isInternetSignal(Activity activity) {
        ConnectivityManager conn = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conn != null) {
            NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
            if (activeNetwork != null) {
                boolean wifiNetwork = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                boolean mobileNetwork = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                return wifiNetwork || mobileNetwork; // Take advantage of short-circuiting
            }
        }
        return false;
    }
}
