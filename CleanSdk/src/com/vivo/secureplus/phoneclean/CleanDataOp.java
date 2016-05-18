package com.vivo.secureplus.phoneclean;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.vivo.secureplus.LogUtils;
import com.vivo.secureplus.SecurePlus;
import com.vivo.secureplus.phoneclean.common.PhoneCleanConstant;
import com.vivo.secureplus.phoneclean.utils.FileUtils;

import java.util.HashMap;

public class CleanDataOp{
    private HashMap<String,String> mLocalTableMap;
    private SQLiteDatabase mDatabase;
	private String mCurrentTable;
    
	public CleanDataOp() {
		// TODO Auto-generated constructor stub
		initLocalTableMap();
		openDataBase(FileUtils.DB_PATH, PhoneCleanConstant.DB_NAME);
	}
	
	private void initLocalTableMap() {
		mLocalTableMap = new HashMap<String,String>();
		mLocalTableMap.put("en_US","path_details_query_en");
		mLocalTableMap.put("zh_CN","path_details_query_zh_rCN");
		mLocalTableMap.put("zh_HK","path_details_query_zh_rHK");
		mLocalTableMap.put("zh_TW","path_details_query_zh_rTW");

        Configuration config = SecurePlus.getApplicationContext().getResources().getConfiguration();
        mCurrentTable = mLocalTableMap.get(config.locale.toString());
        if(null == mCurrentTable){
            mCurrentTable = mLocalTableMap.get("zh_CN");
        }
	}
	
	@SuppressLint("NewApi")
	private SQLiteDatabase openDataBase(String path, String dbfile) {
		if (null != mDatabase) {
			if (!mDatabase.isOpen()) {
				LogUtils.logI("database maybe not been closed");
				mDatabase = null;
			} else {
				LogUtils.logI("database has opened");
				return mDatabase;
			}
		}
		
		if (FileUtils.waitCopyOrLocalUpdateDBDoneIfNeed()) {
			try {
				mDatabase = SQLiteDatabase.openDatabase(path + dbfile, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | 
						SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.CREATE_IF_NECESSARY);
	            LogUtils.logI("use db version="+mDatabase.getVersion());	
			} catch(SQLiteException e) {
				LogUtils.logE("openDataBase " + e.getMessage());
			}
		}
		
		return mDatabase;
	}
	
	public Cursor queryPkg(String pkg) {
		if (null == pkg || null == mDatabase) {
			return null;
		}
        LogUtils.logD("queryPkg use db version="+mDatabase.getVersion());
        Cursor cursor = null;
        try{
            cursor = mDatabase.rawQuery("select * from "+mCurrentTable+" where pkg_name=?", new String[] { pkg });
        }catch(SQLiteException e){
            LogUtils.logE("queryPkg "+e.getMessage());
        }
		return cursor;
	}

    public Cursor queryUnintalled(String installedAppsArgs) {
		if (null == mDatabase) {
			return null;
		}
        LogUtils.logD("queryPkg use db version="+mDatabase.getVersion());
        Cursor cursor = null;
        try{
            cursor = mDatabase.rawQuery("select * from "+mCurrentTable+" where pkg_name not in "+installedAppsArgs, null);
        }catch(SQLiteException e){
            LogUtils.logE("queryUnintalled "+e.getMessage());
        }
		return cursor;
	}
        
	public void closeDatabase() {
		if (null != mDatabase && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}
	}
}
