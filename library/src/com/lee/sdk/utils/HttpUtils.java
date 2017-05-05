/*
 * Copyright (C) 2016 LiHong (https://github.com/leehong2005)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lee.sdk.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Patterns;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

/**
 * @author lihong
 * @date 2016/03/01
 */
final class HttpUtils {
    /**
     * 判断一个字符串是否为合法url
     *
     * @param query String
     * @return true: 是合法url
     */
    public static boolean isUrl(String query) {
        Matcher matcher = Patterns.WEB_URL.matcher(query);
        if (matcher.matches()) {
            return true;
        }

        return false;
    }

    /**
     * 网络是否可用。
     *
     * @param context context
     * @return 连接并可用返回 true
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable();
        return flag;
    }

    /**
     * 获取活动的连接。
     *
     * @param context context
     * @return 当前连接
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }

    /**
     * 创建一个 http client。
     *
     * @param context Context.
     * @return ProxyHttpClient
     */
    public static DefaultHttpClient createHttpClient(Context context) {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        // httpclient.getParams().setParameter("Accept-Encoding", "gzip");

        final int httpTimeout = 30000;
        final int socketTimeout = 50000;
        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), httpTimeout);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), socketTimeout);
        return httpclient;
    }

    /**
     * 根据server下发的Content-Encoding，获取适当的inputstream.当content-encoding为gzip时，
     * 返回GzipInputStream 否则返回原有的inputStream
     *
     * @param resEntity {@link HttpEntity}
     * @return InputStream or null
     * @throws IOException {@link IOException}
     */
    @SuppressLint("DefaultLocale")
    public static InputStream getSuitableInputStream(HttpEntity resEntity) throws IOException {
        if (resEntity == null) {
            return null;
        }
        InputStream inputStream = resEntity.getContent();
        if (inputStream != null) {
            Header header = resEntity.getContentEncoding();
            if (header != null) {
                String contentEncoding = header.getValue();
                if (!TextUtils.isEmpty(contentEncoding) && contentEncoding.toLowerCase().indexOf("gzip") > -1) {
                    inputStream = new GZIPInputStream(inputStream);
                }
            }
        }
        return inputStream;
    }
}
