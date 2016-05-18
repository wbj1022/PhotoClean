package com.vivo.secureplus.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Time;

import com.vivo.secureplus.LogUtils;
import com.vivo.secureplus.SecurePlus;

import java.io.File;

public class CheckManager implements UpdateCallBack{

    private Context mContext;

    private DownloadCallBack mDownloadCallBack;

    private DownloadUtils mDownloadUtils;

    private int mCheckingAppVersion = 0;

    public CheckManager(Context context, DownloadCallBack downloadCallBack) {
		mContext = context;
		mDownloadCallBack = downloadCallBack;
        mDownloadUtils = new DownloadUtils(context);
	}
	
	@Override
	public void beforeCheckNewVersion() {
	}
    
	public void checkNewVersion(String packageName,int versionCode) {
        LogUtils.logD("CheckManager.getCheckOnProgress()=" +CheckManager.getCheckOnProgress()+",currentDBVersion="+versionCode);
        if(!UpdateUtils.isWifiConnect(SecurePlus.getApplicationContext())){
			LogUtils.logI("Wifi not connect!");
			return;
        }

        if (DownloadUtils.isDownloading(mContext)) {
			// current is downloading,dosomething else
			LogUtils.logI("isDownloading!");
			return;
		}
        
        if(!getCheckOnProgress() || isLastCheckTimeout()){
            
            setCheckOnProgress(true);

            UpdateUtils.putLong(mContext,UpdateUtils.PREFS_LAST_APK_CHECK_TIME, System.currentTimeMillis());

            mCheckingAppVersion = versionCode;

    		RequestInfo info = new RequestInfo(mContext, packageName, versionCode);

            CheckNewVersionTask mCheckTask = new CheckNewVersionTask(mContext, this);
    		mCheckTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, info);
        }
	}

    @Override
	public void afterCheckNewVersion(UpdateAppInfo updateInfo,
			boolean isConnectFailed, boolean isTaskCancelled) {
	    if(isConnectFailed || isTaskCancelled || updateInfo == null){
            LogUtils.logI("isConnectFailed="+isConnectFailed+",isTaskCancelled="+isTaskCancelled);
            setCheckOnProgress(false);
            return;
        }

        //check media status
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
            || DownloadUtils.getStorageAvailableSize() < updateInfo.size) {
            LogUtils.logI("media not mounted or space not enough,abort!");
            setCheckOnProgress(false);
            return;
        }
        
        if(isDownloadNeeded(updateInfo)){
            downloadApk(updateInfo);
            if(mDownloadCallBack != null)
		        mDownloadCallBack.onStartDownload();
        }
	}

    private boolean isDownloadNeeded(UpdateAppInfo updateinfo){
        boolean needDowload = true;

        if(mCheckingAppVersion >= updateinfo.vercode){
            UpdateUtils.putInt(mContext, UpdateUtils.LAST_CHECK_APK_VERSION, mCheckingAppVersion);
            LogUtils.logI("current db is newest!");
            setCheckOnProgress(false);
            return false;
        }
        UpdateUtils.putInt(mContext, UpdateUtils.LAST_CHECK_APK_VERSION, updateinfo.vercode);
        String downloadFileStr = getDownloadFile(updateinfo);
        File downloadFile = new File(downloadFileStr);
        if (downloadFile.exists()) {
            if(MdFive.checkMdFive(updateinfo.md5, downloadFile)) {
                LogUtils.logI("already downloaded:" + downloadFileStr);
                needDowload = false;
            }else{
                String savedPackageMd5 = UpdateUtils.getString(mContext, UpdateUtils.DOWNLOAD_APK_NEW_PACKAGE_MD5, null);
                int savedPackageVersion = UpdateUtils.getInt(mContext, UpdateUtils.DOWNLOAD_APK_NEW_UPDATE_VERSION, -1);
                if(savedPackageVersion > updateinfo.vercode && MdFive.checkMdFive(savedPackageMd5, downloadFile)){//downloaded newer and valid,should not happen
                    LogUtils.logI("downloaded file valid and newer,donot need to download");
                    needDowload = false;
                }else{
                    LogUtils.logI("downloaded file not valid or older,delete and download");
                    downloadFile.delete();
                    mDownloadUtils.removeNotSuccessDownload(mContext);
                }
            }
        }
        if(!needDowload){
            LogUtils.logI("already downloaded,somehow not applied,copy now:" + downloadFileStr);
            CopyDBTask mCheckTask = new CopyDBTask();
            mCheckTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadFileStr, updateinfo.vercode);
        }
        return needDowload;
    }
    
	public void downloadApk(UpdateAppInfo info) {

        String downUrl = info.durl;
        String downMd5 = info.md5;
        String downName = info.filename;
        int downSize = info.size;
        int downMode = info.mode;
        long downloadId = -1;
        
        LogUtils.logI("startDownload.downUrl == " + downUrl + "--downMd5 == " + downMd5
                + "--downName == " + downName + "--downSize == " + downSize + "--downMode == " + downMode 
                + "--result.getMd5() == " + info.md5 + "--desPath ==" + UpdateUtils.UPDATE_DOWNLOAD_PATH);
        if (!TextUtils.isEmpty(downUrl)) {
            UpdateUtils.putString(mContext, UpdateUtils.DOWNLOAD_APK_NEW_PACKAGE_MD5, info.md5);
            UpdateUtils.putString(mContext, UpdateUtils.DOWNLOAD_APK_NEW_PACKAGE_REQUIRED_LOW_MD5,info.lowMd5);
            UpdateUtils.putInt(mContext, UpdateUtils.DOWNLOAD_APK_UPDATE_MODE,downMode);
            UpdateUtils.putInt(mContext, UpdateUtils.DOWNLOAD_APK_UPDATE_LEVEL,info.level);
            UpdateUtils.putInt(mContext, UpdateUtils.DOWNLOAD_APK_NEW_UPDATE_VERSION, info.vercode);

            File fileApk = Environment.getExternalStoragePublicDirectory(UpdateUtils.UPDATE_DOWNLOAD_PATH);
            String apkSavedPah = fileApk.getPath() + File.separator + downName+UpdateUtils.DEFAULT_SUFFIX;
			UpdateUtils.putString(mContext, UpdateUtils.DOWNLOAD_APK_NEW_PACKAGE_FILE_PATH, apkSavedPah);
        }

		try {
            if(info.level == UpdateUtils.UpdateLevel.UPDATE_SILENT_DOWN
					|| info.level == UpdateUtils.UpdateLevel.UPDATE_SILENT_DOWN_INSTALL){
    			downloadId = mDownloadUtils.enqueueHide(downUrl,
    					UpdateUtils.UPDATE_DOWNLOAD_PATH, downName+UpdateUtils.DEFAULT_SUFFIX, UpdateUtils.DEFAULT_MIME_TYPE);
            }else{
                downloadId = mDownloadUtils.enqueue(downUrl,
    					UpdateUtils.UPDATE_DOWNLOAD_PATH, downName+UpdateUtils.DEFAULT_SUFFIX);
            }
		} catch (Exception e) {
		    setCheckOnProgress(false);
			LogUtils.logE("start download failed, ERROR: " + e.getMessage());
		}
        
        if(downloadId > 0) {
			UpdateUtils.putLong(mContext, UpdateUtils.DOWNLOAD_APK_DOWN_ID, downloadId);
		}
	}

    private String getDownloadFile(UpdateAppInfo info) {
	    File fileApk = Environment.getExternalStoragePublicDirectory(UpdateUtils.UPDATE_DOWNLOAD_PATH);
        return fileApk.getPath() + File.separator + info.filename + UpdateUtils.DEFAULT_SUFFIX;
	}

    public static boolean getCheckOnProgress() {
        return UpdateUtils.getInt(SecurePlus.getApplicationContext(), UpdateUtils.PREFS_CHECK_ON_PROGRESS, 0) != 0;
	}

	public static void setCheckOnProgress(boolean onprogress) {
		UpdateUtils.putInt(SecurePlus.getApplicationContext(),UpdateUtils.PREFS_CHECK_ON_PROGRESS, (onprogress ? 1 : 0));
	}

    //ensure PREFS_CHECK_ON_PROGRESS is not abnormally set to true forever
    public boolean isLastCheckTimeout() {
        long currentTime = System.currentTimeMillis();
		long lastDBCheckTime = UpdateUtils.getLong(mContext, UpdateUtils.PREFS_LAST_APK_CHECK_TIME, 0);
        if(lastDBCheckTime > currentTime)
            lastDBCheckTime = currentTime;
        LogUtils.logI("isDownloading="+DownloadUtils.isDownloading(mContext));
        if(currentTime - lastDBCheckTime > UpdateUtils.HALF_HOUR && getCheckOnProgress() && !DownloadUtils.isDownloading(mContext)){
            mDownloadUtils.removeNotSuccessDownload(mContext);
            setCheckOnProgress(false);
            return true;
        }
        return false;
	}

	public interface DownloadCallBack {
		public void onStartDownload();

	}

}
