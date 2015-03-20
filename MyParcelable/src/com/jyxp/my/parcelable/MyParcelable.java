package com.jyxp.my.parcelable;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class MyParcelable implements Parcelable {
	
	private int mInteger;
	private MyParcelable2 mParcelable;
	private List<MyParcelable2> myParcelable2s = new ArrayList<MyParcelable2>();
	private MySerializable mMySerializable;
	
	public MyParcelable() {
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unchecked")
	public MyParcelable(Parcel in) {
		// TODO Auto-generated constructor stub
		mInteger = in.readInt();
		mParcelable = in.readParcelable(MyParcelable2.class.getClassLoader());	//这个地方的ClassLoader不能为null
		myParcelable2s = in.readArrayList(MyParcelable2.class.getClassLoader());
		mMySerializable = (MySerializable) in.readSerializable();
	}

	public int getmInteger() {
		return mInteger;
	}

	public void setmInteger(int mInteger) {
		this.mInteger = mInteger;
	}

	public MyParcelable2 getmParcelable() {
		return mParcelable;
	}

	public void setmParcelable(MyParcelable2 mParcelable) {
		this.mParcelable = mParcelable;
	}

	public List<MyParcelable2> getMyParcelable2s() {
		return myParcelable2s;
	}

	public void setMyParcelable2s(List<MyParcelable2> myParcelable2s) {
		this.myParcelable2s = myParcelable2s;
	}

	public MySerializable getmMySerializable() {
		return mMySerializable;
	}

	public void setmMySerializable(MySerializable mMySerializable) {
		this.mMySerializable = mMySerializable;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(mInteger);
		dest.writeParcelable(mParcelable, flags);
		dest.writeList(myParcelable2s);
		dest.writeSerializable(mMySerializable);		
	}
    
	public static final Parcelable.Creator<MyParcelable> CREATOR = new Creator<MyParcelable>() {
		
		@Override
		public MyParcelable[] newArray(int size) {
			// TODO Auto-generated method stub
			return new MyParcelable[size];
		}
		
		@Override
		public MyParcelable createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new MyParcelable(source);
		}
	};
}
