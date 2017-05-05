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

import java.io.File;

import com.lee.sdk.res.R;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {
    /** SystemFacade. */
    SystemFacade mSystemFacade = null;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mSystemFacade == null) {
            mSystemFacade = new RealSystemFacade(context);
        }

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startService(context);
        } else 
//            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//            // move to searchbox
//        } else 
            if (action.equals(Constants.ACTION_RETRY)) {
            startService(context);
        } else if (action.equals(Constants.ACTION_OPEN)
                || action.equals(Constants.ACTION_LIST)
                || action.equals(Constants.ACTION_HIDE)) {
            handleNotificationBroadcast(context, intent);
        }
    }

    /**
     * Handle any broadcast related to a system notification.
     * @param context context
     * @param intent intent
     */
    private void handleNotificationBroadcast(Context context, Intent intent) {
        Uri uri = intent.getData();
        String action = intent.getAction();
        if (Constants.LOGVV) {
            if (action.equals(Constants.ACTION_OPEN)) {
                Log.v(Constants.TAG, "Receiver open for " + uri);
            } else if (action.equals(Constants.ACTION_LIST)) {
                Log.v(Constants.TAG, "Receiver list for " + uri);
            } else { // ACTION_HIDE
                Log.v(Constants.TAG, "Receiver hide for " + uri);
            }
        }

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return;
        }
        try {
            if (!cursor.moveToFirst()) {
                return;
            }

            if (action.equals(Constants.ACTION_OPEN)) {
                openDownload(context, intent, cursor);
                hideNotification(context, uri, cursor);
            } else if (action.equals(Constants.ACTION_LIST)) {
                sendNotificationClickedIntent(intent, cursor);
                
                // 如果下载失败也需要删除通知栏
                int statusColumn = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_STATUS);
                int status = cursor.getInt(statusColumn);
                
                if (Downloads.Impl.isStatusCompleted(status)) {
                    hideNotification(context, uri, cursor);
                }
            } else { // ACTION_HIDE
                hideNotification(context, uri, cursor);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Hide a system notification for a download.
     * @param context context
     * @param uri URI to update the download
     * @param cursor Cursor for reading the download's fields
     */
    private void hideNotification(Context context, Uri uri, Cursor cursor) {
        mSystemFacade.cancelNotification(ContentUris.parseId(uri));

        int statusColumn = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_STATUS);
        int status = cursor.getInt(statusColumn);
        int visibilityColumn =
                cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_VISIBILITY);
        int visibility = cursor.getInt(visibilityColumn);
        if (Downloads.Impl.isStatusCompleted(status)
                && visibility == Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_VISIBILITY,
                    Downloads.Impl.VISIBILITY_VISIBLE);
            context.getContentResolver().update(uri, values, null, null);
        }
    }

    /**
     * Open the download that cursor is currently pointing to, since it's completed notification
     * has been clicked.
     * @param context context
     * @param intent intent
     * @param cursor cursor
     */
    private void openDownload(Context context, Intent intent, Cursor cursor) {
        // 如果有receiver，走receiver的逻辑
        String pckg = cursor.getString(
                cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE));
        
        if (!TextUtils.isEmpty(pckg)) {
            
            String clazz = cursor.getString(
                    cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_NOTIFICATION_CLASS));
            boolean isPublicApi =
                    cursor.getInt(cursor.getColumnIndex(Downloads.Impl.COLUMN_IS_PUBLIC_API)) != 0;
            
            if (!isPublicApi && !TextUtils.isEmpty(clazz)) {
                // modify by qiaopu 此时不能支持multiple
                sendNotificationClickedIntent(intent, cursor, pckg, clazz, isPublicApi, false);
                // end modify
                return;
            }
        }
        
        String filename = cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.DATA));
        String mimetype =
            cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_MIME_TYPE));
        Uri path = Uri.parse(filename);
        // If there is no scheme, then it must be a file
        if (path.getScheme() == null) {
            path = Uri.fromFile(new File(filename));
        }

        Intent activityIntent = new Intent(Intent.ACTION_VIEW);
        activityIntent.setDataAndType(path, mimetype);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(activityIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.download_no_application_title, Toast.LENGTH_LONG).show();
            Log.d(Constants.TAG, "no activity for " + mimetype, ex);
        }
    }

    /**
     * Notify the owner of a running download that its notification was clicked.
     * @param intent the broadcast intent sent by the notification manager
     * @param cursor Cursor for reading the download's fields
     */
    private void sendNotificationClickedIntent(Intent intent, Cursor cursor) {
        String pckg = cursor.getString(
                cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE));
        if (pckg == null) {
            return;
        }

        String clazz = cursor.getString(
                cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_NOTIFICATION_CLASS));
        boolean isPublicApi =
                cursor.getInt(cursor.getColumnIndex(Downloads.Impl.COLUMN_IS_PUBLIC_API)) != 0;
        
        sendNotificationClickedIntent(intent, cursor, pckg, clazz, isPublicApi, true);
    }
    
    /**
     * Notify the owner of a running download that its notification was clicked.
     * @param intent the broadcast intent sent by the notification manager
     * @param cursor Cursor for reading the download's fields
     * @param pckg see Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE
     * @param clazz see Downloads.Impl.COLUMN_NOTIFICATION_CLASS
     * @param isPublicApi see Downloads.Impl.COLUMN_IS_PUBLIC_API
     * @param isSupportedMultiple 是否支持intent中的multiple参数
     */
    private void sendNotificationClickedIntent(Intent intent, Cursor cursor, String pckg,
            String clazz, boolean isPublicApi, boolean isSupportedMultiple) {
        
        Intent appIntent = null;
        if (isPublicApi) {
            appIntent = new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            appIntent.setPackage(pckg);
        } else { // legacy behavior
            if (clazz == null) {
                return;
            }
            appIntent = new Intent(Downloads.Impl.ACTION_NOTIFICATION_CLICKED);
            appIntent.setClassName(pckg, clazz);
            if (isSupportedMultiple && intent.getBooleanExtra("multiple", true)) {
                appIntent.setData(Downloads.Impl.CONTENT_URI);
            } else {
                long downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.Impl._ID));
                appIntent.setData(
                        ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, downloadId));
            }
        }

        mSystemFacade.sendBroadcast(appIntent);
    }
    
    /**
     * start download service.
     * @param context context.
     */
    private void startService(Context context) {
        context.startService(new Intent(context, DownloadService.class));
    }
}
