package com.chetuan.askapp.quence;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chetuan.askapp.model.ModelPrt.Responce;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.util.ResponceUtil;

public class ResponceQuence {

	private Channel channel;
	
	private ChannelGroup channelGroup;
	
	private List<ResponceItem> responces;
	
	private int maxResNum = 100;
	
	private long maxWaitTime = 1000;
	
	private Timer timer;
	
	public ResponceQuence(Channel channel, ChannelGroup channelGroup, int msgRate)
	{
		this.channel = channel;
		this.channelGroup = channelGroup;
		responces = Collections.synchronizedList(new ArrayList<ResponceItem>());
		
		setRate(msgRate);
		
		timer = new Timer(maxWaitTime) {
			@Override
			public void doWhat() {
				responce();
			}
		};
	}
	
	public void setRate(int msgRate)
	{
		if(msgRate <= 10)
		{
			this.maxResNum = 10;
			this.maxWaitTime = 100;
		}
		else if(msgRate > 10 && msgRate <= 40)
		{
			this.maxResNum = 100;
			this.maxWaitTime = 500;
		}
		else if(msgRate > 40 && msgRate <= 100)
		{
			this.maxResNum = 250;
			this.maxWaitTime = 1500;
		}
		else if(msgRate > 100 && msgRate <= 500)
		{
			this.maxResNum = 500;
			this.maxWaitTime = 2000;
		}
		else
		{
			this.maxResNum = 1000;
			this.maxWaitTime = 3000;
		}
	}

	public void addResponce(ResponceItem responceItem)
	{
		responces.add(responceItem);
		if(responces.size() >= maxResNum)
		{
			responce();
		}
		else if(timer.isShutDown()){
			timer = new Timer(maxWaitTime) {
				@Override
				public void doWhat() {
					responce();
				}
			};
		}
	}
	
	private void responce() {
		if((channel == null && channelGroup == null) || responces.size() == 0)
		{
			return;
		}
		else {
			Responce res = ResponceUtil.createResponce(responces);
			responces.clear();
			if(channel != null)
			{
				channel.writeAndFlush(res);
			}
			else if(channelGroup != null)
			{
				channelGroup.writeAndFlush(res);
			}
		}
	}
	
	class Timer extends Thread
	{
		private boolean isShutDown = false;
		
		private long waitTime = 0;
		
		public Timer(long waitTime) {
			super();
			this.waitTime = waitTime;
			start();
		}
		
		public void shutDown() {
			isShutDown = true;
		}
		
		public boolean isShutDown()
		{
			return isShutDown;
		}

		@Override
		public void run() {
			try {
				sleep(waitTime);
				if(!isShutDown)
				{
					doWhat();
					isShutDown = true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void doWhat() {
			
		}
	}
	
}
