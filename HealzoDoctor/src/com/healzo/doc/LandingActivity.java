package com.healzo.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.healzo.net.NetworkService;
import com.healzo.net.Talkback;
import com.healzo.net.UpdateSync;
import com.slidingmenu.lib.SlidingMenu;

public class LandingActivity extends SherlockFragmentActivity implements
		Menufragmentcab.MenuClickInterFace, OnMarkerClickListener, Talkback {

	SlidingMenu menu;
	int count = 1;
	ImageView popup,off_layout;
	private com.healzo.doc.GPSTracker gps;
	public static boolean activity = false;

	public GoogleMap googleMap;
	SupportMapFragment fm; // add this
	private String doctor_mobile;
	private String driver_id;
	
	LatLngBounds.Builder builder = new LatLngBounds.Builder();
	public static Context context;
	// GLOBAL PARAMETERS

	public static final String GLOBAL_PARAM = "global_param";
	public static final String ROOT = "root";
	public static final String SHORT_NAME = "shortname";
	public static final String GLOBAL_VALUE = "global_value";
	private int nodes_length;

	SharedPreferences spf;
	private String Error;
	private Marker myLocMarker;
	
	private ProgressDialog pd;
	private CheckBox toogle_swtch;
	private TextView status_text;
	
	protected boolean isConnectionAvailable() {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing);
		context=LandingActivity.this;
		
		
		spf = getSharedPreferences("authentication", MODE_PRIVATE);
		doctor_mobile= spf.getString("mobileno", null);
		driver_id = spf.getString("driver-id", null);
		
		try {
		String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		TextView ver=(TextView)findViewById(R.id.version);
		status_text=(TextView)findViewById(R.id.status_text);
		
		
		ver.setText("Version: "+versionName+"v");
        } catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pd = new ProgressDialog(LandingActivity.this);
		pd.setMessage("loading");
		

		fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		googleMap = fm.getMap();
		try {
			googleMap.setOnMarkerClickListener(this);
			googleMap.setMyLocationEnabled(true);
			googleMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
				
				@Override
				public void onMapLoaded() {
					// TODO Auto-generated method stub
					createSharedPreference();
				}
			});
		} catch (Exception e) {
			// Toast.makeText(getApplicationContext(),
			// "Please update google play services...",
			// Toast.LENGTH_LONG).show();
		}
	
		Intent intent=getIntent();
		
		createSlideMenu();
		popup = (ImageView) findViewById(R.id.btn_pop_menu);
		off_layout = (ImageView) findViewById(R.id.off_layout);
		
		Log.v("On Create", "btn_pop_menu");
		View status = (View) findViewById(R.id.locationStatus);
		gps = new GPSTracker(LandingActivity.this);
		if (gps.canGetLocation()) {
			status.setBackgroundColor(Color.GREEN);
			try {
				LatLng coordinate = new LatLng(gps.getLatitude(),gps.getLongitude());
				CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(
						coordinate, 15);
				googleMap.animateCamera(yourLocation);

			} catch (Exception e) {
			}

		} else {
			status.setBackgroundColor(Color.RED);
		}
		
		popup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				menu.toggle();
			}
		});

		if (!isMyServiceRunning()) {
			Intent intent_service = new Intent(LandingActivity.this,RequestReceiver.class);
			startService(intent_service);
			//finish();
		}
		
		
		
		toogle_swtch=(CheckBox)findViewById(R.id.toogle_swtch);
		toogle_swtch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				
				if(isChecked) changeStatus("1");
				else changeStatus("2");
					
			}
		});
		
		off_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changeStatus("1");	
			}
		});
		
	}

	
	public void changeStatus(String status){
		if (createNoNetworkAlert()) {
		    //TODO Network Not Available
			
			if(status.equalsIgnoreCase("1")){
				toogle_swtch.setChecked(true);
				off_layout.setVisibility(ImageView.GONE);
				fm.getView().setVisibility(ImageView.VISIBLE);
				status_text.setText("You will receive patient bookings");
			
			}else{
				toogle_swtch.setChecked(false);
				off_layout.setVisibility(ImageView.VISIBLE);
				fm.getView().setVisibility(ImageView.GONE);
				status_text.setText("You will not receive patient bookings");
			}
			
			
			/*String URL = CabzoConstants.URL_DRIVER_STATUS+"status="+status+"&doctor_mobile="+doctor_mobile;

			new NetworkService(LandingActivity.this, LandingActivity.this.context, URL,
					CabzoConstants.KEY_DRIVER_STATUS).execute();*/
		}
	}
	
	public boolean createNoNetworkAlert(){
		if (checkNetwork()) {
			return true;
		}else{
			new AlertDialog.Builder(this)
		    .setTitle("No Internet Connection")
		    .setMessage("Are you sure you want to switch on wifi or mobile data?")
		    .setPositiveButton("Wifi", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		        }
		     })
		    .setNegativeButton("Mobile Data", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        	
		        	startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);	
		        	
		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
		}
		
		return false;
		}
	
	
	private void createSharedPreference() {
		// TODO Auto-generated method stub
		
		if (createNoNetworkAlert()) {
		    //TODO Network Not Available
			String URL = getResources().getString(R.string.ipaddress_port)
					+ "android_global_params.jsp";

			new NetworkService(LandingActivity.this, LandingActivity.this.context, URL,
					CabzoConstants.KEY_GLOBAL_PARAMETERS_SERVICE).execute();
		}
		

	}
	
	private boolean checkNetwork() {
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    } else {
	        Toast.makeText(getApplicationContext(),"Sorry, Network Unavailable! :(", Toast.LENGTH_LONG).show();
	    }
	    return false;
	}
	
	private void createSlideMenu() {
		// TODO Auto-generated method stub
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.RIGHT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(R.layout.menu_bookcab);
		menu.setSlidingEnabled(true);
		Log.v("Sliding Menu", "Created");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		activity = true;
		if(pd!=null)
		pd.dismiss();
		
		Log.v("activity status foreground", activity + "");
		if (UpdateSync.noty_Available) {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nMgr = (NotificationManager) getApplicationContext()
					.getSystemService(ns);
			nMgr.cancel(100);
		}

		if (CircleTimer.noty_Available) {

			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
			nMgr.cancel(101);

			SharedPreferences spf = getSharedPreferences("booking_details",
					MODE_PRIVATE);
			if (spf.getString("bid", null) != null
					&& spf.getString("dmobile", null) != null) {
				Intent intent = new Intent(getApplicationContext(),PassengerConfirmed.class);
				intent.putExtra("bid", spf.getString("bid", null));
				intent.putExtra("dmobile", spf.getString("dmobile", null));
				startActivity(intent);
				CircleTimer.noty_Available = false;
				finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	
		activity = false;
		Log.v("activity status", activity + "");
	}

	public static boolean isActivityForeground() {
		Log.v("activity status background", activity + "");
		return activity;
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (RequestReceiver.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (menu.isMenuShowing()) {
			menu.toggle();
		} else {
			super.onBackPressed();
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startMain);
		}
	}

	@Override
	public void onListitemClick(String item) {
		// TODO Auto-generated method stub
		if (item.equalsIgnoreCase("Logout")) {

		}

	}

	private void parseXML(String xmlString) throws Exception {
		// TODO Auto-generated method stub
		try {
			Document doc = getDomElement(xmlString);
			NodeList nodes = doc.getElementsByTagName(GLOBAL_PARAM);
			retrieveData(doc, nodes);
		} catch (Exception e) {
			// Logger.logError(e);
			throw e;
		}
	}

	private void retrieveData(Document doc, NodeList nodes) {
		// TODO Auto-generated method stub
		Log.v("Global Parametrs Landing", "Global Parametrs added to spf");
		nodes_length = nodes.getLength();

		if (nodes_length != 0) {

			spf = getSharedPreferences("globalparameters", MODE_PRIVATE);
			SharedPreferences.Editor spe = spf.edit();
			for (int i = 0; i < nodes.getLength(); i++) {

				Element element = (Element) nodes.item(i);
				if (getValue(element, SHORT_NAME).equals(
						"driver_accept_trip_timer")) {
					try {
						// timer_pass_list=Integer.parseInt(getValue(element,
						// GLOBAL_VALUE));
						// Log.v("Driver timer", timer_pass_list+"");
						spe.putInt("driver_accept_trip_timer", Integer
								.parseInt(getValue(element, GLOBAL_VALUE)));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				if (getValue(element, SHORT_NAME)
						.equals("driver_waiting_timer")) {
					// timer_wait_driver=Integer.parseInt(getValue(element,
					// GLOBAL_VALUE));
					spe.putInt("driver_waiting_timer",
							Integer.parseInt(getValue(element, GLOBAL_VALUE)));
					Log.v("driverpickup", getValue(element, GLOBAL_VALUE));
				}
				if (getValue(element, SHORT_NAME).equals("noshow_driver_time")) {
					// time_noshow=Integer.parseInt(getValue(element,
					// GLOBAL_VALUE));
					spe.putInt("noshow_driver_time",
							Integer.parseInt(getValue(element, GLOBAL_VALUE)));
					Log.v("time_noshow", getValue(element, GLOBAL_VALUE));
				}
			}
			spe.commit();
		}

	}

	private String getValue(Element item, String str) {
		// TODO Auto-generated method stub
		NodeList n = item.getElementsByTagName(str);
		return getElementValue(n.item(0));

	}

	private String getElementValue(Node elem) {
		// TODO Auto-generated method stub
		try {
			Node child;
			if (elem != null) {
				if (elem.hasChildNodes()) {
					for (child = elem.getFirstChild(); child != null; child = child
							.getNextSibling()) {
						if (child.getNodeType() == Node.CDATA_SECTION_NODE
								|| child.getNodeType() == Node.TEXT_NODE) {
							return child.getNodeValue().trim();
						}
					}
				}
			}
			return "";
		} catch (DOMException e) {
			// Logger.logError(e);
			return "";
		}
	}

	private Document getDomElement(String xmlString) throws SAXException,
			IOException, ParserConfigurationException {
		// TODO Auto-generated method stub
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
		
		if (value.equalsIgnoreCase(CabzoConstants.KEY_GLOBAL_PARAMETERS_SERVICE)) {

			Log.v("result", result);
			try {
				parseXML(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SharedPreferences spf = getSharedPreferences("authentication",MODE_PRIVATE);
			doctor_mobile = spf.getString("mobileno", null);
			driver_id = spf.getString("driver-id", null);


		}
			
		
	}
	
	
	

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		Log.v("Marker", "Clciked");
		if (marker != null) {
			String title = marker.getTitle();

			}
		return true;
	}

	public MarkerOptions getMarker(GeoPoint point, int drawableId) {
		return new MarkerOptions().position(
				new LatLng(point.getLatitudeE6() / 1000000.0, point
						.getLongitudeE6() / 1000000.0)).icon(
				BitmapDescriptorFactory.fromResource(drawableId));
	}

	private Bitmap writeTextOnDrawable(int drawableId, String text) {

		Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
				.copy(Bitmap.Config.ARGB_8888, true);

		Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLACK);
		paint.setTypeface(tf);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(convertToPixels(getApplicationContext(), 13));

		Rect textRect = new Rect();
		paint.getTextBounds(text, 0, text.length(), textRect);

		Canvas canvas = new Canvas(bm);

		// If the text is bigger than the canvas , reduce the font size
		if (textRect.width() >= (canvas.getWidth() - 4)) // the padding on
															// either sides is
															// considered as 4,
															// so as to
															// appropriately fit
															// in the text
			paint.setTextSize(convertToPixels(getApplicationContext(), 7)); // Scaling
																			// needs
																			// to
																			// be
																			// used
																			// for
																			// different
																			// dpi's

		// Calculate the positions
		int xPos = (canvas.getWidth() / 2) - 2; // -2 is for regulating the x
												// position offset

		// "- ((paint.descent() + paint.ascent()) / 2)" is the distance from the
		// baseline to the center.
		int yPos = (int) ((canvas.getHeight() / 2) /*- ((paint.descent() + paint.ascent()) / 2)*/);

		canvas.drawText(text, xPos, yPos - 1, paint);

		return bm;
	}

	public static int convertToPixels(Context context, int nDP) {
		final float conversionScale = context.getResources()
				.getDisplayMetrics().density;

		return (int) ((nDP * conversionScale) + 0.5f);

	}

	public static Date convertStringDate(String datetext) {
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try {
			date = format.parse(datetext);
			System.out.println("Date ->" + date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	

	class GetHindiTask extends AsyncTask<String, Void, String> {
		Context c;
		TextView from_loc, to_loc;
		ProgressBar progress;

		public GetHindiTask(Context applicationContext, TextView from_loc,
				TextView to_loc, ProgressBar progress) {
			this.c = applicationContext;
			this.from_loc = from_loc;
			this.to_loc = to_loc;
			this.progress = progress;
		}

		private String translateText(String input) {
			String value = input;
			try {
				value = value.replace(" ", "-");
				BufferedReader reader = null;
				JSONObject jobj = null;
				String json = "";
				JSONObject predictions = null;
				
				URL url = new URL(
						"https://www.googleapis.com/language/translate/v2?key=AIzaSyD5-BLM0iTWiTVmbbN1XxCzeGSQmkp3WPs&q="
								+ value + "&target=hi&source=en");
				reader = new BufferedReader(new InputStreamReader(
						url.openStream(), "UTF-8"));
				StringBuilder buffer = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					buffer.append(line + "\n");
				}
				json = buffer.toString();
				jobj = new JSONObject(json);
				predictions = jobj.getJSONObject("data");
				JSONArray translations = predictions
						.getJSONArray("translations");
				for (int i = 0; i < translations.length(); i++) {
					JSONObject c = translations.getJSONObject(0);
					value = c.getString("translatedText");
				}
				return value;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return value;
		}

		protected String doInBackground(String... text) {
			String value = translateText(text[0]);
			String value1 = translateText(text[1]);
			String location = value + "~" + value1;
			return location;
		}

		protected void onPostExecute(String translatedText) {
			from_loc.setText(translatedText.split("~")[0]);
			to_loc.setText(translatedText.split("~")[1]);
			progress.setVisibility(ProgressBar.GONE);
		}
	}

}