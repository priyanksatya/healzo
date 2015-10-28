package com.healzo.doc;




import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.healzo.net.NetworkTaskServer;
import com.healzo.net.Talkback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements Talkback{
	SharedPreferences spf;
	private String deviceToken;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spf=getSharedPreferences("authentication",MODE_PRIVATE);
		
        checkDeviceToken();
    }
	
	protected void checkDeviceToken() {
		// TODO Auto-generated method stub
		Log.v("DEV", "DEBUG..............");
		if(spf.getString("deviceToken", null)==null){
			Log.v("SPF", "IF..............");
			deviceToken = (String) ParseInstallation.getCurrentInstallation().get("deviceToken");
			if(deviceToken!=null&&deviceToken.length()!=0){
				//Remove progress dialogue
				pingServer();
				Log.v("deviceToken", "deviceToken");
			}
			else {
				Log.v("SPF", "ELSE..............");
				//TODO make a progress dialogue of app initializing if it doesn't exist otherwise continue
				ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
			        @Override
			        public void done(ParseException e) {
			        	if (e == null) {
				        	ParseInstallation parse = ParseInstallation.getCurrentInstallation();
				        	deviceToken = (String) parse.get("deviceToken");
				        	checkDeviceToken();
			        	} else {
//				        	Toast.makeText(getActivity(), "Error creating token"+e.getMessage(), Toast.LENGTH_SHORT).show();
			        		checkDeviceToken();
			        	}
			        	 
			        	
			        }
			    });
			}
		}else{
			Log.v("deviceToken-ELSE", ""+spf.getString("deviceToken", null));
			Runnable r= new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(2000);
						startActivity(new Intent(getApplicationContext(), LandingActivity.class));
						finish();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new Thread(r).start();
			
			
		}
		
	
	}

	private void pingServer() {
		// TODO Auto-generated method stub
		Log.v("SPF", "PING..............");
		if(isConnectionAvailable()){
			ArrayList<NameValuePair>namevaluepairs=new ArrayList<NameValuePair>();
			namevaluepairs.add(new BasicNameValuePair("mobile",spf.getString("mobileno", null)));
			namevaluepairs.add(new BasicNameValuePair("token",deviceToken));
			Log.v("deviceToken", "deviceToken- pingServer");
			new NetworkTaskServer(MainActivity.this,
					MainActivity.this, 
					namevaluepairs, 
					CabzoConstants.URL_SET_TOKEN, 
					"Intializing App...",
					CabzoConstants.KEY_Intializing).execute();
		}
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
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", null);
		dialog.show();
	}
	@Override
	public void Sucess(String result,String key) {
		// TODO Auto-generated method stub
		if(result!=null && result.trim().equals("success")){
			SharedPreferences.Editor spe=spf.edit();
			spe.putString("deviceToken",deviceToken);
			spe.commit();
			Log.v("App", "Intialized...");
			
			Runnable r= new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(2000);
						Intent intent= new Intent(MainActivity.this, LandingActivity.class);
						finish();
						startActivity(intent);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new Thread(r).start();
			
			}
	}
}
