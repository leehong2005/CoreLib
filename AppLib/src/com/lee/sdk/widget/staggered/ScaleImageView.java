/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.widget.staggered;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

//CHECKSTYLE:OFF
/**
 * This view will auto determine the width or height by determining if the
 * height or width is set and scale the other dimension depending on the images
 * dimension
 * 
 * This view also contains an ImageChangeListener which calls changed(boolean
 * isEmpty) once a change has been made to the ImageView
 * 
 * 这个类放到瀑布流的控件中，为了提高性能，我们自绘制取得的图片，因为系统的方法
 * 在调用setImageDrawable()方法时，它会判断图片大小是否一样大，如果不一样大，
 * 会导致requestLayout()，为了屏蔽这一点，所有的图片都自己绘制。
 * 
 * @author Li Hong
 * 
 * @since 2013-07-25
 */
public class ScaleImageView extends ImageView {
    /**DEBUG*/
    private static final boolean DEBUG = false;
    /**当前显示的bitmap*/
    private Bitmap mCurrentBitmap;
    /**图片改变的监听器接口*/
    private ImageChangeListener mImageChangeListener;
    /**是否绽放到宽度的标志量，它决定是否依赖宽度来测量高度*/
    private boolean mScaleToWidth = false;
    /**图片宽度*/
    private int mImageWidth;
    /**图片高度*/
    private int mImageHeight;
    /**空图片*/
    private Drawable mEmptyDrawable;
    /**空图片的大小*/
    private Rect mEmptyDrawableRect;
    /**空图片的大小*/
    private Rect mFrame;
    
    /**
     * 构造方法
     * 
     * @param context
     */
    public ScaleImageView(Context context) {
        super(context);
        init();
    }

