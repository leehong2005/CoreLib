/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.lee.sdk.downloads;

import java.util.Collection;
import java.util.HashMap;

import com.lee.sdk.res.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * This class handles the updating of the Notification Manager for the
 * cases where there is an ongoing download. Once the download is complete
 * (be it successful or unsuccessful) it is no longer the responsibility
 * of this component to show the download in the notification manager.
 *
 */
class DownloadNotification {

    /** context used to access system services. */
    Context mContext;
    /** mNotifications. */
    HashMap <Long, NotificationItem> mNotifications;
    /** SystemFacade. */
    private SystemFacade mSystemFacade;

    /** log tag. */
    static final String LOGTAG = "DownloadNotification";
    /** WHERE_RUNNING. */
    static final String WHERE_RUNNING =
        "(" + Downloads.Impl.COLUMN_STATUS + " >= '100') AND ("
            + Downloads.Impl.COLUMN_STATUS + " <= '199') AND ("
            + Downloads.Impl.COLUMN_VISIBILITY + " IS NULL OR "
            + Downloads.Impl.COLUMN_VISIBILITY + " == '" + Downloads.Impl.VISIBILITY_VISIBLE + "' OR "
            + Downloads.Impl.COLUMN_VISIBILITY
            + " == '" + Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "')";
    /** WHERE_COMPLETED. */
    static final String WHERE_COMPLETED =
        Downloads.Impl.COLUMN_STATUS + " >= '200' AND "
        + Downloads.Impl.COLUMN_VISIBILITY
        + " == '" + Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "'";


    /**
     * This inner class is used to collate downloads that are owned by
     * the same application. This is so that only one notification line
     * item is used for all downloads of a given application.
     *
     */
    static class NotificationItem {
        /** // This first db _id for the download for the app*/
        int mId; 
        /** total current. */
        long mTotalCurrent = 0;
        /** total total. */
        long mTotalTotal = 0;
        /** mtitles. */
        String mTitle; // download title.
        /** paused text. */
        String mPausedText = null;
        
        /** Ticker text */
        String mTickerText = null;
    }


    /**
     * Constructor
     * @param ctx The context to use to obtain access to the
     *            Notification Service
     * @param systemFacade SystemFacade
     */
    DownloadNotification(Context ctx, SystemFacade systemFacade) {
        mContext = ctx;
        mSystemFacade = systemFacade;
        mNotifications = new HashMap<Long, NotificationItem>();
    }

    /**
     * Update the notification ui.
     * @param downloads downloads.
     */
    public void updateNotification(Collection<DownloadInfo> downloads) {
        updateActiveNotification(downloads);
        updateCompletedNotification(downloads);
    }

