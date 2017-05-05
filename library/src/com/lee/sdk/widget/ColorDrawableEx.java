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

package com.lee.sdk.widget;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;

import com.lee.sdk.utils.APIUtils;

/**
 * 2.3版本的{@link ColorDrawable#draw(Canvas)}方法实现有问题，代码如下：
 * <pre>
 * <code>
 *   public void draw(Canvas canvas) {
 *       canvas.drawColor(mState.mUseColor);
 *   }
 * </code>
 * </pre> 
 *  
 * @author lihong06
 * @since 2014-4-18
 */
public class ColorDrawableEx extends ColorDrawable {
    /**
     * Creates a new black ColorDrawable.
     */
    public ColorDrawableEx() {
        super();
    }

    /**
     * Creates a new ColorDrawable with the specified color.
     *
     * @param color The color to draw.
     */
    public ColorDrawableEx(int color) {
        super(color);
    }
    
    @Override
    public void draw(Canvas canvas) {
        // FIX bug：2.3以下ColorDrawable的绘制有一个bug，直接调用draw color.
        if (APIUtils.hasGingerbread()) {
            super.draw(canvas);
        } else {
            canvas.save();
            canvas.clipRect(getBounds());
            super.draw(canvas);
            canvas.restore();
        }
    }
}
