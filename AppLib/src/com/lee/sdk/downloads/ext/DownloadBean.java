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

import android.content.ContentUris;
import android.net.Uri;

/**
 * 下载信息
 */
public class DownloadBean {
    /** Download Uri **/
    private final Uri mUri;
    /** download id */
    private final long mDownloadId;
    /** 当前下载进度 **/
    private long mCurrentBytes;
    /** 下载文件总大小 **/
    private long mTotalBytes;
    /** 下载speed **/
    private long mSpeedBytes;
    /** 下载的状态 **/
    private DownloadState mDownloadState = DownloadState.NOT_START;

    /**
     * 构造方法
     * 
     * @param uri DownloadProvider的URI
     */
    public DownloadBean(Uri uri) {
        mUri = uri;
        mDownloadId = ContentUris.parseId(uri);
    }

    /**
     * 设置下载状态
     * 
     * @param state PluginDownloadState
     */
    public void setDownloadState(DownloadState state) {
        mDownloadState = state;
    }

    /**
     * 读取下载状态
     * 
     * @return PluginDownloadState
     */
    public DownloadState getDownloadState() {
        return mDownloadState;
    }

    /**
     * 设置当前下载量
     * 
     * @param current currentdownloadbytes
     */
    public void setCurrentBytes(long current) {
        mCurrentBytes = current;
    }

    /**
     * 获取当前下载的量
     * 
     * @return mCurrentBytes
     */
    public long getCurrentBytes() {
        return mCurrentBytes;
    }

    /**
     * 设置下载文件总大小
     * 
     * @param totalbytes kernel download file size
     */
    public void setTotalBytes(long totalbytes) {
        mTotalBytes = totalbytes;
    }

    /**
     * 获取下载文件总大小
     * 
     * @return kernel download file size
     */
    public long getTotalBytes() {
        return mTotalBytes;
    }

    /**
     * 读取下载uri
     * 
     * @return 下载uri
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * @return the mDownloadId
     */
    public long getDownloadId() {
        return mDownloadId;
    }

    /**
     * 设置下载速度
     * 
     * @param speed speed
     */
    public void setSpeed(long speed) {
        mSpeedBytes = speed;
    }

    /**
     * 获取下载速度
     * 
     * @return mSpeed
     */
    public long getSpeed() {
        return mSpeedBytes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DownloadBean=(uri: " + mUri);
        sb.append(", current bytes: " + mCurrentBytes);
        sb.append(", total bytes: " + mTotalBytes);
        sb.append(", speed: " + mSpeedBytes);
        sb.append(", state: " + mDownloadState);
        sb.append(")");
        return sb.toString();
    }
}
