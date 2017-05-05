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
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;

/**
 * This class implements the gallery flow effect.
 * 
 * @author Li Hong
 * @date 2012/11/12
 */
public class GalleryFlow extends BdGallery {
    /**
     * The camera class is used to 3D transformation matrix.
     */
    private Camera mCamera = new Camera();

    /**
     * The max rotation angle.
     */
    private int mMaxRotationAngle = 60;

    /**
     * The max zoom value (Z axis).
     */
    private int mMaxZoom = -120;

    /**
     * The center of the gallery.
     */
    private int mCoveflowCenter = 0;

    /**
     * The constructor method.
     * 
     * @param context
     */
    public GalleryFlow(Context context) {
        this(context, null);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     */
    public GalleryFlow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public GalleryFlow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Enable set transformation.
        this.setStaticTransformationsEnabled(true);
        // Enable set the children drawing order.
        this.setChildrenDrawingOrderEnabled(true);
        // Slot in center
        this.setSlotInCenter(true);
    }

    /**
     * Get the maximum rotation angle.
     * 
     * @return
     */
    public int getMaxRotationAngle() {
        return mMaxRotationAngle;
    }

    /**
     * Set the maximum rotation angle.
     * 
     * @param maxRotationAngle
     */
    public void setMaxRotationAngle(int maxRotationAngle) {
        mMaxRotationAngle = maxRotationAngle;
    }

    /**
     * The maximum zoom.
     * 
     * @return
     */
    public int getMaxZoom() {
        return mMaxZoom;
    }

    /**
     * Set the maximum zoom, default value is -120.
     * 
     * @param maxZoom
     */
    public void setMaxZoom(int maxZoom) {
        mMaxZoom = maxZoom;
    }

    /**
     * @see android.widget.Gallery#getChildDrawingOrder(int, int)
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        // Current selected index.
        int selectedIndex = getSelectedItemPosition() - getFirstVisiblePosition();
        if (selectedIndex < 0) {
            return i;
        }

        if (i < selectedIndex) {
            return i;
        } else if (i >= selectedIndex) {
            return childCount - 1 - i + selectedIndex;
        } else {
            return i;
        }
    }

    /**
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCoveflowCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Calculate the center of the specified view.
     * 
     * @param view
     * 
     * @return
     */
    private int getCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }

    /**
     * @see android.widget.Gallery#getChildStaticTransformation(android.view.View,
     *      android.view.animation.Transformation)
     */
    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        super.getChildStaticTransformation(child, t);

        final int childCenter = getCenterOfView(child);
        final int childWidth = child.getWidth();

        int rotationAngle = 0;
        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);

        View selView = this.getSelectedView();
        // If the child is in the center, we do not rotate it.
        if (child == selView) {
            transformImageBitmap(child, t, 0);
        } else {
            // Calculate the rotation angle.
            rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);

            // Make the angle is not bigger than maximum.
            if (Math.abs(rotationAngle) > mMaxRotationAngle) {
                rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
            }

            transformImageBitmap(child, t, rotationAngle);
        }

        return true;
    }

    /**
     * The center of the cover flow.
     * 
     * @return
     */
    private int getCenterOfCoverflow() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    /**
     * Calculate the transformation for child views
     * 
     * @param child
     * @param t
     * @param rotationAngle
     */
    protected void transformImageBitmap(View child, Transformation t, int rotationAngle) {
        final Matrix imageMatrix = t.getMatrix();
        final int imageHeight = child.getHeight();
        final int imageWidth = child.getWidth();
        final int rotation = Math.abs(rotationAngle);

        mCamera.save();
        // Zoom on Z axis.
        mCamera.translate(0, 0, mMaxZoom);

        if (rotation < mMaxRotationAngle) {
            float zoomAmount = (float) (mMaxZoom + rotation * 1.5f);
            mCamera.translate(0, 0, zoomAmount);
        }

        // Rotate the camera on Y axis.
        mCamera.rotateY(rotationAngle);
        // Get the matrix from the camera, in fact, the matrix is S (scale) transformation.
        mCamera.getMatrix(imageMatrix);

        // The matrix final is T2 * S * T1, first translate the center point to (0, 0),
        // then scale, and then translate the center point to its original point.
        // T * S * T

        // S * T1
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
        // (T2 * S) * T1
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));

        mCamera.restore();
    }
}
