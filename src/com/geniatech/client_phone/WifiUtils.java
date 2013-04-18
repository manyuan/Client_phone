package com.geniatech.client_phone;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiUtils{
	private static WifiManager mWifiManager;
	WifiUtils(WifiManager wifim){
		mWifiManager = wifim;
	}
	public static boolean isWifiConnected(){
		int state = mWifiManager.getWifiState();
		if(state!=WifiManager.WIFI_STATE_ENABLED){
			return false;
		}
		WifiInfo info = mWifiManager.getConnectionInfo();
		if(info.getIpAddress()!=0){
			return true;
		}else{
			return false;
		}
	}
	public static String getActiveSSID(){
		if(isWifiConnected()){
			WifiInfo info = mWifiManager.getConnectionInfo();
			String ssid = info.getSSID();
			return ssid;
		}else{
			return null;
		}
	}
}