package com.healzo.spps.bean;

import java.io.Serializable;

public class PassengerConfirmData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String contact_no;
	public String to_loc;
	public String from_loc;
	public String pname;
	public String bookingid;
	public String landmark;
	
	
	
	
	public PassengerConfirmData(String contact_no, String to_loc,
			String from_loc, String pname, String bookingid,String landmark) {
		super();
		this.contact_no = contact_no;
		this.to_loc = to_loc;
		this.from_loc = from_loc;
		this.pname = pname;
		this.bookingid = bookingid;
		this.landmark=landmark;
	}




	public String getLandmark() {
		return landmark;
	}




	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}




	public String getContact_no() {
		return contact_no;
	}




	public void setContact_no(String contact_no) {
		this.contact_no = contact_no;
	}




	public String getTo_loc() {
		return to_loc;
	}




	public void setTo_loc(String to_loc) {
		this.to_loc = to_loc;
	}




	public String getFrom_loc() {
		return from_loc;
	}




	public void setFrom_loc(String from_loc) {
		this.from_loc = from_loc;
	}




	public String getPname() {
		return pname;
	}




	public void setPname(String pname) {
		this.pname = pname;
	}




	public String getBookingid() {
		return bookingid;
	}




	public void setBookingid(String bookingid) {
		this.bookingid = bookingid;
	}
	
	
}

