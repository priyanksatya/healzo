package com.healzo.doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.healzo.net.NetworkService;
import com.healzo.net.Talkback;
import com.healzo.spps.bean.PassengerConfirmData;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.slidingmenu.lib.SlidingMenu;


public class PassengerConfirmed extends SherlockFragmentActivity implements OnClickListener,Menufragmentdriver.MenuClickInterFace,Talkback ,OnInitListener{
	SlidingMenu menu;   
	Bundle bundle;
	ImageView call,popup;
	TextView contact,pname;
	public static String bookingid;
	Button pass_pick,pass_end;
	public static String driver_mobile;
	private static boolean activity=true;
	public static boolean noshow=true;
	private boolean flag=false;
	public static boolean flag_pick=false;
	//int time_noshow=0;
	public static boolean noshow_click=false;
	static ArrayList<PassengerConfirmData> pdata = new ArrayList<PassengerConfirmData>();
	
	public CountDownTimer waitTimer;
	public static int noshow_time;
	
	public static final String PASSENGER_REQ = "passsenger_req";
    public static final String ROOT = "root";
	public static final String PASS_MOBILE="passmobile";
	public static final String BOOKINGID="bookingid";
	public static final String FROM_LOC="fromlocation";
	public static final String TO_LOC="tolocation";
	public static final String PASS_NAME="pass_name";
	public static final String LANDMARK="landmark";
	public static final String STATUS="status";
	private String Error;
	private int nodes_length;
	private String bid_status;
	String passlist = "";
	private CountDownTimer timer;
	public static String to_location_english;
	public static String from_location_english;
	private boolean flag_click_tripend=false;
	private String crashHandle;
	private GPSTracker gsp;
	private SharedPreferences prefs;
	protected static String seconds;
	TextView to_loc;
	TextToSpeech tts;
	private TextView get_route;
	

	
	@Override
	protected void onDestroy() {
		if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
		
		
		noshow=true;
		noshow_click=false;
		RequestReceiver.notification_status=true;
		noshow_time=0;
		CircleTimer.noty_Available=false;
		SharedPreferences preferences = getSharedPreferences("booking_details", 0);
		if(preferences!=null){
		preferences.edit().clear().commit();
		}
		super.onDestroy();
	}

	
	
