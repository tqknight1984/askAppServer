package com.chetuan.askapp.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.chetuan.askapp.constant.Global;
import com.chetuan.askapp.dispatch.DispatchMethod;
import com.chetuan.askapp.model.Order;
import com.chetuan.askapp.model.User;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Request.DestinationLoc;
import com.chetuan.askapp.model.ModelPrt.Request.Location;
import com.chetuan.askapp.model.ModelPrt.Request.OrderAccept;
import com.chetuan.askapp.model.ModelPrt.Request.OrderInfo;
import com.chetuan.askapp.model.ModelPrt.Request.StartLoc;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.AroundDriverRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.LocationRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.OrderAcceptRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.OrderCreateFailRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.OrderPush;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.OrderUpdatePush;
import com.chetuan.askapp.redis.dao.OrderDao;
import com.chetuan.askapp.util.ResponceUtil;

public class OrderController extends BaseController{
	
	private LocationController locationController;
	
	private UserController userController;
	
	private LocationController getLocationController() {
		if(locationController == null)
		{
			locationController = (LocationController) dispatchCenter.getController(LocationController.class);
		}
		return locationController;
	}
	
	private UserController getUserController() {
		if(userController == null)
		{
			userController = (UserController) dispatchCenter.getController(UserController.class);
		}
		return userController;
	}
	
	@DispatchMethod(path="order/create")
	public void orderCreate(Request request)
	{
		StartLoc startLoc = request.getStartLoc();
		DestinationLoc destinationLoc = request.getDestinationLoc();
		if(startLoc != null && destinationLoc != null)
		{
			String userId = request.getUserId();
			ResponceItem responceItem = null;
			if(getUserController().isUserLogin(userId, request.getTempToken()))
			{
				//用户已经登录
				Order order = new Order();
				order.user_id = Integer.parseInt(request.getUserId());
				order.status = Order.S_WAIT_ACCEPT;//等待接单
				order.create_time = new Date().getTime();
				order.start_lon = startLoc.getLongitude();
				order.start_lat = startLoc.getLatitude();
				order.destination_lon = destinationLoc.getLongitude();
				order.destination_lat = destinationLoc.getLatitude();
				
				if(OrderDao.save(order))
				{
					//订单创建成功
					responceItem = ResponceUtil.createResponceItem(request.getId(), true, "下单成功，请等待接单");
					
					//将订单推送给周围的司机
					OrderPush orderPush = OrderPush.newBuilder()
							.setOrderId("" + order.id)
							.setUserId("" + order.user_id)
							.setCreateTime(order.create_time)
							.setStartLon(order.start_lon)
							.setStartLat(order.start_lat)
							.setDestinationLon(order.destination_lon)
							.setDestinationLat(order.destination_lat)
							.build();
					ResponceItem pushItem = ResponceUtil.createResponceItem(Global.PUSH_ORDER, true, "接收到新的订单", orderPush); 
					
					List<AroundDriverRST> drivers = getLocationController().getAroundDrivers(order.start_lon, order.start_lat);
					int size = drivers.size();
					for (int i = 0; i < size; i++) {
						dispatchCenter.writeToChannel(pushItem, dispatchCenter.getChannelByDeviceId(drivers.get(i).getDeviceId()));	
					}
				}
				else {
					//订单创建失败
					OrderCreateFailRST failRST = OrderCreateFailRST.newBuilder()
							.setReason(1)
							.build();
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "下单失败，请重试", failRST);
				}
			}
			else {
				OrderCreateFailRST failRST = OrderCreateFailRST.newBuilder()
						.setReason(0)
						.build();
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "您还没有登录", failRST);
			}
			
			if(responceItem != null)
			{
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}
	
	@DispatchMethod(path="order/accept")
	public void orderAccept(Request request)
	{
		OrderAccept orderAccept = request.getOrderAccept();
		Location location = request.getLocation();
		if(orderAccept != null && location != null )
		{
			//更新司机位置
			getLocationController().update(request);
			
			ResponceItem responceItem = null;
			String driverId = request.getUserId();
			Order order = OrderDao.get(orderAccept.getOrderId());
			if(order != null && ("" + order.id).equals(orderAccept.getUserId()))
			{
				if(order.status != Order.S_WAIT_ACCEPT)
				{
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, order.status == Order.S_ORDER_CANCLED?"订单已经取消": "订单已经被抢");
				}
				else {
					order.status = Order.S_WAIT_CAR;
					order.driver_id = Long.parseLong(driverId);
					if(OrderDao.update(order))
					{
						//更新司机状态
						getUserController().updateDriverStatus(driverId, false);
						
						OrderAcceptRST orderAcceptRST = OrderAcceptRST.newBuilder()
								.setOrderId("" + order.id)
								.setUserId("" + order.user_id)
								.setDriverId("" + order.driver_id)
								.build();
						responceItem = ResponceUtil.createResponceItem(request.getId(), true, "接单成功", orderAcceptRST);
					}
					else
					{
						responceItem = ResponceUtil.createResponceItem(request.getId(), false, "接单失败");
					}
				}
			}
			else {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "订单不存在");
			}
			
			if(responceItem != null)
			{
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}
	
	@DispatchMethod(path="order/update")
	public void orderUpdate(Request request)
	{
		OrderInfo orderInfo = request.getOrderInfo();
		if(orderInfo != null)
		{
			ResponceItem responceItem = null;
			
			if(getUserController().isUserLogin(request.getUserId(), request.getTempToken()))
			{
				Order order = OrderDao.get(orderInfo.getOrderId());
				if(order != null && orderInfo.getUserId().equals("" + order.user_id))
				{
					order.status = orderInfo.getStatus();
					if(OrderDao.update(order))
					{
						if(order.status == Order.S_IN_CAR)
						{
							getUserController().updateDriverStatus(orderInfo.getDriverId(), false);
						}
						else
						{
							getUserController().updateDriverStatus(orderInfo.getDriverId(), true);
						}
						responceItem = ResponceUtil.createResponceItem(request.getId(), true, "订单信息更新成功");
						
						//将该消息推送给司机
						OrderUpdatePush orderUpdatePush = OrderUpdatePush.newBuilder()
								.setOrderId("" + order.id)
								.setUserId("" + order.user_id)
								.setDriverId("" + order.driver_id)
								.setStatus(order.status)
								.build();
						ResponceItem pushItem = ResponceUtil.createResponceItem(Global.PUSH_ORDER_UPDATE, true, "订单状态改变", orderUpdatePush);
						dispatchCenter.writeToChannel(pushItem, dispatchCenter.getChannelByUserId("" + order.driver_id));
					}
					else {
						responceItem = ResponceUtil.createResponceItem(request.getId(), false, "订单信息更新失败，请重试");
					}
				}
				else {
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "订单信息有误");
				}
			}
			else {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "您还没有登录");
			}
			
			if(responceItem != null)
			{
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}
	
}
