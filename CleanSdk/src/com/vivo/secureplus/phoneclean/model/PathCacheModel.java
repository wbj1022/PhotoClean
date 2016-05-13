package com.vivo.secureplus.phoneclean.model;

import java.util.List;

public class PathCacheModel {
    public String mPath;            	//path; false == mRegularType
    public List<String> mPathList;  	//paths; true == mRegularType
    public String mPackageName;    		//package name
    public Byte mCleanType;         	//0:careful 1:advised
    public String mCategory;        	//category
    public String mUsage;           	//usage
    public String mCleanAlert;      	//alert
    public String mJunkType;        	//system cache,software cache
    public String mDescription;     	//description
    public boolean mRegularType;   		//1 means path is regular expression; 0 means path is normal path.
    public Byte mDisplayType;			//1 means grid, 0 means list
    public boolean mEntirety;			//true means cleansdk has the whole data; false means cleansdk has partital data.
}

