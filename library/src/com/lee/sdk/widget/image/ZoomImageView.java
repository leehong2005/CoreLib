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

package com.lee.sdk.widget.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.lee.sdk.cache.IAsyncView;

/**
 * 这个类支持手势缩放功能。
 * 
 * @author Li Hong
 * @since 2013-7-23
 */
public class ZoomImageView extends ImageViewTouch implements IAsyncView {
    /**
     * 设置图片的Listener
     * 
     * @author lihong06
     * @since 2014-2-22
     */
    public interface OnSetImageBitmapListener {
        /**
         * 设置bitmap时调用
         * 
         * @param bitmap bitmap
         */
        void onSetImageBitmap(Bitmap bitmap);
        
        /**
         * 设置drawable时调用
         * 
         * @param drawable drawable
         */
        void onSetImageDrawable(Drawable drawable);
    }
    
    /**
     * 更新滑动矩形的监听器
     * 
     * @author lihong06
     * @since 2014-4-23
     */
    public interface OnZoomImageListener {
        /**
         * 滑动图片
         * 
         * @param view view
         * @param dx dx
         * @param dy dy
         * @return handled
         */
        boolean onPanBy(ZoomImageView view, double dx, double dy);
        
        /**
         * 滑动是调用
         * 
         * @param view view
         * @param e1 e1
         * @param e2 e2
         * @param velocityX velocityX
         * @param velocityY velocityY
         * @return handled or not
         */
        boolean onFling(ZoomImageView view, MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
        
        /**
         * OnScall 
         * @param view view
         * @param e1 e1
         * @param e2 e2
         * @param distanceX distanceX
         * @param distanceY distanceY
         * @return handled
         */
        boolean onScroll(ZoomImageView view, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
    }
    
    /**异步加载的drawable，里面包含了AsyncTask对象，不能删除*/
    private Drawable mAsyncDrawable = null;
    /**表示是否设置了bitmap*/
    private boolean mHasSetBitmap = false;
    /**最小缩放率*/
    private float mMinZoom = ImageViewTouchBase.ZOOM_INVALID;
    /**最大缩放率*/
    private float mMaxZoom = ImageViewTouchBase.ZOOM_INVALID;
    /** Listener */
    private OnSetImageBitmapListener mListener = null;
    /** 更新滑动矩形的listener */
    private OnZoomImageListener mZoomImageListener = null;
    
    /**
     * The constructor method.
     * 
     * @param context context
     */
    public ZoomImageView(Context context) {
        super(context);
    }
    
    /**
     * The constructor method.
     * 
     * @param context context
     * @param attrs attrs
     */
    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * The constructor method.
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAsyncDrawable(Drawable drawable) {
        mAsyncDrawable = drawable;
    }

    @Override
    public Drawable getAsyncDrawable() {
        return mAsyncDrawable;
    }
    
    @Override
    public boolean isGifSupported() {
        return true;
    }
    
    /**
     * Set the bitmap to the View.
     * 
     * @param bitmap The bitmap object.
     */
    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (null != mListener) {
            mListener.onSetImageBitmap(bitmap);
        }
        
        //super.setImageBitmap(bitmap);
        super.setImageBitmap(bitmap, null, mMinZoom, mMaxZoom);
        mHasSetBitmap = (bitmap != null);
    }
    
    /**
     * Set the drawable to the view, it is same with {@link #setImageBitmap(Bitmap bitmap)} method.
     * 
     * @param drawable drawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        if (null != mListener) {
            mListener.onSetImageDrawable(drawable);
        }
        
        super.setImageDrawable(drawable, null, mMinZoom, mMaxZoom);
        mHasSetBitmap = (drawable != null);
    }
    
    /**
     * 表示当前ImageView上面是否设置了bitmap。
     * 
     * @return true表示已经设置了，否则返回false。
     */
    public boolean hasSetBitmap() {
        return mHasSetBitmap;
    }
    
    /**
     * 设置绽放的范围。
     * 
     * @param minZoom 最小缩放率。
     * @param maxZoom 最大缩放率。
     */
    public void setZoomRange(float minZoom, float maxZoom) {
        mMaxZoom = maxZoom;
        mMinZoom = minZoom;
    }
    
    /**
     * Set the listener 
     * 
     * @param listener listener
     */
    public void setOnSetImageBitmapListener(OnSetImageBitmapListener listener) {
        mListener = listener;
    }
    
    /**
     * Set the listener.
     * 
     * @param listener listener
     */
    public void setOnUpdateRectListener(OnZoomImageListener listener) {
        mZoomImageListener = listener;
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        boolean handled = false;
        if (null != mZoomImageListener) {
            handled = mZoomImageListener.onScroll(this, e1, e2, distanceX, distanceY);
        }
        
        if (!handled) {
            handled = super.onScroll(e1, e2, distanceX, distanceY);
        }
        
        return handled;
    }

    @Override
    protected void panBy(double dx, double dy) {
        boolean handled = false;
        if (null != mZoomImageListener) {
            handled = mZoomImageListener.onPanBy(this, dx, dy);
        }
        
        if (!handled) {
            super.panBy(dx, dy);
        }
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean handled = false;
        if (null != mZoomImageListener) {
            handled = mZoomImageListener.onFling(this, e1, e2, velocityX, velocityY);
        } 
        
        if (!handled) {
            handled = super.onFling(e1, e2, velocityX, velocityY);
        }
        
        return handled;
    }
}
