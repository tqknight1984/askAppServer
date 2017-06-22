package com.chetuan.askapp.model;

public class UserLocation {

	public String userId = "";
	
	public String deviceId = "";
	
	/**
	 * 经度
	 */
	public double longitude;
	
	/**
	 * 纬度
	 */
	public double latitude;
	
	public long updateTime;
	
	@Override
	public String toString() {
		return "{\n" +
				"userId=" + userId + "\n" +
				"deviceId=" + deviceId + "\n" +
				"longitude=" + longitude + "\n" +
				"latitude=" + latitude + "\n" +
				"updateTime=" + updateTime + "\n" +
				"}\n";
	}
	
}
