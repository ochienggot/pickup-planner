package com.nicholasgot.citypulse.androidapp.common;

public class DefaultValues {



	// CTBAV
	//public static final String WEB_SOCKET_SERVER_IP = "192.168.100.30";

	// NUIG
	// public static final String WEB_SOCKET_SERVER_IP = "140.203.155.76";

	// SURREY
	public static final String WEB_SOCKET_SERVER_IP = "131.227.92.55";

//	public static final String REASONING_REQUEST_WEB_SOCKET_END_POINT = "ws://"
//			+ "SERVER_IP" + ":" + "8005"
//			+ "/websockets/reasoning_request";

	public static final String REASONING_REQUEST_WEB_SOCKET_END_POINT = "ws://"
			+ "SERVER_IP" + ":" + "8018" // For Stockholm GDI
			+ "/websockets/reasoning_request";

	public static final String CONTEXTUAL_EVENTS_REQUEST_WEB_SOCKET_END_POINT = "ws://"
			+ "SERVER_IP"
			+ ":"
			+ "8005"
			+ "/websockets/contextual_events_request";
	
	
	public static final String TRAFFIC_STATUS_REQUEST_WEB_SOCKET_END_POINT = "ws://"
			+ "SERVER_IP"
			+ ":"
			+ "8002"
			+ "/";

	public static final int TRAVEL_PLANNER_CONSTRAINTS_REQUEST_CODE = 999;

	public static final int PARKING_PLACE_PLANNER_CONSTRAINTS_REQUEST_CODE = 888;

	public static final String CAR_TRANSPORTATION_TYPE = "CAR";

	public static final String WALK_TRANSPORTATION_TYPE = "WALK";

	public static final String BICYCLE_TRANSPORTATION_TYPE = "BICYCLE";


	public static final Integer NOTIFICATION_SERVICE_PERIOD = 10000;

	public static final String CLOSE_ACTIVITY_MESSAGE = "close activity message";

	public static final String EVENT_ALERT_MESSAGE = "event alert message";
	
	public static final String ERROR_MESSAGE = "error message";

	public static final String EVENT_ALERT_MESSAGE_PAYLOAD = "event alert message payload";
	
	public static final String ERROR_MESSAGE_PAYLOAD = "error message payload";

	public static final String COMMMAND_BUNDLE = "command bundle";

	public static final String COMMAND_GO_TO_PARKING_RECOMANDATION = "go to parking recomandation";

	public static final String COMMAND_GO_TO_TRAVEL_RECOMANDATION = "go to TRAVEL recomandation";
	
	public static String getEndPointForIP(String endPointBase,String IP){
		
		
		return endPointBase.replace("SERVER_IP", IP);
	}

}
