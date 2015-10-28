package com.healzo.doc;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
/**
* This class aims to display the T&C details.
* @author Praveen Kumar
* @version 1.0
*/
public class TermsandconActivity extends Activity {
	private ImageView back;
	private WebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_termsandcon);
		Typeface tf = Typeface.createFromAsset(getAssets(),
	            "fonts/Lobster_1.3.ttf");
		TextView title=(TextView) findViewById(R.id.title_tc);
		title.setTypeface(tf);
		back=(ImageView)findViewById(R.id.btn_back_tc);
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(getResources().getString(R.string.ipaddress_port_web)+"/smartcab/terms-conditions-2");
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
	
				onBackPressed();
			}
		});

	}

}
