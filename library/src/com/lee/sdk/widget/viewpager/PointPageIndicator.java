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

package com.lee.sdk.widget.viewpager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class represents the dot page indicator.
 * 
 * @author lihong06
 * @since 2014-3-20
 */
public class PointPageIndicator extends View {
    /** Normal drawable */
    private Drawable mNormalDrawable = null;
    /** Select drawable */
    private Drawable mSelectDrawable = null;
    /** Point size */
    private Rect mNormalPointRect = new Rect();
    /** Point size */
    private Rect mSelectPointRect = new Rect();
    /** Point margin */
    private int mPointMargin = 0;
    /** Cur position */
    private int mPosition = 0;
    /** Point count */
    private int mPointCount = 0;
    
    /**
     * Constructor method
     * 
     * @param context context
     */
    public PointPageIndicator(Context context) {
        super(context);
        
        init(context);
    }
    
    /**
     * Constructor method
     * 
     * @param context context
     * @param attrs attrs
     */
    public PointPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /**
     * Constructor method
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public PointPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }

    /**
     * Initialize
     * 
     * @param context context
     */
    private void init(Context context) {
        
    }
    
    /**
     * Set point size
     * 
     * @param count count
     * @return this
     */
    public PointPageIndicator setPointCount(int count) {
        mPointCount = count;
        return this;
    }
    
    /**
     * Set point margin
     * 
     * @param margin margin
     * @return this;
     */
    public PointPageIndicator setPointMargin(int margin) {
        mPointMargin = margin;
        return this;
    }
    
    /**
     * Set the selected position
     * 
     * @param position the postion.
     * @return this;
     */
    public PointPageIndicator setCurrentPosition(int position) {
        mPosition = position;
        invalidate();
        return this;
    }
    
    /**
     * Set the point size
     * 
     * @param size size
     * @return this;
     */
    public PointPageIndicator setPointSize(int size) {
        mNormalPointRect.set(0, 0, size, size);
        mSelectPointRect.set(0, 0, size, size);
        return this;
    }
    
    /**
     * Set the point drawable 
     * 
     * @param normalResId normalResId
     * @param selectResId selectResId
     * @return this
     */
    public PointPageIndicator setPointDrawableResId(int normalResId, int selectResId) {
        Resources res = getResources();
        return setPointDrawable(res.getDrawable(normalResId), res.getDrawable(selectResId));
    }
    
    /**
     * Set the point drawable
     * 
     * @param normal normal drawable
     * @param select selected drawable
     * @return this;
     */
    public PointPageIndicator setPointDrawable(Drawable normal, Drawable select) {
        mNormalDrawable = normal;
        mSelectDrawable = select;
        
        if (normal instanceof BitmapDrawable) {
            mNormalPointRect.set(0, 0, normal.getIntrinsicWidth(), normal.getIntrinsicHeight());
        }
        
        if (select instanceof BitmapDrawable) {
            mSelectPointRect.set(0, 0, select.getIntrinsicWidth(), select.getIntrinsicHeight());
        }
        
        return this;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mPointCount <= 0) {
            return;
        }
        
        final int count = mPointCount;
        final int margin = mPointMargin;
        final int height = getHeight();
        final int width = getWidth();
        final int selIndex = mPosition;
        final Rect normalRc = mNormalPointRect;
        final Rect selectRc = mSelectPointRect;
        final Drawable dNormal = mNormalDrawable;
        final Drawable dSelect = mSelectDrawable;
        int left = (width - (margin * (count - 1) + normalRc.width() * (count - 1) + selectRc.width())) / 2;
        int top = 0;
        
        for (int index = 0; index < count; ++index) {
            if (index == selIndex) {
                if (null != dSelect) {
                    top = (height - selectRc.height()) / 2;
                    selectRc.offsetTo(left, top);
                    
                    dSelect.setBounds(selectRc);
                    dSelect.draw(canvas);
                }
                left += (selectRc.width() + margin);
            } else {
                if (null != dNormal) {
                    top = (height - normalRc.height()) / 2;
                    normalRc.offsetTo(left, top);
                    dNormal.setBounds(normalRc);
                    dNormal.draw(canvas);
                }
                left += (normalRc.width() + margin);
            }
        }
    }
}
