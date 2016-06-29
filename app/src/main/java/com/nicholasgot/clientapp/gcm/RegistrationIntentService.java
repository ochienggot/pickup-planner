package com.nicholasgot.clientapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.nicholasgot.clientapp.R;

public class RegistrationIntentService extends IntentService {

    private static final String LOG_TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.v(LOG_TAG, "GCM Registration token: " + token);

            // TODO: Implement method to send any registration to app's servers
            sendRegistrationToServer(token);

            // Subscribe to the topic channels
            subscribeTopics(token);

            sharedPreferences.edit().putBoolean(MyPreferences.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(MyPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }

        // Notify UI that registration has completed
        Intent registrationComplete = new Intent(MyPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(String token) {
        // custom implementation
    }

    private void subscribeTopics(String token) throws Exception {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
