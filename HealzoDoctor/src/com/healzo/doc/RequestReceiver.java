package com.healzo.doc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.healzo.helper.LocationUtils;
import com.healzo.net.NetworkTaskServer;
import com.healzo.net.Talkback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
/**
* This is a service class aims to track the current location of the driver and check's the trip request of driver.
* @author Praveen Kumar
* @version 1.0
*/

public class RequestReceiver extends Service implements Talkback,LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	
	SharedPreferences spf;
	DBHelper db;
	private LocationRequest mLocationRequest;
	// Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    // Declaring a Location Manager
  	protected LocationManager locationManager;
  	// Handle to SharedPreferences for this app
    SharedPreferences mPrefs;
    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
     /*
      * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
      * method handleRequestSuccess of LocationUpdateReceiver.
      *
      */
     boolean mUpdatesRequested = false;
	public static boolean notification_status=true;
	private static final String TAG = "My Service";
	//private int noitification_counter;
	SharedPreferences location_details;
	HttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost;

	TelephonyManager telephonyManager;
	String lat, lon, state = "", country = "",imei;
	java.sql.Timestamp mobiletime;
	
	int day,hour;
	//public static int timer_pass_list;
	//public static  int timer_wait_driver;
	Handler handler= new Handler();
	public static String driver_latlong;
	// flag for network status
 	boolean isNetworkEnabled = false;
 	// flag for GPS status
 	boolean canGetLocation = false;
 	 // flag for GPS status
 	boolean isGPSEnabled = false;
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();

		
		// Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
		
		
		spf=getSharedPreferences("authentication",MODE_PRIVATE);
		
		location_details=getSharedPreferences("location", 0);
		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		httppost= new HttpPost(getString(R.string.ipaddress_port)+"doctor_update_latlng.jsp");//Mumbai Taxi Locations  
        handler.postDelayed(sendUpdatesToUI, 1000);
		
        checkDeviceToken();
	}
	public int onStartCommand(Intent intent, int flags, int startId) {
	    setUpLocationClientIfNeeded();
	    if(!mLocationClient.isConnected() || !mLocationClient.isConnecting())
	    mLocationClient.connect();
	    return START_STICKY;

	}
	
	/*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    private void setUpLocationClientIfNeeded()
    {
    	if(mLocationClient == null) 
            mLocationClient = new LocationClient(this, this, this);
    }
	
    public void getLocation() {
    	
		if (isProvidersEnabled()) {
			// If Google Play Services is available
			if (servicesConnected()) {
				Log.v("Service Connected", "Yes");
				if(mLocationClient.isConnected()){
					Log.v("mLocationClient", "connected");
				// Get the current location
				sendLocation(mLocationClient.getLastLocation());
				}
			}
				
				
		} else {
			//showSettingsAlert();
		}
    }
    public void sendLocation(Location location)
    {
		if(location!=null){
			lat = Double.toString(location.getLatitude());
	    	lon  = Double.toString(location.getLongitude());
	    	Editor location_edit=location_details.edit();
			location_edit.putString("lat", lat.toString());
			location_edit.putString("lon", lon.toString());
			location_edit.commit();
		    driver_latlong=lat+","+lon;
			if(isConnectionAvailable()){
	    		if(location.getLatitude()!=0.0)
	            	new NetworkLatLong(driver_latlong).execute();
	        }	
		} 
    }
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
         /*   Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
               // errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG); needddahadhlaishdilashyduhasldhasd
*/            }
            return false;
        }
	private boolean isProvidersEnabled() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled) {
			// no network provider is enabled
			this.canGetLocation = false;
		}
		else
		this.canGetLocation = true;
		
		return canGetLocation;
	}
	@Override
	public void onDestroy() {
	    super.onDestroy();
	}
	
	 private Runnable sendUpdatesToUI = new Runnable() {
	        public void run() {
	        	
	        	if(isConnectionAvailable()){
	        		Log.v("Connection Available", "Yes");
                	getLocation();
	 	        }
	        	handler.postDelayed(this, 60000);
	        }
	 };
	private String s;
	private String deviceToken;
	
	protected void setalaram() {
			// TODO Auto-generated method stub
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			s = sd.format(new Date());
			db=new DBHelper(RequestReceiver.this);
			Date dt = new Date();
			int h = dt.getHours();
			int m = dt.getMinutes();
			String dd = (h + "");
			String da = "0";
			if (dd.equals(da)) {
				try{
					db.deleteRef();
					db.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			} else {
				System.out.println("no matches found");
			}

		}
		protected void checkDeviceToken() {
			// TODO Auto-generated method stub
			Log.v("DEV", "DEBUG..............");
			if(spf.getString("deviceToken", null)==null){
				deviceToken = (String) ParseInstallation.getCurrentInstallation().get("deviceToken");
				if(deviceToken!=null&&deviceToken.length()!=0){
					//Remove progress dialogue
					pingServer();
					Log.v("deviceToken", "deviceToken");
				}
				else {
					//TODO make a progress dialogue of app initializing if it doesn't exist otherwise continue
					ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
				        @Override
				        public void done(ParseException e) {
				        	if (e == null) {
					        	ParseInstallation parse = ParseInstallation.getCurrentInstallation();
					        	deviceToken = (String) parse.get("deviceToken");
					        	checkDeviceToken();
				        	} else {
//					        	Toast.makeText(getActivity(), "Error creating token"+e.getMessage(), Toast.LENGTH_SHORT).show();
				        		checkDeviceToken();
				        	}
				        	 
				        	
				        }
				    });
				}
			}
		
		}
		private void pingServer() {
			// TODO Auto-generated method stub
			
			if(isConnectionAvailable()){
				ArrayList<NameValuePair>namevaluepairs=new ArrayList<NameValuePair>();
				namevaluepairs.add(new BasicNameValuePair("mobile",spf.getString("mobileno", null)));
				namevaluepairs.add(new BasicNameValuePair("token",deviceToken));
				Log.v("deviceToken", "deviceToken- pingServer");
				new NetworkTaskServer(RequestReceiver.this,
						RequestReceiver.this, 
						namevaluepairs, 
						CabzoConstants.URL_SET_TOKEN, 
						"Intializing App...",
						CabzoConstants.KEY_Intializing).execute();
			}
		}
		@Override
		public void onStart(Intent intent, int startid) {

		   //handler.removeCallbacks(sendUpdatesToUI);
		}
	protected boolean isConnectionAvailable() {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}
	
	public void displayNoInternet() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(RequestReceiver.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", null);
		dialog.show();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}	

	public class NetworkLatLong extends AsyncTask<Void, Void, Void>{
		//ProgressDialog pd;
		String search_latlong;
		String URL = "";
		String lname;
		private String URLKey;
		NetworkLatLong(String search_latlong) {
			this.search_latlong = search_latlong;
			URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
					+ this.search_latlong + "&sensor=true";
			URL = URL.replaceAll(" ", "%20");
			this.URLKey = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
					+ this.search_latlong + "&sensor=true"+"&key=AIzaSyD5-BLM0iTWiTVmbbN1XxCzeGSQmkp3WPs";
			this.URLKey = URLKey.replaceAll(" ", "%20");

		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {

				Log.v("lat---long", lat + "," + lon);
				Calendar calendar = Calendar.getInstance();
				day = calendar.get(calendar.DAY_OF_WEEK);
				hour = calendar.get(calendar.HOUR_OF_DAY);
				java.util.Date now = calendar.getTime();
				java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
						now.getTime());
				mobiletime = currentTimestamp;

					GlobalMethods globalMethods = new GlobalMethods(getApplicationContext());
					String tempLname = globalMethods.getLNameFromLatLong(globalMethods.SendHTTPPost(URL));
					if(tempLname == "" ){
						lname = globalMethods.getLNameFromLatLong(globalMethods.SendHTTPPost(URLKey));
					} else {
						lname = tempLname;
					}
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
				nameValuePairs.add(new BasicNameValuePair("doctor_mobile", spf.getString("mobileno", null)));
				nameValuePairs.add(new BasicNameValuePair("lat", lat.toString()));
				nameValuePairs.add(new BasicNameValuePair("long", lon.toString()));
				nameValuePairs.add(new BasicNameValuePair("address", lname + ""));
				nameValuePairs.add(new BasicNameValuePair("datetime",mobiletime + ""));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				Log.v("location name", lname.toString());
				Log.v("Mobile", spf.getString("mobileno", null));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				response.getEntity().consumeContent();
				Log.d("response", "response" + response);

			} catch (Exception e){
				e.printStackTrace();
				cancel(true);
			}

			return null;
		}
		
		
	}

	@Override
	public void Sucess(String result,String key) {
		// TODO Auto-generated method stub
		if(result!=null && result.trim().equals("success")){
			SharedPreferences.Editor spe=spf.edit();
			spe.putString("deviceToken",deviceToken);
			spe.commit();
			Log.v("App", "Intialized...");
			}
	}
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

}

