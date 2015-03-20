package com.boromax.bthandset;

import java.util.HashMap;
import java.util.Map;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothEventManager {
	private static final String TAG = "BluetoothEventManager";
	private Context mContext;
	private final Map<String, Handler> mHandlerMap;
	private final IntentFilter mAdapterIntentFilter;
	
	
	public interface Handler {
        public void onReceive(Context context, Intent intent, BluetoothDevice device);
    }
	
	public void addHandler(String action, Handler handler) {
        mHandlerMap.put(action, handler);
        mAdapterIntentFilter.addAction(action);
    }
	public void addHandlerEnd()
	{
		mContext.registerReceiver(mBroadcastReceiver, mAdapterIntentFilter);
	}

	 BluetoothEventManager(Context context) {
		 mContext = context;
		 mHandlerMap = new HashMap<String, Handler>();
		 mAdapterIntentFilter = new IntentFilter();
		 
		 

	 }
	 
	 private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            BluetoothDevice device = intent
	                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

	            Handler handler = mHandlerMap.get(action);
	            if (handler != null) {
	                handler.onReceive(context, intent, device);
	            }
	        }
	    };


}
