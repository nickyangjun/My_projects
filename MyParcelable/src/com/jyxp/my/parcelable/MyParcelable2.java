package com.jyxp.my.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class MyParcelable2 implements Parcelable {
	
	private String mString;
	private Character mCharacter;
	private MyEnum mEnum;
	
	public MyParcelable2() {
		// TODO Auto-generated constructor stub
	}
	
	public MyParcelable2(Parcel in) {
		// TODO Auto-generated constructor stub
		mString = in.readString();
		mCharacter = (Character) in.readValue(Character.class.getClassLoader());
		setmEnum((MyEnum) in.readValue(MyEnum.class.getClassLoader()));
	}

	public String getmString() {
		return mString;
	}

	public void setmString(String mString) {
		this.mString = mString;
	}

	public Character getmCharacter() {
		return mCharacter;
	}

	public void setmCharacter(Character mCharacter) {
		this.mCharacter = mCharacter;
	}
	
	public MyEnum getmEnum() {
		return mEnum;
	}

	public void setmEnum(MyEnum mEnum) {
		this.mEnum = mEnum;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(mString);
		dest.writeValue(mCharacter);
		dest.writeValue(mEnum);
	}
	
	public static final Parcelable.Creator<MyParcelable2> CREATOR = new Creator<MyParcelable2>() {
		
		@Override
		public MyParcelable2[] newArray(int size) {
			// TODO Auto-generated method stub
			return new MyParcelable2[size];
		}
		
		@Override
		public MyParcelable2 createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new MyParcelable2(source);
		}
	};

}
