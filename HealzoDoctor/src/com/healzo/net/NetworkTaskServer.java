package com.healzo.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.healzo.doc.CabzoConstants;

	
	public  class NetworkTaskServer extends AsyncTask<String, Void, String> {

		String URL;
		private String output;
		ProgressDialog pd;
		String message;
		String key;
		ArrayList<NameValuePair> namevaluepairs = new ArrayList<NameValuePair>();
		private Context c;
		private Talkback t;
		

		public NetworkTaskServer() {
			// TODO Auto-generated constructor stub
		}

		public NetworkTaskServer(Talkback t, Context c, ArrayList<NameValuePair> namevaluepairs,
				String URL,String message,String key) {
			// TODO Auto-generated constructor stub
			this.t=t;
			this.c=c;
			this.namevaluepairs = namevaluepairs;
			this.URL = URL;
			this.message=message;
			this.key=key;
			Log.v("Name value Pairs", namevaluepairs.toString());
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(!URL.equals(CabzoConstants.URL_SET_TOKEN)){
			pd = ProgressDialog.show(c, "Please Wait...", message);
			pd.setCancelable(false);
			}
		}
		


		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			InputStream is = null;

			try {
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(URL);
				try {
					httpPost.setEntity(new UrlEncodedFormEntity(namevaluepairs));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					cancel(true);
					e1.printStackTrace();
				}
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();

				cancel(true);
			} catch (HttpHostConnectException e) {
				// TODO: handle exception
				e.printStackTrace();

				cancel(true);
			}

			catch (IOException e) {
				if (pd.isShowing()) {
					pd.dismiss();
				}
				e.printStackTrace();

				cancel(true);
			} catch (Exception e) {
				// TODO: handle exception
				if (pd.isShowing()) {
					pd.dismiss();
				}
				e.printStackTrace();
				cancel(true);
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
				Log.v("Result", output);
/*				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(context, output, Toast.LENGTH_LONG).show();
					}
				});*/
			} catch (Exception e) {
				if (pd.isShowing()) {
					pd.dismiss();
				}
				Log.e("Buffer Error", "Error converting result " + e.toString());
				cancel(true);
			}
			return output;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if(!URL.equals(CabzoConstants.URL_SET_TOKEN)){

			if (pd.isShowing()) {
				pd.dismiss();
			}
			}
			
				t.Sucess(result,this.key);
			
		}

	}
