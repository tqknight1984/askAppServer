package com.chetuan.askapp.dispatch;

import io.netty.channel.Channel;

public interface DispatchListener {

	void onChannelRemove(String deviceId, Channel channel);
	
}
