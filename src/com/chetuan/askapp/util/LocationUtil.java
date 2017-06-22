package com.chetuan.askapp.util;

import com.chetuan.askapp.model.UserLocation;

public class LocationUtil {

	private static final double EARTH_RADIUS = 6378.137;
	
	private static double rad(double d)
	{
	   return d * Math.PI / 180.0d;
	}
	
	public static double distance(UserLocation l1, UserLocation l2)
	{
		return distance(l1.longitude, l1.latitude, l2.longitude, l2.latitude);
	}
	
	public static double distance(double lng1, double lat1, double lng2, double lat2)
	{
	   double radLat1 = rad(lat1);
	   double radLat2 = rad(lat2);
	   double a = radLat1 - radLat2;
	   double b = rad(lng1) - rad(lng2);

	   double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
	    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	   s = s * EARTH_RADIUS;
	   s = Math.round(s * 10000) / 10000;
	   return s;
	}
	
	public static void main(String[] args) {
		System.out.println(distance(120.1f, 120, 120.2f, 120));
	}
	
}
