package com.test.cleansdk.utils;

import com.test.cleansdk.model.PhotoModel;

import android.graphics.Bitmap;

public class BlurPhotoUtil {
	
	public final static double CLARITY_VALUE = 120;
	
	public static void detectFeature(PhotoModel model, Bitmap bitmap) {
		if (model == null || bitmap == null) {
			return;
		}
		model.clarity = computeClarity(getScaledBitmap(bitmap));
	}

	private static Bitmap getScaledBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		if (width < height) {
			return Bitmap.createScaledBitmap(bitmap, 64, Math.round(64.0f * height / width), false);
		} else {
			return Bitmap.createScaledBitmap(bitmap, Math.round(64.0f * width / height), 64, false);
		}		
	}
	
	/*
	 * This method is from liebao;
	 * 
	 * */
	private static double computeClarity(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] array = new int[width * height];
		int[][] array2 = new int [height][width];
		bitmap.getPixels(array, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
            	int pixel = array[width * i + j];
                array2[i][j] = PhotoCommonUtil.rgbToGray(pixel);
            }
        }
        int clarity = 0;
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
            	int weight = array2[i-1][j] + array2[i+1][j] + array2[i][j-1] + array2[i][j+1] - 4 * array2[i][j];
            	if (weight > 100 || weight < -100) {
            		clarity++;
            	}
            }
        }
        return clarity;
	}
}
