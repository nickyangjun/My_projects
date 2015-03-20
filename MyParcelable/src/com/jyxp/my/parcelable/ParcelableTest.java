package com.jyxp.my.parcelable;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ParcelableTest extends Activity {
    /** Called when the activity is first created. */
	
	private MyParcelable mMyParcelable;
	private MyParcelable2 mMyParcelable2;
	private MySerializable mMySerializable;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mMyParcelable = new MyParcelable();
        
        mMyParcelable2 = new MyParcelable2();
        mMyParcelable2.setmString("mMyParcelable2");
        mMyParcelable2.setmCharacter('C');
        mMyParcelable2.setmEnum(MyEnum.THREE);
        
        mMySerializable = new MySerializable();
        mMySerializable.setmDouble(Math.PI);        
        mMySerializable.setmFloat(1.00f);
        
        mMyParcelable.setmInteger(10086);
        mMyParcelable.setmParcelable(mMyParcelable2);  
        List<MyParcelable2> mMyParcelable2s = new ArrayList<MyParcelable2>();
        mMyParcelable2s.add(mMyParcelable2);
        mMyParcelable2s.add(mMyParcelable2);
        mMyParcelable.setMyParcelable2s(mMyParcelable2s);
        mMyParcelable.setmMySerializable(mMySerializable);
        
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ParcelableTest.this, ParcelableTest2.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(Constant.BUNDLE_MYPARCELABLE, mMyParcelable);
//				bundle.putSerializable(Constant.BUNDLE_MYSERIALIZABLE, mMySerializable);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
    }
}