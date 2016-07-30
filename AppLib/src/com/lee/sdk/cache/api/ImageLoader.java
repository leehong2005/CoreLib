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

package com.lee.sdk.cache.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.app.BaseApplication;
import com.lee.sdk.cache.BuildConfig;
import com.lee.sdk.cache.IAsyncView;
import com.lee.sdk.cache.ImageCache;
import com.lee.sdk.cache.ImageCache.ImageCacheParams;
import com.lee.sdk.cache.ImageFetcher;
import com.lee.sdk.cache.ImageWorker.OnLoadImageListener;
import com.lee.sdk.cache.task.ImageLoaderTask;
import com.lee.sdk.utils.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * 这个类封装了加载图片的一些业务逻辑，它先从本地文件查找，如果不行，再从网络请求。
 *
 * <p> 这个类提供了{@link Builder}类来创建不同的{@link ImageLoader}的实例， 同时你也可以调用{@link ImageLoader#getInstance()}来得到一个默认的单实例。
 * </p>
 *
 * @author Li Hong
 * @since 2013-7-23
 */
public final class ImageLoader {
    /**
     * TAG
     */
    private static final String TAG = "ImageLoader";

    /**
     * Application的Context
     */
    private Context mAppContext;

    /**
     * ImageLoad的实例
     */
    private static ImageLoader sInstance = null;

    /**
     * 加载图片的具体类
     */
    private ImageFetcher mImageFetcher = null;

    /**
     * 当前缓存路径
     */
    private String mCacheDir = null;

    /**
     * 使用Disk缓存
     */
    private boolean mUseDiskCache = true;

    /**
     * mFadeInBitmap
     */
    private boolean mFadeInBitmap = true;

    /**
     * 缓存最大百分比
     */
    private float mMaxCachePercent = Builder.MAX_CACHE_PERCENT;

    /**
     * 磁盘最大值
     */
    private int mMaxDiskCacheSize = Builder.DISK_CACHE_SIZE;

    /**
     * AsyncView容器，目的是保证IAsyncView生命周期，防止被回收
     */
    private AsyncViewHolder mAsyncViewHolder;

    /**
     * 得到单实例
     *
     * @return instance
     */
    public static synchronized ImageLoader getInstance() {
        if (null == sInstance) {
            Context context = BaseApplication.getAppContext();
            sInstance = Builder.getDefault(context).setFadeInBitmap(true).build();
        }

        return sInstance;
    }

    /**
     * 释放单实例对象
     */
    public static synchronized void release() {
        if (null != sInstance) {
            if (null != sInstance.mAsyncViewHolder) {
                sInstance.mAsyncViewHolder.clear();
            }
        }

        sInstance = null;
    }

    /**
     * 设置图片加载的监听器
     *
     * @param listener listener
     */
    public void setOnLoadImageListener(OnLoadImageListener listener) {
        mImageFetcher.setOnLoadImageListener(listener);
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data   Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        mImageFetcher.addBitmapToCache(data, bitmap);
    }

    /**
     * 构造方法
     *
     * @param context context
     */
    private ImageLoader(Context context) {
        mAppContext = context.getApplicationContext();
    }

    /**
     * 初始化图片加载器
     */
    private void initLoader() {
        final Context context = mAppContext;
        mImageFetcher = new ImageFetcher(context);

        final boolean useDiskCache = mUseDiskCache;
        if (useDiskCache) {
            if (TextUtils.isEmpty(mCacheDir)) {
                if (BuildConfig.DEBUG) {
                    throw new IllegalStateException(
                        "You must set the disk cache directory if you want to use disk cache");
                }
            }

            ImageCacheParams params = new ImageCacheParams(new File(mCacheDir));
            params.setMemCacheSizePercent(context, mMaxCachePercent);
            params.setMaxDiskCacheSize(mMaxDiskCacheSize);
            params.diskCacheEnabled = true;
            mImageFetcher.addImageCache(params);
        } else {
            ImageCacheParams params = new ImageCacheParams(context, "cache_params");
            params.setMemCacheSizePercent(context, mMaxCachePercent);
            params.diskCacheEnabled = false;
            ImageCache imageCache = new ImageCache(params);
            mImageFetcher.setImageCache(imageCache);
        }

        mImageFetcher.setImageFadeIn(mFadeInBitmap);
    }

    /**
     * 加载图片，传入的data参数能唯一标识出一个图片文件，我们在内部会根据这个数据通过成一个key，通过这个key来标识内存缓存中的bimap和磁盘缓存中的文件。
     *
     * <p> 这个方法通常用于加载单独一个数据的bitmap，不太适合于AbsListView类型的数据，如果一定要使用在AbsListView中，在收到 返回的对象时，需要判断回传的数据与当前加载的数据是否相同，原因是由于AbsListView存在View复用机制。
     * </p>
     *
     * @param data     需要加载bitmap的数据, 通常你需要传一个图片的url或path，这个url能唯一定位这个文件。
     * @param listener 图片加载的listener
     * @return true/false
     */
    public boolean loadImage(Object data, OnLoadImageListener listener) {
        if (null == data) {
            return false;
        }

        final IAsyncView imageView = new AsyncView();
        OnLoadImageListenerWrapper listenerWrapper = new OnLoadImageListenerWrapper(listener) {
            @Override
            public void onFinishLoad() {
                if (null != mAsyncViewHolder) {
                    mAsyncViewHolder.remove(imageView);
                }
            }
        };

        if (null == mAsyncViewHolder) {
            mAsyncViewHolder = new AsyncViewHolder();
        }
        mAsyncViewHolder.add(imageView);

        return loadImage(data, imageView, listenerWrapper);
    }

    /**
     * 加载图片，传入的data参数能唯一标识出一个图片文件，我们在内部会根据这个数据通过成一个key， 通过这个key来标识内存缓存中的bimap和磁盘缓存中的文件。
     *
     * @param data 需要加载bitmap的数据, 通常你需要传一个图片的url或path，这个url能唯一定位这个文件。
     * @param view 需要显示图片的View，你需要保证该对象的持久性，在ImageWorker内部，该对象是使用WeakReference来维护， 如果没有引用的对象的话，该对象可能被释放掉，从而导致逻辑不正确，如不能收到回调，图片不能正确设置等。
     * @return true/false
     */
    public boolean loadImage(Object data, IAsyncView view) {
        return loadImage(data, view, null);
    }

    /**
     * 加载图片
     *
     * @param data     需要加载bitmap的数据
     * @param view     需要显示图片的View，你需要保证该对象的持久性，在ImageWorker内部，该对象是使用WeakReference来维护， 如果没有引用的对象的话，该对象可能被释放掉，从而导致逻辑不正确，如不能收到回调，图片不能正确设置等。
     * @param listener 图片加载完成的监听器
     * @return succeed to load bitmap
     */
    public boolean loadImage(Object data, IAsyncView view, OnLoadImageListener listener) {
        return loadImage(data, view, listener, null);
    }

    /**
     * 加载图片
     *
     * @param data     需要加载bitmap的数据
     * @param view     需要显示图片的View，你需要保证该对象的持久性，在ImageWorker内部，该对象是使用WeakReference来维护， 如果没有引用的对象的话，该对象可能被释放掉，从而导致逻辑不正确，如不能收到回调，图片不能正确设置等。
     * @param listener 图片加载完成的监听器
     * @param loadTask 图片加载器
     * @return succeed to load bitmap
     */
    public boolean loadImage(Object data, IAsyncView view, OnLoadImageListener listener, ImageLoaderTask loadTask) {
        // Added by lihong06 2015/01/30 begin ==============
        if (sHasHoldOn) {
            saveWaitingTasks(data, view, listener, loadTask);
            return false;
        }
        // Added by lihong06 2015/01/30 end ==============
        return mImageFetcher.loadImage(data, view, listener, loadTask);
    }

    /**
     * 从内存缓存中得到bitmap
     *
     * @param data 数据
     * @return bitmap对象
     */
    public Bitmap getBitmapFromCache(Object data) {
        return mImageFetcher.getBitmapFromCache(data);
    }

    /**
     * 检查Disk Cache中是否有对应的图片
     *
     * @param data 需要加载bitmap的数据
     * @return true if found in Memory Cache, false otherwise
     */
    public boolean hasBitmapInDiskCache(Object data) {
        return mImageFetcher.hasBitmapInDiskCache(data);
    }

    /**
     * 清除指定数据的图片
     *
     * @param data 数据
     */
    public void clearImage(Object data) {
        if (data instanceof String) {
            if (!TextUtils.isEmpty((String) data)) {
                //mImageResizer.clearCache((String) data);
                mImageFetcher.clearCacheInternal((String) data);
            }
        }
    }

    /**
     * 清除disk上的缓存
     *
     * @param data data
     */
    public void clearDiskImage(Object data) {
        if (data instanceof String) {
            if (!TextUtils.isEmpty((String) data)) {
                mImageFetcher.clearDiskCache((String) data);
            }
        }
    }

    /**
     * 清除内存中的图片对象
     */
    public void clear() {
        mImageFetcher.clearCacheInternal(false);
    }

    /**
     * Get the bitmap count in the memory cache.
     *
     * @return the size
     */
    public int getBitmapSizeInMemCache() {
        return mImageFetcher.getBitmapSizeInMemCache();
    }

    /**
     * 暂停当前工作线程
     *
     * @param pauseWork true表示为暂停，false表示开启
     */
    public void setPauseWork(boolean pauseWork) {
        mImageFetcher.setPauseWork(pauseWork);
    }

    /**
     * 返回ImageLoader是否已经暂停的状态
     *
     * @return true表示已经暂停
     */
    public boolean hasPaused() {
        return mImageFetcher.hasPaused();
    }

    /**
     * AsyncView holder
     *
     * @author lihong06
     * @since 2014-10-21
     */
    private static class AsyncViewHolder {
        /**
         * Holder
         */
        private ArrayList<IAsyncView> mHolder = new ArrayList<IAsyncView>();

        /**
         * @param asyncView asyncView
         */
        public synchronized void add(IAsyncView asyncView) {
            mHolder.add(asyncView);
        }

        /**
         * @param asyncView asyncView
         */
        public synchronized void remove(IAsyncView asyncView) {
            mHolder.remove(asyncView);
        }

        /**
         */
        public void clear() {
            mHolder.clear();
        }
    }

    /**
     * OnLoadImageListener包装类
     *
     * @author lihong06
     * @since 2014-10-21
     */
    private abstract static class OnLoadImageListenerWrapper implements OnLoadImageListener {
        /**
         * Listener
         */
        private OnLoadImageListener mListener;

        /**
         * @param listener listener
         */
        public OnLoadImageListenerWrapper(OnLoadImageListener listener) {
            mListener = listener;
        }

        @Override
        public void onLoadImage(Object data, Object bitmap) {
            if (null != mListener) {
                mListener.onLoadImage(data, bitmap);
            }

            onFinishLoad();
        }

        /**
         * Finish load
         */
        public abstract void onFinishLoad();
    }

    /**
     * 创建ImageLoader的Builder类
     *
     * @author lihong06
     * @since 2014-1-23
     */
    public static final class Builder {
        /**
         * 缓存的最大图片数量
         */
        public static final int MAX_CACHE_NUM = 20;
        /**
         * 缓存的大小的百分比
         */
        public static final float MAX_CACHE_PERCENT = 0.12f;
        /**
         * Disk cache size
         */
        public static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB //SUPPRESS CHECKSTYLE

        /**
         * Context
         */
        private Context mContext;
        /**
         * 当前缓存路径
         */
        private String mCacheDir = null;
        /**
         * 使用Disk缓存
         */
        private boolean mUseDiskCache = false;
        /**
         * mFadeInBitmap
         */
        private boolean mFadeInBitmap = false;
        /**
         * 缓存最大百分比
         */
        private float mMaxCachePercent = MAX_CACHE_PERCENT;
        /**
         * 磁盘最大值
         */
        private int mMaxDiskCacheSize = DISK_CACHE_SIZE;

        /**
         * 构造实例
         *
         * @param context context
         * @return Builder对象
         */
        public static Builder newInstance(Context context) {
            Builder builder = new Builder(context);
            String dir = PathUtils.getImageCacheDirectory(context);
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            // Disk缓存路径
            String cacheDir = file.getAbsolutePath();
            builder.setDiskCacheDir(cacheDir);

            return builder;
        }

        /**
         * 得到默认的Builder
         *
         * @param context context
         * @return Builder对象
         */
        public static Builder getDefault(Context context) {
            // 默认的Builder使用了默认的图片缓存
            String dir = PathUtils.getImageCacheDirectory(context);
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            // Disk缓存路径
            String cacheDir = file.getAbsolutePath();

            return new Builder(context)
                .setUseDiskCache(true)
                .setFadeInBitmap(false)
                .setDiskCacheDir(cacheDir)
                .setMaxCachePercent(MAX_CACHE_PERCENT)
                .setMaxDiskCacheSize(DISK_CACHE_SIZE);
        }

        /**
         * 构造方法
         *
         * @param context context
         */
        private Builder(Context context) {
            mContext = context.getApplicationContext();
        }

        /**
         * 是否使用disk缓存
         *
         * @param useDiskCache true/false
         * @return Builder对象
         */
        public Builder setUseDiskCache(boolean useDiskCache) {
            mUseDiskCache = useDiskCache;
            return this;
        }

        /**
         * @param fadeInBitmap true/false
         * @return Builder对象
         */
        public Builder setFadeInBitmap(boolean fadeInBitmap) {
            mFadeInBitmap = fadeInBitmap;
            return this;
        }

        /**
         * disk缓存目录
         *
         * @param cacheDir 目录
         * @return Builder对象
         */
        public Builder setDiskCacheDir(String cacheDir) {
            mCacheDir = cacheDir;
            return this;
        }

        /**
         * 设置缓存最大百分比
         *
         * @param percent 百分比，必须在0.05f与0.8f之间
         * @return Builder对象
         */
        public Builder setMaxCachePercent(float percent) {
            mMaxCachePercent = percent;
            return this;
        }

        /**
         * 设置磁盘缓存最大值
         *
         * @param maxDiskCacheSize maxDiskCacheSize
         * @return Builder对象
         */
        public Builder setMaxDiskCacheSize(int maxDiskCacheSize) {
            mMaxDiskCacheSize = maxDiskCacheSize;
            return this;
        }

        /**
         * 创建ImageLoader的实例
         *
         * @return ImageLoader
         */
        public ImageLoader build() {
            ImageLoader imageLoader = new ImageLoader(mContext);
            imageLoader.mCacheDir = mCacheDir;
            imageLoader.mUseDiskCache = mUseDiskCache;
            imageLoader.mMaxCachePercent = mMaxCachePercent;
            imageLoader.mFadeInBitmap = mFadeInBitmap;
            imageLoader.mMaxDiskCacheSize = mMaxDiskCacheSize;
            imageLoader.initLoader();
            return imageLoader;
        }
    }

    // Added by lihong06 2015/01/30 begin ============
    /**
     * 是否等待
     */
    private static boolean sHasHoldOn = false;
    /**
     * 锁对象
     */
    private static Object sLockObj = new Object();
    /**
     * 等待任务队列
     */
    private static HashMap<Object, HoldOnParams> sHoldOnList = null;

    /**
     * 暂停时的任务的数据封装
     *
     * @author lihong06
     * @since 2015-1-30
     */
    static class HoldOnParams {
        /**
         * data
         */
        public Object data;
        /**
         * view
         */
        public IAsyncView view;
        /**
         * listener
         */
        public OnLoadImageListener listener;
        /**
         * load task
         */
        public ImageLoaderTask loadTask;
    }

    /**
     * 让当前ImageLoader暂停住，不会发起实际的异步任务操作，它与{@link ImageLoader#setPauseWork(boolean)}不同。
     *
     * @param holdOn 是否hold one
     */
    public static void setHoldOn(boolean holdOn) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "setHoldOn  Hold on: " + holdOn);
        }

        synchronized (sLockObj) {
            sHasHoldOn = holdOn;
        }

        // 开始处理等待的任务
        if (!holdOn) {
            handleWaitingTasks();
        }
    }

    /**
     * 处理等待的任务
     */
    private static void handleWaitingTasks() {
        synchronized (sLockObj) {
            if (null != sHoldOnList && !sHoldOnList.isEmpty()) {
                ImageLoader imageLoader = ImageLoader.getInstance();
                Collection<HoldOnParams> params = sHoldOnList.values();
                for (HoldOnParams param : params) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "handleWaitingTasks   data = " + param.data);
                    }

                    // 加载图片
                    imageLoader.loadImage(param.data, param.view, param.listener, param.loadTask);
                }

                sHoldOnList.clear();
            }
        }
    }

    /**
     * 保存等待的任务
     *
     * @param data     data
     * @param view     view
     * @param listener listener
     * @param loadTask loadTask
     */
    private static void saveWaitingTasks(Object data,
                                         IAsyncView view,
                                         OnLoadImageListener listener,
                                         ImageLoaderTask loadTask) {
        synchronized (sLockObj) {
            if (sHasHoldOn) {
                if (null == sHoldOnList) {
                    sHoldOnList = new HashMap<Object, HoldOnParams>();
                }

                if (!sHoldOnList.containsKey(data)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "saveWaitingTasks   data = " + data);
                    }

                    HoldOnParams params = new HoldOnParams();
                    params.data = data;
                    params.view = view;
                    params.listener = listener;
                    params.loadTask = loadTask;

                    sHoldOnList.put(data, params);
                }
            }
        }
    }
    // Added by lihong06 2015/01/30 end ==============
}
