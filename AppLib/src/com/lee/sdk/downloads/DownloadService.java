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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.Configuration;
import com.lee.sdk.utils.Utils;


/**
 * Performs the background downloads requested by applications that use the Downloads provider.
 */
public class DownloadService extends Service {
    /** amount of time to wait to connect to MediaScannerService before timing out */
    private static final long WAIT_TIMEOUT = 10 * 1000;

    /** Observer to get notified when the content observer's data changes */
    private DownloadManagerContentObserver mObserver;

    /** Class to handle Notification Manager updates */
    private DownloadNotification mNotifier;

    /**
     * The Service's view of the list of downloads, mapping download IDs to the corresponding info
     * object. This is kept independently from the content provider, and the Service only initiates
     * downloads based on this data, so that it can deal with situation where the data in the
     * content provider changes or disappears.
     */
    private Map<Long, DownloadInfo> mDownloads = new HashMap<Long, DownloadInfo>();

    /**
     * The thread that updates the internal download list from the content
     * provider.
     */
    UpdateThread mUpdateThread;

    /**
     * Whether the internal download list should be updated from the content
     * provider.
     */
    private boolean mPendingUpdate;

    /**
     * The ServiceConnection object that tells us when we're connected to and disconnected from
     * the Media Scanner
     */
    private MyMediaScannerConnection mMediaScannerConnection;
    
    /**
     * MyMediaScannerConnectionClient, will receive the scan complete callback.
     */
    private MyMediaScannerConnectionClient mMediaScannerConnectionClient;

    /**
     * is connecting.
     */
    private boolean mMediaScannerConnecting;

    /**
     * The IPC interface to the Media Scanner
     */
    private Object mMediaScannerService;

    /** SystemFacade */
    SystemFacade mSystemFacade;
    
    /**当前下载线程个数. */
    public static volatile int mCurrentThreadNum = 0;

    /**
     * 停止显示到通知栏的广播
     */
    public static final String ACTION_DOWNLOAD_STOP_NOTIFICATION = "com.baidu.searchbox.download.STOP_NOTIFICATION";
    
    /**
     * 开始显示到通知栏的广播
     */
    public static final String ACTION_DOWNLOAD_START_NOTIFICATION = "com.baidu.searchbox.download.STATRT_NOTIFICATION";
    
    /**
     * 控制UI是否显示到通知栏
     */
    private boolean mNotificationStopped;
    
