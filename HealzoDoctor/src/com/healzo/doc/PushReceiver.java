package com.healzo.doc;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.sax.StartElementListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.healzo.net.Talkback;
import com.parse.ParsePushBroadcastReceiver;

public class PushReceiver extends ParsePushBroadcastReceiver {
	
	public static int count_down_circleTime=120;
	public static int count_down_acceptTime=120;
	public static int noshow;
	public static CountDownTimer timer;
	private MediaPlayer m;	
	Context context;
	Intent current_intent;
	
	private boolean isConnectionAvailable() {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}
	
	@Override
    public void onPushOpen(Context context, Intent intent) {
    	this.context=context;
    	JSONObject json=null;
    	Intent i = new Intent(context, RegisterActivity.class);
    	Log.v("isConnectionAvailable()",isConnectionAvailable()+"");
    	//if(isConnectionAvailable()){
    	try {
			json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			System.out.println("-----------JSON onPushOpen----------"+json.toString());
			Log.v("json response", json.toString());
			
			if(json.getString("type")!=null && json.getString("type").equals("newrequest")){
				
				i = new Intent(context, BookingsList.class);
				i.putExtra("json", json.toString());
				Log.v("PUSH OPEN", "new request");
			}
			else if(json.getString("type")!=null && json.getString("type").equals("future_trip_selected")){
				
				i = new Intent(context, LandingActivity.class);
				i.putExtra("future_trip_selected", true);
				i.putExtra("booking_id", json.getString("booking_id"));
				i.putExtras(intent.getExtras());
				Log.v("future_trip_selected", json.getString("booking_id"));
				
			}
			else {
				i = new Intent(context, LandingActivity.class);
				Log.v("PUSH OPEN", "else");
			}
		
        } catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(i);
    	
    }
    
    @Override
    protected void onPushReceive(Context context, Intent intent) {
    	// TODO Auto-generated method stub
    	current_intent=intent;
    	this.context=context;
    	
    	super.onPushReceive(context, intent);
    	
    	SharedPreferences spf=context.getSharedPreferences("globalparameters", 0);
	    if(spf.getInt("driver_waiting_timer", 0)!=0 && spf.getInt("driver_accept_trip_timer", 0)!=0){
	    	Log.v("global values", spf.getInt("driver_waiting_timer", 0)+"---"+spf.getInt("driver_accept_trip_timer", 0)+"----"+spf.getInt("driver_accept_trip_timer", 0));
	    	count_down_circleTime=spf.getInt("driver_waiting_timer", 0);
	    	count_down_acceptTime=spf.getInt("driver_accept_trip_timer", 0);
	    	//count_down_acceptTime=30;
	    	noshow=spf.getInt("noshow_driver_time", 0);
	    }
    	
        try {
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			System.out.println("-----------JSON onPushReceive----------"+json.toString());
			Log.v("json response", json.toString());
					
		} catch (JSONException e) {
			e.printStackTrace();
		}
 
    }
	
	private void parkingCountDownTimer() {
		timer = new CountDownTimer(count_down_acceptTime * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				try {
					 count_down_acceptTime = (int) (millisUntilFinished / 1000);
					
				} catch (Exception e) {
					// Toast.makeText(getApplicationContext(),
					// "Exception in timer", 1000).show();
				}

			}

			public void onFinish() {
				NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(1);
			}
		}.start();
	}
	public static void destroyTimer(){
		
		if(timer!=null){
			timer.cancel();
		}
		
	}

}