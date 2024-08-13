package com.data.bpm;

import android.content.Context;
import android.content.SharedPreferences;

public class DataSharedPref {

	final private static String Data = "data";
	//이름
	final private static String PREFKEY_NAME = "name";
	//나이
	final private static String PREFKEY_AGE = "age";
	//성별
	final private static String PREFKEY_GENDER = "gender";


	public static void setSharedPrefName(Context context, String value){
		SharedPreferences pref = context.getSharedPreferences(Data, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(PREFKEY_NAME, value);
		prefEditor.commit();
	}

	public static String getSharedPrefName(Context context){
		SharedPreferences pref = context.getSharedPreferences(Data, Context.MODE_PRIVATE);
		return pref.getString(PREFKEY_NAME,"");
	}


	public static void setSharedPrefAge(Context context, String value){
		SharedPreferences pref = context.getSharedPreferences(Data, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(PREFKEY_AGE, value);
		prefEditor.commit();
	}

	public static String getSharedPrefAge(Context context){
		SharedPreferences pref = context.getSharedPreferences(Data, Context.MODE_PRIVATE);
		return pref.getString(PREFKEY_AGE,"");
	}

	public static void setSharedPrefGender(Context context, int value){
		SharedPreferences pref = context.getSharedPreferences(Data, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putInt(PREFKEY_GENDER, value);
		prefEditor.commit();
	}

	public static int getSharedPrefGender(Context context){
		SharedPreferences pref = context.getSharedPreferences(Data, Context.MODE_PRIVATE);
		return pref.getInt(PREFKEY_GENDER,1);
	}


}
