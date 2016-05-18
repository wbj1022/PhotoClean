package com.vivo.secureplus.update;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Locale;

public class RequestInfo{

	public String mPackageName;
	public int mOldVercode = -1;
	public Context mContext;
	public boolean mCheckByUser = false;
	public int mFlag = 0;
    
	public int mResponseStatus;
	public Object mResponse;
	
	public RequestInfo(Context ctx, String packageName, int oldvercode) {
		mPackageName = packageName;
		mContext = ctx;
		mOldVercode = oldvercode;
	}

	private void appendGreneralInfomation(HashMap<String, String> params) {
		
        final String mieiCode = UpdateUtils.getImei(mContext);
        final String modelNumber = Build.MODEL;
        final long elapsedTime = SystemClock.elapsedRealtime();

        if (mieiCode == null || mieiCode.equals("0")) {
            params.put("imei", "012345678987654");
        } else {
            params.put("imei", mieiCode);
        }
        params.put("model", modelNumber);
        params.put("elapsedtime", String.valueOf(elapsedTime));
		params.put("language", getLanguage());
		params.put("pver", String.valueOf(1));
		
	}
    
	public HashMap<String, String> getRequestParams() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("appName", mPackageName);
		params.put("verCode", String.valueOf(mOldVercode));
		params.put("flag", String.valueOf(mFlag));
		params.put("manual", mCheckByUser? String.valueOf(1):String.valueOf(0)); 
		
		appendGreneralInfomation(params);
		return params;
	}

	public void setResponse(Object response) {
		mResponse = response;
		
	}

	public void setResponseStatus(int statu) {
		mResponseStatus = statu;
		
	}

	public boolean isCheckByUser() {
		return mCheckByUser;
	}

	public Object getResponse() {
		return mResponse;
	}

	public int getResponseStatus() {
		
		return mResponseStatus;
	}
    
    private String getLanguage() {
        return Locale.getDefault().getLanguage();
	}
}
