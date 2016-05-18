package com.test.cleansdk.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ImageLoader {

	private static ImageLoader mInstance;
	private BitmapLruCache mBitmapLruCache;
	
	public static ImageLoader getLoader() {
		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader();
				}
			}
		}
		return mInstance;
	}
	
	private ImageLoader() {
		initialize();
	}  

	private void initialize() {
		mBitmapLruCache = BitmapLruCache.getGlobalBitmapCache();
		mBitmapLruCache.clearCache();
	}
	
	public void loadImage(long medieId, ImageView imageView) {
		Bitmap bitmapFromMemCache = mBitmapLruCache.getBitmapFromCache(String
				.valueOf(medieId));
		if (bitmapFromMemCache != null) {
			imageView.setImageBitmap(bitmapFromMemCache);
		} else if (BitmapLoaderTask.cancelPotentialWork(medieId, imageView)) {
			BitmapLoaderTask bitmapLoaderTask = new BitmapLoaderTask(imageView);
			Drawable drawable = new BitmapLoaderTask.AsyncDrawable(bitmapFromMemCache, bitmapLoaderTask);
			imageView.setImageDrawable(drawable);
			bitmapLoaderTask.execute(new Long[] { medieId });
		}
	}
	
}
