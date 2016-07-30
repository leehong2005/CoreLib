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

package com.lee.sdk.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

import com.lee.sdk.utils.APIUtils;

/**
 * Performs a flip animation between two views. This implementation is highly
 * inspired by the 3D transition sample in the android SDK, but corrects a huge
 * bug where the destination view remains flipped 180 degree! This is not
 * apparent when the destination is a simple image. Also, this class is more
 * easily reusable, but less customizable by the caller.
 * 
 * The following restrictions apply:
 * <ul>
 * <li>The view that this animation is applied to must be the parent of the two
 * views specified in the constructor
 * <li>The two views specified in the constructor should be siblings, and direct
 * children of the view to which the animation is applied
 * </ul>
 * 
 * The animation is a rotation of the first view, until it reaches 90 degrees,
 * at which point the rotation continues but with the second view. The views
 * also move backwards slightly and them come forward into position again. The
 * net effect is as if you flipped a sheet of paper, where each side shows one
 * of the views. Its a very similar effect to editing the properties of a Mac
 * dashboard widget.
 * 
 * @author rogerta
 * @author Li Hong
 */
public class Flip3dAnimation extends Animation {
    /**
     * The animation listener, you can get callback when the animation is
     * playing.
     */
    public interface OnFlip3dAnimationListener {

        /**
         * Called while the animation is playing.
         * 
         * @param anim
         *            The playing animation.
         * @param interpolatedTime
         *            The time of the animation.
         */
        public void onAnimationPlaying(Animation anim, float interpolatedTime);
    }

    /**
     * This camera to rotate with Y pivot axis.
     */
    private Camera mCamera;
    private View mFromView;
    private View mToView;
    private float mCenterX;
    private float mCenterY;
    private boolean mForward = true;
    private boolean mVisibilitySwapped;

    // Scale values.
    private float mScaleFromX = 1.0f;
    private float mScaleFromY = 1.0f;
    private float mScaleToX = 1.0f;
    private float mScaleToY = 1.0f;

    // Translate values.
    private float mFromXDelta = 0.0f;
    private float mToXDelta = 0.0f;
    private float mFromYDelta = 0.0f;
    private float mToYDelta = 0.0f;

    private OnFlip3dAnimationListener mFlipAnimationListener = null;

    /**
     * Creates a 3D flip animation between two views. If forward is true, its assumed that view1 is
     * "visible" and view2 is "gone" before the animation starts. At the end of the animation, view1
     * will be "gone" and view2 will be "visible". If forward is false, the reverse is assumed.
     * 
     * @param fromView First view in the transition.
     * @param toView Second view in the transition.
     * @param centerX The center of the views in the x-axis.
     * @param centerY The center of the views in the y-axis.
     * @param forward The direction of the animation.
     */
    public Flip3dAnimation(View fromView, View toView, float centerX, float centerY) {

        this.mFromView = fromView;
        this.mToView = toView;
        this.mCenterX = centerX;
        this.mCenterY = centerY;

        setDuration(500);
        setFillAfter(true);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * Set the scale parameters. This method is like {@link ScaleAnimation}.
     * 
     * @param fromX
     * @param toX
     * @param fromY
     * @param toY
     * 
     * @return The Flip3dAnimation object to support lined calling.
     */
    public Flip3dAnimation setScale(float fromX, float toX, float fromY, float toY) {
        mScaleFromX = fromX;
        mScaleToX = toX;
        mScaleFromY = fromY;
        mScaleToY = toY;

        return this;
    }

    /**
     * Set the translate parameters. This method is like
     * {@link TranslateAnimation}.
     * 
     * @param fromXDelta
     * @param toXDelta
     * @param fromYDelta
     * @param toYDelta
     * 
     * @returnThe Flip3dAnimation object to support lined calling.
     */
    public Flip3dAnimation setTranslate(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        mFromXDelta = fromXDelta;
        mToXDelta = toXDelta;
        mFromYDelta = fromYDelta;
        mToYDelta = toYDelta;

        return this;
    }

    /**
     * Set the animation listener.
     * 
     * @param listener
     */
    public void setFlip3dAnimationListener(OnFlip3dAnimationListener listener) {
        mFlipAnimationListener = listener;
    }

    /**
     * Reverse the animation.
     */
    public void reverse() {
        mForward = false;
        View temp = mToView;
        mToView = mFromView;
        mFromView = temp;
    }

    /**
     * @see android.view.animation.Animation#initialize(int, int, int, int)
     */
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);

        mCamera = new Camera();

        if (APIUtils.hasHoneycombMR1()) {
            // The default location is set at 0, 0, -8.
            mCamera.setLocation(0, 0, -16);
        }
    }

    /**
     * @see android.view.animation.Animation#applyTransformation(float,
     *      android.view.animation.Transformation)
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        // Angle around the y-axis of the rotation at the given time. It is
        // calculated both in radians and in the equivalent degrees.
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);
        float time = mForward ? interpolatedTime : (1.0f - interpolatedTime);

        // Once we reach the midpoint in the animation, we need to hide the
        // source view and show the destination view. We also need to change
        // the angle by 180 degrees so that the destination does not come in
        // flipped around. This is the main problem with SDK sample, it does not
        // do this.
        if (interpolatedTime >= 0.5f) {
            degrees -= 180.f;

            if (!mVisibilitySwapped) {
                mFromView.setVisibility(View.INVISIBLE);
                mToView.setVisibility(View.VISIBLE);

                mVisibilitySwapped = true;
            }
        }

        if (null != mFlipAnimationListener) {
            mFlipAnimationListener.onAnimationPlaying(this, time);
        }

        if (mForward) {
            degrees = -degrees;
        }

        final Matrix matrix = t.getMatrix();

        float dx = 0.0f;
        float dy = 0.0f;
        float sx = 1.0f;
        float sy = 1.0f;

        if (mFromXDelta != mToXDelta) {
            dx = mFromXDelta + ((mToXDelta - mFromXDelta) * time);
        }

        if (mFromYDelta != mToYDelta) {
            dy = mFromYDelta + ((mToYDelta - mFromYDelta) * time);
        }

        if (mScaleFromX != 1.0f || mScaleToX != 1.0f) {
            sx = mScaleFromX + ((mScaleToX - mScaleFromX) * time);
        }

        if (mScaleFromY != 1.0f || mScaleToY != 1.0f) {
            sy = mScaleFromY + ((mScaleToY - mScaleFromY) * time);
        }

        mCamera.save();
        mCamera.rotateY(degrees);
        mCamera.getMatrix(matrix);
        mCamera.restore();

        matrix.postScale(sx, sy);
        matrix.postTranslate(dx, dy);

        matrix.preTranslate(-mCenterX, -mCenterY);
        matrix.postTranslate(mCenterX, mCenterY);
    }
}
