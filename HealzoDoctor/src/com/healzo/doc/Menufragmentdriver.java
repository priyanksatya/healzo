package com.healzo.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;



public class Menufragmentdriver extends SherlockFragment {
	ListView list;
	MenuClickInterFace mClick;
    ArrayList<String> listdata=new ArrayList<String>();
    public boolean click=false;
	public String output;

	interface MenuClickInterFace {
		void onListitemClick(String item);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mClick = (MenuClickInterFace) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		list = (ListView) getView().findViewById(R.id.frag_list_driver);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String i=(String) arg0.getItemAtPosition(arg2);
				mClick.onListitemClick(i);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.menulist_confirm_driver, container, false);
		list = (ListView) view.findViewById(R.id.frag_list_driver);
    	//listdata.clear();
    	new SettingsFragment().notifyDataSetChanged();
		listdata.add("Passenger No Show");
		list.setAdapter(new SettingsFragment());
		return view;
	}
public class SettingsFragment extends BaseAdapter{

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listdata.size();
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
		View v= LayoutInflater.from(getActivity()).inflate(R.layout.settings_list_fragment, null);
		TextView item=(TextView) v.findViewById(R.id.item);
		item.setText(listdata.get(position));
		v.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					if(listdata.get(position)=="Passenger No Show"){
						if(click==false){
						if(isConnectionAvailable()){
							if(PassengerConfirmed.noshow_click){
							if(PassengerConfirmed.noshow){
							//new NetworkTaskNoShow(PassengerConfirmed.bookingid).execute();
								final Dialog dialog=new Dialog(getActivity());
								dialog.setContentView(R.layout.dialog);
								dialog.setTitle("Confirmation");
								TextView msg=(TextView) dialog.findViewById(R.id.d_dname);
								Button accept=(Button) dialog.findViewById(R.id.accept);
								Button reject=(Button) dialog.findViewById(R.id.reject);
								accept.setText("Yes");
								reject.setText("No");
								msg.setText("Do you really want to report Noshow by passenger? Please confirm.");
								dialog.setCanceledOnTouchOutside(false);
								dialog.show();
								accept.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										final Handler netowrk = new Handler();
										StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
									    StrictMode.setThreadPolicy(policy);
									    ThreadPolicy tp = ThreadPolicy.LAX;
									    StrictMode.setThreadPolicy(tp);
									  final String URL=getResources().getString(R.string.ipaddress_port)+"android_driver_noshow.jsp?bookingid="+PassengerConfirmed.bookingid+"&drivermobile="+PassengerConfirmed.driver_mobile;
										Runnable r=new Runnable() {
											
											@Override
											public void run() {
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
													Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_LONG).show();
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
												String split[]=output.split("@");
												if(split[0].trim().equals("success")){
													click=true;
													netowrk.removeCallbacks(this);
													Toast.makeText(getActivity(), "Sorry for the Passenger Noshow.", Toast.LENGTH_LONG).show();
													Intent intent=new Intent(getActivity(), LandingActivity.class);
													startActivity(intent);
													getActivity().finish();
													
												}
												if(split[0].trim().equals("failure")){
													netowrk.removeCallbacks(this);
													click=true;
													Toast.makeText(getActivity(), split[1], Toast.LENGTH_SHORT).show();
													Intent intent=new Intent(getActivity(), LandingActivity.class);
													startActivity(intent);
													getActivity().finish();
												}

											}
										};netowrk.post(r);

									}
								});
								reject.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}
								});
														
							}
							else
							Toast.makeText(getActivity(), "You have already Picked Up Passenger", Toast.LENGTH_SHORT).show();
						}
							else
							Toast.makeText(getActivity(), "Please wait! You can report Noshow after "+PassengerConfirmed.noshow_time+" Seconds", Toast.LENGTH_SHORT).show();
						}
						else
					     displayNoInternet();
					}
						else{
							Toast.makeText(getActivity(), "You have already informed about Passenger No Show", Toast.LENGTH_SHORT).show();
						}
					}

			}
		});
		return v;
	}
	
}

protected boolean isConnectionAvailable() {
	// TODO Auto-generated method stub
	ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netinfo = cm.getActiveNetworkInfo();
	if (netinfo != null && netinfo.isConnected())
		return true;
	else
		return false;
}

public void displayNoInternet() {
	AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
	dialog.setTitle("Network State");
	dialog.setIcon(android.R.drawable.ic_menu_search);
	dialog.setMessage("No Internet Connection");
	dialog.setCancelable(false);
	dialog.setPositiveButton("Ok", null);
	dialog.show();
}

}
