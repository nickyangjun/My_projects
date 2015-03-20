
package com.softwinner.update;

import com.softwinner.update.ThreadTask;
import com.softwinner.update.UpdateActivity;
import com.softwinner.update.Utils;
import com.softwinner.update.ui.InstallPackage;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

public class UpdateService extends Service {
    private static final String TAG = "UpdateService";

    public static final String KEY_START_COMMAND = "start_command";
    public static final String KEY_DOWNLOAD_SUCESS = "update_download_success";
    public static final int MSG_DOWNLOAD_OK = 89;
    public static final int MSG_DOWNLOAD_UPDATE = MSG_DOWNLOAD_OK+1;

    private Context mContext;

    private Preferences mPreference;

    public static final int START_COMMAND_START_CHECKING = 101;

    public static final int START_COMMAND_DOWNLOAD = 102;

    public static final int TASK_ID_CHECKING = 101;

    public static final int TASK_ID_DOWNLOAD = 102;

    public static final String DOWNLOAD_OTA_PATH = Utils.DOWNLOAD_PATH;

    private CheckBinder mBinder = new CheckBinder();

    private static ThreadTask mCheckingTask;

    private static ThreadTask mDownloadTask;

    final Handler mHandler = new Handler(){
    	public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_DOWNLOAD_OK:
            	createDiag();
                break;
            case MSG_DOWNLOAD_UPDATE:

                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(getApplicationContext(),DownloadActivity.class);
                intent.putExtra(KEY_DOWNLOAD_SUCESS, true);
                startActivity(intent);

            	break;
            default:
                
            }
        };  
    };
    private int batterylevl=0;
    
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            int current=intent.getExtras().getInt("level");//获得当前电量
            int total=intent.getExtras().getInt("scale");//获得总电量
            int percent=current*100/total;
            Log.i(TAG,"****current: "+current+" total: "+total+" percent: "+percent);
            batterylevl=percent;
            mPreference.setBatterylevl(batterylevl);
        	}else if(intent.getAction().equals(KEY_DOWNLOAD_SUCESS)){
        		Log.i(TAG,"****download update suceess!!!!");
        		if(!mPreference.getDownloadActivityRuning()){
        			mHandler.sendEmptyMessage(MSG_DOWNLOAD_OK);
        		}
        	}
        	
        }
    };
    public class CheckBinder extends Binder {    
        public boolean resetTask(int taskId){
            boolean b = false;
            switch (taskId) {
                case TASK_ID_CHECKING:
                    b = mCheckingTask.reset();
                    break;
                case TASK_ID_DOWNLOAD:
                    b = mDownloadTask.reset();
                    break;
            }
            return b;
        }
        
        public void setTaskPause(int taskId){
            switch (taskId) {
                case TASK_ID_CHECKING:
                    mCheckingTask.pause();
                    break;
                case TASK_ID_DOWNLOAD:
                    mDownloadTask.pause();
                    break;
            }
        }
        
        public void setTaskResume(int taskId){
            switch (taskId) {
                case TASK_ID_CHECKING:
                    mCheckingTask.resume();
                    break;
                case TASK_ID_DOWNLOAD:
                    mDownloadTask.resume();
                    break;
            }
        }

        public int getTaskRunnningStatus(int taskId) {
            int status = -1;
            switch (taskId) {
                case TASK_ID_CHECKING:
                    status = mCheckingTask.getRunningStatus();
                    break;
                case TASK_ID_DOWNLOAD:
                    status = mDownloadTask.getRunningStatus();
                    break;
            }
            return status;
        }

        public Object getTaskResult(int taskId) {
            Object result = null;
            switch (taskId) {
                case TASK_ID_CHECKING:
                    result = mCheckingTask.getResult();
                    break;
                case TASK_ID_DOWNLOAD:
                    result = mDownloadTask.getResult();
                    break;
            }
            return result;
        }

        public int getTaskErrorCode(int taskId) {
            int errorCode = -1;
            switch (taskId) {
                case TASK_ID_CHECKING:
                    errorCode = mCheckingTask.getErrorCode();
                    break;
                case TASK_ID_DOWNLOAD:
                    errorCode = mDownloadTask.getErrorCode();
                    break;
            }
            return errorCode;
        }

        public int getTaskProgress(int taskId) {
            int progress = -1;
            switch (taskId) {
                case TASK_ID_CHECKING:
                    progress = mCheckingTask.getProgress();
                    break;
                case TASK_ID_DOWNLOAD:
                    progress = mDownloadTask.getProgress();
                    break;
            }
            return progress;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void sendNewVersionBroadcast() {
        Intent intent = new Intent(LoaderReceiver.UPDATE_GET_NEW_VERSION);
        sendBroadcast(intent);
    }

    public int onStartCommand(Intent intent, int paramInt1, int paramInt2) {
		if(intent == null)
			return 0;
       
       
        int cmd = intent.getIntExtra(KEY_START_COMMAND, 0);
        if (Utils.DEBUG) {
            Log.i(TAG, "get a start cmd : " + cmd);
        }
        switch (cmd) {
            case START_COMMAND_START_CHECKING:
            	Log.v(TAG,"mDownloadTask status="+mDownloadTask.getRunningStatus());
                if (mDownloadTask.getRunningStatus() == ThreadTask.RUNNING_STATUS_UNSTART) {
                    mCheckingTask.start();
                }
                break;
            case START_COMMAND_DOWNLOAD: 
            	Log.v(TAG,"mCheckingTask status="+mCheckingTask.getRunningStatus());
                if(mCheckingTask.getRunningStatus() != ThreadTask.RUNNING_STATUS_RUNNING){
                    mDownloadTask.start();
                }
                break;
            default:
                break;

        }
       

        return 0;
    }

    private void initInstance() {
        if (mCheckingTask == null) {
            mCheckingTask = new CheckingTask(this, mHandler);
        }
        if (mDownloadTask == null) {
            mDownloadTask = new DownloadTask(this, mHandler);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"UpdateService onCreate");
        mContext = getBaseContext();
        mPreference = new Preferences(mContext);
        initInstance();
        IntentFilter intentToReceiveFilter = new IntentFilter();
        // intentToReceiveFilter.addAction(CLEAN_NOTIFICATION_INTENT);
        intentToReceiveFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentToReceiveFilter.addAction(KEY_DOWNLOAD_SUCESS);
        registerReceiver(mIntentReceiver, intentToReceiveFilter);//注册BroadcastReceiver
    }
    @Override
	public void onDestroy() {
		Log.i(TAG, "UpdateService onDestroy");
		 unregisterReceiver(mIntentReceiver);  
		super.onDestroy();
    this.stopSelf();

	}
    private void createDiag(){
       	
    	final Dialog dialog = new Dialog(mContext); 
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.custom_update_dialog);
    	TextView mDescription = (TextView) dialog.findViewById(R.id.update_new_version_description);
    	Button mCombineBtn = (Button) dialog.findViewById(R.id.update_downloadupdate);
    	Button mCancel = (Button) dialog.findViewById(R.id.update_cancel);
    	mCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//mDownloadTask.reset();
				dialog.dismiss();
			}
    		
    	});
    	mCombineBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_DOWNLOAD_UPDATE);
				dialog.dismiss();
			}
    		
    	});
    	mDescription.setText(mPreference.getPackageDescriptor());
         dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//use alert.  
         // dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
         dialog.setCancelable(false);
         dialog.show();  
    }
}