	private void countDownTimer(int time_value,final int mode) {
		/*prefs = PreferenceManager.getDefaultSharedPreferences(this);
		time_value=prefs.getInt(mode+"", time_value);
		final Editor edit=prefs.edit();*/
		
		timer = new CountDownTimer(time_value * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				try {
					long durationSeconds = millisUntilFinished / 1000;
					seconds=String.valueOf(durationSeconds);
					//edit.putInt(""+mode, Math.round(durationSeconds));
				} catch (Exception e) {
				}

			}

			public void onFinish() {
				//prefs.edit().remove(mode+"");
				if(mode==1)
		    	pass_pick.setVisibility(View.VISIBLE);
				if(mode==2){
					flag_click_tripend=true;
				}
			}
		}.start();
	}

	
	public void animateMap(LatLng latlng,GoogleMap map){
		CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(latlng) // Sets the center of the map to
        .zoom(10)                   // Sets the zoom
        .bearing(10) // Sets the orientation of the camera to east
        .tilt(45)    // Sets the tilt of the camera to 30 degrees
        .build();    // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(
        cameraPosition));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passenger_confirmed);
		Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Lobster_1.3.ttf");
		TextView title=(TextView) findViewById(R.id.title_confirm);
		title.setTypeface(tf);
		
		tts = new TextToSpeech(this, this);
		
		RequestReceiver.notification_status=false;
		createSlideMenu();
		
		gsp=  new GPSTracker(getApplicationContext());
		call=(ImageView) findViewById(R.id.call_passenger);
		contact=(TextView) findViewById(R.id.p_mobile_no_confirm);
		pname=(TextView) findViewById(R.id.p_name_confirm);
		popup=(ImageView) findViewById(R.id.btn_pop_menu_selected);
		pass_pick=(Button) findViewById(R.id.pass_pick);
		pass_end=(Button) findViewById(R.id.pass_end);
		get_route=(TextView)findViewById(R.id.get_route);
		to_loc=(TextView) findViewById(R.id.location);
		bundle=getIntent().getExtras();
		 String crashHandle=bundle.getString("crashHandle");
			if(crashHandle!=null){
				
				bookingid=bundle.getString("bid").trim();
				driver_mobile=bundle.getString("dmobile").trim();
				
				if(bundle.getString("crashHandle").equalsIgnoreCase("tripend")){
					Log.v("tripend", "tripend");
					pass_pick.setVisibility(Button.GONE);
					pass_end.setVisibility(Button.VISIBLE);
					countDownTimer(600, 2);
				}
				if(bundle.getString("crashHandle").equalsIgnoreCase("tripstart")){
					Log.v("tripstart", "tripstart");
					//pass_pick.setVisibility(Button.VISIBLE);
					//pass_end.setVisibility(Button.GONE);
					countDownTimer(60,1);
				}
		}else{
			countDownTimer(60,1);
		}
		
		setData();
		playNotification();
		call.setOnClickListener(this);
		
		
		get_route.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(gsp.canGetLocation){
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+gsp.latitude+","+gsp.longitude+"&daddr="+PassengerConfirmed.to_location_english));
			        startActivity(i);
				}else{
					Toast.makeText(getApplicationContext(), "Location no available...", Toast.LENGTH_LONG).show();
				}
			}
		});
		popup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				menu.toggle();
			}
		});
		
		
		pass_pick.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if(isConnectionAvailable()){
					MixpanelAPI mixpanel = MixpanelAPI.getInstance(getApplicationContext(), getString(R.string.MIXPANEL_TOKEN));
			        mixpanel.track("Start trip clicked",null);
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				    StrictMode.setThreadPolicy(policy);
				    ThreadPolicy tp = ThreadPolicy.LAX;
				    StrictMode.setThreadPolicy(tp);
				    final String  URL=getResources().getString(R.string.ipaddress_port)+
				    		"doctor_start_consultation.jsp?bookingid="+bookingid+"&doctor_mobile="+driver_mobile;
					
				    
				    new NetworkService(PassengerConfirmed.this,
				    		PassengerConfirmed.this, 
							URL,
							CabzoConstants.KEY_BOOKING_IF_PASSENGER_CONFIRMED).execute();
		
					}
					else
			      displayNoInternet();
				
			}
		});
		
		
		
		pass_end.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(flag_click_tripend){
				if(isConnectionAvailable()){
					MixpanelAPI mixpanel = MixpanelAPI.getInstance(getApplicationContext(), getString(R.string.MIXPANEL_TOKEN));
			        mixpanel.track("End trip clicked",null);
					//new NetworkTaskPickend(bookingid, driver_mobile).execute();
					final Handler netowrk = new Handler();
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				    StrictMode.setThreadPolicy(policy);
				    ThreadPolicy tp = ThreadPolicy.LAX;
				    StrictMode.setThreadPolicy(tp);
				    final String URL=getResources().getString(R.string.ipaddress_port)+
				    		"doctor_end_consultation.jsp?bookingid="+bookingid+"&doctor_mobile="+driver_mobile;
				    
				    new NetworkService(PassengerConfirmed.this,
				    		PassengerConfirmed.this, 
							URL,
							CabzoConstants.KEY_BOOKING_IF_END_TRIP).execute();

					}
					else
			      displayNoInternet();
				}
				else
			     Toast.makeText(getApplicationContext(), "please wait for "+seconds+" seconds", Toast.LENGTH_SHORT).show();
			}
				
		});
		
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		activity=true;
	}
	@Override
	protected void onPause() {
		activity = false;
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    switch(keyCode)
	    {
	        case KeyEvent.KEYCODE_BACK:
	        	
	    		if(menu.isMenuShowing()) {
	    			menu.toggle();
	    		}
	    		else{
		        	if(flag==false)
			            moveTaskToBack(true);
			        if(flag==true)
			        	finish();
	    		}

	            return true;
	    }
	    return false;
	}	

	public static boolean isActivityForeground() {
		return activity;
	}

	private void setData() {
		
		   bundle=getIntent().getExtras();
		   pdata.clear();
		  
			
			if (isConnectionAvailable()) {
				if(bundle.getString("bid").trim().length()==10){
					bookingid=bundle.getString("bid").trim();
					driver_mobile=bundle.getString("dmobile").trim();
					
			        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				    StrictMode.setThreadPolicy(policy);
				    ThreadPolicy tp = ThreadPolicy.LAX;
				    StrictMode.setThreadPolicy(tp);
					
				    final String URL = getResources().getString(R.string.ipaddress_port)+"android_driver_passenger_det.jsp?bookingid="+bookingid+"&drivermobile="+driver_mobile;
    
					new NetworkService(PassengerConfirmed.this,
							PassengerConfirmed.this, 
							URL,
							CabzoConstants.KEY_BOOKING_SET_DATA).execute();
				}
			} else {
				displayNoInternet();
			}

	}
	private void displayNoInternet() {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(PassengerConfirmed.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", null);
		dialog.show();
	}
	private boolean isConnectionAvailable() {
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}
	private void createSlideMenu() {
		
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.RIGHT);
		menu.setTouchModeAbove(SlidingMenu.RIGHT);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(R.layout.menu_confirm_driver);
		menu.setSlidingEnabled(true);
	}
	@Override
	public void onClick(View v) {
		
		if(v.getId()==R.id.call_passenger){
			MixpanelAPI mixpanel = MixpanelAPI.getInstance(getApplicationContext(), getString(R.string.MIXPANEL_TOKEN));
	        mixpanel.track("Call click",null);
			long numberIn = 0;
			Intent call=new Intent();
			try{
				numberIn = Long.parseLong(contact.getText().toString());
				}
			catch(NumberFormatException nfe){
				nfe.printStackTrace();
			}
			call.setAction(Intent.ACTION_CALL);
			call.setData(Uri.parse("tel:"+numberIn));
			startActivity(call);
		}
	}
	@Override
	public void onListitemClick(String item) {
		
		
	}
	
	
 public class NetworkTaskLocation extends AsyncTask<Void, Void, Void> {
	String search_latlong;
	String URL = "";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_FORMATTED_ADDRESS = "formatted_address";
	//ProgressDialog pd;
	String lname;
	private String URLKey;

	NetworkTaskLocation(String search_latlong) {
		this.search_latlong = search_latlong;
		URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ this.search_latlong + "&sensor=true";
		URL = URL.replaceAll(" ", "%20");
		this.URLKey = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ this.search_latlong + "&sensor=true"+"&key=AIzaSyD5-BLM0iTWiTVmbbN1XxCzeGSQmkp3WPs";
		this.URLKey = URLKey.replaceAll(" ", "%20");

	}

	@Override
	protected void onPreExecute() {
		
/*		pd = ProgressDialog.show(BookCab.this, "Please Wait","Getting Current Location");
		pd.setCancelable(true);*/
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			GlobalMethods globalMethods = new GlobalMethods(getApplicationContext());
			String tempLname = globalMethods.getLNameFromLatLong(globalMethods.SendHTTPPost(URL));
			if(tempLname == "" ){
				lname = globalMethods.getLNameFromLatLong(globalMethods.SendHTTPPost(URLKey));
			} else {
				lname = tempLname;
			}
		} catch (Exception e){
			e.printStackTrace();
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
				Toast.makeText(getApplicationContext(), "An error occured please try again", 2000).show();	
				}
			});
			cancel(true);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		
		//pd.dismiss();
		String []location=lname.split(", Maharashtra");
		String location1=PassengerConfirmed.to_location_english; //TODO set english
		//String []location=lname.split(",AndhraPradesh");
		//String []location1=from_loc.getText().toString().split(",AndhraPradesh");
		if(isConnectionAvailable())
		new NetworkTaskDistance(location[0], location1).execute();
		else
			displayNoInternet();
	}

}

