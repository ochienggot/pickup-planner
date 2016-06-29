package com.nicholasgot.clientapp.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.nicholasgot.clientapp.MainActivity;
import com.nicholasgot.clientapp.R;

public class MyGcmListenerService extends GcmListenerService {

    public static final String LOG_TAG = "MyGcmListenerService";

    /**
     * Called when message is received
     *
     * @param from senderID of the sender
     * @param data Data bundle containing message as key/value pairs
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(LOG_TAG, "From: " + from);
        Log.d(LOG_TAG, "Message: " + message);

        if (from.startsWith("/topics")) {
            // message received from some topic
        }
        else {
            // normal downstream message
        }

        // Process message here
        // e.g sync with the server, save msg in local db, update UI

        // Might be useful to show a notification to the user indicating that a message was received
        sendNotification(message);
    }

    /**
     * Create and show simple notification containing the received GCM message
     *
     * @param message
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_blue_700_36dp)
                .setContentTitle("New GCM message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
