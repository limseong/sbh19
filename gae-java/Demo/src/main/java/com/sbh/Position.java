package com.sbh;

public class Position {
	double latitude;
	double longitude;
	String name;
	
	public Position(double lt, double ln, String nm) {
		latitude = lt;
		longitude = ln;
		name = nm;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public String getName() {
		return name;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public void setName(String name) {
		this.name = name;
	}
}
