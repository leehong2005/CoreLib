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

package com.lee.sdk.downloads.ext;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.lee.sdk.Configuration;
import com.lee.sdk.downloads.DownloadManager;
import com.lee.sdk.downloads.DownloadManager.Query;
import com.lee.sdk.downloads.Downloads;
import com.lee.sdk.downloads.OpenDownloadReceiver;
import com.lee.sdk.utils.WebAddress;

/**
 * DownloadManagerEx
 */
public final class DownloadManagerEx {
    /** DEBUG **/
    public static final boolean DEBUG = Configuration.DEBUG;
    /** TAG **/
    public static final String TAG = "DownloadManagerEx";
    /** ContentResolver **/
    private ContentResolver mResolver;
    /** package name. */
    private String mPackageName;
    /** downloadmanager **/
    private DownloadManager mDownloadManger;
    /** Uri与DownloadObserver的映射表 */
    private HashMap<Uri, DownloadObserver> mUriToObserver = new HashMap<Uri, DownloadObserver>();
    /** 单例 */
    private static volatile DownloadManagerEx sInstance = null;

    /**
     * 得到单实例
     * 
     * @param context Context
     * @param packageName 应用的包名
     * @return 单例
     */
    public static DownloadManagerEx getInstance(Context context, String packageName) {
        if (null == sInstance) {
            synchronized (DownloadManagerEx.class) {
                if (null == sInstance) {
                    sInstance = new DownloadManagerEx(context, packageName);
                }
            }
        }
        return sInstance;
    }

    /**
     * constructor
     * 
     * @param context context
     * @param packageName packageName
     */
    private DownloadManagerEx(Context context, String packageName) {
        mResolver = context.getContentResolver();
        mPackageName = packageName;
        mDownloadManger = new DownloadManager(mResolver, packageName);
    }

    /**
     * 注册监听器
     * 
     * @param context context
     * @param uri 下载的Uri
     * @param listener 更新listener
     */
    public void registerObserver(Context context, Uri uri, DownloadListener listener) {
        if (listener == null) {
            if (DEBUG) {
                Log.e(TAG, "registerObserver(listener == null)");
            }
            return;
        }

        long id = getIdFromUri(uri);
        if (-1 == id) {
            if (DEBUG) {
                Log.e(TAG, "registerObserver(id == -1)");
            }
            return;
        }

        DownloadObserver observer = mUriToObserver.get(uri);
        if (observer == null) {
            observer = new DownloadObserver(context, uri);
            mUriToObserver.put(uri, observer);
            context.getContentResolver().registerContentObserver(uri, true, observer);
        }

        observer.addListener(listener);
    }

    /**
     * 删除observer
     * 
     * @param context context
     * @param uri 下载的Uri
     * @param listener 监听下载状态的listener
     */
    public void unregisterObserver(Context context, Uri uri, DownloadListener listener) {
        if (uri == null) {
            return;
        }

        DownloadObserver observer = mUriToObserver.get(uri);
        if (observer == null) {
            return;
        }

        observer.removeListener(listener);
        if (observer.isListenersEmpty()) {
            context.getContentResolver().unregisterContentObserver(observer);
            mUriToObserver.remove(uri);
        }
    }

    /**
     * 删除observer
     * 
     * @param context context
     * @param uri 下载的Uri
     */
    public void unregisterObserver(Context context, Uri uri) {
        if (uri == null) {
            return;
        }

        DownloadObserver observer = mUriToObserver.get(uri);
        if (observer == null) {
            return;
        }

        observer.clearListeners();
        context.getContentResolver().unregisterContentObserver(observer);
        mUriToObserver.remove(uri);
    }

    /**
     * 进入APP时，可以读取此状态，然后如果是暂停的话，再根据状态来判断是否进行二次下载 获取当前的下载数据
     * 
     * @param uri 下载的Uri
     * @return DownloadBean
     */
    public DownloadBean queryDownloadData(Uri uri) {
        if (null == uri) {
            return null;
        }
        DownloadBean pdb = new DownloadBean(uri);
        queryDownloadData(pdb);
        return pdb;
    }

