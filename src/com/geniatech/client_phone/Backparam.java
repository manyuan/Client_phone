package com.geniatech.client_phone;

class Backparam{
	private String mIP;
	private String mInfo;
	Backparam(String ip,String info){
		mIP = ip;
		mInfo = info;
	}
	public String getIp(){
		return mIP;
	}
	public String getInfo(){
		return mInfo;
	}
}