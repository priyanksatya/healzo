package com.healzo.spps.bean;

import java.io.Serializable;

public class TripHistoryData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String date;
	public String from_loc;
	public String to_loc;
	public String status;
	public String type;
	
	public TripHistoryData() {
		// TODO Auto-generated constructor stub
	}

	public TripHistoryData(String date,String from_loc, String to_loc,String status,String type) {
		this.date=date;
		this.from_loc = from_loc;
		this.to_loc = to_loc;
		this.status=status;
		this.type=type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getFrom_loc() {
		return from_loc;
	}

	public void setFrom_loc(String from_loc) {
		this.from_loc = from_loc;
	}

	public String getTo_loc() {
		return to_loc;
	}

	public void setTo_loc(String to_loc) {
		this.to_loc = to_loc;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}
