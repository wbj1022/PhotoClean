package com.vivo.secureplus.update;

public interface UpdateCallBack {
	
	public void beforeCheckNewVersion();
	public void afterCheckNewVersion(UpdateAppInfo updateInfo,
			boolean isConnectFailed, boolean isTaskCancelled);
	//public void afterDownload(Context context, Intent intent);
}
