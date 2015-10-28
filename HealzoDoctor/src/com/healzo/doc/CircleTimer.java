package com.healzo.doc;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.healzo.net.NetworkService;
import com.healzo.net.Talkback;

public class CircleTimer extends Activity implements Talkback{
	public static boolean noty_Available=false;
	float time = 1;
	int count_down = PushReceiver.count_down_acceptTime;
	float increment_value=360/count_down;
	float angle=increment_value;
	int rect_x=40,rect_y=40;
	String time_disp;
	TextView time_display;
	Button cancel_booking;
	CountDownTimer waitTimer;
	Handler h = new Handler();
	private float time_spent=count_down*1000;
	private Bundle bundle;
	private String booking_id;
	private String driver_mobile;
	private static boolean activity=true;
	public static final String PASSENGER_REQ = "passsenger_req";
    public static final String ROOT = "root";
	public static final String STATUS="status";
	
	private String Error;
	String passlist = "";
	private int nodes_length;
	private String bid_status;
	private CountDownTimer timer_count;
	
	private MediaPlayer m;
	
	static Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_circle_timer);
		RequestReceiver.notification_status=false;	
		context=CircleTimer.this;

		bundle=getIntent().getExtras();
		if(bundle!=null)
		{
			booking_id=bundle.getString("bid");
			driver_mobile=bundle.getString("dmobile");
			
		}
		
		int density= getResources().getDisplayMetrics().densityDpi;
		//h.postDelayed(timer, 0);
	    callTImer();
		switch(density)
	    {
	    case DisplayMetrics.DENSITY_LOW:
	       //Toast.makeText(CircleTimer.this, "LDPI", Toast.LENGTH_SHORT).show();
	       rect_x=12;rect_y=12;
	        break;
	    case DisplayMetrics.DENSITY_MEDIUM:
	         //Toast.makeText(CircleTimer.this, "MDPI", Toast.LENGTH_SHORT).show();
	         rect_x=16;rect_y=16;
	        break;
	    case DisplayMetrics.DENSITY_HIGH:
	        //Toast.makeText(CircleTimer.this, "HDPI", Toast.LENGTH_SHORT).show();
	        rect_x=25;rect_y=25;
	        break;
	    case DisplayMetrics.DENSITY_XHIGH:
	         //Toast.makeText(CircleTimer.this, "XHDPI", Toast.LENGTH_SHORT).show();
	         rect_x=34;rect_y=34;
	        break;
	    }
		
		LinearLayout l1= (LinearLayout)findViewById(R.id.surface);
		time_display=(TextView) findViewById(R.id.time);
		
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(rect_x*2,rect_y*2);
		l1.setLayoutParams(parms);
		
	    MySurfaceView mySurface=new MySurfaceView(getApplicationContext(), l1.getWidth(), l1.getHeight());
	    l1.addView(mySurface);
		
	    playNotification();
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(timer_count!=null)
		timer_count.cancel();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		activity=true;
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		activity=false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    switch(keyCode)
	    {
	        case KeyEvent.KEYCODE_BACK:
	        	moveTaskToBack(true);
	        	return true;
	    }
	    return false;
	}	

	public static boolean isActivityForeground() {
		return activity;
	}
	
	private void displayNoInternet() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialog = new AlertDialog.Builder(CircleTimer.this);
		dialog.setTitle("Network State");
		dialog.setIcon(android.R.drawable.ic_menu_search);
		dialog.setMessage("No Internet connection. Cancelling trip");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   Intent intent=new Intent(CircleTimer.this, MainActivity.class);
	        	   startActivity(intent);
	        	   finish();
	           }
	       });
		dialog.show();
	}
	private boolean isConnectionAvailable() {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;
	}

	
	public void callTImer(){
		timer_count =new CountDownTimer(count_down*1000, 1000) {//CountDownTimer(edittext1.getText()+edittext2.getText()) also parse it to long

			 public void onTick(long millisUntilFinished) {
				 	time= (int) (millisUntilFinished / 1000);
				 	int millis = (int) time*1000;
				    TimeZone tz = TimeZone.getTimeZone("UTC");
				    SimpleDateFormat df = new SimpleDateFormat("mm:ss");
				    df.setTimeZone(tz);
				    time_disp = df.format(new Date(millis));
				    time_display.setText(time_disp+"");
				    angle=angle+increment_value;
			}

			 public void onFinish() {
				 if(isConnectionAvailable()){
					 checkifselected();
				 } else {
					 displayNoInternet();
				 }
			 }
			}
			.start();
	}
	
	
	private synchronized void stopThread(Thread theThread)
	{
	    if (theThread != null)
	    {
	        theThread = null; 
	    }
	}
	private void createNotification(Context ctx) {
		// TODO Auto-generated method stub
		long[] vibrate = {0, 100, 200, 300,400};
		noty_Available=true;
		playBeep(ctx);
		SharedPreferences spf=getSharedPreferences("booking_details", MODE_PRIVATE);
		SharedPreferences.Editor spe=spf.edit();
		//spe.putString("validation","true");
		spe.putString("bid", booking_id);
		spe.putString("dmobile",driver_mobile);
		spe.commit();
		
		Intent intent = new Intent(ctx, PassengerConfirmed.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("bid", booking_id);
		intent.putExtra("dmobile", driver_mobile);
		intent.putExtra("to_loc",getIntent().getStringExtra("to_loc"));
		
	    PendingIntent pIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    // Build notification
	    Notification notification = new NotificationCompat.Builder(ctx)
	        .setSmallIcon(R.drawable.passenger_pick)
	        .setContentTitle("Click to View Passenger Details")
	        .setContentText("notification")
	        .setVibrate(vibrate)
	        .setContentIntent(pIntent).build();
	    
	    NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

	    // Hide the notification after its selected
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    notification.flags |=Notification.FLAG_NO_CLEAR;
	    notification.flags |=Notification.FLAG_ONGOING_EVENT;
	   // notification.defaults |= Notification.DEFAULT_SOUND;
	    notificationManager.notify(101, notification);
	    
}
protected void checkifselected() {
		// TODO Auto-generated method stub
	final Handler netowrk = new Handler();
	
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    ThreadPolicy tp = ThreadPolicy.LAX;
    StrictMode.setThreadPolicy(tp);
	final String URL = getResources().getString(R.string.ipaddress_port)+"android_driver_passenger_det.jsp?bookingid="+booking_id+"&drivermobile="+driver_mobile;

	new NetworkService(CircleTimer.this,
			CircleTimer.this, 
			URL,
			CabzoConstants.KEY_BOOKING_IF_SELECTED).execute();
	}
public class MyThread extends Thread{

    private SurfaceHolder msurfaceHolder;
    private MySurfaceView mSurfaceView;
    private boolean mrun =false;

    public MyThread(SurfaceHolder holder, MySurfaceView mSurfaceView) {

        this.msurfaceHolder = holder;
        this.mSurfaceView=mSurfaceView;
    }

    public void startrun(boolean run) {
        mrun=run;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        super.run();
         Canvas canvas;
         while (mrun) {
            canvas=null;
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

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	// private Bitmap bitmap ;
	 private MyThread thread;
	 private int x=20,y=20;int width,height;

	 public MySurfaceView(Context context,int w,int h) {
	     super(context);

	     width=w;
	     height=h;
	     thread=new MyThread(getHolder(),this);
	     getHolder().addCallback(MySurfaceView.this);
	     setFocusable(true);
	 }

	 @Override
	 public void onDraw(Canvas canvas) {
	     super.onDraw(canvas);
	     try{
		canvas.drawColor(getResources().getColor(R.color.Background_black_transparent));
		
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		p.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.INNER));
		p.setStrokeWidth(1);
		p.setStyle(Paint.Style.FILL);
		
		Paint black = new Paint();
		black.setColor(Color.BLACK);
		black.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.SOLID));
		black.setStrokeWidth(1);
		black.setStyle(Paint.Style.FILL);
		
		
		RectF rectF = new RectF(1, 1, rect_x*2,rect_y*2);
		canvas.drawCircle(rectF.centerX(), rectF.centerY(), rect_x, p);
		
		if(time<2)canvas.drawArc(rectF, 0, 360, true, black);
		else canvas.drawArc(rectF, 0, angle, true, black);
	}
	catch(NullPointerException npe){
	if(waitTimer != null) {
		   waitTimer.cancel();
		   waitTimer=null;
		}
    thread.interrupt();

}
	 }

	 @Override
	 public boolean onTouchEvent(MotionEvent event) {
	     return true;
	 }

	 @Override
	 public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
	     // TODO Auto-generated method stub

	 }

	 @Override
	 public void surfaceCreated(SurfaceHolder holder) {

		
		 try{
			    thread.startrun(true);
			    thread.start();
		 	}
		 	catch(Exception e){
			 thread=new MyThread(getHolder(),this);
			 thread.startrun(true);
			 thread.start();
		 }

	 }

	 @Override
	 public void surfaceDestroyed(SurfaceHolder holder) {

		 thread.startrun(false);
	     thread.interrupt();
	 }

}


