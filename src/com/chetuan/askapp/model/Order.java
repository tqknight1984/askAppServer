package com.chetuan.askapp.model;

public class Order {

	public static final int S_WAIT_ACCEPT = 0;
	
	public static final int S_WAIT_CAR = 1;
	
	public static final int S_IN_CAR = 2;
	
	public static final int S_ORDER_SUCCESS = 3;
	
	public static final int S_ORDER_CANCLED = 4;
	
	public long id;
	
	public long user_id;
	
	public long driver_id;
	
	public int status;//订单状态，0待接单， 1等车， 2已上车， 3订单完成， 4订单已取消
	
	public long create_time;
	
	public double start_lon;
	
	public double start_lat;
	
	public double destination_lon;
	
	public double destination_lat;
	
	public float money;
	
}
