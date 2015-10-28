package com.healzo.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.healzo.mapsutil.Navigator;

public class GetMapActivity extends FragmentActivity 
{
	private GoogleMap myMap;
	
    Polyline line;
    Context context;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
	private GPSTracker gsp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_map);
		
		/*String start=getIntent().getExtras().getString("from_loc");
		String end =getIntent().getExtras().getString("to_loc");
		TextView from=(TextView)findViewById(R.id.from_location);
		TextView to=(TextView)findViewById(R.id.to_location);
		
		from.setText(start);
		to.setText(end);
		*/
		//String start="Huda colony, Chandanagar, Hyderabad";
		//String end ="Miyapur, Hyderabad";
				
		
		context = GetMapActivity.this;
		
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment)fragment;
        myMap = mapFragment.getMap();
       
       /* Navigator nav= new Navigator(myMap, PassengerConfirmed.from_location_english, PassengerConfirmed.to_location_english);
        nav.findDirections(true);
        */
        /*gsp= new GPSTracker(GetMapActivity.this);
        try{
        	myMap.setMyLocationEnabled(true);
	 	}
	     catch(Exception e){
	    	 //Toast.makeText(getApplicationContext(), "Please update google play services...", Toast.LENGTH_LONG).show();
	     }
        if(gsp.canGetLocation()){
			
        	double latitude = gsp.getLatitude();
        	double longitude = gsp.getLongitude();
        	//String mapPath = "https://maps.google.com/?ll=37.0625,-95.677068&spn=29.301969,56.513672&t=h&z=4";

        	try{
        		LatLng coordinate = new LatLng( gsp.getLatitude(), gsp.getLongitude());
            	CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            	myMap.animateCamera(yourLocation);
            
        }
   	     catch(Exception e){
   	    	// Toast.makeText(getApplicationContext(), "Please update google play services...", Toast.LENGTH_LONG).show();
   	     }
        	
        }
        String urlTopass = makeURL(gsp.getLatitude(),gsp.getLongitude(),end);
        Log.v("URL", urlTopass);
        new connectAsyncTask(urlTopass).execute();*/
        
	}
	 private class connectAsyncTask extends AsyncTask<Void, Void, String> {
	        private ProgressDialog progressDialog;
	        String url;

	        connectAsyncTask(String urlPass) {
	            url = urlPass;
	        }

	        @Override
	        protected void onPreExecute() {
	            // TODO Auto-generated method stub
	            super.onPreExecute();
	            progressDialog = new ProgressDialog(GetMapActivity.this);
	            progressDialog.setMessage("Fetching route, Please wait...");
	            progressDialog.setIndeterminate(true);
	            progressDialog.show();
	        }

	        @Override
	        protected String doInBackground(Void... params) {
	            JSONParser jParser = new JSONParser();
	            String json = jParser.getJSONFromUrl(url);
	            return json;
	        }

	        @Override
	        protected void onPostExecute(String result) {
	            super.onPostExecute(result);
	            progressDialog.hide();
	            if (result != null) {
	                drawPath(result);
	            }
	        }
	    }

	    public String makeURL(double sourcelat, double sourcelog, String endlocation) {
	    	
	        StringBuilder urlString = new StringBuilder();
	        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
	        urlString.append("?origin=");// from
	        urlString.append(sourcelat);
	        urlString.append(",");
	        urlString.append(sourcelog);
	        urlString.append("&destination=");// to
	        urlString.append(endlocation.replace(" ", "%20"));
	        urlString.append("&sensor=false&mode=driving&alternatives=true");
	        return urlString.toString();
	    }

	    public class JSONParser {

	        InputStream is = null;
	        JSONObject jObj = null;
	        String json = "";

	        // constructor
	        public JSONParser() {
	        }

	        public String getJSONFromUrl(String url) {

	            // Making HTTP request
	            try {
	                // defaultHttpClient
	                DefaultHttpClient httpClient = new DefaultHttpClient();
	                HttpPost httpPost = new HttpPost(url);

	                HttpResponse httpResponse = httpClient.execute(httpPost);
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
	                BufferedReader reader = new BufferedReader(
	                        new InputStreamReader(is, "iso-8859-1"), 8);
	                StringBuilder sb = new StringBuilder();
	                String line = null;
	                while ((line = reader.readLine()) != null) {
	                    sb.append(line + "\n");
	                }

	                json = sb.toString();
	                Log.v("", json);
	                is.close();
	            } catch (Exception e) {
	                Log.e("Buffer Error", "Error converting result " + e.toString());
	            }
	            return json;

	        }
	    }

	    public void drawPath(String result) {
	        if (line != null) {
	            myMap.clear();
	        }
	        try {
	            // Tranform the string into a json object
	            final JSONObject json = new JSONObject(result);
	            JSONArray routeArray = json.getJSONArray("routes");
	            JSONObject routes = routeArray.getJSONObject(0);
	            
	            JSONArray legs= routes.getJSONArray("legs");
	            JSONObject leg = legs.getJSONObject(0);
	            
	            
	            JSONObject startJson=leg.getJSONObject("start_location");
	            JSONObject endJson=leg.getJSONObject("end_location");
	            
	            double slat=startJson.getDouble("lat");
	            double slng=startJson.getDouble("lng");
	            double elat=endJson.getDouble("lat");
	            double elng=endJson.getDouble("lng");
	            
	            myMap.addMarker(new MarkerOptions().position(new LatLng(slat, slng)).icon(
		                BitmapDescriptorFactory.fromResource(R.drawable.pin)));
		        myMap.addMarker(new MarkerOptions().position(new LatLng(elat, elng)).icon(
		                BitmapDescriptorFactory.fromResource(R.drawable.pin)));
		        
	            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
	            String encodedString = overviewPolylines.getString("points");
	            List<LatLng> list = decodePoly(encodedString);
	            PolylineOptions options = new PolylineOptions().width(2).color(Color.BLUE).geodesic(true);
                
	            for (int z = 0; z < list.size() - 1; z++) {
	                LatLng src = list.get(z);
	                LatLng dest = list.get(z + 1);
	                
	                options.add(new LatLng(src.latitude, src.longitude),
	                                new LatLng(dest.latitude, dest.longitude));
	                
	                /*line = myMap.addPolyline(new PolylineOptions()
	                        .add(new LatLng(src.latitude, src.longitude),
	                                new LatLng(dest.latitude, dest.longitude))
	                        .width(2).color(Color.BLUE).geodesic(true));
	                */
	                builder.include(list.get(z));					
	    		}
	            line=myMap.addPolyline(options);

	            
		        
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    private List<LatLng> decodePoly(String encoded) {

	        List<LatLng> poly = new ArrayList<LatLng>();
	        int index = 0, len = encoded.length();
	        int lat = 0, lng = 0;

	        while (index < len) {
	            int b, shift = 0, result = 0;
	            do {
	                b = encoded.charAt(index++) - 63;
	                result |= (b & 0x1f) << shift;
	                shift += 5;
	            } while (b >= 0x20);
	            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	            lat += dlat;

	            shift = 0;
	            result = 0;
	            do {
	                b = encoded.charAt(index++) - 63;
	                result |= (b & 0x1f) << shift;
	                shift += 5;
	            } while (b >= 0x20);
	            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	            lng += dlng;

	            LatLng p = new LatLng((((double) lat / 1E5)),
	                    (((double) lng / 1E5)));
	            poly.add(p);
	        }

	        return poly;
	    }

}