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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.Configuration;

/**
 * Storage操作相关的工具类
 * 
 * @author liuxinjian
 * @since 2013-12-24
 */
public final class StorageUtils {
    /** Log TAG */
    private static final String TAG = "StorageUtils";
    /** Log switch */
    private static final boolean DEBUG = Configuration.DEBUG & true;

    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private StorageUtils() {
    }

    /** 根据系统时间生成文件名的格式 */
    private static SimpleDateFormat sDateFormat = null;
    
    /**
     * 根据系统时间生成文件名
     * 
     * @param suffix
     *            文件后缀名
     * @return 文件名
     */
    public static synchronized String createFileName(String suffix) {
        if (null == sDateFormat) {
            sDateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss-SSS");
        }
        Date date = new Date();
        return String.format("%s.%s", sDateFormat.format(date), suffix);
    }
    
    /**
     * 判断外部存储是否可写
     * 
     * 此方法内采用文件读写操作来检测，所以相对比较耗时，请谨慎使用。
     * 
     * @return true:可写; false 不存在/没有mounted/不可写
     */
    public static boolean isExternalStorageWriteable() {
        boolean writealbe = false;
        long start = System.currentTimeMillis();
        if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
            File esd = Environment.getExternalStorageDirectory();
            if (esd.exists() && esd.canWrite()) {
                File file = new File(esd, ".696E5309-E4A7-27C0-A787-0B2CEBF1F1AB");
                if (file.exists()) {
                    writealbe = true;
                } else {
                    try {
                        writealbe = file.createNewFile();
                    } catch (IOException e) {
                        if (DEBUG) {
                            Log.w(TAG, "isExternalStorageWriteable() can't create test file.");
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.i(TAG, "Utility.isExternalStorageWriteable(" + writealbe + ") cost " + (end - start) + "ms.");
        }
        return writealbe;
    }
    
    /**
     * 确定SD卡缓存路径在使用前已经存在.
     * 
     * @param dir
     *            目录
     * @return 是否建立成功
     */
    public static boolean ensureDirectoryExist(final File dir) {
        if (dir == null) {
            return false;
        }
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException e) {
                return false;
            }
        }
        return true;
    }
}
