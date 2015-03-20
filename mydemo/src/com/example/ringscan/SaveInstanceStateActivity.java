package com.example.ringscan;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class SaveInstanceStateActivity extends Activity {
	EditText text;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instancestate);
		Log.i("nick","-----onCreate----");
		text = (EditText)findViewById(R.id.editText1);
		if(savedInstanceState != null){
			String  message = savedInstanceState.getString("message");
			Log.i("nick","------message:"+message);
		}

	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	//	Log.i("nick","-----onStart----");
	}
	@Override
	public void onResume()
	{
		super.onResume();
		//Log.i("nick","-----onResume----");
	}
	public void onRestart(){
		super.onRestart();
		//Log.i("nick","-----onRestart----");
	}
	@Override
	public void onPause()
	{
		super.onPause();
		Log.i("nick","-----onPause----");
	}
	@Override
	public void onStop()
	{
		super.onStop();
		Log.i("nick","-----onStop----");
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i("nick","-----onDestroy----");
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         Log.i("nick","---onConfigurationChanged----");
          if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_LANDSCAPE) {
               // land donothing is ok
        	  Log.i("nick","----is landscape----");
          } else if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
               // port donothing is ok
        	  Log.i("nick","----is portrait----");
          }
    } 
	
    //位于 onPause 与 onStop之间调用
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		Log.i("nick","-----onSaveInstanceState----");
		savedInstanceState.putString("message", text.getText().toString());
	}
	
	//onSaveInstanceState方法和onRestoreInstanceState方法“不一定”是成对的被调用的
	//旋转屏幕会调用到它，按home键再返回不会调用到它
	   @Override
	    public void onRestoreInstanceState(Bundle savedInstanceState){
	        super.onRestoreInstanceState(savedInstanceState);
	       
	      String  message = savedInstanceState.getString("message");
	      Log.i("nick","-----onRestoreInstanceState:"+message);
	    }
}
