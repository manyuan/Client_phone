package com.geniatech.client_phone;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class PhoneActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone);
		Intent intent = new Intent();
		intent.setClass(this, com.geniatech.client_phone.wifi.WifiSettings.class);
		startActivity(intent);
	}

}