public class NetworkTaskDistance extends AsyncTask<Void, Void, Void> {
	String origin, destination;
	String URL = "";
	private static final String TAG_ELEMENTS = "elements";
	private static final String TAG_ROWS = "rows";
	private static final String TAG_DURATION = "duration";
	private static final String TAG_TEXT = "text";
	String dist;
	private String duration;
	private int durn;
	

	NetworkTaskDistance(String origin, String destination) {
		this.origin = origin;
		this.destination = destination;
		URL = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
				+ this.origin
				+ "&destinations="
				+ this.destination
				+ "&mode=transit&sensor=false"
				+ "&key=AIzaSyD5-BLM0iTWiTVmbbN1XxCzeGSQmkp3WPs";
		URL = URL.replaceAll(" ", "%20");

	}

	@Override
	protected Void doInBackground(Void... params) {
		
		InputStream is = null;
		String json = "";
		JSONObject jobj = null;
		JSONArray rows = null;
		//Log.v("URL", URL);
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(URL);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
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
			json = sb.toString();
			//Log.v("Result", json);
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jobj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		try {

			rows = jobj.getJSONArray(TAG_ROWS);
			for (int i = 0; i < rows.length(); i++) {

				JSONObject elem = rows.getJSONObject(0);
				JSONArray elements = elem.getJSONArray(TAG_ELEMENTS);
				JSONObject dist = elements.getJSONObject(0);
				JSONObject dura = dist.getJSONObject(TAG_DURATION);
				duration = dura.getString(TAG_TEXT);
			}
			String[] durnary;
//			System.out.println("***************URL IS*********"+URL);
			if(duration != null){
			if (duration.contains("hours")) {
				durnary = duration.split("hours");
				if (durnary[1].contains("mins"))
					durn = ((Integer.valueOf(durnary[0].trim()) * 60) + (Integer
							.valueOf(durnary[1].replace("mins", "").trim())));
				else {
					if (durnary[1].contains("min")) {
						durn = ((Integer.valueOf(durnary[0].trim()) * 60) + (Integer
								.valueOf(durnary[1].replace("min", "")
										.trim())));
					}
				}
				duration = String.valueOf(durn);

			}
			if (duration.contains("hour")) {
				durnary = duration.split("hour");
				if (durnary[1].contains("mins"))
					durn = ((Integer.valueOf(durnary[0].trim()) * 60) + (Integer
							.valueOf(durnary[1].replace("mins", "").trim())));
				else {
					if (durnary[1].contains("min")) {
						durn = ((Integer.valueOf(durnary[0].trim()) * 60) + (Integer.valueOf(durnary[1].replace("min", "").trim())));
					}
				}

				duration = String.valueOf(durn);
			} else if (!(duration.contains("hours"))
					&& !(duration.contains("hour"))) {
				if (duration.contains("mins"))
					duration = (duration.replace("mins", "").trim());
				else {
					if (duration.contains("min"))
						duration = (duration.replace("min", "").trim());
				}
			}
		}
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void result) {
		
		if(duration==null){
			if(isConnectionAvailable())
				  new NetworkTaskLocation(RequestReceiver.driver_latlong).execute();
			  else
				  displayNoInternet();
		}else{
			
			waitTimer=new CountDownTimer(((Integer.parseInt(duration)*60)+PushReceiver.noshow)*1000, 1000) {
				private int time;
					
					
					public void onTick(long millisUntilFinished) {
						  time= (int) (millisUntilFinished / 1000);
						  Log.v("Total Seconds",((Integer.parseInt(duration)*60)+PushReceiver.noshow)*1000+"");
							Log.v("Full TIme",time+"");
							noshow_time=time;
						 }

						 public void onFinish() {
							 noshow_click=true;
							 if(!flag_pick){
							        Toast.makeText(getApplicationContext(), "If you have picked up passenger, please tap on Passenger Pickedup Button.", Toast.LENGTH_SHORT).show();
							        playNotification();
							        }
							 }
						}.start();
		}
	
	}
	
	

}

public void playNotification() {
    try {
    	MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.driver_sound);
    	mediaPlayer.start();
    	} catch (Exception e) 
    	{
        e.printStackTrace();
    }
}


