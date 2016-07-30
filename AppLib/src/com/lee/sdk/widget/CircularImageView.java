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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * 圆形Imageview
 * 
 * @author xulingzhi
 * @since 2014-8-19
 */
public class CircularImageView extends MaskedImageView {
    /** image color */
    public static final int IMAGE_COLOR = -16777216;

    /**
     * 构造方法
     * 
     * @param paramContext paramContext
     */
    public CircularImageView(Context paramContext) {
        super(paramContext);
    }

    /**
     * 构造方法
     * 
     * @param paramContext paramContext
     * @param paramAttributeSet paramAttributeSet
     */
    public CircularImageView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    /**
     * 构造方法
     * 
     * @param paramContext paramContext
     * @param paramAttributeSet paramAttributeSet
     * @param paramInt paramInt
     */
    public CircularImageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    @Override
    public Bitmap createMask() {
        int i = getWidth();
        int j = getHeight();
        try {
            Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
            Bitmap localBitmap = Bitmap.createBitmap(i, j, localConfig);
            Canvas localCanvas = new Canvas(localBitmap);
            Paint localPaint = new Paint(1);
            localPaint.setColor(IMAGE_COLOR);
            // 计算padding
            int left = getPaddingLeft();
            int top = getPaddingTop();
            int right = getWidth() - getPaddingRight();
            int bottom = getHeight() - getPaddingBottom();
            RectF localRectF = new RectF(left, top, right, bottom);
            localCanvas.drawOval(localRectF, localPaint);
            return localBitmap;
        } catch (OutOfMemoryError m) {
            return null;
        }
    }
}
