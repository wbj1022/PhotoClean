package com.vivo.secureplus;

import com.vivo.secureplus.phoneclean.PhoneCleanManager;

import android.content.Context;

public final class SecurePlus {
	private static Context mContext;
	private final static String mSDKVerion = "3.0.0";
	
	public static String getSDKVersion() {
		return mSDKVerion;
	}
	
	public static void init(Context context, boolean updateDB) {
		mContext = context.getApplicationContext();
		PhoneCleanManager.copyOrUpdateLocalDB(updateDB);
	}

	public static Context getApplicationContext() {
		return mContext;
	}
}
