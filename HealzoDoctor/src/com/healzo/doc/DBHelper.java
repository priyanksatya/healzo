package com.healzo.doc;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.healzo.spps.bean.TripRequest;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="taxi_request.db";
	private static final String TABLE_NAME="request_data";
	private static final String TABLE_NAME_REF="request_data_ref";
	private static final int DATABASE_VERSION=1;
	private static  String DB_PATH;
	
	//coloumns fields related to our table
	
	private static final String KEY_DMOBILE="driver_mobile";
	private static final String KEY_BID="bookingid";
	private static final String KEY_FROM_LOC="from_loc";
	private static final String KEY_TO_LOC="to_loc";
	private static final String KEY_LANDMARK="landmark";
	private static final String KEY_STATUS="status";
	
	
	private static final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"(id INTEGER PRIMARY KEY AUTOINCREMENT,driver_mobile TEXT,bookingid TEXT,from_loc TEXT,to_loc TEXT,landmark TEXT);";
	private static final String CREATE_TABLE_REF="CREATE TABLE "+TABLE_NAME_REF+"(id INTEGER PRIMARY KEY AUTOINCREMENT,bookingid TEXT,status TEXT);";

	
	public static String driver_mobile;
	public static String bookingid;
	public static String from_loc;
	public static String to_loc;
	public static String landmark;
	public static int no_of_records;
	
	public DBHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE);
		db.execSQL(CREATE_TABLE_REF);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME_REF);
 
        // Create tables again
        onCreate(db);
	}
	public boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }


	public void insertData(String mobile,String bid,String from_loc,String to_loc,String landmark){
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues values=new ContentValues();
		//values.put(KEY_ID, 1); As it is autoincrement field
		values.put(KEY_DMOBILE, mobile);
		values.put(KEY_BID, bid);
		values.put(KEY_FROM_LOC, from_loc);
		values.put(KEY_TO_LOC, to_loc);
		values.put(KEY_LANDMARK, landmark);
		//values.put(KEY_STATUS, status);	
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	
	public void insertDataRef(String bid,String status){
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues values=new ContentValues();
		//values.put(KEY_ID, 1); As it is autoincrement field
		values.put(KEY_BID, bid);
		values.put(KEY_STATUS, status);	
		db.insert(TABLE_NAME_REF, null, values);
		db.close();
	}
	
    public ArrayList<TripRequest> getData(){
    	ArrayList<TripRequest> request_list=new ArrayList<TripRequest>();
    	request_list.clear();
    	String stmt="select r.* from request_data r  left outer join request_data_ref rf on r.bookingid = rf.bookingid where rf.bookingid is null;";
    	SQLiteDatabase db=this.getWritableDatabase();
    	Cursor cursor=db.rawQuery(stmt, null);
    	if(cursor.moveToFirst()){	
    		//Integer id1=cursor.getInt(0);
    		//Log.v("ID",id1.toString());
    		do{
    		TripRequest tr=new TripRequest();
    			
    		Log.v("DBdriver_mobile", cursor.getString(1));
    	    tr.setDmobile(cursor.getString(1));
    	    
    		Log.v("DB-BookingID", cursor.getString(2));
    		tr.setBid(cursor.getString(2));
    		
    		Log.v("DB-fromloc", cursor.getString(3));
    		tr.setFrm_loc(cursor.getString(3));
    		
    		Log.v("DB-toloc", cursor.getString(4));
    		tr.setTo_loc(cursor.getString(4));
    		
    		Log.v("DB-Landmark", cursor.getString(5));
    		tr.setLandmark(cursor.getString(5));
    		
/*    		Log.v("DB-Status", cursor.getString(6));
    		tr.setStatus(cursor.getString(6));*/
    		
    		request_list.add(tr);
    		
    		}while (cursor.moveToNext());
    	}
    	cursor.close();
    	db.close();
		return request_list;
    	
}
    public int getCount(){
    	String stmt="SELECT COUNT(*) FROM "+TABLE_NAME;
    	SQLiteDatabase db=this.getWritableDatabase();
    	Cursor cursor=db.rawQuery(stmt, null);
    	if(cursor.moveToFirst()){	
    		//Integer id1=cursor.getInt(0);
    		//Log.v("ID",id1.toString());
    		no_of_records=cursor.getInt(0);
    	}
    	cursor.close();
    	//db.close();
		return no_of_records;
    	
}
    public int getCountRef(){
    	String stmt="SELECT COUNT(*) FROM "+TABLE_NAME_REF;
    	SQLiteDatabase db=this.getWritableDatabase();
    	Cursor cursor=db.rawQuery(stmt, null);
    	if(cursor.moveToFirst()){	
    		//Integer id1=cursor.getInt(0);
    		//Log.v("ID",id1.toString());
    		no_of_records=cursor.getInt(0);
    	}
    	cursor.close();
    	//db.close();
		return no_of_records;
    	
}
    public void delete(){
    	SQLiteDatabase db=this.getWritableDatabase();
    	if(getCount()!=0){
    	db.execSQL("delete from " +TABLE_NAME);}
    	db.close();
    }
    
    public void deleteRef(){
    	SQLiteDatabase db=this.getWritableDatabase();
    	if(getCountRef()!=0){
    	db.execSQL("delete from " +TABLE_NAME_REF);}
    	db.close();
    }
    
/*    public void statusChange(String bid) {
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(KEY_STATUS, "1");	
		db.update(TABLE_NAME, values, KEY_BID+"='"+bid+"'", null);
		db.close();
	}*/
}
