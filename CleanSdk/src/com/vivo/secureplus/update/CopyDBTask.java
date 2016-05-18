package com.vivo.secureplus.update;

import android.os.AsyncTask;

import com.vivo.secureplus.LogUtils;
import com.vivo.secureplus.phoneclean.common.PhoneCleanConstant;
import com.vivo.secureplus.phoneclean.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CopyDBTask extends
		AsyncTask<Object, Void, Void> {

	private static final String TAG = "CopyDBTask";

    public CopyDBTask() {
    }

	@Override
	protected Void doInBackground(Object... params) {
        String filePath = (String)params[0];
        int vercode = (Integer)params[1];//parse package?
        int currentVercode = FileUtils.getCurrentDBVersion();
        LogUtils.logD("filePath="+filePath+",vercode="+vercode+",currentVercode="+currentVercode);
        if(currentVercode < vercode){
            extractAndCopyDB(filePath);
        }else{
            LogUtils.logI("already newest");
        }
        CheckManager.setCheckOnProgress(false);
        return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

    private void extractAndCopyDB(String filePath){
        boolean success = false;
        int BUFFER = 4096;
        File destFile = new File(FileUtils.DB_PATH + PhoneCleanConstant.DB_NAME);
        File bakFile = new File(FileUtils.DB_PATH + PhoneCleanConstant.DB_NAME+".bak");
        try {
            ZipFile zf = new ZipFile(filePath);
            for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {  
                ZipEntry entry = (ZipEntry) entries.nextElement();
                LogUtils.logD("get entry="+entry.getName());
                if(("assets/"+PhoneCleanConstant.DB_NAME).equals(entry.getName())){
                    LogUtils.logI("copy begin");
                    int count;
                    byte data[] = new byte[BUFFER];
                    InputStream fis = zf.getInputStream(entry);
                    
                    File destDir = new File(destFile.getParent());
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    if(destFile.exists()){
                        if(!destFile.renameTo(bakFile))
                            break;
                    }
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = fis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    
                    dest.flush();
                    dest.close();
                    fis.close();
                    success = true;
                    break;
                }
                if(!entries.hasMoreElements()){
                    LogUtils.logE("error!bad download package,not db found!");
                }
            }
        }
        catch (Exception e) {
            LogUtils.logE(e.getMessage());
        }
        finally{
            File journaldb = new File(FileUtils.DB_PATH,PhoneCleanConstant.DB_JOURNAL_NAME);
            if(journaldb.exists()){
            	journaldb.delete();
            }
            if(success){
                LogUtils.logI("copy db success!");
                if(bakFile.exists()){
                	bakFile.delete();
                }
            }else{
                LogUtils.logE("copy db failed!");
                if(bakFile.exists()){
                    bakFile.renameTo(destFile);
                }
            }
        }
   }
    
}
