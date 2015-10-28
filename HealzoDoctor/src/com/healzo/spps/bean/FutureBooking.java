package com.healzo.spps.bean;

import java.io.Serializable;




public class FutureBooking implements Serializable {

	String from_loc,to_loc,from_latlong,to_latlong,time,status,driver_status,booking_id,passenger_name,passenger_mobile;

	public FutureBooking(String from_loc, String to_loc, String from_latlong,
			String to_latlong, String time, String status,String driver_status, String booking_id,String passenger_name, String passenger_mobile) {
		super();
		this.from_loc = from_loc;
		this.to_loc = to_loc;
		this.from_latlong = from_latlong;
		this.to_latlong = to_latlong;
		this.time = time;
		this.status = status;
		this.driver_status=driver_status;
		this.booking_id = booking_id;
		this.passenger_name=passenger_name;
		this.passenger_mobile = passenger_mobile;
		
	}

	
	
	
	public String getPassenger_name() {
		return passenger_name;
	}

	public void setPassenger_name(String passenger_name) {
		this.passenger_name = passenger_name;
	}

	public String getPassenger_mobile() {
		return passenger_mobile;
	}

	public void setPassenger_mobile(String passenger_mobile) {
		this.passenger_mobile = passenger_mobile;
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

	public String getFrom_latlong() {
		return from_latlong;
	}

	public void setFrom_latlong(String from_latlong) {
		this.from_latlong = from_latlong;
	}

	public String getTo_latlong() {
		return to_latlong;
	}

	public void setTo_latlong(String to_latlong) {
		this.to_latlong = to_latlong;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDriver_status() {
		return driver_status;
	}

	public void setDriver_status(String driver_status) {
		this.driver_status = driver_status;
	}

	public String getBooking_id() {
		return booking_id;
	}

	public void setBooking_id(String booking_id) {
		this.booking_id = booking_id;
	}



}
