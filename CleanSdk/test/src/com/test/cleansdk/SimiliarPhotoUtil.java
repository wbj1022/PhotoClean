package com.test.cleansdk;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class SimiliarPhotoUtil {
	
	private final static int GRID_SIZE = 6;
	
	public static void detectFeature(PhotoModel model, Bitmap bitmap) {
		if (model == null || bitmap == null) {
			return;
		}
		model.avgGrid = computeAvgGrid(getScaledBitmap(bitmap, 100));
		model.colorGrid = computeColorGrid(getScaledBitmap(bitmap, GRID_SIZE));
	}
	
	private static Bitmap getScaledBitmap(Bitmap bitmap, int newSize) {
		return Bitmap.createScaledBitmap(bitmap, newSize, newSize, true);
	}
	
	private static int computeAvgGrid(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int totalGrad = 0;
		
		int[] array = new int[width * height];
		int[][] array2 = new int [height][width];
		bitmap.getPixels(array, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
            	int pixel = array[width * i + j];
                array2[i][j] = PhotoCommonUtil.rgbToGray(pixel);
            }
        }
		for (int i = 1; i < height - 1; i++) {
			for (int j = 1; j < width - 1; j++) {
				int grayDis = (Math.abs(array2[i][j + 1] - array2[i][j - 1]) +
						Math.abs(array2[i + 1][j] - array2[i - 1][j])) / 2;
				totalGrad += grayDis;
			}
		}
        return totalGrad / (height * width);
	}
	
	private static String computeColorGrid(Bitmap bitmap) {
		int[] array = new int[GRID_SIZE * GRID_SIZE];
		bitmap.getPixels(array, 0, GRID_SIZE, 0, 0, GRID_SIZE, GRID_SIZE);
        StringBuilder colorString = new StringBuilder("");
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int pixel = array[i * GRID_SIZE + j];
                colorString.append(((0xFF0000 & pixel) >> 16) + ",").append(((0xFF00 & pixel) >> 8) + ",").append((pixel & 0xFF) + ",");
            }
        }
        String result = "";
        if (colorString.length() > 0) {
        	result = colorString.subSequence(0, colorString.length() - 1).toString();
        }
        return result;
    }

	public static boolean isSameValueArray(int[] array1, int[] array2) {
		if (array1 == null || array2 == null) {
			return false;
		}
		if (array1.length == array2.length && array1.length > 0) {
			int length = array1.length;
			int similiarPoints = 0;
			int totalDis = 0;
			for (int i = 0; i < length; i++) {
				int abs = Math.abs(array1[i] - array2[i]);
				totalDis += abs;
				if (abs < 40) {
					similiarPoints++;
				}
			}
			if ((similiarPoints + 0.01) / length > 0.8 && totalDis / length < 32) {
				return true;
			}
		}
		return false;
	}
	
	public static int[] stringIntValueSplit(String colorGrid) {
		if (TextUtils.isEmpty(colorGrid)) {
			return null;
		}
		String[] split = colorGrid.split(",");
		if (split == null || split.length <= 0) {
			return null;
		}
		int[] array = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			array[i] = Integer.parseInt(split[i]);
		}
		return array;
	}
	
	/*
	 * dHash 1.缩小图片：收缩到9*8的大小，一遍它有72的像素点
	 * 2.转化为灰度图：把缩放后的图片转化为256阶的灰度图。（具体算法见平均哈希算法步骤）
	 * 3.计算差异值：dHash算法工作在相邻像素之间，这样每行9个像素之间产生了8个不同的差异，一共8行，则产生了64个差异值
	 * 4.获得指纹：如果左边的像素比右边的更亮，则记录为1，否则为0.
	 */
	
	public int WIDTH = 9;
	public int HEIGHT = 8;
	public String dHash(int[] pixels) {
		int[] comps = new int[(WIDTH - 1) * HEIGHT];
		int k = 0;
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < (WIDTH - 1); j++) {
				if (pixels[i * WIDTH + j] > pixels[i * WIDTH + j + 1]) {
					comps[k] = 1;
				} else {
					comps[k] = 0;
				}
				k++;
			}
		}
		StringBuffer hashCode = new StringBuffer();
		for (int i = 0; i < k; i++) {
			hashCode.append(Integer.toString(comps[i]));
		}
		return hashCode.toString();
	}
	
	public int getHammingDistance(String str1, String str2) {
		int different = 0;
		for (int i = 0, length = str1.length(); i < length; i++) {
			if (str1.charAt(i) != str2.charAt(i)) {
				different++;
			}
		}
		return different;
	}
}
