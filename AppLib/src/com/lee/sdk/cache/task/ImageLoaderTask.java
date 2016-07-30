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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.cache.BuildConfig;
import com.lee.sdk.cache.ILoadImage;
import com.lee.sdk.cache.ImageCache;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 图片加载器的通用类，它实现了基本的图片加载的流程
 * 
 * <p>
 * 调用顺序是：
 * <li>{@link #downloadStream(Object)}}
 * <li>{@link #decodeStream(Object, InputStream, boolean)}}
 * </p>
 * 
 * @author lihong06
 * @since 2014-10-14
 */
public class ImageLoaderTask {
    /** DEBUG */
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /** TAG */
    private static final String TAG = "ImageLoaderTask";
    /** App context */
    private Context mAppContext;
    /** Image Cahce */
    private ImageCache mImageCache;
    
    /**
     * @param context context
     */
    public ImageLoaderTask(Context context) {
        mAppContext = context.getApplicationContext();
    }
    
    /**
     * Set image cache
     * 
     * @param imageCache imageCache
     */
    public void setImageCache(ImageCache imageCache) {
        mImageCache = imageCache;
    }

    /**
     * 根据指定的数据加载流
     * 
     * @param data data
     * @return stream
     */
    public InputStream downloadStream(Object data) {
        return onLoadStream(data);
    }

    /**
     * 解析数据流，输出Bitmap或Drawable对象
     * 
     * @param data 数据
     * @param is 可能的输入流
     * @param isGifSupported 是否支持GIF格式
     * @return Bitmap或Drawable对象
     */
    public Object decodeStream(Object data, InputStream is, boolean isGifSupported) {
        return onDecodeStream(data, is, isGifSupported);
    }

    /**
     * 根据数据来加载流
     * 
     * @param data 对应的数据
     * @return 返回流
     */
    private InputStream onLoadStream(Object data) {
        InputStream is = null;
        if (data instanceof ILoadImage) {
            ILoadImage loadImage = ((ILoadImage) data);
            String url = loadImage.getUrl();
            is = onLoadStream(url, loadImage.getHeader());
        } else if (data instanceof String) {
            String url = (String) data;
            Map<String, String> header = null;
            is = onLoadStream(url, header);
        }
        
        return is;
    }
    
    /**
     * Decode the stream
     * 
     * @param data data
     * @param is the stream
     * @param isGifSupported support gif or not
     * @return object
     */
     protected Object onDecodeStream(final Object data, InputStream is, boolean isGifSupported) {
        if (null == is || null == data) {
            return null;
        }
         
        long start = System.currentTimeMillis();
        
        boolean isGif = false;
        FileDescriptor fd = null;
        Object retObject = null;
        BufferedInputStream bis = null;
        
        if (is instanceof FileInputStream) {
            try {
                fd = ((FileInputStream) is).getFD();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // 判断是否支持GIF
        if (isGifSupported) {
            final int bufSize = 8 * 1024;
            final int gifSize = 3;
        
            bis = new BufferedInputStream(is, bufSize);
            byte[] buf = new byte[gifSize];
            try {
                bis.mark(gifSize);
                bis.read(buf, 0, gifSize);
                bis.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        
            // 判断是否是GIF
            isGif = isGif(buf);
        } else {
            bis = new BufferedInputStream(is);
        }
        
        boolean close = true;
        try {
            if (null != bis) {
                // 如果解析图片失败，如何处理？删掉Disk缓存？只有在加载图片失败时才删除Disk缓存
                retObject = decodeBitmap(data, bis, fd, new Runnable() {
                    @Override
                    public void run() {
                        clearDiskCache(data);
                        if (DEBUG) {
                            Log.e(TAG, "onDecodeStream failed,  remove the disk cache file.");
                        }
                    }
                });
                
                if (DEBUG) {
                    Log.i(TAG, "onDecodeStream,  data = " + data + ",  object = " + retObject);
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (close) {
                closeSafely(is);
                closeSafely(bis);
            }
        }
        
        long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.i(TAG, "ImageLoaderTask#onDecodeStream(),      time = " + (end - start) + "  data = "
                + data + ",   isGif = " + isGif + ",  support Gif = " + isGifSupported);
        }
        
        return retObject;
    }

    /**
     * 根据一个URL加载图片
     * 
     * @param url URL
     * @param headers 如是url，下载时需要添加的头信息
     * @return bitmap对象
     */
    protected InputStream onLoadStream(String url, Map<String, String> headers) {
        if (!TextUtils.isEmpty(url)) {
            if (HttpUtils.isUrl(url)) {
                return loadStreamFromNet(url, headers);
            } else {
                return loadStreamFromFile(url);
            }
        }

        return null;
    }
    
    /**
     * 清除缓存
     * 
     * @param data data
     */
    public void clearDiskCache(Object data) {
        if (null != mImageCache) {
            if (data instanceof String) {
                mImageCache.clearDiskCache((String) data);
            }
        }
    }
    
    /**
     * 从文件中加载流
     * 
     * @param path path
     * @return stream
     */
    private InputStream loadStreamFromFile(String path) {
        File file = new File(path);
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        
        return fis;
    } 
    
    /**
     * Fetch bitmap from network
     * 
     * @param url url
     * @param headers headers
     * @return bitmap
     */
    private InputStream loadStreamFromNet(String url, Map<String, String> headers) {
        if (DEBUG) {
            long id = Thread.currentThread().getId();
            Log.i(TAG, " ========== loadStreamFromNet() begin ============   url = " + url);
            Log.d(TAG, "        header = " + headers + ",   thread id = " + id);
        }
        
        Context context = mAppContext;
        if (!HttpUtils.isNetworkConnected(context)) {
            return null;
        }
        
        long start = System.currentTimeMillis();
        final String data = url;
        final ImageCache imageCache = mImageCache;
        if (null == imageCache) {
            return null;
        }
        
        boolean succeed = Utils.downloadUrlToStream(context, url, headers, new Utils.OnProcessStreamListener() {
            @Override
            public boolean processStream(InputStream is) {
                if (DEBUG) {
                    long id = Thread.currentThread().getId();
                    Log.d(TAG, "begin to download stream to temp file.    thread id = " + id);
                }

                // 下载文件，注意不能下拉将网络流直接写到Disk缓存中，如果网络慢的话，会导致整个Disk缓存被lock。
                File file = downloadStreamToFile(data, is);

                if (null != file && file.exists()) {
                    if (DEBUG) {
                        Log.d(TAG, "        downloadStreamToFile()   succeed = true");
                    }

                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        imageCache.addStreamToCache(data, fis);
                        return true;
                    } catch (FileNotFoundException e) {
                        if (DEBUG) {
                            Log.d(TAG, "addStreamToCache  exception = " + e);
                        }
                    } finally {
                        closeSafely(fis);
                        // 删除文件
                        file.delete();
                    }
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "        downloadStreamToFile()   succeed = false");
                    }
                }

                return false;
            }
        });
        
        InputStream stream = null;
        if (succeed) {
            stream = imageCache.getStreamFromDiskCache(data);
        }
        
        long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.i(TAG, "       ImageLoaderTask#loadStreamFromNet()    fetch stream and save to cache total time = "
                + (end - start) + ",   succeed = " + succeed + ",  url = " + url);
            Log.i(TAG, " ========== loadStreamFromNet() end ============ url = " + url);
        }
        
        return stream;
    }
    
    /**
     * 下载网络流到临时文件中，返回文件流
     * 
     * @param data data
     * @param is 网络流
     * @return 文件
     */
    private File downloadStreamToFile(String data, InputStream is) {
        final ImageCache imageCache = mImageCache;
        if (null == imageCache) {
            return null;
        }
        
        long threadId = Thread.currentThread().getId();
        File diskCacheDir = imageCache.getDiskCacheDir();
        // 确保文件目录存在
        if (!diskCacheDir.exists()) {
            synchronized (ImageLoaderTask.class) {
                diskCacheDir.mkdirs();
            }
        }
        // key_temp_{thread id}
        String key = ImageCache.hashKeyForDisk(data) + "_temp_" + String.valueOf(threadId);
        File file = new File(diskCacheDir, key);
        FileOutputStream fos = null;
        boolean succeed = false;
        
        try {
            fos = new FileOutputStream(file);
            long size = Utils.copyStream(is, fos);
            succeed = (size > 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeSafely(is);
            closeSafely(fos);
        }
        
        return (succeed) ? file : null;
    }
    
    /**
     * 判断是否是GIF格式
     * 
     * @param buf 数据，长度必须>=3
     * @return true/false
     */
    private boolean isGif(byte[] buf) {
        if (null == buf) {
            return false;
        }
        
        if (buf.length < 3) { //SUPPRESS CHECKSTYLE
            return false;
        }
        
        // Is GIF?
        // G:71, I:73, F:70
        boolean isGif = (buf[0] == 71 && buf[1] == 73 && buf[2] == 70); // SUPPRESS CHECKSTYLE
        return isGif;
    }
    
    /**
     * 解析Bitmap
     * 
     * @param data data
     * @param is is
     * @param fd fd
     * @param failAction action，这个动作为在图片加载失败时调用，如果是OOM的话，则不会调用该action
     * @return bitmap
     */
    private Object decodeBitmap(Object data, InputStream is, FileDescriptor fd, Runnable failAction) {
        ILoadImage loadImage = (data instanceof ILoadImage) ? (ILoadImage) data : null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = getSampleSize(loadImage, fd);
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(is, null, opts);
            if (null == bmp) {
                if (null != failAction) {
                    failAction.run();
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return bmp;
    }
    
    /**
     * 计算sample size
     * 
     * @param loadImage loadImage
     * @param fd fd
     * @return sample size
     */
    private int getSampleSize(ILoadImage loadImage, FileDescriptor fd) {
        if (null != loadImage && null != fd) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, opts);
            if (opts.mCancel || opts.outWidth == -1 || opts.outHeight == -1) {
                return 1;
            }
            
            int sampleSize = loadImage.getSampleSize(opts);
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            
            return sampleSize;
        }
        
        return 1;
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
}
