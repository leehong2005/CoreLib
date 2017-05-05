/*
 * Copyright (C) 2015 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

/**
 * 实现了自动剪裁的图片。
 * 
 * <p>
 * 这个类提供了接口{@link #setClipMatrix(float, float)}来设置当前目标的宽度和高度，图片会填充对应的宽和高，按照等比例缩放。
 * 默认的显示模式是居中显示，你也可以通过{@link #setGravity(int)}接口来设置显示模式。
 * </p>
 * 
 * @author lihong06
 * @since 2015-5-11
 */
public class ScaleClipBitmapDrawable extends BitmapDrawable {
    /**
     * 拉伸图片的模式
     * 
     * @author lihong06
     * @since 2015-5-12
     */
    public enum Gravity {
        /** 居中 */
        CENTER,
        /** 水平居中 */
        CENTER_HORIZONTAL,
        /** 垂直居中 */
        CENTER_VERTICAL
    }
    
    /**
     * The world matrix to draw the bitmap.
     */
    private Matrix mMatrix = new Matrix();

    /**
     * The paint to draw the bitmap.
     */
    private Paint mPaint = new Paint();
    
    /**
     * 图片显示的模式
     */
    private Gravity mGravity = Gravity.CENTER;

    /**
     * 构造方法
     * 
     * @param bitmap The bitmap to draw.
     */
    @SuppressWarnings("deprecation")
    public ScaleClipBitmapDrawable(Bitmap bitmap) {
        super(bitmap);
    }
    
    /**
     * 构造方法
     * 
     * @param res res
     * @param bitmap bitmap
     */
    public ScaleClipBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    @Override
    public void draw(Canvas canvas) {
        // Do not call super.draw(), we do this is for special specification.
        // Draw the bitmap width the specified matrix.
        if (null != getBitmap()) {
            canvas.drawBitmap(this.getBitmap(), this.mMatrix, mPaint);
        } else {
            super.draw(canvas);
        }
    }
    
    /**
     * 设置图片的显示模式
     * 
     * @param gravity gravity
     */
    public void setGravity(Gravity gravity) {
        mGravity = gravity;
    }

    /**
     * Calculate the matrix of a bitmap to display.
     * 
     * @param dstWidth The destination width.
     * @param dstHeight The destination height.
     */
    public void setClipMatrix(float dstWidth, float dstHeight) {
        float width = dstWidth;
        float height = dstHeight;

        // Get the size of bitmap.
        float bitmapHeight = this.getIntrinsicHeight();
        float bitmapWidth = this.getIntrinsicWidth();

        // Check the bitmap data.
        if (bitmapHeight == 0 || bitmapWidth == 0) {
            return;
        }

        // Calculate the scale ratio on both axis x and y.
        float scaleRatioX = width / bitmapWidth;
        float scaleRatioY = height / bitmapHeight;

        // Get the final scale which we need to apply to image to let it fit the
        // clip region.
        float scale = Math.max(scaleRatioX, scaleRatioY);

        // Preventing to zoom in the image, if you want the image to be zoomed
        // in, please remove following handling.
        if (scale < 1.0f) {
            scale = 1.0f;
        }

        // Calculate the final size of scaled image.
        float newWidth = scale * bitmapWidth;
        float newHeight = scale * bitmapHeight;

        // Reset the matrix to identity matrix to let every calculation begin
        // with a identify one.
        mMatrix.reset();

        // 1. Scale the image with the calculated scale ration, the pivot of
        // scaling is set to the center of image.
        mMatrix.setScale(scale, scale);

        // 2. Then translate the image to let the longer edge of it fit the clip
        // region.
        switch (mGravity) {
        case CENTER:
            mMatrix.postTranslate((width - newWidth) / 2.0f, (height - newHeight) / 2.0f);
            break;
            
        case CENTER_HORIZONTAL:
            mMatrix.postTranslate((width - newWidth) / 2.0f, 0);
            break;
            
        case CENTER_VERTICAL:
            mMatrix.postTranslate(0, (height - newHeight) / 2.0f);
            break;
        }
    }
}
