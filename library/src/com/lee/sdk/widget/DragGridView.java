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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.lee.sdk.dragdrop.DragListener;
import com.lee.sdk.dragdrop.DragParams;
import com.lee.sdk.dragdrop.IDataObject;
import com.lee.sdk.dragdrop.IDragLayout;
import com.lee.sdk.dragdrop.IDragSource;
import com.lee.sdk.dragdrop.IDropTarget;

/**
 * This class implements the function that user can swap position of items by dragging.
 * 
 * @author Li Hong
 * 
 * @date 2013/03/06
 */
public class DragGridView extends GridView implements IDropTarget {
    /**
     * This interface defines method for the adapter of grid.
     */
    public interface IDragAdapter {
        /**
         * Swap the item in the adapter data.
         * 
         * @param dragPosition The drag source position.
         * @param curPosition The current position in which drag view locates.
         * 
         *            NOTE: The subclass typically implements this method like this:
         * 
         *            <li>[1] Object item = list.remove({@link dragPosition});
         *            <p>
         *            <li>[2] list.add({@link curPosition}, item);
         *            <li>[3] notifyDataChanged();
         */
        public void swap(int dragPosition, int curPosition);

        /**
         * Set the hide item position.
         * 
         * @param position The position of item which view should be invisible.
         * 
         *            NOTE: You need to remember the hide position passed by this method, in the
         *            {@link ListAdapter#getView(int, View, android.view.ViewGroup)} method, you
         *            typically implement like this:
         * 
         *            <pre class="prettyprint">
         * public void setHidePositioin(int position) {
         *     mHidePosition = position;
         * }
         * 
         * public View getView(int position, View convertView, ViewGroup parent) {
         *     // ...
         *     convertView.setVisible((position == mHidePosition) ? View.INVISIBLE : VIEW.VISIBLE);
         * 
         *     return convertView;
         * }
         * </pre>
         */
        public void setHidePositioin(int position);

        /**
         * Update the dataset, typically you call {@link BaseAdapter#notifyDataChanged()} method.
         */
        public void update();
    }

    /**
     * This interface defines some extra methods, such as insert or remove item. A listview or
     * gridview can receive data from other source in other view group.
     */
    public interface IDragAdapter2 extends IDragAdapter {
        /**
         * Insert the data into specified position.
         * 
         * @param position The insert position.
         * @param data The data will be inserted.
         * 
         *            NOTE: In this method, you should always check the data type whether matches
         *            what you expect.
         */
        public void insert(int position, IDataObject data);

        /**
         * Remove data at specified position.
         * 
         * @param position
         */
        public void remove(int position);
    }

    /**
     * The current drag position. This member is like {@link mSrcDragPosition}, it will change to
     * this value which current dragging over position when it swaps items.
     */
    private int mDragPosition = INVALID_POSITION;

    /**
     * The original drag position. Please think about this case: Drag the position 0 item and
     * quickly move to position 2. In fact, we will do swap animation twice.
     * 
     * <pre class="prettyprint">
     * [1] {@link mDragPosition} = 0, {@link mCurPosition} = 1;
     * [2] {@link mDragPosition} = 1, {@link mCurPosition} = 2;
     * 
     * However, in this case, the {@link mSrcDragPosition} is still 0 (original drag position).
     * In this situation, we will swap {@link mSrcDragPosition} to {@link mCurPosition}.
     * After finish swapping, these three value are:
     * {@link mDragPosition} = 2;
     * {@link mCurPosition} = 2;
     * {@link mSrcDragPosition} = 2;
     * </pre>
     */
    private int mSrcDragPosition = INVALID_POSITION;

    /**
     * The current dragging position, in which the dragging view locates.
     */
    private int mCurPosition = INVALID_POSITION;

    /**
     * The animation duration time.
     */
    private int mAnimDuration = 400;

    /**
     * The scroll distance.
     */
    private int mScrollDistance = 250;

    /**
     * Indicate has drag end.
     */
    private boolean mHasDragEnd = false;

    /**
     * Indicate has insert a dummy data for drag enter effect.
     */
    private boolean mHasInsertDummy = false;

    /**
     * The drag layout, we use it to start the dragging operation if we need start dragging by
     * myself.
     */
    private IDragLayout mDragLayout = null;

