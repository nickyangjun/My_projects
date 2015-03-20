package com.sinpo.xnfc;

import java.util.HashMap;

import android.app.Application;

public class MyApplication extends Application {

	public static int nocard = 0;
	public static int isOdep = 1;
	public static int isNdef = 2;
	
	public static HashMap<Integer,Object> tagsMap=new HashMap<Integer, Object>();
	
	private static int cards_type;
	@Override
	public void onCreate() {
		super.onCreate();
		setCardsType(nocard);
	}
	
	public void setCardsType(int type){
		cards_type = type;
	}
	public int getCardsType(){
		return cards_type;
	}

}
