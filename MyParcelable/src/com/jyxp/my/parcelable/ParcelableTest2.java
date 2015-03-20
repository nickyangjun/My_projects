package com.jyxp.my.parcelable;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ParcelableTest2 extends Activity {
	
	private TextView mResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		
		mResult = (TextView) findViewById(R.id.result);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			StringBuilder sb = new StringBuilder();
			MyParcelable mMyParcelable = bundle.getParcelable(Constant.BUNDLE_MYPARCELABLE);
			int mInteger = mMyParcelable.getmInteger();
			sb.append(mInteger + "\r\n");
			
			List<MyParcelable2> mMyParcelable2s = mMyParcelable.getMyParcelable2s();
			for (MyParcelable2 myParcelable2 : mMyParcelable2s) {
				sb.append(myParcelable2.getmString() + "\r\n");
				sb.append(myParcelable2.getmCharacter() + "\r\n");
				sb.append(myParcelable2.getmEnum().toString() + "\r\n");
			}
			MySerializable mMySerializable = mMyParcelable.getmMySerializable();
			double mDouble = mMySerializable.getmDouble();
			sb.append(mDouble + "\r\n");
			float mFloat = mMySerializable.getmFloat();
			sb.append(mFloat + "\r\n");
			
			MyParcelable2 mMyParcelable2 = mMyParcelable.getmParcelable();
			String mString = mMyParcelable2.getmString();
			sb.append(mString + "\r\n");
			char mCharacter = mMyParcelable2.getmCharacter();
			sb.append(mCharacter + "\r\n");
			MyEnum mEnum = mMyParcelable2.getmEnum();
			sb.append(mEnum.toString() + "\r\n");
			
			mResult.setText(sb.toString());
			
		} else {
			mResult.setText("Bundle is null");
		}
	}
}
