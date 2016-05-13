package com.vivo.secureplus.phoneclean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Time;

import com.vivo.secureplus.BaseManager;
import com.vivo.secureplus.LogUtils;
import com.vivo.secureplus.SecurePlus;
import com.vivo.secureplus.Version;
import com.vivo.secureplus.phoneclean.model.PathCacheModel;
import com.vivo.secureplus.phoneclean.utils.FileUtils;
import com.vivo.secureplus.update.CheckManager;
import com.vivo.secureplus.update.UpdateUtils;

import java.util.List;

public class PhoneCleanManager extends BaseManager {
	private ScanTask mScanTask;
	
	@Override
	public void create(Context paramContext) {
		mScanTask = new ScanTask(paramContext);
		LogUtils.logI("clean jar version=" + Version.GIT_VERSION + ",branch=" + Version.CURRENT_BRANCH);
	}
	
	public int getSingletonType() {
		return BaseManager.TYPE_ONCE;
	}
	
	public List<PathCacheModel> scanPackageSoftCache(String pkgName) {
		return mScanTask.scanPackageSoftCache(pkgName);
	}

    public List<PathCacheModel> getUninstalledRubbish() {
		return mScanTask.getUninstalledRubbish();
	}
    
	@SuppressLint("NewApi")
	public static void copyOrUpdateLocalDB(final boolean updateDB) {
		new Thread() {
			public void run() {
				FileUtils.copyOrUpdateLocalDB();
				if(updateDB){
                    int currentDBVersion = FileUtils.getCurrentDBVersion();
                    CheckManager mCheckManager = new CheckManager(SecurePlus.getApplicationContext(), null);
                    mCheckManager.checkNewVersion(FileUtils.FAKE_CLEAN_APK_NAME, currentDBVersion);
                }
			}
        }.start();
	}

    public static List<String> getIntactDataApps() {
		return FileUtils.getIntactDataApps();
	}

    public static int getCurrentDBVersion(){
        return FileUtils.getCurrentDBVersion();
    }
    
	public void destroy() {
		if(mScanTask != null) {
			mScanTask.onDestory();
			mScanTask = null;
		}
	}
}