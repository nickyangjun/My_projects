package com.sinpo.xnfc;

import java.util.ArrayList;

import com.sinpo.xnfc.card.CardManager;
import com.sinpo.xnfc.card.NdefCard;
import com.sinpo.xnfc.card.pboc.Fmcos1208;
import com.sinpo.xnfc.info.Ndef_info;
import com.sinpo.xnfc.tech.Iso14443;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WriteIsodepCard extends Activity{
	private MyApplication mMyApplication;
	private ViewPager mTabPager;
	private ImageView mTab1, mTab2, mTab3;
	private int currIndex = 0;
	private int zero = 0;
	private int one;
	private int two;
	private int three;
	private Button btn_write;
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;
	private Context mContext;
	private Iso14443.Tag Iso1443_Tag;
	private Dialog dialog1=null;
	private EditText et_name,et_cardnums,et_account;
	private boolean waiting_card = true;
	
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.writeisodepcard);
		mMyApplication = (MyApplication) getApplication();
		mContext = this;
		this.res = getResources();
		
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		btn_write = (Button) findViewById(R.id.btnwrite_in);
		
		mTabPager = (ViewPager) findViewById(R.id.tabpager);
		mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		mTab1 = (ImageView) findViewById(R.id.img_newcard);
		mTab2 = (ImageView) findViewById(R.id.img_charge);
		mTab3 = (ImageView) findViewById(R.id.img_consle);
		
		mTab1.setOnClickListener(new MyOnClickListener(0));
		mTab2.setOnClickListener(new MyOnClickListener(1));
		mTab3.setOnClickListener(new MyOnClickListener(2));
		
		LayoutInflater mLi = LayoutInflater.from(this);
		View view1 = mLi.inflate(R.layout.main_tab_newcard, null);
		View view2 = mLi.inflate(R.layout.main_tab_chager, null);
		View view3 = mLi.inflate(R.layout.main_tab_consle, null);
		
		et_name = (EditText) view1.findViewById(R.id.et_input_name);
		et_cardnums = (EditText) view1.findViewById(R.id.et_input_nums);
		et_account = (EditText)view1.findViewById(R.id.et_input_account);
		
		final ArrayList<View> views = new ArrayList<View>();
		views.add(view1);
		views.add(view2);
		views.add(view3);
		PagerAdapter mPagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(views.get(position));
			}

			// @Override
			// public CharSequence getPageTitle(int position) {
			// return titles.get(position);
			// }

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(views.get(position));
				return views.get(position);
			}
		};

		mTabPager.setAdapter(mPagerAdapter);
	}
	
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			mTabPager.setCurrentItem(index);
		}
	};
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		public void onPageSelected(int arg0) {
			Animation animation = null;
			Log.i("nick","pagechange: "+arg0);
			switch (arg0) {
			
			case 0:
				mTab1.setImageDrawable(getResources().getDrawable(
						R.drawable.newcard_f));
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(
							R.drawable.charg));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, 0, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(
							R.drawable.consle));
				}
				break;
			case 1:
				mTab2.setImageDrawable(getResources().getDrawable(
						R.drawable.charg_f));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, one, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(
							R.drawable.newcard));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(
							R.drawable.consle));
				} 
				break;
			case 2:
				mTab3.setImageDrawable(getResources().getDrawable(
						R.drawable.consle_f));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, two, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(
							R.drawable.newcard));
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(
							R.drawable.charg));
				} 
				break;
			
			}
			currIndex = arg0;
			
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageScrollStateChanged(int arg0) {
		}
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

        final IsoDep isodep = IsoDep.get(tag);
		if(isodep != null){
			Log.i("nick","----isodep------");
			
			//这里仅对我们的Fmcos1208支持写操作
			Iso1443_Tag = new Iso14443.Tag(isodep);	
			waiting_card = false;
		}
	}
	private Handler myHandler = new Handler(){
		  @Override  
	        public void handleMessage(Message msg) {  
			  canlseCustomDialog1();
					 
		  }
	};
	
	public void startwrite(View view){
		Log.i("nick","-----write-------");
		createCustomDialog1();
		waiting_card = true;
		
		new Thread(new writecardrunnable()).start();
		
	}
	
 class writecardrunnable implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(waiting_card){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Iso1443_Tag.connect();
		if(currIndex == 0){
			do {
				String name = et_name.getText().toString();
				String cardnums = et_cardnums.getText().toString();
				String account = et_account.getText().toString();
				Fmcos1208.createWriteCard(Iso1443_Tag, res,mContext,name,cardnums,account);
				
			} while (false);
		}
		if(currIndex == 2){
			
			
			do {
				
				Fmcos1208.eraseFiles(Iso1443_Tag, res,mContext);
				
			} while (false);
			
			
		}
		Iso1443_Tag.close();
		myHandler.obtainMessage().sendToTarget();
	}
	 
 }
	
	 private void createCustomDialog1(){  
	        dialog1 = new Dialog(this);  
	        dialog1.setContentView(R.layout.dialog_layout1);  
	        dialog1.setTitle(getString(R.string.btn_write));  
	        dialog1.show();  
	        
	 } 
	 private void canlseCustomDialog1(){
		 if(dialog1!=null){
				dialog1.dismiss();
			}
	 }
	 
}
