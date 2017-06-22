package com.chetuan.askapp.redis;

import java.io.IOException;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {

	private static JedisPool pool;
	
	private static int preTomIdx = 0;
	
	private static int timeout=2000;


	static {

		try {

			Properties props = new Properties();
			props.load(RedisManager.class.getClassLoader().getResourceAsStream("redis.properties"));

			JedisPoolConfig config = new JedisPoolConfig();
			config.setMinIdle(Integer.valueOf(props.getProperty("jedis.pool.minIdle")));
			config.setMaxIdle(Integer.valueOf(props.getProperty("jedis.pool.maxIdle")));
			config.setMaxWaitMillis(Long.valueOf(props.getProperty("jedis.pool.maxWaitMillis")));
			config.setTestOnBorrow(Boolean.valueOf(props.getProperty("jedis.pool.testOnBorrow")));
			config.setTestOnReturn(Boolean.valueOf(props.getProperty("jedis.pool.testOnReturn")));
			config.setMaxTotal(Integer.valueOf(props.getProperty("jedis.pool.maxActive")));

			preTomIdx = Integer.valueOf(props.getProperty("prepare.tomcat.index"));
			
			timeout = Integer.valueOf(props.getProperty("jedis.pool.timeout"));

			pool = new JedisPool(config, props.getProperty("redis.ip"),
					Integer.valueOf(props.getProperty("redis.port")), timeout);
			
			System.out.println("redis is ready");

		} catch (IOException e) {

			e.printStackTrace();

		}

	}


	public static Jedis getJedisObject() {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
		} catch (Exception e) {
			if (jedis != null)
				pool.returnBrokenResource(jedis);
			jedis = pool.getResource();
			e.printStackTrace();
		}
		return jedis;
	}

	public static void recycleJedisOjbect(Jedis jedis) {
		pool.returnResource(jedis);
	}
	
	public static long getIdx(String key) {
		Long idx = -1l;
		Jedis jedis = getJedisObject();
		idx = jedis.incr(key);
		System.out.println("idx:" + idx);
		recycleJedisOjbect(jedis);
		return idx;
	}

}
