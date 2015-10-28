package com.healzo.doc;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactActivity extends Activity {
	private ImageView back;
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		Typeface tf = Typeface.createFromAsset(getAssets(),
	            "fonts/Lobster_1.3.ttf");
		TextView title=(TextView) findViewById(R.id.title_contact);
		title.setTypeface(tf);
		
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(getResources().getString(R.string.ipaddress_port_web)+"/smartcab/contact-us-2");
		back=(ImageView)findViewById(R.id.btn_back_cu);
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
	}



}
