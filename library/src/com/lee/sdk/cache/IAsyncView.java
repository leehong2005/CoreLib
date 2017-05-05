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

import android.graphics.drawable.Drawable;


/**
 * This interface defines the methods for the View which to show the bitmap, typically 
 * is the image view.
 * 
 * <p>
 * NOTE:
 * Suggest caller uses {@link AsyncView} class which is extended from {@link IAsyncView}.
 * In future, this interface may be rename, in face, this interface is like listener or callback.
 * </p>
 * 
 * <p>
 * 建议使用者直接使用 {@link AsyncView} 类，而不是直接实现该接口。
 * </p>
 * 
 * @author LiHong
 * @date 2012/10/25
 */
public interface IAsyncView {
    /**
     * Set the drawable to the view
     * 
     * @param drawable
     */
    public void setImageDrawable(Drawable drawable);
    
    /**
     * Set the drawable to the view to save.
     * 
     * @param drawable
     */
    public void setAsyncDrawable(Drawable drawable);

    /**
     * Return the drawable object which is set by {@link #setAsyncDrawable(Drawable)} method.
     * 
     * @return The object which just set by calling {@link #setAsyncDrawable(Drawable)} method.
     */
    public Drawable getAsyncDrawable();
    
    /**
     * 是否支持GIF
     * 
     * @return true/false
     */
    public boolean isGifSupported();
}
//CHECKSTYLE:ON
