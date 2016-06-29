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

        ConnectivityManager conn = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conn != null && (
                (conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() ==
                        NetworkInfo.State.DISCONNECTED) &&
                        (conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() ==
                                NetworkInfo.State.DISCONNECTED ))) {

            Toast.makeText(activity,
                    "You are not connected to the internet. Please connect and try again.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static Boolean isInternetSignal(Activity activity) {

        ConnectivityManager conn = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
        boolean noWifi = activeNetwork.getType() != ConnectivityManager.TYPE_WIFI;
        boolean noCellular = activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE;
        if (noWifi && noCellular) {
//                (conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() ==
//                        NetworkInfo.State.DISCONNECTED) &&
//                        (conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() ==
//                                NetworkInfo.State.DISCONNECTED ))) {

            Toast.makeText(activity,
                    "You are not connected to the internet. Please connect and try again.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
