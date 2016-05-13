package com.vivo.secureplus.phoneclean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.vivo.secureplus.SecurePlus;
import android.util.Log;
import com.vivo.secureplus.LogUtils;
import com.vivo.secureplus.phoneclean.common.AESCrpyt;
import com.vivo.secureplus.phoneclean.common.MatchRegularPath;
import com.vivo.secureplus.phoneclean.model.PathCacheModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanTask {
    private AESCrpyt mAESCrpyt = new AESCrpyt();
    private List<String> mStoragePaths = new ArrayList<String>();
    private CleanDataOp mCleanDataOp;
    
	public ScanTask(Context context) {
		initStoragePaths(context);
		mCleanDataOp = new CleanDataOp();
	}

	private void initStoragePaths(Context ctx) {
        StorageManager storageManager = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
        
        StorageVolume[] volumes = storageManager.getVolumeList();
        for (StorageVolume volume : volumes) {
            String path = volume.getPath();
            if(!path.contains("/otg") && !path.contains("/usbotg")){
                File file = new File(path);
                
                if (file.isDirectory() && file.getTotalSpace() > 0l) {
                    mStoragePaths.add(path);
			    }
            }
        }
	}
	
	private void addNormalSoftCache(String queryPath, Cursor c, List<PathCacheModel> scanRet) {
		for(String root : mStoragePaths){
            File file = new File(root+"/"+queryPath);
            
            if(file.exists()){
        		String pkg = c.getString(2);
                int cleanType = c.getInt(3);
                String categoryDesc = c.getString(4);
                String usage = c.getString(5);
                String alert = c.getString(6);
                String junktype = c.getString(7);
        		String desc = c.getString(8);
        		int displayType = c.getInt(10);
        		int entirety = c.getInt(11);
                
                PathCacheModel pcm = new PathCacheModel();
                pcm.mPath = file.getAbsolutePath();
                pcm.mPackageName = pkg;
                pcm.mCategory = categoryDesc;
                pcm.mDescription = desc;
                pcm.mCleanType = (byte) cleanType;
                pcm.mCleanAlert = alert;
                pcm.mUsage = usage;
                pcm.mJunkType = junktype;
                pcm.mRegularType = false;
                pcm.mDisplayType = (byte) displayType;
                pcm.mEntirety = 1 == entirety;
                scanRet.add(pcm);
            }
		}
	}
	
	private void addRegularSoftCache(String queryPath, Cursor c, List<PathCacheModel> scanRet) {
		List<String> paths = MatchRegularPath.matchPath(mStoragePaths, queryPath);

		if (null != paths) {
			String pkg = c.getString(2);
	        int cleanType = c.getInt(3);
	        String categoryDesc = c.getString(4);
	        String usage = c.getString(5);
	        String alert = c.getString(6);
	        String junktype = c.getString(7);
			String desc = c.getString(8);
    		int displayType = c.getInt(10);
    		int entirety = c.getInt(11);
	        
	        PathCacheModel pcm = new PathCacheModel();
	        pcm.mPathList = paths;
	        pcm.mPackageName = pkg;
	        pcm.mDescription = desc;
	        pcm.mCategory = categoryDesc;
	        pcm.mCleanType = (byte) cleanType;
	        pcm.mCleanAlert = alert;
	        pcm.mUsage = usage;
	        pcm.mJunkType = junktype;
	        pcm.mRegularType = true;
            pcm.mDisplayType = (byte) displayType;
            pcm.mEntirety = 1 == entirety;
	        scanRet.add(pcm);
		}
	}
	
	@SuppressLint("NewApi")
	List<PathCacheModel> scanPackageSoftCache(String pkgName) {
		List<PathCacheModel> scanResult = new ArrayList<PathCacheModel>();
		Cursor cursor = null;
		
		try {
			cursor = mCleanDataOp.queryPkg(pkgName);
			Log.v("franco", "pkgName = " + pkgName);
			
			Log.v("franco", "===========================");
			if (null == cursor) {
				LogUtils.logE("cursor is null");
				return null;
			}

			while (cursor.moveToNext()) {

    		    int regularType = cursor.getInt(9);
    		    Log.v("franco", "cursor.getString(1) = " + cursor.getString(1));
    			String queryPath = mAESCrpyt.decrypt(cursor.getString(1));
    			if (0 == regularType) {
    				addNormalSoftCache(queryPath, cursor, scanResult);
    			} else if (1 == regularType) {
    				addRegularSoftCache(queryPath, cursor, scanResult);
    			} else {
    				LogUtils.logW("scanPackageSoftCache regularType is error");
    				return null;
    			}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) cursor.close();
		}
		
		MatchRegularPath.clearCache();
		return scanResult;
	}

    List<PathCacheModel> getUninstalledRubbish() {
		List<PathCacheModel> scanResult = new ArrayList<PathCacheModel>();
		Cursor cursor = null;

        try {
			cursor = mCleanDataOp.queryUnintalled(getInstalledPackagesFormatedArgs());
			if (null == cursor) {
				LogUtils.logE("cursor is null");
				return null;
			}

			while (cursor.moveToNext()) {
    		    int regularType = cursor.getInt(9);
    			String queryPath = mAESCrpyt.decrypt(cursor.getString(1));

    			if (0 == regularType) {
    				addNormalSoftCache(queryPath, cursor, scanResult);
    			} else if (1 == regularType) {
    				addRegularSoftCache(queryPath, cursor, scanResult);
    			} else {
    				LogUtils.logW("scanPackageSoftCache regularType is error");
    				return null;
    			}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) cursor.close();
		}
		
		MatchRegularPath.clearCache();
        return scanResult;
	}

    public String getInstalledPackagesFormatedArgs() {
        PackageManager pm = SecurePlus.getApplicationContext().getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);

        StringBuilder argsBuilder = new StringBuilder();
        argsBuilder.append("(");
        
        final int argsCount = installedApps.size();
        
        for (int i=0; i<argsCount; i++) {
            argsBuilder.append("'");
            argsBuilder.append(installedApps.get(i).packageName);
            argsBuilder.append("'");
            if (i < argsCount - 1) {
                argsBuilder.append(",");
            }
        }
        argsBuilder.append(")");
        LogUtils.logI("argsBuilder="+argsBuilder);
        return argsBuilder.toString();
    }
    
	public void onDestory() {
		if (null != mCleanDataOp) {
			mCleanDataOp.closeDatabase();
			mCleanDataOp = null;
		}
	}
}
