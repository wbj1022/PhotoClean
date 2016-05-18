package com.test.cleansdk.imageloader;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.LruCache;

public class ThumbnailsLruChe {
	private static final String TAG = "ThumbnailsLruChe";
	private static ThumbnailsLruChe mInstance = null;
	private LruCache<String, Bitmap> mThumbCache = null;
	public Handler mWorkerHandler = null;
	private HandlerThread hd = null;

	public ThumbnailsLruChe(boolean isNoHandler) {
		if (false == isNoHandler) {
			return;
		}
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 8;
		mThumbCache = new LruCache<String, Bitmap>(cacheSize) {

			/**
			 * Notify the removed entry that is no longer being cached
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key,
					Bitmap oldValue, Bitmap newValue) {
				// TODO Auto-generated method stub
				Log.d(TAG, "-----------entryRemoved-------------");
				if (evicted || (newValue != null && oldValue != newValue)) {
					oldValue.recycle();
				}
			}

			/**
			 * Measure item size in kilobytes rather than units which is more
			 * practical for a bitmap cache
			 */
			@Override
			protected int sizeOf(String key, Bitmap value) {
				final int bitmapSize = value.getByteCount() / 1024;
				return bitmapSize == 0 ? 1 : bitmapSize;
			}

		};
	}

	public ThumbnailsLruChe() {
		hd = new HandlerThread("ImageLoaderThread");
		hd.start();
		if (null == mWorkerHandler) {
			mWorkerHandler = new Handler(hd.getLooper());
		}

		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 8;
		Log.d("mashuai", "********* ImageAndVideoInCameraAdapter*************"
				+ cacheSize);
		mThumbCache = new LruCache<String, Bitmap>(cacheSize) {

			/**
			 * Notify the removed entry that is no longer being cached
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key,
					Bitmap oldValue, Bitmap newValue) {
				// TODO Auto-generated method stub
				Log.d(TAG, "-----------entryRemoved-------------");
				if (evicted || (newValue != null && oldValue != newValue)) {
					oldValue.recycle();
				}
			}

			/**
			 * Measure item size in kilobytes rather than units which is more
			 * practical for a bitmap cache
			 */
			@Override
			protected int sizeOf(String key, Bitmap value) {
				final int bitmapSize = value.getByteCount() / 1024;
				return bitmapSize == 0 ? 1 : bitmapSize;
			}

		};
	}

	public static synchronized ThumbnailsLruChe getInstance() {
		if (mInstance == null) {
			mInstance = new ThumbnailsLruChe();
		}
		return mInstance;
	}

	/**
	 * Adds a bitmap to both memory and disk cache.
	 * 
	 * @param key
	 *            Unique identifier for the bitmap to store
	 * @param value
	 *            The bitmap drawable to store
	 */
	public void addBitmapToCache(String key, Bitmap value) {
		if (key == null || value == null) {
			return;
		}

		// Add to memory cache
		if (mThumbCache != null) {
			mThumbCache.put(key, value);
		}
	}

	/**
	 * Get from memory cache.
	 *
	 * @param data
	 *            Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		Bitmap memValue = null;

		if (mThumbCache != null) {
			memValue = mThumbCache.get(key);
		}
		return memValue;
	}

	/**
	 * Clears the memory cache associated with this ImageCache object.
	 */
	public void clearCache() {
		if (mThumbCache != null) {
			mThumbCache.evictAll();
			Log.d(TAG, "Memory cache cleared");
		}
	}

	/**
	 * Close HandlerThread.
	 */
	public void closeHandlerThread() {
		if (hd != null) {
			hd.getLooper().quit();
			hd = null;
			Log.d(TAG, "Close Handler thread!");
		}
	}
}
