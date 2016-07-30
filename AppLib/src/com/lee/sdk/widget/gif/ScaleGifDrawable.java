/*
 * Copyright (C) 2013 Lee Hong (http://blog.csdn.net/leehong2005)
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

package com.lee.sdk.widget.gif;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import com.lee.sdk.Configuration;

/**
 * 这个类扩展于{@link GifDrawable}，可以指定缩放率，如果从资源中加载，会自动对该资源进行缩放。
 * 
 * @author lihong06
 * @since 2015-3-17
 */
public class ScaleGifDrawable extends GifDrawable {
    /**
     * 缩放比率
     */
    private float mScale = 1.0f;
    
    /**
     * @param afd afd
     * @throws IOException IOException
     */
    public ScaleGifDrawable(AssetFileDescriptor afd) throws IOException {
        super(afd);
    }

    /**
     * @param assets assets
     * @param assetName assetName
     * @throws IOException IOException
     */
    public ScaleGifDrawable(AssetManager assets, String assetName) throws IOException {
        super(assets, assetName);
    }

    /**
     * @param bytes bytes
     * @throws IOException IOException
     */
    public ScaleGifDrawable(byte[] bytes) throws IOException {
        super(bytes);
    }

    /**
     * @param buffer buffer
     * @throws IOException IOException
     */
    public ScaleGifDrawable(ByteBuffer buffer) throws IOException {
        super(buffer);
    }

    /**
     * @param resolver resolver
     * @param uri uri
     * @throws IOException IOException
     */
    public ScaleGifDrawable(ContentResolver resolver, Uri uri) throws IOException {
        super(resolver, uri);
    }

    /**
     * @param file file
     * @throws IOException IOException
     */
    public ScaleGifDrawable(File file) throws IOException {
        super(file);
    }

    /**
     * @param fd fd
     * @throws IOException IOException
     */
    public ScaleGifDrawable(FileDescriptor fd) throws IOException {
        super(fd);
    }

    /**
     * @param stream stream
     * @throws IOException IOException
     */
    public ScaleGifDrawable(InputStream stream) throws IOException {
        super(stream);
    }

    /**
     * @param res res
     * @param id id
     * @throws NotFoundException NotFoundException
     * @throws IOException IOException
     */
    public ScaleGifDrawable(Resources res, int id) throws NotFoundException, IOException {
        super(res, id);
        resolveDensity(res, id);
    }

    /**
     * @param filePath filePath
     * @throws IOException IOException
     */
    public ScaleGifDrawable(String filePath) throws IOException {
        super(filePath);
    }
    
    @Override
    public int getIntrinsicHeight() {
        return (int) (super.getIntrinsicHeight() * mScale);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (super.getIntrinsicWidth() * mScale);
    }
    
    /**
     * 计算密度
     * 
     * @param res res
     * @param id id
     */
    private void resolveDensity(Resources res, int id) {
        try {
            TypedValue outValue = new TypedValue();
            res.getValue(id, outValue, true);
            float density = outValue.density; 
            float targetDensity = res.getDisplayMetrics().densityDpi;
            if (density > 0) {
                mScale = targetDensity / density;
            }
            
            if (Configuration.DEBUG) {
                Log.d("ScaleGifDrawable", "resolveDensity   scaled = " + mScale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
