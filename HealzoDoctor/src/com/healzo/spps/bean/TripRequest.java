package com.healzo.spps.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TripRequest implements Serializable{

	private String dmobile;
	private String bid;
	private String frm_loc;
	private String to_loc;
	private String landmark;
	private String status;
	
	

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDmobile() {
		return dmobile;
	}
	public void setDmobile(String dmobile) {
		this.dmobile = dmobile;
	}
	public String getBid() {
		return bid;
	}
	public void setBid(String bid) {
		this.bid = bid;
	}
	public String getFrm_loc() {
		return frm_loc;
	}
	public void setFrm_loc(String frm_loc) {
		this.frm_loc = frm_loc;
	}
	public String getTo_loc() {
		return to_loc;
	}
	public void setTo_loc(String to_loc) {
		this.to_loc = to_loc;
	}
	public String getLandmark() {
		return landmark;
	}
	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}
	
}
