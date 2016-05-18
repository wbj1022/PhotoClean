package com.test.cleansdk.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.test.cleansdk.model.PhotoModel;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;

public class ImageUtil {
	private static ImageUtil mInstance;
	private static final String[] IMAGE_INCAMERA_PROJECTION = new String[] {
			MediaColumns._ID, ImageColumns.BUCKET_ID, FileColumns.MEDIA_TYPE,
			ImageColumns.BUCKET_DISPLAY_NAME, MediaColumns.DATA,
			MediaColumns.DISPLAY_NAME, MediaColumns.DATE_MODIFIED };

	private static final String TAG = "PhotoManager";
	private static final String EXTERNAL_MEDIA = "external";
	private static String INTERNAL_STORAGE_DECTORY = "/storage/emulated/0";
	private static String EXTERNAL_STORAGE_DECTORY = "/storage/sdcard0";
	private static String EXTERNAL_SD_DECTORY = "/storage/sdcard1";
	public static final int INTERNAL_CAMERA_BUCKET_ID1 = getBucketId(INTERNAL_STORAGE_DECTORY
			+ "/DCIM/Camera");

	public static final int INTERNAL_CAMERA_BUCKET_ID1_CN = getBucketId(INTERNAL_STORAGE_DECTORY
			+ "/DCIM/\u76f8\u673a");

	public static final int EXTERNAL_CAMERA_BUCKET_ID1 = getBucketId(EXTERNAL_STORAGE_DECTORY
			+ "/DCIM/Camera");

	public static final int EXTERNAL_CAMERA_BUCKET_ID1_CN = getBucketId(EXTERNAL_STORAGE_DECTORY
			+ "/DCIM/\u76f8\u673a");

	public static final int EXTERNAL_SD_CAMERA_BUCKET_ID1 = getBucketId(EXTERNAL_SD_DECTORY
			+ "/DCIM/Camera");

	public static final int EXTERNAL_SD_CAMERA_BUCKET_ID1_CN = getBucketId(EXTERNAL_SD_DECTORY
			+ "/DCIM/\u76f8\u673a");

	public static final int INTERNAL_CAMERA_BUCKET_ID2 = getBucketId(INTERNAL_STORAGE_DECTORY
			+ "/Camera");

	public static final int INTERNAL_CAMERA_BUCKET_ID2_CN = getBucketId(INTERNAL_STORAGE_DECTORY
			+ "/\u76f8\u673a");

	public static final int EXTERNAL_CAMERA_BUCKET_ID2 = getBucketId(EXTERNAL_STORAGE_DECTORY
			+ "/Camera");

	public static final int EXTERNAL_CAMERA_BUCKET_ID2_CN = getBucketId(EXTERNAL_STORAGE_DECTORY
			+ "/\u76f8\u673a");

	public static final int EXTERNAL_SD_CAMERA_BUCKET_ID2 = getBucketId(EXTERNAL_SD_DECTORY
			+ "/Camera");

	public static final int EXTERNAL_SD_CAMERA_BUCKET_ID2_CN = getBucketId(EXTERNAL_SD_DECTORY
			+ "/\u76f8\u673a");

