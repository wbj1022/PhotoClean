package com.test.cleansdk;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Environment;

public class FileUtil {

	public final static String OUT_PUT_PATH = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "Cleansdk";
	public final static String OUT_PUT_FILE = "ScanResultInfo.txt";

	private final static long SIZE_GB = 1024 * 1024 * 1024;
	private final static long SIZE_MB = 1024 * 1024;
	private final static long SIZE_KB = 1024;

	public static long getFileSize(File file) {
		if (file == null || !file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			return file.length();
		} else {
			File[] files = file.listFiles();
			long dirSize = 0;
			for (File f : files) {
				dirSize += getFileSize(f);
			}
			return dirSize;
		}
	}

	public static String getStringForSize(long size) {
		String sizeStr = null;
		if (size > SIZE_GB) {
			float sizeFloat = (float) size / 1024 / 1024 / 1024;
			sizeStr = String.format("%.1f", sizeFloat) + "GB";
		} else if (size > SIZE_MB) {
			float sizeFloat = (float) size / 1024 / 1024;
			sizeStr = String.format("%.1f", sizeFloat) + "MB";
		} else if (size > SIZE_KB) {
			float sizeFloat = (float) size / 1024;
			sizeStr = String.format("%.1f", sizeFloat) + "KB";
		} else {
			float sizeFloat = (float) size;
			sizeStr = String.format("%.1f", sizeFloat) + "B";
		}
		return sizeStr;
	}

	public static void writeFileSdcard(String message) {
		try {
			File path = new File(OUT_PUT_PATH);
			File file = new File(OUT_PUT_PATH + File.separator + OUT_PUT_FILE);
			if (!path.exists()) {
				path.mkdir();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(message.getBytes());
			stream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
