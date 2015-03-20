package com.softwinner.update.ui;

import com.softwinner.update.OtaUpgradeUtils;
import com.softwinner.update.Preferences;
import com.softwinner.update.R;
import com.softwinner.update.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InstallPackage extends LinearLayout implements OtaUpgradeUtils.ProgressListener{
    
    private ProgressBar mProgressBar;
    private OtaUpgradeUtils mUpdateUtils;
    private LinearLayout mOutputField;
    private LayoutInflater mInflater;
    private String mPackagePath;
    private Handler mHandler = new Handler();
    private Button mDismiss;
    private Preferences mPreference;
    private Context mContext;

    public InstallPackage(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        mUpdateUtils = new OtaUpgradeUtils(context);
        mInflater = LayoutInflater.from(context);
        
        mPreference = new Preferences(mContext);
        requestFocus();
    }
    
    public void setPackagePath(String path){
        mPackagePath = path;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressBar = (ProgressBar) findViewById(R.id.verify_progress);
        mOutputField = (LinearLayout) findViewById(R.id.output_field);
        final TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
        tv.setText(R.string.install_ota_output_confirm);
        mOutputField.addView(tv);
        Animation animation = new AlphaAnimation(0.0f,1.0f);
        animation.setDuration(600);
        LayoutAnimationController controller = 
            new LayoutAnimationController(animation);
        mOutputField.setLayoutAnimation(controller);
        mDismiss = (Button)findViewById(R.id.confirm_cancel);
        findViewById(R.id.confirm_update).setOnClickListener(new View.OnClickListener() {            
            @Override
            public void onClick(View v) {
            	if(mPreference.getBatterylevl()<Utils.POWER_LEVEL_UPDATE){
            		
            		 AlertDialog.Builder localBuilder = new AlertDialog.Builder(mContext);
            		 localBuilder.setIcon(android.R.drawable.ic_dialog_alert); 		 
            		 String aler_msg = String.format(mContext.getString(R.string.alertDialog_lowpower_msg),mPreference.getBatterylevl(),Utils.POWER_LEVEL_UPDATE);
            		 localBuilder.setTitle(mContext.getString(R.string.alertDialog_lowpowe_title));
            		 localBuilder.setMessage(aler_msg);
            		 localBuilder.setPositiveButton(mContext.getString(R.string.alertDialog_lowpower_ok),new
            		 DialogInterface.OnClickListener() {
            		  
            		  @Override public void onClick(DialogInterface arg0, int arg1) {
            		  					
            			
            		            }
            		 
            		           } );
            		           AlertDialog sAlertDialog = localBuilder.create();
            		           sAlertDialog.show();

            		return;
            	}
            	
                tv.setText(R.string.install_ota_output_start);
                new Thread(new Runnable(){
                    @Override
                    public void run() {                        
                        mUpdateUtils.upgradeFromOta(mPackagePath, InstallPackage.this);
                    }            
                }).start();
                mDismiss.setEnabled(false);
            }
        });
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(!mDismiss.isEnabled() && keyCode == KeyEvent.KEYCODE_BACK){
    		return true;
    	}
		return super.onKeyDown(keyCode, event);
	}

	public void deleteSource(boolean b){
        mUpdateUtils.deleteSource(b);
    }

    @Override
    public void onProgress(final int progress) {
        mHandler.post(new Runnable(){

            @Override
            public void run() {
                if(progress == 0){
                    TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_checking);
                    mOutputField.addView(tv);
                }else if(progress == 100){
                    TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_check_ok);
                    mOutputField.addView(tv);
                }
                mProgressBar.setProgress(progress/2);                
            }            
        });        
    }

    @Override
    public void onVerifyFailed(int errorCode, Object object) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                tv.setText(R.string.install_ota_output_check_error);
                mOutputField.addView(tv);
                mDismiss.setEnabled(true);
            }}        
        );        
    }

    @Override
    public void onCopyProgress(final int progress) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                if(progress == 0){
                    TextView tv1 = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv1.setText(R.string.install_ota_output_copying);
                    mOutputField.addView(tv1);
                }else if(progress == 100){
                    TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_copy_ok);
                    mOutputField.addView(tv);
                    tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                    tv.setText(R.string.install_ota_output_restart);
                    mOutputField.addView(tv);
                }        
                mProgressBar.setProgress(50 + progress/2);
            }            
        });        
    }

    @Override
    public void onCopyFailed(int errorCode, Object object) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                TextView tv = (TextView) mInflater.inflate(R.layout.medium_text, null);
                tv.setText(R.string.install_ota_output_copy_failed);
                mOutputField.addView(tv);
                mDismiss.setEnabled(true);
            }            
        });        
    }
}
