package com.softwinner.update.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.softwinner.shared.FileSelector;
import com.softwinner.update.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectLocalmm extends Activity{
	
	public static final String FILE = "file";
	Button mbtnflash;
	Button mbtnsdcard;
	private String sdcardpath=null;
	private String flashpath=null;
	private Context mContext;
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.selectlocalmm);
	        mContext=this;
	        mbtnflash = (Button) findViewById(R.id.btn_local_flash);
	        mbtnsdcard = (Button) findViewById(R.id.btn_local_sdcard);
	        
	        Log.i("Update","+++"+getSDPath());
	        Log.i("Update","+++"+getExterPath());
	        sdcardpath = getExterPath();
	        flashpath = getSDPath();
	        if(sdcardpath != null){
	        	mbtnsdcard.setVisibility(View.VISIBLE);
	        }else{
	        	mbtnsdcard.setVisibility(View.GONE);
	        }
	        if(flashpath != null){
	        	mbtnflash.setVisibility(View.VISIBLE);
	        }else{
	        	mbtnflash.setVisibility(View.GONE);
	        }
	        mbtnflash.setOnClickListener(btnClick);
	        mbtnsdcard.setOnClickListener(btnClick);
	        
	 }
	 
	 private OnClickListener btnClick = new OnClickListener() {
		 @Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.btn_local_flash:
					Intent intent0 = new Intent(mContext,FileSelector.class);
					intent0.putExtra("localpath", flashpath);
	            	Activity activity = (Activity) mContext;
	            	activity.startActivityForResult(intent0, 0);
					break;
				case R.id.btn_local_sdcard:
					Intent intent1 = new Intent(mContext,FileSelector.class);
					intent1.putExtra("localpath", sdcardpath);
	            	Activity activity1 = (Activity) mContext;
	            	activity1.startActivityForResult(intent1, 0);
					break;
				
				}
			}
			
	 };
	 
	 public String getSDPath(){
		  File sdDir = null;
		  boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
		  if (sdCardExist)
		  {
			  sdDir = Environment.getExternalStorageDirectory();//获取跟目录
			  return sdDir.toString();
		  }
		 
		  return null;
		 }
	 public String getExterPath(){
		 		String sdcard_path=null;
                 try {
                         Runtime runtime = Runtime.getRuntime();
                         Process proc = runtime.exec("mount");
                         InputStream is = proc.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is);
                         String line;
                         BufferedReader br = new BufferedReader(isr);
                         while ((line = br.readLine()) != null) {

                                 if (line.contains("secure")) continue;
                                 if (line.contains("asec")) continue;
                                  String columns[] = line.split(" ");
                                  if(columns[1].equalsIgnoreCase("/storage/sdcard1")){
                                  sdcard_path=columns[1];
                                  }

                         }

                 }
                 catch (Exception e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                 }
                 return sdcard_path;
         }
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        if(data != null){
		        Bundle bundle = data.getExtras();
		        String file = bundle.getString("file");
		        Log.i("Update","SelectLocalmm onActivityResult: "+file);
		        if (file != null) {
		        	Intent intent = new Intent();
		            intent.putExtra(FILE, file);
		            setResult(0, intent);
		            finish();
		        	
		        	}
		        }
	 }


}
