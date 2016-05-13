package com.test.cleansdk;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.media.ExifInterface;

public class PhotoCommonUtil {

	public static int rgbToGray(int pixel) {
		int red = (pixel >> 16) & 0xFF;
		int green = (pixel >> 8) & 0xFF;
		int blue = (pixel) & 0xFF;
		// return (0.30 * red + 0.59 * green + 0.11 * blue); //避免浮点数运算
		//==return (int) ((30 * red + 59 * green + 11 * blue) * 1.0 / 100); // 不如位移运算
		return (76 * red + 151 * green + 28 * blue) >> 8;
	}

	public static Bitmap rgbToGray(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int gray = rgbToGray(pixels[width * i + j]);
				gray = alpha | (gray << 16) | (gray << 8) | gray;
				pixels[width * i + j] = gray;
			}
		}
		Bitmap grayBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
		grayBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return grayBitmap;
	}

	public static int readPhotoDegree(String filePath) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(filePath);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	public static Bitmap rotateImage(int n, Bitmap bitmap) {
		if (bitmap == null) {
			return bitmap;
		}
		Matrix matrix = new Matrix();
		matrix.postRotate((float) n);
		try {
			return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
		} catch (OutOfMemoryError outOfMemoryError) {
			return bitmap;
		}
	}
	
	/*
	 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	 */
//	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//		// Raw height and width of image 
//		final int height = options.outHeight;
//		final int width = options.outWidth;
//		int inSampleSize = 1;
//
//		if (height > reqHeight || width > reqWidth) {
//
//			final int halfHeight = height / 2;
//			final int halfWidth = width / 2;
//
//			// Calculate the largest inSampleSize value that is a power of 2 and
//			// keeps both
//			// height and width larger than the requested height and width.
//			while (halfHeight > inSampleSize * reqHeight && halfWidth > inSampleSize * reqWidth) {
//				inSampleSize *= 2;
//			}
//		}
//		return inSampleSize;
//	}

}
