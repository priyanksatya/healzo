package com.healzo.doc;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.mixpanel.android.mpmetrics.MixpanelAPI;



public class Menufragmentcab extends SherlockFragment {
	ListView list;
	MenuClickInterFace mClick;
    ArrayList<String> listdata=new ArrayList<String>();
	private TextView header;
	private ImageView header_img;
	boolean flag=true;
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
		list = (ListView) getView().findViewById(R.id.frag_list);
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
		View view = inflater.inflate(R.layout.menulist_bookcab, container, false);
		list = (ListView) view.findViewById(R.id.frag_list);
		header=(TextView)view.findViewById(R.id.frag_header);
		header_img=(ImageView)view.findViewById(R.id.frag_header_img);

    	//listdata.clear();
    	new SettingsFragment().notifyDataSetChanged();
    	listdata.add("My Profile");
		listdata.add("Travel History");
		listdata.add("About us");
		listdata.add("Logout");
		list.setAdapter(new SettingsFragment());
		
		header_img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				flag=true;
				header.setText("SETTINGS");
				header_img.setBackgroundResource(0);
				listdata.clear();
				listdata.add("My Profile");
				listdata.add("Travel History");
				listdata.add("About us");
				listdata.add("Logout");
				list.invalidateViews();
			}
		});
		
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
		ImageView img = (ImageView)v.findViewById(R.id.setting_image);
		if(position==2 && flag==true ){
			img.setBackgroundResource(R.drawable.icon_slidemenu);}
		item.setText(listdata.get(position));
		v.setOnClickListener(new OnClickListener() {
			
			

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (listdata.get(position) == "Travel History") {
					Intent intent = new Intent(getActivity(),TravelHistory.class);
					getActivity().finish();
					startActivity(intent);

				}
				if (listdata.get(position) == "My Profile") {
					Intent intent = new Intent(getActivity(),MyProfileActivity.class);
					getActivity().finish();
					MixpanelAPI mixpanel = MixpanelAPI.getInstance(getActivity(), getString(R.string.MIXPANEL_TOKEN));
					mixpanel.track("My Profile click", null);
					startActivity(intent);

				}
				
				if (listdata.get(position) == "Logout") {
					SharedPreferences settings = getActivity().getSharedPreferences("authentication", getActivity().MODE_PRIVATE);
					settings.edit().clear().commit();
					
					Intent intent = new Intent(getActivity(),RegisterActivity.class);
					getActivity().finish();
					startActivity(intent);
					
				}
				if (listdata.get(position) == "About us") {
					flag=false;
					header.setText("About us");
					header_img.setBackgroundResource(R.drawable.icon_back);
					listdata.clear();
					listdata.add("Terms and Conditions & Privacy Policy");
					listdata.add("Contact Us");
					list.invalidateViews();
				}
				else if (listdata.get(position) == "Terms and Conditions & Privacy Policy") {
					
					Intent intent = new Intent(getActivity(),
							TermsandconActivity.class);
					startActivity(intent);
				}
				else if (listdata.get(position) == "Contact Us") {
					
					Intent intent = new Intent(getActivity(),
							ContactActivity.class);
					startActivity(intent);

				}
			}
		});
		return v;
	}
	
}
}