private void parseXML(String xmlString) throws Exception {
	
    try {
        Document doc = getDomElement(xmlString);
        NodeList nodes = doc.getElementsByTagName(PASSENGER_REQ);
        retrieveData(doc,nodes);
    } catch (Exception e) {
        //Logger.logError(e);
        throw e;
    }
}

private void retrieveData(Document doc, NodeList nodes) {
	
	nodes_length=nodes.getLength();
	if(nodes_length!=0){
		passlist="Found";
    for(int i = 0 ; i < nodes.getLength() ; i++) {
        
    	Element element = (Element) nodes.item(i);	
		
		PassengerConfirmed.from_location_english = getValue(element, FROM_LOC);
        PassengerConfirmed.to_location_english = getValue(element, TO_LOC);
        
        PassengerConfirmData cd=new PassengerConfirmData(getValue(element, PASS_MOBILE), getValue(element, TO_LOC), getValue(element, FROM_LOC), getValue(element, PASS_NAME), getValue(element, BOOKINGID),getValue(element, LANDMARK));
        pdata.add(cd);
        if(getValue(element, STATUS).length()!=0){
        	bid_status="yes";
        	result();
        }
        else{
        	bid_status="no";
        result();}
    }
}
	else{
		passlist="Not Found";
		result();
		}
}

