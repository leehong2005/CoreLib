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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;

import com.lee.sdk.cache.task.ImageLoaderTask;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

//CHECKSTYLE:OFF

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    /**
     * The load image listener. 
     * 
     * @author Li Hong
     * @since 2013-7-24
     */
    public interface OnLoadImageListener {
        /**
         * Called when finished to load image.
         * 
         * <p>
         * 如果加载普通图片，result为bitmap，如果加载Gif，result则为Drawable对象。
         * </p>
         * 
         * @param data the data you just passed to load image.
         * @param result the loaded bitmap, the bitmap may be null. if it is null, the
         *        bitmap loading operation may fail. 
         */
        void onLoadImage(Object data, Object result);
    }
    
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mUseCache = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;
    private static final int MESSAGE_CLEAR_BY_DATA = 4;
    private static final boolean DEBUG = true & BuildConfig.DEBUG;
    
    private WeakReference<OnLoadImageListener> mListener;

    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }
    
    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param listener The load image listener, this listener only be used when load bitmap in work thread.
     * @param imageLoader The image loader.
     * 
     * @return the Bitmap object, if the bitmap associated this data exist in cache, it 
     * will return it, otherwise return null;
     */
    public boolean loadImage(Object data, IAsyncView imageView) {
        return loadImage(data, imageView, null);
    }
    
    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param listener The load image listener, this listener only be used when load bitmap in work thread.
     * @param imageLoader The image loader.
     * 
     * @return the Bitmap object, if the bitmap associated this data exist in cache, it 
     * will return it, otherwise return null;
     */
    public boolean loadImage(Object data, IAsyncView imageView, ImageLoaderTask loaderTask) {
        return loadImage(data, imageView, null, null);
    }
    
    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param listener The load image listener, this listener only be used when load bitmap in work thread.
     * @param imageLoader The image loader.
     * 
     * @return the Bitmap object, if the bitmap associated this data exist in cache, it 
     * will return it, otherwise return null;
     */
    public boolean loadImage(Object data, IAsyncView imageView, OnLoadImageListener listener, ImageLoaderTask loaderTask) {
        if (data == null) {
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "ImageWorker loadImage data = " + data);
        }
        Bitmap bitmap = null;
        boolean succeed = false;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
            if (DEBUG) {
                Log.d(TAG, "get bitmap from memcache data = " + data);
            }
        }

        if (bitmap != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(new BitmapDrawable(mResources, bitmap));
            // Here set the drawable to null.
            imageView.setAsyncDrawable(null);
            
            // Added by LiHong at 2013/07/24 begin =======
            perfermOnLoadImage(data, bitmap);
            if (DEBUG) {
                Log.d(TAG, "loadImage try to nofity listener data = " + data);
            }
            if (null != listener) {
                listener.onLoadImage(data, bitmap);
                if (DEBUG) {
                    Log.d(TAG, "loadImage after nofity listener data = " + data);
                }
            }
            // Added by LiHong at 2013/07/24 end =========
            
            succeed = true;
            
//        } else if (cancelPotentialWork(data, imageView)) {
//            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
//            final AsyncDrawable asyncDrawable =
//                    new AsyncDrawable(mResources, mLoadingBitmap, task);
//            imageView.setImageBitmap(mLoadingBitmap);
//            imageView.setAsyncDrawable(asyncDrawable);
//
//            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
//            // framework and slightly modified. Refer to the docs at the top of the class
//            // for more info on what was changed.
//            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
//        }
        }  else {
            // NOTE: we will NOT create a new AsyncDrawable object when cancel potential work,
            // we can reuse it.
            boolean createTask = false;
            AsyncDrawable asyncDrawable = getAsyncDrawable(imageView);
            if (null != asyncDrawable) {
                if (asyncDrawable.cancelPotentialWork(data)) {
                    createTask = true;
                }
            } else {
                createTask = true;
            }
            if (DEBUG) {
                Log.d(TAG, "loadImage createTask = " + data);
            }
            if (createTask) {
                if (null == asyncDrawable) {
                    asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap);
                }

                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                asyncDrawable.setWorkerTask(task);
                // Set the listener.
                if (DEBUG) {
                    Log.d(TAG, "loadImage setLoadImageListener listener = " + listener);
                }
                
                // ensure image cache
                if (null != loaderTask) {
                    loaderTask.setImageCache(mImageCache);
                }
                asyncDrawable.setOnLoadImageListener(listener);
                asyncDrawable.setImageLoaderTask(loaderTask);
                Drawable drawable = (null != mLoadingBitmap) ? new BitmapDrawable(mResources, mLoadingBitmap) : null;
                imageView.setImageDrawable(drawable);
                imageView.setAsyncDrawable(asyncDrawable);
                
                // NOTE: This uses a custom version of AsyncTask that has been pulled from the
                // framework and slightly modified. Refer to the docs at the top of the class
                // for more info on what was changed.
                //task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
                if (DEBUG) {
                    Log.d(TAG, "BitmapWorkerTask start " + data);
                }
                succeed = true;
            }
        }
        
        return succeed;
    }

    /**
     * Get the bitmap from cache.
     * 
     * @param data
     * 
     * @return
     */
    public Bitmap getBitmapFromCache(Object data) {
        Bitmap bitmap = null;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }
        
        return bitmap;
    }
    
    /**
     * Adds a bitmap to both memory and disk cache.
     * 
     * @param data Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (null != mImageCache) {
            mImageCache.addBitmapToCache(data, bitmap);
        }
    }
    
    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * Adds an {@link ImageCache} to this worker in the background (to prevent disk access on UI
     * thread).
     * @param cacheParams
     */
    public void addImageCache(ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        ImageCache cache = new ImageCache(mImageCacheParams);
        setImageCache(cache);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }
    
    /**
     * Sets the {@link ImageCache} object to use with this ImageWorker. Usually you will not need
     * to call this directly, instead use {@link ImageWorker#addImageCache} which will create and
     * add the {@link ImageCache} object in a background thread (to ensure no disk access on the
     * main/UI thread).
     *
     * @param imageCache
     */
    public void setImageCache(ImageCache imageCache) {
        mImageCache = imageCache;
    }

    /**
     * Get the image cache object.
     *  
     * @return imageCache
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }
    
    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }
    
    /**
     * If set to true, the bitmap will add to cache for reuse.
     * 
     * @param useCache
     */
    public void setUseCache(boolean useCache) {
        mUseCache = useCache;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
    }
    
    /**
     * Set the load image listener.
     * 
     * @param listener
     */
    public void setOnLoadImageListener(OnLoadImageListener listener) {
        if (null == listener) {
            mListener = null;
        } else {
            mListener = new WeakReference<OnLoadImageListener>(listener);
        }
    }

    private void perfermOnLoadImage(Object data, Object bitmap) {
        if (null != mListener) {
            OnLoadImageListener listener = mListener.get();
            if (null != listener) {
                listener.onLoadImage(data, bitmap);
            }
        }
    }
    
    /**
     * Process the data and return the bitmap or input stream.
     * 
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, IAsyncView)}
     * @return bitmap or input stream
     */
    protected abstract InputStream downloadStream(Object data);
    
    /**
     * Decode the input stream, return the bitmap or drawble, if the returned value is drawable, typically it is for GIF.
     * 
     * @param object data
     * @param is input stream.
     * @param isGifSupported 是否支持GIF，true/false
     * @return bitmap or drawable
     */
    protected abstract Object decodeStream(Object data, InputStream is, boolean isGifSupported);

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(IAsyncView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.mData;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, IAsyncView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(IAsyncView imageView) {
        if (imageView != null) {
            //final Drawable drawable = imageView.getDrawable();
            final Drawable drawable = imageView.getAsyncDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
    
    private static OnLoadImageListener getLoadImageListener(IAsyncView imageView) {
        if (imageView != null) {
            //final Drawable drawable = imageView.getDrawable();
            final Drawable drawable = imageView.getAsyncDrawable();
            if (DEBUG) {
                Log.d(TAG, "getLoadImageListener drawable = " + drawable);
            }
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                OnLoadImageListener listener = asyncDrawable.getLoadImageListener();
                asyncDrawable.setOnLoadImageListener(null);
                return listener;
            }
        }
        return null;
    }
    
    private static ImageLoaderTask getImageLoaderTask(IAsyncView imageView) {
        if (null != imageView) {
            final Drawable drawable = imageView.getAsyncDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                ImageLoaderTask task = asyncDrawable.getImageLoaderTask();
                asyncDrawable.setImageLoaderTask(null);
                return task;
            }
        }
        
        return null;
    }
    
    /**
     * Get the drawable from the IAsyncView.
     * 
     * @param imageView
     * @return
     */
    private static AsyncDrawable getAsyncDrawable(IAsyncView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getAsyncDrawable();
            if (drawable instanceof AsyncDrawable) {
                return (AsyncDrawable)drawable;
            }
        }
        
        return null;
    }
    
    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, Object> {
        /** 是否支持GIF，如果文件是GIF，则返回GifDrawable，否则返回Bitmap */
        private boolean mIsGifSupported = true;
        /** 当前Task中的数据 */
        private Object mData;
        private final WeakReference<IAsyncView> imageViewReference;

        public BitmapWorkerTask(IAsyncView imageView) {
            imageViewReference = new WeakReference<IAsyncView>(imageView);
            mIsGifSupported = imageView.isGifSupported();
        }

        /**
         * Background processing.
         */
        @Override
        protected Object doInBackground(Object... params) {
            // 
            // NOTE: 
            // 这里有一个严重问题：原来的缓存逻辑都是直接从网络流中decode为Bitmap，然后再将bitmap compress到文件中,
            // 在这种情况下，图片可能会失真，如果实际图片带有透明度的话，保存到文件中，透明度的信息可能丢失，导致下次
            // 从文件缓存中读取出来，透明的部分就会显示黑色。
            // 解决方案：将文件写入流中，然后再从文件中读取，这样的好处是不会丢失图片信息，其次是在解析时可以计算sample size
            // 防止 OOM
            //
            return doInBackgroundForStream(params);
        }

        /**
         * Background processing.
         * 
         * @param data data
         */
        private Object doInBackgroundForStream(Object... params) {
            mData = params[0];
            final String dataString = String.valueOf(mData);
            InputStream inputStream = null;
            Bitmap bitmap = null;
            
            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                inputStream = mImageCache.getStreamFromDiskCache(dataString);
            }
            
            final ImageLoaderTask externalImageLoaderTask = getImageLoaderTask(getAttachedImageView());
            
            // Modified:
            // Read the input stream from cache and process it by the subclasses.
            //
            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (inputStream == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                if (null != externalImageLoaderTask) {
                    inputStream = externalImageLoaderTask.downloadStream(mData);
                } else {
                    inputStream = downloadStream(mData);
                }
            }
            
            // Removed by lihong06 2014/11/29 begin =======
            // The stream has been downloaded to disk cache, so do NOT add to cache again.
//            if (inputStream != null && mImageCache != null) {
//                if (!fromDiskCache) {
//                    // If use cache, we add the input stream to cache. 
//                    if (mUseCache) {
//                        mImageCache.addStreamToCache(dataString, inputStream);
//                        // Fetch the stream
//                        inputStream = mImageCache.getStreamFromDiskCache(dataString);
//                    }
//                }
//            }
            // Removed by lihong06 2014/11/29 end =========
            
            Object retData = null;
            
            if (null != externalImageLoaderTask) {
                retData = externalImageLoaderTask.decodeStream(mData, inputStream, mIsGifSupported);
            } else {
                retData = decodeStream(mData, inputStream, mIsGifSupported);
            }
            
            if (retData instanceof Bitmap) {
                bitmap = (Bitmap) retData;
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null && mImageCache != null) {
                // If use cache, we add the bitmap to cache. 
                if (mUseCache) {
                    mImageCache.addBitmapToCache(dataString, bitmap, true);
                }
            }
            
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work,  return data = " + retData);
            }

            return retData;
        }
        
        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Object result) {
            onPostExecuteForStream(mData, result);
        }
        
        /**
        * Once the image is processed, associates it to the imageView
        */
        private void onPostExecuteForStream(Object data, Object result) {
            if (DEBUG) {
                Log.i(TAG, " ========= onPostExecuteForStream() begin =============");
                Log.i(TAG, "     inputData = " + data + ",  outputData = " + result);
            }
            
            Drawable drawable = null;

            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                result = null;
            }
           
            if (result instanceof Bitmap) {
                drawable = new BitmapDrawable(mResources, (Bitmap) result);
            } else if (result instanceof Drawable) {
                drawable = (Drawable) result;
            }

            final IAsyncView imageView = getAttachedImageView();
            // Find the listener.
            final OnLoadImageListener listener = getLoadImageListener(imageView);
            
            if (null != imageView) {
                setImageDrawable(imageView, drawable);
                imageView.setAsyncDrawable(null);
            }
            
            if (null != listener) {
                listener.onLoadImage(data, result);
            }

            // 通知全局的listener
            perfermOnLoadImage(data, result);
            
            if (DEBUG) {
                Log.i(TAG, " ========= onPostExecuteForStream() end =============");
            }
       }

        @Override
        protected void onCancelled(Object bitmap) {
            super.onCancelled(bitmap);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }
        
        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private IAsyncView getAttachedImageView() {
            final IAsyncView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        private OnLoadImageListener loadImageListener;
        private ImageLoaderTask imageLoaderTask;

        public AsyncDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }
        
        @SuppressWarnings("unused")
        @Deprecated
        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }
        
        public void setWorkerTask(BitmapWorkerTask bitmapWorkerTask) {
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
        
        public void setOnLoadImageListener(OnLoadImageListener listener) {
            loadImageListener = listener;
        }
        
        public OnLoadImageListener getLoadImageListener() {
            return loadImageListener;
        }
        
        public void setImageLoaderTask(ImageLoaderTask loaderTask) {
            imageLoaderTask = loaderTask;
        }
        
        public ImageLoaderTask getImageLoaderTask() {
            return imageLoaderTask;
        }
        
        public boolean cancelPotentialWork(Object data) {
            BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask();
            if (bitmapWorkerTask != null) {
                final Object bitmapData = bitmapWorkerTask.mData;
                if (bitmapData == null || !bitmapData.equals(data)) {
                    bitmapWorkerTask.cancel(true);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                    }
                } else {
                    // The same work is already in progress.
                    return false;
                }
            }
            return true;
        }
    }

    
    /**
     * Called when the processing is complete and the final bitmap should be set on the ImageView.
     *
     * @param imageView
     * @param bitmap
     */
    private void setImageDrawable(IAsyncView imageView, Drawable drawable) {
        if (null == drawable) {
            imageView.setImageDrawable(null);
            return;
        }
        
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drwabale and the final bitmap
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(Color.TRANSPARENT),
                            drawable
                    });
            // Set background to loading bitmap
            /*
            imageView.setBackgroundDrawable(
                    new BitmapDrawable(mResources, mLoadingBitmap));
             */
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }
    
    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
                case MESSAGE_CLEAR_BY_DATA:
                    String data = (String)params[1];
                    clearCacheInternal(data);
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    public void clearCacheInternal() {
        clearCacheInternal(true);
    }
    
    public void clearCacheInternal(boolean clearDiskCache) {
        if (mImageCache != null) {
            mImageCache.clearCache(clearDiskCache);
        }
    }
    
    public void clearCacheInternal(String data) {
        if (null != mImageCache) {
            mImageCache.clearCache(data);
        }
    }
    
    public void clearDiskCache(String data) {
        if (null != mImageCache) {
            mImageCache.clearDiskCache(data);
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }
    
    /**
     * Get the bitmap count in the memory cache.
     * 
     * @return the size
     */
    public int getBitmapSizeInMemCache() {
        if (null != mImageCache) {
            return mImageCache.getBitmapSizeInMemCache();
        }
        
        return 0;
    }
    
    /**
     * Clear the cache of specified data, this method only clear the memory cache, it will NOT
     * clear the disk cache.
     *
     * @param data Unique identifier for which item to get
     */
    public void clearCache(String data) {
        new CacheAsyncTask().execute(MESSAGE_CLEAR_BY_DATA, data);
    }
    
    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }
    
    /**
     * 检查Disk cache中是否有data对应的bitmap
     * 
     * @param data Unique identifier for which item to get
     * @returntrue if found in Disk Cache, false otherwise
     */
    public boolean hasBitmapInDiskCache(Object data) {
        if (mImageCache != null) {
            return mImageCache.hasBitmapInDiskCache(String.valueOf(data));
        }
        return false;
    }

    /**
     * 是否已经暂停加载
     * 
     * @return true 已经暂停, false 未暂停
     */
    public boolean hasPaused() {
        return mPauseWork;
    }
}
//CHECKSTYLE:ON
