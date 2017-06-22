package com.chetuan.askapp.redis.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.chetuan.askapp.model.User;
import com.chetuan.askapp.redis.RedisManager;

import redis.clients.jedis.Jedis;

public class UserDao extends BaseRedisDao{

	public static boolean isPhoneUsed(String phone) {
		String id = null;
		Jedis jedis = RedisManager.getJedisObject();
		try {
			id = jedis.get("ask.user.phone:" + phone);
		} catch (Exception e) {
			e.printStackTrace();
			id = null;
		} finally {
			RedisManager.recycleJedisOjbect(jedis);
		}
		return StringUtils.isNotEmpty(id);
	}

	public static boolean register(User user)
	{
		Integer id = null;
		Jedis jedis = RedisManager.getJedisObject();
	    try {
	    	if(user == null || StringUtils.isBlank(user.phone) ||StringUtils.isBlank(user.password)){
	    		return false;
	    	}
	    	
	    	//生成用户主键
	    	id = jedis.incr("ask.user.id.max").intValue();
	    	user.id = id;
	    	Map<String,String> map = userToMap(user);
	    	jedis.hmset(user_detail_Map + id, map);
	    	jedis.set("ask.user.phone:" + user.phone, "" + id);
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

	public static boolean updateUser(User user) {
		boolean flag = true;
		if (user == null || user.id == 0) {
			return false;
		}
		Jedis jedis = RedisManager.getJedisObject();
		try {
			Map<String, String> map = userToMap(user);
			jedis.hmset("ask.user.id:" + user.id, map);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		} finally {
			RedisManager.recycleJedisOjbect(jedis);
		}
		return flag;
	}

	public static User getUserMapById(String id) {
		Map<String, String> map = null;
		Jedis jedis = RedisManager.getJedisObject();
		try {
			map = jedis.hgetAll("ask.user.id:" + id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RedisManager.recycleJedisOjbect(jedis);
		}
		return mapToUser(map);
	}

	public static User getUserMapByPhone(String phone) {
		Map<String, String> map = null;
		Jedis jedis = RedisManager.getJedisObject();
		try {
			String id = jedis.get("ask.user.phone:" + phone);
			if (StringUtils.isNotEmpty(id)) {
				map = jedis.hgetAll("ask.user.id:" + id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RedisManager.recycleJedisOjbect(jedis);
		}
		return mapToUser(map);
	}

	public static Long getUserIdByPhone(String phone) {
		Long id = null;
		Jedis jedis = RedisManager.getJedisObject();
		try {
			String idStr = jedis.get("ask.user.phone:" + phone);
			if (StringUtils.isNotEmpty(idStr)) {
				id = Long.parseLong(idStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RedisManager.recycleJedisOjbect(jedis);
		}
		return id;
	}

	public static boolean deleteUserById(User user) {
		boolean flag = false;
		Jedis jedis = RedisManager.getJedisObject();
		try {
			jedis.del("ask.user.id:" + user.id);
			jedis.del("ask.user.phone:" + user.phone);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RedisManager.recycleJedisOjbect(jedis);
		}
		return flag;
	}

	private static final Map<String, String> userToMap(User user) {
		if (user == null) {
			return null;
		}
		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put("id", "" + user.id);
		userMap.put("name", user.name);
		userMap.put("phone", user.phone);
		userMap.put("token", user.token);
		userMap.put("password", user.password);
		userMap.put("token_expire_time", "" + user.token_expire_time);
		userMap.put("registe_time", "" + user.registe_time);
		return userMap;
	}

	private static final User mapToUser(Map<String, String> map) {
		if (map == null) {
			return null;
		}
		User user = new User();
		try {
			user.id = Long.parseLong(map.get("id"));
			user.name = map.get("name");
			user.phone = map.get("phone");
			user.password = map.get("password");
			user.token = map.get("token");
			user.token_expire_time = Long.parseLong(map.get("token_expire_time"));
			user.registe_time = Long.parseLong(map.get("registe_time"));
		} catch (Exception e) {
			return null;
		}
		return user;
	}

	public static void main(String[] args) {
		User user = new User();
		user.name = "邓伯操";
		user.phone = "18918273895";
		user.token = "";
		user.registe_time = new Date().getTime();
		// if(register(user))
		// {
		// System.out.println(user.id);
		// }

		deleteUserById(user);

		System.out.println(isPhoneUsed("18918273895"));
	}

}
