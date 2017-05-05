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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;

import com.lee.sdk.R;
import com.lee.sdk.utils.DensityUtils;

/**
 * The class is for wheel effect widget, like the wheel in iOS. This wheel view is based Adapter, you can put anything
 * in this view, all decided by you custom Adapter. 
 * 
 * <p>
 * You can call this method {@link #setSoundEffectsEnabled(boolean)} to enable or disable the play sound effect.
 * </p>
 * 
 * <p>
 * You should care the core methods:
 * <li>{@link #setOnEndFlingListener(OnEndFlingListener)}, set the wheel fling listener.
 * <li>{@link #setAdapter(android.widget.SpinnerAdapter)}, set the adapter for wheel filling view.
 * <li>{@link #setSelection(int)}, select an item.
 * <li>{@link #getSelectedItemPosition()}, get the selected item position.
 * <li>{@link #setScrollCycle(boolean)}, scroll cycle or not.
 * </p>
 * 
 * @author lihong06
 * @since 2014-3-6
 */
public class WheelView extends BdGallery {
    /**
     * Shadow colors
     */
    private static final int[] SHADOWS_COLORS = { 0xFF111111, 0x00AAAAAA, 0x00AAAAAA };
    
    /**
     * The selector.
     */
    private Drawable mSelectorDrawable = null;
    
    /**
     * The bound rectangle of selector.
     */
    private Rect mSelectorBound = new Rect();
    /**
     * The top shadow.
     */
    private Drawable mTopShadow = null;
    
    /**
     * The bottom shadow.
     */
    private Drawable mBottomShadow = null;

    /**
     * The height of the shadow.
     */
    private int mShadowHeight;
    
    /** 
     * Default size, normally, the selector size is equal with the adapter view's width.
     */
    private int mDefSelectorSize;

    /**
     * The constructor method.
     * 
     * @param context
     */
    public WheelView(Context context) {
        super(context);

        initialize(context);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     */
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize(context);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(context);
    }

    /**
     * Initialize.
     * 
     * @param context
     */
    private void initialize(Context context) {
        this.setVerticalScrollBarEnabled(false);
        this.setSlotInCenter(true);
        this.setOrientation(BdGallery.VERTICAL);
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        this.setUnselectedAlpha(1.0f);

        // This lead the onDraw() will be called.
        this.setWillNotDraw(false);

        // The selector rectangle drawable.
        this.mSelectorDrawable = getContext().getResources().getDrawable(R.drawable.sdk_wheel_val);
        this.mTopShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        this.mBottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);

        // The default background.
        this.setBackgroundResource(R.drawable.sdk_wheel_bg);
        // Disable the sound effect default.
        this.setSoundEffectsEnabled(false);
        // Set the default size.
        this.mDefSelectorSize = DensityUtils.dip2px(getContext(), 50);
    }
    
    /**
     * Set the selector for the wheel view.
     * 
     * @param selector the selector drawable.
     */
    public void setSelectorDrawable(Drawable selector) {
        if (mSelectorDrawable != selector) {
            mSelectorDrawable = selector;
            invalidate();
        }
    }

    /**
     * Set the shadow drawable for top and bottom.
     * 
     * @param topShadow top shadow
     * @param bottomShadow bottom shadow
     */
    public void setShadowDrawable(Drawable topShadow, Drawable bottomShadow) {
        mTopShadow = topShadow;
        mBottomShadow = bottomShadow;
        invalidate();
    }
    
    /**
     * Set the height of the shadow drawable.
     * 
     * @param shadowHeight height of shadow.
     */
    public void setShadowDrawableHeight(int shadowHeight) {
        mShadowHeight = shadowHeight;
    }
    
    /**
     * Called by draw to draw the child views. This may be overridden by derived classes to gain
     * control just before its children are drawn (but after its own view has been drawn).
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // After draw child, we do the following things:
        // +1, Draw the center rectangle.
        // +2, Draw the shadows on the top and bottom.
        drawCenterRect(canvas);
        drawShadows(canvas);
    }

    /**
     * Current do not support horizontal, if you use it, IllegalArgumentException exception will be
     * thrown.
     * 
     * @param orientation must be {@link BdGallery#VERTICAL}.
     */
    @Override
    public void setOrientation(int orientation) {
        if (BdGallery.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("The orientation must be VERTICAL");
        }

        super.setOrientation(orientation);
    }
    
    /**
     * Call when the ViewGroup is layout.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (BdGallery.HORIZONTAL == getOrientation()) {
            calcSelectorBoundHorizontal();
        } else {
            calcSelectorBoundVertical();
        }
    }

    /**
     * @see com.lee.sdk.widget.BdGallery#setSelectedPositionInt(int)
     */
    @Override
    protected void selectionChanged() {
        super.selectionChanged();

        playSoundEffect(SoundEffectConstants.CLICK);
    }

    /**
     * Draw the selector drawable.
     * 
     * @param canvas canvas
     */
    private void drawCenterRect(Canvas canvas) {
        if (null != mSelectorBound && !mSelectorBound.isEmpty()) {
            if (null != mSelectorDrawable) {
                mSelectorDrawable.setBounds(mSelectorBound);
                mSelectorDrawable.draw(canvas);
            }
        }
    }

    /**
     * Draw the shadow
     * 
     * @param canvas canvas
     */
    private void drawShadows(Canvas canvas) {
        if (BdGallery.HORIZONTAL == getOrientation()) {
            drawShadowsHorizontal(canvas);
        } else {
            drawShadowsVertical(canvas);
        }
    }

    /**
     * Draw the shadow for horizontal
     * 
     * @param canvas canvas
     */
    private void drawShadowsHorizontal(Canvas canvas) {
        // TODO: current do not support horizontal layout.
    }

    /**
     * Draw the shadow for vertical
     * 
     * @param canvas canvas
     */
    private void drawShadowsVertical(Canvas canvas) {
        if (mShadowHeight <= 0) {
            // 取选择区域的高度2倍
            mShadowHeight = (int) (2.0 * mSelectorBound.height());
            // 高度不能超过选择区域
            mShadowHeight = Math.min(mShadowHeight, mSelectorBound.top);
        }
        
        final int height = mShadowHeight;
        if (null != mTopShadow) {
            mTopShadow.setBounds(0, 0, getWidth(), height);
            mTopShadow.draw(canvas);
        }

        if (null != mBottomShadow) {
            mBottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
            mBottomShadow.draw(canvas);
        }
    }

    /**
     * Calculate the selector bound for horizontal
     */
    private void calcSelectorBoundHorizontal() {
        int galleryCenter = getCenterOfGallery();
        View v = this.getChildAt(0);
        int width = (null != v) ? v.getMeasuredWidth() : mDefSelectorSize;
        int left = galleryCenter - width / 2;
        int right = left + width;

        mSelectorBound.set(left, getPaddingTop(), right, getHeight() - getPaddingBottom());
    }

    /**
     * Calculate the selector bound for vertical
     */
    private void calcSelectorBoundVertical() {
        int galleryCenter = getCenterOfGallery();
        View v = this.getChildAt(0);

        int height = (null != v) ? v.getMeasuredHeight() : mDefSelectorSize;
        int top = galleryCenter - height / 2;
        int bottom = top + height;

        mSelectorBound.set(getPaddingLeft(), top, getWidth() - getPaddingRight(), bottom);
    }
}