    /**
     * 根据id读取数据库数据
     * 
     * @param pdb DownloadBean
     */
    public void queryDownloadData(DownloadBean pdb) {
        if (-1 == pdb.getDownloadId()) {
            return;
        }

        Cursor cursor = mDownloadManger.query(new Query().setFilterById(pdb.getDownloadId()));
        try {
            if (null == cursor || cursor.getCount() == 0 || !cursor.moveToFirst()) {
                pdb.setCurrentBytes(0);
                pdb.setTotalBytes(-1);
                pdb.setDownloadState(DownloadState.NOT_START);

                if (DEBUG) {
                    Log.w(TAG, "null == cursor || cursor.getCount() == 0 || !cursor.moveToFirst()");
                }
                return;
            }

            // 读取下载数据,可根据需求再扩展返回数据
            final int totalBytesColumnIndex = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            final int currentBytesIndex = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            final int statusIndex = cursor.getColumnIndex(Downloads.Impl.COLUMN_STATUS);

            final long total = cursor.getLong(totalBytesColumnIndex);
            final long current = cursor.getLong(currentBytesIndex);
            final int status = cursor.getInt(statusIndex);

            if (DEBUG) {
                Log.d(TAG, "query(total=" + total + ", current=" + current + ", status=" + status + ")");
            }

            pdb.setTotalBytes(total);
            pdb.setCurrentBytes(current);
            pdb.setDownloadState(DownloadState.convert(status));
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
    }

    /**
     * 暂停下载
     * 
     * @param uri Uri
     */
    public void pauseDownload(final Uri uri) {
        long id = getIdFromUri(uri);
        if (-1 == id) {
            if (DEBUG) {
                Log.e(TAG, "pauseDownload(id=-1)");
            }
        } else {
            if (DEBUG) {
                Log.w(TAG, "pauseDownload(uri=" + uri + ")");
            }
            mDownloadManger.pauseDownload(id);
        }
    }

    /**
     * 恢复下载
     * 
     * @param uri Uri
     */
    public void resumeDownload(final Uri uri) {
        long id = getIdFromUri(uri);
        if (-1 == id) {
            if (DEBUG) {
                Log.e(TAG, "resumeDownload(id == -1)");
            }
        } else {
            if (DEBUG) {
                Log.w(TAG, "resumeDownload(uri=" + uri + ")");
            }
            mDownloadManger.resumeDownload(id);
        }
    }

    /**
     * 取消下载
     * 
     * @param uri Uri
     */
    public void cancelDownload(final Uri uri) {
        long id = getIdFromUri(uri);
        if (-1 == id) {
            if (DEBUG) {
                Log.e(TAG, "cancelDownload(id == -1)");
            }
        } else {
            if (DEBUG) {
                Log.w(TAG, "cancelDownload(uri=" + uri + ")");
            }
            mDownloadManger.remove(id);
        }
    }

    /**
     * 根据Uri取得ID
     * 
     * @param uri Uri
     * @return ID
     */
    private long getIdFromUri(Uri uri) {
        long id = -1;
        if (uri != null) {
            id = ContentUris.parseId(uri);
        } else {
            if (DEBUG) {
                Log.e(TAG, "getIdFromUri(uri == null)");
            }
        }
        return id;
    }

    /**
     * @param url 下载地址
     * @return Uri uri
     */
    public Uri doDownload(String url) {
        return doDownload(url, null, null, false, false, false, false);
    }

    /**
     * @param url 下载地址
     * @param destinationDir 下载目录不以/结束
     * @param destinationName 下载文件名
     * @return Uri uri
     */
    public Uri doDownload(String url, String destinationDir, String destinationName) {
        return doDownload(url, destinationDir, destinationName, false, false, false, false);
    }

    /**
     * @param url 下载地址
     * @param destinationDir 下载目录不以/结束
     * @param destinationName 下载文件名
     * @param receiverClassName 调起的类名
     * @param isVisibleInDownloadsUi 是否在下载页面显示
     * @param visibility 是否在状态栏显示
     * @param wifiOnly 只在wifi网络下载
     * @param publicApi 只有在publicApi为true的时候才会存储下载项对网络的需求，静默下载插件引入
     * @return Uri uri
     */
    public Uri doDownload(String url, String destinationDir, String destinationName, String receiverClassName,
            boolean isVisibleInDownloadsUi, boolean visibility, boolean wifiOnly, boolean publicApi) {
        if (!TextUtils.isEmpty(destinationDir)) {
            File dir = new File(destinationDir);
            if (!dir.exists()) {
                // 目录不存在
                dir.mkdirs();
            }
        }

        WebAddress webAddress = null;
        try {
            webAddress = new WebAddress(url);
            webAddress.mPath = encodePath(webAddress.mPath);
        } catch (Exception e) {
            // This only happens for very bad urls, we want to chatch the
            // exception here
            e.printStackTrace();
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_URI, webAddress.toString());
        values.put(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE, mPackageName);
        values.put(Downloads.Impl.COLUMN_NOTIFICATION_CLASS, receiverClassName);
        // values.put(Downloads.Impl.COLUMN_MIME_TYPE, "application/vnd.android.package-archive");
        // //此处mimetype 不设置
        if (!TextUtils.isEmpty(destinationDir) && !TextUtils.isEmpty(destinationName)) {
            values.put(Downloads.Impl.COLUMN_DESTINATION, Downloads.Impl.DESTINATION_FILE_URI);
            String file = "file://" + destinationDir + File.separator + destinationName;
            values.put(Downloads.Impl.COLUMN_FILE_NAME_HINT, file);
        } else {
            if (!TextUtils.isEmpty(destinationName)) {
                values.put(Downloads.Impl.COLUMN_FILE_NAME_HINT, destinationName);
            }
            values.put(Downloads.Impl.COLUMN_DESTINATION, Downloads.Impl.DESTINATION_EXTERNAL);
        }
        values.put(Downloads.Impl.COLUMN_NO_INTEGRITY, true); // 禁止去检测文件的完整性，否则在网络中断或者其他异常下会删除文件
        values.put(Downloads.Impl.COLUMN_DESCRIPTION, webAddress.mHost);
        int vis = visibility ? Downloads.Impl.VISIBILITY_VISIBLE : Downloads.Impl.VISIBILITY_HIDDEN;
        values.put(Downloads.Impl.COLUMN_VISIBILITY, vis); // 不在通知栏显示
        values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, isVisibleInDownloadsUi); // 不在DownloadList中显示
        values.put(Downloads.Impl.COLUMN_IS_PUBLIC_API, publicApi);
        if (wifiOnly) {
            values.put(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, DownloadManager.Request.NETWORK_WIFI);
        }
        Uri uri = mResolver.insert(Downloads.Impl.CONTENT_URI, values);
        if (DEBUG) {
            Log.w(TAG, "doDownload(uri=" + uri + ")");
        }
        return uri;
    }

