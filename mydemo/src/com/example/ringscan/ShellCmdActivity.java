package com.example.ringscan;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ShellCmdActivity extends Activity implements OnClickListener{
	
	EditText mEditText;
	TextView mTextView,mroot;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shellcmd);
		
		mroot = (TextView) findViewById(R.id.tv_root);
		
		if(RootCmd.haveRoot()){
			mroot.setText("your system has already Root!!!");
			mroot.setVisibility(View.VISIBLE);
		}else{
			mroot.setText("your system not Root!!!");
			mroot.setVisibility(View.VISIBLE);
		}
		
		mEditText = (EditText) findViewById(R.id.et_cmd);
		mTextView =(TextView) findViewById(R.id.tv_result);
		findViewById(R.id.btn_excute).setOnClickListener(this);
		
//		new testUI(){
//
//			@Override
//			public void changeUI() {
//				// TODO Auto-generated method stub
//				mTextView.setText("11111111111111");
//			}
//			
//		}.changeUI();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				int i=100000;
				while(i>0){
					i--;
					Log.i("nick", "id"+getTaskId());
				}
			}
		}).start();
//		
	}


	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		switch(arg0.getId()){
		case R.id.btn_excute:
			String cmd = mEditText.getText().toString();
			if(cmd.length()>0){
				mTextView.setText(RootCmd.execRootCmd(cmd));
			}else{
				Toast.makeText(getApplicationContext(), "cmd can not null", 1).show();
			}
			break;
		}
	}
	
	protected abstract class testUI{
		private  final ExecutorService executorService= Executors.newFixedThreadPool(2);
		public testUI() {
			// TODO Auto-generated constructor stub
			Callable<String> mCallable = new Callable<String>() {

				@Override
				public String call() throws Exception {
					// TODO Auto-generated method stub
					return null;
				}
				
			};
			executorService.submit(mCallable);
			
			
		}
		public abstract void changeUI();
	}
	
	

}