    /**
     * The drag adapter.
     */
    private IDragAdapter mDragAdapter = null;

    /**
     * The cached drag parameter.
     */
    private DragParams mDragParams = null;

    /**
     * The rectangle of a child for "From" animation.
     */
    private Rect mHitReceFrom = new Rect();

    /**
     * The rectangle of a child for "To" animation.
     */
    private Rect mHitReceTo = new Rect();

    /**
     * Rectangle used for hit testing children
     */
    private Rect mTouchFrame = new Rect();

    /**
     * The drag listener for scrolling grid view up and down.
     */
    private GridViewDragListener mDragListener = null;

    /**
     * The doing animation children list.
     */
    private ArrayList<View> mAnimChildren = new ArrayList<View>();

    /**
     * The animation for children.
     */
    private AnimationListener mAnimListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // Remove one child.
            if (mAnimChildren.size() > 0) {
                mAnimChildren.remove(mAnimChildren.size() - 1);
            }

            // If the list becomes empty, means all children finish doing
            // animation,
            // we swap the position of data.
            if (mAnimChildren.isEmpty()) {
                swapPosition();
            }
        }
    };

    /**
     * The constructor method.
     * 
     * @param context
     */
    public DragGridView(Context context) {
        super(context);

        initialize(context);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     */
    public DragGridView(Context context, AttributeSet attrs) {
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
    public DragGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(context);
    }

    /**
     * @see android.widget.GridView#setAdapter(android.widget.ListAdapter)
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof IDragAdapter)) {
            throw new IllegalArgumentException("The adapter must implement the IDragAdapter interface.");
        }

        super.setAdapter(adapter);

        mDragAdapter = (IDragAdapter) adapter;
    }

    /**
     * Set the animation duration.
     * 
     * @param duration
     */
    public void setAnimDuration(int duration) {
        mAnimDuration = duration;
    }

    /**
     * Set the scroll distance.
     * 
     * @param distance
     */
    public void setScrollDistance(int distance) {
        mScrollDistance = distance;
    }

    /**
     * Get the scroll distance.
     * 
     * @return
     */
    public int getScrollDistance() {
        return mScrollDistance;
    }

    /**
     * Set the drag layout for dragging.
     * 
     * @param dragLayout
     */
    public void setDragLayout(IDragLayout dragLayout) {
        if (null == dragLayout) {
            throw new IllegalArgumentException("The dragLayout must be not null.");
        }

        this.mDragLayout = dragLayout;
        this.mDragListener = new GridViewDragListener(this);
        this.mDragLayout.registerDragListener(mDragListener);
    }

    /**
     * Get the {@link IDragLayout} set from {@link #setDragLayout()} method.
     * 
     * @return
     */
    public IDragLayout getDragLayout() {
        return mDragLayout;
    }

    /**
     * Get the drag adapter.
     * 
     * @return
     */
    public IDragAdapter getDragAdapter() {
        return mDragAdapter;
    }

    /**
     * Set the current drag position. Typically this method can be called by the drag source.
     * 
     * @param dragPosition The current drag source.
     */
    public void setDragPosition(int dragPosition) {
        mDragPosition = dragPosition;
        mSrcDragPosition = dragPosition;

        setHidePosition(dragPosition);
    }

    /**
     * Get the current drag position.
     * 
     * @return
     */
    public int getDragPosition() {
        return mDragPosition;
    }

    /**
     * Call this method to start dragging.
     * 
     * @param view This view will be dragged.
     * @param position The position of this view in its adapter.
     * 
     * @return true if succeed to drag, otherwise false.
     */
    public boolean startDrag(View view, int position) {
        if (!(view instanceof IDragSource)) {
            throw new ClassCastException("The item view must implement IDragSource interface.");
        }

        if (null != mDragLayout) {
            IDragSource dragSource = (IDragSource) view;
            if (!dragSource.startDragBySelf()) {
                return false;
            }

            mDragLayout.setWindowToken(this.getWindowToken());
            boolean succeed = mDragLayout.startDrag(dragSource);
            if (succeed) {
                mDragPosition = position;
                mCurPosition = position;
                mSrcDragPosition = position;

                view.setVisibility(View.INVISIBLE);

                return true;
            }
        }

        return false;
    }

    /**
     * Initialize context.
     * 
     * @param context
     */
    private void initialize(Context context) {
        this.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return startDrag(view, position);
            }
        });
    }

    /**
     * @see com.lee.sdk.dragdrop.IDropTarget#onDrop(com.lee.sdk.dragdrop.DragParams)
     */
    @Override
    public void onDrop(DragParams params) {
        // setDragPosition(INVALID_POSITION);
        // Here do not set the drag position, because children may do animation
        // when this
        // method is called, we will do real drop operation when all animation
        // finish.
        removeDummyItem();
    }

    /**
     * @see com.lee.sdk.dragdrop.IDropTarget#onDragEnter(com.lee.sdk.dragdrop.DragParams)
     */
    @Override
    public void onDragEnter(DragParams params) {
        mDragParams = params;
    }

    /**
     * @see com.lee.sdk.dragdrop.IDropTarget#onDragging(com.lee.sdk.dragdrop.DragParams)
     */
    @Override
    public void onDragging(DragParams params) {
        if (null != mDragListener) {
            if (mDragListener.isScrolling()) {
                return;
            }
        }

        setHasDragEnd(false);
        int curIndex = pointToPosition2(params.getX(), params.getY());
        if (curIndex != INVALID_POSITION) {
            mCurPosition = curIndex;
        }

        // If the drag position is invalid, means the drag source may from other
        // view group.
        insertDummyItem();

        // Do the swap animation.
        if (mCurPosition != mDragPosition && INVALID_POSITION != mDragPosition) {
            int firstPosition = getFirstVisiblePosition();
            doAnimation(mDragPosition - firstPosition, mCurPosition - firstPosition);
            mDragPosition = mCurPosition;
        }
    }

    /**
     * @see com.lee.sdk.dragdrop.IDropTarget#onDragOver(com.lee.sdk.dragdrop.DragParams)
     */
    @Override
    public void onDragOver(DragParams params) {
    }

    /**
     * @see com.lee.sdk.dragdrop.IDropTarget#onDragLeave(com.lee.sdk.dragdrop.DragParams)
     */
    @Override
    public void onDragLeave(DragParams params) {
        mDragParams = null;

        removeDummyItem();
    }

    /**
     * @see com.lee.sdk.dragdrop.IDropTarget#acceptDrop(com.lee.sdk.dragdrop.DragParams)
     * 
     *      NOTE: Here return true, in fact, should check the data contained in the params whether
     *      matches what we expect. Subclass can override this method.
     */
    @Override
    public boolean acceptDrop(DragParams params) {
        return true;
    }

    /**
     * Maps a point to a position in the list.
     * 
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    private int pointToPosition2(int x, int y) {
        View dragChild = getChildAt(mDragPosition);
        if (null != dragChild) {
            dragChild.getHitRect(mTouchFrame);
            if (y > mTouchFrame.top && y < mTouchFrame.bottom) {
                return pointToPosition(x, y);
            }
        }

        final Rect frame = mTouchFrame;
        final int count = getChildCount();
        int left = 0;
        int top = 0;
        int right = getRight();

        // Find the left bounds.
        for (int i = 0; i < count; ++i) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (top != frame.top) {
                top = frame.top;
                left = 0;
            }

            if (x > left && x < frame.right && y > frame.top && y < frame.bottom) {
                return getFirstVisiblePosition() + i;
            }

            left = frame.right;
        }

        // Find the right bound.
        for (int i = count - 1; i >= 0; --i) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (top != frame.top) {
                top = frame.top;
                right = getRight();
            }

            if (x > frame.left && x < right && y > frame.top && y < frame.bottom) {
                return getFirstVisiblePosition() + i;
            }

            right = frame.left;
        }

        return INVALID_POSITION;
    }

    /**
     * Indicate the children are doing animation or not.
     * 
     * @return
     */
    protected boolean isDoingAnimation() {
        return (!mAnimChildren.isEmpty());
    }

    /**
     * Swap the position of data in adapter. Typically this method is called when all children
     * finish doing animation.
     */
    private void swapPosition() {
        // Clear the animation of children.
        clearAnimations();

        // Dismiss the drag view if possible, in some case, if user drags an
        // item and fling quickly, however, in this time, views may do animation, so
        // here, we dismiss the drag view by ourselves so that the drag view can
        // remain at its current position.
        dismissDragView();

        mDragParams = null;

        if (mSrcDragPosition != mCurPosition && mSrcDragPosition >= 0 && mCurPosition >= 0) {
            // Swap the data item position.
            mDragAdapter.swap(mSrcDragPosition, mCurPosition);
            // Set the current position.
            setDragPosition(mCurPosition);
        }

        // If has drag end, we update the grid.
        if (mHasDragEnd) {
            updateGridView();
        }
    }

    /**
     * Do the translate animation.
     * 
     * @param dragPosition
     * @param curPosition
     */
    private void doAnimation(int dragPosition, int curPosition) {
        if (dragPosition < curPosition) {
            // Do the left translate animation.
            for (int index = curPosition; index > dragPosition; index--) {
                View childFrom = getChildAt(index);
                View childTo = getChildAt(index - 1);
                if (null != childFrom && null != childTo) {
                    doAnimation(childFrom, childTo);
                }
            }
        } else {
            // Do the right translate animation.
            for (int index = curPosition; index < dragPosition; index++) {
                View childFrom = getChildAt(index);
                View childTo = getChildAt(index + 1);
                if (null != childFrom && null != childTo) {
                    doAnimation(childFrom, childTo);
                }
            }
        }
    }

    /**
     * Do the animation.
     * 
     * @param childFrom
     * @param childTo
     */
    private void doAnimation(View childFrom, View childTo) {
        final Rect hitRectFrom = mHitReceFrom;
        final Rect hitRectTo = mHitReceTo;

        if (null != childFrom && null != childTo) {
            Animation anim = childFrom.getAnimation();
            if (null != anim) {
                return;
            }

            childFrom.getHitRect(hitRectFrom);
            childTo.getHitRect(hitRectTo);

            // TODO
            // childFrom.layout(hitRectTo.left, hitRectTo.top, hitRectTo.right, hitRectTo.bottom);
            // childTo.layout(hitRectFrom.left, hitRectFrom.top, hitRectFrom.right,
            // hitRectFrom.bottom);

            int toX = hitRectTo.left - hitRectFrom.left;
            int toY = hitRectTo.top - hitRectFrom.top;
            Animation ta = new TranslateAnimation(0, toX, 0, toY);
            // Animation ta = new TranslateAnimation(hitRectFrom.left, hitRectTo.left,
            // hitRectFrom.top, hitRectTo.top);
            ta.setDuration(mAnimDuration);
            ta.setFillAfter(true);
            ta.setAnimationListener(mAnimListener);
            childFrom.startAnimation(ta);

            mAnimChildren.add(childFrom);
        }
    }

    /**
     * Clear the animation of all children.
     */
    private void clearAnimations() {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; ++index) {
            View child = getChildAt(index);
            if (null != child) {
                child.setAnimation(null);
            }
        }
    }

    /**
     * Indicate the drag end or not.
     * 
     * @param dragEnd
     */
    private void setHasDragEnd(boolean dragEnd) {
        mHasDragEnd = dragEnd;
    }

    /**
     * Set the hide position.
     * 
     * @param position
     */
    private void setHidePosition(int position) {
        if (null != mDragAdapter) {
            mDragAdapter.setHidePositioin(position);
        }
    }

    /**
     * Dismiss the drag view.
     */
    private void dismissDragView() {
        // Remove the drag view.
        if (null != mDragParams && !mDragParams.isAutoDismissDragView()) {
            mDragParams.dismissDragView();
        }
    }

    /**
     * Update the grid view.
     */
    private void updateGridView() {
        // Check whether is doing animation.
        if (!mAnimChildren.isEmpty()) {
            // Current there is one child is doing animation at least.
            // We do not dismiss the drag view, and make it remains at its
            // current location.
            // This drag view will dismiss when animation finishes.
            if (null != mDragParams) {
                mDragParams.setAutoDismissDragView(false);
            }
            return;
        }

        this.mDragParams = null;
        this.setDragPosition(INVALID_POSITION);

        // Request layout so that children can layout correctly, please
        // think about this case, change the screen orientation while you
        // are dragging, here, if we do not make grid view request layout,
        // the children may layout incorrectly.
        IDragAdapter adapter = this.getDragAdapter();
        if (null != adapter) {
            adapter.update();
        }
    }

    /**
     * Insert the dummy item for dragging effect.
     */
    private void insertDummyItem() {
        /*
         * if (INVALID_POSITION == mDragPosition) { if (mCurPosition != mDragPosition) { if
         * (mDragAdapter instanceof IDragAdapter2) { int lastPosition = getCount() - 1;
         * ((IDragAdapter2)mDragAdapter).insert(lastPosition, mDragParams.getDataObject());
         * setDragPosition(lastPosition); mHasInsertDummy = true; } } }
         */
    }

    /**
     * Remove the dummy item.
     */
    private void removeDummyItem() {
        if (mHasInsertDummy) {
            /*
             * if (mDragAdapter instanceof IDragAdapter2) {
             * ((IDragAdapter2)mDragAdapter).remove(mDragPosition);
             * setDragPosition(INVALID_POSITION); }
             */
        }

        mHasInsertDummy = false;
    }

    /**
     * The drag listener for the grid view to scroll up and down when dragging over the bottom or
     * top rectangle.
     */
    private static class GridViewDragListener extends DragListener {
        /**
         * Scroll mode.
         */
        private static final int SCROLL_NONE = 0;

        /**
         * Scroll up
         */
        private static final int SCROLL_UP = 1;

        /**
         * Scroll down.
         */
        private static final int SCROLL_DOWN = 2;

        /**
         * The scroll duration, milliseconds.
         */
        private static final int SCROLL_DURATION = 150;

        /**
         * The scroll duration, milliseconds.
         */
        private static final int SCROLL_REGION = 80;

        /**
         * The scroll message id.
         */
        private static final int SCROLL_MESSAGE_ID = 0x01;

        /**
         * The scroll mode.
         */
        private int mScrollMode = SCROLL_NONE;

        /**
         * The grid view.
         */
        private DragGridView mGridView = null;

        /**
         * This handler to scroll the grid view up or down.
         */
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (null == mGridView) {
                    return;
                }

                if (SCROLL_UP == mScrollMode) {
                    mGridView.smoothScrollBy(mGridView.getScrollDistance(), SCROLL_DURATION);
                } else if (SCROLL_DOWN == mScrollMode) {
                    mGridView.smoothScrollBy(-mGridView.getScrollDistance(), SCROLL_DURATION);
                }
            }
        };

        /**
         * The constructor method.
         * 
         * @param gridView
         */
        public GridViewDragListener(DragGridView gridView) {
            mGridView = gridView;
        }

        /**
         * Indicate the grid view is scrolling or not.
         * 
         * @return
         */
        public boolean isScrolling() {
            return (SCROLL_NONE != mScrollMode);
        }

        /**
         * @see com.lee.sdk.dragdrop.DragListener#onDragEnd()
         */
        @Override
        public void onDragEnd() {
            super.onDragEnd();

            if (null != mGridView) {
                mGridView.removeDummyItem();
                mGridView.updateGridView();
                mGridView.setHasDragEnd(true);
            }
        }

        /**
         * @see com.lee.sdk.dragdrop.DragListener#onDragging(com.lee.sdk.dragdrop.DragParams)
         */
        @SuppressLint("HandlerLeak")
        @Override
        public void onDragging(DragParams params) {
            final GridView gridView = mGridView;
            int screenX = params.getRawX();
            int screenY = params.getRawY();
            int[] location = new int[] { screenX, screenY };

            gridView.getLocationInWindow(location);
            screenX = screenX - location[0];
            screenY = screenY - location[1];

            final int height = gridView.getHeight();
            final int width = gridView.getWidth();

            mHandler.removeMessages(SCROLL_MESSAGE_ID);

            // At the top region, scroll down.
            if (screenY >= 0 && screenY < SCROLL_REGION && screenX >= 0 && screenX <= width) {
                mScrollMode = SCROLL_DOWN;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(SCROLL_MESSAGE_ID), SCROLL_DURATION);
                return;
            }

            // At the bottom region, scroll up.
            if (screenY >= height - SCROLL_REGION && screenY < height && screenX >= 0 && screenX <= width) {
                mScrollMode = SCROLL_UP;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(SCROLL_MESSAGE_ID), SCROLL_DURATION);
                return;
            }

            mScrollMode = SCROLL_NONE;
        }
    }
}
