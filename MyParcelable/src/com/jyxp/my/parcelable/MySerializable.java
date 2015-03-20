package com.jyxp.my.parcelable;

import java.io.Serializable;

public class MySerializable implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Double mDouble;
	private Float mFloat;
	
	public MySerializable() {
		// TODO Auto-generated constructor stub
	}

	public Double getmDouble() {
		return mDouble;
	}

	public void setmDouble(Double mDouble) {
		this.mDouble = mDouble;
	}

	public Float getmFloat() {
		return mFloat;
	}

	public void setmFloat(Float mFloat) {
		this.mFloat = mFloat;
	}
}
