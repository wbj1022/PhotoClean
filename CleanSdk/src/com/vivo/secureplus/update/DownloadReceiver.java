package com.vivo.secureplus.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.vivo.secureplus.LogUtils;

public class DownloadReceiver extends BroadcastReceiver{
	private static final String TAG = "DownloadReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		LogUtils.logD("action=" + action);
		
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long idExtas = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            int status = DownloadUtils.getDownloadStatus(context, idExtas);
            String fileName = DownloadUtils.getDownloadFileName(context, idExtas);
            LogUtils.logD("id=" + idExtas + ",fileName="+fileName + ",status="+status);
            
            long cachedDownloadId = UpdateUtils.getLong(context, UpdateUtils.DOWNLOAD_APK_DOWN_ID, -1);
            String cachedFileName = UpdateUtils.getString(context, UpdateUtils.DOWNLOAD_APK_NEW_PACKAGE_FILE_PATH, null);
            int cachedVercode = UpdateUtils.getInt(context, UpdateUtils.DOWNLOAD_APK_NEW_UPDATE_VERSION, -1);
            LogUtils.logD("cachedDownloadId=" + cachedDownloadId + ",cachedFileName="+cachedFileName + ",cachedVercode="+cachedVercode);
       		if(idExtas == cachedDownloadId && fileName != null && fileName.equals(cachedFileName)) {
                if(DownloadUtils.isStatusSuccess(status)) {
                    LogUtils.logD("download success,begin to copy");
            		CopyDBTask mCheckTask = new CopyDBTask();
	                mCheckTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName, Integer.valueOf(cachedVercode));
            	}else{
            	    LogUtils.logE("download failed!!!status="+status);
                    CheckManager.setCheckOnProgress(false);
                }
			}
        }
		
	}
}
