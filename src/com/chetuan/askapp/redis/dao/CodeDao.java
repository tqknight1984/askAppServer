package com.chetuan.askapp.redis.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;

import com.chetuan.askapp.model.Code;
import com.chetuan.askapp.redis.RedisManager;
import com.chetuan.askapp.util.ConfigUtil;

public class CodeDao {

	public static boolean save(Code code)
	{
		if(code == null
			|| StringUtils.isEmpty(code.code)
			|| StringUtils.isEmpty(code.phone))
		{
			return false;
		}
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	Map<String,String> map = codeToMap(code);
	    	String key = "ask.code.phone:" + code.phone;
	    	jedis.hmset(key, map);
	    	jedis.expire(key, ConfigUtil.codeExpire());
	    } catch(Exception e){
	    	e.printStackTrace();
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
		return false;
	}
	
	public static Code get(String phone)
	{
		Code code = null;
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	String key = "ask.code.phone:" + phone;
	    	code = mapToCode(jedis.hgetAll(key));
	    } catch(Exception e){
	    	e.printStackTrace();
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
		return code;
	}

	public static void remove(String phone)
	{
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	String key = "ask.code.phone:" + phone;
	    	jedis.del(key);
	    } catch(Exception e){
	    	e.printStackTrace();
	    }finally {
	    	RedisManager.recycleJedisOjbect(jedis);
	    }
	}
	
	private static Map<String, String> codeToMap(Code code) {
		if(code == null)
		{
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("phone", code.phone);
		map.put("code", code.code);
		map.put("time", "" + code.time);
		return map;
	}
	
	private static Code mapToCode(Map<String, String> map) {
		if(map == null)
		{
			return null;
		}
		
		Code code = new Code();
		try {
			code.phone = map.get("phone");
			code.code = map.get("code");
			code.time = Long.parseLong(map.get("time"));
		} catch (Exception e) {
			code = null;
		}
		return code;
	}
	
}
