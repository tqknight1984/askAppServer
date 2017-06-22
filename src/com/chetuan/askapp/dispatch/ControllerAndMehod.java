package com.chetuan.askapp.dispatch;

import java.lang.reflect.Method;

import com.chetuan.askapp.controller.BaseController;

public class ControllerAndMehod {

	public BaseController controller;
	
	public Method method;

	public ControllerAndMehod(BaseController controller, Method method) {
		this.controller = controller;
		this.method = method;
	}
	
}
