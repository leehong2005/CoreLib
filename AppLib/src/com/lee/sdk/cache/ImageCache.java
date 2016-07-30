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

package com.lee.sdk.cache;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// CHECKSTYLE:OFF

/**
 * This class holds our bitmap caches (memory and disk).
 */
@SuppressLint("NewApi")
public class ImageCache {
    private static final String TAG = "ImageCache";

    // Default memory cache size
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

    // Default disk cache size
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_CLEAR_DISK_CACHE_ON_START = false;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private DiskLruCache mDiskLruCache;
    private LruCache<String, Bitmap> mMemoryCache;
    private ImageCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    /**
     * Creating a new ImageCache object using the specified parameters.
     *
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public ImageCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    /**
     * Creating a new ImageCache object using the default parameters.
     *
     * @param context The context to use
     * @param uniqueName A unique name that will be appended to the cache directory
     */
    public ImageCache(Context context, String uniqueName) {
        init(new ImageCacheParams(context, uniqueName));
    }

    /**
     * Initialize the cache, providing all parameters.
     *
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(ImageCacheParams cacheParams) {
        mCacheParams = cacheParams;

        // Set up memory cache
        if (mCacheParams.memoryCacheEnabled) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Memory cache created (size = " + mCacheParams.memCacheSize + ")");
            }
            
            mMemoryCache = new LruCache<String, Bitmap>(mCacheParams.memCacheSize) {
                /**
                 * Measure item size in bytes rather than units which is more practical
                 * for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    if (mCacheParams.useMemCacheCount()) {
                        return 1;
                    }
                    
                    return getBitmapSize(bitmap);
                }
            };
        }

        // By default the disk cache is not initialized here as it should be initialized
        // on a separate thread due to disk access.
        if (cacheParams.initDiskCacheOnCreate) {
            // Set up disk cache
            initDiskCache();
        }
    }

    /**
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            long start = System.currentTimeMillis();
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(
                                    diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "Disk cache initialized");
                            }
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "initDiskCache - " + e);
                            }
                        }
                    }
                }
            }
            
            long end = System.currentTimeMillis();
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "initDiskCache -    time = " + (end - start) + "  ms");
            }
            
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     * @param data Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        addBitmapToCache(data, bitmap, true);
    }
    
    /**
     * Adds a bitmap to both memory and disk cache.
     * @param data Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap, boolean addToDiskCache) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(data) == null) {
            mMemoryCache.put(data, bitmap);
            
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "addBitmapToCache   memory cache size = " + mMemoryCache.size());
            }
        }

        // Not add bitmap to cache.
        if (!addToDiskCache) {
            return;
        }
        
        // Disk cache not enable
        if (!mCacheParams.diskCacheEnabled) {
            return;
        }
        
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            bitmap.compress(
                                    mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }
    
    /**
     * Adds a bitmap to disk cache.
     * 
     * @param data Unique identifier for the bitmap to store
     * @param is The input stream
     */
    public void addStreamToCache(String data, InputStream is) {
        if (data == null || is == null) {
            return;
        }
        
        // DO NOT Add to memory cache
        
        // Disk cache not enable
        if (!mCacheParams.diskCacheEnabled) {
            return;
        }
        
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            
                            // Copy the input stream to output stream
                            byte[] buf = new byte[1024 * 3];
                            int len = 0;
                            long size = 0;
                            while ((len = is.read(buf)) > 0) {
                                out.write(buf, 0, len);
                                size += len;
                            }
                            
                            // Avoid cache empty file to disk. 
                            if (size > 0) {
                                out.flush();
                                editor.commit();
                            } else {
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "ImageCache#addStreamToCache(), " +
                                        "failed to add stream to cache file, the data = " + data);
                                }
                            }
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "ImageCache#addStreamToCache()  disk cache has exist,  data = " + data);
                        }
                    }
                } catch (final IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "addBitmapToCache - " + e);
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "addBitmapToCache - " + e);
                    }
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }
    
    /**
     * Get the bitmap count in the memory cache.
     * 
     * @return the size
     */
    public int getBitmapSizeInMemCache() {
        if (null != mMemoryCache) {
            return mMemoryCache.getCount();
        }
        
        return 0;
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(data);
            if (memBitmap != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Memory cache hit");
                }
                return memBitmap;
            }
        }
        return null;
    }

    /**
     * 检查diskcache中有没有data对应的图片
     * 
     * @param data Unique identifier for which item to get
     * @return true if found in disk cache, false otherwise
     */
    public boolean hasBitmapInDiskCache(String data) {
        // Disk cache not enable
        if (!mCacheParams.diskCacheEnabled) {
            return false;
        }
        
        final String key = hashKeyForDisk(data);
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            
            if (mDiskLruCache != null) {
                DiskLruCache.Snapshot snapshot = null;
                try {
                    snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit");
                        }
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (snapshot != null) {
                        snapshot.close();
                    }
                }
            }
            return false;
        }
    }
    
    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        // Do not check the object is null, in one case, if the image worker is initializing disk cache, at this time,
        // a bitmap request from disk cache is coming, mDiskLruCache is still null, so we will request bitmap from
        // network or other source, typically it will call listener to tell caller to load bitmap, however, the data 
        // may exit a cache file in local, it is not reasonable to get bitmap from network. The correct step is 
        // waiting the complete of initializing work.
        /*
        if (null == mDiskLruCache) {
            return null;
        }
        */
        
        // Disk cache not enable
        if (!mCacheParams.diskCacheEnabled) {
            return null;
        }
        
        final String key = hashKeyForDisk(data);
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            try {
                                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                return bitmap;
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (final IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "getBitmapFromDiskCache - " + e);
                    }
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return null;
        }
    }
    
    /**
     * Get the input stream from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The input stream if found in cache, null otherwise, you should be responsible for closing the stream
     */
    public InputStream getStreamFromDiskCache(String data) {
        // Do not check the object is null, in one case, if the image worker is initializing disk cache, at this time,
        // a bitmap request from disk cache is coming, mDiskLruCache is still null, so we will request bitmap from
        // network or other source, typically it will call listener to tell caller to load bitmap, however, the data 
        // may exit a cache file in local, it is not reasonable to get bitmap from network. The correct step is 
        // waiting the complete of initializing work.
        /*
        if (null == mDiskLruCache) {
            return null;
        }
        */
        
        // Disk cache not enable
        if (!mCacheParams.diskCacheEnabled) {
            return null;
        }
        
        final String key = hashKeyForDisk(data);
        
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "getStreamFromDiskCache - key = " + key + ",   data = "
                + data + ",   mDiskLruCache = " + mDiskLruCache);
        }
        
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit! getStreamFromDiskCache   snapshot = " + snapshot);
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        return inputStream;
                    }
                    
                } catch (final IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "getStreamFromDiskCache - " + e);
                    }
                } finally {
                    
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "getStreamFromDiskCache - mDiskLruCache = null.");
                }
            }
            return null;
        }
    }
    
    /**
     * Clear the disk cache
     * 
     * @param data data
     */
    public void clearDiskCache(String data) {
        if (null != mDiskLruCache && !mDiskLruCache.isClosed()) {
            try {
                final String key = hashKeyForDisk(data);
                mDiskLruCache.remove(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Clear the cache of specified data, this method only clear the memory cache, it will NOT
     * clear the disk cache.
     *
     * @param data Unique identifier for which item to get
     */
    public void clearCache(String data) {
        if (null != mMemoryCache) {
            mMemoryCache.remove(data);
        }
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        clearCache(false);
    }
    
    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache(boolean clearDiskCache) {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Memory cache cleared");
            }
        }

        if (clearDiskCache) {
            synchronized (mDiskCacheLock) {
                mDiskCacheStarting = true;
                if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                    try {
                        mDiskLruCache.delete();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache cleared");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "clearCache - " + e);
                    }
                    mDiskLruCache = null;
                    initDiskCache();
                }
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Disk cache flushed");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache closed");
                        }
                    }
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "close - " + e);
                    }
                }
            }
        }
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        private boolean useMemCacheCount = false; 
        public File diskCacheDir;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean clearDiskCacheOnStart = DEFAULT_CLEAR_DISK_CACHE_ON_START;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        public ImageCacheParams(Context context, String uniqueName) {
            diskCacheDir = getDiskCacheDir(context, uniqueName);
        }

        public ImageCacheParams(File diskCacheDir) {
            this.diskCacheDir = diskCacheDir;
        }

        /**
         * Sets the memory cache size based on a percentage of the device memory class.
         * Eg. setting percent to 0.2 would set the memory cache to one fifth of the device memory
         * class. Throws {@link IllegalArgumentException} if percent is < 0.05 or > .8.
         *
         * This value should be chosen carefully based on a number of factors
         * Refer to the corresponding Android Training class for more discussion:
         * http://developer.android.com/training/displaying-bitmaps/
         *
         * @param context Context to use to fetch memory class
         * @param percent Percent of memory class to use to size memory cache
         */
        public void setMemCacheSizePercent(Context context, float percent) {
            if (percent < 0.05f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.05 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * getMemoryClass(context) * 1024 * 1024);
        }
        
        public void setMaxDiskCacheSize(int diskCacheSize) {
            this.diskCacheSize = diskCacheSize;
        }
        
        public void setMemCacheSizeCount(Context context, int count) {
            if (count <= 5 || count > 10000) {
                throw new IllegalArgumentException("setMemCacheSizeCount - count must be "
                        + "is too small or huge");
            }
            
            useMemCacheCount = true;
            memCacheSize = count;
        }
        
        public boolean useMemCacheCount() {
            return useMemCacheCount;
        }

        private static int getMemoryClass(Context context) {
            return ((ActivityManager) context.getSystemService(
                    Context.ACTIVITY_SERVICE)).getMemoryClass();
        }
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        File cacheDir = getExternalCacheDir2(context);
        if (null != cacheDir) {
            return new File(cacheDir, uniqueName);
        }
        
        return null;
        /**
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
        */
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    public static int getBitmapSize(Bitmap bitmap) {
        if (hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    public static boolean isExternalStorageRemovable() {
        if (hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory. Use {@link ImageCache#getExternalCacheDir2(Context)} method.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @Deprecated
    public static File getExternalCacheDir(Context context) {
        if (hasFroyo()) {
            File file =  context.getExternalCacheDir();
            if (null != file) {
                return file;
            }
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
    
    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    public static File getExternalCacheDir2(Context context) {
        File cacheDir = null;
        // If have SD card
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable()) {
            // API Level is 8
            if (hasFroyo()) {
                cacheDir = context.getExternalCacheDir();
            }
    
            if (null == cacheDir) {
                boolean sdcardWriteable = isExternalStorageWriteable();
                if (sdcardWriteable) {
                    cacheDir = Environment.getExternalStorageDirectory();
                }
            }
            
            if (null == cacheDir) {
                final String cachePath = "/Android/data/" + context.getPackageName() + "/cache/";
                cacheDir = new File(Environment.getExternalStorageDirectory().getPath() + cachePath);
            }
        }
        
        if (null == cacheDir) {
            cacheDir = context.getCacheDir();
        }
        
        return cacheDir;
    }
    
    /**
     * 判断外部存储是否可写
     * 
     * 此方法内采用文件读写操作来检测，所以相对比较耗时，请谨慎使用。
     * 
     * @return true:可写; false 不存在/没有mounted/不可写
     */
    public static boolean isExternalStorageWriteable() {
        boolean writealbe = false;
        
        if (TextUtils.equals(Environment.MEDIA_MOUNTED,
            Environment.getExternalStorageState())) {
            File esd = Environment.getExternalStorageDirectory();
            if (esd.exists() && esd.canWrite()) {
                File file = new File(esd,
                        ".696E5309-E4A7-27C0-A787-0B2CEBF1F1AB");
                if (file.exists()) {
                    writealbe = true;
                } else {
                    try {
                        writealbe = file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return writealbe;
    }
    
    /**
     * 得到当前缓存的目录
     * 
     * @return 缓存目录
     */
    public File getDiskCacheDir() {
        File diskCacheDir = mCacheParams.diskCacheDir;
        return diskCacheDir;
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressWarnings("deprecation")
    public static long getUsableSpace(File path) {
        if (hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }
    
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        final int JELLY_BEAN = 16;//Build.VERSION_CODES.JELLY_BEAN
        return Build.VERSION.SDK_INT >= JELLY_BEAN;
    }
}
// CHECKSTYLE:ON
