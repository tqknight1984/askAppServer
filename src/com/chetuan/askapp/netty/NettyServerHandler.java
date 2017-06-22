package com.chetuan.askapp.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import com.chetuan.askapp.constant.Global;
import com.chetuan.askapp.dispatch.DispatchCenter;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.util.ResponceUtil;

public class NettyServerHandler extends ChannelHandlerAdapter {

	private DispatchCenter dispatchCenter = DispatchCenter.getInstance();
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		dispatchCenter.addChannel(ctx.channel());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		dispatchCenter.removeChannel(ctx.channel());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Request request = null;
		System.out.println("this is server >>> "+msg);
			
		try {
			request = (Request) msg;
			if(request != null && request.getId() != null && request.getUserId() != null && request.getMethod() != null)
			{
				String deviceId = request.getDeviceId();
				if(request.getMethod().equals(Global.ROOT_URL))
				{
					dispatchCenter.bindDeviceChannel(request.getDeviceId(), ctx.channel());
					ctx.channel().write(ResponceUtil.createResponce(ResponceUtil.createResponceItem(request.getId(), true, "地址列表获取成功","0000", dispatchCenter.getRootUrlRST())));
				}
				else if(dispatchCenter.getChannelByDeviceId(deviceId) != null){
					dispatchCenter.dispatch(request);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.close();
	}
	
}
