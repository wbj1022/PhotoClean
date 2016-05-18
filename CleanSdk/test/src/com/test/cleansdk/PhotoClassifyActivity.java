package com.test.cleansdk;

import java.util.ArrayList;
import java.util.List;

import com.test.cleansdk.model.PhotoModel;
import com.test.cleansdk.utils.ImageUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;

public class PhotoClassifyActivity extends Activity {

	private Context mContext;
	private GridView mGridView;
	private Button cleanButton;
	private PhotoAdapter mPictureAdapter;
	private PhotoManagerImpl mPhotoManagerImpl;
	private String type;
	private List<PhotoModel> photoList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.blur_picture_activity);
		mContext = this;
		initValues();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private void initValues() {
		mPhotoManagerImpl = PhotoManagerImpl.getInstance(mContext.getApplicationContext());
		mGridView = (GridView) findViewById(R.id.blur_picture);
		Intent intent = getIntent();
		type = intent.getExtras().getString("type");
		if ("blur".equals(type)) {
			photoList = mPhotoManagerImpl.getBlurPhotos();
			mPictureAdapter = new PhotoAdapter(mContext, photoList, mGridView);
			mGridView.setAdapter(mPictureAdapter);
		} else if ("similiar".equals(type)) {
			List<List<PhotoModel>> similiarPictureModel = mPhotoManagerImpl.getSimiliarPhotos();
			photoList = getPhotoFromCategory(similiarPictureModel);
			mPictureAdapter = new PhotoAdapter(mContext, photoList, mGridView);
			mGridView.setAdapter(mPictureAdapter);
		} else if ("compress".equals(type)) {
			photoList = ImageUtil.getCameraPhoto(mContext);
			mPictureAdapter = new PhotoAdapter(mContext, photoList, mGridView);
			mGridView.setAdapter(mPictureAdapter);
		}
		cleanButton = (Button) findViewById(R.id.clean_button);
		cleanButton.setOnClickListener(listener);
	}

	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if ("compress".equals(type) && photoList != null && photoList.size() > 0) {
				for (PhotoModel model : photoList) {
					boolean success = mPhotoManagerImpl.compressPhoto(model.filePath);
					Log.v("franco", model.filePath + "   compress " + success);
				}
			}
		}
	};
	
	private List<PhotoModel> getPhotoFromCategory(List<List<PhotoModel>> similiarPictureModel) {
		if (similiarPictureModel == null || similiarPictureModel.size() <= 0) {
			return null;
		}
		List<PhotoModel> result = new ArrayList<PhotoModel>();		
		for (List<PhotoModel>list : similiarPictureModel) {
			for (PhotoModel model : list) {
				result.add(model);
			}
			int size = list.size();
			if (size % 4 == 1) {
				PhotoModel model1 = new PhotoModel();
				result.add(model1);
				
				PhotoModel model2 = new PhotoModel();
				result.add(model2);
				
				PhotoModel model3 = new PhotoModel();
				result.add(model3);
			} else if (size % 4 == 2) {
				PhotoModel model1 = new PhotoModel();
				result.add(model1);
				
				PhotoModel model2 = new PhotoModel();
				result.add(model2);
			} else if (size % 4 == 3) {
				PhotoModel model1 = new PhotoModel();
				result.add(model1);
			}
		}
		return result;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}