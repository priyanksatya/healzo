package com.healzo.net;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.healzo.doc.BookingsList;
import com.healzo.doc.LandingActivity;
import com.healzo.doc.R;
import com.healzo.doc.RequestReceiver;

public class UpdateSync extends AsyncTask<String, Void, Void> {
	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

	private String Error;
	private Context context;
	public static int count_down_circleTime;
	public static int count_down_acceptTime;
	public static int noshow;
	public static boolean noty_Available=false;
	

	public UpdateSync(Context context, ArrayList<NameValuePair> nameValuePairs) {
		this.context = context;
		this.nameValuePairs = nameValuePairs;
		
		
	}
	
	@Override
	protected void onPreExecute() {
	

	}

	@Override
	protected void onPostExecute(Void unused) {

		super.onPostExecute(unused);
		
		Log.v("No Request","No Request");
		
	    SharedPreferences spf=context.getSharedPreferences("globalparameters", 0);
	    if(spf.getInt("driver_waiting_timer", 0)!=0 && spf.getInt("driver_accept_trip_timer", 0)!=0){
	    	Log.v("global values", spf.getInt("driver_waiting_timer", 0)+"---"+spf.getInt("driver_accept_trip_timer", 0)+"----"+spf.getInt("driver_accept_trip_timer", 0));
	    	count_down_circleTime=spf.getInt("driver_waiting_timer", 0);
	    	count_down_acceptTime=spf.getInt("driver_accept_trip_timer", 0);
	    	noshow=spf.getInt("noshow_driver_time", 0);
	    }
	 if(ServerToDB.nodes_count!=0){
		 Log.v("Request count","Request count");
		 if(ServerToDB.list_trip_final.size()!=0){
	/*		Intent intent = new Intent(context, BookingsList.class);
			 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 context.startActivity(intent);*/
			 Log.v("Request","Request");
			 if(LandingActivity.isActivityForeground()){
				 Intent intent=new Intent(context, BookingsList.class);
				 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				 context.startActivity(intent);
				 RequestReceiver.notification_status=false;
			 }
			 else
			 {
				 
				 ArrayList<String> runningactivities = new ArrayList<String>();
			

				 String booking_list= context.getPackageName()+".BookingsList";
				 if(runningactivities.contains("ComponentInfo{com.app/"+booking_list+"}")==true){
				     Toast.makeText(context,"Activity is in foreground, active",1000).show(); 
				     Log.v("Update Sync", "Booking list in in foreground");
				 }
				 else{
					 //Log.v("Update Sync", "Booking list in in background");
					 
					 if(BookingsList.notify_flag_accept){
						 createNotification(context);
						 }
					 //Log.v("Update Sync", "Booking list Notificaton Created");
				 } 
				 
				 
			 }
			 
			 
			
			
		 }
	 }

	}

	private void createNotification(Context ctx) {
		// TODO Auto-generated method stub
		long[] vibrate = {0, 100, 200, 300,400};
		noty_Available=true;
		Intent intent = new Intent(ctx, BookingsList.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    // Build notification
	    Notification notification = new NotificationCompat.Builder(ctx)
	        .setSmallIcon(R.drawable.passenger_pick)
	        .setContentTitle("Click to View Booking List")
	        .setContentText("notification")
	        .setVibrate(vibrate)
	        .setContentIntent(pIntent).build();
	    
	    NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

	    // Hide the notification after its selected
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    //notification.flags |=Notification.FLAG_NO_CLEAR;
	    notification.flags |=Notification.FLAG_ONGOING_EVENT;
	    notification.defaults |= Notification.DEFAULT_SOUND;
	    notificationManager.notify(100, notification);
	}

	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub

		int responcecode = 0;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;

		//--------------------------------------------------------------------------------------------------------//
		if(params[0].toString().equalsIgnoreCase("driver"))
        {
		
		httppost = new HttpPost(context.getString(R.string.ipaddress_port)+"android_driver_trip_req.jsp?");
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			//System.out.println(nameValuePairs);
			} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		try {
			
			HttpResponse response = httpclient.execute(httppost);
			int responseCode = response.getStatusLine().getStatusCode();
			switch(responseCode) {
			case 200:
			HttpEntity entity = response.getEntity();
			    if(entity != null) {
			        String responseBody = EntityUtils.toString(entity);
			    	ServerToDB sd= new ServerToDB(context);
			    	try {
						sd.parseXML(responseBody);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			    break;
			}

		} catch (Exception e) {
			Log.e("EXXXXXXXXXXXXX", e.toString());
			Error = e.getMessage();
			cancel(true);
		}
		
	
        }
		return null;
	}
}
