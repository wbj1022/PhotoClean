package com.vivo.secureplus.phoneclean.common;

import com.vivo.secureplus.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchRegularPath {
    private static HashMap<String, File[]> mPathCache = new HashMap<String, File[]>();
    
    public static void clearCache(){
        if(mPathCache != null)
            mPathCache.clear();
    }
	/**
	 * @param initPaths:regularPath's prefix.for example: /storage/emulated0/0,/storage/sdcard0
	 * @param regularPath:a regular path from clean_info.db
	 * @return ret:result that matched file system. return null if not match.
	 */
	public static List<String> matchPath(List<String> initPaths, String regularPath) {
		List<String> ret = new LinkedList<String>();
		if (!validateInitPath(initPaths, regularPath)) {
			LogUtils.logE("matchPath parameter is error");
			return null;
		} else {
			copyToRetList(initPaths, ret);
		}

		String [] splitedPaths = regularPath.split(File.separator);

        int incCount = 0;
		int lastCount = ret.size();
        for (String splitedPath : splitedPaths) {
            String s = splitedPath;

            if ('\u534d' != s.charAt(0)) {
                incCount += normalAppendToRetList(s, ret);
            } else {
                s = s.substring(1);
                incCount = regularAppendToRetList(s, ret);
            }

            for (int j = 0; j < lastCount; j++) {
                ret.remove(0);
            }
            if (0 == incCount) {
                return null;
            }
            lastCount = incCount;
            incCount = 0;
        }
		return ret;
	}

	private static int regularAppendToRetList(String str, List<String> ret) {
		int retCount = 0;
		int size = ret.size();
		
		for (int i = 0; i < size; i++) {
			String s = ret.get(i);
			File f = new File(s);
			
			if (!f.exists()) {
				LogUtils.logW("regularAppendToRetList f doesnot exist " + f.getAbsolutePath());
				continue;
			}
            File[] fs;
            if(mPathCache.get(s) != null){
                fs = mPathCache.get(s);
            }else{
			    fs = f.listFiles();
                if (null == fs || (0 == fs.length)) {
    				LogUtils.logW("regularAppendToRetList f is not dir " + f.getAbsolutePath());
    				continue;
			    }
                mPathCache.put(s, fs);
            }

            Pattern p = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
            for (File f1 : fs) {
                String fileName = f1.getName();
                Matcher m = p.matcher(fileName);

                if (m.matches()) {
                    ret.add(f1.getAbsolutePath());
                    retCount++;
                }
            }
		}
		
		return retCount;
	}
	
	private static int normalAppendToRetList(String s, List<String> ret) {
		int retCount = 0;
		int size = ret.size();
		
		for (int i = 0; i < size; i++) {
			String tmp = ret.get(i) + File.separator + s;
			if (new File(tmp).exists()) {
				ret.add(ret.get(i) + File.separator + s);
				retCount++;	
			} else {
				LogUtils.logW("normalAppendToRetList file does not exist " + tmp);
            }
		}
		
		return retCount;
	}
	
	private static void copyToRetList(List<String> initPaths, List<String> ret) {
		int size = initPaths.size();
		for(int i = 0; i < size; i++) {
			ret.add(initPaths.get(i));
		}
	}
	
	private static boolean validateInitPath(List<String> initPath, String regularPath) {
        return !(null == initPath || (0 == initPath.size()) || File.separatorChar == regularPath.charAt(0));
    }
	
	/*private static List<StringBuilder> initCutRegularPath(String regularPath) {
		List<StringBuilder> cutedRegularPaths = new ArrayList<StringBuilder>();
		
		if (regularPath.charAt(0) == File.separatorChar) {
			LogUtils.logE("initCutRegularPath regularPath is error");
			return null;
		}
		
		int len = regularPath.length();
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < len; i++) {
			if (regularPath.charAt(i) == File.separatorChar) {
				cutedRegularPaths.add(sb);
				sb = new StringBuilder();
			} else {
				sb.append(regularPath.charAt(i));
				if (i == len - 1) {
					cutedRegularPaths.add(sb);
				}
			}
		}
		
		return cutedRegularPaths;
	}*/
}