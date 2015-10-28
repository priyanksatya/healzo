package com.healzo.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

	
	public  class NetworkService extends AsyncTask<String, Void, String> {

		String URL;
		private String output;
		ProgressDialog pd;
		String key;
		private Context c;
		private Talkback t;
		private InputStream is;
		

		public NetworkService() {
			// TODO Auto-generated constructor stub
		}
		
		public NetworkService(Talkback t, Context c,String URL,String key) {
			// TODO Auto-generated constructor stub
			this.t=t;
			this.c=c;
			this.URL = URL;
			this.key=key;
			Log.v("URL", URL);
			
			
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			
			if(c!=null){
				pd = new ProgressDialog(this.c);
			    pd.setMessage("connecting...");
			    pd.show();
				pd.setCancelable(false);
			}
			super.onPreExecute();
		}
		


		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return getRespUrl(this.URL);
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if(c!=null){
			if (pd.isShowing()) {
				pd.dismiss();
			}
			}
			t.Sucess(result,this.key);
			
		}
		
		public String getRespUrl(String url) {
	    	String result = "";
	   	    // Making HTTP request
	        try {
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpGet httpget = new HttpGet(url);
	            httpget.addHeader("accept","application/json");
	            HttpResponse httpResponse = httpClient.execute(httpget);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            is = httpEntity.getContent(); 
	            
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	         
	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            is.close();
	            result = sb.toString();
	            Log.v("SubString", result.substring(2034, 2046));
	        } catch (Exception e) {
	            Log.e("Buffer Error", "Error converting result " + e.toString());
	        }
	        
	        return result;
	 
	    }

	}

