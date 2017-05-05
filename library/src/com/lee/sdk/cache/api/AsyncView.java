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

import android.graphics.drawable.Drawable;

import com.lee.sdk.cache.IAsyncView;

/**
 * 这个类实现了{@link IAsyncView}接口，在使用{@link ImageLoader#loadImage(Object, IAsyncView)}
 * 方法时，可以直接传入AsyncView的派生类。或者AsyncView作为类的成员变量
 * 
 * <br>
 * 这个类的作用就是让使用者不需要去实现过多的不必须的方法，让派生类更加关注图片显示的逻辑
 * 
 * @author LiHong
 * @since 2013-11-24
 */
public class AsyncView implements IAsyncView {
    /**
     * 异步加载的drawable，里面包含了AsyncTask对象，不能删除
     */
    private Drawable mAsyncDrawable = null;

    /**
     * 是否支持GIF
     */
    private boolean mIsSupportGif = false;

    @Override
    public void setImageDrawable(Drawable drawable) {
        // do nothing
    }

    @Override
    public void setAsyncDrawable(Drawable drawable) {
        mAsyncDrawable = drawable;
    }

    @Override
    public Drawable getAsyncDrawable() {
        return mAsyncDrawable;
    }

    /**
     * 设置是否支持GIF
     *
     * @param supportGif true/false
     */
    public void setSupportGif(boolean supportGif) {
        mIsSupportGif = supportGif;
    }

    @Override
    public boolean isGifSupported() {
        return mIsSupportGif;
    }
}
