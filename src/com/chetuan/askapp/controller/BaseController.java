package com.chetuan.askapp.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.chetuan.askapp.dispatch.DispatchCenter;
import com.chetuan.askapp.model.ModelPrt.Request;

public abstract class BaseController {

	protected DispatchCenter dispatchCenter;
	
	public BaseController() {
	}
	
	public final void setDispatchCenter(DispatchCenter dc) {
		this.dispatchCenter = dc;
		addListener();
	}
	
	protected void addListener() {
	}
	
	public final void dispatchAction(Method method, Request request)
	{
		if(method != null)
		{
			try {
				method.invoke(this, request);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
}