    /**
     * 构造方法
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 构造方法
     * 
     * @param context
     * @param attrs
     */
    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.setScaleType(ScaleType.CENTER_INSIDE);
        //this.mEmptyDrawable = getResources().getDrawable(R.drawable.picture_loading_padding);
    }

    /**
     * 回收当前显示在ImageView上面的bitmap
     */
    public void recycle() {
        setImageBitmap(null);
        if ((this.mCurrentBitmap == null) || (this.mCurrentBitmap.isRecycled())) {
            return;
        }
        
        try {
            this.mCurrentBitmap.recycle();
            this.mCurrentBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see android.widget.ImageView#setImageBitmap(android.graphics.Bitmap)
     */
//    @Override
//    public void setImageBitmap(Bitmap bm) {
//        if (mCurrentBitmap != bm) {
//            mCurrentBitmap = bm;
//            invalidate();
//        }
//        
//        // 这里不需要调用父类的方法，因为我们打算自绘制图片。
//        //super.setImageBitmap(mCurrentBitmap);
//        
//        if (mImageChangeListener != null) {
//            mImageChangeListener.changed((mCurrentBitmap == null));
//        }
//    }

//    @Override
//    public void setImageDrawable(Drawable d) {
//        
//        // 这里不需要调用父类的方法，因为我们打算自绘制图片。
//        //super.setImageDrawable(d);
//        
//        if (d instanceof BitmapDrawable) {
//            mCurrentBitmap = ((BitmapDrawable) d).getBitmap();
//        } else {
//            mCurrentBitmap = null;
//        }
//        
//        if (mImageChangeListener != null) {
//            mImageChangeListener.changed((d == null));
//        }
//    }

    /**
     * 图片改变的监听器接口
     */
    public interface ImageChangeListener {
        // a callback for when a change has been made to this imageView
        public void changed(boolean isEmpty);
    }

    /**
     * 得到设置的图片改变监听器接口对象
     * 
     * @return
     */
    public ImageChangeListener getImageChangeListener() {
        return mImageChangeListener;
    }

    /**
     * 设置图片改变监听器接口对象
     * 
     * @param imageChangeListener
     */
    public void setImageChangeListener(ImageChangeListener imageChangeListener) {
        this.mImageChangeListener = imageChangeListener;
    }

    /**
     * 设置图片的宽度
     * 
     * @param w
     */
    public void setImageWidth(int w) {
        if (mImageWidth != w) {
            mImageWidth = w;
            requestLayout();
        }
    }

    /**
     * 设置图片的高度
     * 
     * @param h
     */
    public void setImageHeight(int h) {
        if (mImageHeight != h) {
            mImageHeight = h;
            requestLayout();
        }
    }
    
    /**
     * 设置空图片，当没有设置bitmap时，这个图片就会显示出来。
     * 
     * @param drawable drawble.
     */
    public void setEmptyDrawable(Drawable drawable) {
        if (mEmptyDrawable != drawable) {
            mEmptyDrawable = drawable;
            
            // 当前图片为空
            if (null == mCurrentBitmap) {
                invalidate();
            }
        }
    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
        // 这里不需要调用父类的方法，因为我们打算自绘制图片。
        //super.onDraw(canvas);
        
        // 如果当前图片为空，绘制出默认的图片
        if (null == mCurrentBitmap) {
            if (null != mEmptyDrawable) {
                if (null == mEmptyDrawableRect) {
                    mEmptyDrawableRect = new Rect(0, 0, 
                            mEmptyDrawable.getIntrinsicWidth(), 
                            mEmptyDrawable.getIntrinsicHeight());
                }
                
                int left = (getWidth()  - mEmptyDrawableRect.width()) / 2;
                int top  = (getHeight() - mEmptyDrawableRect.height()) / 2;
                mEmptyDrawableRect.offsetTo(left, top);
                mEmptyDrawable.setBounds(mEmptyDrawableRect);
                mEmptyDrawable.draw(canvas);
            } else {
                super.onDraw(canvas);
            }
        } else {
            if (null == mFrame) {
                mFrame = new Rect();
            }
            
            // 绘制出当前的图片，填充满View大小
            final Rect rect = mFrame;
            rect.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(mCurrentBitmap, null, rect, null);
        }
        
        if (DEBUG) {
            if (mNetRequestStarted) {
                canvas.drawColor(Color.argb(128, 0, 0, 255));
            }

            if (mNetRequestError) {
                canvas.drawText("Error", 10, getHeight() / 2, mTextPaint);
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        /**
         * if both width and height are set scale width first. modify in future
         * if necessary
         */
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            mScaleToWidth = true;
        } else if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            mScaleToWidth = false;
        } else {
            throw new IllegalStateException("width or height needs to be set to match_parent or a specific dimension");
        }
        
        if (mImageWidth == 0) {
            // nothing to measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        } else {
            if (mScaleToWidth) {
                int iw = mImageWidth;
                int ih = mImageHeight;
                int heightC = width * ih / iw;
                if (height > 0) {
                    if (heightC > height) {
                        // do NOT let height be greater then set max
                        heightC = height;
                        width = (int) ((float) heightC * iw / ih + 0.5f); // SUPPRESS CHECKSTYLE 四舍五入
                    }
                }

                this.setScaleType(ScaleType.CENTER_CROP);
                setMeasuredDimension(width, heightC);
            } else {
                // need to scale to height instead
                int marg = 0;
                int iw = mImageWidth;
                int ih = mImageHeight;

                width = height * iw / ih;
                height -= marg;
                setMeasuredDimension(width, height);
            }
        }
    }
    
    // TODO: for test
    private Paint mTextPaint = new Paint();
    private boolean mNetRequestStarted = false;
    private boolean mNetRequestError = false;
    
    protected void onStartLoadUrl(boolean started) {
        if (DEBUG) {
            if (this.mNetRequestStarted != started) {
                this.mNetRequestStarted = started;
                invalidate();
            }
        }
    }
    
    protected void onLoadUrlError(boolean error) {
        if (DEBUG) {
            if (mNetRequestError != error) {
                mNetRequestError = error;
                mTextPaint.setColor(Color.RED);
                mTextPaint.setTextSize(25);
                invalidate();
            }
        }
    }
}
//CHECKSTYLE:ON
