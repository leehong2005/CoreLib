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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 在ImageView上盖上一个形状遮罩 要盖的形状由抽象方法
 * {@link com.baidu.searchbox.ui.MaskedImageView#createMask}构造
 * 
 * @author xulingzhi
 * @since 2014-8-19
 */
public abstract class MaskedImageView extends ImageView {
    /** int value */
    public static final int INT_VALUE = 31;
    /** MASK_XFERMODE */
    private static final Xfermode MASK_XFERMODE;
    /** 遮罩bitmap */
    private Bitmap mMask;
    /** 画笔 */
    private Paint mPaint;
    /** 是否需要遮罩层 */
    private boolean mShouldMasked = false;

    static {
        PorterDuff.Mode localMode = PorterDuff.Mode.DST_IN;
        MASK_XFERMODE = new PorterDuffXfermode(localMode);
    }

    /**
     * 构造方法
     * 
     * @param paramContext paramContext
     */
    public MaskedImageView(Context paramContext) {
        super(paramContext);
        init();
    }

    /**
     * 构造方法
     * 
     * @param paramContext paramContext
     * @param paramAttributeSet paramAttributeSet
     */
    public MaskedImageView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    /**
     * 构造方法
     * 
     * @param paramContext paramAttributeSet
     * @param paramAttributeSet paramAttributeSet
     * @param paramInt paramInt
     */
    public MaskedImageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    /**
     * 启用mask
     */
    protected void enableMask() {
        mShouldMasked = true;
    }

    /**
     * 停用mask
     */
    protected final void disableMask() {
        mShouldMasked = false;
    }

    /**
     * 是否启用了mask
     * 
     * @return true or false
     */
    public final boolean isEnableMask() {
        return mShouldMasked;
    }

    /**
     * 初始化
     */
    private void init() {
        Paint localPaint1 = new Paint();
        this.mPaint = localPaint1;
        this.mPaint.setFilterBitmap(false);
        Paint localPaint2 = this.mPaint;
        Xfermode localXfermode1 = MASK_XFERMODE;
        localPaint2.setXfermode(localXfermode1);
    }

    /**
     * 创建遮罩用的mask bitmap
     * 
     * @return Bitmap
     */
    public abstract Bitmap createMask();

    @Override
    protected void onDraw(Canvas paramCanvas) {
        if (!isEnableMask()) {
            super.onDraw(paramCanvas);
            return;
        }

        float f1 = getWidth();
        float f2 = getHeight();
        int i = paramCanvas.saveLayer(0.0F, 0.0F, f1, f2, null, INT_VALUE);

        // 绘制ImageView自身的逻辑
        super.onDraw(paramCanvas);

        // 创建Mask
        if ((this.mMask == null) || (this.mMask.isRecycled())) {
            Bitmap localBitmap1 = createMask();
            this.mMask = localBitmap1;
        }

        // 绘制Mask
        if (null != this.mMask) {
            paramCanvas.drawBitmap(this.mMask, 0.0F, 0.0F, this.mPaint);
        }

        paramCanvas.restoreToCount(i);
    }
}