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

import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneActivity extends Activity {
	private static final String TAG = "PhoneActivity";
	public static final int MSG_REMOTE_INFO = 0;
	public static final int MSG_START_REFRESH = 1;
	public static final int MSG_REFRESH_UI = 2;
	public static final int MSG_CONNECT_ERROR = 3;
	
	public static final String SPLIT_TAG = "\n    ";
	
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
	private boolean mUpdateList = false;
	private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            	mTextView.setText("Current AP is:  "+WifiUtils.getActiveSSID());
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            	mTextView.setText("Current AP is:  "+WifiUtils.getActiveSSID());
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            	mTextView.setText("Current AP is:  "+WifiUtils.getActiveSSID());
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone);
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWifiUtils = new WifiUtils(mWifiManager);
		
		mMultiLock=mWifiManager.createMulticastLock("multiLock");
		mMultiLock.acquire();
		//mBoxServiceHandle.send
		
		mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        
		initViews();
		multicastListen();
		mPhoneHandle.sendEmptyMessageDelayed(MSG_START_REFRESH, 1000);
	}
	
	@Override
	protected void onDestroy() {
		mMultiLock.release();
		mPhoneHandle.removeMessages(MSG_START_REFRESH);
		mPhoneHandle.removeMessages(MSG_REFRESH_UI);
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
		//mListBoxs.add("box devices:");
		mListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mListBoxs);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(mListItemListener);
		
		mRefreshBtn = (Button)findViewById(R.id.button1);
		mRefreshBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//mTextView.setText("Current AP is:  "+WifiUtils.getBroadcastIp());
				//sendCmdData("192.168.1.244","abcdefg1234567");
				//sendCmdData("192.168.43.1", "abcdeffffffff");
				mListBoxs.clear();
				//mListBoxs.add("box devices:");
				mListAdapter.notifyDataSetChanged();
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
			Multicastsender ms = new Multicastsender(str, WifiUtils.getBroadcastIp(), Config.MULTICAST_PORT);
			@Override
			public void run() {
		        ms.send();
		        if(Config.DEBUG) Log.i(TAG,"--------------------------send multicast:"+ str);
			}
		}).start();
	}
	public boolean sendCmdData_r(String ip,String info,int i){
		NetAsyncTask task = new NetAsyncTask(ip, info, this);
		task.execute(1);
		return true;
		/*
		final String str = info;
		final String ip_ = ip;
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendCmdData_r(ip_,str,0);
			}
		}).start();
		*/
	}
	private static boolean sendCmdData_rr(String ip,String info,int timeout){
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
	private void openWifiList(){
		Intent intent = new Intent();
		intent.setClass(this, com.geniatech.client_phone.wifi.WifiSettings.class);
		startActivity(intent);
	}
	public String multicastListen_r() {
        byte[] data = new byte[256];
        String message = null;
        
        try {
            //InetAddress ip = InetAddress.getByName(Config.MULTICAST_SEND_HOST);
            mMultiListenSocket = new DatagramSocket(Config.MULTICAST_PORT);
            //mMultiListenSocket.setSoTimeout(Config.LISTEN_TIME_OUT);
            
            DatagramPacket packet = new DatagramPacket(data, data.length);
            mMultiListenSocket.receive(packet);
            message = new String(packet.getData(), 0, packet.getLength());
            if(Config.DEBUG) Log.i(TAG,"----------------------multicastListen_r data:"+ message);
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
			//if(arg2==0) return;
			String str = mListBoxs.get(arg2);
			str = str.replace(SPLIT_TAG, "#");
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
				sendCmdChang2ap(mPhoneHandle,ipAddress);
			}
		});
		builder.create().show();
	}
	private void sendCmdChang2ap_r(Handler handle,String ip){
		final Handler h = handle;
		final String ip_ = ip;
		new Thread(new Runnable() {
			public void run() {
				boolean isOk = sendCmdData_r(ip_, Config.CMD_BOX_WIFI_2_AP,Config.LISTEN_TIME_OUT);
				if(!isOk) h.sendEmptyMessage(MSG_CONNECT_ERROR);
			}
		}).start();
		
	}
	private void sendCmdChang2ap(Handler handle,String ip){
		final Handler h = handle;
		final String ip_ = ip;
				boolean isOk = sendCmdData_r(ip_, Config.CMD_BOX_WIFI_2_AP,Config.LISTEN_TIME_OUT);
				if(!isOk) h.sendEmptyMessage(MSG_CONNECT_ERROR);

	}
	public void testListItems(Handler handle){
		if(!mUpdateList) return;
		final Handler h = handle;
		new Thread(new Runnable() {
			public void run() {
				testListItems_r();
				h.sendEmptyMessageDelayed(MSG_REFRESH_UI, Config.LISTEN_TIME_OUT);
			}
		}).start();
	}	
	public void testListItems_r(){
		final List<String> listDatas = mListBoxs;
		for(String str:listDatas){
				String ip = MsgUtils.getInfo(str, Config.CMD_BOX_IP);
				if(ip == null) continue;
				boolean isOnline = sendCmdData_r(ip, Config.CMD_TEST,2000);
				if(!isOnline){
					synchronized (mListBoxs) {
						mListBoxs.remove(str);
					}
				}
		}
	}
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mReceiver, mIntentFilter);
		mTextView.setText("Current AP is:  "+WifiUtils.getActiveSSID());
		startUpdateList();
	}
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mReceiver);
		stopUpdateList();
	}
	private void startUpdateList(){
		mUpdateList = true;
		mPhoneHandle.sendEmptyMessage(MSG_REFRESH_UI);
	}
	private void stopUpdateList(){
		mUpdateList = false;
		mPhoneHandle.removeMessages(MSG_REFRESH_UI);
	}


	private Handler mPhoneHandle = new Handler(){
        public void handleMessage(Message msg) {
            if(MSG_REMOTE_INFO==msg.what){
            	MsgUtils msgutil = (MsgUtils)msg.obj;
            	handleRemoteInfo(msgutil);
            }else if(MSG_START_REFRESH == msg.what){
            	sendMulticast(Config.CMD_PHONE_REQUEST_ID);
            	sendEmptyMessageDelayed(MSG_START_REFRESH, Config.REFRESH_LIST_TIME);
            }else if(MSG_REFRESH_UI == msg.what){
            	//testListItems(mPhoneHandle);
            	mListAdapter.notifyDataSetChanged();
            }else if(MSG_CONNECT_ERROR == msg.what){
            	Toast.makeText(getApplicationContext(), "Sorry,connect error!", Toast.LENGTH_LONG).show();
            }
        }
    };
    private void handleRemoteInfo(MsgUtils msgutil){
    	String cmd = msgutil.getCmd();
    	if(cmd==null) return;
    	if(Config.DEBUG) Log.i(TAG,"===============handleRemoteInfo=======>>msg cmd:"+cmd);
    	if(cmd.equals(Config.CMD_BOX_REPORT_ID)){
    		String tmp = msgutil.toString();
    		//tmp = tmp.replaceAll("#", "    ");
    		//tmp = tmp.replaceAll("=", ":");
    		tmp = tmp.replaceAll("rptID#", "    ");
    		tmp = tmp.replaceAll("#", SPLIT_TAG);
    		if(!mListBoxs.contains(tmp)){
	    		synchronized (mListBoxs) {
	    			mListBoxs.add(tmp);
				}
    			mListAdapter.notifyDataSetChanged();
    		}
    		if(Config.DEBUG) Log.i(TAG,"===============CMD_BOX_REPORT_ID=======>>:"+msgutil.toString());
    	}
    }
}
