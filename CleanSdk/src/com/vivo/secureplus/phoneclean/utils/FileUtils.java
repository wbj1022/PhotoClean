package com.vivo.secureplus.phoneclean.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.vivo.secureplus.LogUtils;
import com.vivo.secureplus.SecurePlus;
import com.vivo.secureplus.phoneclean.common.PhoneCleanConstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {
	
	private static final String PACKAGE_NAME = SecurePlus.getApplicationContext().getPackageName();
	public static final String DB_PATH = "/data/data/" + PACKAGE_NAME + "/databases/";
    private static final String DB_CONFIG_FILE = "version.ini";
    private static final int ASSET_DB_VERSION = getAssetDatabaseVersion();
    public static final String FAKE_CLEAN_APK_NAME = "com.cleandb";
    private static boolean COPY_OR_LOCAL_UPDATE_DONE = false;
    private static int WAIT_COUNT = 20;
    
    private static int getAssetDatabaseVersion(){
		int version=0;
        InputStream inputStream = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
		Context mContext = SecurePlus.getApplicationContext();
        String result;
        try {
            inputStream = mContext.getAssets().open(DB_CONFIG_FILE);
            inputReader = new InputStreamReader(inputStream); 
            bufReader = new BufferedReader(inputReader);

            while((result = bufReader.readLine()) != null){
                if(result.contains("db.version=")){
                    version = Integer.parseInt(result.substring(result.lastIndexOf("=") + 1).trim());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) { 
            e.printStackTrace();
        } catch (NumberFormatException e){
            e.printStackTrace();
        } finally{
            try{
                if(bufReader != null)
                    bufReader.close();
                if(inputReader != null)
                    inputReader.close();
                if(inputStream != null)
                    inputStream.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return version;
	}
    /**
     * @hide
     * @return null if copy database is fail; otherwise, retrun sdk support package list.
     */
    public static List<String> getIntactDataApps() {
    	List<String> ret = null;
    	
    	if (waitCopyOrLocalUpdateDBDoneIfNeed()) {
    		SQLiteDatabase db = null;
    		Cursor c = null;
    		try {
    			ret = new ArrayList<String>();
    			db = SQLiteDatabase.openDatabase(DB_PATH + PhoneCleanConstant.DB_NAME, null, 
    					SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.CREATE_IF_NECESSARY);
    			c = db.rawQuery("select pkg_name,entirety from package_names" + " where entirety=?", new String[] { "1" });
    			
    			while (c.moveToNext()) {
    				String pkg_name = c.getString(0);
    				int entirety = c.getInt(1);
    				
    				if (1 == entirety) {
    					ret.add(pkg_name);
    				}
    			}
    		} catch(Exception e) {
    			e.printStackTrace();
    		} finally {
    			if (null != c) {
                    c.close();
                }
    			if (null != db) {
    				db.close();
                }
    		}
    	}
    	
    	return ret;
    }

	private static boolean extractAssetToDatabaseDirectory(String fileName) {
        LogUtils.logD("extractAssetToDatabaseDirectory");
        InputStream sourceDatabase = null;
        OutputStream destination = null;
		try {
			File dbDir = new File(FileUtils.DB_PATH);
			if(!dbDir.exists()) {
				boolean success = dbDir.mkdirs();
				if (!success) {
					LogUtils.logE("create DB_PATH error");
					return false;
				}
			}
			File dbFile = new File(FileUtils.DB_PATH + fileName);
			if (!dbFile.exists()) {
				dbFile.createNewFile();
			}

			Context mContext = SecurePlus.getApplicationContext(); 
			sourceDatabase = mContext.getAssets().open(fileName);
			destination = new FileOutputStream(dbFile);
			byte[] buffer = new byte[4096];
			int length;
			
			while ((length = sourceDatabase.read(buffer)) > 0) {
				destination.write(buffer, 0, length);
			}
			destination.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally {
			try {
				if(sourceDatabase != null) {
					sourceDatabase.close();
				}
				if(destination != null) {
					destination.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean extractAssetToFilesDirectory(String fileName) {
		try {
			int length;
			Context mContext = SecurePlus.getApplicationContext(); 
			InputStream sourceFile = mContext.getAssets().open(fileName);
			File destinationPath = new File(mContext.getFilesDir() + "/" + fileName);
			OutputStream destination = new FileOutputStream(destinationPath);

			byte[] buffer = new byte[4096];
			while ((length = sourceFile.read(buffer)) > 0) {
				destination.write(buffer, 0, length);
			}
			sourceFile.close();
			destination.flush();
			destination.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressLint("NewApi")
	public static void copyOrUpdateLocalDB() {
		String dbPath = FileUtils.DB_PATH + PhoneCleanConstant.DB_NAME;
		if (!(new File(dbPath).exists())) {
			if (extractAssetToDatabaseDirectory(PhoneCleanConstant.DB_NAME)) {
				COPY_OR_LOCAL_UPDATE_DONE = true;
			}
		} else {
		    int dataDBVersion = -1;
		    try{
    			SQLiteDatabase database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | 
    					SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.CREATE_IF_NECESSARY);
    			dataDBVersion = database.getVersion();
                database.close();
            }catch(SQLiteException e){
                LogUtils.logE("copyOrUpdateLocalDB "+e.getMessage());
            }
            
            if(FileUtils.ASSET_DB_VERSION > dataDBVersion){
            	LogUtils.logI("update cleaninfo database");
                File journaldb = new File(FileUtils.DB_PATH,PhoneCleanConstant.DB_JOURNAL_NAME);
                if(journaldb.exists()){
                	journaldb.delete();
                }
				if (FileUtils.extractAssetToDatabaseDirectory(PhoneCleanConstant.DB_NAME)) {
					COPY_OR_LOCAL_UPDATE_DONE = true;
				}
            } else {
            	COPY_OR_LOCAL_UPDATE_DONE = true;
            }
		}
	}

    public static int getCurrentDBVersion() {
        int dataDBVersion = -1;
        String dbPath = FileUtils.DB_PATH + PhoneCleanConstant.DB_NAME;
        if (FileUtils.waitCopyOrLocalUpdateDBDoneIfNeed()) {
            try{
                SQLiteDatabase database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | 
    					SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.CREATE_IF_NECESSARY);
                dataDBVersion = database.getVersion();
                database.close();
            }catch(SQLiteException e){
                LogUtils.logE("getCurrentDBVersion "+e.getMessage());
            }
        }
        return dataDBVersion;
    }
	public static boolean waitCopyOrLocalUpdateDBDoneIfNeed() {
		if (!COPY_OR_LOCAL_UPDATE_DONE) {
			int count = 0;
			while(count < WAIT_COUNT) {
				if (!COPY_OR_LOCAL_UPDATE_DONE) {
					count++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					return true;
				}
			}
			LogUtils.logW("waitCopyOrLocalUpdateDBDoneIfNeed timeout");
			return false;
		}
		return true;
	}
	
	public static File mkdirFiles(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		file.createNewFile();

		return file;
	}

    public static boolean isPackageInstalled(String filePath) {
        Context mContext = SecurePlus.getApplicationContext();
        PackageManager mPm = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = mPm.getPackageArchiveInfo(filePath, 0);
            mPm.getPackageInfo(packageInfo.packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isPackageBroken(String filePath) {
        Context mContext = SecurePlus.getApplicationContext();
        PackageManager mPm = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = mPm.getPackageArchiveInfo(filePath, 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return true;
        }
        return packageInfo == null; 
    }
	
}