    /**
     * @param url 下载地址
     * @param destinationDir 下载目录不以/结束
     * @param destinationName 下载文件名
     * @param isVisibleInDownloadsUi 是否在下载页面显示
     * @param visibility 是否在状态栏显示
     * @param wifiOnly 只在wifi网络下载
     * @param publicApi 只有在publicApi为true的时候才会存储下载项对网络的需求，静默下载插件引入
     * @return Uri uri
     */
    public Uri doDownload(String url, String destinationDir, String destinationName, boolean isVisibleInDownloadsUi,
            boolean visibility, boolean wifiOnly, boolean publicApi) {
        return doDownload(url, destinationDir, destinationName, OpenDownloadReceiver.class.getCanonicalName(),
                isVisibleInDownloadsUi, visibility, wifiOnly, publicApi);
    }

    /**
     * This is to work around the fact that java.net.URI throws Exceptions instead of just encoding
     * URL's properly Helper method for onDownloadStartNoStream.
     * 
     * @param path web path
     * @return encode path.
     * */
    private static String encodePath(String path) {
        char[] chars = path.toCharArray();

        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']') {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return path;
        }

        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 
     * 下载进度监听类 只针对一个内核的uri进行监听，多个内核需要new多个监听器
     * 
     * @author liuxinjian
     * @since 2013-4-24
     */
    class DownloadObserver extends ContentObserver {

        /** 上一秒钟下载的bytes,用于计算下载速度 **/
        private long mLastBytes = 0;

        /** 记录下载的时间 **/
        private long mLastTime = 0;

        /** 上一次下载状态 **/
        private DownloadState mLastState = DownloadState.NOT_START;

        /** 下载信息 */
        private DownloadBean mPdb;

        /** 监听下载状态的listener集合 */
        private HashSet<DownloadListener> mListeners = new HashSet<DownloadListener>();

        /**
         * 构造函数
         * 
         * @param context context
         * @param uri URI
         */
        public DownloadObserver(Context context, Uri uri) {
            super(new Handler(Looper.getMainLooper()));
            mPdb = new DownloadBean(uri);
            if (DEBUG) {
                Log.w(TAG, "new DownloadObserver(" + uri + ")");
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            queryDownloadData(mPdb);
            final long now = System.currentTimeMillis();
            if ((mLastState == mPdb.getDownloadState() && mLastBytes == mPdb.getCurrentBytes()) || mLastTime == now) {
                return;
            }

            if (DownloadState.DOWNLOADING == mPdb.getDownloadState()) {
                // 速度统计很不准，所以只计算 DOWNLOADING状态 和 DOWNLOADING状态 之间的下载速度。
                mPdb.setSpeed(((mPdb.getCurrentBytes() - mLastBytes) * DateUtils.SECOND_IN_MILLIS) / (now - mLastTime));
            } else {
                mPdb.setSpeed(0);
            }

            if (DEBUG) {
                Log.i(TAG, "DownloadObserver.onChange(" + mPdb + ")");
            }
            mLastBytes = mPdb.getCurrentBytes();
            mLastState = mPdb.getDownloadState();
            mLastTime = now;

            synchronized (this) {
                DownloadListener[] listeners = new DownloadListener[mListeners.size()];
                mListeners.toArray(listeners);
                for (DownloadListener listener : listeners) {
                    listener.onChanged(mPdb);
                }
            }
        }

        /**
         * 设置下载监听器
         * 
         * @param listener listener
         * @return true or false.
         */
        public synchronized boolean addListener(DownloadListener listener) {
            return mListeners.add(listener);
        }

        /**
         * 取消监听
         * 
         * @param listener listener
         * @return true or false.
         */
        public synchronized boolean removeListener(DownloadListener listener) {
            return mListeners.remove(listener);
        }

        /**
         * 取消所有监听
         */
        public synchronized void clearListeners() {
            mListeners.clear();
        }

        /**
         * 判断监听器集合是否为空
         * 
         * @return true or false.
         */
        public boolean isListenersEmpty() {
            return mListeners.isEmpty();
        }
    }
}
