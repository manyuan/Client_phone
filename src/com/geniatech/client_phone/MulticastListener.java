package com.geniatech.client_phone;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

public class MulticastListener {
	private final static String TAG = "MulticastListener";
    private int port;
    private String host;
    
    public MulticastListener(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public String listen1() {
        byte[] data = new byte[256];
        String message = null;
        try {
            InetAddress ip = InetAddress.getByName(this.host);
            MulticastSocket ms = new MulticastSocket(this.port);
            ms.joinGroup(ip);
            DatagramPacket packet = new DatagramPacket(data, data.length);
            ms.receive(packet);
            message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(message);
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
            message = null;
        }
        return message;
    }
    public String listen() {
        byte[] data = new byte[256];
        String message = null;
        DatagramSocket ms = null;
        try {
            InetAddress ip = InetAddress.getByName("192.168.1.255");
            ms = new DatagramSocket(this.port);
            ms.setSoTimeout(Config.LISTEN_TIME_OUT);
            
            DatagramPacket packet = new DatagramPacket(data, data.length);
            ms.receive(packet);
            message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(message);
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
            if(ms!=null) ms.close();
            message = null;
        }
        return message;
    }
 /*
    public static void main(String[] args) {
        int port = 1234;
        String host = "224.0.0.1";
        MulticastListener ml = new MulticastListener(host, port);
        while(true) {
            ml.listen();
        }
    }
    */
}
