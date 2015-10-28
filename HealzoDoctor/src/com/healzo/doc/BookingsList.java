package com.healzo.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.healzo.net.NetworkService;
import com.healzo.net.Talkback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

/**
 * This class aims to display the Trip request(From Location,To
 * Location,Landmark).
 * 
 * @author Praveen Kumar
 * @version 1.0
 */

public class BookingsList extends Activity implements Talkback,OnClickListener,OnInitListener,OnMapLoadedCallback{
	
	
	Button accpet_trip, reject_trip;
	
	
	float time = 1;
	int count_down = PushReceiver.count_down_acceptTime;
	float increment_value = 360 / count_down;
	float angle = increment_value;
	int rect_x = 40, rect_y = 40;
	CountDownTimer waitTimer;
	TextView time_display;
	String time_disp;
	public static boolean notify_flag_accept = true;

	Handler h_time = new Handler();
	int resp_time_count = 0;

	private float time_spent = count_down * 1000;
	private static boolean activity = true;
	boolean flag = false;
	boolean flag_click = false;
	private int size;

	private String driver_mobile;

	private String booking_id;
	private static int inc = 1;
	LocationManager locationManager;
	ProgressDialog pd;
	private CountDownTimer count_timer;
	private SharedPreferences spf;
	 // Google Map
    JSONObject json;
	GoogleMap googleMap;
	
	private Bitmap scaleImage(Resources res, int id, int lessSideSize) {
        Bitmap b = null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, id, o);

        float sc = 0.0f;
        int scale = 1;
        // if image height is greater than width
        if (o.outHeight > o.outWidth) {
            sc = o.outHeight / lessSideSize;
            scale = Math.round(sc);
        }
        // if image width is greater than height
        else {
            sc = o.outWidth / lessSideSize;
            scale = Math.round(sc);
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        b = BitmapFactory.decodeResource(res, id, o2);
        return b;
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookings_list);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMap();
        accpet_trip = (Button) findViewById(R.id.accept_trip);
		reject_trip = (Button) findViewById(R.id.reject_trip);
		spf=getSharedPreferences("authentication",MODE_PRIVATE);
		setData();
		
		callTImer(count_down);
		setSurfaceView();
        accpet_trip.setOnClickListener(new OnClickListener() {

			// private NetworkTaskAccept task;
			// private Handler acceptTrip;

			@Override
			public void onClick(View v) {
				
				MixpanelAPI mixpanel = MixpanelAPI.getInstance(getApplicationContext(), getString(R.string.MIXPANEL_TOKEN));
		        mixpanel.track("Trip accepted",null);
		        
		        if (isConnectionAvailable()) {
					if (flag_click == false) {

						String URL = getResources().getString(
								R.string.ipaddress_port)
								+ "doctor_trip_response.jsp?doctor_mobile="
								+ driver_mobile
								+ "&bookingid="
								+ booking_id;
						
						new NetworkService(BookingsList.this,
								BookingsList.this, 
								URL,
								CabzoConstants.KEY_BOOKING_ACCEPT_TRIP).execute();
					}
				} else {
					displayNoInternet();
				}
		        
			}
		});

		/**
		 * It cancel the trip request
		 */
		reject_trip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(BookingsList.this,LandingActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

	}
	
	public void playNotification() {
	    try {MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.driver_sound);
	    mediaPlayer.start();} catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	

	private synchronized void stopThread(Thread theThread) {
		if (theThread != null) {
			theThread = null;
		}
	}

	private void setSurfaceView() {
		LinearLayout l1 = (LinearLayout) findViewById(R.id.surface_list);
		time_display = (TextView) findViewById(R.id.time_list);

		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
				rect_x * 2, rect_y * 2);
		l1.setLayoutParams(parms);

