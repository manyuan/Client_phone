/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geniatech.client_phone.wifi;

import com.geniatech.client_phone.R;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class WifiApEnabler implements Preference.OnPreferenceChangeListener {
    private final Context mContext;
    private final CheckBoxPreference mCheckBox;
    private final CharSequence mOriginalSummary;

    private WifiManager mWifiManager;

    ConnectivityManager mCm;
    private String[] mWifiRegexs;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
           

        }
    };

    public WifiApEnabler(Context context, CheckBoxPreference checkBox) {
        mContext = context;
        mCheckBox = checkBox;
        mOriginalSummary = checkBox.getSummary();
        checkBox.setPersistent(false);

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    public void resume() {
        
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mCheckBox.setOnPreferenceChangeListener(null);
    }

    private void enableWifiCheckBox() {
        boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        if(!isAirplaneMode) {
            mCheckBox.setEnabled(true);
        } else {
            mCheckBox.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object value) {

        final ContentResolver cr = mContext.getContentResolver();
        boolean enable = (Boolean)value;

        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = mWifiManager.getWifiState();
        
        return false;
    }

    void updateConfigSummary(WifiConfiguration wifiConfig) {
        
    }

    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;

        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiTethered = true;
            }
        }
        for (Object o: errored) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiErrored = true;
            }
        }

    }

    private void handleWifiApStateChanged(int state) {
       
    }
}
