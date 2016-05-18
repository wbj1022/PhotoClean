package com.vivo.secureplus.update;

import android.text.TextUtils;

import org.json.JSONObject;

public class UpdateAppInfo{
	
    protected static final String UPDATE_STAT = "stat";
    protected static final String UPDATE_SIZE = "size";
    protected static final String UPDATE_MSG = "msg";
    protected static final String UPDATE_MD5 = "md5";
    protected static final String UPDATE_VERSION = "version";
    protected static final String UPDATE_VERCODE = "vercode";
    protected static final String UPDATE_FILENAME = "filename";
    protected static final String UPDATE_ADDTIME = "addTime";
    protected static final String UPDATE_DES = "description";
    protected static final String UPDATE_DURL = "durl";
    protected static final String UPDATE_LEVEL = "level";
    protected static final String UPDATE_LOWMD5 = "lowMd5";
    protected static final String UPDATE_MODE = "mode";
    protected static final String UPDATE_PATCH_MD5 = "patchMd5";
    protected static final String UPDATE_PATCH_SIZE = "patchSize";
    protected static final String UPDATE_PATCH_URL = "patchUrl";
    protected static final String UPDATE_PATCH_FILENAME = "patchFilename";
    protected static final String UPDATE_LOG_SWITCH = "logswitch";
    protected static final String UPDATE_SEND_CONTENT = "sendContent";
    protected static final String UPDATE_SEND_TITLE = "sendTitle";
    

    public String mPackageName = null;
    public int stat = -1;
    // 全量升级包大小
    public int size = -1;
    // 自升级version name
    public String version = null;
    // 自升级version code
    public int vercode = 0;
    // 没用到 未知
    public String msg = null;
    // 全量升级包md5
    public String md5 = null;
    // 没用到 未知
    public String addTime = null;
    // 升级描述
    public String description = null;
    // 全量包下载地址downloadurl
    public String durl = null;
    // 下载文件包名
    public String filename = null;
    // 升级等级：
    public int level = -1;
    // 低版本md5
    public String lowMd5 = null;
    // 差分升级OR全量升级
    public int mode = -1;
    // patch包名字
    public String patchFilename = null;
    // patch包的md5
    public String patchMd5 = null;
    // patch包的大小
    public int patchSize = -1;
    // 增量包patch下载地址
    public String patchUrl = null;
    public int logswitch = -1;
    // 通知栏提示的title
    public String sendTitle;
    // 通知栏提示的content
    public String sendContent;
    

    public UpdateAppInfo fromResponse(Object response) {
    	
        UpdateAppInfo info = null;
        String json = (String)response;
        if (!TextUtils.isEmpty(json)) {

            info = new UpdateAppInfo();

            try {
                JSONObject item = new JSONObject(json);
                info.stat = JsonParserUtils.getInt(UPDATE_STAT, item);
                info.size = JsonParserUtils.getInt(UPDATE_SIZE, item);
                info.msg = JsonParserUtils.getRawString(UPDATE_MSG, item);
                info.md5 = JsonParserUtils.getRawString(UPDATE_MD5, item);
                info.version = JsonParserUtils.getRawString(UPDATE_VERSION, item);
                info.vercode = JsonParserUtils.getInt(UPDATE_VERCODE, item);
                info.filename = JsonParserUtils.getRawString(UPDATE_FILENAME, item);
                info.addTime = JsonParserUtils.getRawString(UPDATE_ADDTIME, item);
                info.description = JsonParserUtils.getRawString(UPDATE_DES, item);
                info.durl = JsonParserUtils.getRawString(UPDATE_DURL, item);
                info.level = JsonParserUtils.getInt(UPDATE_LEVEL, item);
                info.lowMd5 = JsonParserUtils.getRawString(UPDATE_LOWMD5, item);
                info.mode = JsonParserUtils.getInt(UPDATE_MODE, item);
                info.patchMd5 = JsonParserUtils.getRawString(UPDATE_PATCH_MD5, item);
                info.patchSize = JsonParserUtils.getInt(UPDATE_PATCH_SIZE, item);
                info.patchUrl = JsonParserUtils.getRawString(UPDATE_PATCH_URL, item);
                info.patchFilename = JsonParserUtils.getRawString(UPDATE_PATCH_FILENAME, item);
                info.logswitch = JsonParserUtils.getInt(UPDATE_LOG_SWITCH, item);
                info.sendTitle = JsonParserUtils.getRawString(UPDATE_SEND_TITLE, item);
                info.sendContent = JsonParserUtils.getRawString(UPDATE_SEND_CONTENT, item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return info;
    }
    
    @Override
    public String toString() {
        return "UpdateAppInfo [stat=" + stat + ", size=" + size + ", version=" + version + ", vercode=" + vercode
                + ", msg=" + msg + ", md5=" + md5 + ", addTime=" + addTime + ", description=" + description + ", durl="
                + durl + ", filename=" + filename + ", level=" + level + ", lowMd5=" + lowMd5 + ", mode=" + mode
                + ", patchFilename=" + patchFilename + ", patchMd5=" + patchMd5 + ", patchSize=" + patchSize
                + ", patchUrl=" + patchUrl + ", logswitch=" + logswitch + ", sendTitle=" + sendTitle + ", sendContent="
                + sendContent + "]";
    }
}