		MySurfaceView mySurface = new MySurfaceView(getApplicationContext(),
				l1.getWidth(), l1.getHeight());
		l1.addView(mySurface);
	}

	@Override
	protected void onResume() {
		super.onResume();
		activity = true;
		RequestReceiver.notification_status = false;
	}

	@Override
	protected void onPause() {
		activity = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (count_timer != null)
			count_timer.cancel();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			//if (flag == true)
				moveTaskToBack(true);
			//if (flag == false)
			//	finish();
			return true;
		}
		return false;
	}

	public static boolean isActivityForeground() {
		return activity;
	}
	
	
	public void alert(){
		
		
		
		LayoutInflater factory = LayoutInflater.from(this);
		final View deleteDialogView = factory.inflate(R.layout.driver_dialog, null);
		final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
		deleteDialog.setView(deleteDialogView);
	
		Button ok= (Button) deleteDialogView.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				deleteDialog.dismiss();
				
				Intent intent = new Intent(BookingsList.this,LandingActivity.class);
				startActivity(intent);
				finish();
			}
		});
		deleteDialog.show();
	}

	public void callTImer(int sec) {

		count_timer = new CountDownTimer(sec * 1000, 1000) {
			
			public void onTick(long millisUntilFinished) {
				
				
				time = (int) (millisUntilFinished / 1000);
				int millis = (int) time * 1000;
				TimeZone tz = TimeZone.getTimeZone("UTC");
				SimpleDateFormat df = new SimpleDateFormat("mm:ss");
				df.setTimeZone(tz);
				time_disp = df.format(new Date(millis));
				PushReceiver.count_down_acceptTime=(int) time;
				time_display.setText(time_disp + "");
				angle = angle + increment_value;
			}

			public void onFinish() {
				RequestReceiver.notification_status = true;
				Toast.makeText(getApplicationContext(), "You have responded too late", Toast.LENGTH_SHORT).show();

				if (isActivityForeground() == false) {
					if (pd != null) {
						pd.dismiss();
					}
					finish();
				}
				if (isActivityForeground() == true) {
					if (pd != null) {
						pd.dismiss();
					}
					alert();
				}
				
			}
		}.start();
	}

	public class MyThread extends Thread {

		private SurfaceHolder msurfaceHolder;
		private MySurfaceView mSurfaceView;
		private boolean mrun = false;

		public MyThread(SurfaceHolder holder, MySurfaceView mSurfaceView) {

			this.msurfaceHolder = holder;
			this.mSurfaceView = mSurfaceView;
		}

		public void startrun(boolean run) {

			mrun = run;
		}

		@SuppressLint("WrongCall")
		@Override
		public void run() {

			super.run();
			Canvas canvas;
			while (mrun) {
				canvas = null;
				try {
					canvas = msurfaceHolder.lockCanvas(null);
					synchronized (msurfaceHolder) {
						mSurfaceView.onDraw(canvas);
					}
				} finally {
					if (canvas != null) {
						msurfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}

		}

	}

	public class MySurfaceView extends SurfaceView implements
			SurfaceHolder.Callback {

		// private Bitmap bitmap ;
		private MyThread thread;
		private int x = 20, y = 20;
		int width, height;

		public MySurfaceView(Context context, int w, int h) {
			super(context);

			width = w;
			height = h;
			thread = new MyThread(getHolder(), this);
			getHolder().addCallback(MySurfaceView.this);
			setFocusable(true);
		}

		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			try {
				Paint p = new Paint();

				canvas.drawColor(getResources().getColor(
						R.color.Background_orange));

				// .drawPoint(x, y, p);

				p.setColor(Color.WHITE);
				p.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.INNER));
				p.setStrokeWidth(1);
				p.setStyle(Paint.Style.FILL);

				Paint black = new Paint();
				black.setColor(getResources().getColor(R.color.circle_inner));
				black.setMaskFilter(new BlurMaskFilter(1,
						BlurMaskFilter.Blur.SOLID));
				black.setStrokeWidth(1);
				black.setStyle(Paint.Style.FILL);

				RectF rectF = new RectF(1, 1, rect_x * 2, rect_y * 2);
				canvas.drawCircle(rectF.centerX(), rectF.centerY(), rect_x, p);

				if (time < 2)
					canvas.drawArc(rectF, 0, 360, true, black);
				else
					canvas.drawArc(rectF, 0, angle, true, black);

			}

			catch (NullPointerException npe) {
				if (waitTimer != null) {
					waitTimer.cancel();
					waitTimer = null;
				}
				thread.interrupt();

			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			thread.startrun(true);
			try {
				thread.start();
			} catch (Exception e) {
				thread = new MyThread(getHolder(), this);
				thread.startrun(true);
				thread.start();
			}

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			thread.startrun(false);
			// thread.stop();
			thread.interrupt();

			// finish();

		}

	}

	/**
	 * This Method will check whether the device is connected to internet or not
	 * 
	 * @return true or false
	 */
	protected boolean isConnectionAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}

	/**
	 * This Method will display an alert message "No Internet Connectivity"
	 */
	public void displayNoInternet() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(BookingsList.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", null);
		dialog.show();
	}

	/**
	 * This is used to get the trip request stored in the array list and will
	 * display on the screen.
	 */
	private void setData() {

		driver_mobile = spf.getString("mobileno", null);
		try {
			json=new JSONObject(getIntent().getStringExtra("json"));
			//driver_mobile=json.getString("patient_mobile");
			booking_id=json.getString("booking_id");
			
			TextView loc=(TextView)findViewById(R.id.location);
			loc.setText(json.getString("to_loc"));
			
			/*LatLng coordinate1 = new LatLng(Double.parseDouble(json.get("to_latlong").toString().split(",")[0]),Double.parseDouble(json.get("to_latlong").toString().split(",")[1]));
			googleMap.addMarker(new MarkerOptions().position(coordinate1)
					.title("From Location").icon(BitmapDescriptorFactory.fromBitmap(scaleImage(getResources(), 
							R.drawable.locationpin_size, 100))));*/
			} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		String aResponse = result;
		Log.v("RESP", result);
		if ((null != aResponse)) { 
			try{
				Document doc = getDomElement(result);
				NodeList nodes = doc.getElementsByTagName("passsenger_info");
					Element element = (Element) nodes.item(0);
					String name= getValue(element, "name");
					String to_loc= getValue(element, "to_loc");
					String mobile= getValue(element, "mobile");
					
					
			}catch (Exception e){
				
			}
			Intent intent = new Intent(BookingsList.this,PassengerConfirmed.class);
			intent.putExtra("bid",booking_id);
			intent.putExtra("dmobile",driver_mobile);
			//intent.putExtra("from_loc",getIntent().getStringExtra("from_loc"));
			intent.putExtra("to_loc",getIntent().getStringExtra("to_loc"));
			startActivity(intent);
			finish();
			
			/*if (aResponse.trim().equals("success")) {
				flag_click = true;

				Intent intent = new Intent(BookingsList.this,CircleTimer.class);
				intent.putExtra("bid",booking_id);
				intent.putExtra("dmobile",driver_mobile);
				//intent.putExtra("from_loc",getIntent().getStringExtra("from_loc"));
				intent.putExtra("to_loc",getIntent().getStringExtra("to_loc"));
				startActivity(intent);
				finish();
			}*/

			if (aResponse.trim().equals("Sorry,Booking Id Cancel")) {

				flag_click = false;
				if (pd != null) {
					pd.dismiss();
				}

				RequestReceiver.notification_status = true;
				Toast.makeText(BookingsList.this,"Sorry! This Trip Has Been Cancelled",Toast.LENGTH_LONG).show();
				Intent _intent = new Intent(BookingsList.this,LandingActivity.class);
				startActivity(_intent);
				finish();
			}
			if (aResponse.trim().equals("failure")) {

				flag_click = false;
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(BookingsList.this,"Failure",Toast.LENGTH_LONG).show();
			}

		} else {
			// ALERT MESSAGE
			Toast.makeText(getBaseContext(),"No Response From Server.",Toast.LENGTH_SHORT).show();
		}
		
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
	


	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
	    } else {
	        Log.e("TTS", "Initilization Failed!");
	    }
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
		
	}
	
	
}
