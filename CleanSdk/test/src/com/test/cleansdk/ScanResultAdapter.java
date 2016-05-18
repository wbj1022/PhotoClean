package com.test.cleansdk;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class ScanResultAdapter extends ArrayAdapter {

	private LayoutInflater inflater;
	private ArrayList<ScanResultModel> mScanResultModel;
	
	class ScanResultItemHolder {
		TextView pkgName;
		TextView scanTime;
		TextView tipFromDatabase;
		TextView resultInfo;
	}
	
	@SuppressWarnings("unchecked")
	public ScanResultAdapter(Context context, int textViewResourceId,
			ArrayList <ScanResultModel> scanResultList) {
		super(context, textViewResourceId, scanResultList);
		// TODO Auto-generated constructor stub
		
		inflater = LayoutInflater.from(context);
		mScanResultModel = scanResultList;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ScanResultItemHolder holder;
		ScanResultModel scanResultModel = mScanResultModel.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.scan_result_item, null);
			holder = new ScanResultItemHolder();
			holder.pkgName = (TextView) convertView.findViewById(R.id.pkg_name);
			holder.scanTime = (TextView) convertView.findViewById(R.id.scan_time);
			holder.tipFromDatabase = (TextView) convertView.findViewById(R.id.tip_from_database);
			holder.resultInfo = (TextView) convertView.findViewById(R.id.result_info);
			convertView.setTag(holder);
		} else {
			holder = (ScanResultItemHolder) convertView.getTag();
		}
		holder.pkgName.setText(scanResultModel.packageName + "/" + scanResultModel.label);
		holder.scanTime.setText(scanResultModel.scanTime + "ms");
		holder.tipFromDatabase.setText(scanResultModel.tipFromDatabase);
		if (scanResultModel.resultInfo != null) {
			holder.resultInfo.setText(scanResultModel.resultInfo);
			holder.resultInfo.setVisibility(View.VISIBLE);
		} else {
			holder.resultInfo.setVisibility(View.GONE);
		}
		return convertView;
		
	}

	
}
