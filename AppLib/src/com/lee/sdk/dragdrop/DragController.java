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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * This class is used to initiate a drag within a view. When a drag starts it creates a special view
 * (a DragView) that moves around the screen until the user ends the drag.
 * 
 * @author Li Hong
 * @date 2013/03/04
 */
/* public */ class DragController {
    /**
     * Indicates the drag is a move.
     */
    public static final int DRAG_ACTION_MOVE = 1;

    /**
     * Indicates the drag is a copy.
     */
    public static final int DRAG_ACTION_COPY = 2;

    /**
     * The drag border value,if the drag distance exceeds the value,then means start drag.
     */
    private static final int DRAG_TOLERANCE_VALUE = 15;

    /**
     * The drag action. One of {@link DragController#DRAG_ACTION_MOVE} or
     * {@link DragController#DRAG_ACTION_COPY}
     */
    private int mDragAction = 0;

    /**
     * The context.
     */
    private Context mContext = null;

    /**
     * Used to getHitRect,to avoid gc thrash.
     */
    private Rect mRectTemp = new Rect();

    /**
     * Info about the screen.
     */
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /**
     * Used to save the left and top of the drag view.
     */
    private final int[] mCoordinatesTemp = new int[2];

    /**
     * Whether or not start drag.
     */
    private boolean mPrepareDrag = false;

    /**
     * Whether or not we're dragging.
     */
    private boolean mDragging = false;

    /**
     * Whether or not we start drag.
     */
    private boolean mStartDrag = false;

    /**
     * Whether can start drag.
     */
    private boolean mCanStartDrag = false;

    /**
     * Indicate touch up event has occurred.
     */
    private boolean mHasTouchUp = false;

    /**
     * Raw X coordinate of the down event.
     */
    private float mMotionDownX = 0.0f;

    /**
     * Raw Y coordinate of the down event.
     */
    private float mMotionDownY = 0.0f;

    /**
     * Where the drag originated.
     */
    private IDragSource mDragSource = null;

    /**
     * The view that moves around while you drag.
     */
    private DragView mDragView = null;

    /**
     * The window token used as the parent for the DragView.
     */
    private IBinder mWindowToken = null;

    /**
     * The input method manager.
     */
    private InputMethodManager mInputMethodManager = null;

    /**
     * The main window view of the main activity.
     */
    private View mRootView = null;

    /**
     * The last drop target.
     */
    private IDropTarget mLastDropTarget = null;

    /**
     * The drag event params.
     */
    private DragParams mDragParams = null;

    /**
     * The dragListener list contain registered listener.
     */
    private ArrayList<IDragListener> mRegisterListenerList = new ArrayList<IDragListener>();

    /**
     * The IDragListener.
     */
    private ArrayList<IDragListener> mNeedNotifiedListenerList = null;

    /**
     * The constructor of the dragController.
     * 
     * @param context The application's context.
     * @param view The root of the view hierarchy.
     */
    public DragController(Context context, View view) {
        mContext = context;
        mRootView = view;
        
        if (null == view) {
            throw new IllegalArgumentException("The view can NOT be null.");
        }

        // Get the screen size and saved to a member.
        recordScreenSize();
    }

    /**
     * Starts a drag. It creates a bitmap of the view being dragged. That bitmap is what you see
     * moving. The actual view can be repositioned if that is what the onDrop handle chooses to do.
     * 
     * @param v The view that is being dragged.
     * @param source An object representing where the drag originated.
     * @param dragInfo The data associated with the object that is being dragged.
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *            {@link #DRAG_ACTION_COPY}.
     * 
     * @return true if succeed to start dragging, otherwise false.
     */
    private boolean startDrag(View v, IDragSource source, IDataObject dragInfo, int dragAction) {
        Point point = new Point(0, 0);
        Bitmap b = source.getDragBitmap(point, mDragParams);

        /** Get the left and top of the view and assign to mCoordinatesTemp. */
        int[] loc = mCoordinatesTemp;
        v.getLocationOnScreen(loc);

        /** Get the left and top of the view that bitmap come from. */
        int screenX = loc[0] + point.x;
        int screenY = loc[1] + point.y;

        point = null;
        if (b == null) {
            screenX = loc[0];
            screenY = loc[1];
            b = getViewBitmap(v);
        }

        if (b == null) {
            // out of memory?
            return false;
        }

        startDrag(b, screenX, screenY, 0, 0, b.getWidth(), b.getHeight(), source, dragInfo, dragAction);

        /** Remove it */
        // b.recycle();

        return true;
    }

    /**
     * Starts a drag.
     * 
     * @param b The bitmap to display as the drag image. It will be re-scaled to the enlarged size.
     * @param screenX The x position on screen of the left of the bitmap.
     * @param screenY The y position on screen of the top of the bitmap.
     * @param textureLeft The left edge of the region inside b to use.
     * @param textureTop The top edge of the region inside b to use.
     * @param textureWidth The width of the region inside b to use.
     * @param textureHeight The height of the region inside b to use.
     * @param source An object representing where the drag originated.
     * @param dragInfo The data associated with the object that is being dragged.
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *            {@link #DRAG_ACTION_COPY}.
     */
    private void startDrag(Bitmap b, int screenX, int screenY, int textureLeft, int textureTop, int textureWidth,
            int textureHeight, IDragSource source, IDataObject dragInfo, int dragAction) {
        /**
         * Hide soft keyboard, if visible
         */
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        mDragAction = dragAction;

        /** The relative touch x and y in the view coordinate. */
        int registrationX = ((int) mMotionDownX) - screenX;
        int registrationY = ((int) mMotionDownY) - screenY;

        /** Get the drag listener */
        mNeedNotifiedListenerList = getDragListener(mDragSource, null, dragInfo);

        final DragView dragView = new DragView(mContext, b, registrationX, registrationY, textureLeft, textureTop,
                textureWidth, textureHeight);
        mDragView = dragView;
        mDragParams.setDragView(mDragView);
        dragView.show(mWindowToken, (int) mMotionDownX, (int) mMotionDownY);
    }

    /**
     * Draw the view into a bitmap.
     * 
     * @param v Get the bitmap of the view.
     * @return The bitmap constructed from the view.
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);
        // First destroy the cached drawing cache, sometime, the
        // returned drawing cache may be old one.
        v.destroyDrawingCache();
        v.setDrawingCacheEnabled(true);
        v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        Bitmap cacheBitmap = v.getDrawingCache();

        if (null == cacheBitmap) {
            return null;
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
            return bitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Call this from a drag source view like this.
     * 
     * @param event The motion event.
     * @return True if the drag is going,otherwise false.
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mPrepareDrag;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        endDrag();
    }

    /**
     * Call this method to cancel drag event, this method will try to send ACTION_CANCEL event to
     * system.
     */
    public void forceCanceDrag() {

        // If have not prepared drag, we do nothing.
        if (!mPrepareDrag) {
            return;
        }

        try {
            MotionEvent cancelEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            this.onTouchEvent(cancelEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call this from where need to process the motion event.
     * 
     * @param ev The motion event.
     * @return Return true if you have consumed the event,false if you haven't. The default
     *         implementation always returns false.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        // If touch down, we should recording screen size, because if user change the
        // screen orientation, the display metrics should be changed, so we recording screen
        // size here.
        if (MotionEvent.ACTION_DOWN == action) {
            recordScreenSize();
        }

        final int screenX = clamp((int) ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int) ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        if (!mPrepareDrag) {
            // NOTE: 
            // 这种情况通常发生在使用触摸笔的PAD上面，UP/CANCEL在启动拖拽之前就已经发生，导致我们再也收不到UP/CANCEL
            // 就没有机会重置状态，这导致阻塞住了用户的所有输入事件.
            // 
            // Please think about this case, if you call startDrag() method to start dragging,
            // however, before the startDrag() calling, the ACTION_UP or ACTION_CANCEL event occur, in this
            // situation, we block all touch event and do not response user touch input, so this case is very fatal for
            // application, therefore, we must remember the touch up or cancel.
            switch (action) {
            case MotionEvent.ACTION_DOWN:
                // If the action down, we remember the motion point.
                mMotionDownX = screenX;
                mMotionDownY = screenY;
                mHasTouchUp = false;
                break;

            case MotionEvent.ACTION_UP: // Fall through
            case MotionEvent.ACTION_CANCEL:
                mHasTouchUp = true;
                break;
            }

            return false;
        }

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            if (!mCanStartDrag) {
                return false;
            }
            onMove(ev, screenX, screenY);
            break;

        case MotionEvent.ACTION_UP:
            if (mPrepareDrag) {
                drop(screenX, screenY);
            }
            endDrag();
            break;

        case MotionEvent.ACTION_CANCEL:
            cancelDrag();
            break;
        }

        return true;
    }

    /**
     * Set the window taken.
     * 
     * @param token The token of the window that is making the request .
     */
    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listener which will be notified when a drag starts or ends.
     */
    public void registerDragListener(IDragListener listener) {
        if (null != listener) {
            if (!mRegisterListenerList.contains(listener)) {
                mRegisterListenerList.add(listener);
            }
        }
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void unregisterDragListener(IDragListener listener) {
        mRegisterListenerList.remove(listener);
    }

    /**
     * Called when the Long press occurring.
     * 
     * @param e The motion event.
     */
    public void onLongPress(MotionEvent e) {
        recordScreenSize();
        IDragSource dragSource = findDragSource((int) e.getRawX(), (int) e.getRawY(), (ViewGroup) mRootView);
        final int screenX = clamp((int) e.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int) e.getRawY(), 0, mDisplayMetrics.heightPixels);
        mMotionDownX = screenX;
        mMotionDownY = screenY;

        // Modified by ZhouYuanqi on 2012/02/24 begin =========.
        // If the the start of dragging not be control by the drag source
        // we will start dragging when found the drag source and long press.
        if (null != dragSource && !dragSource.startDragBySelf()) {
            startDrag(dragSource);
        }
        // Modified by ZhouYuanqi on 2012/02/24 end =========.
    }

    /**
     * Called when the drag source control the start of dragging.
     * 
     * @param dragSource The drag source.
     */
    public boolean startDrag(IDragSource dragSource) {
        boolean succeed = false;

        // If the touch up event occurs, we return directly.
        // If the drag is doing, we return directly.
        if (mHasTouchUp || mPrepareDrag) {
            return succeed;
        }

        if (null != dragSource) {
            // If the drag parameter is not NULL, we must dismiss the drag view in it
            // if possible.
            if (null != mDragParams) {
                mDragParams.dismissDragView();
            }

            mDragParams = new DragParams(dragSource, null, null, (int) mMotionDownX, (int) mMotionDownY, 0, 0, null);

            if (dragSource.canBeDragged(mDragParams)) {
                View v = (View) dragSource;
                IDataObject info = mDragParams.getDataObject();
                mPrepareDrag = true;
                mDragSource = dragSource;
                succeed = startDrag(v, mDragSource, info, DragController.DRAG_ACTION_MOVE);
                mCanStartDrag = true;
            } else {
                mDragParams = null;
            }
        }

        // If not succeed to drag drop, reset the member to initial value.
        if (!succeed) {
            reset();
        }

        return succeed;
    }

    /**
     * Get the drag listener by the x and y;
     * 
     * @param x The raw x coordinate.
     * @param y The raw y coordinate.
     * @return The IDragListener with the specified view setted.
     */
    private ArrayList<IDragListener> getDragListener(IDragSource source, IDropTarget target, IDataObject info) {
        return mRegisterListenerList;
        /*
         * ArrayList<IDragListener> listenerList = new ArrayList<IDragListener>(); if (null !=
         * m_registerListenerList) { for (IDragListener listener : m_registerListenerList) { if
         * (!listener.disableNotify(source, target, info)) { listenerList.add(listener); } } }
         * return listenerList;
         */
    }

    /**
     * Find the drag source view;
     * 
     * @param x The specified x coordinate.
     * @param y The specified y coordinate.
     * @param group The specified view to find the dragSource.
     * @return return source found,otherwise return null.
     */
    private IDragSource findDragSource(int x, int y, View group) {
        if (null == group) {
            return null;
        }

        View target = findEventTarget(x, y, group);

        if (null != target) {
            if (target instanceof IDragSource) {
                return (IDragSource) target;
            } else {
                ViewParent parent = target.getParent();
                while (null != parent) {
                    if (parent instanceof IDragSource) {
                        return (IDragSource) parent;
                    }

                    parent = parent.getParent();
                }
            }
        }

        return null;
    }

    /**
     * Find the view where the event occurred;
     * 
     * @param x The specified x coordinate.
     * @param y The specified y coordinate.
     * @param v The specified view to find the target.
     * @return return event target found,otherwise return null.
     */
    public View findEventTarget(int x, int y, View v) {
        if (null == v) {
            return null;
        }

        int visibility = v.getVisibility();
        if (View.VISIBLE != visibility) {
            return null;
        }

        getLocationOnScreen(v, mRectTemp);

        if (mRectTemp.contains(x, y)) {
            if (v instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) v;
                int count = group.getChildCount();
                for (int i = count - 1; i >= 0; i--) {
                    View child = group.getChildAt(i);
                    View target = findEventTarget(x, y, child);
                    if (null != target) {
                        return target;
                    }
                }

                getLocationOnScreen(group, mRectTemp);
                if (mRectTemp.contains(x, y)) {
                    return group;
                }
            } else {
                return v;
            }
        }

        return null;
    }

    /**
     * Find the drop target during the moving.
     * 
     * @param x The specified x coordinate.
     * @param y The specified y coordinate.
     * @param group The specified view to find the drop target.
     * @return return target found,otherwise return null.
     */
    private IDropTarget findDropTarget(int x, int y, View group) {
        if (null == group) {
            return null;
        }
        View target = findEventTarget(x, y, group);

        if (null != target) {
            if (target instanceof IDropTarget) {
                return (IDropTarget) target;
            } else {
                ViewParent parent = target.getParent();
                while (null != parent) {
                    if (parent instanceof IDropTarget) {
                        return (IDropTarget) parent;
                    }

                    parent = parent.getParent();
                }
            }
        }
        return null;
    }

    /**
     * Process the move action.
     * 
     * @param ev
     * @param screenX
     * @param screenY
     */
    private void onMove(MotionEvent ev, int screenX, int screenY) {
        if (!mCanStartDrag) {
            return;
        }

        // We should check the move distance, because this method is called very frequently.
        if (!isRealDrag(screenX, screenY)) {
            return;
        }

        IDropTarget dropTarget = findDropTarget(screenX, screenY, mRootView);
        int[] pointInParent = getLocationOnWindow((View) dropTarget, screenX, screenY);
        if (null == mDragParams) {
            mDragParams = new DragParams(mDragSource, dropTarget, null, screenX, screenY, pointInParent[0],
                    pointInParent[1], mDragView);
        } else {
            mDragParams.setRawX(screenX);
            mDragParams.setRawY(screenY);
            mDragParams.setX(pointInParent[0]);
            mDragParams.setY(pointInParent[1]);
            mDragParams.setDropTarget(dropTarget);
        }

        IDataObject info = mDragParams.getDataObject();

        /** Get the drag listener */
        mNeedNotifiedListenerList = getDragListener(mDragSource, dropTarget, info);

        if (!mStartDrag) {
            if (null != mNeedNotifiedListenerList) {
                for (IDragListener item : mNeedNotifiedListenerList) {
                    item.onDragStart(mDragSource, info, mDragAction);
                }
            }
            mStartDrag = true;
        }

        /** update the drag view */
        mDragView.move((int) ev.getRawX(), (int) ev.getRawY());

        mDragging = true;

        if (dropTarget != null) {
            if (mLastDropTarget == dropTarget) {
                dropTarget.onDragOver(mDragParams);
            } else {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragLeave(mDragParams);
                }
                dropTarget.onDragEnter(mDragParams);
            }
            dropTarget.onDragging(mDragParams);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragLeave(mDragParams);
            }
        }

        mLastDropTarget = dropTarget;

        if (null != mNeedNotifiedListenerList) {
            for (IDragListener item : mNeedNotifiedListenerList) {
                item.onDragging(mDragParams);
            }
        }
    }

    /**
     * Drop the view.
     * 
     * @param x The raw x coordinate.
     * @param y The raw y coordinate.
     * @return return true if have a drop target,this does not mean drop successfully.
     */
    private boolean drop(int x, int y) {
        boolean handled = false;
        IDropTarget dropTarget = null;

        if (mDragging) {
            dropTarget = findDropTarget((int) x, (int) y, mRootView);

            if (dropTarget != null) {
                int[] pointInParent = getLocationOnWindow((View) dropTarget, x, y);
                if (null == mDragParams) {
                    mDragParams = new DragParams(mDragSource, dropTarget, null, x, y, pointInParent[0],
                            pointInParent[1], mDragView);
                } else {
                    mDragParams.setRawX(x);
                    mDragParams.setRawY(y);
                    mDragParams.setX(pointInParent[0]);
                    mDragParams.setY(pointInParent[1]);
                    mDragParams.setDropTarget(dropTarget);
                }

                if (dropTarget.acceptDrop(mDragParams)) {
                    dropTarget.onDrop(mDragParams);
                    handled = true;
                } else {
                    handled = false;
                }
            }
        } else {
            handled = false;
        }

        if (null != mDragSource) {
            mDragSource.onDropCompleted(dropTarget, mDragParams, handled);
        }

        return handled;
    }

    /**
     * End the drag.
     */
    private void endDrag() {
        // Always notify the listener dragging end.
        // if (m_dragging)
        {
            if (null != mNeedNotifiedListenerList) {
                for (IDragListener item : mNeedNotifiedListenerList) {
                    item.onDragEnd();
                }
            }
        }

        if (mPrepareDrag) {
            if (mDragView != null && mDragParams.isAutoDismissDragView()) {
                mDragParams.dismissDragView();
            }
        }

        // Reset the member to initial values.
        reset();
    }

    /**
     * Get the screen size.
     */
    private void recordScreenSize() {
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                mDisplayMetrics);
    }

    /**
     * Whether the user real want drag or just long press.
     * 
     * @param x The raw x of drag position.
     * @param y The raw y of drag position.
     * @return true if the drag offset is more than {@link DragController#DRAG_TOLERANCE_VALUE}
     */
    private boolean isRealDrag(int x, int y) {
        if (Math.abs(x - (int) mMotionDownX) > DRAG_TOLERANCE_VALUE
                || Math.abs(y - (int) mMotionDownY) > DRAG_TOLERANCE_VALUE) {
            return true;
        }

        return false;
    }

    /**
     * Get the location of view on the screen coordinate.
     * 
     * @param v The view want to get the absolute rectangle.
     * @param rc The rect reference will update after the function called.
     */
    private void getLocationOnScreen(View v, Rect rc) {
        // Here, we do NOT use the View#getHitRect() method, because in Android 3.0 or later, the
        // View#getHitRect() method considers the view transformation.
        // In Android 3.0 or later, the View#getHitRect() method will apply the transformation
        // in view, the View provides setX(), setY(), setRotationX() etc methods to change the
        // transformation of the view.
        // So, the View will maintain a matrix in it, here, please think about this case:
        // If a view has a transform 100 on Y direction, but the view real bound is [0, 0, 200, 200]
        // for instance, however, the getHitRect() method may return the bound is [0, 100, 200, 300].
        // For fixing this case, we will apply the matrix in the view to the point (x, y).
        // v.getHitRect(rc);
        rc.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        int[] lt = new int[] { 0, 0 };
        v.getLocationOnScreen(lt);
        rc.offset(lt[0] - v.getLeft(), lt[1] - v.getTop());
    }

    /**
     * Clamp val to be min and max.
     */
    private static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val >= max) {
            return max - 1;
        } else {
            return val;
        }
    }

    /**
     * Computes the coordinates of this view in its window.
     * 
     * @param v The specified view object.
     * @return The location point.
     */
    private int[] getLocationOnWindow(View v, int x, int y) {
        if (null == v) {
            return new int[] { 0, 0 };
        }

        int[] loc = new int[] { 0, 0 };
        v.getLocationOnScreen(loc);

        return new int[] { x - loc[0], y - loc[1] };
    }

    /**
     * Reset the members to initial value.
     */
    private void reset() {
        mPrepareDrag = false;
        mDragParams = null;
        mDragView = null;
        mDragSource = null;
        mLastDropTarget = null;
        mStartDrag = false;
        mCanStartDrag = false;
        mDragging = false;
        mHasTouchUp = false;
        mCoordinatesTemp[0] = 0;
        mCoordinatesTemp[1] = 0;
        mMotionDownX = 0;
        mMotionDownY = 0;

        mRectTemp.setEmpty();
    }
}