private void result() {
	
	
	if (passlist.equals("Not Found")) {
		RequestReceiver.notification_status=true;
		AlertDialog.Builder dialog_cab = new AlertDialog.Builder(PassengerConfirmed.this);
		dialog_cab.setTitle("PASSENGER STATUS");
		dialog_cab.setIcon(android.R.drawable.ic_menu_search);
		dialog_cab.setMessage("Sorry! You are not selected");
		dialog_cab.setCancelable(false);
		dialog_cab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
					Intent intent=new Intent(PassengerConfirmed.this, LandingActivity.class);
					startActivity(intent);
					finish();
			}
		});
		dialog_cab.show();
		//Toast.makeText(getApplicationContext(),"Sorry All Drivers are Busy!... Try Again Later", Toast.LENGTH_LONG).show();
	} else if (passlist.equals("Found")) {
		RequestReceiver.notification_status=false;
		if(bid_status.equalsIgnoreCase("no")){
		PassengerConfirmData cld = pdata.get(0);
		  
		  contact.setText(cld.getContact_no());
		  pname.setText(cld.getPname());
		   
		  to_loc.setSingleLine();
		  to_loc.setEllipsize(TruncateAt.MARQUEE);
		  to_loc.setHorizontallyScrolling(true);
		  to_loc.setFocusableInTouchMode(true);
		  to_loc.setSelected(true);
		   
		  if(isConnectionAvailable())
			  new NetworkTaskLocation(RequestReceiver.driver_latlong).execute();
		  else
			  displayNoInternet();
	}
	
	if(bid_status.equalsIgnoreCase("yes")){
		RequestReceiver.notification_status=true;
		AlertDialog.Builder dialog_cab = new AlertDialog.Builder(PassengerConfirmed.this);
		dialog_cab.setTitle("PASSENGER STATUS");
		dialog_cab.setIcon(android.R.drawable.ic_menu_search);
		dialog_cab.setMessage("Sorry! Trip Request Booking Has Been Cancelled");
		dialog_cab.setCancelable(false);
		dialog_cab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Intent intent=new Intent(PassengerConfirmed.this, LandingActivity.class);
				startActivity(intent);
				finish();
			}
		});
		dialog_cab.show();
	}
	
	}
}
private String getValue(Element item, String str) {
	
	NodeList n = item.getElementsByTagName(str);
    return getElementValue(n.item(0));
	
}

private String getElementValue(Node elem) {
	
    try {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.CDATA_SECTION_NODE 
                            || child.getNodeType() == Node.TEXT_NODE )
                    {
                        return child.getNodeValue().trim();
                    }
                }
            }
        }
        return "";
    } catch (DOMException e) {
        //Logger.logError(e);
        return "";
    }
}

private Document getDomElement(String xmlString) throws SAXException, IOException, ParserConfigurationException {
	
    Document doc = null;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(xmlString));
    doc = db.parse(is);
	return doc;
}

@Override
public void Sucess(String result, String value) {
	// TODO Auto-generated method stub
	String output=result;
	if(value.equalsIgnoreCase(CabzoConstants.KEY_BOOKING_IF_PASSENGER_CONFIRMED)){
	
	if(output.trim().equals("success")){
		noshow=false;
		flag_pick=true;
		//Toast.makeText(PassengerConfirmed.this, "Trip Started", Toast.LENGTH_SHORT).show();
		pass_pick.setVisibility(Button.GONE);
		pass_pick.postDelayed(new Runnable() {
		    public void run() {
		    	countDownTimer(600, 2);
		    	pass_end.setVisibility(Button.VISIBLE);
		    }
		}, 500);
		
	}
	if(output.trim().equals("Sorry,Booking Id Cancel")){
		noshow=false;
		if(waitTimer != null) {
			   waitTimer.cancel();
			   waitTimer = null;
			}
		Toast.makeText(PassengerConfirmed.this, "Passenger has reported Noshow. Trip is cancelled", Toast.LENGTH_SHORT).show();
		Intent intent=new Intent(PassengerConfirmed.this, LandingActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	}
	if(value.equalsIgnoreCase(CabzoConstants.KEY_BOOKING_IF_END_TRIP)){
		
		if(output.trim().equals("success")){
			noshow=false;
			CircleTimer.noty_Available=false;
			RequestReceiver.notification_status=true;
			//Toast.makeText(PassengerConfirmed.this, "Trip Ended", Toast.LENGTH_SHORT).show();
			pass_end.setVisibility(Button.GONE);
			flag=true;
			Intent intent=new Intent(PassengerConfirmed.this, LandingActivity.class);
			startActivity(intent);
			finish();
		}
	}
	if(value.equalsIgnoreCase(CabzoConstants.KEY_BOOKING_SET_DATA)){
	
		if(result!=null){
			try {
				parseXML(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}else{
			
		}
		}
	}

private void speakOut(String text) {
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
}
@Override
public void onInit(int status) {
	// TODO Auto-generated method stub
	if (status == TextToSpeech.SUCCESS) {
		 
		int result = tts.setLanguage(new Locale("hin", "IN"));
        /*if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "This Language is not supported");
        } else {
        	tts.setLanguage(Locale.US);
            //btnSpeak.setEnabled(true);
            //speakOut();
        }*/

    } else {
        Log.e("TTS", "Initilization Failed!");
    }
}

}
