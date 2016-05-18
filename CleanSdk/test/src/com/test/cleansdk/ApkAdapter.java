package com.test.cleansdk;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class ApkAdapter extends ArrayAdapter {

	private LayoutInflater inflater;
	private ArrayList<ApkItemModel> mApkModelList;
	private String isInstalled;
	private String notInstalled;
	private String isBroken;
	private String notBroken;

	class ApkItemHolder {
		ImageView icon;
		TextView label;
		TextView des;
	}

	@SuppressWarnings("unchecked")
	public ApkAdapter(Context context, int textViewResourceId,
			ArrayList<ApkItemModel> apkModelList) {
		super(context, textViewResourceId, apkModelList);
		// TODO Auto-generated constructor stub

		inflater = LayoutInflater.from(context);
		mApkModelList = apkModelList;
		isInstalled = context.getString(R.string.is_installed);
		notInstalled = context.getString(R.string.not_installed);
		isBroken = context.getString(R.string.is_broken);
		notBroken = context.getString(R.string.not_broken);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ApkItemHolder holder;
		ApkItemModel apkModel = mApkModelList.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.package_item, null);
			holder = new ApkItemHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.pkg_icon);
			holder.label = (TextView) convertView.findViewById(R.id.pkg_label);
			holder.des = (TextView) convertView.findViewById(R.id.pkg_des);
			convertView.setTag(holder);
		} else {
			holder = (ApkItemHolder) convertView.getTag();
		}
		holder.icon.setBackground(apkModel.icon);
		holder.label.setText(apkModel.label);
		String des;
		if (apkModel.isInstalled) {
			des = isInstalled;
		} else {
			if (apkModel.isBroken) {
				des = notInstalled + " " + isBroken;
			} else {
				des = notInstalled + " " + notBroken;;
			}
		}
		holder.des.setText(des);
		return convertView;
	}

}
