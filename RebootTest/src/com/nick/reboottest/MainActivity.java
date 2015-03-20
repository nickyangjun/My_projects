package com.nick.reboottest;

import android.R.integer;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	public static final String ACTION_REBOOT ="android.intent.action.REBOOT";
	public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
	private SharedPreferences mSharePref;
	EditText reboot_intertime,set_reboottimes;
	TextView reboot_times;
	PowerManager.WakeLock wl;
	KeyguardLock kl;
	int reboottimes=0;
	private static boolean is_reboot = true;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.mSharePref = getSharedPreferences(BootbroadcastReceiver.spf_name, Context.MODE_PRIVATE);
		
		reboot_intertime = (EditText) findViewById(R.id.intertime);
		reboot_times = (TextView) findViewById(R.id.reboot_times);
		set_reboottimes = (EditText) findViewById(R.id.setreboottimes);
		set_reboottimes.setText(String.valueOf(mSharePref.getLong("set_reboot_times", 1000)));
		set_reboottimes.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if(set_reboottimes.getText().toString().length()>0){
					long settimes = Integer.parseInt(set_reboottimes.getText().toString());
					if(settimes>0){
						mSharePref.edit().putLong("set_reboot_times", settimes).commit();
					}
				}
			}
		});
		
		findViewById(R.id.reboot_start).setOnClickListener(this);
		findViewById(R.id.reboot_stop).setOnClickListener(this);
		findViewById(R.id.clear_reboottimes).setOnClickListener(this);
		
		//获取电源管理器对象
		PowerManager pm=(PowerManager) getSystemService(Context.POWER_SERVICE);
		//获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
		//点亮屏幕
		wl.acquire();
		
		//得到键盘锁管理器对象
		KeyguardManager km= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		//参数是LogCat里用的Tag
		 kl = km.newKeyguardLock(Context.KEYGUARD_SERVICE);
		//解锁
		kl.disableKeyguard();
		
		initConfig();
	}

	private void initConfig() {
		// TODO Auto-generated method stub
	
		reboottimes = mSharePref.getInt("reboot_times", 0);
		reboot_times.setText(this.getString(R.string.reboot_times, reboottimes));
		if(mSharePref.getBoolean("reboot_start", false) && mSharePref.getLong("reboot_inter", 8000)>0){
			if(mSharePref.getLong("set_reboot_times", 1000) > reboottimes){
				long time = mSharePref.getLong("reboot_inter", 8000);
				reboot_intertime.setText(String.valueOf(time));
				reboottimes++;
				mSharePref.edit().putInt("reboot_times", reboottimes).commit();
				new rebootThread(time).start();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.reboot_start:	 
			is_reboot = true;
			if(reboot_intertime.getText().toString().length()>0){
				long time = Integer.parseInt(reboot_intertime.getText().toString());
				mSharePref.edit().putBoolean("reboot_start", true).commit();
				mSharePref.edit().putLong("reboot_inter", time).commit();
				reboottimes++;
				mSharePref.edit().putInt("reboot_times", reboottimes).commit();
				 new rebootThread(time).start();
			 }else{
				 Toast.makeText(getApplicationContext(), R.string.reboot_inter_tip, Toast.LENGTH_LONG).show();
			 }
			break;
		case R.id.reboot_stop:
			 is_reboot = false;
			 mSharePref.edit().putBoolean("reboot_start", false).commit();
			break;
		case R.id.clear_reboottimes:
			mSharePref.edit().putInt("reboot_times", 0).commit();
			reboot_times.setText(this.getString(R.string.reboot_times, 0));
			break;
		}
		

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		wl.release();
		kl.reenableKeyguard();
	}
	
	class rebootThread extends Thread{
		private long time;
		public rebootThread(long time) {
			// TODO Auto-generated constructor stub
			this.time = time;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				sleep(time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 if(is_reboot){
				 Log.i("nick", "broadcast->reboot");
				 Intent intent = new Intent(Intent.ACTION_REBOOT);
				 intent.putExtra("nowait", 1);
				 intent.putExtra("interval", 1);
				 intent.putExtra("window", 0);
				 sendBroadcast(intent);
			 }
		}
	} 

}
