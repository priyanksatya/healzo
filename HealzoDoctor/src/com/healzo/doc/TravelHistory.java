package com.healzo.doc;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.healzo.doc.R.color;
import com.healzo.spps.bean.TripHistoryData;

/**
* This class aims to display the Travel History of Driver.
* @author Praveen Kumar
* @version 1.0
*/
public class TravelHistory extends Activity {

	private ListView listview;
	ArrayList<TripHistoryData> pickdata=new ArrayList<TripHistoryData>();
	private ImageView back;
	private SharedPreferences spf;
	private String mobile;
	private String driver_id;
	public AlertDialog.Builder dialog_cab;
	
	public static final String DRIVER_LOG = "driver_log";
    public static final String ROOT = "root";
	public static final String SELECTION_TIME="selection_time";
	public static final String FROM_LOC="fromlocation";
	public static final String TO_LOC="tolocation";
	public static final String STATUS="status";
	public static final String TRIP_TYPE="trip_type";
	
	private int nodes_length;

	
	private String Error;
	String cablist = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_travel_history);
		spf=getSharedPreferences("authentication",MODE_PRIVATE);
		Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Lobster_1.3.ttf");
		TextView title=(TextView) findViewById(R.id.title_th);
		title.setTypeface(tf);
		back=(ImageView)findViewById(R.id.btn_back_bhistory);
		listview=(ListView) findViewById(R.id.history_booking);
		
		checkSharedPreference();		
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//if(dialog_cab!=null)((DialogInterface) dialog_cab).dismiss();
				Intent intent=new Intent(TravelHistory.this, LandingActivity.class);
				startActivity(intent);
				finish();
			}
			
		          
		});
	}
	private void checkSharedPreference() {
		// TODO Auto-generated method stub
		if(spf.getString("validation", null)!=null && spf.getString("driver-id", null)!=null && spf.getString("mobileno", null)!=null){
			
			if(isConnectionAvailable()){								
			mobile=spf.getString("mobileno", null);
			driver_id=spf.getString("driver-id", null);
			//new NetworkTaskCabList().execute();
			final Handler netowrk = new Handler();
			
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		    ThreadPolicy tp = ThreadPolicy.LAX;
		    StrictMode.setThreadPolicy(tp);
			final String URL = getResources().getString(R.string.ipaddress_port)+"android_driver_history_all.jsp?driver_id="+driver_id;
			
			Log.v("URL", URL);
		    Runnable r=new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = null;
					httppost = new HttpPost(URL);		
						
					try {
						
						HttpResponse response = httpclient.execute(httppost);
						int responseCode = response.getStatusLine().getStatusCode();
						switch(responseCode) {
						case 200:
						HttpEntity entity = response.getEntity();
						    if(entity != null) {
						    	String responseBody = EntityUtils.toString(entity);
						    	parseXML(responseBody);
						    }
						    break;
						}

					} catch (Exception e) {
						Log.e("EXXXXXXXXXXXXX", e.toString());
						Error = e.getMessage();
					}

				}
				private void parseXML(String xmlString) throws Exception {
					// TODO Auto-generated method stub
				    try {
				        Document doc = getDomElement(xmlString);
				        NodeList nodes = doc.getElementsByTagName(DRIVER_LOG);
				        retrieveData(doc,nodes);
				    } catch (Exception e) {
				        //Logger.logError(e);
				        throw e;
				    }
				}

				private void retrieveData(Document doc, NodeList nodes) {
					// TODO Auto-generated method stub
					nodes_length=nodes.getLength();
					if(nodes_length!=0){
						cablist="Found";
				    for(int i = 0 ; i < nodes.getLength() ; i++) {
				        
				    	Element element = (Element) nodes.item(i);		        
				        TripHistoryData cd=new TripHistoryData(getValue(element, SELECTION_TIME), 
				        				getValue(element, FROM_LOC), 
				        				getValue(element, TO_LOC), 
				        				getValue(element, STATUS),
				        				getValue(element, TRIP_TYPE));
				        pickdata.add(cd);
				        }
				    result();
				}
					else{
						cablist="Not Found";
						result();
						}
				}

				private void result() {
					// TODO Auto-generated method stub
					netowrk.removeCallbacks(this);
					if (cablist.equals("Not Found")) {
						dialog_cab = new AlertDialog.Builder(TravelHistory.this);
						dialog_cab.setTitle("CAB LIST STATUS");
						dialog_cab.setIcon(android.R.drawable.ic_menu_search);
						dialog_cab.setMessage("Sorry! No Booking History Found");
						dialog_cab.setCancelable(false);
						dialog_cab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							
							}
						});
						dialog_cab.show();
						//Toast.makeText(getApplicationContext(),"Sorry All Drivers are Busy!... Try Again Later", Toast.LENGTH_LONG).show();
					} else if (cablist.equals("Found")) {
						setData();
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
					// TODO Auto-generated method stub
				    Document doc = null;
				    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				    DocumentBuilder db = dbf.newDocumentBuilder();
				    InputSource is = new InputSource();
				    is.setCharacterStream(new StringReader(xmlString));
				    doc = db.parse(is);
					return doc;
				}

				
			};netowrk.post(r);
			}
			
			else
				displayNoInternet();
					}
		}
	public void displayNoInternet() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(TravelHistory.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet Connection");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", null);
		dialog.show();
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
	
	
	private void setData() {
		// TODO Auto-generated method stub
		listview.setAdapter(new ListHistory());
	}
	public class ListHistory extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pickdata.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub			
			View v= LayoutInflater.from(TravelHistory.this).inflate(R.layout.history_list, null);
			TextView date=(TextView) v.findViewById(R.id.history_date);
			TextView from_loc=(TextView) v.findViewById(R.id.history_from_loc);
			TextView to_loc=(TextView) v.findViewById(R.id.history_to_loc);
			TextView status=(TextView) v.findViewById(R.id.history_status);
			TextView type=(TextView) v.findViewById(R.id.history_type);
			final TripHistoryData thd=pickdata.get(position);
			date.setText(thd.getDate().toString());
			from_loc.setText(thd.getFrom_loc().toString());
			to_loc.setText(thd.getTo_loc().toString());
			status.setText(thd.getStatus().toString());
			
			String trip_type=thd.getType().toString();
			if(trip_type.equalsIgnoreCase("f")) {
				type.setText("Future Booking");
				type.setTextColor(Color.WHITE);
			}
					
			if(trip_type.equalsIgnoreCase("c")) {
				type.setText("Current Booking");
				type.setTextColor(Color.YELLOW);
			}
			
			return v;
		}
		
	}

}
