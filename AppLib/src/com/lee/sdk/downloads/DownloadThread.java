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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.ContentValues;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.lee.sdk.Configuration;
import com.lee.sdk.downloads.Downloads.Impl;
import com.lee.sdk.res.R;

/**
 * Runs an actual download
 */
public class DownloadThread extends Thread {

    private Context mContext;
    private DownloadInfo mInfo;
    private SystemFacade mSystemFacade;

    public DownloadThread(Context context, SystemFacade systemFacade, DownloadInfo info) {
        mContext = context;
        mSystemFacade = systemFacade;
        mInfo = info;
        setName("DownloadThread:" + info.mUri);
    }

    /**
     * Returns the user agent provided by the initiating app, or use the default one
     * 
     * @return see the description.
     */
    private String userAgent() {
        String userAgent = mInfo.mUserAgent;

        if (userAgent == null) {
            userAgent = Constants.DEFAULT_USER_AGENT;
        }
        return userAgent;
    }

    /**
     * State for the entire run() method.
     */
    private static class State {
        /** file name. */
        public String mFilename;
        /** output stream. */
        public FileOutputStream mStream;
        /** mime type. */
        public String mMimeType;
        /** retry count. */
        public boolean mCountRetry = false;
        /** mRetryAfter */
        public int mRetryAfter = 0;
        /** mRedirectCount */
        public int mRedirectCount = 0;
        /** mNewUri */
        public String mNewUri;
        /** mGotData */
        public boolean mGotData = false;
        /** mRequestUri */
        public String mRequestUri;

        /**
         * constructor.
         * 
         * @param info downloadinfo
         */
        public State(DownloadInfo info) {
            mMimeType = sanitizeMimeType(info.mMimeType);
            mRedirectCount = info.mRedirectCount;
            mRequestUri = info.mUri;
            mFilename = info.mFileName;
        }
    }

    /**
     * State within executeDownload()
     */
    private static class InnerState {
        /** current download size. */
        public int mBytesSoFar = 0;
        /** header etag. */
        public String mHeaderETag;
        /** mContinuingDownload. */
        public boolean mContinuingDownload = false;
        /** mHeaderContentLength. */
        public String mHeaderContentLength;
        /** mHeaderContentDisposition */
        public String mHeaderContentDisposition;
        /** mHeaderContentLocation */
        public String mHeaderContentLocation;
        /** mBytesNotified */
        public int mBytesNotified = 0;
        /** mTimeLastNotification */
        public long mTimeLastNotification = 0;
    }

    /**
     * Raised from methods called by run() to indicate that the current request should be stopped
     * immediately.
     * 
     * Note the message passed to this exception will be logged and therefore must be guaranteed not
     * to contain any PII, meaning it generally can't include any information about the request URI,
     * headers, or destination filename.
     */
    private static class StopRequest extends Throwable {
        /** final status. */
        public int mFinalStatus;

        /**
         * stop he current request.
         * 
         * @param finalStatus finalStatus
         * @param message message
         */
        public StopRequest(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        /**
         * stop the current request.
         * 
         * @param finalStatus finalStatus
         * @param message message
         * @param throwable throwable
         */
        public StopRequest(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }

    /**
     * Raised from methods called by executeDownload() to indicate that the download should be
     * retried immediately.
     */
    private static class RetryDownload extends Throwable {

    }

    /**
     * Executes the download in a separate thread
     */
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        State state = new State(mInfo);
        AndroidHttpClient client = null;
        PowerManager.WakeLock wakeLock = null;
        int finalStatus = Downloads.Impl.STATUS_UNKNOWN_ERROR;

        try {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
            wakeLock.acquire();

            if (Constants.LOGV) {
                Log.v(Constants.TAG, "initiating download for " + mInfo.mUri);
            }

            client = AndroidHttpClient.newInstance(userAgent(), mContext);
            boolean finished = false;
            while (!finished) {
                if (Constants.LOGV) {
                    Log.i(Constants.TAG, "Initiating request for download " + mInfo.mId);
                }
                HttpGet request = new HttpGet(state.mRequestUri);
                try {
                    executeDownload(state, client, request);
                    finished = true;
                } catch (RetryDownload exc) {
                    // fall through
                    exc.printStackTrace();
                } finally {
                    request.abort();
                    request = null;
                }
            }

            if (Constants.LOGV) {
                Log.v(Constants.TAG, "download completed for " + mInfo.mUri);
            }
            finalizeDestinationFile(state);
            finalStatus = Downloads.Impl.STATUS_SUCCESS;
        } catch (StopRequest error) {
            // remove the cause before printing, in case it contains PII
            if (Constants.LOGV) {
                Log.w(Constants.TAG, "Aborting request for download " + mInfo.mId + ": " + error.getMessage());
            }
            finalStatus = error.mFinalStatus;

            displayMsg(finalStatus, error.getMessage());

            // fall through to finally block
        } catch (Throwable ex) { // sometimes the socket code throws unchecked exceptions
            Log.w(Constants.TAG, "Exception for id " + mInfo.mId + ": " + ex);
            finalStatus = Downloads.Impl.STATUS_UNKNOWN_ERROR;
            displayMsg(finalStatus, ex.getMessage());
            // falls through to the code that reports an error
        } finally {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
            if (client != null) {
                client.close();
                client = null;
            }
            cleanupDestination(state, finalStatus);
            notifyDownloadCompleted(finalStatus, state.mCountRetry, state.mRetryAfter, state.mRedirectCount,
                    state.mGotData, state.mFilename, state.mNewUri, state.mMimeType);

            --DownloadService.mCurrentThreadNum;
            if (!Downloads.Impl.isStatusCompleted(finalStatus)) {
                mInfo.mHasActiveThread = false;
            }
        }
    }

