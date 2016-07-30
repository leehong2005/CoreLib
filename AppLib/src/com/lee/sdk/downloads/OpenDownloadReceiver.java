/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.lee.sdk.Configuration;
import com.lee.sdk.res.R;

/**
 * This {@link BroadcastReceiver} handles {@link Intent}s to open and delete files downloaded by the
 * Browser.
 */
public class OpenDownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ContentResolver cr = context.getContentResolver();
        Uri data = intent.getData();
        // 下载成功
        if (Configuration.DEBUG) {
            Log.d("OpenDownloadReceiver", "download finished: " + data);
        }

        // 如果接收到的下载uri为空，则没有必要查询数据库
        if (null == data) {
            return;
        }
        Cursor cursor = null;
        try {
            cursor = cr.query(data, new String[] { Downloads.Impl._ID, Downloads.Impl.DATA,
                    Downloads.Impl.COLUMN_MIME_TYPE, Downloads.COLUMN_STATUS, Downloads.Impl.COLUMN_TITLE,
                    Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI }, null, null, null);
            if (cursor.moveToFirst()) {
                final String filename = cursor.getString(cursor.getColumnIndex(Downloads.Impl.DATA));
                final String mimetype = cursor.getString(cursor.getColumnIndex(Downloads.Impl.COLUMN_MIME_TYPE));
                String action = intent.getAction();
                int status = cursor.getInt(cursor.getColumnIndex(Downloads.Impl.COLUMN_STATUS));
                boolean isDownloaded = Downloads.isStatusCompleted(status) && Downloads.isStatusSuccess(status);
                if (Downloads.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                    if (isDownloaded) {

                        Intent launchIntent = new Intent(Intent.ACTION_VIEW);
                        Uri path = Uri.parse(filename);
                        // If there is no scheme, then it must be a file
                        if (path.getScheme() == null) {
                            path = Uri.fromFile(new File(filename));
                        }
                        launchIntent.setDataAndType(path, mimetype);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(launchIntent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(context, R.string.download_no_application_title, Toast.LENGTH_LONG).show();
                        }
                    } else {

                    }
                } else if (Intent.ACTION_DELETE.equals(action)) {
                    if (deleteFile(cr, filename, mimetype)) {
                        cr.delete(data, null, null);
                    }
                } else {
                    // 下载成功
                    if (Configuration.DEBUG) {
                        Log.d("OpenDownloadReceiver", "download finished: " + data);
                    }

                    if (isDownloaded) {
                        int isVisibleInUi = cursor.getInt(cursor
                                .getColumnIndexOrThrow(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI));
                        if (isVisibleInUi != 0) {
                            // 广播到 下载管理界面
                            String columnTitle = cursor.getString(cursor.getColumnIndex(Downloads.Impl.COLUMN_TITLE));
                            // SearchBoxDownloadManager.getInstance(context).sendCompleteMsg(ContentUris.parseId(data),
                            // filename, mimetype, columnTitle);
                        }

                        // if (!checkUpdatePackageDownloaded(context, data, filename)) {
                        // //checkPluginDownloaded(context, data, filename);
                        // }
                    }
                }
            }
        } catch (Exception e) {
            if (Configuration.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Remove the file from the SD card.
     * 
     * @param cr ContentResolver used to delete the file.
     * @param filename Name of the file to delete.
     * @param mimetype Mimetype of the file to delete.
     * @return boolean True on success, false on failure.
     */
    private boolean deleteFile(ContentResolver cr, String filename, String mimetype) {
        Uri uri;
        if (mimetype.startsWith("image")) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mimetype.startsWith("audio")) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (mimetype.startsWith("video")) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = null;
        }
        return (uri != null && cr.delete(uri,
                MediaStore.MediaColumns.DATA + " = " + DatabaseUtils.sqlEscapeString(filename), null) > 0)
                || new File(filename).delete();
    }
}
