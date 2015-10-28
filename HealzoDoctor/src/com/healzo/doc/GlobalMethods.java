package com.healzo.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class GlobalMethods{
    Context mContext;

    // constructor
    public GlobalMethods(Context context){
        this.mContext = context;
    }

	public HttpEntity SendHTTPPost(String url) throws ClientProtocolException, IOException{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			return httpEntity;
	}

	public String getLNameFromLatLong(HttpEntity entity) throws IllegalStateException, IOException, JSONException {
		String lname = "";
		JSONArray predictions;
		JSONObject jobj = getJObj(entity);
		if (!jobj.get("status").equals("OK")) {
			return lname;
		}
		predictions = jobj.getJSONArray("results");
		for (int i = 0; i < predictions.length(); i++) {
			JSONObject c = predictions.getJSONObject(0);
			lname = c.getString("formatted_address");
		}
		return lname;
	}

	public String getLatLongFromName(HttpEntity entity) throws IllegalStateException, IOException, JSONException{
		JSONObject jobj = getJObj(entity);
		if(!jobj.get("status").equals("OK")){
			return "";
		}
		double lat = ((JSONArray)jobj.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
		double lon = ((JSONArray)jobj.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
		return lat+","+lon;
	}
	private JSONObject getJObj(HttpEntity entity) throws IllegalStateException, IOException, JSONException{
		InputStream is = entity.getContent();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(is, "iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		String json = sb.toString();
		JSONObject jobj = new JSONObject(json);
		return jobj;
	}
}