package com.sinpo.xnfc;

import com.sinpo.xnfc.card.CardManager;
import com.sinpo.xnfc.card.NdefCard;
import com.sinpo.xnfc.info.Ndef_info;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WriteNdefCard extends Activity{
	private EditText etinputnums,etinputaccount,etinputname,etinputpwd,etinputtxt,etinputbalance;
	private Button btnwrite;
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;
	private boolean iswrited = false;
	private String writedat="";
	private String writename="";
	private String writeaccount="";
	private String writenums="";
	private String writepwd="";
	private String writebalance="";
	Dialog dialog1=null;
	private MyApplication mMyApplication;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.writecard);
		
		final Resources res = getResources();
		this.res = res;
		mMyApplication = (MyApplication) getApplication();
		
		etinputtxt = (EditText) findViewById(R.id.et_input_txt);
		etinputnums = (EditText) findViewById(R.id.et_input_nums);
		etinputaccount = (EditText) findViewById(R.id.et_input_account);
		etinputname = (EditText) findViewById(R.id.et_input_name);
		etinputpwd = (EditText) findViewById(R.id.et_input_passw);
		etinputbalance = (EditText)findViewById(R.id.et_input_balance);
		btnwrite = (Button) findViewById(R.id.btnwrite_in);
		
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		btnwrite.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(etinputnums.getText().toString().length()<=0){
					Toast.makeText(getApplicationContext(), getString(R.string.no_dat_nums), Toast.LENGTH_LONG).show();
					return;
				}
				if(etinputaccount.getText().toString().length()<=0){
					Toast.makeText(getApplicationContext(), getString(R.string.no_dat_account), Toast.LENGTH_LONG).show();
					return;
				}
				if(etinputname.getText().toString().length()<=0){
					Toast.makeText(getApplicationContext(), getString(R.string.no_dat_name), Toast.LENGTH_LONG).show();
					return;
				}
				if(etinputpwd.getText().toString().length()<=0){
					Toast.makeText(getApplicationContext(), getString(R.string.no_dat_pwd), Toast.LENGTH_LONG).show();
					return;
				}
				if(etinputbalance.getText().toString().length()<=0){
					writebalance="Balance:<br />0.00"+"<br />";
				}else{
					writebalance="Balance:<br />"+etinputbalance.getText().toString()+"<br />";
				}
				writename="Name:<br />"+etinputname.getText().toString()+"<br />";
				writeaccount="Account Number:<br />"+etinputaccount.getText().toString()+"<br />";
				writenums="Card Number:<br />"+etinputnums.getText().toString()+"<br />";
				writepwd="Password:<br />"+etinputpwd.getText().toString()+"<br />";
				writedat=writename+writenums+writeaccount+writebalance+writepwd;
						
				if(etinputtxt.getText().toString().length()>0){
					writedat += "TXT:<br />"+etinputtxt.getText().toString()+"<br />";
				}
				if(writedat.length()>0){
					createCustomDialog1();
					iswrited=true;
					
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.no_dat), Toast.LENGTH_LONG).show();
				}
			}
			
		});
		
		
		
	
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		super.onPause();

		if (nfcAdapter != null)
			nfcAdapter.disableForegroundDispatch(this);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		if(mMyApplication.tagsMap.containsKey(MyApplication.isNdef)){ //得到前面读Ndef卡的数据
			Ndef_info ninfo = (Ndef_info)mMyApplication.tagsMap.get(MyApplication.isNdef);
			if(ninfo.getTxt()!= null){etinputtxt.setText(ninfo.getTxt());}
			if(ninfo.getCard_nums()!= null){etinputnums.setText(ninfo.getCard_nums());}
			if(ninfo.getAccount()!= null){etinputaccount.setText(ninfo.getAccount());}
			if(ninfo.getName()!= null){etinputname.setText(ninfo.getName());}
			if(ninfo.getPasswd()!= null){etinputpwd.setText(ninfo.getPasswd());}
			if(ninfo.getBalance()!= null){etinputbalance.setText(ninfo.getBalance());}
			mMyApplication.tagsMap.clear();
		}
		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					CardManager.FILTERS, CardManager.TECHLISTS);

		
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Log.d("NFCTAG", intent.getAction());
		 //获取Tag对象
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //创建NdefMessage对象和NdefRecord对象

		final Ndef ndef = Ndef.get(tag);
		if(ndef != null){
			Log.i("nick","----Ndef------");
			if(iswrited){
				if(NdefCard.write(writedat,ndef)){
					canlseCustomDialog1();
					Toast.makeText(getApplicationContext(), getString(R.string.write_success), Toast.LENGTH_LONG).show();
				}else{			
					canlseCustomDialog1();
					Toast.makeText(getApplicationContext(), getString(R.string.write_fail), Toast.LENGTH_LONG).show();
				}
				
			}
		}
	}
	
	 public void createCustomDialog1(){  
	        dialog1 = new Dialog(this);  
	        dialog1.setContentView(R.layout.dialog_layout1);  
	        dialog1.setTitle(getString(R.string.btn_write));  
	        dialog1.show();  
	        
	 } 
	 public void canlseCustomDialog1(){
		 if(dialog1!=null){
				dialog1.dismiss();
			}
	 }
	 
	 

}