    /**
     * 通知栏消息控制消息监听
     */
    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_DOWNLOAD_STOP_NOTIFICATION)) {
                mNotificationStopped = true;
                if (null == mSystemFacade) {
                    mSystemFacade = new RealSystemFacade(context);
                }
                // modify by qiaopu 2014-3-27 处理线程同步
//                for (Long id : mDownloads.keySet()) {
//                    DownloadInfo info = mDownloads.get(id);
                
                DownloadInfo[] infos;
                synchronized (mDownloads) {
                    Collection<DownloadInfo> values = mDownloads.values();
                    infos = values.toArray(new DownloadInfo[values.size()]);
                }
                
                for (DownloadInfo info : infos) {
                // end modify
                    
                    if (null != info && info.mStatus != Downloads.Impl.STATUS_SUCCESS) {
                        mSystemFacade.cancelNotification(info.mId);
                    }
                }
            } else if (action.equals(ACTION_DOWNLOAD_START_NOTIFICATION)) {
                mNotificationStopped = false;
            }
        }
    };
    
    /**
     * 注册通知栏控制消息监听
     */
    private void registerNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOAD_STOP_NOTIFICATION);
        filter.addAction(ACTION_DOWNLOAD_START_NOTIFICATION);
        registerReceiver(mNotificationReceiver, filter);
    }
    
    /**
     * 取消通知栏控制消息监听
     */
    private void unregisterNotificationReceiver() {
        unregisterReceiver(mNotificationReceiver);
    }
    
    /**
     * Receives notifications when the data in the content provider changes
     */
    private class DownloadManagerContentObserver extends ContentObserver {

        /**
         * default constructor.
         */
        public DownloadManagerContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(final boolean selfChange) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "Service ContentObserver received notification");
            }
            updateFromProvider();
        }

    }

    /**
     * Gets called back when the connection to the media
     * scanner is established or lost.
     */
    public class MyMediaScannerConnection extends MediaScannerConnection {
        
        /**
         * default constructor
         * @param context MyMediaScannerConnection
         * @param client MediaScannerConnectionClient
         */
        public MyMediaScannerConnection(Context context,
                MediaScannerConnectionClient client) {
            super(context, client);
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            super.onServiceConnected(className, service);
            
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "Connected to Media Scanner");
            }
            synchronized (DownloadService.this) {
                try {
                    mMediaScannerConnecting = false;
                    mMediaScannerService = this;
                    if (mMediaScannerService != null) {
                        updateFromProvider();
                    }
                } finally {
                    // notify anyone waiting on successful connection to MediaService
                    DownloadService.this.notifyAll();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            super.onServiceDisconnected(className);
            
            try {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "Disconnected from Media Scanner");
                }
            } finally {
                synchronized (DownloadService.this) {
                    mMediaScannerService = null;
                    mMediaScannerConnecting = false;
                    // notify anyone waiting on disconnect from MediaService
                    DownloadService.this.notifyAll();
                }
            }
        }

        /**
         * disconnect.
         */
        public void disconnectMediaScanner() {
            synchronized (DownloadService.this) {
                mMediaScannerConnecting = false;
                if (mMediaScannerService != null) {
                    mMediaScannerService = null;
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "Disconnecting from Media Scanner");
                    }
                    try {
                        //unbindService(this);
                        disconnect();
                    } catch (IllegalArgumentException ex) {
                        Log.w(Constants.TAG, "unbindService failed: " + ex);
                    } finally {
                        // notify anyone waiting on unsuccessful connection to MediaService
                        DownloadService.this.notifyAll();
                    }
                }
            }
        }

    }
    
    /**
     * MyMediaScannerConnectionClient.
     */
    static class MyMediaScannerConnectionClient implements MediaScannerConnectionClient {
        /** OnScanCompletedListener. */
        OnScanCompletedListener listener;
        
        @Override
        public void onMediaScannerConnected() {
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (listener != null) {
                listener.onScanCompleted(path, uri);
            }
        }
        
    }

    /**
     * Returns an IBinder instance when someone wants to connect to this
     * service. Binding to this service is not allowed.
     * @param i intent.
     * @return UnsupportedOperationException
     * @throws UnsupportedOperationException
     */
    @Override
    public IBinder onBind(Intent i) {
        throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
    }

    /**
     * Initializes the service when it is first created
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Service onCreate");
        }
        //处理downloadreceiver被disable的情况
        dealComponetsDisabled();
        //调用DownloadServiceCallback，实现一些初始化操作
        if (this.getApplicationContext() instanceof DownloadServiceCallback) {
            ((DownloadServiceCallback) this.getApplicationContext()).onDownloadServiceCreate();
        }
        if (mSystemFacade == null) {
            mSystemFacade = new RealSystemFacade(this);
        }

        mObserver = new DownloadManagerContentObserver();
        getContentResolver().registerContentObserver(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
                true, mObserver);

        synchronized (DownloadService.this) {
            mMediaScannerService = null;
            mMediaScannerConnecting = false; 
        }
        
        mMediaScannerConnectionClient = new MyMediaScannerConnectionClient();
        mMediaScannerConnection = new MyMediaScannerConnection(this, mMediaScannerConnectionClient);

        mNotifier = new DownloadNotification(this, mSystemFacade);
        // 如果下载启动时，取消所有的notification,则会导致更新提示的notification也消失
        // mSystemFacade.cancelAllNotifications();

        registerNotificationReceiver();
        
        updateFromProvider();
        
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
        updateFromProvider();
    }
    
    /**
     * Cleans up when the service is destroyed
     */
    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Service onDestroy");
        }
        unregisterNotificationReceiver();
        super.onDestroy();
    }

    /**
     * Parses data from the content provider into private array
     */
    private void updateFromProvider() {
        synchronized (this) {
            mPendingUpdate = true;
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread();
                mSystemFacade.startThread(mUpdateThread);
            }
        }
    }
    
    /**
     * used to update.
     */
    private class UpdateThread extends Thread {
        /**
         * constructor.
         */
        public UpdateThread() {
            super("Download Service");
        }
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            trimDatabase();
            removeSpuriousFiles();

            boolean keepService = false;
            // for each update from the database, remember which download is
            // supposed to get restarted soonest in the future
            long wakeUp = Long.MAX_VALUE;
            for (;;) {
                synchronized (DownloadService.this) {
                    if (mUpdateThread != this) {
                        throw new IllegalStateException(
                                "multiple UpdateThreads in DownloadService");
                    }
                    if (!mPendingUpdate) {
                        mUpdateThread = null;
                        if (!keepService) {
                            stopSelf();
                        }
                        if (wakeUp != Long.MAX_VALUE) {
                            scheduleAlarm(wakeUp);
                        }
                        return;
                    }
                    mPendingUpdate = false;
                }

                long now = mSystemFacade.currentTimeMillis();
                boolean mustScan = false;
                keepService = false;
                wakeUp = Long.MAX_VALUE;
                Set<Long> idsNoLongerInDatabase = new HashSet<Long>(mDownloads.keySet());

                Cursor cursor = getContentResolver().query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
                        null, null, null, null);
                if (cursor == null) {
                    continue;
                }
                try {
                    DownloadInfo.Reader reader =
                            new DownloadInfo.Reader(getContentResolver(), cursor);
                    int idColumn = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);

                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        idsNoLongerInDatabase.remove(id);
                        DownloadInfo info = mDownloads.get(id);
                        if (info != null) {
                            updateDownload(reader, info, now);
                        } else {
                            info = insertDownload(reader, now);
                        }

                        if (info.shouldScanFile() && !scanFile(info, true, false)) {
                            mustScan = true;
                            keepService = true;
                        }
                        if (info.hasCompletionNotification()) {
                            keepService = true;
                        }
                        long next = info.nextAction(now);
                        if (next == 0) {
                            keepService = true;
                        } else if (next > 0 && next < wakeUp) {
                            // begin caohaitao 20111130
                            // fix APPSEARCH-53 【软件搜索】【下载】外部存储空间不足时，下载失败后，偶尔出现系统通知栏中的下载进度不会及时消失
                            // 由于重试的时候 stopSelf 退出的话， observer无法监听，导致删除的时候无法监听到删除。
                            keepService = true;
                            // end caohaitao 20111130
                            
                            wakeUp = next;
                        }
                    }
                } finally {
                    cursor.close();
                }

                for (Long id : idsNoLongerInDatabase) {
                    deleteDownload(id);
                }

                // is there a need to start the DownloadService? yes, if there are rows to be
                // deleted.
                if (!mustScan) {
                    for (DownloadInfo info : mDownloads.values()) {
                        if (info.mDeleted && TextUtils.isEmpty(info.mMediaProviderUri)) {
                            mustScan = true;
                            keepService = true;
                            break;
                        }
                    }
                }
                
                if (!mNotificationStopped) {
                    mNotifier.updateNotification(mDownloads.values());
                }
                
                if (mustScan) {
                    bindMediaScanner();
                } else {
                    mMediaScannerConnection.disconnectMediaScanner();
                }

                // look for all rows with deleted flag set and delete the rows from the database
                // permanently
                for (DownloadInfo info : mDownloads.values()) {
                    if (info.mDeleted) {
                        // this row is to be deleted from the database. but does it have
                        // mediaProviderUri?
                        if (TextUtils.isEmpty(info.mMediaProviderUri)) {
                            if (info.shouldScanFile()) {
                                // initiate rescan of the file to - which will populate
                                // mediaProviderUri column in this row
                                if (!scanFile(info, false, true)) {
                                    throw new IllegalStateException("scanFile failed!");
                                }
                            } else {
                                // this file should NOT be scanned. delete the file.
                                Helpers.deleteFile(getContentResolver(), info.mId, info.mFileName,
                                        info.mMimeType);
                            }
                        } else {
                            // yes it has mediaProviderUri column already filled in.
                            // delete it from MediaProvider database and then from downloads table
                            // in DownProvider database (the order of deletion is important).
                            getContentResolver().delete(Uri.parse(info.mMediaProviderUri), null,
                                    null);
                            // the following deletes the file and then deletes it from downloads db
                            Helpers.deleteFile(getContentResolver(), info.mId, info.mFileName,
                                    info.mMimeType);
                        }
                    }
                }
            }
        }
        /**
         * connect the media scaner service.
         */
        private void bindMediaScanner() {
            synchronized (DownloadService.this) {
                if (!mMediaScannerConnecting) {
                    mMediaScannerConnecting = true;
                    //bindService(intent, mMediaScannerConnection, BIND_AUTO_CREATE);
                    mMediaScannerConnection.connect();
                }
            }
        }
        /**
         * scheduleAlarm to restart the service.
         * @param wakeUp when
         */
        private void scheduleAlarm(long wakeUp) {
            AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarms == null) {
                if (Configuration.DEBUG) {
                    Log.e(Constants.TAG, "couldn't get alarm manager");
                }
                return;
            }

            if (Constants.LOGV) {
                Log.v(Constants.TAG, "scheduling retry in " + wakeUp + "ms");
            }

            Intent intent = new Intent(Constants.ACTION_RETRY);
            intent.setClassName(getPackageName(),
                    DownloadReceiver.class.getName());
            alarms.set(
                    AlarmManager.RTC_WAKEUP,
                    mSystemFacade.currentTimeMillis() + wakeUp,
                    PendingIntent.getBroadcast(DownloadService.this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT));
        }
    }

    /**
     * Removes files that may have been left behind in the cache directory
     */
    private void removeSpuriousFiles() {
        File[] files = Environment.getDownloadCacheDirectory().listFiles();
        if (files == null) {
            // The cache folder doesn't appear to exist (this is likely the case
            // when running the simulator).
            return;
        }
        HashSet<String> fileSet = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(Constants.KNOWN_SPURIOUS_FILENAME)) {
                continue;
            }
            if (files[i].getName().equalsIgnoreCase(Constants.RECOVERY_DIRECTORY)) {
                continue;
            }
            fileSet.add(files[i].getPath());
        }

        Cursor cursor = getContentResolver().query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
                new String[] { Downloads.Impl.DATA }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    fileSet.remove(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Iterator<String> iterator = fileSet.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "deleting spurious file " + filename);
            }
            File file = new File(filename);
            boolean deleted = file.delete();
            if (!deleted) {
                if (Configuration.DEBUG) {
                    Log.w(Constants.TAG, "removeSpuriousFiles delete file failed");
                }
            }
        }
    }

    /**
     * Drops old rows from the database to prevent it from growing too large
     */
    private void trimDatabase() {
        Cursor cursor = getContentResolver().query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
                new String[] { Downloads.Impl._ID },
                Downloads.Impl.COLUMN_STATUS + " >= '200'", null,
                Downloads.Impl.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            // This isn't good - if we can't do basic queries in our database, nothing's gonna work
            if (Configuration.DEBUG) {
                Log.e(Constants.TAG, "null cursor in trimDatabase");
            }
            return;
        }
        if (cursor.moveToFirst()) {
            int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
            int columnId = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);
            while (numDelete > 0) {
                Uri downloadUri = ContentUris.withAppendedId(
                        Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, cursor.getLong(columnId));
                getContentResolver().delete(downloadUri, null, null);
                //TODO: 
                //删除需要更新已下载界面数量
                if (!cursor.moveToNext()) {
                    break;
                }
                numDelete--;
            }
        }
        cursor.close();
    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     * @param reader reader
     * @param now now time
     * @return DownloadInfo
     */
    private DownloadInfo insertDownload(DownloadInfo.Reader reader, long now) {
        DownloadInfo info = reader.newDownloadInfo(this, mSystemFacade);
        
        synchronized (mDownloads) {
            mDownloads.put(info.mId, info);
        }

        if (Constants.LOGVV) {
            info.logVerboseInfo();
        }

        info.startIfReady(now);
        return info;
    }

    /**
     * Updates the local copy of the info about a download.
     * @param reader reader
     * @param now now time
     * @param info DownloadInfo
     */
    private void updateDownload(DownloadInfo.Reader reader, DownloadInfo info, long now) {
        int oldVisibility = info.mVisibility;
        int oldStatus = info.mStatus;

        reader.updateFromDatabase(info);

        boolean lostVisibility =
                oldVisibility == Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && info.mVisibility != Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && Downloads.Impl.isStatusCompleted(info.mStatus);
        boolean justCompleted =
                !Downloads.Impl.isStatusCompleted(oldStatus)
                && Downloads.Impl.isStatusCompleted(info.mStatus);
        if (lostVisibility || justCompleted) {
            mSystemFacade.cancelNotification(info.mId);
        }

        info.startIfReady(now);
    }

    /**
     * Removes the local copy of the info about a download.
     * @param id id.
     */
    private void deleteDownload(long id) {
        DownloadInfo info = mDownloads.get(id);
        if (info.shouldScanFile()) {
            scanFile(info, false, false);
        }
        if (info.mStatus == Downloads.Impl.STATUS_RUNNING) {
            info.mStatus = Downloads.Impl.STATUS_CANCELED;
        }
        if (info.mDestination != Downloads.Impl.DESTINATION_EXTERNAL && info.mFileName != null) {
            File file = new File(info.mFileName);
            boolean deleted = file.delete();
            if (!deleted) {
                if (Configuration.DEBUG) {
                    Log.w(Constants.TAG, "deleteDownload delete file failed");
                }
            }
        }
        mSystemFacade.cancelNotification(info.mId);
        
        synchronized (mDownloads) {
            mDownloads.remove(info.mId);
        }
    }

    /**
     * Attempts to scan the file if necessary.
     * @param info downloadinfo
     * @param updateDatabase whether update the database
     * @param deleteFile whether delete the file.
     * @return true if the file has been properly scanned.
     */
    private boolean scanFile(DownloadInfo info, final boolean updateDatabase,
            final boolean deleteFile) {
        
        synchronized (this) {
           
           if (mMediaScannerService == null) {
                // not bound to mediaservice. but if in the process of connecting to it, wait until
                // connection is resolved
                while (mMediaScannerConnecting) {
                    if (Configuration.DEBUG) {
                        Log.d(Constants.TAG, "waiting for mMediaScannerService service: ");
                    }
                    try {
                        this.wait(WAIT_TIMEOUT);
                    } catch (InterruptedException e1) {
                        throw new IllegalStateException("wait interrupted");
                    }
                }
            }
            // do we have mediaservice?
            if (mMediaScannerService == null) {
                // no available MediaService And not even in the process of connecting to it
                return false;
            }
            
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "Scanning file " + info.mFileName);
            }
            try {
                final Uri key = info.getAllDownloadsUri();
                final String mimeType = info.mMimeType;
                final ContentResolver resolver = getContentResolver();
                final long id = info.mId;
                final boolean scaned = info.mMediaScanned;
                
                OnScanCompletedListener listener = new OnScanCompletedListener() {
                    
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (updateDatabase) {
                            // file is scanned and mediaprovider returned uri. store it in downloads
                            // table (i.e., update this downloaded file's row)
                            boolean changed = false;
                            ContentValues values = new ContentValues();
                            if (!scaned) {
                                values.put(Constants.MEDIA_SCANNED, 1);
                                changed = true;
                            }
                            if (uri != null) {
                                values.put(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI,
                                                uri.toString());
                                changed = true;
                            }
                            if (changed) {
                                int result = getContentResolver().update(key, values, null, null);
                                if (result == 0) {
                                    if (Constants.LOGV) {
                                        Log.v(Constants.TAG, "Scanning file update failed " + key);
                                    }
                                }
                            }
                        } else if (deleteFile) {
                            if (uri != null) {
                                // use the Uri returned to delete it from the MediaProvider
                                getContentResolver().delete(uri, null, null);
                            }
                            // delete the file and delete its row from the downloads db
                            Helpers.deleteFile(resolver, id, path, mimeType);
                        }
                        
                        mMediaScannerConnectionClient.listener = null;
                    }
                };
                
                mMediaScannerConnectionClient.listener = listener;
                mMediaScannerConnection.scanFile(info.mFileName, info.mMimeType);
                
