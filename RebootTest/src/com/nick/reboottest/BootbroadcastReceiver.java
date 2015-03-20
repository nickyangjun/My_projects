package com.nick.reboottest;

import com.nick.reboottest.R.string;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class BootbroadcastReceiver extends BroadcastReceiver {
	private SharedPreferences mSharePref;
	public static final String spf_name = "reboot_config";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("nick","----:"+intent.getAction());
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent intent2=new Intent(context, MainActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent2);
		}
	}
}