    /**
     * 显示错误提示。
     * 
     * @param status 状态，参考 Downloads.status_*
     * @param msg 信息
     */
    private void displayMsg(final int status, final String msg) {
        // 显示下载错误提示
        // Intent intent = new Intent();
        // intent.setClass(mContext, DownloadMsgActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.putExtra(DownloadMsgActivity.EXTRA_STATUS, status);
        // intent.putExtra(DownloadMsgActivity.EXTRA_MSG, msg);
        //
        // mContext.startActivity(intent);
        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                showMsg(status, msg);
            }
        });
    }

    /**
     * 显示消息。
     * 
     * @param status 状态，参考 Downloads.STATUS_XXX
     * @param msg 默认消息
     */
    private void showMsg(int status, String msg) {
        String errorMsg = getErrorMessage(status);
        if (!TextUtils.isEmpty(errorMsg)) {
            Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * the appropriate error message for the failed download pointed to by cursor.
     * 
     * @param status status 状态，参考 Downloads.STATUS_XXX
     * @return the appropriate error message for the failed download pointed to by cursor
     */
    private String getErrorMessage(int status) {
        switch (status) {

        case Downloads.STATUS_INSUFFICIENT_SPACE_ERROR:
            // if (isOnExternalStorage(cursor)) {
            return mContext.getString(R.string.dialog_insufficient_space_on_external);
            // } else {
            // return getString(R.string.dialog_insufficient_space_on_cache);
            // }

        case Downloads.STATUS_DEVICE_NOT_FOUND_ERROR:
            return mContext.getString(R.string.dialog_media_not_found);

        default:
            // return getString(R.string.dialog_failed_body);
            return null;
        }
    }

    /**
     * Fully execute a single download request - setup and send the request, handle the response,
     * and transfer the data to the destination file.
     * 
     * @param state state
     * @param client client
     * @param request request
     * @throws StopRequest StopRequest
     * @throws RetryDownload RetryDownload
     */
    private void executeDownload(State state, AndroidHttpClient client, HttpGet request) throws StopRequest,
            RetryDownload {
        InnerState innerState = new InnerState();

        byte[] data = new byte[Constants.BUFFER_SIZE];

        setupDestinationFile(state, innerState);
        addRequestHeaders(innerState, request);

        // check just before sending the request to avoid using an invalid connection at all
        checkConnectivity(state);

        if (Constants.LOGV) {
            Log.i(Constants.TAG, "sendRequest for begin: " + mInfo.mId);
        }
        HttpResponse response = sendRequest(state, client, request);
        if (Constants.LOGV) {
            Log.i(Constants.TAG, "sendRequest for end: " + mInfo.mId);
        }
        checkPausedOrCanceled(state);

        handleExceptionalStatus(state, innerState, response);

        if (Constants.LOGV) {
            Log.v(Constants.TAG, "received response for " + mInfo.mUri);
        }

        processResponseHeaders(state, innerState, response);
        InputStream entityStream = openResponseEntity(state, response);
        mInfo.updateStatus(Impl.STATUS_RUNNING);
        if (Constants.LOGV) {
            Log.i(Constants.TAG, "executeDownload.transferData() set status: STATUS_RUNNING");
        }
        transferData(state, innerState, data, entityStream);
    }

    /**
     * Check if current connectivity is valid for this request.
     * 
     * @param state state
     * @throws StopRequest StopRequest
     */
    private void checkConnectivity(State state) throws StopRequest {
        int networkUsable = mInfo.checkCanUseNetwork();
        if (networkUsable != DownloadInfo.NETWORK_OK) {
            int status = Downloads.Impl.STATUS_WAITING_FOR_NETWORK;
            if (networkUsable == DownloadInfo.NETWORK_UNUSABLE_DUE_TO_SIZE) {
                status = Downloads.Impl.STATUS_QUEUED_FOR_WIFI;
                mInfo.notifyPauseDueToSize(true);
            } else if (networkUsable == DownloadInfo.NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE) {
                status = Downloads.Impl.STATUS_QUEUED_FOR_WIFI;
                mInfo.notifyPauseDueToSize(false);
            }
            throw new StopRequest(status, mInfo.getLogMessageForNetworkError(networkUsable));
        }
    }

    /**
     * Transfer as much data as possible from the HTTP response to the destination file.
     * 
     * @param state state.
     * @param innerState inner state
     * @param data buffer to use to read data
     * @param entityStream stream for reading the HTTP response entity
     * @throws StopRequest StopRequest
     */
    private void transferData(State state, InnerState innerState, byte[] data, InputStream entityStream)
            throws StopRequest {
        for (;;) {
            int bytesRead = readFromResponse(state, innerState, data, entityStream);
            if (bytesRead == -1) { // success, end of stream already reached
                handleEndOfStream(state, innerState);
                return;
            }

            state.mGotData = true;
            writeDataToDestination(state, data, bytesRead);
            innerState.mBytesSoFar += bytesRead;
            reportProgress(state, innerState);
            if (Constants.LOGV) {
                Log.d(Constants.TAG, "downloaded " + innerState.mBytesSoFar + " for " + mInfo.mUri);
            }

            checkPausedOrCanceled(state);
        }
    }

    /**
     * Called after a successful completion to take any necessary action on the downloaded file.
     * 
     * @param state state
     * @throws StopRequest StopRequest
     */
    private void finalizeDestinationFile(State state) throws StopRequest {
        if (isDrmFile(state)) {
            // transferToDrm(state); // delete by caohaitao
            if (Configuration.DEBUG) {
                Log.e("DownloadThread", "finalizeDestinationFile drm file failed.");
            }
        } else {
            // make sure the file is readable
            // FileUtils.setPermissions(state.mFilename, 0644, -1, -1); // delete by caohaitao
            syncDestination(state);
        }
    }

    /**
     * Called just before the thread finishes, regardless of status, to take any necessary action on
     * the downloaded file.
     * 
     * @param state state
     * @param finalStatus final status.
     */
    private void cleanupDestination(State state, int finalStatus) {
        closeDestination(state);
        // modify by dongqi,防止删除暂停状态的文件
        if (state.mFilename != null && finalStatus == Downloads.Impl.STATUS_CANCELED
        /* Downloads.Impl.isStatusError(finalStatus) */) {
            File file = new File(state.mFilename);
            boolean deleted = file.delete();
            if (!deleted) {
                if (Configuration.DEBUG) {
                    Log.w(Constants.TAG, "cleanupDestination delete file failed");
                }
            }
            state.mFilename = null;
        }
    }

    /**
     * Sync the destination file to storage.
     * 
     * @param state state
     */
    private void syncDestination(State state) {
        FileOutputStream downloadedFileStream = null;
        try {
            downloadedFileStream = new FileOutputStream(state.mFilename, true);
            downloadedFileStream.getFD().sync();
        } catch (FileNotFoundException ex) {
            Log.w(Constants.TAG, "file " + state.mFilename + " not found: " + ex);
        } catch (SyncFailedException ex) {
            Log.w(Constants.TAG, "file " + state.mFilename + " sync failed: " + ex);
        } catch (IOException ex) {
            Log.w(Constants.TAG, "IOException trying to sync " + state.mFilename + ": " + ex);
        } catch (RuntimeException ex) {
            Log.w(Constants.TAG, "exception while syncing file: ", ex);
        } finally {
            if (downloadedFileStream != null) {
                try {
                    downloadedFileStream.close();
                } catch (IOException ex) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex);
                } catch (RuntimeException ex) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex);
                }
            }
        }
    }

    /**
     * true if the current download is a DRM file
     * 
     * @param state state
     * @return true if the current download is a DRM file
     */
    private boolean isDrmFile(State state) {
        return Constants.MIMETYPE_DRM_MESSAGE.equalsIgnoreCase(state.mMimeType);
    }

    /**
     * Transfer the downloaded destination file to the DRM store.
     * 
     * @param state state
     * @throws StopRequest StopRequest
     */
    private void transferToDrm(State state) throws StopRequest {
        // delete by caohaitao
        /*
         * File file = new File(state.mFilename); Intent item =
         * DrmStore.addDrmFile(mContext.getContentResolver(), file, null); file.delete();
         * 
         * if (item == null) { throw new StopRequest(Downloads.Impl.STATUS_UNKNOWN_ERROR,
         * "unable to add file to DrmProvider"); } else { state.mFilename = item.getDataString();
         * state.mMimeType = item.getType(); }
         */
    }

    /**
     * Close the destination output stream.
     * 
     * @param state state
     */
    private void closeDestination(State state) {
        try {
            // close the file
            if (state.mStream != null) {
                state.mStream.close();
                state.mStream = null;
            }
        } catch (IOException ex) {
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "exception when closing the file after download : " + ex);
            }
            // nothing can really be done if the file can't be closed
        }
    }

    /**
     * Check if the download has been paused or canceled, stopping the request appropriately if it
     * has been.
     * 
     * @param state state
     * @throws StopRequest StopRequest
     */
    private void checkPausedOrCanceled(State state) throws StopRequest {
        synchronized (mInfo) {
            if (mInfo.mControl == Downloads.Impl.CONTROL_PAUSED) {
                throw new StopRequest(Downloads.Impl.STATUS_PAUSED_BY_APP, "download paused by owner");
            }
        }
        if (mInfo.mStatus == Downloads.Impl.STATUS_CANCELED) {
            throw new StopRequest(Downloads.Impl.STATUS_CANCELED, "download canceled");
        }
    }

    /**
     * Report download progress through the database if necessary.
     * 
     * @param state state
     * @param innerState innerState
     */
    private void reportProgress(State state, InnerState innerState) {
        long now = mSystemFacade.currentTimeMillis();
        if (innerState.mBytesSoFar - innerState.mBytesNotified > Constants.MIN_PROGRESS_STEP
                && now - innerState.mTimeLastNotification > Constants.MIN_PROGRESS_TIME) {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, innerState.mBytesSoFar);
            mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
            innerState.mBytesNotified = innerState.mBytesSoFar;
            innerState.mTimeLastNotification = now;
        }
    }

    /**
     * Write a data buffer to the destination file.
     * 
     * @param state state
     * @param data buffer containing the data to write
     * @param bytesRead how many bytes to write from the buffer
     * @throws StopRequest StopRequest
     */
    private void writeDataToDestination(State state, byte[] data, int bytesRead) throws StopRequest {
        for (;;) {
            try {
                if (state.mStream == null) {
                    state.mStream = new FileOutputStream(state.mFilename, true);
                }
                state.mStream.write(data, 0, bytesRead);
                if (mInfo.mDestination == Downloads.Impl.DESTINATION_EXTERNAL && !isDrmFile(state)) {
                    closeDestination(state);
                }
                return;
            } catch (IOException ex) {
                if (mInfo.isOnCache()) {
                    if (Helpers.discardPurgeableFiles(mContext, Constants.BUFFER_SIZE)) {
                        continue;
                    }
                } else if (!Helpers.isExternalMediaMounted()) {
                    throw new StopRequest(Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR,
                            "external media not mounted while writing destination file");
                }

                long availableBytes = Helpers.getAvailableBytes(Helpers.getFilesystemRoot(state.mFilename));
                if (availableBytes < bytesRead) {
                    // modify by dognqi 修改提示文案
                    // since @20130424
                    throw new StopRequest(Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                            mContext.getString(R.string.download_noenough_space), ex);
                }
                throw new StopRequest(Downloads.Impl.STATUS_FILE_ERROR, "while writing destination file: "
                        + ex.toString(), ex);
            }
        }
    }

    /**
     * Called when we've reached the end of the HTTP response stream, to update the database and
     * check for consistency.
     * 
     * @param state state
     * @param innerState innerState
     * @throws StopRequest StopRequest
     */
    private void handleEndOfStream(State state, InnerState innerState) throws StopRequest {
        // add by liuqingbiao 为软件搜索添加
        // 增加了针对下载类型的判断，在开始下载前，判断是否与之前写入数据库中的mimetype一致，如果是指定下载文件，则判断是否下载正确
        // 主要是为了解决下载准入页面问题
        // equalsIgnoreCase字符串判断调换解决可能存在的空指针
        if (Constants.MIMETYPE_APK.equalsIgnoreCase(state.mMimeType) && state.mFilename != null) {
            if (!FileVerifier.isAPK(state.mFilename)) {
                throw new StopRequest(Downloads.Impl.STATUS_FILE_ERROR, "下载文件内容错误：" + state);
            }
        } // end
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, innerState.mBytesSoFar);
        if (innerState.mHeaderContentLength == null) {
            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, innerState.mBytesSoFar);
        }
        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);

        boolean lengthMismatched = (innerState.mHeaderContentLength != null)
                && (innerState.mBytesSoFar != Integer.parseInt(innerState.mHeaderContentLength));
        if (lengthMismatched) {
            if (cannotResume(innerState)) {
                throw new StopRequest(Downloads.Impl.STATUS_CANNOT_RESUME, "mismatched content length");
            } else {
                throw new StopRequest(getFinalStatusForHttpError(state), "closed socket before end of file");
            }
        }
    }

    /***
     * can not resume ?
     * 
     * @param innerState innerState
     * @return not return true
     */
    private boolean cannotResume(InnerState innerState) {
        return innerState.mBytesSoFar > 0 && !mInfo.mNoIntegrity && innerState.mHeaderETag == null;
    }

    /**
     * Read some data from the HTTP response stream, handling I/O errors.
     * 
     * @param state state
     * @param innerState innerState
     * @param data buffer to use to read data
     * @param entityStream stream for reading the HTTP response entity
     * @return the number of bytes actually read or -1 if the end of the stream has been reached
     * @throws StopRequest StopRequest
     */
    private int readFromResponse(State state, InnerState innerState, byte[] data, InputStream entityStream)
            throws StopRequest {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            logNetworkState();
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, innerState.mBytesSoFar);
            mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
            if (cannotResume(innerState)) {
                String message = "while reading response: " + ex.toString()
                        + ", can't resume interrupted download with no ETag";
                throw new StopRequest(Downloads.Impl.STATUS_CANNOT_RESUME, message, ex);
            } else {
                throw new StopRequest(getFinalStatusForHttpError(state), "while reading response: " + ex.toString(), ex);
            }
        }
    }

    /**
     * Open a stream for the HTTP response entity, handling I/O errors.
     * 
     * @param state state
     * @param response response
     * @throws StopRequest StopRequest
     * @return an InputStream to read the response entity
     */
    private InputStream openResponseEntity(State state, HttpResponse response) throws StopRequest {
        try {
            return response.getEntity().getContent();
        } catch (IOException ex) {
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while getting entity: " + ex.toString(), ex);
        }
    }

    /**
     * logNetworkState.
     */
    private void logNetworkState() {
        if (Constants.LOGX) {
            Log.i(Constants.TAG, "Net " + (Helpers.isNetworkAvailable(mSystemFacade) ? "Up" : "Down"));
        }
    }

    /**
     * Read HTTP response headers and take appropriate action, including setting up the destination
     * file and updating the database.
     * 
     * @param state state
     * @param innerState InnerState
     * @param response response
     * @throws StopRequest StopRequest
     * @throws RetryDownload RetryDownload
     */
    private void processResponseHeaders(State state, InnerState innerState, HttpResponse response) throws StopRequest,
            RetryDownload {
        readETagResponseHeaders(state, innerState, response);

        if (innerState.mContinuingDownload) {
            // ignore response headers on resume requests
            return;
        }

        synchronized (mInfo) { // add by caoahitao fix SEARHBOX-65
                               // 【百度搜索】【Android一期】【下载】下载历史记录中，文件大小不显示
            readResponseHeaders(state, innerState, response);

            try {
                state.mFilename = Helpers
                        .generateSaveFile(
                                mContext,
                                mInfo.mUri,
                                mInfo.mHint,
                                innerState.mHeaderContentDisposition,
                                innerState.mHeaderContentLocation,
                                state.mMimeType,
                                mInfo.mDestination,
                                (innerState.mHeaderContentLength != null) ? Long
                                        .parseLong(innerState.mHeaderContentLength) : 0, mInfo.mIsPublicApi);
            } catch (Helpers.GenerateSaveFileError exc) {
                throw new StopRequest(exc.mStatus, exc.mMessage);
            }
            try {

                // <<add by zhangyunhua 如果下载文件时，文件没有创建，则创建文件
                File file = new File(state.mFilename);
                if (!file.exists()) {
                    try {
                        File parentFile = file.getParentFile();
                        if (parentFile == null || parentFile.exists()) {
                            file.createNewFile();
                        } else {
                            if (parentFile.mkdirs()) {
                                file.createNewFile();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // end by zhangyunhua>>
                state.mStream = new FileOutputStream(state.mFilename);
            } catch (FileNotFoundException exc) {
                throw new StopRequest(Downloads.Impl.STATUS_FILE_ERROR, "while opening destination file: "
                        + exc.toString(), exc);
            }
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "writing " + mInfo.mUri + " to " + state.mFilename);
            }

            updateDatabaseFromHeaders(state, innerState);
        }

        // check connectivity again now that we know the total size
        checkConnectivity(state);
    }

    /**
     * Update necessary database fields based on values of HTTP response headers that have been
     * read.
     * 
     * @param state state
     * @param innerState innerState
     */
    private void updateDatabaseFromHeaders(State state, InnerState innerState) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.DATA, state.mFilename);
        if (innerState.mHeaderETag != null) {
            values.put(Constants.ETAG, innerState.mHeaderETag);
        }
        if (state.mMimeType != null) {
            values.put(Downloads.Impl.COLUMN_MIME_TYPE, state.mMimeType);
        }
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, mInfo.mTotalBytes);
        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
    }

    /**
     * 当用户暂停了下载，后又恢复下载。期间，如果下载数据内容发生变化的话，客户端须删除旧的数据，然后重新下载，这种判断就是通过ETag来判断内容是否发生变化。
     * 
     * @param state state
     * @param innerState innerState
     * @param response response
     * @throws RetryDownload RetryDownload
     */
    private void readETagResponseHeaders(State state, InnerState innerState, HttpResponse response)
            throws RetryDownload {
        if (TextUtils.isEmpty(innerState.mHeaderETag)) {
            if (Configuration.DEBUG) {
                Log.i(Constants.TAG, "innerState.mHeaderETag == null");
            }
            return;
        }

        Header header = response.getFirstHeader("ETag");
        if (header != null) {
            String etag = header.getValue()/* + "::" + System.currentTimeMillis() */;
            if (Configuration.DEBUG) {
                Log.i(Constants.TAG, "downlaodthread: etag = etag");
            }
            /**
             * Sprint5.0, 小说下载须支持断点续传。 但有一种case: 当用户暂停了下载，后又恢复下载。期间，如果小说数据内容发生变化的话，
             * 客户端须删除旧的数据，然后重新下载，这种判断就是通过ETag来判断内容是否发生变化。 所以，当旧的ETag不为空时，须对比新旧的ETag分别处理
             */
            if (!TextUtils.equals(innerState.mHeaderETag, etag)) {
                if (Configuration.DEBUG) {
                    Log.i(Constants.TAG, "innerState.mHeaderETag != etag");
                }
                innerState.mBytesSoFar = 0;
                innerState.mHeaderContentLength = "0";
                innerState.mBytesNotified = 0;
                innerState.mTimeLastNotification = 0;
                innerState.mHeaderETag = null;

                closeDestination(state);
                File f = new File(state.mFilename);
                if (f.exists()) {
                    // The download hadn't actually started, we can restart
                    // from scratch
                    boolean deleted = f.delete();
                    if (!deleted) {
                        if (Configuration.DEBUG) {
                            Log.v(Constants.TAG, "setupDestinationFile delete file failed");
                        }
                    }
                }

                mInfo.mTotalBytes = 0;
                mInfo.mCurrentBytes = 0;
                /**
                 * 抛出RetryDownloady异常
                 */
                throw new RetryDownload();
            } else {
                if (Configuration.DEBUG) {
                    Log.i(Constants.TAG, "innerState.mHeaderETag == etag");
                }
            }
        }
    }

    /**
     * Read headers from the HTTP response and store them into local state.
     * 
     * @param state state
     * @param innerState innerState
     * @param response response
     * @throws StopRequest StopRequest
     */
    private void readResponseHeaders(State state, InnerState innerState, HttpResponse response) throws StopRequest {
        Header header = response.getFirstHeader("Content-Disposition");
        if (header != null) {
            innerState.mHeaderContentDisposition = header.getValue();
        }
        header = response.getFirstHeader("Content-Location");
        if (header != null) {
            innerState.mHeaderContentLocation = header.getValue();
        }

        if (state.mMimeType == null) {
            header = response.getFirstHeader("Content-Type");
            if (header != null) {
                state.mMimeType = sanitizeMimeType(header.getValue());
            }
        }
        header = response.getFirstHeader("ETag");
        if (header != null) {
            innerState.mHeaderETag = header.getValue();
        }
        String headerTransferEncoding = null;
        header = response.getFirstHeader("Transfer-Encoding");
        if (header != null) {
            headerTransferEncoding = header.getValue();
        }
        if (headerTransferEncoding == null) {
            header = response.getFirstHeader("Content-Length");
            if (header != null) {
                innerState.mHeaderContentLength = header.getValue();
                mInfo.mTotalBytes = Long.parseLong(innerState.mHeaderContentLength);
            }
        } else {
            // Ignore content-length with transfer-encoding - 2616 4.4 3
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "ignoring content-length because of xfer-encoding");
            }
        }
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Content-Disposition: " + innerState.mHeaderContentDisposition);
            Log.v(Constants.TAG, "Content-Length: " + innerState.mHeaderContentLength);
            Log.v(Constants.TAG, "Content-Location: " + innerState.mHeaderContentLocation);
            Log.v(Constants.TAG, "Content-Type: " + state.mMimeType);
            Log.v(Constants.TAG, "ETag: " + innerState.mHeaderETag);
            Log.v(Constants.TAG, "Transfer-Encoding: " + headerTransferEncoding);
        }

        boolean noSizeInfo = innerState.mHeaderContentLength == null
                && (headerTransferEncoding == null || !headerTransferEncoding.equalsIgnoreCase("chunked"));
        if (!mInfo.mNoIntegrity && noSizeInfo) {
            throw new StopRequest(Downloads.Impl.STATUS_HTTP_DATA_ERROR, "can't know size of download, giving up");
        }
    }

    /**
     * Check the HTTP response status and handle anything unusual (e.g. not 200/206).
     * 
     * @param state state
     * @param innerState innerState
     * @param response response
     * @throws StopRequest StopRequest
     * @throws RetryDownload RetryDownload
     */
    private void handleExceptionalStatus(State state, InnerState innerState, HttpResponse response) throws StopRequest,
            RetryDownload {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 503 && mInfo.mNumFailed < Constants.MAX_RETRIES) { // SUPPRESS CHECKSTYLE
            handleServiceUnavailable(state, response);
        }
        if (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307) { // SUPPRESS
                                                                                                // CHECKSTYLE
            handleRedirect(state, response, statusCode);
        }

        int expectedStatus = innerState.mContinuingDownload ? 206 : Downloads.Impl.STATUS_SUCCESS; // SUPPRESS
                                                                                                   // CHECKSTYLE
        if (statusCode != expectedStatus) {
            handleOtherStatus(state, innerState, statusCode);
        }
    }

    /**
     * Handle a status that we don't know how to deal with properly.
     * 
     * @param state state
     * @param innerState innerState
     * @param statusCode statusCode
     * @throws StopRequest StopRequest
     */
    private void handleOtherStatus(State state, InnerState innerState, int statusCode) throws StopRequest {
        int finalStatus;
        if (Downloads.Impl.isStatusError(statusCode)) {
            finalStatus = statusCode;
        } else if (statusCode >= 300 && statusCode < 400) { // SUPPRESS CHECKSTYLE
            finalStatus = Downloads.Impl.STATUS_UNHANDLED_REDIRECT;
        } else if (innerState.mContinuingDownload && statusCode == Downloads.Impl.STATUS_SUCCESS) {
            finalStatus = Downloads.Impl.STATUS_CANNOT_RESUME;
        } else {
            finalStatus = Downloads.Impl.STATUS_UNHANDLED_HTTP_CODE;
        }
        throw new StopRequest(finalStatus, "http error " + statusCode);
    }

    /**
     * Handle a 3xx redirect status.
     * 
     * @param state state
     * @param response response
     * @param statusCode statusCode
     * @throws StopRequest StopRequest
     * @throws RetryDownload RetryDownload
     */
    private void handleRedirect(State state, HttpResponse response, int statusCode) throws StopRequest, RetryDownload {
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "got HTTP redirect " + statusCode);
        }
        if (state.mRedirectCount >= Constants.MAX_REDIRECTS) {
            throw new StopRequest(Downloads.Impl.STATUS_TOO_MANY_REDIRECTS, "too many redirects");
        }
        Header header = response.getFirstHeader("Location");
        if (header == null) {
            return;
        }
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Location :" + header.getValue());
        }

        String newUri;
        try {
            newUri = new URI(mInfo.mUri).resolve(new URI(header.getValue())).toString();
        } catch (URISyntaxException ex) {
            if (Constants.LOGV) {
                Log.d(Constants.TAG, "Couldn't resolve redirect URI " + header.getValue() + " for " + mInfo.mUri);
            }
            throw new StopRequest(Downloads.Impl.STATUS_HTTP_DATA_ERROR, "Couldn't resolve redirect URI");
        }
        ++state.mRedirectCount;
        state.mRequestUri = newUri;
        if (statusCode == 301 || statusCode == 303) { // SUPPRESS CHECKSTYLE
            // use the new URI for all future requests (should a retry/resume be necessary)
            state.mNewUri = newUri;
        }
        throw new RetryDownload();
    }

    /**
     * Handle a 503 Service Unavailable status by processing the Retry-After header.
     * 
     * @param state state
     * @param response response
     * @throws StopRequest StopRequest
     */
    private void handleServiceUnavailable(State state, HttpResponse response) throws StopRequest {
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "got HTTP response code 503");
        }
        state.mCountRetry = true;
        Header header = response.getFirstHeader("Retry-After");
        if (header != null) {
            try {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "Retry-After :" + header.getValue());
                }
                state.mRetryAfter = Integer.parseInt(header.getValue());
                if (state.mRetryAfter < 0) {
                    state.mRetryAfter = 0;
                } else {
                    if (state.mRetryAfter < Constants.MIN_RETRY_AFTER) {
                        state.mRetryAfter = Constants.MIN_RETRY_AFTER;
                    } else if (state.mRetryAfter > Constants.MAX_RETRY_AFTER) {
                        state.mRetryAfter = Constants.MAX_RETRY_AFTER;
                    }
                    state.mRetryAfter += Helpers.RANDOM.nextInt(Constants.MIN_RETRY_AFTER + 1);
                    state.mRetryAfter *= 1000; // SUPPRESS CHECKSTYLE
                }
            } catch (NumberFormatException ex) {
                // ignored - retryAfter stays 0 in this case.
                ex.printStackTrace();
            }
        }
        throw new StopRequest(Downloads.Impl.STATUS_WAITING_TO_RETRY, "got 503 Service Unavailable, will retry later");
    }

    /**
     * Send the request to the server, handling any I/O exceptions.
     * 
     * @param state state
     * @param client client
     * @param request request
     * @return sendRequest
     * @throws StopRequest StopRequest
     */
    private HttpResponse sendRequest(State state, HttpClient client, HttpGet request) throws StopRequest {
        try {
            return client.execute(request);
        } catch (IllegalArgumentException ex) {
            throw new StopRequest(Downloads.Impl.STATUS_HTTP_DATA_ERROR, "while trying to execute request: "
                    + ex.toString(), ex);
        } catch (IOException ex) {
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while trying to execute request: "
                    + ex.toString(), ex);
        }
    }

    /**
     * getFinalStatusForHttpError
     * 
     * @param state state
     * @return getFinalStatusForHttpError
     */
    private int getFinalStatusForHttpError(State state) {
        if (!Helpers.isNetworkAvailable(mSystemFacade)) {
            return Downloads.Impl.STATUS_WAITING_FOR_NETWORK;
        } else if (mInfo.mNumFailed < Constants.MAX_RETRIES) {
            state.mCountRetry = true;
            return Downloads.Impl.STATUS_WAITING_TO_RETRY;
        } else {
            if (Configuration.DEBUG) {
                Log.w(Constants.TAG, "reached max retries for " + mInfo.mId);
            }
            return Downloads.Impl.STATUS_HTTP_DATA_ERROR;
        }
    }

    /**
     * Prepare the destination file to receive data. If the file already exists, we'll set up
     * appropriately for resumption.
     * 
     * @param state state
     * @param innerState innerState
     * @throws StopRequest StopRequest
     */
    private void setupDestinationFile(State state, InnerState innerState) throws StopRequest {
        if (state.mFilename != null) { // only true if we've already run a thread for this download
            if (!Helpers.isFilenameValid(state.mFilename)) {
                // this should never happen
                throw new StopRequest(Downloads.Impl.STATUS_FILE_ERROR, "found invalid internal destination filename");
            }
            // We're resuming a download that got interrupted
            File f = new File(state.mFilename);
            if (f.exists()) {
                long fileLength = f.length();
                if (fileLength == 0) {
                    // The download hadn't actually started, we can restart from scratch
                    boolean deleted = f.delete();
                    if (!deleted) {
                        if (Configuration.DEBUG) {
                            Log.v(Constants.TAG, "setupDestinationFile delete file failed");
                        }
                    }
                    state.mFilename = null;
                } else if (mInfo.mETag == null && !mInfo.mNoIntegrity) {
                    // This should've been caught upon failure
                    boolean deleted = f.delete();
                    if (!deleted) {
                        if (Configuration.DEBUG) {
                            Log.v(Constants.TAG, "setupDestinationFile delete file failed");
                        }
                    }
                    throw new StopRequest(Downloads.Impl.STATUS_CANNOT_RESUME,
                            "Trying to resume a download that can't be resumed");
                } else {
                    // All right, we'll be able to resume this download
                    try {
                        state.mStream = new FileOutputStream(state.mFilename, true);
                    } catch (FileNotFoundException exc) {
                        throw new StopRequest(Downloads.Impl.STATUS_FILE_ERROR,
                                "while opening destination for resuming: " + exc.toString(), exc);
                    }
                    innerState.mBytesSoFar = (int) fileLength;
                    if (mInfo.mTotalBytes != -1) {
                        innerState.mHeaderContentLength = Long.toString(mInfo.mTotalBytes);
                    }
                    innerState.mHeaderETag = mInfo.mETag;
                    innerState.mContinuingDownload = true;
                }
            }
        }

        if (state.mStream != null && mInfo.mDestination == Downloads.Impl.DESTINATION_EXTERNAL && !isDrmFile(state)) {
            closeDestination(state);
        }
    }

    /**
     * Add custom headers for this download to the HTTP request.
     * 
     * @param innerState innerState
     * @param request request
     */
    private void addRequestHeaders(InnerState innerState, HttpGet request) {
        for (Pair<String, String> header : mInfo.getHeaders()) {
            request.addHeader(header.first, header.second);
        }

        if (innerState.mContinuingDownload) {
            if (innerState.mHeaderETag != null) {
                request.addHeader("If-Match", innerState.mHeaderETag);
            }
            request.addHeader("Range", "bytes=" + innerState.mBytesSoFar + "-");
        }
    }

    /**
     * Stores information about the completed download, and notifies the initiating application.
     * 
     * @param status status
     * @param countRetry countRetry
     * @param retryAfter retryAfter
     * @param redirectCount redirectCount
     * @param gotData gotData
     * @param filename filename
     * @param uri uri
     * @param mimeType mimeType
     */
    private void notifyDownloadCompleted(int status, boolean countRetry, int retryAfter, int redirectCount,
            boolean gotData, String filename, String uri, String mimeType) {
        notifyThroughDatabase(status, countRetry, retryAfter, redirectCount, gotData, filename, uri, mimeType);
        if (Downloads.Impl.isStatusCompleted(status)) {
            mInfo.sendIntentIfRequested();
        }
    }

    /**
     * notify Through Database
     * 
     * @param status status
     * @param countRetry countRetry
     * @param retryAfter retryAfter
     * @param redirectCount redirectCount
     * @param gotData gotData
     * @param filename filename
     * @param uri uri
     * @param mimeType mimeType
     */
    private void notifyThroughDatabase(int status, boolean countRetry, int retryAfter, int redirectCount,
            boolean gotData, String filename, String uri, String mimeType) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_STATUS, status);
        // begin 不删除下载文件的名字，因为在重试的时候，要保持断点续传，删掉文件名字后，无法实现 added by liuqingbiao
        // values.put(Downloads.Impl.DATA, filename);
        // end
        if (uri != null) {
            values.put(Downloads.Impl.COLUMN_URI, uri);
        }
        values.put(Downloads.Impl.COLUMN_MIME_TYPE, mimeType);
        values.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
        values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, retryAfter + (redirectCount << 28)); // SUPPRESS
                                                                                                // CHECKSTYLE
        if (!countRetry) {
            values.put(Constants.FAILED_CONNECTIONS, 0);
        } else if (gotData) {
            values.put(Constants.FAILED_CONNECTIONS, 1);
        } else {
            values.put(Constants.FAILED_CONNECTIONS, mInfo.mNumFailed + 1);
        }

        mContext.getContentResolver().update(mInfo.getAllDownloadsUri(), values, null, null);
    }

    /**
     * Clean up a mimeType string so it can be used to dispatch an intent to view a downloaded
     * asset.
     * 
     * @param mimeType either null or one or more mime types (semi colon separated).
     * @return null if mimeType was null. Otherwise a string which represents a single mimetype in
     *         lowercase and with surrounding whitespaces trimmed.
     */
    private static String sanitizeMimeType(String mimeType) {
        try {
            mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);

            final int semicolonIndex = mimeType.indexOf(';');
            if (semicolonIndex != -1) {
                mimeType = mimeType.substring(0, semicolonIndex);
            }
            return mimeType;
        } catch (NullPointerException npe) {
            return null;
        }
    }
}
