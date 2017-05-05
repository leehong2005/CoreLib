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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.widget.ImageView;

import com.lee.sdk.utils.APIUtils;

/**
 * 圆角带阴影图标。
 * 在使用时: <li>调用{@link #setIconSize(int, int)}设置图标的宽高（不包括阴影）</li> <li>调用
 * {@link #setShadowLayer(float, float, float, int)}设置阴影的属性</li> <li>调用
 * {@link #setRoundRect(int, int)}设置圆角属性</li>
 * 
 * <pre class="prettyprint">
 * RoundRectImageView iv = (RoundRectImageView) findViewById(R.id.imageview);
 * iv.setIconSize(75, 75); // 图标大小为75px * 75px
 * iv.setRoundRect(14, 14); // 圆角半径为14 px
 * iv.setShadowLayer(3, 0, 1, Color.argb((int) (0.4f * 255), 0, 0, 0)); // 阴影大小为3px,
 *                                                                      // 位移（y轴)为1px,颜色为40%透明黑色
 * </pre>
 * 
 * @since 2013-6-5
 */
public class RoundRectImageView extends ImageView {
    /** 阴影paint */
    private Paint mOutShadowPaint = new Paint();
    /** 用于绘制的Bitmap，因为Drawable无法使用XFermode绘制，故需转换成Bitmap */
    private Bitmap mResolvedBitmap;
    /** 绘制Bitmap的Paint */
    private Paint mBitmapPaint = new Paint();
    /** 绘制图标区域的Paint */
    private Paint mRoundRectPaint = new Paint();
    /** 圆角框路径 */
    private Path mRoundPath = new Path();
    /** 直角框 */
    private RectF mRoundRect = new RectF();
    /** round rect x */
    private int mRoundRectX = 0;
    /** round rect y */
    private int mRoundRectY = 0;
    /** 图标宽度 */
    private int mIconWidth = -1;
    /** 图标高度 */
    private int mIconHeight = -1;
    /** 按下压黑效果 */
    private int mPressColor = 0;

    /**
     * @param context context
     */
    public RoundRectImageView(Context context) {
        super(context);
        init();
    }

    /**
     * 
     * @param context context
     * @param attrs attrs
     */
    public RoundRectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public RoundRectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化
     */
    @SuppressLint("NewApi")
    private void init() {
        if (APIUtils.hasHoneycomb()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mOutShadowPaint.setColor(Color.TRANSPARENT);
        mOutShadowPaint.setStyle(Style.FILL);
        mOutShadowPaint.setAntiAlias(true);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
        mBitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        mRoundRectPaint.setColor(Color.argb(255, 0, 0, 0));// SUPPRESS CHECKSTYLE 黑色
        mRoundRectPaint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mResolvedBitmap != null && (mRoundRectX != 0 || mRoundRectY != 0)) {
            int save = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.MATRIX_SAVE_FLAG
                    | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                    | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            // super.onDraw(canvas);
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mRoundRect.set(0, 0, getWidth() - getPaddingLeft() - getPaddingRight(), getHeight() - getPaddingTop()
                    - getPaddingBottom());
            mRoundPath.reset();
            mRoundPath.addRoundRect(mRoundRect, mRoundRectX, mRoundRectY, Direction.CW);

            canvas.drawPath(mRoundPath, mRoundRectPaint);
            canvas.drawBitmap(mResolvedBitmap, null, mRoundRect, mBitmapPaint);
            if (mPressColor != 0
                    && StateSet.stateSetMatches(PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET, getDrawableState())) {
                canvas.drawColor(mPressColor, Mode.SRC_OVER);
            }

            canvas.clipPath(mRoundPath, Op.DIFFERENCE);
            canvas.drawColor(0x00000000, Mode.CLEAR);
            canvas.drawPath(mRoundPath, mOutShadowPaint);

            canvas.restoreToCount(save);

        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mIconWidth != -1 && mIconHeight != -1) {
            int measuredWidth = mIconWidth + getPaddingLeft() + getPaddingRight();
            int measuredHeight = mIconHeight + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(measuredWidth, measuredHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 设置圆角半径
     * 
     * @param x 圆角x轴半径
     * @param y 圆角轴半径
     */
    public void setRoundRect(int x, int y) {
        mRoundRectX = x;
        mRoundRectY = y;
        resolveImage();
    }

    /**
     * 设置阴影
     * 
     * @param radius radius
     * @param dx dx
     * @param dy dy
     * @param color 阴影颜色
     */
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        RectF rect = new RectF(radius, radius, radius, radius);
        rect.offset(dx, dy);
        int left = rect.left < 0 ? 0 : (int) (rect.left + 0.5f); // SUPPRESS CHECKSTYLE 浮点取整
        int right = rect.right < 0 ? 0 : (int) (rect.right + 0.5f); // SUPPRESS CHECKSTYLE 浮点取整
        int top = rect.top < 0 ? 0 : (int) (rect.top + 0.5f); // SUPPRESS CHECKSTYLE 浮点取整
        int bottom = rect.bottom < 0 ? 0 : (int) (rect.bottom + 0.5f); // SUPPRESS CHECKSTYLE 浮点取整
        setPadding(left, top, right, bottom);
        mOutShadowPaint.setShadowLayer(radius, dx, dy, color);
    }

    /**
     * 设置Logo的宽高，不包含阴影
     * 
     * @param width width
     * @param height height
     */
    public void setIconSize(int width, int height) {
        mIconWidth = width;
        mIconHeight = height;
        resolveImage();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        resolveImage();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        resolveImage();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        resolveImage();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        resolveImage();
    }

    /**
     * 将Drawable转化成bitmap
     */
    private void resolveImage() {
        Drawable d = getDrawable();
        if (d == null) {
            return;
        }

        if (mRoundRectX == 0 && mRoundRectY == 0) {
            return;
        }

        if (mIconWidth == -1 && mIconHeight == -1) {
            return;
        }

        if (d instanceof BitmapDrawable) {
            mResolvedBitmap = ((BitmapDrawable) d).getBitmap();
        } else {
            try {
                Canvas newCanvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Config.ARGB_8888);
                newCanvas.setBitmap(bitmap);
                d.draw(newCanvas);
                mResolvedBitmap = bitmap;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }

        super.setImageDrawable(null);
    }

    /**
     * 设置按下时压黑的颜色
     * 
     * @param color color
     */
    public void setPressedColor(int color) {
        mPressColor = color;
    }

    /**
     * 设置按下时压黑的颜色
     * 
     * @param colorRes color resource
     */
    public void setPressedColorResource(int colorRes) {
        mPressColor = getResources().getColor(colorRes);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mPressColor != 0) {
            invalidate();
        }
    }
}
