package com.chetuan.askapp.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by YT on 2015/11/11.
 */
public class ModelUtil {

    private static Gson mGson = new Gson();
    
    private static JsonParser parser = new JsonParser();
    
	public static <T> T jsonToModel(JsonElement jsonElement, Class<T> clazz)
    {
        T t = null;
        try
        {
            t = (T) mGson.fromJson(jsonElement, clazz);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> String modelToJsonStr( T t)
    {
        String json = "";
        try
        {
            json = mGson.toJson(t);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        return json;
    }

    public static <T> JsonElement modelToJson( T t)
    {
        JsonElement json = null;
        try
        {
            json = mGson.toJsonTree(t);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        return json;
    }
    
    public static JsonObject strToJsonObject(String str)
    {
        JsonObject json = null;
        try
        {
        	json = parser.parse(str).getAsJsonObject();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        return json;
    }
    
    public static JsonArray strToJsonArray(String str)
    {
    	JsonArray json = null;
        try
        {
        	json = parser.parse(str).getAsJsonArray();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        return json;
    }
    
    public static boolean hasParas(JsonObject json, String ... paras)
    {
    	if(json == null)
    	{
    		return false;
    	}
    	else if(paras == null)
    	{
    		return true;
    	}
    		
    	int size = paras.length;
    	for(int i = size; i < size; i ++)
    	{
    		if(!json.has(paras[i]))
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
}
