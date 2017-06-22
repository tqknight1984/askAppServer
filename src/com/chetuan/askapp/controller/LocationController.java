package com.chetuan.askapp.controller;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.chetuan.askapp.dispatch.DispatchListener;
import com.chetuan.askapp.dispatch.DispatchMethod;
import com.chetuan.askapp.model.Order;
import com.chetuan.askapp.model.UserLocation;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Request.Location;
import com.chetuan.askapp.model.ModelPrt.Request.OrderInfo;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.AroundDriverRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.LocationRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.LoginRST;
import com.chetuan.askapp.redis.dao.OrderDao;
import com.chetuan.askapp.util.LocationUtil;
import com.chetuan.askapp.util.ResponceUtil;

public class LocationController extends BaseController{
	
	private static final String FLOAT_FORMATE = "%1$.1f";
	
	private Map<String, HashMap<String, UserLocation>> driverLocationsMap = new HashMap<String, HashMap<String, UserLocation>>();
	
	private Map<String, String> deviceIdAndDriverLocation = new HashMap<String, String>();
	
	private Map<String, UserLocation> driverLocations = new HashMap<String, UserLocation>();
	
	private Map<String, UserLocation> userLocations = new HashMap<String, UserLocation>();
	
	private UserController userController;
	
	private UserController getUserController() {
		if(userController == null)
		{
			userController = (UserController) dispatchCenter.getController(UserController.class);
		}
		return userController;
	}
	
	@Override
	protected void addListener() {
		dispatchCenter.addDispatchListener(LocationController.class.getSimpleName() + ".clearLocation", new DispatchListener() {
			@Override
			public void onChannelRemove(String deviceId, Channel channel) {
				userLocations.remove(deviceId);
				String locationKey = deviceIdAndDriverLocation.get(deviceId);
				if(locationKey != null)
				{
					HashMap<String, UserLocation> tempLocations = driverLocationsMap.get(locationKey.split(":")[0]);
					if(tempLocations != null)
					{
						tempLocations.remove(deviceId);
					}
					deviceIdAndDriverLocation.remove(deviceId);
				}
			}
		});
	}
	
//	@DispatchMethod(path="location/update")
	public void update(Request request)
	{
		Location location = request.getLocation();
		if(location != null)
		{
			long now = new Date().getTime();
			String deviceId = request.getDeviceId();
//			if(request.getClientType() == 0)
//			{
//				//普通用户 位置更新
//				UserLocation userLocation = userLocations.get(deviceId);
//				if(userLocation == null)
//				{
//					userLocation = new UserLocation();
//					userLocation.userId = request.getUserId();
//					userLocation.deviceId = deviceId;
//					userLocation.longitude = location.getLongitude();
//					userLocation.latitude = location.getLatitude();
//					userLocation.updateTime = now;
//					
//					userLocations.put(deviceId, userLocation);
//				}
//				else {
//					userLocation.longitude = location.getLongitude();
//					userLocation.latitude = location.getLatitude();
//					userLocation.updateTime = now;
//				}
////				System.out.println(userLocations);
//			}
//			else {
//				UserLocation userLocation = driverLocations.get(deviceId);
//				if(userLocation == null)
//				{
//					userLocation = new UserLocation();
//					userLocation.userId = request.getUserId();
//					userLocation.deviceId = request.getDeviceId();
//					userLocation.longitude = location.getLongitude();
//					userLocation.latitude = location.getLatitude();
//					userLocation.updateTime = now;
//					
//					driverLocations.put(deviceId, userLocation);
//				}
//				else {
//					userLocation.longitude = location.getLongitude();
//					userLocation.latitude = location.getLatitude();
//					userLocation.updateTime = now;
//				}
//				
//				String oldLocationDevice = deviceIdAndDriverLocation.get(deviceId);
//				String newLocationKey = String.format(FLOAT_FORMATE, userLocation.longitude)
//						+ "_" + String.format(FLOAT_FORMATE, userLocation.latitude);
//				String oldLocationKey = (oldLocationDevice == null?null: oldLocationDevice.split(":")[0]);
//				if(StringUtils.isEmpty(oldLocationDevice))
//				{
//					deviceIdAndDriverLocation.put(deviceId, newLocationKey + ":" + deviceId);
//					HashMap<String, UserLocation> tempLocations = driverLocationsMap.get(newLocationKey);
//					if(tempLocations == null)
//					{
//						tempLocations = new HashMap<String, UserLocation>();
//						driverLocationsMap.put(newLocationKey, tempLocations);
//					}
//					tempLocations.put(deviceId, userLocation);
//				}
//				else if(!newLocationKey.equals(oldLocationKey)) {
//					HashMap<String, UserLocation> oldTempLocations = driverLocationsMap.get(oldLocationKey);
//					if(oldTempLocations != null)
//					{
//						oldTempLocations.remove(deviceId);
//					}
//					
//					HashMap<String, UserLocation> newTempLocations = driverLocationsMap.get(newLocationKey);
//					if(newTempLocations == null)
//					{
//						newTempLocations = new HashMap<String, UserLocation>();
//						driverLocationsMap.put(newLocationKey, newTempLocations);
//					}
//					newTempLocations.put(deviceId, userLocation);
//				}
////				System.out.println(driverLocationsMap);
//			}
		}
	}
	
