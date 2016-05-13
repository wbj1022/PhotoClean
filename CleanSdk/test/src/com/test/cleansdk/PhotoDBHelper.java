package com.test.cleansdk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotoDBHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "photo_detail";
	//public static final String FILE_NAME = "file_name";
	public static final String FILE_PATH = "file_path";
	public static final String MEDIA_ID = "media_id";
	public static final String LAST_MODIFIED = "last_modified";
	//public static final String BLUR = "blur";
	public static final String SIMILIAR_GROUP = "similiar_group";
	public static final String COLOR_GRID = "color_grid";
	public static final String AVG_GRID = "avg_grid";
	public static final String CLARITY = "clarity";

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "photo.db";

	private static final String CREAT_TABLE_SQL = "create table if not exists " + TABLE_NAME 
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ FILE_PATH + " String not null,"
			+ MEDIA_ID + " long not null,"
			+ LAST_MODIFIED + " long not null,"
			//+ BLUR + " double DEFAULT 0,"
			+ CLARITY + " double DEFAULT 0,"
			+ SIMILIAR_GROUP + " integer DEFAULT 0,"
			+ COLOR_GRID + " String  not null,"
			+ AVG_GRID + " integer DEFAULT 0" + ")";
	
	public static PhotoDBHelper mInstance;

	public PhotoDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static PhotoDBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PhotoDBHelper(context);
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREAT_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}
