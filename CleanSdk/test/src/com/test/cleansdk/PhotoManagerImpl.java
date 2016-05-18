package com.test.cleansdk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;

public class PhotoManagerImpl implements PhotoMangerInterface {
	
	//private static final String TAG = "PhotoManagerImpl";
	private static PhotoManagerImpl mInstance;
	private Context mContext;
	private ContentResolver mResolver;
	private PhotoDao mPhotoDao;
	private int similiarGroups;
	
	public static PhotoManagerImpl getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PhotoManagerImpl(context);
		}
		return mInstance;		
	}
	
	public PhotoManagerImpl(Context context) {
		mContext = context;
		mResolver = context.getContentResolver();
		mPhotoDao = PhotoDao.getInstance(mContext);
	}
	
	@Override
	public void detectFeature(List<PhotoModel> models) {
		// TODO Auto-generated method stub
		if (models != null) {
			for (PhotoModel model : models) {
				detectFeature(model);
			}
			updateSimiliarInfo();
		}
	}
	
	@Override
	public boolean detectFeature(PhotoModel model) {
		// TODO Auto-generated method stub
		
		boolean isDetected = false;
		PhotoModel recordModel = mPhotoDao.getDbRecorder(model.filePath);
		if (recordModel != null) {
			isDetected = true;
			File file = new File(model.filePath);
			if (file == null || !file.exists()) {
				mPhotoDao.delete(model.filePath);
				return false;
			}
			if (model.lastModified == recordModel.lastModified) {
				return true;
			}
		}
		
		Bitmap bitmap = Thumbnails.getThumbnail(mResolver, model.mediaId, Images.Thumbnails.MINI_KIND, null);
		SimiliarPhotoUtil.detectFeature(model, bitmap);
		BlurPhotoUtil.detectFeature(model, bitmap);
		mPhotoDao.saveOrUpdatePhotoFeature(model, isDetected);
		
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		return true;
	}
	
	@Override
	public void updateSimiliarInfo() {
		// TODO Auto-generated method stub
		similiarGroups = 0;
		List<List<PhotoModel>> similiarPhotoModelList = classifySimiliarPhotos();
		if (similiarPhotoModelList != null && similiarPhotoModelList.size() > 0) {
			for (List<PhotoModel> modelList : similiarPhotoModelList) {
				if (modelList != null && modelList.size() > 1) {
					similiarGroups++;
					for (PhotoModel model : modelList) {
						model.similiarGroup = similiarGroups;
						mPhotoDao.updateSimiliarFeature(model.filePath, similiarGroups);
					}
				}
			}
		}
	}
	
	@Override
	public int getSimiliarGroupCount() {
		// TODO Auto-generated method stub
		return similiarGroups;
	}
	
	public List<List<PhotoModel>> classifySimiliarPhotos() {
		// TODO Auto-generated method stub
		List<PhotoModel> totalPhotos = getTotalPhotos();
		if (totalPhotos == null || totalPhotos.size() < 2) {
			return null;
		}
		int size = totalPhotos.size();
		PhotoModel model1;
		PhotoModel model2;
		int[] stringIntValueSplit1;
		int[] stringIntValueSplit2;
		List<List<PhotoModel>> similiarPhotos = new ArrayList<List<PhotoModel>>();
		for (int i = 0; i < size; i++) {
			model1 = totalPhotos.get(i);
			if (model1.isCompared) {
				continue;
			}
			model1.isCompared = true;
			stringIntValueSplit1 = SimiliarPhotoUtil.stringIntValueSplit(model1.colorGrid);
			if (stringIntValueSplit1 == null || stringIntValueSplit1.length < 1) {
				continue;
            }
			List<PhotoModel> list = new ArrayList<PhotoModel>();
			for (int j = i + 1; j < size; j++) {
				model2 = totalPhotos.get(j);
				if (model2.isCompared) {
					continue;
				}
				if (Math.abs(model1.avgGrid - model2.avgGrid) <= 4.0) {
					stringIntValueSplit2 = SimiliarPhotoUtil.stringIntValueSplit(model2.colorGrid);
					if (stringIntValueSplit1 == null || stringIntValueSplit1.length < 1) {
						continue;
		            }
					if (SimiliarPhotoUtil.isSameValueArray(stringIntValueSplit1, stringIntValueSplit2)) {
						if (list.size() < 1) {
							list.add(model1);
						}
						list.add(model2);
						model2.isCompared = true;
					}
				}
			}
			if (list.size() > 1) {
				similiarPhotos.add(list);
			} else {
				mPhotoDao.updateSimiliarFeature(model1.filePath, 0);
			}
		}
		return similiarPhotos;
	}

	public List<List<PhotoModel>> getSimiliarPhotos() {
		Cursor cursor = null;
		String[] columns = new String[] {PhotoDBHelper.FILE_PATH, PhotoDBHelper.MEDIA_ID, PhotoDBHelper.CLARITY, PhotoDBHelper.SIMILIAR_GROUP};
		String selection = PhotoDBHelper.SIMILIAR_GROUP + ">=?";
		String[] selectionArgs = new String[] { "1" };
		try {
			cursor = mPhotoDao.mPhotoDB.query(PhotoDBHelper.TABLE_NAME,
					columns, selection, selectionArgs, null, null,
					PhotoDBHelper.SIMILIAR_GROUP);
		} catch (Exception e) {
		}
		List<List<PhotoModel>> similiarPhotos = null;
		List<PhotoModel> lastGroups = new ArrayList<PhotoModel>();
		if (cursor != null && cursor.getCount() > 0) {
			similiarPhotos = new ArrayList<List<PhotoModel>>();
			int lastGroup = 1;
			while(cursor.moveToNext()) {
				PhotoModel model = new PhotoModel();
				model.filePath = cursor.getString(0);
				model.mediaId = cursor.getLong(1);
				model.clarity = cursor.getDouble(2);
				model.similiarGroup = cursor.getInt(3);
				if (model.similiarGroup > lastGroup) {
					similiarPhotos.add(lastGroups);
					lastGroups = new ArrayList<PhotoModel>();
					lastGroup++;
				}
				lastGroups.add(model);
			}
			similiarPhotos.add(lastGroups);
		}
		similiarGroups = (similiarPhotos == null) ? 0 : similiarPhotos.size();
		return similiarPhotos;
	}

	@Override
	public List<PhotoModel> getBlurPhotos() {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		String[] columns = new String[] {PhotoDBHelper.FILE_PATH, PhotoDBHelper.MEDIA_ID, PhotoDBHelper.CLARITY};
		String selection = PhotoDBHelper.CLARITY + "<=?";
		String[] selectionArgs = new String[] { BlurPhotoUtil.CLARITY_VALUE + "" };
		try {
			cursor = mPhotoDao.mPhotoDB.query(PhotoDBHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		} catch (Exception e) {
		}
		List<PhotoModel> blurPhotos = null;
		if (cursor != null && cursor.getCount() > 0) {
			blurPhotos = new ArrayList<PhotoModel>();
			while(cursor.moveToNext()) {
				PhotoModel model = new PhotoModel();				
				model.filePath = cursor.getString(0);
				model.mediaId = cursor.getLong(1);
				model.clarity = cursor.getDouble(2);
				blurPhotos.add(model);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return blurPhotos;
	}
	
	@Override
	public List<String> getBlurPhotoPaths() {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		String[] columns = new String[] {PhotoDBHelper.FILE_PATH, PhotoDBHelper.CLARITY};
		String selection = PhotoDBHelper.CLARITY + "<=?";
		String[] selectionArgs = new String[] { BlurPhotoUtil.CLARITY_VALUE + "" };
		try {
			cursor = mPhotoDao.mPhotoDB.query(PhotoDBHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		} catch (Exception e) {
		}
		List<String> blurPhotoPaths = null;
		if (cursor != null && cursor.getCount() > 0) {
			blurPhotoPaths = new ArrayList<String>();
			while(cursor.moveToNext()) {
				String path = cursor.getString(0);
				blurPhotoPaths.add(path);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return blurPhotoPaths;
	}
	
	public long getBLurPhotoSize() {
		List<PhotoModel> blurPhotos = getBlurPhotos();
		if (blurPhotos == null || blurPhotos.size() <= 0) {
			return 0;
		}
		long size = 0;
		for (PhotoModel model : blurPhotos) {
			File file = new File(model.filePath);
			if (file != null && file.exists()) {
				size += file.length();
			}
		}
		return size;
	}

	public List<PhotoModel> getTotalPhotos() {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		List<PhotoModel> photoModelList = null;
		String[] columns = new String[] {PhotoDBHelper.FILE_PATH, PhotoDBHelper.COLOR_GRID, PhotoDBHelper.AVG_GRID};
		try {
			cursor = mPhotoDao.mPhotoDB.query(PhotoDBHelper.TABLE_NAME, columns, null, null, null, null, null);
		} catch (Exception e) {
		}
		if (cursor != null && cursor.getCount() > 0) {
			photoModelList = new ArrayList<PhotoModel>();
			while(cursor.moveToNext()) {
				PhotoModel photoModel = new PhotoModel();
				photoModel.filePath = cursor.getString(0);
				photoModel.colorGrid = cursor.getString(1);
				photoModel.avgGrid = cursor.getInt(2);
				photoModelList.add(photoModel);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return photoModelList;
	}
	
	@Override
	public void refreshDB() {
		// TODO Auto-generated method stub
		List<PhotoModel> totalPhotos = getTotalPhotos();
		if (totalPhotos != null && totalPhotos.size() > 0) {
			for (PhotoModel model : totalPhotos) {
				if (model != null && !TextUtils.isEmpty(model.filePath)) {
					File file = new File(model.filePath);
					if (file == null || !file.exists()) {
						mPhotoDao.delete(model.filePath);
					}
				}
			}
		}
	}

	@Override
	public boolean compressPhoto(String filePath) {
		// TODO Auto-generated method stub
		return PhotoCompressUtils.compressPhoto(filePath);
	}
	
}
