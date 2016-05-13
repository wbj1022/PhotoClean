package com.test.cleansdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoCleanActivity extends Activity {

	private static final int MSG_NO_PICTURE = 0;
	private static final int MSG_DETECTING = 1;
	private static final int MSG_BLUR_SIZE = 2;
	private static final int MSG_SIMILIAR_GROUPS = 3;
	private Context mContext;
	private RelativeLayout progressView;
	private ImageView progressImageView;
	private TextView progressTextView;
	private RelativeLayout blurPictureView;
	private TextView blurSize;
	private RelativeLayout similiarPictureView;
	private TextView similiarSize;
	private RelativeLayout compressPictureView;
	private TextView compressSize;
	private HandlerThread mHandlerThread;
	private Handler mRunnableHandler;
	private PhotoManagerImpl mPhotoManagerImpl;
	private int mScreenWidth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.picture_clean_activity);
		mContext = this; 
		initValues();
		startDetectPictures();
	}
	
	private void initValues() {
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mPhotoManagerImpl = PhotoManagerImpl.getInstance(mContext.getApplicationContext());
		progressView = (RelativeLayout) findViewById(R.id.progress);
		progressImageView = (ImageView) findViewById(R.id.progress_background);
		progressTextView = (TextView) findViewById(R.id.progress_text);
		
		blurSize = (TextView) findViewById(R.id.blur_size);
		blurPictureView = (RelativeLayout) findViewById(R.id.blur_pictures);
		blurPictureView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, PhotoClassifyActivity.class);
				intent.putExtra("type", "blur");
				startActivity(intent);
			}
		});
		
		similiarSize = (TextView) findViewById(R.id.similiar_size);
		similiarPictureView = (RelativeLayout) findViewById(R.id.similiar_pictures);
		similiarPictureView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, PhotoClassifyActivity.class);
				intent.putExtra("type", "similiar");
				startActivity(intent);
			}
		});
		
		compressSize = (TextView) findViewById(R.id.compress_size);
		compressPictureView = (RelativeLayout) findViewById(R.id.compress_pictures);
		compressPictureView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, PhotoClassifyActivity.class);
				intent.putExtra("type", "compress");
				startActivity(intent);
			}
		});
	}
	
	private void startDetectPictures() {
		mHandlerThread = new HandlerThread("detect_pictures");
		mHandlerThread.start();
		mRunnableHandler = new Handler(mHandlerThread.getLooper());
		mRunnableHandler.post(mScanningRunnable);
	}
	
	private void setScanProgress(int percent) {
		LayoutParams lp = (LayoutParams) progressImageView.getLayoutParams();
		lp.width = (int) (mScreenWidth * percent / 100);
		progressImageView.setLayoutParams(lp);
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NO_PICTURE:
				String message1 = (String) msg.obj;
				progressTextView.setText(message1);
				break;
			case MSG_DETECTING:
				String message2 = (String) msg.obj;
				int percent = msg.arg1;
				progressView.setVisibility(View.VISIBLE);
				setScanProgress(percent);
				progressTextView.setText(message2);
				break;
			case MSG_BLUR_SIZE:
				progressView.setVisibility(View.GONE);
				String blurSizeStr = (String) msg.obj;
				blurSize.setText(blurSizeStr);
			case MSG_SIMILIAR_GROUPS:
				progressView.setVisibility(View.GONE);
				int similiarGroups = msg.arg1;
				similiarSize.setText(getString(R.string.similiar_groups, similiarGroups));
			default:
				super.handleMessage(msg);
			}

		}

	};
	
	private Runnable mScanningRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mPhotoManagerImpl.refreshDB();
			Message msg;
			Cursor cursor = ImageUtil.getCameraPhotoCursor(mContext);
			if (cursor == null || cursor.getCount() <= 0) {
				msg = mHandler.obtainMessage(MSG_NO_PICTURE);
				msg.obj = getString(R.string.no_picture);
				mHandler.sendMessage(msg);
				return;
			}
			int index = 0;
			int total = cursor.getCount();
			while (cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
				long mediaId = cursor.getLong(0);
				long lastModified = cursor.getLong(cursor.getColumnIndex(MediaColumns.DATE_MODIFIED));
				PhotoModel model = new PhotoModel();
				model.filePath = path;
				model.mediaId = mediaId;
				model.lastModified = lastModified;
				index++;
				msg = mHandler.obtainMessage(MSG_DETECTING);
				msg.obj = getString(R.string.detecting) + path;
				msg.arg1 = 100 * index / total;
				mHandler.sendMessage(msg);
				mPhotoManagerImpl.detectFeature(model);
			}
			long blurSize = mPhotoManagerImpl.getBLurPhotoSize();
			String blurSizeStr = FileUtil.getStringForSize(blurSize);
			msg = mHandler.obtainMessage(MSG_BLUR_SIZE);
			msg.obj = blurSizeStr;
			mHandler.sendMessage(msg);
			mPhotoManagerImpl.updateSimiliarInfo();
			int similiarGroups = mPhotoManagerImpl.getSimiliarGroupCount();
			msg = mHandler.obtainMessage(MSG_SIMILIAR_GROUPS);
			msg.arg1 = similiarGroups;
			mHandler.sendMessage(msg);
		}
		
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