	public static ImageUtil getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ImageUtil(context);
		}
		return mInstance;
	}

	public ImageUtil(Context context) {
		context.getContentResolver();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		if (width == 320) { // 320x480
			
		} else if (width == 540) { // 540x960
			
		} else if (width == 480) { // 480x800
			
		} else if (width == 720) { // 720x1280
			
		} else if (width == 1080) { // 1080x1920
			
		} else if (width == 1440) { // 1440x2560
			
		} else {
		}
	}

	@SuppressLint("DefaultLocale")
	private static int getBucketId(String path) {
		return path.toLowerCase().hashCode();
	}
	
	public static List<String> getCameraPhotoPath(Context context) {
		Cursor cursor = getCameraPhotoCursor(context);
		List<String> photoPath = null;
		if (cursor != null && cursor.getCount() > 0) {
			photoPath = new ArrayList<String>();
			while (cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
				photoPath.add(path);
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return photoPath;
	}
	
	public static List<PhotoModel> getCameraPhoto(Context context) {
		Cursor cursor = getCameraPhotoCursor(context);
		List<PhotoModel> modelList = null;
		if (cursor != null && cursor.getCount() > 0) {
			modelList = new ArrayList<PhotoModel>();
			while (cursor.moveToNext()) {
				PhotoModel model = new PhotoModel();
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
				long id = cursor.getLong(cursor.getColumnIndex(MediaColumns._ID));
				model.filePath = path;
				model.mediaId = id;
				modelList.add(model);
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return modelList;
	}

	public static Cursor getCameraPhotoCursor(Context context) {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Files.getContentUri(EXTERNAL_MEDIA),
					IMAGE_INCAMERA_PROJECTION,
					"(" + ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + " OR "
							+ ImageColumns.BUCKET_ID + " =? " + ") AND "
							+ FileColumns.MEDIA_TYPE + " =? ",
					new String[] { String.valueOf(INTERNAL_CAMERA_BUCKET_ID1),
							String.valueOf(INTERNAL_CAMERA_BUCKET_ID1_CN),
							String.valueOf(EXTERNAL_CAMERA_BUCKET_ID1),
							String.valueOf(EXTERNAL_CAMERA_BUCKET_ID1_CN),
							String.valueOf(EXTERNAL_SD_CAMERA_BUCKET_ID1),
							String.valueOf(EXTERNAL_SD_CAMERA_BUCKET_ID1_CN),
							String.valueOf(INTERNAL_CAMERA_BUCKET_ID2),
							String.valueOf(INTERNAL_CAMERA_BUCKET_ID2_CN),
							String.valueOf(EXTERNAL_CAMERA_BUCKET_ID2),
							String.valueOf(EXTERNAL_CAMERA_BUCKET_ID2_CN),
							String.valueOf(EXTERNAL_SD_CAMERA_BUCKET_ID2),
							String.valueOf(EXTERNAL_SD_CAMERA_BUCKET_ID2_CN),
							String.valueOf(FileColumns.MEDIA_TYPE_IMAGE) },
					MediaStore.Images.Media.DATE_MODIFIED);
		} catch (Exception e) {
			return null;
		}
		return cursor;
	}
	
	public static Bitmap loadImageFromLocal(ContentResolver resolver, long mediaId, ImageView imageView) {
		if (mediaId < 0) {
			return null;
		}
		Bitmap bitmap = Thumbnails.getThumbnail(resolver, mediaId, Images.Thumbnails.MINI_KIND, null);
		
		return bitmap;
	}

	public static Bitmap loadImageFromLocal(String filePath, ImageView imageView) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if (file == null || !file.exists()) {
			return null;
		}
		ImageSize imageSize = getImageViewSize(imageView);
		
		Bitmap bitmap = null;

		BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
		mBitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, mBitmapOptions);
		if (mBitmapOptions.mCancel || mBitmapOptions.outWidth == -1
				|| mBitmapOptions.outHeight == -1) {
			Log.e(TAG, "Photo " + filePath + "is broken");
			return null;
		}
		mBitmapOptions.inSampleSize = caculateInSampleSize(mBitmapOptions, imageSize.width, imageSize.width);
		mBitmapOptions.inJustDecodeBounds = false;
		mBitmapOptions.inDither = false;
		mBitmapOptions.inPreferredConfig = null;
		try {
			bitmap = BitmapFactory.decodeFile(filePath, mBitmapOptions);
		} catch (OutOfMemoryError e) {
			bitmap = null;
		}
		int photoDegree = PhotoCommonUtil.readPhotoDegree(filePath);
		if (photoDegree != 0) {
			bitmap = PhotoCommonUtil.rotateImage(photoDegree, bitmap);
		}
		return bitmap;
	}
	
	public static int caculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;

		int inSampleSize = 1;

		if (width > reqWidth || height > reqHeight) {
			int widthRadio = Math.round(width * 1.0f / reqWidth);
			int heightRadio = Math.round(height * 1.0f / reqHeight);

			inSampleSize = Math.max(widthRadio, heightRadio);
		}

		return inSampleSize;
	}
	
	public static ImageSize getImageViewSize(ImageView imageView) {

		ImageSize imageSize = new ImageSize();
		DisplayMetrics displayMetrics = imageView.getContext().getResources()
				.getDisplayMetrics();

		LayoutParams lp = imageView.getLayoutParams();

		int width = imageView.getWidth();// 获取imageview的实际宽度
		if (width <= 0) {
			width = lp.width;// 获取imageview在layout中声明的宽度
		}
		if (width <= 0) {
			width = imageView.getMaxWidth();// 检查最大值
		}
		if (width <= 0) {
			width = displayMetrics.widthPixels;
		}

		int height = imageView.getHeight();// 获取imageview的实际高度
		if (height <= 0) {
			height = lp.height;// 获取imageview在layout中声明的宽度
		}
		if (height <= 0) {
			height = imageView.getMaxHeight();// 检查最大值
		}
		if (height <= 0) {
			height = displayMetrics.heightPixels;
		}
		imageSize.width = width;
		imageSize.height = height;

		return imageSize;
	}
	
	public static class ImageSize {
		int width;
		int height;
	}
}
