package com.test.cleansdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;

public class PhotoCompressUtils {
	public final static String TAG = "PhotoCompressUtils";
	public final static int QUALITY = 50;
	public final static double THRESHOLD = 614400.0;
	
	public static boolean compressPhoto(String filePath, long memoryLimit) {
		if (TextUtils.isEmpty(filePath)) {
			return false;
		}
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
			Log.e(TAG, "Photo " + filePath + "is broken");
			return false;
		}
		int width = options.outWidth;
		int height = options.outHeight;
		if (width * height * 4 >= memoryLimit ) {
			Log.i(TAG, filePath + " is out of memory !");
			return false;
		}
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.ARGB_8888;
		options.inSampleSize = 1;
		Bitmap bitmap = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			bitmap = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD(), null, options);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bitmap == null) {
			Log.i(TAG, filePath + " decode failed !");
			return false;
		} 
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, QUALITY, byteArrayOutputStream);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);  
			fileOutputStream.write(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            fileOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static boolean compressPhoto(String filePath) {
		return compressPhoto(filePath, calculateMemoryLimit());
		/*if (TextUtils.isEmpty(filePath)) {
			return false;
		}
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			Log.e(TAG, "Photo " + filePath + "is broken");
			return false;
		}
		int width = options.outWidth;
		int height = options.outHeight;
		long size = width * height;
		if (size > THRESHOLD) {
			options.inSampleSize = (int) Math.sqrt(size / 409600.0);
		}
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.ARGB_8888;
		options.inPurgeable = true;
		options.inInputShareable = true;
		Bitmap bitmap = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			bitmap = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD(), null, options);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bitmap == null) {
			return false;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
		double fileSize = byteArrayOutputStream.toByteArray().length;
		Bitmap zoomImage = bitmap;
		if (fileSize > THRESHOLD) {
			double scale = fileSize / THRESHOLD;
			zoomImage = zoomImage(bitmap, width / Math.sqrt(scale),
					height / Math.sqrt(scale));
		}
		return storeImage(zoomImage, filePath);*/
	}

	private static boolean storeImage(final Bitmap bitmap, final String s) {
		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(
					new File(s));
			bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
			fileOutputStream.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Bitmap zoomImage(Bitmap bitmap, double scaleX, double scaleY) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale((float) scaleX / width, (float) scaleY / height);
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}
	
	public static long calculateMemoryLimit() {
		 long result = 0;
		 Runtime runtime = Runtime.getRuntime();
		 result = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
		 if (result * 2L < runtime.maxMemory()) {
			 result = runtime.maxMemory() / 2L;
		 }
		 return result;
	}

}
