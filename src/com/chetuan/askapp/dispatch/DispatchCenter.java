package com.chetuan.askapp.dispatch;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.chetuan.askapp.controller.BaseController;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.RootUrlRST;
import com.chetuan.askapp.quence.ResponceQuence;
import com.chetuan.askapp.util.ClassUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

public class DispatchCenter {
	
	private static DispatchCenter INSTANCE;
	
	private RootUrlRST rootUrlRST;
	
	private Map<String, Channel> deviceIdAndChannel;
	
	private BiMap<String, String> userIdAndDeviceId;
	
	private Map<String, ResponceQuence> responceQuences;
	
	public ChannelGroup channelGroup;
	
	private Map<String, BaseController> controllerMap;
	
	private Map<String, ControllerAndMehod> methodMap;
	
	private List<Long> receiveMsgTimes;
	
	private int lastMsgRate = 0;
	
	private long lastRateVersion = 0;
	
	private Map<String, DispatchListener> onChannelRemoves;
	
	public void addDispatchListener(String key, DispatchListener listener)
	{
		onChannelRemoves.put(key, listener);
	}
	
	public void removeDispatchListener(String key)
	{
		onChannelRemoves.remove(key);
	}
	
	public static DispatchCenter getInstance() {
		if(INSTANCE == null)
		{
			INSTANCE = new DispatchCenter();
		}
		return INSTANCE;
	}
	
	private DispatchCenter() {
		controllerMap = new HashMap<String, BaseController>();
		methodMap = new HashMap<String, ControllerAndMehod>();
		userIdAndDeviceId = HashBiMap.create();
		deviceIdAndChannel = new HashMap<String, Channel>();
		channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		responceQuences = new HashMap<String, ResponceQuence>();
		
		receiveMsgTimes = new ArrayList<Long>();
		
		onChannelRemoves = new HashMap<String, DispatchListener>();
		
		//获取RootUrlRST的所有属性
		RootUrlRST.Builder builder = RootUrlRST.newBuilder();
		Map<String, FieldDescriptor> descriptors = new HashMap<String, FieldDescriptor>();
		List<FieldDescriptor> fileDescriptors = RootUrlRST.getDescriptor().getFields();
		for(FieldDescriptor descriptor: fileDescriptors)
		{
			if(descriptor.getType() == Type.STRING)
			{
				String name = descriptor.getName();
				descriptors.put(name, descriptor);
			}
		}
		
		Set<Class<?>> classes = ClassUtil.getClasses("com.chetuan.askapp.controller");  
        for (Class<?> clas :classes) {  
        	if(BaseController.class == clas.getSuperclass())
        	{
        		try {
					BaseController controller = (BaseController) clas.newInstance();
					controller.setDispatchCenter(this);
					controllerMap.put(clas.getName(), controller);
					
					Method[] methods = clas.getMethods();
					for(int i = 0; i < methods.length; i ++)
					{
						Method temp = methods[i];
						DispatchMethod dispatchMethod = temp.getAnnotation(DispatchMethod.class);
						if(dispatchMethod != null)
						{
							String path = dispatchMethod.path();
							if(path.equals(""))
							{
								path = temp.getName();
							}
							if(methodMap.containsKey(path))
							{
								try {
									throw(new Exception("存在名字或路径重复的dispatchMethod"));
								} catch (Exception e) {
									e.printStackTrace();
								}
								System.exit(1);
							}
							else {
								System.out.println("path mapping: path=" + path + "  controller=" + controller.getClass().getName() + "  method=" + temp.getName());
								methodMap.put(path, new ControllerAndMehod(controller, temp));
								FieldDescriptor fd = descriptors.get(temp.getName());
								if(fd != null)
								{
									builder.setField(fd, path);
								}
							}
						}
					}
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
        	}
        }
        rootUrlRST = builder.build();
	}
	
	public RootUrlRST getRootUrlRST() {
		return rootUrlRST;
	}
	
	public void bindUserDevice(String userId, String deviceId)
	{
		userIdAndDeviceId.forcePut(userId, deviceId);
	}
	
	public void unbindUserDevice(String userId)
	{
		userIdAndDeviceId.remove(userId);
	}
	
	public String getBindDevice(String userId)
	{
		return userIdAndDeviceId.get(userId);
	}
	
	public String getBindUser(String deviceId)
	{
		return userIdAndDeviceId.inverse().get(deviceId);
	}
	
	public void bindDeviceChannel(String deviceId, Channel channel)
	{
		deviceIdAndChannel.put(deviceId, channel);
	}
	
	public void addChannel(Channel channel)
	{
		channelGroup.add(channel);
	}
	
	public void removeChannel(Channel channel)
	{
		String deviceId = null;
		Iterator<Entry<String, Channel>> it = deviceIdAndChannel.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, Channel> keyVal = it.next();
			if(channel.equals(keyVal.getValue()))
			{
				deviceId = keyVal.getKey();
				break;
			}
		}
		if(deviceId != null)
		{
			Iterator<Map.Entry<String, DispatchListener>> it1 = onChannelRemoves.entrySet().iterator();
			while (it1.hasNext()) {
				it1.next().getValue().onChannelRemove(deviceId, channel);
			}
			deviceIdAndChannel.remove(deviceId);
		}
		channelGroup.remove(channel);
	}
	
