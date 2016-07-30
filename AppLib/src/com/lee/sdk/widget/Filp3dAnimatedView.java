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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lee.sdk.anim.Flip3dAnimation;
import com.lee.sdk.anim.Flip3dAnimation.OnFlip3dAnimationListener;

/**
 * The flip 3D animated view, it contains two image view to do the flip animtion.
 * 
 * @author Li Hong
 * @date 2013/03/25
 */
public class Filp3dAnimatedView extends FrameLayout {
    private float mWidth = 100.0f;
    private float mHeight = 100.0f;
    private ImageView mImageViewFrom = null;
    private ImageView mImageViewTo = null;
    private View mAnimatedView = null;
    private OnFlip3dAnimationListener mListener = null;

    /**
     * The constructor method.
     * 
     * @param context
     */
    public Filp3dAnimatedView(Context context) {
        this(context, null);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     */
    public Filp3dAnimatedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public Filp3dAnimatedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(context);
    }

    /**
     * Initialize the views.
     * 
     * @param context
     */
    private void initialize(Context context) {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        mImageViewFrom = new ImageView(context);
        mImageViewTo = new ImageView(context);
        mImageViewTo.setVisibility(View.GONE);
        FrameLayout.LayoutParams paramsFrom = new FrameLayout.LayoutParams(width, height);
        FrameLayout.LayoutParams paramsTo = new FrameLayout.LayoutParams(width, height);
        paramsFrom.gravity = Gravity.CENTER;
        paramsTo.gravity = Gravity.CENTER;

        this.addView(mImageViewTo, paramsTo);
        this.addView(mImageViewFrom, paramsFrom);

        mAnimatedView = this;
    }

    /**
     * Set the bitmaps, from and to.
     * 
     * @param fromBitmap
     * @param toBitmap
     */
    public void setBitmaps(Bitmap fromBitmap, Bitmap toBitmap) {
        mImageViewFrom.setVisibility(View.VISIBLE);
        mImageViewTo.setVisibility(View.GONE);
        mImageViewFrom.setImageBitmap(fromBitmap);
        mImageViewTo.setImageBitmap(toBitmap);
    }

    /**
     * Set the size of the from and to Views.
     * 
     * @param width
     * @param height
     */
    public void setViewSize(int width, int height) {
        if (0 == width || 0 == height) {
            throw new IllegalArgumentException("width or height can not be 0.");
        }

        mWidth = width;
        mHeight = height;
        FrameLayout.LayoutParams paramsFrom = (FrameLayout.LayoutParams) mImageViewFrom.getLayoutParams();
        FrameLayout.LayoutParams paramsTo = (FrameLayout.LayoutParams) mImageViewTo.getLayoutParams();

        paramsFrom.width = width;
        paramsFrom.height = height;
        paramsTo.width = width;
        paramsTo.height = height;

        mImageViewFrom.requestLayout();
        mImageViewTo.requestLayout();
    }

    /**
     * Set flip animation listener.
     * 
     * @param listener
     */
    public void setFlipAnimationListener(Flip3dAnimation.OnFlip3dAnimationListener listener) {
        mListener = listener;
    }

    /**
     * Do animation.
     * 
     * @param animViewFrom The from animation view.
     * @param animViewTo The to animation view.
     * @param duration The animation duration.
     * @param reverse indicate reverse or not.
     * @param listener The animation listener.
     */
    public void doAnimation(View animViewFrom, View animViewTo, long duration, boolean reverse,
            AnimationListener listener) {
        final View animatedView = mAnimatedView;
        float centerX = animatedView.getWidth() / 2;
        float centerY = animatedView.getHeight() / 2;
        float fromXDelta = animViewFrom.getLeft() + animViewFrom.getWidth() / 2 - centerX - animatedView.getLeft();
        float fromYDelta = animViewFrom.getTop() + animViewFrom.getHeight() / 2 - centerY - animatedView.getTop();
        float toXDelta = animViewTo.getLeft() + animViewTo.getWidth() / 2 - centerX - animatedView.getLeft();
        float toYDelta = animViewTo.getTop() + animViewTo.getHeight() / 2 - centerY - animatedView.getTop();
        float fromX = animViewFrom.getWidth() / mWidth;
        float fromY = animViewFrom.getHeight() / mHeight;
        float toX = animViewTo.getWidth() / mWidth;
        float toY = animViewTo.getHeight() / mHeight;

        Flip3dAnimation animation = new Flip3dAnimation(mImageViewFrom, mImageViewTo, centerX, centerY);

        animation.setTranslate(fromXDelta, toXDelta, fromYDelta, toYDelta);
        animation.setScale(fromX, toX, fromY, toY);
        animation.setDuration(duration);
        animation.setAnimationListener(listener);
        animation.setFlip3dAnimationListener(mListener);

        if (reverse) {
            animation.reverse();
        }

        animatedView.startAnimation(animation);
    }
}
