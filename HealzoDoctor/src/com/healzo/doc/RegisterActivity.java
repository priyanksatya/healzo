package com.healzo.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class RegisterActivity extends Activity {

	public String output;
	private EditText mobile;
	private EditText input;
	String value,mobi;
	private LinearLayout view;
	MixpanelAPI mixpanel; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, getString(R.string.MIXPANEL_TOKEN));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		input = new EditText(this);
		mobile = new EditText(this);
		if(isConnectionAvailable()){
			//http://115.96.0.179:8080/smarttaxi/css/SPPS.apk
			checkSharedPreference();         
			
		} else {
			displayNoInternet();
			
		}
	}
	

	private void displayNoInternet() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                Intent intent = getIntent();
	                finish();
	                startActivity(intent);
	                
	           }
	       });
		dialog.show();
	}
	private boolean isConnectionAvailable() {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}
	 
		private void checkSharedPreference() {
			// TODO Auto-generated method stub
			SharedPreferences spf=getSharedPreferences("authentication",MODE_PRIVATE);
			if(spf.getString("validation", null)!=null && spf.getString("driver-id", null)!=null){
				if(spf.getString("driver-id", null)!=null){
					Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			}
			else{
				final AlertDialog.Builder alertDialog=new AlertDialog.Builder(RegisterActivity.this);
				alertDialog.setTitle("Authentication");
				//alertDialog.setMessage("Please Enter Driver ID and Mobile Number");
				alertDialog.setMessage("Please Enter Mobile Number and Password");
				view = new  LinearLayout(RegisterActivity.this);
				view.setOrientation(LinearLayout.VERTICAL);
				input.setHint("Password");
				mobile.setHint("Mobile Number");
				view.addView(mobile);
				view.addView(input);
				
				alertDialog.setView(view);
				input.setInputType(InputType.TYPE_CLASS_PHONE);
				mobile.setInputType(InputType.TYPE_CLASS_PHONE);
				alertDialog.setCancelable(false);
				alertDialog.setPositiveButton("CONTINUE",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								value=input.getText().toString();
								mobi=mobile.getText().toString();
								if(value.length()!=0 && mobi.length()!=0){
									if(isConnectionAvailable())
										new NetworkTaskRegister(value, mobi).execute();
									else
										displayNoInternet();
								
								}
								
							}
						});
				alertDialog.setNegativeButton("CANCEL",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
				    
				alertDialog.show();
			}
		}
		public class NetworkTaskRegister extends AsyncTask<Void, Void, Void>{

			String URL;
			 public NetworkTaskRegister(String driver_id,String driver_mobile) {
				super();
				
				this.URL=getResources().getString(R.string.ipaddress_port)+"doctor_login.jsp?doctor_mobile="+driver_mobile+"&password="+driver_id;
				Log.v("URL Register", URL);
			 }

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				InputStream is = null;

				try {
					// defaultHttpClient
					
					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(URL);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					HttpEntity httpEntity = httpResponse.getEntity();
					is = httpEntity.getContent();
				} catch (SocketTimeoutException ste) {
					//ste.printStackTrace();
					Toast.makeText(RegisterActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					e.printStackTrace();
				} 

				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "iso-8859-1"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					output = sb.toString();
					Log.v("output",output);
				} catch (Exception e) {
					Log.e("Buffer Error", "Error converting result " + e.toString());
				}

				return null;
			}
			 @Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub 
				super.onPostExecute(result);
				
				if(output!=null){
					Log.v("RESULE", output);
					
				if(output.trim().equals("success")){
					SharedPreferences spf=getSharedPreferences("authentication", MODE_PRIVATE);
					SharedPreferences.Editor spe=spf.edit();
					spe.putString("validation","true");
					spe.putString("driver-id", value.trim());
					spe.putString("mobileno", mobi.trim());
					//spe.putInt("notification_count", 1);
					spe.commit();
					Intent intent_service=new Intent(RegisterActivity.this, RequestReceiver.class);
					startService(intent_service);
					
					Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
					startActivity(intent);
			        finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "Please Enter Your valid Doctor ID and Mobile Number to proceed", Toast.LENGTH_LONG).show();
					view.removeAllViews();
					checkSharedPreference();
					
					Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
					startActivity(intent);
			        finish();
					
				}
				}
			}

			
		}
		
}
