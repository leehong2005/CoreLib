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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.util.Map;

/**
 * ILoadImage的实现
 * 
 * @author lihong06
 * @since 2014-7-28
 */
public abstract class ILoadImageImpl implements ILoadImage {
    @Override
    public Bitmap loadImage() {
        return null;
    }

    @Override
    public String toString() {
        String url = getUrl();
        if (!TextUtils.isEmpty(url)) {
            return url;
        }
        
        return super.toString();
    }

    @Override
    public int getSampleSize(BitmapFactory.Options options) {
        return 1;
    }

    @Override
    public Map<String, String> getHeader() {
        return null;
    }
}
