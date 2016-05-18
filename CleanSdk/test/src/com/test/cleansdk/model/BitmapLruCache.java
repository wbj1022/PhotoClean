package com.test.cleansdk.model;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class BitmapLruCache {

	private static final String TAG = "BitmapLruCache";
	private static BitmapLruCache sInstance;
	private LruCache<String, Bitmap> mLruCache;

	public static BitmapLruCache getGlobalBitmapCache() {
		if (BitmapLruCache.sInstance == null) {
			BitmapLruCache.sInstance = new BitmapLruCache();
		}
		return BitmapLruCache.sInstance;
	}

	private BitmapLruCache() {
		initialize();
	}

	private void initialize() {
		int maxMemory = (int) Runtime.getRuntime().maxMemory() / 6;
		Log.i(TAG, "maxMemory: " + maxMemory);

		mLruCache = new LruCache<String, Bitmap>(maxMemory) {
			@Override
			protected int sizeOf (String s, Bitmap bitmap) {
				return bitmap.getByteCount();
			}

			@Override
			protected void entryRemoved(boolean evicted, String key,
					Bitmap oldValue, Bitmap newValue) {
				// TODO Auto-generated method stub
				if (evicted || (newValue != null && oldValue != newValue)) {
                    oldValue.recycle();
                }
			}
		};
	}

	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (getBitmapFromCache(key) == null && bitmap != null) {
			mLruCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromCache(String key) {
		return mLruCache.get(key);
	}
	
	public void clearCache() {
        if (mLruCache != null) {
        	mLruCache.evictAll();
        }
    }

}
