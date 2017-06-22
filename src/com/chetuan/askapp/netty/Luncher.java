package com.chetuan.askapp.netty;

import com.chetuan.askapp.dispatch.DispatchCenter;

public class Luncher {
	
	

	public static void main(String[] args) {
		
		//new HttpFileServer(9190);
		
		DispatchCenter.getInstance();
		new NettyServer(9100);
		
		
	}
	
}