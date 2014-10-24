package com.waverim.sherlock;

public class User {
	private int user_id;
	private int photo_num;
	private int loc_id;
	private double lat;
	private double lng;
	private String share_time;
	
	public User(int user_id, int photo_num, int loc_id, double lat, double lng, String share_time) {
		this.user_id = user_id;
		this.photo_num = photo_num;
		this.loc_id = loc_id;
		this.lat = lat;
		this.lng = lng;
		this.share_time = share_time;
	}
	
	public int getUserId () {
		return user_id;
	}
	public int getPhotoNum () {
		return photo_num;
	}
	public int getLocId () {
		return loc_id;
	}
	public double getLat () {
		return lat;
	}
	public double getLng () {
		return lng;
	}
	public String getShareTime () {
		return share_time;
	}
}
