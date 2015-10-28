package com.healzo.doc;

public class CabzoConstants {
	
	/* CONFIGURATION OF WEBSERVICE DETAILS */
	
	public static String IP_ADDRESS="103.247.96.117"; // test server ip
	public static String PORT="9090"; // test server port
	public static String CONTEXT_NAME="Healzo-Web-Services"; // test server context_name

	//public static String IP_ADDRESS="123.252.130.202"; // test server ip
	//public static String PORT="8080"; // test server port
	//public static String CONTEXT_NAME="smartcabmsg1.1"; // test server context_name
	
	public static String URL_SET_TOKEN="http://"+IP_ADDRESS+":"+PORT+"/"+CONTEXT_NAME+"/"+"save_token.jsp";
	public static String URL_PUSH_SERVICE="http://"+IP_ADDRESS+":"+PORT+"/"+CONTEXT_NAME+"/"+"android_push.jsp";
	
	
	public static String URL_FUTURE_BOOKING="http://"+IP_ADDRESS+":"+PORT+"/"+CONTEXT_NAME+"/"+"android_passenger_future_all.jsp?";
	public static String URL_ACCPET_FUTURE_BOOKING="http://"+IP_ADDRESS+":"+PORT+"/"+CONTEXT_NAME+"/"+"android_driver_future_response.jsp?";
	public static String URL_CANCEL_FUTURE_BOOKING="http://"+IP_ADDRESS+":"+PORT+"/"+CONTEXT_NAME+"/"+"android_driver_future_response_cancel.jsp?";
	
	
	//http://103.247.96.117:9090/Cabzo-Web-Services/android_update_driver_status.jsp? 
	
	
	// Updates....
	public static String URL_DRIVER_STATUS="http://"+IP_ADDRESS+":"+PORT+"/"+CONTEXT_NAME+"/"+"doctor_update_status.jsp?";
	
	
	
	public static String KEY_Intializing="KEY_Intializing";
	public static String KEY_BOOKING_ACCEPT_TRIP="KEY_BOOKING_ACCEPT_TRIP";
	public static String KEY_BOOKING_IF_SELECTED="KEY_BOOKING_IF_SELECTED";
	public static String KEY_BOOKING_IF_PASSENGER_CONFIRMED="KEY_BOOKING_IF_PASSENGER_CONFIRMED";
	public static String KEY_BOOKING_IF_END_TRIP="KEY_BOOKING_IF_END_TRIP";
	public static String KEY_BOOKING_SET_DATA="KEY_BOOKING_SET_DATA";
	
	public static String KEY_CHECK_MODE_SERVICE="KEY_CHECK_MODE_SERVICE";
	public static String KEY_GLOBAL_PARAMETERS_SERVICE="KEY_GLOBAL_PARAMETERS_SERVICE";
	public static String KEY_FUTURE_BOOKING="KEY_FUTURE_BOOKING";
	public static String KEY_ACCPET_FUTURE_BOOKING="KEY_ACCPET_FUTURE_BOOKING";
	public static String KEY_CANCEL_FUTURE_BOOKING="KEY_CANCEL_FUTURE_BOOKING";
	public static String KEY_DRIVER_STATUS="KEY_DRIVER_STATUS";
	
	
	public static String STATUS_READY_TO_ACCEPT= "1";
	public static String STATUS_DRIVER_ON_HOLD= "2";
	public static String STATUS_DRIVER_ACCPETED_TRIP= "3";
	public static String STATUS_DRIVER_GOT_SELECTED= "4";
	public static String STATUS_TRIP_STARTED= "5";
	public static String STATUS_MANUALCHANGED= "6";
	

}
