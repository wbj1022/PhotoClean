package com.vivo.secureplus.update;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParserUtils {

    public static String getRawString(String name, JSONObject json) {
        try {
            if (json.isNull(name)) {
                return null;
            } else {
                return json.getString(name);
            }
        } catch (JSONException jsone) {
            return null;
        }
    }

    public static JSONArray getJSONArray(String name, JSONObject json) {
        try {
            if (name != null && json != null && json.isNull(name)) {
                return null;
            } else {
                if (json != null) {
                    return json.getJSONArray(name);
                }
            }
        } catch (JSONException jsone) {
            return null;
        }
        return null;
    }

    public static Boolean getBoolean(String name, JSONObject json) {
        try {
            return !json.isNull(name) && json.getBoolean(name);
        } catch (JSONException e) {
            // TODO: handle exception
            return false;
        }

    }

    public static JSONObject getObject(String name, JSONObject json) {
        try {
            if (json.isNull(name)) {
                return null;
            } else {
                return json.getJSONObject(name);
            }
        } catch (JSONException e) {
            // TODO: handle exception
            return null;
        }
    }

    public static int getInt(String name, JSONObject json) {
        return getInt(getRawString(name, json));
    }

    private static int getInt(String str) {
        if (isNull(str)) {
            return 0;
        } else {
            try {
                return Integer.valueOf(str);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
    }

    public static int getInt(JSONObject json, String name) {

        String str = getRawString(name, json);

        if (isNull(str)) {
            return -1;
        } else {
            try {
                return Integer.valueOf(str);
            } catch (NumberFormatException nfe) {
                return -1;
            }
        }
    }

    public static long getLong(String name, JSONObject json) {
        return getLong(getRawString(name, json));
    }

    private static long getLong(String str) {
        if (isNull(str)) {
            return 0;
        } else {
            try {
                return Long.valueOf(str);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
    }

    public static float getFloat(String name, JSONObject json) {
        return getFloat(getRawString(name, json));
    }

    private static float getFloat(String str) {
        if (isNull(str)) {
            return 0.0f;
        } else {
            try {
                return Float.valueOf(str);
            } catch (NumberFormatException nfe) {
                return 0.0f;
            }
        }
    }

    private static boolean isNull(String str) {
        return str == null || TextUtils.isEmpty(str) || "null".equals(str);
    }
}
