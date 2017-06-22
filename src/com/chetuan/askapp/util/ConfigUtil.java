package com.chetuan.askapp.util;

import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {

	private static int codeExpire;
	
	private static int codeSendInterval;
	
	private static long tokenExpire;
	
	static {
		Properties props = new Properties();
		try {
			props.load(ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties"));
			codeExpire = Integer.parseInt((String) props.get("code.expire"));
			codeSendInterval = Integer.parseInt((String) props.get("code.sendInterval"));
			tokenExpire = Long.parseLong((String) props.get("token.expire"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int codeExpire()
	{
		return codeExpire;
	}
	
	public static int codeSendInterval()
	{
		return codeSendInterval;
	}
	
	public static long tokenExpire()
	{
		return tokenExpire;
	}
	
}
