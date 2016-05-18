package com.vivo.secureplus.update;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;

import com.vivo.secureplus.LogUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpConnect {
    private static final String TAG = "HttpConnect";

    private Context mContext = null;

    private Object mConnectionId = null;

    private HttpResponed mResponedCallback = null;

    private String mUrlAddress = null;

    private HashMap<String, String> mRequestParams = null;

    private HashMap<Object, Object> mHttpClientSetParams = null;

    private HashMap<String, String> mRequestSetParams = null;

    private HttpClient mHttpClient = null;

    private HttpHost mHttpHost = null;

    private int mConnectType = -1;

    private int mTryNum = 0;

    private String mProxyIP = null;

    private String mUserAgent = null;

    private int mRetryTryNum = 0;

    private boolean mDisconnected = false;

    /** The default user agent used for downloads */
    public static final String DEFAULT_USER_AGENT = "IQooAppstore";

    public static final int CONNECT_TYPE_HTTPCLIENT_GET = 0;

    public static final int CONNECT_TYPE_HTTPCLIENT_POST = 1;

    private static final int CONNECT_TYPE_HTTPCONNECTION_GET = 2;

    private static final int CONNECT_TYPE_HTTPCONNECTION_POST = 3;

    public static final int CONNECT_TYPE_HTTPCLIENT_HEAD = 4;

    public static final int RETURN_TYPE_IS_RESPONSE = 1;

    public static final int RETURN_TYPE_DEFAULT_IS_STRING = 0;

    private int mReturnType = 0;

    public HttpConnect(Context context, String userAgent, String proxyIp) {
        mContext = context;

        if(userAgent == null){
            mUserAgent = DEFAULT_USER_AGENT;
        }else{
            mUserAgent = userAgent;
        }

        mProxyIP = proxyIp;

        mDisconnected = false;
        //mHttpClient = getHttpClient();
        mHttpClient = AndroidHttpClient.newInstance(mUserAgent, mContext);//getHttpClient();
    }

    public HttpConnect(Context context, String userAgent, String proxyIp, HttpHost httphost) {
        mContext = context;

        if(userAgent == null){
            mUserAgent = DEFAULT_USER_AGENT;
        }else{
            mUserAgent = userAgent;
        }

        mProxyIP = proxyIp;

        mDisconnected = false;
        //mHttpClient = getHttpClient();
        mHttpClient = AndroidHttpClient.newInstance(mUserAgent, mContext);//getHttpClient();
        mHttpHost = httphost;
    }

    public void connectAgain(){
        connect(mUrlAddress, mRequestParams, mConnectType, mRetryTryNum, mConnectionId, mResponedCallback);
    }

    /**
     * 
     * @param url
     * @param params  requestparams
     * @param connectType 
     * @param tryNum    
     * @param connId    
     * @param httpResponed 
     * @param httpclientsetparams 
     * @param requestsetparams    
     * @param returnValues   
     */
    public void connect(String url, HashMap<String, String> params,
            int connectType, int tryNum, Object connId,
            HttpResponed httpResponed) {
        this.connect(url, params, connectType, tryNum, connId, httpResponed, 
                null, null);
    }

    /**
     * 
     * @param url
     * @param params  requestparams
     * @param connectType 
     * @param tryNum    
     * @param connId    
     * @param httpResponed  
     * @param httpclientsetparams    
     * @param requestsetparams    
     */
    public void connect(String url, HashMap<String, String> params,
            int connectType, int tryNum, Object connId,
            HttpResponed httpResponed,
            HashMap<Object, Object> httpclientsetparams,
            HashMap<String, String> requestsetparams
            ) {
        this.connect(url, params, connectType, tryNum, connId, httpResponed, 
                httpclientsetparams, requestsetparams, RETURN_TYPE_DEFAULT_IS_STRING);
    }

    public void connect(String url, HashMap<String, String> params, int connectType, int tryNum,
            Object connId, HttpResponed httpResponed,HashMap<Object, Object> httpclientsetparams,
            HashMap<String, String> requestsetparams, int returnValues) {
        mResponedCallback = httpResponed;
        mConnectionId = connId;
        mUrlAddress = url;
        mRequestParams = params;
        mTryNum = tryNum;
        mConnectType = connectType;
        mHttpClientSetParams = httpclientsetparams;
        mRequestSetParams = requestsetparams;
        mReturnType = returnValues;

        LogUtils.logD("connect(): mUrlAddress=" + mUrlAddress + ", mTryNum = " + mTryNum +
                ", proxy ip = " + mProxyIP +
                ", connect type  = " + mConnectType);

        switch (connectType) {
            case CONNECT_TYPE_HTTPCLIENT_GET:
            case CONNECT_TYPE_HTTPCLIENT_POST:
            case CONNECT_TYPE_HTTPCLIENT_HEAD:
                doHttpClient(mUrlAddress, mRequestParams, connectType);
                break;
            case CONNECT_TYPE_HTTPCONNECTION_GET:
            case CONNECT_TYPE_HTTPCONNECTION_POST:
                doHttpConnection(mUrlAddress, mRequestParams, connectType);
                break;
            default:
            	LogUtils.logE("connect, unsupport connect type: " + connectType);
        }
    }

    public void disconnect(){
        if(mHttpClient != null){
            if(mHttpClient instanceof DefaultHttpClient){
                mHttpClient.getConnectionManager().shutdown();
            }else if(mHttpClient instanceof AndroidHttpClient){
                ((AndroidHttpClient)mHttpClient).close();
            }
        }
        mDisconnected = true;
        mHttpClient = null;
    }

    private void doHttpClient(String url, HashMap<String, String> params, int method) {    	
        HttpRequestBase httpRequest = null;
        String newUrl = url;
        if (method == CONNECT_TYPE_HTTPCLIENT_GET) {
            if (params != null && params.size() > 0) {
                String paramStr = "";
                try {
                    Iterator<Entry<String, String>> iter = params.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
                        key = URLEncoder.encode(key, "UTF-8");
                        String val = entry.getValue();
                        if(val != null)
                            val = URLEncoder.encode(val, "UTF-8");
                        paramStr += paramStr = "&" + key + "=" + val;
                    }
                } catch (UnsupportedEncodingException e) {
                    /*
                     *exception throwed by URLEncoder.encode()
                     *-if the specified encoding scheme is invalid.
                     */
                    e.printStackTrace();
                    if(mResponedCallback != null){
                        mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CLIENT_ERROR, null);
                    }
                    return;
                }
                if (!paramStr.equals("")) {
                    paramStr = paramStr.replaceFirst("&", "?");
                    newUrl += paramStr;
                }
            }

            LogUtils.logD("doGet the url after encode is " + newUrl);

            try {
                LogUtils.logI("request the url:"+newUrl);
                httpRequest = new HttpGet(newUrl);
                if (mRequestSetParams != null && mRequestSetParams.size() > 0) {
                    Iterator<Entry<String, String>> iter = mRequestSetParams.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
                        String val = entry.getValue();
                        LogUtils.logD("key="+key+",    val="+val);
                        httpRequest.addHeader(key, val);
                    }
                }
            } catch (IllegalArgumentException e) {
                /* throwed by new HttpGet();
                 * if the uri is invalid. 
                 */
                e.printStackTrace();
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CLIENT_ERROR, null);
                }
                return;
            }
        } else if (method == CONNECT_TYPE_HTTPCLIENT_POST) {

            List<NameValuePair> httpParams = null;
            if(params != null){
                httpParams = new ArrayList<NameValuePair>();
                Iterator<Entry<String, String>> iter = params.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String key = entry.getKey();
                    String val = entry.getValue();
                    httpParams.add(new BasicNameValuePair(key, val));
                }
            }


            try {
                httpRequest = new HttpPost(newUrl);
                if (mRequestSetParams != null && mRequestSetParams.size() > 0) {
                    Iterator<Entry<String, String>> iter = mRequestSetParams.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
                        String val = entry.getValue();
                        LogUtils.logD("key="+key+",    val="+val);
                        httpRequest.addHeader(key, val);
                    }
                }
            } catch (IllegalArgumentException e) {
                /*
                 * throwed by new HttpPost(); if the uri is invalid.
                 */
                e.printStackTrace();
                if (mResponedCallback != null) {
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CLIENT_ERROR, null);
                }
                return;
            }

            try {
                if(httpParams != null){
                    ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));
                }
            } catch (UnsupportedEncodingException e) {
                /*
                 *exception throwed by URLEncoder.encode()
                 *-if the specified encoding scheme is invalid.
                 */
                e.printStackTrace();
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CLIENT_ERROR, null);
                }
                return;
            }
        } else if (method == CONNECT_TYPE_HTTPCLIENT_HEAD) {//add by wangzhongyi
            if (params != null && params.size() > 0) {
                Iterator<Entry<String, String>> iter = params.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String key = entry.getKey();
                    String val = entry.getValue();
                }
            }
            try {
                httpRequest = new HttpHead(newUrl);
                if (mRequestSetParams != null && mRequestSetParams.size() > 0) {
                    Iterator<Entry<String, String>> iter = mRequestSetParams.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
                        String val = entry.getValue();
                        httpRequest.addHeader(key, val);
                    }
                }
            } catch (IllegalArgumentException e) {
                /*
                 * throwed by new HttpPost(); if the uri is invalid.
                 */
                e.printStackTrace();
                if (mResponedCallback != null) {
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CLIENT_ERROR, null);
                }
                return;
            }
        } else {
            LogUtils.logE("doHttpClient, unspport connect type: " + method);
            return;
        }

        HttpClient httpClient = mHttpClient;
        if(mDisconnected || httpClient == null){
            if(mResponedCallback != null){
                mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CONNECTION_DISCONNECTION, null);
            }
            return;
        }
        if (mHttpHost != null) {
            ConnRouteParams.setDefaultProxy(httpClient.getParams(), mHttpHost);
        }

        if (mHttpClientSetParams != null && mHttpClientSetParams.size() > 0) {
            Iterator<Entry<Object, Object>> iter = mHttpClientSetParams.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Object, Object> entry = iter.next();
                String key = (String)entry.getKey();
                Object val = entry.getValue();
                httpClient.getParams().setParameter(key, val);
            }
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
        if(networkinfo == null){
            if(mResponedCallback != null){
                mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NETWORK_ERROR, null);
                LogUtils.logI("connect failed");
            }
            return;
        }
        if ((networkinfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
             String host = android.net.Proxy.getDefaultHost();
             int port = android.net.Proxy.getDefaultPort();  
             if (host != null && port != -1) {
                 LogUtils.logD("Proxy host: is " + host + " port is: " + port);

                 HttpHost proxy = new HttpHost(host, port, "http"); 
                 httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                 HttpClientParams.setRedirecting(httpClient.getParams(), true);
             }
        }

        InputStream inputStream = null;
        HttpResponse httpResponse = null;
        String inputSteamString = null;
        try {

            httpResponse = httpClient.execute(httpRequest);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            httpRequest.abort();
            LogUtils.logE("doHttpClientConnect, can't connect because ClientProtocolException");
            if(mResponedCallback != null){
                mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_CLIENT_ERROR, null);
            }
            return;
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
            httpRequest.abort();
            LogUtils.logE("doHttpClientConnect, can't connect because IllegalArgumentException");
            if(mResponedCallback != null){
                mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NETWORK_ERROR, null);
            }
            return;
        }catch (IOException e) {
            LogUtils.logE("doHttpClientConnect, get a IOException when connect to server"); 
            e.printStackTrace();
            httpRequest.abort();
            if (mTryNum >= 1){
                mTryNum --;
                doHttpClient(url, params, method);
                return;
            }else{
                LogUtils.logE("doHttpClientConnect, can't connect because IOException");
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NETWORK_ERROR, null);
                }
                return;
            }
        }catch(IllegalStateException e){
            LogUtils.logE("doHttpClientConnect, get a IllegalStateException when connect to server"); 
            e.printStackTrace();
            httpRequest.abort();
            if (mTryNum >= 1){
                mTryNum --;
                doHttpClient(url, params, method);
                return;
            }else{
                LogUtils.logE("doHttpClientConnect, can't connect because IllegalStateException");
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NETWORK_ERROR, null);
                }
                return;
            }
        }catch(NullPointerException e){
            LogUtils.logE("doHttpClientConnect, get a NullPointerException when connect to server");
            e.printStackTrace();
            httpRequest.abort();
            if (mTryNum >= 1){
                mTryNum --;
                doHttpClient(url, params, method);
                return;
            }else{
                LogUtils.logE("doHttpClientConnect, can't connect because NullPointerException");
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NETWORK_ERROR, null);
                }
                return;
            }
        }

        if(httpResponse == null){
            httpRequest.abort();
            LogUtils.logE("doHttpClientConnect, got httpRespones is null");            
            if (mTryNum >= 1){
                mTryNum --;
                doHttpClient(url, params, method);
                return;
            }else{
                LogUtils.logE("can not connect to server because get HttpRespones always null");
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NO_RESPONSE, null);
                }
                return;
            }
        }

        if (mReturnType == 1) {
            if(mResponedCallback != null){
                mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_SUCCESS, httpResponse);
            }
            httpRequest.abort();
            return;
        }

        StatusLine statusLine =  httpResponse.getStatusLine();
        if(statusLine == null){
            httpRequest.abort();
            LogUtils.logE("doHttpClientConnect, get status line is null");
            if (mTryNum >= 1){
                mTryNum --;
                doHttpClient(url, params, method);
                return;
            }else{
                LogUtils.logE("can not get the status code");
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_NO_STATUS_CODE, null);
                }
                return;
            }
        }
        
        int ret =statusLine.getStatusCode();

        if (ret == HttpURLConnection.HTTP_OK) {

            HttpEntity httpEntity = httpResponse.getEntity();
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            if (httpEntity != null) {
                try {
                    inputStream = httpEntity.getContent();

                    byte[] buf = new byte[8192];
                    int len = 0;
                    int count = 0;
                    int lowWater = buf.length / 2;
                    while (len != -1) {
                        synchronized (this) {
                            len = inputStream.read(buf, count, buf.length - count);
                            if (len != -1) {
                                count += len;
                            }
                            if (len == -1 || count >= lowWater) {
                                data.write(buf, 0, count);
                                count = 0;
                            }
                        }
                    }

                    inputSteamString = data.toString();
                } catch (IllegalStateException e) {
                    httpRequest.abort();
                    LogUtils.logE("doHttpClientConnect, " + "get an IllegalStateException when get content from HttpResonse");
                    if (mTryNum >= 1) {
                        mTryNum--;
                        doHttpClient(url, params, method);
                        return;
                    } else {
                        LogUtils.logE("doHttpClientConnect, can not get content from HttpResponse because IllegalStateException");
                        if (mResponedCallback != null) {
                            mResponedCallback.respond(this,	mConnectionId,HttpResponed.CONNECT_FAILED_GET_CONTENT_ERROR, null);
                        }
                        return;
                    }
                } catch (IOException e) {
                    httpRequest.abort();
                    e.printStackTrace();
                    LogUtils.logE("doHttpClientConnect, get an IOException when get content from HttpResponse");
                    if (mTryNum >= 1) {
                        mTryNum--;
                        doHttpClient(url, params, method);
                        return;
                    } else {
                        LogUtils.logE("doHttpClientConnect, can not get content from HttpResponse because IOException");
                        if (mResponedCallback != null) {
                            mResponedCallback.respond(this,	mConnectionId, HttpResponed.CONNECT_FAILED_GET_CONTENT_ERROR, null);
                        }
                        return;
                    }
                }finally{
                    if(inputStream != null){
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            httpRequest.abort();
            LogUtils.logE("doHttpClientConnect, get wrong status code : " + ret);
            if (mTryNum >= 1){
                mTryNum --;
                doHttpClient(url, params, method);
                return;
            }else{
                LogUtils.logE("doHttpClientConnect, status code error, status code is " +  ret);
                if(mResponedCallback != null){
                    mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_FAILED_WRONG_STATUS_CODE, null);
                }
                return;
            }
        }
        
        if(mResponedCallback != null){
            mResponedCallback.respond(this, mConnectionId, HttpResponed.CONNECT_SUCCESS, inputSteamString);
        }

        httpRequest.abort();
    }

    private HttpClient getHttpClient() {
        HttpClient httpClient = null;

                BasicHttpParams httpParams = new BasicHttpParams();

                HttpConnectionParams.setConnectionTimeout(httpParams, 8 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 8 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);


        HttpClientParams.setRedirecting(httpParams, true);


        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);

        httpClient = new DefaultHttpClient(httpParams);
        return httpClient;
    }

    private HttpURLConnection mHttpConn;
    //private OutputStream mOutPutStream;  
    private InputStream mInPutStream;
    private InputStream doHttpConnection(String url, HashMap<String, String> params, int method) {
        try {
            URL tempUrlObject = null;
            if (mProxyIP != null && mProxyIP.length() > 0) {
                String addStr = null;
                addStr = "http://" + mProxyIP + "/" + mUrlAddress;
                tempUrlObject = new URL(addStr);
                mHttpConn = (HttpURLConnection) tempUrlObject.openConnection();
            } else {
                tempUrlObject = new URL(mUrlAddress);
                mHttpConn = (HttpURLConnection) tempUrlObject.openConnection();
            }
        } catch (IOException e) {
            if (mTryNum >= 1){
                mTryNum --;
                return doHttpConnection(url, params, method);
            }else{
                return null;
            }
        }
        
        if (mHttpConn == null) {
            if (mTryNum >= 1){
                mTryNum --;
                return doHttpConnection(url, params, method);
            }else{
                return null;
            }
        }

        try {

            if (method == CONNECT_TYPE_HTTPCONNECTION_POST) {
                mHttpConn.setRequestProperty("request-hs", "post");
            } else if (method == CONNECT_TYPE_HTTPCONNECTION_GET) {
                mHttpConn.setRequestProperty("request-hs", "get");
            } else {
                LogUtils.logE("do http connection, unsupport connect type: " + method);
                return null;
            }

            if (mProxyIP != null) {
                URL tempUrlObject = new URL(mUrlAddress);
                mHttpConn.setRequestProperty("X-Online-Host", tempUrlObject.getHost() + ":"
                        + tempUrlObject.getPort());
            }

            if (params != null) {
                Iterator<Entry<String, String>> iter = params.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String key = entry.getKey();
                    key = URLEncoder.encode(key, "UTF-8");
                    String val = entry.getValue();
                    val = URLEncoder.encode(val, "UTF-8");
                    mHttpConn.setRequestProperty(key, val);
                }
            }

        } catch (IllegalStateException e){
            /*
             * exception throwed by setRequestProperty()
             * - if the connection has been already established.
             */
            e.printStackTrace();
            mHttpConn.disconnect();
            return null;
        }catch(NullPointerException e){
            /*
             * exception throwed by setRequestProperty()
             * - if the parameter  field is  null.
             */
            e.printStackTrace();
            mHttpConn.disconnect();
            return null;
        }catch(UnsupportedEncodingException e){
            /*
             *exception throwed by URLEncoder.encode()
             *- if the specified encoding scheme is invalid.
             */
            e.printStackTrace();
            mHttpConn.disconnect();
            return null;
        }catch(MalformedURLException e){
            /*
             * exception throwed by new URL();
             * - if the given string  spec could not be parsed as a URL.
             */
            e.printStackTrace();
            mHttpConn.disconnect();
            return null;
        }

        int ret = 0;
        try {
             ret = mHttpConn.getResponseCode();
        } catch (Exception e) {
            if (mTryNum >= 1){
                mTryNum --;
                return doHttpConnection(url, params, method);
            }else{
                LogUtils.logE("can not get the status code");
                mHttpConn.disconnect();
                return null;
            }
        }

        if (ret == HttpURLConnection.HTTP_OK) {
            try {
                mInPutStream = mHttpConn.getInputStream();

                if (mInPutStream != null) {

                    if(mResponedCallback != null){
                        //mResponedCallback.handleInputStream(mInPutStream);
                    }
                    mHttpConn.disconnect();
                    return mInPutStream;
                } else {
                    if (mTryNum >= 1){
                        mTryNum --;
                        return doHttpConnection(url, params, method);
                    }else{
                        LogUtils.logE("get nothing from server");
                        mHttpConn.disconnect();
                        //mResponedCallback.handleInputStream(null);
                        return null;
                    }
                }
            } catch (IOException e) {
                if (mTryNum >= 1){
                    mTryNum --;
                    return doHttpConnection(url, params, method);
                }else{
                    LogUtils.logE("get content error");
                    mHttpConn.disconnect();
                }
            }
        } else {
            if (mTryNum >= 1){
                mTryNum --;
                return doHttpConnection(url, params, method);
            }else{
                LogUtils.logE("status code error, status code is " +  ret);
                mHttpConn.disconnect();
            }
        }

        //mResponedCallback.handleInputStream(null);
        return null;
    }

    public static String convertStreamToString(InputStream is) {
        /*  
         * To convert the InputStream to String we use the BufferedReader.readLine()  
         * method. We iterate until the BufferedReader return null which means  
         * there's no more data to read. Each line will appended to a StringBuilder  
         * and returned as String.  
         */  
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));   
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
