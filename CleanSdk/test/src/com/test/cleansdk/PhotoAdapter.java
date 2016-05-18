package com.test.cleansdk;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoAdapter extends BaseAdapter {

	private Context mContext;
	private List<PhotoModel> photoList;
	private LayoutInflater inflater;
	private ImageLoader mLoader;
	
	class Holder {
		ImageView imageView;
		TextView textView;
	}

	public PhotoAdapter(Context context, List<PhotoModel> pictureList, GridView gridView) {
		
		this.mContext = context;
		this.photoList = pictureList;
		this.inflater = LayoutInflater.from(context);
		
		ImageUtil.getInstance(context);
        mLoader = ImageLoader.getLoader();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return (photoList == null) ? 0 : photoList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return (photoList == null) ? null : photoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.blur_pic_item, null);
			holder = new Holder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.picture);
			holder.textView = (TextView) convertView.findViewById(R.id.description);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		final String path = photoList.get(position).filePath;
		long mediaId = photoList.get(position).mediaId;
		mLoader.loadImage(mediaId, holder.imageView);
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (path != null) {
					File file = new File(path);
					if (file != null && file.exists()) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(file), "image/*");
						mContext.startActivity(intent);
					}
				}
			}
		});
		
		return convertView;
	}
}
