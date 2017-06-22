package com.chetuan.askapp.redis.dao;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

import com.chetuan.askapp.model.Order;
import com.chetuan.askapp.redis.RedisManager;

public class OrderDao {

	public static boolean save(Order order)
	{
		if(order == null)
		{
			return false;
		}
		Integer id = null;
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	id = jedis.incr("taxi.order.id.max").intValue();
	    	order.id = id;
	    	Map<String,String> map = orderToMap(order);
	    	String key = "taxi.order.id:" + order.id;
	    	jedis.hmset(key, map);
	    	jedis.zadd("taxi.order.user_id:" + order.user_id, order.create_time, "" + id);
	    } catch(Exception e){
	    	e.printStackTrace();
	    	id = null;
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
	    if(id != null)
	    {
	    	return true;
	    }
		return false;
	}
	
	public static boolean update(Order order)
	{
		if(order == null)
		{
			return false;
		}
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	Map<String,String> map = orderToMap(order);
	    	String key = "taxi.order.id:" + order.id;
	    	jedis.hmset(key, map);
	    	if(order.driver_id != 0 && jedis.zrank("taxi.order.driver_id:" + order.driver_id, "" + order.id) == null)
	    	{
	    		jedis.zadd("taxi.order.driver_id:" + order.driver_id, order.create_time, "" + order.id);	    		
	    	}
	    } catch(Exception e){
	    	e.printStackTrace();
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
		return false;
	}
	
	public static boolean remove(Order order, boolean byUser, boolean byDriver)
	{
		boolean flag = false;
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	String key = "taxi.order.id:" + order.id;
	    	if(byUser)
	    	{
	    		jedis.zrem("taxi.order.user_id:" + order.user_id, "" + order.id);
	    		if(order.driver_id == 0 || jedis.zrank("taxi.order.driver_id:" + order.driver_id, "" + order.id) == null)
		    	{
	    			jedis.del(key);
		    	}
	    	}
	    	if(byDriver)
	    	{
	    		jedis.zrem("taxi.order.driver_id:" + order.driver_id, "" + order.id);
	    		if(jedis.zrank("taxi.order.user_id:" + order.user_id, "" + order.id) == null)
		    	{
	    			jedis.del(key);
		    	}
	    	}
	    	flag = true;
	    } catch(Exception e){
	    	e.printStackTrace();
	    	flag = false;
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
		return flag;
	}
	
	public static Order get(String orderId)
	{
		if(orderId == null)
		{
			return null;
		}
		Order order = null;
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	String key = "taxi.order.id:" + orderId;
	    	Map<String, String> map = jedis.hgetAll(key);
	    	order = mapToOrder(map);
	    } catch(Exception e){
	    	e.printStackTrace();
	    	order = null;
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
		return order;
	}
	
	private static Map<String, String> orderToMap(Order order) {
		if(order == null)
		{
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", "" + order.id);
		map.put("user_id", "" + order.user_id);
		map.put("driver_id", "" + order.driver_id);
		map.put("status", "" + order.status);
		map.put("create_time", "" + order.create_time);
		map.put("start_lon", "" + order.start_lon);
		map.put("start_lat", "" + order.start_lat);
		map.put("destination_lon", "" + order.destination_lon);
		map.put("destination_lat", "" + order.destination_lat);
		map.put("money", "" + order.money);
		return map;
	}
	
	private static Order mapToOrder(Map<String, String> map) {
		if(map == null)
		{
			return null;
		}
		
		Order order = new Order();
		try {
			order.id = Long.parseLong(map.get("id"));
			order.user_id = Long.parseLong(map.get("user_id"));
			order.driver_id = Long.parseLong(map.get("driver_id"));
			order.status = Integer.parseInt(map.get("status"));
			order.create_time = Long.parseLong(map.get("create_time"));
			order.start_lon = Double.parseDouble(map.get("start_lon"));
			order.start_lat = Double.parseDouble(map.get("start_lat"));
			order.destination_lon = Double.parseDouble(map.get("destination_lon"));
			order.destination_lat = Double.parseDouble(map.get("destination_lat"));
			order.money = Float.parseFloat(map.get("money"));
		} catch (Exception e) {
			order = null;
		}
		return order;
	}
	
}
