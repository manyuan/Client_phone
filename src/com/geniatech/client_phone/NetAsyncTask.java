package com.geniatech.client_phone;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class NetAsyncTask extends AsyncTask<Integer, Integer, String> {
	private static final String TAG = "NetAsyncTask";
	private static final int DIALOG_TIME = 1000; //1s
    private ProgressDialog mDialog;
    private String mIP;
    private String mInfo;
    public NetAsyncTask(String ip, String info,Context cntx) {  
        super();  
        this.mIP = ip;
        mInfo = info;
        this.mDialog = new ProgressDialog(cntx);
    }  

    @Override  
    protected String doInBackground(Integer... params) {  
        if(mIP == null || mIP.equals("0.0.0.0")){
        	multicastListen_r(mInfo);
        }else{
        	sendCmdData_r(mIP,mInfo,Config.LISTEN_TIME_OUT);
        }
        return "abd";  
    }  
  
    @Override  
    protected void onPostExecute(String result){
    	mDialog.setMessage("send complete.");
    	mDialog.show();
    	delay(DIALOG_TIME);
    	mDialog.dismiss();
    }  
    
    @Override  
    protected void onPreExecute() {
    	mDialog.setMessage("sending...");
    	mDialog.show();
    }
    
    @Override  
    protected void onProgressUpdate(Integer... values) {  
         
    }
    
    public void multicastListen_r(String str) {
    	Multicastsender ms = new Multicastsender(str, WifiUtils.getBroadcastIp(), Config.MULTICAST_PORT);
	    ms.send();
    }
    private static boolean sendCmdData_r(String ip,String info,int timeout){
		boolean isSend = true;
        Socket socket = null;  
        BufferedReader br = null;  
        PrintWriter pw = null;  
        try {
            socket = new Socket(ip, Config.DATA_PORT);
            socket.setSoTimeout(timeout);
            if(Config.DEBUG) Log.i(TAG,"--------------------------Socket=" + socket);
            br = new BufferedReader(new InputStreamReader(  
                    socket.getInputStream()));  
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
                    socket.getOutputStream())));  
            pw.println(info);  
            pw.flush();
            if(Config.DEBUG) Log.i(TAG,"--------------------------send data:"+ info);
        } catch (Exception e) {
            e.printStackTrace();
            isSend = false;
        } finally {
            try {
                System.out.println("close......");
                if(br!=null) br.close();
                if(pw!=null) pw.close();
                if(socket!=null) socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
        }
        return isSend;
	}
    public void delay(int ms){
    	try{
    		Thread.sleep(ms);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}  