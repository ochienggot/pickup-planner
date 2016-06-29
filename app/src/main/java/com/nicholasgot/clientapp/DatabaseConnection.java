package com.nicholasgot.clientapp;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Connect to the database through web server
 */
public class DatabaseConnection {
    public static final String LOCAL_HOST = "http://213.159.185.35:5000"; // work network
//    public static final String LOCAL_HOST = "http://192.168.1.2:5000"; // home network
//    public static final String LOCAL_HOST = "http://10.148.13.118:5000"; // Er Studio network
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String LOG_TAG = DatabaseConnection.class.getSimpleName();

    /**
     * POSTs to the webservice endpoint
     * @param source source lat/lon pair
     * @param destination destination lat/lon pair
     */
    public void postLocation(String source, String destination) {
        final String CLIENT = "clientapp";
        final String REQUESTS = "requests";

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(LOCAL_HOST).newBuilder();
        urlBuilder.addPathSegment(CLIENT)
                .addPathSegment(REQUESTS);
        String okUrl = urlBuilder.build().toString();

        RequestBody formBody;
        final String SOURCE = "source";
        final String DESTINATION = "destination";

        try {
            JSONObject json = new JSONObject();
            json.put(SOURCE, source);
            json.put(DESTINATION, destination);
            formBody = RequestBody.create(JSON, json.toString());

        } catch (JSONException je) {
            formBody = new FormBody.Builder()
                    .add(SOURCE, source)
                    .add(DESTINATION, destination)
                    .build();

            Log.v(LOG_TAG, "Json Error: " + je);
        }

        Request request = new Request.Builder()
                .url(okUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                else {
                    String rowsInserted = response.body().string();
                    Log.v(LOG_TAG, "Rows inserted: " + rowsInserted);
                }
            }
        });
    }
}