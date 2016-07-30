/*
 * Copyright (C) 2013 Lee Hong (http://blog.csdn.net/leehong2005)
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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.lee.sdk.Configuration;

/**
 * NetUtils
 * 
 * @author LiuXinjian
 * @since 2014-1-3
 */
public final class NetUtils {
    
    /** Log TAG */
    private static final String TAG = "NetUtils";

    /** Log switch */
    private static final boolean DEBUG = Configuration.DEBUG & true;

    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private NetUtils() {
    }
    
    /**
     * 网络是否可用。(
     * 
     * @param context
     *            context
     * @return 连接并可用返回 true
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable();
        if (DEBUG) {
            Log.d(TAG, "isNetworkConnected, rtn: " + flag);
        }
        return flag;
    }
    
    /**
     * wifi网络是否可用
     * 
     * @param context
     *            context
     * @return wifi连接并可用返回 true
     */
    public static boolean isWifiNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable()
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        if (DEBUG) {
            Log.d(TAG, "isWifiNetworkConnected, rtn: " + flag);
        }
        return flag;
        
    }
    
    /**
     * 获取活动的连接。
     * 
     * @param context
     *            context
     * @return 当前连接
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }

}