	public Channel getChannelByDeviceId(String deviceId)
	{
		Channel Channel = deviceIdAndChannel.get(deviceId);
		return Channel;
	}
	
	public Channel getChannelByUserId(String userId)
	{
		String deviceId = userIdAndDeviceId.get(userId);
		if(deviceId == null)
		{
			return null;
		}
		
		Channel Channel = deviceIdAndChannel.get(deviceId);
		return Channel;
	}
	
	public void writeToAllChannel(ResponceItem responceItem)
	{
		if(responceItem == null)
		{
			return;
		}
		ResponceQuence all = responceQuences.get("quence_all");
		if(all == null)
		{
			all = new ResponceQuence(null, channelGroup, getRequestRate());
			responceQuences.put("quence_all", all);
		}
		else {
			all.setRate(getRequestRate());
		}
		all.addResponce(responceItem);
	}
	
	public void writeToChannel(ResponceItem responceItem, Channel channel)
	{
		if(responceItem == null || channel == null)
		{
			return;
		}
		String str = channel.id().asShortText();
		ResponceQuence quence = responceQuences.get(str);
		if(quence == null)
		{
			quence = new ResponceQuence(channel, null, getRequestRate());
			responceQuences.put(str, quence);
		}
		else {
			quence.setRate(getRequestRate());
		}
		quence.addResponce(responceItem);
	}
	
	public void addController(BaseController controller)
	{
		controllerMap.put(controller.getClass().getSimpleName(), controller);
	}
	
	public BaseController getController(Class<?> clas)
	{
		String clasName = clas.getName();
		BaseController controller = controllerMap.get(clasName);
		if(controller == null)
		{
			try {
				controller = (BaseController) Class.forName(clasName).newInstance();
				controllerMap.put(clasName, controller);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return controller;
	}	

	public void dispatch(Request request)
	{
		receiveMsgTimes.add(new Date().getTime());
		
		ControllerAndMehod controllerAndMehod = methodMap.get(request.getMethod());
		if(controllerAndMehod != null 
				&& controllerAndMehod.controller != null
				&& controllerAndMehod.method != null)
		{
			controllerAndMehod.controller.dispatchAction(controllerAndMehod.method, request);
		}
	}
	
	private int getRequestRate()
	{
		long now = new Date().getTime();
		if(now - lastRateVersion <= 10000)
		{
			return lastMsgRate;
		}
		
		try {
			Long temp = null;
			for(int i = 0; i < receiveMsgTimes.size(); i = 0)
			{
				if(receiveMsgTimes.size() > 0)
				{
					temp = receiveMsgTimes.get(0);
					if(temp == null)
					{
						receiveMsgTimes.remove(i);
					}
					else if(now - receiveMsgTimes.get(i) > 10000)
					{
						receiveMsgTimes.remove(i);
					}
					else
					{
						break;
					}
				}
			}
			temp = null;
			
			if(receiveMsgTimes.size() == 0)
			{
				lastMsgRate = 0;
			}
			else {
				lastMsgRate = (int) (receiveMsgTimes.size() * 1000 / (now - receiveMsgTimes.get(0) + 1000));
				lastMsgRate = Math.max(0, lastMsgRate);
			}
			
			lastRateVersion = now;
		} catch (Exception e) {
		}
		return lastMsgRate;
	}
	
}