    /**
     * updateActiveNotification.
     * @param downloads downloads
     */
    private void updateActiveNotification(Collection<DownloadInfo> downloads) {
        // Collate the notifications
        mNotifications.clear();
        for (DownloadInfo download : downloads) {
            if (!isActiveAndVisible(download)) {
                continue;
            }
            
            // fix bug BaiduSearchAndroid-248
            // 通知栏更新，只更新下载中的任务，暂停的通知栏消息，不寄予更新
            if (download.mStatus != Downloads.STATUS_RUNNING) {
                continue;
            }
            if (download.mStatus == Downloads.STATUS_PENDING) {
                continue;
            }
                
            long max = download.mTotalBytes;
            long progress = download.mCurrentBytes;
            long id = download.mId;
            String title = download.mTitle;
            if (title == null || title.length() == 0) {
                // 首选 hint 文件名
                if (!TextUtils.isEmpty(download.mHint)) {
                    title = download.mHint;
                } else {
                    title = mContext.getResources().getString(
                            R.string.download_unknown_title);
                }
            }
            
            NotificationItem item = new NotificationItem();
            item.mId = (int) id;
            item.mTitle = title;
            item.mTotalCurrent = progress;
            item.mTotalTotal = max;
            mNotifications.put(id, item);
            
            if (download.mStatus == Downloads.Impl.STATUS_QUEUED_FOR_WIFI
                    && item.mPausedText == null) {
                item.mPausedText = mContext.getResources().getString(
                        R.string.notification_need_wifi_for_size);
            }
            
        }
            
        // Add the notifications
        for (NotificationItem item : mNotifications.values()) {
            // Build the notification object
            VersionedNotification notification = VersionedNotification.getInstance(mContext);

            boolean hasPausedText = (item.mPausedText != null);
            int iconResource = android.R.drawable.stat_sys_download;
            if (hasPausedText) {
                iconResource = android.R.drawable.stat_sys_warning;
            }
            notification.setSmallIcon(iconResource);
            notification.setOngoing(true);

            String title = item.mTitle;
            notification.setContentTitle(title);
            if (hasPausedText) {
                notification.setContentText(item.mPausedText);
            } else {
                notification.setProgress((int) item.mTotalTotal, 
                        (int) item.mTotalCurrent, 
                        item.mTotalTotal == -1);
//                notification.setContentText(getDownloadingText(item.mTotalTotal, item.mTotalCurrent));
                notification.setContentInfo(
                        getDownloadingText(item.mTotalTotal, item.mTotalCurrent));
            }
            if (item.mTickerText == null) {
                item.mTickerText = item.mTitle + " " + mContext.getResources().getString(
                      R.string.download_begin);
            }
            notification.setTicker(item.mTickerText);
            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(
                    ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, item.mId));
            intent.putExtra("multiple", false);

            notification.setContentIntent(PendingIntent.getBroadcast(
                    mContext, 0, intent, 0));
            notification.setWhen(0);
            
            mSystemFacade.postNotification(item.mId, notification.getNotification());

        }
    }
    
    /**
     * updateCompletedNotification.
     * @param downloads DOWNLOADS
     */
    private void updateCompletedNotification(Collection<DownloadInfo> downloads) {
        for (DownloadInfo download : downloads) {
            if (!isCompleteAndVisible(download)) {
                continue;
            }
            // Add the notifications
            VersionedNotification nv = VersionedNotification.getInstance(mContext);
            
            Notification n = nv.getNotification();
            //n.icon = R.drawable.sdk_downloads_notification_icon;
//            n.icon = R.drawable.icon;
//            nv.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon));
            nv.setSmallIcon(android.R.drawable.stat_sys_download_done);
            long id = download.mId;
            String title = download.mTitle;
            if (title == null || title.length() == 0) {
                // 首选 hint 文件名
                if (!TextUtils.isEmpty(download.mHint)) {
                    title = download.mHint;
                } else {
                    title = mContext.getResources().getString(
                            R.string.download_unknown_title);
                }
            } 
            
            Uri contentUri =
                ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
            String caption;
            Intent intent;
            if (Downloads.Impl.isStatusError(download.mStatus)) {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_failed);
                intent = new Intent(Constants.ACTION_LIST);
                n.icon = android.R.drawable.stat_sys_warning; // 下载失败 notification icon.
            } else {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_complete);
                if (download.mDestination == Downloads.Impl.DESTINATION_EXTERNAL) {
                    intent = new Intent(Constants.ACTION_OPEN);
                } else {
                    intent = new Intent(Constants.ACTION_LIST);
                }
            }
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);

            n.when = download.mLastMod;
            n.setLatestEventInfo(mContext, title, caption,
                    PendingIntent.getBroadcast(mContext, 0, intent, 0));

            intent = new Intent(Constants.ACTION_HIDE);
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);
            n.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

            mSystemFacade.postNotification(download.mId, n);
        }
    }

    /**
     * isActiveAndVisible 
     * @param download DownloadInfo
     * @return is active and visibility == visible
     */
    private boolean isActiveAndVisible(DownloadInfo download) {
        return 100 <= download.mStatus && download.mStatus < 200 // SUPPRESS CHECKSTYLE
                && download.mVisibility != Downloads.VISIBILITY_HIDDEN;
    }
    /**
     * isCompleteAndVisible 
     * @param download DownloadInfo
     * @return is completed and visibility == visible
     */
    private boolean isCompleteAndVisible(DownloadInfo download) {
        return download.mStatus >= 200 // SUPPRESS CHECKSTYLE
                && download.mVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    }

    /**
     * Helper function to build the downloading text.
     * @param totalBytes totalBytes
     * @param currentBytes currentBytes
     * @return the string 
     */
    private String getDownloadingText(long totalBytes, long currentBytes) {
        if (totalBytes <= 0) {
            return "";
        }
        long progress = currentBytes * 100 / totalBytes; // SUPPRESS CHECKSTYLE
        StringBuilder sb = new StringBuilder();
        sb.append(progress);
        sb.append('%');
        return sb.toString();
    }
    
    

}
