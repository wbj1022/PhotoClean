package com.vivo.secureplus.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

public class UpdateUtils {
	public static final String CHECK_UPDATE_URL;
	public static final String UPDATE_DOWNLOAD_PATH = "/.Secureplus/";
    static {
    	if(Log.isLoggable("test_update_url", Log.DEBUG)) {
    		CHECK_UPDATE_URL = "http://113.98.231.125:2111/upapk/apk/query";
    	}else {
    		CHECK_UPDATE_URL = "http://comm.vivo.com.cn/upapk/apk/query";
    	}
    }
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String DEFAULT_SUFFIX = ".sdb";

    private static String QCOM_PLATFORM = "QCOM";
    private static String MTK_PLATFORM = "MTK";
    private static String PLATFORM_TAG = "ro.vivo.product.solution";
    private static String ROM_VERSION_TAG = "ro.vivo.rom.version";

    public static final String DOWNLOAD_APK_DOWN_ID = "download_apk_down_id";

    public static final String DOWNLOAD_APK_UPDATE_MODE = "download_apk_update_mode";

    public static final String DOWNLOAD_APK_UPDATE_LEVEL = "download_apk_update_level";

    public static final String DOWNLOAD_APK_NEW_PACKAGE_REQUIRED_LOW_MD5 = "download_apk_new_package_required_low_md5";
    public static final String DOWNLOAD_APK_NEW_PACKAGE_FILE_PATH = "download_apk_new_package_file_path";
    public static final String DOWNLOAD_APK_NEW_PACKAGE_MD5 = "download_apk_new_package_md5";
    public static final String DOWNLOAD_APK_NEW_UPDATE_VERSION = "download_apk_new_update_version";
    public static final String LAST_CHECK_APK_VERSION = "last_check_apk_version";

    public static final String PREFS_LAST_APK_CHECK_TIME = "lastdbchecktime";
    public static final String PREFS_CHECK_ON_PROGRESS = "checkonprogress";
    
    public static final long HALF_HOUR =  1800000L;// half a hour

    public final class UpdateLevel {
        public static final int UPDATE_NORMAL = 1;
        public static final int UPDATE_SILENT_DOWN = 2;
        public static final int UPDATE_FORCE = 3;
        public static final int UPDATE_SILENT_DOWN_INSTALL = 4;
    }
    public final class UpdateMode {
        public static final int UPDATE_ALL = 1;
        public static final int UPDATE_PATCH = 0;
    }

    public static int getStringAscii(String str) {
		int asciiValue = 0;
		char[] c = str.toCharArray();
		for(int i:c) {
			asciiValue += i;
		}
		return asciiValue;
    }
    
	public static int getRomVersionFlag() {
		String result = SystemProperties.get(ROM_VERSION_TAG);
		if(TextUtils.isEmpty(result)) {
			return 1;
		}
		return getStringAscii(result);
	}
	
	public static int getPlatformFlag() {
		String result = SystemProperties.get(PLATFORM_TAG);
		if(TextUtils.isEmpty(result)) {
			return 2;
		}else if(QCOM_PLATFORM.equals(result)) {
			return 3;
		}else if(MTK_PLATFORM.equals(result)) {
			return 4;
		}else {
			return 5;
		}
	}
	
	public static int getAndroidFlag() {
		return android.os.Build.VERSION.SDK_INT;
	}
    
    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei;
        if(Build.VERSION.SDK_INT >= 21) {
        	try {
        		imei = tm.getImei(0);
        	} catch (Exception e) {
        		imei = tm.getDeviceId();
        	}
        	
        } else {
        	imei = tm.getDeviceId();
        }
        return imei;
    }

    public static boolean isWifiConnect(Context context){
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

	public synchronized static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("clean_sdk_update_pref", Context.MODE_MULTI_PROCESS);
    }

    public static boolean putInt(Context context, String key, int value) {
        return getPrefs(context).edit().putInt(key, value).commit();
    }

    public static int getInt(Context context, String key, int def) {
        return getPrefs(context).getInt(key, def);
    }

    public static boolean putBoolean(Context context, String key, boolean value) {
        return getPrefs(context).edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key, boolean def) {
        return getPrefs(context).getBoolean(key, def);
    }

    public static boolean putString(Context context, String key, String value) {
        return getPrefs(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key, String def) {
        return getPrefs(context).getString(key, def);
    }
    
    public static boolean putStringSet(Context context, String key, Set<String> value) {
        return getPrefs(context).edit().putStringSet(key, value).commit();
    }

    public static Set<String> getStringSet(Context context, String key, Set<String> def) {
        return getPrefs(context).getStringSet(key, def);
    }

    public static boolean putFloat(Context context, String key, float value) {
        return getPrefs(context).edit().putFloat(key, value).commit();
    }

    public static float getFloat(Context context, String key, float def) {
        return getPrefs(context).getFloat(key, def);
    }

    public static boolean putLong(Context context, String key, long value) {
        return getPrefs(context).edit().putLong(key, value).commit();
    }

    public static long getLong(Context context, String key, long def) {
        return getPrefs(context).getLong(key, def);
    }
    
	public static String getPrefixKey(String prefix, String key) {
		return prefix + "_" + key;
	}
}
