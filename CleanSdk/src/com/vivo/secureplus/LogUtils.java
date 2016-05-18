package com.vivo.secureplus;

import android.util.Log;
import android.os.SystemProperties;

public class LogUtils {
	public static final String TAG = "SecurePlus_PhoneClean";
	public static boolean DEBUG = SystemProperties.get("persist.sys.log.ctrl", "no").equals("yes");
	
	public static void logD(String msg) {
		if (DEBUG) {			
			Log.d(TAG, msg);
		}
	}
	
	public static void logI(String msg) {
		Log.i(TAG, msg);
	}
	
	public static void logW(String msg) {
		Log.w(TAG, msg);
	}
	
	public static void logE(String msg) {
		Log.e(TAG, msg);
	}
}
