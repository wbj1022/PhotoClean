package com.vivo.secureplus.update;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Downloads;

import com.vivo.secureplus.LogUtils;

import java.io.File;

@SuppressLint("NewApi")
public class DownloadUtils {
	
	private static final String TAG = "DownloadUtils";

    DownloadManager mDownloadManager;

    public DownloadUtils(Context context){
        mDownloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    public static int getDownloadStatus(Context context, long downloadId) {
    	int status = -1;   	
        Uri uri;
        Cursor cursor = null;
        try {
        	uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId);
			cursor = context.getContentResolver().query(uri,
					new String[] { Downloads.Impl.COLUMN_STATUS }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
                status = cursor.getInt(0);
            }
        } catch (Exception e) {
        	LogUtils.logD("check isStatusSuccess Error: " + e.getMessage());
        } finally {
        	if(cursor != null) {
                cursor.close();
        	}
        }
    	return status;
    }

    public static String getDownloadFileName(Context context, long downloadId) {
    	String filename = null;   	
        Uri uri;
        Cursor cursor = null;
        try {
        	uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId);
			cursor = context.getContentResolver().query(uri,
					new String[] { Downloads.Impl._DATA }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
                filename = cursor.getString(0);
            }
        } catch (Exception e) {
        	LogUtils.logE("check isStatusSuccess Error: " + e.getMessage());
        } finally {
        	if(cursor != null) {
                cursor.close();
        	}
        }
    	return filename;
    }
        
    public long enqueueHide(String url, String dirType, String fileName, String mimeType) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(dirType, fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setMimeType(mimeType);
        return mDownloadManager.enqueue(request);
    }

    public long enqueue(String url, String dirType, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(dirType, fileName);
        request.setTitle("i Manager database update");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        return mDownloadManager.enqueue(request);
    }
    
    public static boolean isDownloading(Context context) {
    	long downloadId = UpdateUtils.getLong(context, UpdateUtils.DOWNLOAD_APK_DOWN_ID, -1);
    	
        Uri uri;
        Cursor cursor = null;
        try {
        	uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, downloadId);
			cursor = context.getContentResolver().query(uri,
					new String[] { Downloads.Impl.COLUMN_STATUS }, null,
					null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int status = cursor.getInt(0);
                LogUtils.logD("downloadId="+downloadId+",status="+status);
                if(status == Downloads.Impl.STATUS_PENDING || status == Downloads.Impl.STATUS_RUNNING
                    || status == Downloads.Impl.STATUS_WAITING_TO_RETRY || status == Downloads.Impl.STATUS_WAITING_FOR_NETWORK
                    || status == Downloads.Impl.STATUS_QUEUED_FOR_WIFI) {
                	cursor.close();
                	return true;
                }
            }
        } catch (Exception e) {
        	LogUtils.logE("isDownloading Error: " + e.getMessage());
        } finally {
        	if(cursor != null) {
                cursor.close();
        	}
        }
        return false;
    }
    
    public void removeNotSuccessDownload(Context context) {
    	long downloadId = UpdateUtils.getLong(context, UpdateUtils.DOWNLOAD_APK_DOWN_ID, -1);
    	if(downloadId != -1L && !isStatusSuccess(getDownloadStatus(context, downloadId))) {
            try{
    		    mDownloadManager.remove(downloadId);
            }catch(java.lang.UnsupportedOperationException e){
                LogUtils.logE("remove Error: " + e.getMessage());
            }
    	}
    }

	public static long getStorageAvailableSize() {
		try {
			File pathFile = Environment.getExternalStorageDirectory(); 
			StatFs sf = new StatFs(pathFile.getPath());

			long blockSize = sf.getBlockSizeLong();
			long availCount = sf.getAvailableBlocksLong();
			
			return blockSize * availCount - 1024 * 500;
		} catch (Exception e) {
			return -1;
		}
	}

    /**
     * Returns whether the status is a success (i.e. 2xx).
     */
    public static boolean isStatusSuccess(int status) {
        return (status >= 200 && status < 300);
    }

}
