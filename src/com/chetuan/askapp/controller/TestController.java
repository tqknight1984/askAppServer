package com.chetuan.askapp.controller;

import java.util.ArrayList;
import java.util.List;

import com.chetuan.askapp.dispatch.DispatchMethod;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Request.Login;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.LoginRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.Test;
import com.chetuan.askapp.util.ResponceUtil;

public class TestController extends BaseController{

	@DispatchMethod
	public void test(Request request)
	{
		List<Test> tests = new ArrayList<Test>();
		for(int i = 0; i< 10; i ++)
		{
			Test.Builder builder = Test.newBuilder();
			builder.setMsg("testA success");
			tests.add(builder.build());
		}
		ResponceItem responceItem = ResponceUtil.createResponceItem(request.getId(), true, "testA success", tests);
		dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByUserId(request.getUserId()));
	}
	
	@DispatchMethod(path="test/closeChannel")
	public void closeChannel(Request request)
	{
		dispatchCenter.getChannelByUserId(request.getUserId()).close();
	}
	
	
	@DispatchMethod(path="test/test1")
	public void test1(Request request)
	{
		Login login = request.getLogin();
		if(login != null)
		{
			//...
			LoginRST loginRST =  LoginRST.newBuilder()
					.setId(1000)
					.setName("")
					.build();
			ResponceItem responceItem = ResponceUtil.createResponceItem(request.getId(), true, "qweqwe", loginRST);
			dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
		}
	}
	
}
