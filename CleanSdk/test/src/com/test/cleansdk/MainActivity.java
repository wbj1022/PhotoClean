package com.test.cleansdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.test.cleansdk.model.ApkItemModel;
import com.test.cleansdk.model.ScanResultModel;
import com.test.cleansdk.utils.FileUtil;
import com.vivo.secureplus.ManagerCreatorF;
import com.vivo.secureplus.phoneclean.CleanDataOp;
import com.vivo.secureplus.phoneclean.PhoneCleanManager;
import com.vivo.secureplus.phoneclean.model.PathCacheModel;
import com.vivo.secureplus.phoneclean.utils.FileUtils;
import com.vivo.secureplus.SecurePlus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

	private static final String TAG = "Cleansdk";
	
	private static final int BTN_SCAN_CACHE = 0;
	private static final int BTN_SCAN_PACKAGE = 1;
	private static final int BTN_SCAN_RESDUAL = 2;
	private static final int BTN_SCAN_PICTURE = 3;
	
	private static final int MSG_SCAN = 0;
	private static final int MSG_SCAN_CACHE_DONE = 1;
	private static final int MSG_SCAN_PACKAGE_DONE = 2;
	
	private static final int SCAN_STATE_CACHE = 0;
	private static final int SCAN_STATE_PACKAGE = 1;
	private static final int SCAN_STATE_RESDUAL = 2;
	
	private int scanState = -1;

	private Context mContext;
	private PhoneCleanManager mPCleanM;
	private CleanDataOp mCleanDataOp;
	private TextView txtScanningForPackage;
	private ListView mListView;
	private GridView mButtonsGridView;
	private ScanResultAdapter mScanResultAdapter;
	private ApkAdapter mApkAdapter;
	private PackageManager mPm;
	private SimpleDateFormat formatter;
	private Handler mRunnableHandler;
	private HandlerThread mHandlerThread;
	private List<ApplicationInfo> applicationInfos;
	private ArrayList<ScanResultModel> mScanResultModelList;
	private ArrayList<ApkItemModel> mApkModelList;
	private String hasNoInfoInDatabase;
	private String totalSizeStr;
	private Drawable defaultIcon;
	private int totalInstalledApk;
	private int totalBrokenApk;

	private int[] textArray = new int[] { R.string.cache_scan, R.string.package_scan, 
			R.string.residual_scan, R.string.picture_scan };
	

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int size = 0;
			
			switch (msg.what) {
			case MSG_SCAN:
				String label = (String) msg.obj;
				txtScanningForPackage.setText(label);
				break;
			case MSG_SCAN_CACHE_DONE:
				String scanDoneInfo = (String) msg.obj;
				txtScanningForPackage.setText(scanDoneInfo);
				mScanResultAdapter = new ScanResultAdapter(mContext,
						R.layout.scan_result_item, mScanResultModelList);
				mListView.setAdapter(mScanResultAdapter);
				mListView.setVisibility(View.VISIBLE);
				break;
				
			case MSG_SCAN_PACKAGE_DONE:
				ArrayList<ApkItemModel> apkModelList = (ArrayList<ApkItemModel>) msg.obj;
				size = (apkModelList == null) ? 0 : apkModelList.size();
				if (size > 0) {
					txtScanningForPackage.setText(getString(R.string.total_apk_num, size, totalInstalledApk, totalBrokenApk));
				} else {
					txtScanningForPackage.setText(getString(R.string.no_apk));
				}
				mApkAdapter = new ApkAdapter(mContext,
						R.layout.package_item, apkModelList);
				mListView.setAdapter(mApkAdapter);
				mListView.setVisibility(View.VISIBLE);
				break;
				
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	private Runnable mScanningRunnable = new Runnable() {

		Message msg;
		@Override
		public void run() {
			
			switch (scanState) {
			case SCAN_STATE_CACHE:
				if (mScanResultModelList != null) {
					mScanResultModelList.clear();
				}
				List<PathCacheModel> cacheList;
				int validApkNums = 0;
				int totalValidPathsNums = 0;
				long totalScanTime = 0;
				StringBuilder finalScanInfo = new StringBuilder();
				for (ApplicationInfo info : applicationInfos) {
					ScanResultModel scanResult = new ScanResultModel();
					int realityPathsNum = 0;
					long totalSize = 0;
					scanResult.packageName = info.packageName;
					scanResult.label = info.loadLabel(mPm);
					Log.i(TAG, "Package : " + scanResult.packageName + "/" + scanResult.label);
					finalScanInfo.append("Package : ").append(scanResult.packageName).append("/").append(scanResult.label).append("\n");
					long timeStart = System.currentTimeMillis();
					if (info != null && info.packageName != null) {
						cacheList = mPCleanM.scanPackageSoftCache(info.packageName);
						long timeFinish = System.currentTimeMillis();

						scanResult.scanTime = timeFinish - timeStart;
						scanResult.totalPath = getTotalPathForPackage(info.packageName);
						Log.i(TAG, "ScanTime : " + scanResult.scanTime + "ms");
						finalScanInfo.append("ScanTime : ").append(scanResult.scanTime).append("ms").append("\n");
						totalScanTime += scanResult.scanTime;

						if (cacheList != null && cacheList.size() > 0) {
							validApkNums++;
							scanResult.validPathNum = scanResult.totalPath
									- cacheList.size();
							StringBuilder build = new StringBuilder();
							int i = 0;
							for (PathCacheModel pcm : cacheList) {
								if (pcm != null) {
									i++;
									build.append(i + ". ").append("info : ")
											.append("mCleanType = ").append(pcm.mCleanType)
											.append(", mCategory = ").append(pcm.mCategory)
											.append(", mUsage = ").append(pcm.mUsage).append(", mCleanAlert = ")
											.append(pcm.mCleanAlert).append(" mJunkType = ")
											.append(pcm.mJunkType).append(", mDescription = ")
											.append(pcm.mDescription).append(", mRegularType = ")
											.append(pcm.mRegularType).append(", mDisplayType = ")
											.append(pcm.mDisplayType).append(", mEntirety = ")
											.append(pcm.mEntirety).append("\n").append("\n");

									if (pcm.mPath != null) {
										totalValidPathsNums++;
										realityPathsNum++;
										long dirSize = FileUtil.getFileSize(new File(pcm.mPath));
										String dirSizeWithUint = FileUtil.getStringForSize(dirSize);
										totalSize += dirSize;
										build.append("mPath = ").append(pcm.mPath).append("\n");
										build.append("mSize = ").append(dirSizeWithUint).append("\n");
									}
									List<String> pathList = pcm.mPathList;
									int j = 0;
									if (pathList != null && pathList.size() > 0) {
										totalValidPathsNums += pathList.size();
										realityPathsNum += pathList.size();
										for (String path : pathList) {
											j++;
											long dirSize = FileUtil.getFileSize(new File(path));
											String dirSizeWithUint = FileUtil.getStringForSize(dirSize);
											totalSize += dirSize;
											build.append("mPath ").append(j).append(" = ").append(path).append("\n");
											build.append("mSize ").append(j).append(" = ").append(dirSizeWithUint).append("\n").append("\n");
										}
									}
								}
								build.append("\n");
							}
							scanResult.realityPathNum = realityPathsNum;
							scanResult.tipFromDatabase = getString(R.string.tip_path_details, scanResult.totalPath, cacheList.size(), realityPathsNum);
							scanResult.totalSize = totalSize;
							StringBuilder build2 = new StringBuilder();
							build2.append(totalSizeStr).append(FileUtil.getStringForSize(totalSize)).append("\n").append("\n");
							scanResult.resultInfo = build2.append(build).toString();
							finalScanInfo.append("Tip : ").append(scanResult.tipFromDatabase).append("\n").append(build2).append("\n");
							Log.i(TAG, "Tip : " + scanResult.tipFromDatabase);
							Log.i(TAG, scanResult.resultInfo);
						} else {
							if (scanResult.totalPath > 0) {
								scanResult.tipFromDatabase = getString(R.string.tip_has_database_info, scanResult.totalPath);
							} else {
								scanResult.tipFromDatabase = hasNoInfoInDatabase;
							}
							finalScanInfo.append("Tip : ").append(scanResult.tipFromDatabase).append("\n");
							Log.i(TAG, "Tip : " + scanResult.tipFromDatabase);
						}
						mScanResultModelList.add(scanResult);
						finalScanInfo.append("\n").append("--------------------------------").append("\n");
						Log.i(TAG, "---------------------------------------------------------------------------------------");
						msg = mHandler.obtainMessage(MSG_SCAN);
						msg.obj = getString(R.string.scanning_for_package) + info.packageName;
						mHandler.sendMessage(msg);
					}
				}
				FileUtil.writeFileSdcard(finalScanInfo.toString());
				int totalNum = (applicationInfos == null) ? 0 : applicationInfos.size();
				long endTime = System.currentTimeMillis();
				Message msg = mHandler.obtainMessage(MSG_SCAN_CACHE_DONE);
				msg.obj = getString(R.string.scan_done_info, totalNum, (int) totalScanTime, validApkNums, totalValidPathsNums);
				mHandler.sendMessage(msg);
				break;
			
			case SCAN_STATE_PACKAGE:
				String section = "(" + FileColumns.DATA + " LIKE '%.apk') AND (" + FileColumns.DATA
                        + " NOT LIKE '%/.%')";
				String[] columns = new String[] { FileColumns._ID, FileColumns.DATA, FileColumns.SIZE };
				PackageInfo packageInfo = null;
				ApkItemModel model = null;
				ApplicationInfo applicationInfo = null;
				AssetManager assMgr = null;
				Cursor cursor = null;
				try {
					cursor = MainActivity.this.getContentResolver().query(Files.getContentUri("external"), columns,
							section, null, null);
				} catch (Exception e) {
				}
				
				while (cursor.moveToNext()) {
					model = new ApkItemModel();
					model.path = cursor.getString(1);
					msg = mHandler.obtainMessage(MSG_SCAN);
					msg.obj = getString(R.string.scanning_for_package) + model.path;
					mHandler.sendMessage(msg);
					model.isInstalled = FileUtils.isPackageInstalled(model.path);
					model.isBroken = FileUtils.isPackageBroken(model.path);
					if (model.isInstalled) {
						totalInstalledApk++;
					}
					if (model.isBroken) {
						totalBrokenApk++;
					}
					try {
						packageInfo = mPm.getPackageArchiveInfo(model.path, 0);
						applicationInfo = packageInfo.applicationInfo;
						assMgr = new AssetManager();
						assMgr.addAssetPath(model.path);
						Resources res = new Resources(assMgr, null, null);
						if (applicationInfo != null && applicationInfo.labelRes != 0) {
							model.label = res.getText(applicationInfo.labelRes);
						}
						model.icon = res.getDrawable(applicationInfo.icon);
					} catch (Exception e) {
					
					}
					if (model.icon == null) {
						model.icon = defaultIcon;
					}
					if (model.label == null) {
						model.label = model.path;
					}
					mApkModelList.add(model);
				}
				if (cursor != null) {
					cursor.close();
				}

				msg = mHandler.obtainMessage(MSG_SCAN_PACKAGE_DONE);
				msg.obj = mApkModelList;
				mHandler.sendMessage(msg);
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this;
		initViews();
		initGlobalValues();
	}

	private void setScanState(int state) {
		scanState = state;
	}
	
	private void initViews() {
		mButtonsGridView = (GridView) findViewById(R.id.buttons);
		txtScanningForPackage = (TextView) findViewById(R.id.txt_scanning_for_package);
		mListView = (ListView) findViewById(R.id.listView);
		
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0, totalItem = textArray.length; i < totalItem; i++) {
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put("text", getString(textArray[i]));
			data.add(hashMap);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data,
				R.layout.grid_view_item, new String[] {"text"},
				new int[] { R.id.text });
		mButtonsGridView.setAdapter(adapter);
		mButtonsGridView.setOnItemClickListener(new ItemClickListener());
	}

	private void initGlobalValues() {
		mPm = mContext.getPackageManager();
		formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss.SSS");
		applicationInfos = mPm.getInstalledApplications(0);
		mScanResultModelList = new ArrayList<ScanResultModel>();
		mApkModelList = new ArrayList<ApkItemModel>();
		SecurePlus.init(this, true);
		mPCleanM = ManagerCreatorF.getManager(PhoneCleanManager.class);
		mCleanDataOp = new CleanDataOp();
		hasNoInfoInDatabase = getString(R.string.tip_has_no_database_info);
		totalSizeStr = getString(R.string.total_size);
		defaultIcon = mPm.getDefaultActivityIcon();
	}
	
	class ItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			
			totalInstalledApk = 0;
			totalBrokenApk = 0;
			mListView.setVisibility(View.GONE);
			if (mScanResultAdapter != null) {
				mScanResultAdapter.clear();
			}
			switch (position) {
			case BTN_SCAN_CACHE:
				setScanState(SCAN_STATE_CACHE);
				mHandlerThread = new HandlerThread("scan_cache");
				mHandlerThread.start();
				mRunnableHandler = new Handler(mHandlerThread.getLooper());
				mRunnableHandler.post(mScanningRunnable);
				break;
				
			case BTN_SCAN_PACKAGE:
				setScanState(SCAN_STATE_PACKAGE);
				mHandlerThread = new HandlerThread("scan_package");
				mHandlerThread.start();
				mRunnableHandler = new Handler(mHandlerThread.getLooper());
				mRunnableHandler.post(mScanningRunnable);
				break;
				
			case BTN_SCAN_RESDUAL:
				mPCleanM.getUninstalledRubbish();
				break;
				
			case BTN_SCAN_PICTURE:
				startActivity(new Intent(mContext, PhotoCleanActivity.class));

			default:
				break;
			}
		}
	}

	private int getTotalPathForPackage(String pkgName) {
		Cursor cursor = null;
		int result = 0;
		try {
			cursor = mCleanDataOp.queryPkg(pkgName);
			if (cursor != null) {
				result = cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRunnableHandler != null) {
			mRunnableHandler.removeCallbacks(mScanningRunnable);
			Looper looper = mRunnableHandler.getLooper();
			if (looper != null) {
				looper.quit();
			}
		}
		if (mHandler != null) {
			mHandler.removeMessages(MSG_SCAN_CACHE_DONE);
			mHandler.removeMessages(MSG_SCAN_PACKAGE_DONE);
			mHandler.removeMessages(MSG_SCAN);
		}
	}

}
