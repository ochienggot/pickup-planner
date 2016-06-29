package com.nicholasgot.citypulse.androidapp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.nicholasgot.citypulse.androidapp.common.DefaultValues;

public class TravelStatusService extends Service {

	private Looper mServiceLooper;
	private NotificationHandler notificationHandler;

	private String request;

	private final int CONTINOUS_WEKSOCKET_SESION = -1;

	private boolean stopThread = false;

	Service currentService = this;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {

		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		notificationHandler = new NotificationHandler(mServiceLooper);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		request = intent.getStringExtra(Execution.TRAVEL_STATUS_REQUEST);

		System.out
				.println("Started notification service (TRAVEL_STATUS_REQUEST) for the following request "
						+ request);

		Message msg = notificationHandler.obtainMessage();
		msg.arg1 = startId;
		notificationHandler.sendMessage(msg);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {

		if (notificationHandler != null) {
			notificationHandler.stopThread();
		}

		System.out
				.println("Stoped notification service for the following request "
						+ request);
	}

	private final class NotificationHandler extends Handler implements
			LocationListener {

		private Location lastLocation = null;

		public void stopThread() {

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.removeUpdates(this);

			stopThread = true;
		}

		public NotificationHandler(Looper looper) {
			super(looper);

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 10, this);
		}

		@Override
		public void handleMessage(Message msg) {

			ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
					.build();

			ClientManager clientManager = ClientManager.createClient();

			Endpoint clientEndpoint = new Endpoint() {

				@Override
				public void onOpen(Session session, EndpointConfig config) {

					session.addMessageHandler(new MessageHandler.Whole<String>() {

						public void onMessage(String message) {

							// System.out
							// .println("Received travel status event "
							// + message);

							Intent intent = new Intent();
							intent.setAction(Execution.TRAVEL_STATUS_EVENT);
							intent.putExtra(
									Execution.TRAVEL_STATUS_EVENT_PAYLOAD,
									message);
							sendBroadcast(intent);

						}

					});

				}
			};

			SharedPreferences settingsPreferences = getSharedPreferences(
					"SettingsPreferences", Context.MODE_PRIVATE);

			String requestEndpoint = DefaultValues.getEndPointForIP(
					DefaultValues.TRAFFIC_STATUS_REQUEST_WEB_SOCKET_END_POINT,
					settingsPreferences.getString("serverLocation",
							DefaultValues.WEB_SOCKET_SERVER_IP));


			Session session = null;
			try {

				System.out.println("Connecting to data federation "
						+ requestEndpoint);

				session = clientManager.connectToServer(clientEndpoint, cec,
						new URI(requestEndpoint));

				session.setMaxIdleTimeout(CONTINOUS_WEKSOCKET_SESION);

				System.out.println("The request for data federation "
						+ request);
				session.getBasicRemote().sendText(request);

				while (!stopThread) {
					Thread.sleep(DefaultValues.NOTIFICATION_SERVICE_PERIOD);
				}

				session.close(new CloseReason(new CloseCode() {

					@Override
					public int getCode() {
						return 1000;
					}
				}, "The user stopped the reasoning."));

			} catch (DeploymentException e) {
				// TODO Auto-generated catch block
				System.out
						.println("Unable to open the connection with data federation  Deployment exception");

				Intent intent = new Intent();
				intent.setAction(Execution.TRAVEL_STATUS_EVENT);
				intent.putExtra(Execution.TRAVEL_STATUS_EVENT_PAYLOAD,
						"FAULT: Unable to connect to data federation endpoint: "
								+ requestEndpoint);
				sendBroadcast(intent);

				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out
						.println("Unable to open the connection with data federation IO exception");
				e.printStackTrace();
				
				Intent intent = new Intent();
				intent.setAction(Execution.TRAVEL_STATUS_EVENT);
				intent.putExtra(Execution.TRAVEL_STATUS_EVENT_PAYLOAD,
						"FAULT: Unable to connect to data federation endpoint: "
								+ requestEndpoint);
				sendBroadcast(intent);
				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				System.out
						.println("Unable to open the connection with data federation URI sintax error");
				e.printStackTrace();
				
				Intent intent = new Intent();
				intent.setAction(Execution.TRAVEL_STATUS_EVENT);
				intent.putExtra(Execution.TRAVEL_STATUS_EVENT_PAYLOAD,
						"FAULT: Unable to connect to data federation endpoint: "
								+ requestEndpoint);
				sendBroadcast(intent);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				Intent intent = new Intent();
				intent.setAction(Execution.TRAVEL_STATUS_EVENT);
				intent.putExtra(Execution.TRAVEL_STATUS_EVENT_PAYLOAD,
						"FAULT: Unable to connect to data federation endpoint: "
								+ requestEndpoint);
				sendBroadcast(intent);
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			lastLocation = location;
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}
	}
}