/*               

                String[] paths = new String[]{info.mFileName};
                String[] mimeTypes = new String[]{info.mMimeType};
 
                MediaScannerConnection.scanFile(this, paths, mimeTypes, new OnScanCompletedListener() {
                    
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (updateDatabase) {
                            // file is scanned and mediaprovider returned uri. store it in downloads
                            // table (i.e., update this downloaded file's row)
                            ContentValues values = new ContentValues();
                            values.put(Constants.MEDIA_SCANNED, 1);
                            if (uri != null) {
                                values.put(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI,
                                                uri.toString());
                            }
                            getContentResolver().update(key, values, null, null);
                        } else if (deleteFile) {
                            if (uri != null) {
                                // use the Uri returned to delete it from the MediaProvider
                                getContentResolver().delete(uri, null, null);
                            }
                            // delete the file and delete its row from the downloads db
                            Helpers.deleteFile(resolver, id, path, mimeType);
                        }
                    }
                });*/

                return true;
            } catch (Exception e) {
                Log.w(Constants.TAG, "Failed to scan file " + info.mFileName);
                return false;
            }
        }
    }
    /**
     * Interface for notifying clients of the result of scanning a
     * requested media file.
     */
    public interface OnScanCompletedListener {
        /**
         * Called to notify the client when the media scanner has finished
         * scanning a file.
         * @param path the path to the file that has been scanned.
         * @param uri the Uri for the file if the scanning operation succeeded
         * and the file was added to the media database, or null if scanning failed.
         */
        void onScanCompleted(String path, Uri uri);
    }
    
    /**
     * 检测下载模块组件是否可用，如果不可用，则重新set enabled
     */
    private void dealComponetsDisabled() {
        PackageManager pm = getPackageManager();
        //check downloadreceiver
        if (!Utils.isComponentEnable(getApplicationContext(), DownloadReceiver.class.getName())) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "enable the disabled downloadreceiver");
            }
            ComponentName cn = new ComponentName(getPackageName(), DownloadReceiver.class.getName());
            pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        //check openddownloadreceiver
        if (!Utils.isComponentEnable(getApplicationContext(), OpenDownloadReceiver.class.getName())) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "enable the disabled OpenDownloadReceiver");
            }
            ComponentName cn = new ComponentName(getPackageName(), OpenDownloadReceiver.class.getName());
            pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}
