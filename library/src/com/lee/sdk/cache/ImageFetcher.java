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
import android.util.Log;

import com.lee.sdk.cache.task.ImageLoaderTask;

import java.io.InputStream;

/**
 * A subclass of {@link ImageWorker} that fetches and resizes images fetched from a URL.
 * 
 * @author lihong06
 * @since 2014-10-14
 */
public class ImageFetcher extends ImageWorker {
    /**
     * DEBUG
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /**
     * TAG
     */
    private static final String TAG = "ImageFetcher";
    /**
     * 图片加载的Task对象
     */
    private ImageLoaderTask mImageLoaderTask;

    /**
     * @param context context
     */
    public ImageFetcher(Context context) {
        super(context);
        mImageLoaderTask = new ImageLoaderTask(context);
    }

    @Override
    public void setImageCache(ImageCache imageCache) {
        super.setImageCache(imageCache);
        mImageLoaderTask.setImageCache(imageCache);
    }

    @Override
    protected InputStream downloadStream(Object data) {
        if (DEBUG) {
            Log.d(TAG, "ImageFetcher#downloadStream() downlaod stream,  data = " + data);
        }

        return mImageLoaderTask.downloadStream(data);
    }

    @Override
    protected Object decodeStream(Object data, InputStream is, boolean isGifSupported) {
        if (DEBUG) {
            Log.d(TAG, "ImageFetcher#decodeStream() decode stream,  data = " + data);
        }

        return mImageLoaderTask.decodeStream(data, is, isGifSupported);
    }
}
