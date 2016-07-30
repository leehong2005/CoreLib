package com.lee.sdk.test.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class DownloadUtil {
    public interface IDownloadListener {
        public void onDownloadStart(String url);

        public void onInProgress(String url, int percent);

        public void onDownloadComplete(String url);

        public void onDownloadFailed(String url, int errorCode);
    }

    static final int MSG_DOWNLOAD_START = 0x01;
    static final int MSG_DOWNLOAD_PROCESS = 0x02;
    static final int MSG_DOWNLOAD_COMPLETE = 0x03;
    static final int MSG_DOWNLOAD_FAIL = 0x04;

    long mContentLength = 0;
    boolean mCancelDownload = false;
    Context mContext = null;
    IDownloadListener mListener = null;
    String mDownloadDir = "";
    String mDownloadUrl = "";
    String mDownloadFile = "";

    public DownloadUtil(Context context) {
        mContext = context;
    }

    public long getContentLength() {
        return mContentLength;
    }

    public String getDownloadFilePath() {
        return mDownloadFile;
    }

    public void cancelDownload() {
        mCancelDownload = true;
    }

    public void setDownloadListener(IDownloadListener listener) {
        mListener = listener;
    }

    public void setDownloadDir(String downloadDir) {
        mDownloadDir = downloadDir;
    }

    public boolean startDownloadAsync(String url) {
        if (TextUtils.isEmpty(mDownloadDir)) {
            return false;
        }

        mDownloadUrl = url;
        mCancelDownload = false;

        final String downloadUrl = url;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    onDownloadStart(downloadUrl);

                    doDownload(downloadUrl);

                    onDownloadComplete(downloadUrl);
                } catch (Exception e) {
                    e.printStackTrace();

                    onDownloadFailed(downloadUrl, -1);
                }
            }

        }, "download_thread");

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        return true;
    }

    public void startDownload(String downloadUrl) {
        try {
            mDownloadUrl = downloadUrl;

            onDownloadStart(downloadUrl);

            doDownload(downloadUrl);

            onDownloadComplete(downloadUrl);
        } catch (Exception e) {
            e.printStackTrace();

            onDownloadFailed(downloadUrl, -1);
        }
    }

    protected void onInProgress(String url, int percent) {
        if (null != mListener) {
            mListener.onInProgress(mDownloadUrl, percent);
        }
    }

    protected void onDownloadStart(String url) {
        if (null != mListener) {
            mListener.onDownloadStart(mDownloadUrl);
        }
    }

    protected void onDownloadComplete(String url) {
        if (null != mListener) {
            mListener.onDownloadComplete(mDownloadUrl);
        }
    }

    protected void onDownloadFailed(String url, int errorCode) {
        if (null != mListener) {
            mListener.onDownloadFailed(mDownloadUrl, errorCode);
        }
    }

    private boolean hasCancelled() {
        return mCancelDownload;
    }

    private File createDownloadDir() {
        File file = new File(mDownloadDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    private String getFileName(String url) {
        String fileName = "00.zip";
        int index = url.lastIndexOf("/");
        if (index >= 0) {
            fileName = url.substring(index + 1);
        }

        return fileName;
    }

    private int onProcessStream(long contentLength, InputStream inputStream) {
        int retVal = 0;

        if (null == inputStream) {
            return -1;
        }

        String url = mDownloadUrl;
        File file = createDownloadDir();
        String fileName = getFileName(url);

        try {
            File zipFile = new File(file, fileName);

            mDownloadFile = zipFile.getAbsolutePath();

            FileOutputStream os = new FileOutputStream(zipFile);
            InputStream is = inputStream;

            byte[] buf = new byte[1024];
            int len = 0;
            int size = 0;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
                size += len;
                float percent = 100 * (float) size / (float) contentLength;
                Log.e("leehong2", "  percent = " + percent);
                onInProgress(url, (int) percent);

                Thread.yield();

                if (hasCancelled()) {
                    break;
                }
            }

            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            retVal = -1;
        } catch (IOException e) {
            e.printStackTrace();
            retVal = -2;
        }

        return retVal;
    }

    private InputStream doDownload(String url) throws Exception {
        SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        HttpClient defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schemeRegistry),
                params);

        HttpConnectionParams.setConnectionTimeout(params, 60 * 1000);
        HttpConnectionParams.setSoTimeout(params, 60 * 1000);

        HttpGet httpGet = new HttpGet(url);
        InputStream inputStream = null;

        try {
            HttpResponse objResponse = defaultHttpClient.execute(httpGet);
            int httpStatus = objResponse.getStatusLine().getStatusCode();
            if (httpStatus == HttpStatus.SC_OK) {
                HttpEntity entity = objResponse.getEntity();
                if (null != entity) {
                    long contentLength = entity.getContentLength();

                    mContentLength = contentLength;

                    inputStream = entity.getContent();
                    int errorCode = onProcessStream(contentLength, inputStream);
                    if (errorCode < 0) {
                        onDownloadFailed(url, errorCode);
                    }
                }
            } else if (httpStatus >= 400 && httpStatus < 600) {
                throw new ConnectTimeoutException();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != httpGet) {
                httpGet.abort();
            }

            defaultHttpClient.getConnectionManager().shutdown();
        }

        return inputStream;
    }
}
