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

import com.lee.sdk.downloads.DownloadManager;

/**
 * 下载状态
 */
public enum DownloadState {
    /**
     * 未开始
     */
    NOT_START,

    /**
     * 下载中
     */
    DOWNLOADING,

    /**
     * 下载暂停
     */
    DOWNLOAD_PAUSED,

    /**
     * 下载完成
     */
    DOWNLOADED,

    /**
     * 下载失败
     */
    DOWNLOAD_FAILED;

    /**
     * Convert Value of {@link #COLUMN_STATUS} to DownloadState.
     * 
     * @param status Value of {@link #COLUMN_STATUS}
     * @return DownloadState
     */
    public static DownloadState convert(int status) {
        DownloadState state = null;
        switch (status) {
        case DownloadManager.STATUS_PENDING:
            state = DOWNLOADING;
            break;
        case DownloadManager.STATUS_RUNNING:
            state = DOWNLOADING;
            break;
        case DownloadManager.STATUS_PAUSED:
            state = DOWNLOAD_PAUSED;
            break;
        case DownloadManager.STATUS_SUCCESSFUL:
            state = DOWNLOADED;
            break;
        case DownloadManager.STATUS_FAILED:
            state = DOWNLOAD_FAILED;
            break;
        default:
            state = NOT_START;
            break;
        }
        return state;
    }
}
