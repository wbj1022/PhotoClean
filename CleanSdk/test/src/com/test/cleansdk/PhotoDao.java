package com.test.cleansdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PhotoDao {
	public static PhotoDao mInstance;
	public SQLiteDatabase mPhotoDB;

	public static PhotoDao getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PhotoDao(context);
		}
		return mInstance;
	}

	public PhotoDao(Context context) {
		if (mPhotoDB == null) {
			mPhotoDB = PhotoDBHelper.getInstance(context).getWritableDatabase();
		}
	}

	public PhotoModel getDbRecorder(String path) {
		Cursor cursor = null;
		PhotoModel model = null;
		try {
			cursor = mPhotoDB.rawQuery("select * from "
					+ PhotoDBHelper.TABLE_NAME + " where "
					+ PhotoDBHelper.FILE_PATH + " = ? ", new String[] { path });
			if (cursor != null && cursor.moveToNext()) {
				model = new PhotoModel();
				model.lastModified = cursor.getLong(3);
			}
		} catch (Exception e) {
			model = null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return model;
	}
	
	public void saveOrUpdatePhotoFeature(PhotoModel model, boolean isDetected) {
		ContentValues values = new ContentValues();
		values.put(PhotoDBHelper.FILE_PATH, model.filePath);
		values.put(PhotoDBHelper.MEDIA_ID, model.mediaId);
		values.put(PhotoDBHelper.LAST_MODIFIED, model.lastModified);
		values.put(PhotoDBHelper.CLARITY, model.clarity);
		values.put(PhotoDBHelper.COLOR_GRID, model.colorGrid);
		values.put(PhotoDBHelper.AVG_GRID, model.avgGrid);
		if (isDetected) {
			mPhotoDB.update(PhotoDBHelper.TABLE_NAME, values, PhotoDBHelper.FILE_PATH + "=?", new String[] {model.filePath});
		} else {
			mPhotoDB.insert(PhotoDBHelper.TABLE_NAME, null, values);
		}
		
	}
	
	public void updateSimiliarFeature(String path, int group) {
		ContentValues values = new ContentValues();
		values.put(PhotoDBHelper.SIMILIAR_GROUP, group);
		mPhotoDB.update(PhotoDBHelper.TABLE_NAME, values, PhotoDBHelper.FILE_PATH + "=?", new String[] {path});
	}
	
	public int delete(String path) {
		return mPhotoDB.delete(PhotoDBHelper.TABLE_NAME, PhotoDBHelper.FILE_PATH + "=?", new String[] {path});
	}
}
