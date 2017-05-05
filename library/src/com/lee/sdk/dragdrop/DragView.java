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

package com.lee.sdk.dragdrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * A DragView is a special view used by a DragController. During a drag operation, what is actually
 * moving on the screen is a DragView. A DragView is constructed using a bitmap of the view the user
 * really wants to move.
 * 
 * @author Li Hong
 * @date 2013/03/05
 */
public class DragView extends View {
    /**
     * Number of pixels to add to the dragged item for scaling,In Launcher, value is 40.
     */
    private final static int DRAG_SCALE = 10;

    /**
     * The alpha of the drag view.
     */
    private final static float ALPHA_DRAGVIEW = 0.6f;

    /**
     * The animation scale.
     */
    private float mAnimationScale = 1.0f;

    /**
     * The bitmap of the view.
     */
    private Bitmap mBitmap = null;

    /**
     * The relative x in the parent coordinate.
     */
    private int mRegistrationX = 0;

    /**
     * The relative y in the parent coordinate.
     */
    private int mRegistrationY = 0;

    /**
     * The scale of the view.
     */
    private float mScale = 0;

    /**
     * The window manager used to move.
     */
    private WindowManager mWindowManager = null;

    /**
     * The layout params of the window manager.
     */
    private WindowManager.LayoutParams mLayoutParams = null;

    /**
     * Construct the drag view.
     * 
     * @param context A context.
     * @param bitmap The view that we're dragging around. We scale it up when we draw it.
     * @param registrationX The relative touch x in the view.
     * @param registrationY The relative touch y in the view.
     * @param left The left edge of the view.
     * @param top The top edge of the view.
     * @param width The width of the view.
     * @param height The height of the view.
     */
    public DragView(Context context, Bitmap bitmap, int registrationX, int registrationY, int left, int top, int width,
            int height) {
        super(context);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Matrix scale = new Matrix();
        float scaleFactor = width;
        mScale = (scaleFactor + DRAG_SCALE) / scaleFactor;
        scale.setScale(mScale, mScale);

        try {
            mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, scale, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX + (DRAG_SCALE / 2);
        mRegistrationY = registrationY + (DRAG_SCALE / 2);
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * 
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (null != mBitmap) {
            setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Do the drawing on the canvas.
     * 
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (null == mBitmap) {
            return;
        }

        canvas.save();

        float scale = mAnimationScale;
        if (scale < 0.999f) {
            // allow for some float error.
            float width = mBitmap.getWidth();
            float offset = (width - (width * scale)) / 2;
            canvas.translate(offset, offset);
            canvas.scale(scale, scale);
        }

        // Draw the bitmap.
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);

        canvas.restore();
    }

    /**
     * This is called when the view is detached from a window. At this point it no longer has a
     * surface for drawing.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mBitmap = null;
    }

    /**
     * Set the scale of the view.
     * 
     * @param scale The scale of the view.
     */
    public void setScale(float scale) {
        if (scale > 1.0f) {
            mAnimationScale = 1.0f;
        } else {
            mAnimationScale = scale;
        }

        invalidate();
    }

    /**
     * Create a window containing this view and show it.
     * 
     * @param windowToken obtained from v.getWindowToken() from one of your views
     * @param touchX the x coordinate the user touched in screen coordinates
     * @param touchY the y coordinate the user touched in screen coordinates
     */
    public void show(IBinder windowToken, int touchX, int touchY) {
        WindowManager.LayoutParams lp = null;
        int pixelFormat = PixelFormat.TRANSLUCENT;
        int flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                touchX - mRegistrationX, touchY - mRegistrationY,
                WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL, flags, pixelFormat);
        // lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.token = windowToken;
        lp.setTitle("DragView");
        lp.alpha = ALPHA_DRAGVIEW;
        mLayoutParams = lp;
        mWindowManager.addView(this, lp);
    }

    /**
     * Move the window containing this view.
     * 
     * @param touchX the x coordinate the user touched in screen coordinates
     * 
     * @param touchY the y coordinate the user touched in screen coordinates
     */
    public void move(int touchX, int touchY) {
        if (null != mLayoutParams) {
            WindowManager.LayoutParams lp = mLayoutParams;
            lp.x = touchX - mRegistrationX;
            lp.y = touchY - mRegistrationY;
            mWindowManager.updateViewLayout(this, lp);
        }
    }

    /**
     * Remove the view from the window.
     */
    public void removeFromWindow() {
        if (null != mWindowManager) {
            mWindowManager.removeView(this);
        }
    }
}
