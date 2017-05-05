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
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * This class extends the FrameLayout, it implements the drag and drop. If you want to use the
 * drag-drop function in you application, you must let this layout as the root of you layout which
 * you want to be dragged and dropped.
 * 
 * @author Li Hong
 * 
 * @date 2013/03/05
 */
public class DragLayout extends FrameLayout implements IDragLayout {
    /**
     * The global drag controller to control the drag and drop.
     */
    private DragController mDragController = null;

    /**
     * The GestureDetector that process the user's gesture.
     */
    private GestureDetector mDestureDetector = null;

    /**
     * @param context
     */
    public DragLayout(Context context) {
        super(context);

        initialize(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize(context);
    }

    /**
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(context);
    }

    /**
     * Initialize.
     * 
     * @param context
     */
    private void initialize(Context context) {
        // Create the drag controller with this layout as root view.
        mDragController = new DragController(context, this);

        // The gesture detector.
        mDestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                mDragController.onLongPress(e);
            }
        });
    }

    /**
     * Set the window taken.
     * 
     * @param token The token of the window that is making the request .
     */
    public void setWindowToken(IBinder token) {
        mDragController.setWindowToken(token);
    }

    /**
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDragController.onTouchEvent(event) || super.onTouchEvent(event);
    }

    /**
     * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (null != mDestureDetector) {
            mDestureDetector.onTouchEvent(ev);
            onTouchEvent(ev);
        }

        return mDragController.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    /**
     * @see com.lee.sdk.dragdrop.IDragLayout#registerDragListener(com.lee.sdk.dragdrop.IDragListener)
     */
    @Override
    public void registerDragListener(IDragListener listener) {
        mDragController.registerDragListener(listener);
    }

    /**
     * @see com.lee.sdk.dragdrop.IDragLayout#unregisterDragListener(com.lee.sdk.dragdrop.IDragListener)
     */
    @Override
    public void unregisterDragListener(IDragListener listener) {
        mDragController.unregisterDragListener(listener);
    }

    /**
     * @see com.lee.sdk.dragdrop.IDragLayout#cancelDrag()
     */
    @Override
    public void cancelDrag() {
        mDragController.forceCanceDrag();
    }

    /**
     * @see com.lee.sdk.dragdrop.IDragLayout#startDrag(com.lee.sdk.dragdrop.IDragSource)
     */
    @Override
    public boolean startDrag(IDragSource dragSource) {
        if (null != mDragController) {
            return mDragController.startDrag(dragSource);
        }

        return false;
    }
}
