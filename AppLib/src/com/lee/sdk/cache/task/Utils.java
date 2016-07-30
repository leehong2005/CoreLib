/*
 * Copyright (C) 2016 LiHong (https://github.com/leehong2005)
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

package com.lee.sdk.cache.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.cache.BuildConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 这里使用的是HttpClient，由于HttpClient在后续的高版本操作系统上面不再支持，后续可以考虑再重构
 *
 * @author lihong
 * @date 2016/03/01
 */
final class Utils {
    /**
     * DEBUG
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * TAG
     */
    private static final String TAG = "ImageLoader";

    /**
     * 从输入流中读取字节写入输出流
     *
     * @param is 输入流
     * @param os 输出流
     * @return 复制大字节数
     */
    public static long copyStream(InputStream is, OutputStream os) {
        if (null == is || null == os) {
            return 0;
        }

        try {
            final int DEFAULT_BUFFER_SIZE = 1024 * 3;
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            long size = 0;
            int len = 0;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
                size += len;
            }
            os.flush();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 安全关闭.
     *
     * @param closeable Closeable.
     */
    public static void closeSafely(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从网络下载流，并写入到输出流中
     *
     * @param context context
     * @param url url
     * @param listener listener
     * @return true/false
     */
    public static boolean downloadUrlToStream(Context context, String url,
                                              Map<String, String> headers,
                                              OnProcessStreamListener listener) {
        if (null == listener || TextUtils.isEmpty(url)) {
            return false;
        }

        long start = System.currentTimeMillis();
        // 从网络获取图片
        final DefaultHttpClient httpClient = HttpUtils.createHttpClient(context);
        HttpGet getRequest = null;
        boolean succeed = false;

        try {
            getRequest = new HttpGet(url);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    getRequest.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpResponse httpResponse = httpClient.execute(getRequest);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (DEBUG) {
                    Log.w(TAG, "Error " + statusCode + " while retrieving bitmap from " + url);
                }
                return false;
            }

            final HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream inputStream = null;
                try {
//                    inputStream = httpEntity.getContent();
                    // 有些url返回的响应数据为gzip压缩后的数据，需要根据头信息获取适合的inputStream
                    inputStream = HttpUtils.getSuitableInputStream(httpEntity);
                    long end = System.currentTimeMillis();
                    if (DEBUG) {
                        Log.e(TAG, "fetch image from network, time = " + (end - start) + " ms,    length = "
                            + httpEntity.getContentLength() + "         url = " + url);
                    }

                    if (null != inputStream) {
                        start = System.currentTimeMillis();
                        succeed = listener.processStream(inputStream);
                        end = System.currentTimeMillis();
                        if (DEBUG) {
                            Log.e(TAG, "fetch image from network, processStream time = " + (end - start) + " ms");
                        }
                    }
                } catch (Exception e) {
                    // 结束前需要有异常回调
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    System.gc();
                } finally {
                    closeSafely(inputStream);
                    httpEntity.consumeContent();
                }
            }
        } catch (IOException e) {
            // 结束前需要有异常回调
            if (getRequest != null) {
                getRequest.abort();
            }

            if (DEBUG) {
                Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
            }
        } catch (IllegalStateException e) {
            // 结束前需要有异常回调
            if (getRequest != null) {
                getRequest.abort();
            }

            if (DEBUG) {
                Log.w(TAG, "Incorrect URL: " + url);
            }
        } catch (Exception e) {
            // 结束前需要有异常回调
            if (getRequest != null) {
                getRequest.abort();
            }
            if (DEBUG) {
                Log.w(TAG, "Error while retrieving bitmap from " + url, e);
            }
        } finally {
            if (null != httpClient) {
                httpClient.getConnectionManager().shutdown();
            }
        }

        return succeed;
    }

    /**
     * @author lihong06
     * @since 2014-10-15
     */
    public interface OnProcessStreamListener {
        /**
         * 处理输入流
         *
         * @param is 输入流
         * @return succeed/fail
         */
        boolean processStream(InputStream is);
    }
}
