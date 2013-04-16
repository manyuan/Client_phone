package com.geniatech.client_phone;

public class MsgUtils{
	private String mMessage;
	MsgUtils(String msg){
		mMessage = msg;
	}
	@Override
	public String toString(){
		return mMessage;
	}
	public String getCmd(){
		String[] cmd = null;
		if(mMessage!=null){
			cmd = mMessage.split("#");
		}
		if(cmd!=null){
			return cmd[0];
		}else{
			return null;
		}
	}
	public String getInfo(String info){
		String[] cmds = null;
		if(mMessage!=null){
			cmds = mMessage.split("#");
		}
		if(cmds == null) return null;
		
		for(String data:cmds){
			if(data.contains(info)){
				String res = data.replace(info, "");
				return res;
			}
		}
		return null;
	}
	public static String getDefReportString(){
		return Config.CMD_BOX_REPORT_ID + "#"
				+Config.CMD_BOX_ID+"6666#"
				+Config.CMD_BOX_IP+"192.168.1.255";
	}
	public static MsgUtils getDefWifi2ApMsg(){
		return new MsgUtils(Config.CMD_BOX_WIFI_2_AP + 
				"#" + Config.CMD_BOX_SSID + Config.DEF_AP_SSID +
				"#" + Config.CMD_BOX_SEC_TYPE + Config.DEF_AP_SEC_TYPE +
				"#" + Config.CMD_BOX_PASSWORD + Config.DEF_AP_PASSWORD);
	}
}