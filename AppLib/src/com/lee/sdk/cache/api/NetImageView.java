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
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 可以异步加载图片的ImageView，显示默认图片等。
 *
 * <p>
 *     请调用{@link #setImageUrl(String)}方法来加载图片
 * </p>
 *
 * @author lihong
 * @date 2015/11/13
 */
public class NetImageView extends ImageView {
    /**
     * 默认图片的ID
     */
    private int mDefaultImageResId = -1;

    /**
     * AsyncView
     */
    private AsyncView mAsyncView = new AsyncViewImpl();

    /**
     * 构造方法
     *
     * @param context context
     */
    public NetImageView(Context context) {
        super(context);
    }

    /**
     * 构造方法
     *
     * @param context context
     * @param attrs attrs
     */
    public NetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造方法
     *
     * @param context context
     * @param attrs attrs
     * @param defStyleAttr defStyleAttr
     */
    public NetImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置图片的URL
     *
     * @param url url
     */
    public void setImageUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            ImageLoader.getInstance().loadImage(url, mAsyncView);
        } else {
            if (mDefaultImageResId > 0) {
                setImageResource(mDefaultImageResId);
            } else {
                setImageDrawable(null);
            }
        }
    }

    /**
     * 设置默认图片
     *
     * @param resId resId
     */
    public void setDefaultImage(int resId) {
        mDefaultImageResId = resId;
    }

    /**
     * 设置图片
     *
     * @param drawable
     */
    private void onSetImageDrawable(Drawable drawable) {
        this.setImageDrawable(drawable);
    }

    /**
     * AsyncViewImpl
     */
    private class AsyncViewImpl extends AsyncView {
        @Override
        public void setImageDrawable(Drawable drawable) {
            onSetImageDrawable(drawable);
        }
    }
}
