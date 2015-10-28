package com.healzo.net;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

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

import android.content.Context;
import android.util.Log;

import com.healzo.doc.DBHelper;
import com.healzo.spps.bean.TripRequest;

public class ServerToDB {
	
	private Context c;
	
	public static final String DRIVER_REQ = "driver_req";
    public static final String ROOT = "root";
	public static final String DRIVER_MOBILE="drivermobile";
	public static final String BOOKINGID="bookingid";
	public static final String FROM_LOCATION="fromlocation";
	public static final String TO_LOCATION="tolocation";
	public static final String LANDMARK="landmark";
	
	DBHelper db;
	public static ArrayList<TripRequest> list_trip_final=new ArrayList<TripRequest>();
	
	HashMap<String, String> dtdc_address = new HashMap<String, String>();

	public static int nodes_count=0;


	
	
	ServerToDB(Context c){
		this.c=c;
	}
	
	public void parseXML(String xmlString) throws Exception {
    try {
        Document doc = getDomElement(xmlString);
        NodeList nodes = doc.getElementsByTagName(DRIVER_REQ);
        retrieveData(doc,nodes);
    } catch (Exception e) {
        //Logger.logError(e);
        throw e;
    }
}
   
static public Document getDomElement(String xmlString) throws ParserConfigurationException, SAXException, IOException {
    Document doc = null;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(xmlString));
    doc = db.parse(is);
    return doc; 
}


private void retrieveData(Document doc, NodeList nodes) {
	db=new DBHelper(c);
	list_trip_final.clear();
	db.delete();
	nodes_count=nodes.getLength();   
    for(int i = 0 ; i < nodes.getLength() ; i++) {
        Element element = (Element) nodes.item(i);
        Log.v("Mobile",getValue(element, DRIVER_MOBILE));
        Log.v("bookingid",getValue(element, BOOKINGID));
        Log.v("fromlocation",getValue(element, FROM_LOCATION));
        Log.v("tolocation",getValue(element, TO_LOCATION));
        Log.v("landmark",getValue(element, LANDMARK));
        //list.add(name);
/*        String stmt="SELECT bookingid FROM request_data";
    	SQLiteDatabase sdb=db.getWritableDatabase();
    	Cursor cursor=sdb.rawQuery(stmt, null);
    	if(cursor.moveToFirst()){	
    		//Integer id1=cursor.getInt(0);
    		//Log.v("ID",id1.toString());
    		do{
    		
    		if(!(getValue(element, BOOKINGID).equals(cursor.getString(0))))
    		{
    			db.insertData(getValue(element, DRIVER_MOBILE), getValue(element, BOOKINGID), getValue(element, FROM_LOCATION), getValue(element, TO_LOCATION), getValue(element, LANDMARK),"0");	

    		}
   		
    		
    		}while (cursor.moveToNext());
    	}
    	else{
            db.insertData(getValue(element, DRIVER_MOBILE), getValue(element, BOOKINGID), getValue(element, FROM_LOCATION), getValue(element, TO_LOCATION), getValue(element, LANDMARK),"0");

    	}
    	cursor.close();  */ 
        
        db.insertData(getValue(element, DRIVER_MOBILE), getValue(element, BOOKINGID), getValue(element, FROM_LOCATION), getValue(element, TO_LOCATION), getValue(element, LANDMARK));

    }
    list_trip_final=db.getData();
    //return list;
}

static public String getValue(Element item, String str) {
    NodeList n = item.getElementsByTagName(str);
    return getElementValue(n.item(0));
}

static public final String getElementValue( Node elem ) {
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
}