	@DispatchMethod(path="location/aroundDriver")
	public void aroundDriver(Request request)
	{
		Location location = request.getLocation();
		if(location != null)
		{
			List<AroundDriverRST> aroundDrivers = getAroundDrivers(location.getLongitude(), location.getLatitude());
			int size = aroundDrivers.size();
			ResponceItem responceItem = ResponceUtil.createResponceItem(request.getId(), true, 
					 size == 0?"暂无可用车辆": "周围共" + size + "辆车可用", aroundDrivers);
			dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
		}
	}
	
	@DispatchMethod(path="location/updateAndGet")
	public void updateAndGetLocation(Request request)
	{
		Location location = request.getLocation();
		if(location != null)
		{
			update(request);
			
			ResponceItem responceItem = null;
			OrderInfo orderInfo = request.getOrderInfo();
			if(orderInfo != null)
			{
				Order order = OrderDao.get(orderInfo.getOrderId());
				if(order != null && orderInfo.getUserId().equals("" + order.user_id) && orderInfo.getDriverId().equals("" + order.driver_id))
				{
					//订单信息匹配，可以获取用户/司机的位置信息
//					if(request.getClientType() == 0 && request.getUserId().equals("" + order.user_id))
//					{
//						//普通用户端，获取司机的位置信息
//						LocationRST locationRST = getDriverLocation(orderInfo.getDriverId());
//						if(locationRST != null)
//						{
//							responceItem = ResponceUtil.createResponceItem(request.getId(), true, "获取司机位置成功", locationRST);
//						}
//					}
//					else if(request.getClientType() == 1 && request.getUserId().equals("" + order.driver_id))
//					{
//						//司机端，获取普通用户的位置信息
//						LocationRST locationRST = getUserLocation(orderInfo.getUserId());
//						if(locationRST != null)
//						{
//							responceItem = ResponceUtil.createResponceItem(request.getId(), true, "获取用户位置成功", locationRST);
//						}
//					}
				}
			}
			
			if(responceItem != null)
			{
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}
	
	public LocationRST getDriverLocation(String driverId)
	{
		String deviceId = dispatchCenter.getBindDevice(driverId);
		if(deviceId != null)
		{
			UserLocation userLocation = driverLocations.get(deviceId);
			if(userLocation != null)
			{
				return LocationRST.newBuilder()
						.setLongitude(userLocation.longitude)
						.setLatitude(userLocation.latitude)
						.build();
			}
		}
		return null;
	}
	
	public LocationRST getUserLocation(String userId)
	{
		String deviceId = dispatchCenter.getBindDevice(userId);
		if(deviceId != null)
		{
			UserLocation userLocation = userLocations.get(deviceId);
			if(userLocation != null)
			{
				return LocationRST.newBuilder()
						.setLongitude(userLocation.longitude)
						.setLatitude(userLocation.latitude)
						.build();
			}
		}
		return null;
	}
	
	public List<AroundDriverRST> getAroundDrivers(double lon, double lat)
	{
		double longitude = Double.parseDouble(String.format(FLOAT_FORMATE, lon));
		double latitude = Double.parseDouble(String.format(FLOAT_FORMATE, lat));
		List<AroundDriverRST> aroundDrivers = new ArrayList<AroundDriverRST>();
		for(double i = -0.1d; i <= 0.1d; i += 0.1d)
		{
			for(double j = -0.1d; j <= 0.1d; j += 0.1d)
			{
				//循环扫描周围九个经纬度群里面的点
				String locationKey = String.format(FLOAT_FORMATE, longitude + i)
						+ "_" + String.format(FLOAT_FORMATE, latitude + j);
				HashMap<String, UserLocation> tempLocations = driverLocationsMap.get(locationKey);
				if(tempLocations != null && tempLocations.size() > 0)
				{
					Iterator<Entry<String, UserLocation>> it = tempLocations.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, UserLocation> keyVal = it.next();
						UserLocation ul = keyVal.getValue();
						//TODO 暂未考虑司机的状态，后续加上
						if(getUserController().isDriverAvailabel(ul.userId) && LocationUtil.distance(longitude, latitude, 
								ul.longitude, ul.latitude) <= 5)
						{
							AroundDriverRST temp = AroundDriverRST.newBuilder()
									.setUserId(ul.userId)
									.setDeviceId(ul.deviceId)
									.setLongitude(ul.longitude)
									.setLatitude(ul.latitude)
									.build();
							aroundDrivers.add(temp);
						}
					}
				}
			}
		}
		return aroundDrivers;
	}
	
}