private void parseXML(String xmlString) throws Exception {
	// TODO Auto-generated method stub
    try {
        Document doc = getDomElement(xmlString);
        NodeList nodes = doc.getElementsByTagName(PASSENGER_REQ);
        retrieveData(doc,nodes,xmlString);
    } catch (Exception e) {
        //Logger.logError(e);
        throw e;
    }
}


private void retrieveData(Document doc, NodeList nodes,String respoce) {
	// TODO Auto-generated method stub
	nodes_length=nodes.getLength();
	if(nodes_length!=0){
		passlist="Found";
    for(int i = 0 ; i < nodes.getLength() ; i++) {
        
    	Element element = (Element) nodes.item(i);		        
        if(getValue(element, STATUS).length()!=0){
        	bid_status="yes";
        	result(respoce);
        }
        else{
        	bid_status="no";
        	result(respoce);
        }
    }
}
	else{
		passlist="Not Found";
		result(respoce);
		}
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
			
			Intent intent = new Intent(CircleTimer.this,LandingActivity.class);
			startActivity(intent);
			finish();
		}
	});
	deleteDialog.show();
}


private void result(String responseBody) {
	// TODO Auto-generated method stub
	playNotification();
	if (passlist.equals("Not Found")) {
		RequestReceiver.notification_status=true;

		//Toast.makeText(getApplicationContext(),"Sorry! You are not selected", Toast.LENGTH_LONG).show();
		RequestReceiver.notification_status=true;
		/*Intent intent=new Intent(CircleTimer.this, LandingActivity.class);
		startActivity(intent);
		finish();
		*/
		alert();
		
	} else if (passlist.equals("Found")) {
		RequestReceiver.notification_status=false;
		if(bid_status.equalsIgnoreCase("no")){
	    	if(isActivityForeground()==false){
	    	finish();
	    	createNotification(CircleTimer.this);}
	    	if(isActivityForeground()==true){
		    Intent intent=new Intent(CircleTimer.this, PassengerConfirmed.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    intent.putExtra("bid", booking_id);
		    intent.putExtra("dmobile", driver_mobile);
		    intent.putExtra("from_loc",getIntent().getStringExtra("from_loc"));
			intent.putExtra("to_loc",getIntent().getStringExtra("to_loc"));
			intent.putExtra("resp", responseBody);
		    startActivity(intent);
		    finish();
		    }
	}
	
	if(bid_status.equalsIgnoreCase("yes")){
		RequestReceiver.notification_status=true;

		Toast.makeText(getApplicationContext(),"Sorry! Trip Request Booking Has Been Cancelled", Toast.LENGTH_LONG).show();
		Intent intent=new Intent(CircleTimer.this, LandingActivity.class);
		startActivity(intent);
		finish();

	}
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




@Override
public void Sucess(String result,String key) {
	// TODO Auto-generated method stub
	
	if(key.equalsIgnoreCase(CabzoConstants.KEY_BOOKING_IF_SELECTED)){
		try {
			parseXML(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	if(result.equals("push")){
		if(isActivityForeground()==false){
    	finish();
    	createNotification(CircleTimer.this);
    	}
    	if(isActivityForeground()==true){
    	playBeep(getApplicationContext());
	    
    	Intent intent=new Intent(CircleTimer.this, PassengerConfirmed.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    intent.putExtra("bid", booking_id);
	    intent.putExtra("dmobile", driver_mobile);
	   // intent.putExtra("resp", responseBody);
	    startActivity(intent);
	    finish();
	    }
}
	else if(result.equals("cancel")){
		Toast.makeText(getApplicationContext(),"Sorry! Trip Request Booking Has Been Cancelled", Toast.LENGTH_LONG).show();
		Intent intent=new Intent(CircleTimer.this, LandingActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
	}
	else if(result.equals("notselected")){
		Toast.makeText(getApplicationContext(),"Sorry! You are not selected", Toast.LENGTH_LONG).show();
		Intent intent=new Intent(CircleTimer.this, LandingActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();

	}
}
public void playBeep(Context context) {
    try {
    	MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.driver_sound);
    mediaPlayer.start();} 
    catch (Exception e) {
        e.printStackTrace();
    }
}
}
