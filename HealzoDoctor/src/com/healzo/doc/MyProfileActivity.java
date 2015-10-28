package com.healzo.doc;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.healzo.net.NetworkService;
import com.healzo.net.Talkback;

public class MyProfileActivity extends Activity implements Talkback{

	private ImageView back;
	TextView name, mobile_text, vno, s_start, s_end, available, hold,
			r_location, location_time, status;
	private SharedPreferences spf;

	private String mobile;
	private String driver_id;
	public AlertDialog.Builder dialog_cab;

	java.sql.Timestamp mobiletime;

	int day, hour;

	public static final String DRIVER_LOG = "passsenger_req";
	public static final String ROOT = "root";
	public static final String DRIVER_NAME = "doctorname";
	public static final String DRIVER_MOBILE = "doctormobile";
	public static final String VEHICLE_NO = "vehiclno";
	public static final String SHIFT_FROM = "shiftfrom";
	public static final String SHIFT_TO = "shiftto";
	public static final String STATUS = "status";
	public static final String AVAILABLE = "available";
	public static final String HOLD = "hold";
	public static final String REALTIME_LOCATION = "realtimeloc";
	public static final String LOCATION_TIME = "locationtime";
	private int nodes_length;

	private String Error;
	String cablist = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_profile);
		spf = getSharedPreferences("authentication", MODE_PRIVATE);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/Lobster_1.3.ttf");
		TextView title = (TextView) findViewById(R.id.title_mp);
		title.setTypeface(tf);
		name = (TextView) findViewById(R.id.name_profile);
		mobile_text = (TextView) findViewById(R.id.mobile_profile);
		vno = (TextView) findViewById(R.id.vehicle_profile);
		s_start = (TextView) findViewById(R.id.shiftstart_profile);
		s_end = (TextView) findViewById(R.id.shiftend_profile);
		status = (TextView) findViewById(R.id.lstatus_profile);
		available = (TextView) findViewById(R.id.available_profile);
		hold = (TextView) findViewById(R.id.hold_profile);
		r_location = (TextView) findViewById(R.id.rlocation_profile);
		location_time = (TextView) findViewById(R.id.time_profile);

		name.setSelected(true);
		mobile_text.setSelected(true);
		vno.setSelected(true);
		s_start.setSelected(true);
		s_end.setSelected(true);
		status.setSelected(true);
		hold.setSelected(true);
		r_location.setSelected(true);
		location_time.setSelected(true);
		available.setSelected(true);

		checkSharedPreference();

		back = (ImageView) findViewById(R.id.btn_back_mp);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// if(dialog_cab!=null)((DialogInterface) dialog_cab).dismiss();
				Intent intent = new Intent(MyProfileActivity.this,LandingActivity.class);
				startActivity(intent);
				finish();
			}

		});
	}

	private void checkSharedPreference() {
		// TODO Auto-generated method stub
		if (spf.getString("validation", null) != null
				&& spf.getString("driver-id", null) != null
				&& spf.getString("mobileno", null) != null) {

			if (isConnectionAvailable()) {
				mobile = spf.getString("mobileno", null);
				driver_id = spf.getString("driver-id", null);

				final Handler netowrk = new Handler();
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
				ThreadPolicy tp = ThreadPolicy.LAX;
				StrictMode.setThreadPolicy(tp);
				final String URL = getResources().getString(R.string.ipaddress_port)+ "doctor_profile.jsp?doctor_mobile="+ mobile;

				new NetworkService(MyProfileActivity.this,
						MyProfileActivity.this, 
						URL,
						CabzoConstants.KEY_BOOKING_ACCEPT_TRIP).execute();
				
				
			}

			else
				displayNoInternet();
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
		AlertDialog.Builder dialog = new AlertDialog.Builder(
				MyProfileActivity.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", null);
		dialog.show();
	}

	@Override
	public void Sucess(String result, String value) {
		// TODO Auto-generated method stub
		Log.v("Result",result);
		try {
			parseXML(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void parseXML(String xmlString) throws Exception {
		// TODO Auto-generated method stub
		try {
			Document doc = getDomElement(xmlString);
			NodeList nodes = doc.getElementsByTagName(DRIVER_LOG);
			retrieveData(doc, nodes);
		} catch (Exception e) {
			// Logger.logError(e);
			throw e;
		}
	}

	private void retrieveData(Document doc, NodeList nodes) {
		// TODO Auto-generated method stub
		nodes_length = nodes.getLength();
		if (nodes_length != 0) {
			cablist = "Found";
			for (int i = 0; i < nodes.getLength(); i++) {

				Element element = (Element) nodes.item(i);

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				Calendar calendar = Calendar.getInstance();
				day = calendar.get(calendar.DAY_OF_WEEK);
				hour = calendar.get(calendar.HOUR_OF_DAY);
				java.util.Date now = calendar.getTime();
				java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
				mobiletime = currentTimestamp;
				String mtime = mobiletime + "";
				int mobileMinutes = 0;
				try {
					java.util.Date fechaNueva = format.parse(mtime);
					String mobiletime[] = format.format(fechaNueva).split(" ");
					String mobtime[]=mobiletime[1].split(":");
					mobileMinutes=(Integer.parseInt(mobtime[0])*60)+Integer.parseInt(mobtime[1]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String stime[] = getValue(element,LOCATION_TIME).split(" ");
				String servertime[]=stime[1].split(":");
				int serverMins=(Integer.parseInt(servertime[0])*60)+Integer.parseInt(servertime[1]);
				Log.v("Mobile Mins----Server Mins----Time Diff", mobileMinutes+"-----"+serverMins+"-----"+(mobileMinutes-serverMins)+"");
				if((mobileMinutes-serverMins)>10){
					location_time.setTextColor(Color.RED);
				}
				if (getValue(element, STATUS).equalsIgnoreCase("Ready to Accept Trip")) {
					status.setTextColor(Color.WHITE);
				}
				if (getValue(element, AVAILABLE).equalsIgnoreCase("Ready to Accept Trip")) {
					available.setTextColor(Color.WHITE);
				}
				if (getValue(element, HOLD).equalsIgnoreCase("Ready to Accept Trip")) {
					hold.setTextColor(Color.WHITE);
				}
				name.setText(getValue(element, DRIVER_NAME));
				mobile_text.setText(getValue(element,DRIVER_MOBILE));
				vno.setText(getValue(element, VEHICLE_NO));
				s_start.setText(getValue(element, SHIFT_FROM));
				s_end.setText(getValue(element, SHIFT_TO));
				status.setText(getValue(element, STATUS));
				available.setText(getValue(element, AVAILABLE));
				hold.setText(getValue(element, HOLD));
				r_location.setText(getValue(element,REALTIME_LOCATION));
				location_time.setText(getValue(element,LOCATION_TIME));
			}
			result();
		} else {
			cablist = "Not Found";
			result();
		}
	}

	private void result() {
		// TODO Auto-generated method stub
		if (cablist.equals("Not Found")) {
			dialog_cab = new AlertDialog.Builder(
					MyProfileActivity.this);
			dialog_cab.setTitle("Driver Profile");
			dialog_cab
					.setIcon(android.R.drawable.ic_menu_search);
			dialog_cab
					.setMessage("Sorry! Your Profile Not Found. Try Again Later");
			dialog_cab.setCancelable(false);
			dialog_cab.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							// TODO Auto-generated method stub

						}
					});
			dialog_cab.show();
			// Toast.makeText(getApplicationContext(),"Sorry All Drivers are Busy!... Try Again Later",
			// Toast.LENGTH_LONG).show();
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

	private Document getDomElement(String xmlString)
			throws SAXException, IOException,
			ParserConfigurationException {
		// TODO Auto-generated method stub
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlString));
		doc = db.parse(is);
		return doc;
	}
}
