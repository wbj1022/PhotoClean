package com.vivo.secureplus.update;

import android.content.Context;
import android.os.AsyncTask;

import com.vivo.secureplus.LogUtils;

import org.apache.http.params.CoreConnectionPNames;

import java.util.HashMap;

public class CheckNewVersionTask extends
		AsyncTask<RequestInfo, Integer, UpdateAppInfo> {

	private static final String TAG = "CheckNewVersion";
	private static final int DEFAULT_TIMEOUT = 8000;

	private Context mContext = null;
	private boolean mConnectFailed = false;
	private UpdateCallBack mCallBack = null;

	public CheckNewVersionTask(Context context) {
		this(context, null);
	}
	
	public CheckNewVersionTask(Context context, UpdateCallBack callBack) {
		mContext = context;
		mCallBack = callBack;
	}

	@Override
	protected UpdateAppInfo doInBackground(RequestInfo... params) {
	    UpdateAppInfo updateinfo = new UpdateAppInfo();
		final RequestInfo mRequestInfo= params[0];
		HttpConnect httpConnect = null;
		HashMap<Object, Object> clientsetparams = new HashMap<Object, Object>();
		HashMap<String, String> requestsetparams = new HashMap<String, String>();
		requestsetparams.put("Cache-Control", "no-cache");
		clientsetparams.put(CoreConnectionPNames.CONNECTION_TIMEOUT,
				DEFAULT_TIMEOUT);
		clientsetparams.put(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_TIMEOUT);
		try {
			httpConnect = new HttpConnect(mContext, null, null);
			LogUtils.logI("mOldVercode="
					+ mRequestInfo.mOldVercode + ", mPackageName="
					+ mRequestInfo.mPackageName);
			try {
				httpConnect.connect(UpdateUtils.CHECK_UPDATE_URL,
						mRequestInfo.getRequestParams(),
						HttpConnect.CONNECT_TYPE_HTTPCLIENT_GET, 1, null,
						new HttpResponed() {
							@Override
							public void respond(HttpConnect connect,
									Object connId, int connStatus,
									Object response) {
								LogUtils.logI("respond: connStatus="
										+ connStatus + ", response="
										+ response);
								mRequestInfo.setResponse(response);
								mRequestInfo.setResponseStatus(connStatus);
								
								if(connStatus != HttpResponed.CONNECT_SUCCESS) {
									mConnectFailed = true;
								}
							}
						}, clientsetparams, requestsetparams);

			} catch (Exception e) {
				LogUtils.logE(e.getMessage());
			}
            
			if (isCancelled()) {
				return null;
			}
			
			if(mRequestInfo.getResponse() != null) {
				updateinfo = updateinfo.fromResponse(mRequestInfo.getResponse());
				if((updateinfo.version != null 
						&& updateinfo.description != null
						&& updateinfo.size != -1)) {
					updateinfo.mPackageName = mRequestInfo.mPackageName;
				}
			}
			
			

		} catch (Exception e) {
			LogUtils.logE(e.getMessage());
		} finally {
			if (httpConnect != null) {
				httpConnect.disconnect();
			}
		}
		
		return updateinfo;
	}

	@Override
	protected void onPostExecute(UpdateAppInfo result) {
		super.onPostExecute(result);
		if (mCallBack != null) {
			mCallBack.afterCheckNewVersion(result, mConnectFailed, isCancelled());
		}
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		if (mCallBack != null) {
			mCallBack.beforeCheckNewVersion();
		}
	}

}
