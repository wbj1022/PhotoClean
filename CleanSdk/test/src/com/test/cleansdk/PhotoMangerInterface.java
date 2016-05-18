package com.test.cleansdk;

import java.util.List;

import com.test.cleansdk.model.PhotoModel;

public interface PhotoMangerInterface {
	void refreshDB();

	boolean detectFeature(PhotoModel model);

	void detectFeature(List<PhotoModel> models);

	List<PhotoModel> getBlurPhotos();

	List<String> getBlurPhotoPaths();

	void updateSimiliarInfo();

	List<List<PhotoModel>> getSimiliarPhotos();

	int getSimiliarGroupCount();

	boolean compressPhoto(String filePath);
}
