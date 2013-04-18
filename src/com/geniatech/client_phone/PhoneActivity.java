package com.geniatech.client_phone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PhoneActivity extends Activity {
	private static final String TAG = "PhoneActivity";
	public static final int MSG_REMOTE_INFO = 0;
	public static final int MSG_START_REFRESH = 1;
	
	public static boolean mIsNetThreadRun=true;
	private WifiManager mWifiManager;
	private WifiUtils mWifiUtils;
	private MulticastLock mMultiLock;
	
	private Button mRefreshBtn;
	private Button mListWifiBtn;
	private TextView mTextView;
	private ListView mListView;
	private List<String> mListBoxs;
	private ArrayAdapter<String> mListAdapter;
	DatagramSocket mMultiListenSocket = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone);
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWifiUtils = new WifiUtils(mWifiManager);
		
		mMultiLock=mWifiManager.createMulticastLock("multiLock");
		mMultiLock.acquire();
		//mBoxServiceHandle.send
		
		initViews();
		multicastListen();
		mPhoneHandle.sendEmptyMessageDelayed(MSG_START_REFRESH, 1000);
	}
	
	@Override
	protected void onDestroy() {
		mMultiLock.release();
		mPhoneHandle.removeMessages(MSG_START_REFRESH);
		if(mMultiListenSocket!=null){
			mMultiListenSocket.disconnect();
			mMultiListenSocket.close();
		}
		mIsNetThreadRun = false;
		if(Config.DEBUG) Log.i(TAG,"--=-=-=-=-=-=-->>>>>>>>>>>>onDestroy()");
		super.onDestroy();
		System.exit(0);
	}
	private void initViews(){
		mListView = (ListView)findViewById(R.id.listview1);
		mListBoxs = new ArrayList<String>();
		mListBoxs.add("box devices:");
		mListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mListBoxs);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(mListItemListener);
		
		mRefreshBtn = (Button)findViewById(R.id.button1);
		mRefreshBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mTextView.setText("sent: "+Config.CMD_PHONE_REQUEST_ID);
				//sendCmdData("192.168.1.244","abcdefg1234567");
				sendMulticast(Config.CMD_PHONE_REQUEST_ID);
			}
		});
		mListWifiBtn = (Button)findViewById(R.id.button2);
		mListWifiBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				openWifiList();
			}
		});
		mTextView = (TextView)findViewById(R.id.textView1);
		if(WifiUtils.isWifiConnected()) mTextView.setText("WIFI connected.");
		else mTextView.setText("please connect one WIFI access point not connected.");
	}
	public void multicastListen(){
		new Thread(new Runnable() {
			@Override
			public void run() {
		        while(mIsNetThreadRun) {
		            String str = multicastListen_r();
		            if(Config.DEBUG) Log.i(TAG,"=============== multicastListen ===close==== "+str);
		            if(str == null) continue;
		            MsgUtils msgutil = new MsgUtils(str);
		            Message msg = Message.obtain(mPhoneHandle, MSG_REMOTE_INFO, msgutil);
		            mPhoneHandle.sendMessage(msg);
		        }
			}
		}).start();
	}
	public void cmdListen(){
		new Thread(new Runnable() {
			public void run() {
				while(mIsNetThreadRun){
				ServerSocket s = null;  
		        Socket socket = null;  
		        BufferedReader br = null;  
		        PrintWriter pw = null;  
		        try {
		            s = new ServerSocket(Config.DATA_PORT);
		            s.setSoTimeout(Config.LISTEN_TIME_OUT);
		            
		            socket = s.accept();
		            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		            String str = br.readLine(); 
		            while(str!=null){
			            MsgUtils msgutil = new MsgUtils(str);
			            Message msg = Message.obtain(mPhoneHandle, MSG_REMOTE_INFO, msgutil);
			            mPhoneHandle.sendMessage(msg);
			            str = br.readLine();
		            }
		            
		        } catch (Exception e) {
		            e.printStackTrace();
		            if(Config.DEBUG) Log.i(TAG,"============cmdListen!!====close...==============");
		        }finally{
		            try {  
		                if(br!=null) br.close();  
		                //pw.close();  
		                if(socket!=null) socket.close();  
		                if(s!=null) s.close();  
		            } catch (Exception e2) {  
		                e2.printStackTrace();
		            }  
		        }  
				}
			}
		}).start();
	}
	public static void sendMulticast(String info){
		final String str = info;
		new Thread(new Runnable() {
			Multicastsender ms = new Multicastsender(str, Config.MULTICAST_SEND_HOST, Config.MULTICAST_PORT);
			@Override
			public void run() {
		        ms.send();
		        if(Config.DEBUG) Log.i(TAG,"--------------------------send multicast:"+ str);
			}
		}).start();
	}
	public static void sendCmdData(String ip,String info){
		final String str = info;
		final String ip_ = ip;
		new Thread(new Runnable() {
			@Override
			public void run() {
		        Socket socket = null;  
		        BufferedReader br = null;  
		        PrintWriter pw = null;  
		        try {
		            socket = new Socket(ip_, Config.DATA_PORT);  
		            if(Config.DEBUG) Log.i(TAG,"--------------------------Socket=" + socket);
		            br = new BufferedReader(new InputStreamReader(  
		                    socket.getInputStream()));  
		            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
		                    socket.getOutputStream())));  
	                pw.println(str);  
	                pw.flush();
	                if(Config.DEBUG) Log.i(TAG,"--------------------------send data:"+ str);
		        } catch (Exception e) {
		            e.printStackTrace();
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
			}
		}).start();
	}
	private void openWifiList(){
		Intent intent = new Intent();
		intent.setClass(this, com.geniatech.client_phone.wifi.WifiSettings.class);
		startActivity(intent);
	}
	public String multicastListen_r() {
        byte[] data = new byte[256];
        String message = null;
        
        try {
            InetAddress ip = InetAddress.getByName(Config.MULTICAST_SEND_HOST);
            mMultiListenSocket = new DatagramSocket(Config.MULTICAST_PORT);
            //mMultiListenSocket.setSoTimeout(Config.LISTEN_TIME_OUT);
            
            DatagramPacket packet = new DatagramPacket(data, data.length);
            mMultiListenSocket.receive(packet);
            message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(message);
            mMultiListenSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            if(mMultiListenSocket!=null) mMultiListenSocket.close();
            message = null;
        }
        return message;
    }
	private OnItemClickListener mListItemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(Config.DEBUG) Log.i(TAG,"==========listView===arg2:"+arg2+"===arg3:"+arg3);
			String str = mListBoxs.get(arg2);
			MsgUtils msg = new MsgUtils(str);
			String ip = msg.getInfo(Config.CMD_BOX_IP);
			showDialog(ip);
		}
	};
	private void showDialog(String ip){
		final String ipAddress = ip;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Change to Soft AP mode?");
		builder.setNegativeButton("cancel", null);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				sendCmdChang2ap(ipAddress);
			}
		});
		builder.create().show();
	}
	private void sendCmdChang2ap(String ip){
		sendCmdData(ip, Config.CMD_BOX_WIFI_2_AP);
	}
	private Handler mPhoneHandle = new Handler(){
        public void handleMessage(Message msg) {
            if(MSG_REMOTE_INFO==msg.what){
            	MsgUtils msgutil = (MsgUtils)msg.obj;
            	handleRemoteInfo(msgutil);
            }else if(MSG_START_REFRESH == msg.what){
            	sendMulticast(Config.CMD_PHONE_REQUEST_ID);
            	sendEmptyMessageDelayed(MSG_START_REFRESH, 5000);
            }
        }
    };
    private void handleRemoteInfo(MsgUtils msgutil){
    	String cmd = msgutil.getCmd();
    	if(cmd==null) return;
    	if(Config.DEBUG) Log.i(TAG,"===============handleRemoteInfo=======>>msg cmd:"+cmd);
    	if(cmd.equals(Config.CMD_BOX_REPORT_ID)){
    		mTextView.setText(msgutil.toString());
    		String tmp = msgutil.toString();
    		//tmp = tmp.replaceAll("#", "    ");
    		//tmp = tmp.replaceAll("=", ":");
    		//tmp = tmp.replaceAll("rptID", "");
    		//if(!mListBoxs.contains(tmp)){
    			mListBoxs.add(tmp);
    			mListAdapter.notifyDataSetChanged();
    		//}
    		if(Config.DEBUG) Log.i(TAG,"===============CMD_BOX_REPORT_ID=======>>:"+msgutil.toString());
    	}
    }
}
