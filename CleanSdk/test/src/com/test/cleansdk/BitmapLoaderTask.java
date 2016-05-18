package com.test.cleansdk;

import java.lang.ref.WeakReference;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.widget.ImageView;

public class BitmapLoaderTask extends AsyncTask<Long, Void, Bitmap> {

	private Context mContext;
	private ContentResolver mResolver;
	private long data;
	private BitmapFactory.Options options;
	private WeakReference<ImageView> imageViewReference;

	public BitmapLoaderTask(final ImageView imageView) {

		mContext = imageView.getContext().getApplicationContext();
		mResolver = mContext.getContentResolver();
		data = 0L;
		options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(Long... params) {
		// TODO Auto-generated method stub

		data = params[0];
		Bitmap bitmap;
		if (data <= 0) {
			bitmap = null;
		} else {
			bitmap = Thumbnails.getThumbnail(mResolver, data, Images.Thumbnails.MINI_KIND, options);
		}
		String key = String.valueOf(params[0]);
		BitmapLruCache.getGlobalBitmapCache().addBitmapToCache(key, bitmap);
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// TODO Auto-generated method stub
		
		Bitmap bitmap = result;
		if (isCancelled() && result != null) {
			result.recycle();
			bitmap = null;
		}
		if (imageViewReference != null) {
			ImageView imageView = imageViewReference.get();
			if (this == getBitmapLoaderTask(imageView) && imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	public static BitmapLoaderTask getBitmapLoaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				return ((AsyncDrawable) drawable).getBitmapWorkerTask();
			}
		}
		return null;
	}
	
	public static boolean cancelPotentialWork(long mediaId, ImageView imageView) {
        BitmapLoaderTask bitmapLoaderTask = BitmapLoaderTask.getBitmapLoaderTask(imageView);
        if (bitmapLoaderTask != null) {
            if (bitmapLoaderTask.data != 0L && bitmapLoaderTask.data == mediaId) {
                return false;
            }
            bitmapLoaderTask.cancel(true);
        }
        return true;
    }

	static class AsyncDrawable extends BitmapDrawable {
		private WeakReference<BitmapLoaderTask> bitmapLoaderTaskReference;

		public AsyncDrawable(Bitmap bitmap, BitmapLoaderTask bitmapWorkerTask) {
			super(null, bitmap);
			bitmapLoaderTaskReference = new WeakReference<BitmapLoaderTask>(bitmapWorkerTask);
		}

		public BitmapLoaderTask getBitmapWorkerTask() {
			return bitmapLoaderTaskReference.get();
		}
	}

}
