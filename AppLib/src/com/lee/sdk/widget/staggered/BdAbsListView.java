/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.lee.sdk.widget.staggered;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.ListAdapter;

import com.lee.sdk.R;
//CHECKSTYLE:OFF
/**
 * Base class that can be used to implement virtualized lists of items. A list does
 * not have a spatial definition here. For instance, subclases of this class can
 * display the content of the list in a grid, in a carousel, as stack, etc.
 * 
 * @attr ref android.R.styleable#AbsListView_listSelector
 * @attr ref android.R.styleable#AbsListView_drawSelectorOnTop
 * @attr ref android.R.styleable#AbsListView_stackFromBottom
 * @attr ref android.R.styleable#AbsListView_scrollingCache
 * @attr ref android.R.styleable#AbsListView_textFilterEnabled
 * @attr ref android.R.styleable#AbsListView_transcriptMode
 * @attr ref android.R.styleable#AbsListView_cacheColorHint
 * @attr ref android.R.styleable#AbsListView_fastScrollEnabled
 * @attr ref android.R.styleable#AbsListView_smoothScrollbar
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public abstract class BdAbsListView extends BdAdapterView<ListAdapter> implements ViewTreeObserver.OnGlobalLayoutListener,
        ViewTreeObserver.OnTouchModeChangeListener {

    protected static final boolean DEBUG = true;
    private static final String TAG = "BdAbsListView";
    
    public static final int ACTION_POINTER_INDEX_MASK  = 0xff00;
    public static final int ACTION_POINTER_INDEX_SHIFT = 8;
    
    // FIXME not supported features... (removed from original AbsListView)...
    // Filter
    // Fast Scroll
    // Clipping Padding Region

    /**
     * Disables the transcript mode.
     *
     * @see #setTranscriptMode(int)
     */
    public static final int TRANSCRIPT_MODE_DISABLED = 0;
    /**
     * The list will automatically scroll to the bottom when a data set change
     * notification is received and only if the last item is already visible
     * on screen.
     *
     * @see #setTranscriptMode(int)
     */
    public static final int TRANSCRIPT_MODE_NORMAL = 1;
    /**
     * The list will automatically scroll to the bottom, no matter what items
     * are currently visible.
     *
     * @see #setTranscriptMode(int)
     */
    public static final int TRANSCRIPT_MODE_ALWAYS_SCROLL = 2;

    /**
     * Indicates that we are not in the middle of a touch gesture
     */
    static final int TOUCH_MODE_REST = -1;

    /**
     * Indicates we just received the touch event and we are waiting to see if the it is a tap or a
     * scroll gesture.
     */
    static final int TOUCH_MODE_DOWN = 0;

    /**
     * Indicates the touch has been recognized as a tap and we are now waiting to see if the touch
     * is a longpress
     */
    static final int TOUCH_MODE_TAP = 1;

    /**
     * Indicates we have waited for everything we can wait for, but the user's finger is still down
     */
    static final int TOUCH_MODE_DONE_WAITING = 2;

    /**
     * Indicates the touch gesture is a scroll
     */
    static final int TOUCH_MODE_SCROLL = 3;
    
    /**
     * Indicates the view is in the process of being flung
     */
    static final int TOUCH_MODE_FLING = 4;

    /**
     * Indicates the touch gesture is an overscroll - a scroll beyond the beginning or end.
     */
    static final int TOUCH_MODE_OVERSCROLL = 5;

    /**
     * Indicates the view is being flung outside of normal content bounds
     * and will spring back.
     */
    static final int TOUCH_MODE_OVERFLING = 6;

    /**
     * Regular layout - usually an unsolicited layout from the view system
     */
    static final int LAYOUT_NORMAL = 0;

    /**
     * Show the first item
     */
    static final int LAYOUT_FORCE_TOP = 1;

    /**
     * Force the selected item to be on somewhere on the screen
     */
    static final int LAYOUT_SET_SELECTION = 2;

    /**
     * Show the last item
     */
    static final int LAYOUT_FORCE_BOTTOM = 3;

    /**
     * Make a mSelectedItem appear in a specific location and build the rest of
     * the views from there. The top is specified by mSpecificTop.
     */
    static final int LAYOUT_SPECIFIC = 4;

    /**
     * Layout to sync as a result of a data change. Restore mSyncPosition to have its top
     * at mSpecificTop
     */
    static final int LAYOUT_SYNC = 5;

    /**
     * Layout as a result of using the navigation keys
     */
    static final int LAYOUT_MOVE_SELECTION = 6;
    
    /**
     * Always allow a user to over-scroll this view, provided it is a
     * view that can scroll.
     *
     * @see #getOverScrollMode()
     * @see #setOverScrollMode(int)
     */
    static final int OVER_SCROLL_ALWAYS = 0;

    /**
     * Allow a user to over-scroll this view only if the content is large
     * enough to meaningfully scroll, provided it is a view that can scroll.
     *
     * @see #getOverScrollMode()
     * @see #setOverScrollMode(int)
     */
    static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;

    /**
     * Controls how the next layout will happen
     */
    int mLayoutMode = LAYOUT_NORMAL;

    /**
     * Should be used by subclasses to listen to changes in the dataset
     */
    AdapterDataSetObserver mDataSetObserver;

    /**
     * The adapter containing the data to be displayed by this view
     */
    ListAdapter mAdapter;

    /**
     * Indicates whether the list selector should be drawn on top of the children or behind
     */
    boolean mDrawSelectorOnTop = false;

    /**
     * The drawable used to draw the selector
     */
    Drawable mSelector;

    /**
     * Defines the selector's location and dimension at drawing time
     */
    Rect mSelectorRect = new Rect();

    /**
     * The data set used to store unused views that should be reused during the next layout
     * to avoid creating new ones
     */
    final RecycleBin mRecycler = new RecycleBin();

    /**
     * The selection's left padding
     */
    int mSelectionLeftPadding = 0;

    /**
     * The selection's top padding
     */
    int mSelectionTopPadding = 0;

    /**
     * The selection's right padding
     */
    int mSelectionRightPadding = 0;

    /**
     * The selection's bottom padding
     */
    int mSelectionBottomPadding = 0;

    /**
     * This view's padding
     */
    Rect mListPadding = new Rect();

    /**
     * Subclasses must retain their measure spec from onMeasure() into this member
     */
    int mWidthMeasureSpec = 0;

    /**
     * The top scroll indicator
     */
    View mScrollUp;

    /**
     * The down scroll indicator
     */
    View mScrollDown;

    /**
     * When the view is scrolling, this flag is set to true to indicate subclasses that
     * the drawing cache was enabled on the children
     */
    boolean mCachingStarted;

    /**
     * The position of the view that received the down motion event
     */
    int mMotionPosition;

    /**
     * The offset to the top of the mMotionPosition view when the down motion event was received
     */
    int mMotionViewOriginalTop;

    /**
     * The desired offset to the top of the mMotionPosition view after a scroll
     */
    int mMotionViewNewTop;

    /**
     * The X value associated with the the down motion event
     */
    int mMotionX;

    /**
     * The Y value associated with the the down motion event
     */
    int mMotionY;

    /**
     * One of TOUCH_MODE_REST, TOUCH_MODE_DOWN, TOUCH_MODE_TAP, TOUCH_MODE_SCROLL, or
     * TOUCH_MODE_DONE_WAITING
     */
    int mTouchMode = TOUCH_MODE_REST;

    /**
     * Y value from on the previous motion event (if any)
     */
    int mLastY;

    /**
     * How far the finger moved before we started scrolling
     */
    int mMotionCorrection;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    /**
     * Handles one frame of a fling
     */
    private FlingRunnable mFlingRunnable;
    
    /**
     * Handles scrolling between positions within the list.
     */
    private PositionScroller mPositionScroller;

    /**
     * The offset in pixels form the top of the BdAdapterView to the top
     * of the currently selected view. Used to save and restore state.
     */
    int mSelectedTop = 0;

    /**
     * Indicates whether the list is stacked from the bottom edge or
     * the top edge.
     */
    boolean mStackFromBottom;

    /**
     * When set to true, the list automatically discards the children's
     * bitmap cache after scrolling.
     */
    boolean mScrollingCacheEnabled;

    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;

    /**
     * Indicates whether to use pixels-based or position-based scrollbar
     * properties.
     */
    private boolean mSmoothScrollbarEnabled = true;

    /**
     * Rectangle used for hit testing children
     */
    private Rect mTouchFrame;

    /**
     * The position to resurrect the selected position to.
     */
    int mResurrectToPosition = INVALID_POSITION;

    private ContextMenuInfo mContextMenuInfo = null;
    
    /**
     * Maximum distance to record overscroll
     */
    int mOverscrollMax;

    /**
     * Content height divided by this is the overscroll limit.
     */
    static final int OVERSCROLL_LIMIT_DIVISOR = 3;

    /**
     * Used to request a layout when we changed touch mode
     */
    private static final int TOUCH_MODE_UNKNOWN = -1;
    private static final int TOUCH_MODE_ON = 0;
    private static final int TOUCH_MODE_OFF = 1;

    private int mLastTouchMode = TOUCH_MODE_UNKNOWN;

    private static final boolean PROFILE_SCROLLING = false;
    private boolean mScrollProfilingStarted = false;

    private static final boolean PROFILE_FLINGING = false;
    private boolean mFlingProfilingStarted = false;

    /**
     * The last CheckForTap runnable we posted, if any
     */
    private Runnable mPendingCheckForTap;

    /**
     * Acts upon click
     */
    private BdAbsListView.PerformClick mPerformClick;

    /**
     * This view is in transcript mode -- it shows the bottom of the list when the data
     * changes
     */
    private int mTranscriptMode;

    /**
     * Indicates that this list is always drawn on top of a solid, single-color, opaque
     * background
     */
    private int mCacheColorHint;

    /**
     * The select child's view (from the adapter's getView) is enabled.
     */
    private boolean mIsChildViewEnabled;
    
    /**
     * The scroll mode.
     */
    private int mOverScrollMode;

    /**
     * The last scroll state reported to clients through {@link OnScrollListener}.
     */
    private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    private int mTouchSlop;
    @SuppressWarnings("unused")
    private float mDensityScale;
    private Runnable mClearScrollingCache;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    final boolean[] mIsScrap = new boolean[1];

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    /**
     * Maximum distance to overscroll by during edge effects
     */
    int mOverscrollDistance;

    /**
     * Maximum distance to overfling during edge effects
     */
    int mOverflingDistance;

    // These two EdgeGlows are always set and used together.
    // Checking one for null is as good as checking both.

    /**
     * Tracks the state of the top edge glow.
     */
    private EdgeEffect mEdgeGlowTop;

    /**
     * Tracks the state of the bottom edge glow.
     */
    private EdgeEffect mEdgeGlowBottom;

    /**
     * An estimate of how many pixels are between the top of the list and
     * the top of the first position in the adapter, based on the last time
     * we saw it. Used to hint where to draw edge glows.
     */
    private int mFirstPositionDistanceGuess;

    /**
     * An estimate of how many pixels are between the bottom of the list and
     * the bottom of the last position in the adapter, based on the last time
     * we saw it. Used to hint where to draw edge glows.
     */
    private int mLastPositionDistanceGuess;

    /**
     * Used for determining when to cancel out of overscroll.
     */
    private int mDirection = 0;

    /**
     * Interface definition for a callback to be invoked when the list or grid
     * has been scrolled.
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling. Note navigating the list using the trackball counts as
         * being in the idle state since these transitions are not animated.
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the screen
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed a fling. The
         * animation is now coasting to a stop
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link Adapter#getView(int, View, ViewGroup)}.
         *
         * @param view The view whose scroll state is being reported
         *
         * @param scrollState The current scroll state. One of {@link #SCROLL_STATE_IDLE},
         * {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        public void onScrollStateChanged(BdAbsListView view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         * @param view The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if
         *        visibleItemCount == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount the number of items in the list adaptor
         */
        public void onScroll(BdAbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount);
    }

    public BdAbsListView(Context context) {
        super(context);
        initAbsListView();

        setVerticalScrollBarEnabled(true);
        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
        initializeScrollbars(a);
        a.recycle();
    }

    public BdAbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.absListViewStyle);
    }

    public BdAbsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAbsListView();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbsListView, defStyle, 0);

        Drawable d = a.getDrawable(R.styleable.AbsListView_listSelector);
        if (d != null) {
            setSelector(d);
        }

        mDrawSelectorOnTop = a.getBoolean(R.styleable.AbsListView_drawSelectorOnTop, false);

        boolean stackFromBottom = a.getBoolean(R.styleable.AbsListView_stackFromBottom, false);
        setStackFromBottom(stackFromBottom);

        boolean scrollingCacheEnabled = a.getBoolean(R.styleable.AbsListView_scrollingCache, true);
        setScrollingCacheEnabled(scrollingCacheEnabled);

        int transcriptMode = a.getInt(R.styleable.AbsListView_transcriptMode,
                TRANSCRIPT_MODE_DISABLED);
        setTranscriptMode(transcriptMode);

        int color = a.getColor(R.styleable.AbsListView_cacheColorHint, 0);
        setCacheColorHint(color);

        boolean smoothScrollbar = a.getBoolean(R.styleable.AbsListView_smoothScrollbar, true);
        setSmoothScrollbarEnabled(smoothScrollbar);

        a.recycle();
    }

    private void initAbsListView() {
        // Setting focusable in touch mode will set the focusable property to true
        setClickable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setAlwaysDrawnWithCacheEnabled(false);
        setScrollingCacheEnabled(true);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = hasGingerbread() ? configuration.getScaledOverscrollDistance() : 0;
        mOverflingDistance = hasGingerbread() ? configuration.getScaledOverflingDistance() : 0;

        mDensityScale = getContext().getResources().getDisplayMetrics().density;
    }

    // 这个API的Level 9，为了兼容Level 9以下的平台，因此，我们不需要添加override
    public void setOverScrollMode(int mode) {
        if (mode != OVER_SCROLL_NEVER) {
            if (mEdgeGlowTop == null) {
                mEdgeGlowTop = new EdgeEffect(getContext());
                mEdgeGlowBottom = new EdgeEffect(getContext());
            }
        } else {
            mEdgeGlowTop = null;
            mEdgeGlowBottom = null;
        }
        
        mOverScrollMode = mode;
        
        // 如果API Level大于等于9的话，我们才调用父类的方
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            super.setOverScrollMode(mode);
        }
    }
    
    /**
     * NOTE: 这个方法是在Level 9才引入的
     * 
     * Returns the over-scroll mode for this view. The result will be
     * one of {@link #OVER_SCROLL_ALWAYS} (default), {@link #OVER_SCROLL_IF_CONTENT_SCROLLS}
     * (allow over-scrolling only if the view content is larger than the container),
     * or {@link #OVER_SCROLL_NEVER}.
     *
     * @return This view's over-scroll mode.
     */
    public int getOverScrollMode() {
        if (hasGingerbread()) {
            return super.getOverScrollMode();
        }
        
        return mOverScrollMode;
    }
    
    /**
     * @return true if all list content currently fits within the view boundaries
     */
    private boolean contentFits() {
        final int childCount = getChildCount();
        if (childCount != mItemCount) {
            return false;
        }

        return getChildAt(0).getTop() >= 0 && getChildAt(childCount - 1).getBottom() <= getBottom();
    }
    
    /**
     * When smooth scrollbar is enabled, the position and size of the scrollbar thumb
     * is computed based on the number of visible pixels in the visible items. This
     * however assumes that all list items have the same height. If you use a list in
     * which items have different heights, the scrollbar will change appearance as the
     * user scrolls through the list. To avoid this issue, you need to disable this
     * property.
     *
     * When smooth scrollbar is disabled, the position and size of the scrollbar thumb
     * is based solely on the number of items in the adapter and the position of the
     * visible items inside the adapter. This provides a stable scrollbar as the user
     * navigates through a list of items with varying heights.
     *
     * @param enabled Whether or not to enable smooth scrollbar.
     *
     * @see #setSmoothScrollbarEnabled(boolean)
     * @attr ref android.R.styleable#AbsListView_smoothScrollbar
     */
    public void setSmoothScrollbarEnabled(boolean enabled) {
        mSmoothScrollbarEnabled = enabled;
    }

    /**
     * Returns the current state of the fast scroll feature.
     *
     * @return True if smooth scrollbar is enabled is enabled, false otherwise.
     *
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    @ViewDebug.ExportedProperty
    public boolean isSmoothScrollbarEnabled() {
        return mSmoothScrollbarEnabled;
    }

    /**
     * Set the listener that will receive notifications every time the list scrolls.
     *
     * @param l the scroll listener
     */
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
        invokeOnItemScrollListener();
    }

    /**
     * Notify our scroll listener (if there is one) of a change in scroll state
     */
    void invokeOnItemScrollListener() {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, mFirstPosition, getChildCount(), mItemCount);
        }
    }

    /**
     * Indicates whether the children's drawing cache is used during a scroll.
     * By default, the drawing cache is enabled but this will consume more memory.
     *
     * @return true if the scrolling cache is enabled, false otherwise
     *
     * @see #setScrollingCacheEnabled(boolean)
     * @see View#setDrawingCacheEnabled(boolean)
     */
    @ViewDebug.ExportedProperty
    public boolean isScrollingCacheEnabled() {
        return mScrollingCacheEnabled;
    }

    /**
     * Enables or disables the children's drawing cache during a scroll.
     * By default, the drawing cache is enabled but this will use more memory.
     *
     * When the scrolling cache is enabled, the caches are kept after the
     * first scrolling. You can manually clear the cache by calling
     * {@link android.view.ViewGroup#setChildrenDrawingCacheEnabled(boolean)}.
     *
     * @param enabled true to enable the scroll cache, false otherwise
     *
     * @see #isScrollingCacheEnabled()
     * @see View#setDrawingCacheEnabled(boolean)
     */
    public void setScrollingCacheEnabled(boolean enabled) {
        if (mScrollingCacheEnabled && !enabled) {
            clearScrollingCache();
        }
        mScrollingCacheEnabled = enabled;
    }

    @Override
    public void getFocusedRect(Rect r) {
        View view = getSelectedView();
        if (view != null && view.getParent() == this) {
            // the focused rectangle of the selected view offset into the
            // coordinate space of this view.
            view.getFocusedRect(r);
            offsetDescendantRectToMyCoords(view, r);
        } else {
            // otherwise, just the norm
            super.getFocusedRect(r);
        }
    }

    private void useDefaultSelector() {
        setSelector(getResources().getDrawable(android.R.drawable.list_selector_background));
    }

    /**
     * Indicates whether the content of this view is pinned to, or stacked from,
     * the bottom edge.
     *
     * @return true if the content is stacked from the bottom edge, false otherwise
     */
    @ViewDebug.ExportedProperty
    public boolean isStackFromBottom() {
        return mStackFromBottom;
    }

    /**
     * When stack from bottom is set to true, the list fills its content starting from
     * the bottom of the view.
     *
     * @param stackFromBottom true to pin the view's content to the bottom edge,
     *        false to pin the view's content to the top edge
     */
    public void setStackFromBottom(boolean stackFromBottom) {
        if (mStackFromBottom != stackFromBottom) {
            mStackFromBottom = stackFromBottom;
            requestLayoutIfNecessary();
        }
    }

    void requestLayoutIfNecessary() {
        if (getChildCount() > 0) {
            resetList();
            requestLayout();
            invalidate();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        mDataChanged = true;
        requestLayout();
    }

    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests && !mInLayout) {
            super.requestLayout();
        }
    }

    /**
     * The list is empty. Clear everything out.
     */
    void resetList() {
        removeAllViewsInLayout();
        mFirstPosition = 0;
        mDataChanged = false;
        mNeedSync = false;
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        mSelectedTop = 0;
        mSelectorRect.setEmpty();
        invalidate();
    }

    @Override
    protected int computeVerticalScrollExtent() {
        final int count = getChildCount();
        if (count > 0) {
            if (mSmoothScrollbarEnabled) {
                int extent = count * 100;

                View view = getChildAt(0);
                //final int top = view.getTop();
                final int top = getFillChildTop();

                int height = view.getHeight();
                if (height > 0) {
                    extent += (top * 100) / height;
                }

                view = getChildAt(count - 1);
                //final int bottom = view.getBottom();
                final int bottom = getScrollChildBottom();
                height = view.getHeight();
                if (height > 0) {
                    extent -= ((bottom - getHeight()) * 100) / height;
                }

                return extent;
            } else {
                return 1;
            }
        }
        return 0;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        final int firstPosition = mFirstPosition;
        final int childCount = getChildCount();
        if (firstPosition >= 0 && childCount > 0) {
            if (mSmoothScrollbarEnabled) {
                final View view = getChildAt(0);
                //final int top = view.getTop();
                final int top = getFillChildTop();
                int height = view.getHeight();
                if (height > 0) {
                    return Math.max(firstPosition * 100 - (top * 100) / height
                            + (int) ((float) getScrollY() / getHeight() * mItemCount * 100), 0);
                }
            } else {
                int index;
                final int count = mItemCount;
                if (firstPosition == 0) {
                    index = 0;
                } else if (firstPosition + childCount == count) {
                    index = count;
                } else {
                    index = firstPosition + childCount / 2;
                }
                return (int) (firstPosition + childCount * (index / (float) count));
            }
        }
        return 0;
    }

    @Override
    protected int computeVerticalScrollRange() {
        int result;
        if (mSmoothScrollbarEnabled) {
            result = Math.max(mItemCount * 100, 0);
            if (getScrollY() != 0) {
                // Compensate for overscroll
                result += Math.abs((int) ((float) getScrollY() / getHeight() * mItemCount * 100));
            }
        } else {
            result = mItemCount;
        }
        return result;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        final int count = getChildCount();
        final float fadeEdge = super.getTopFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        } else {
            if (mFirstPosition > 0) {
                return 1.0f;
            }

            final int top = getChildAt(0).getTop();
            final float fadeLength = (float) getVerticalFadingEdgeLength();
            // return top < mPaddingTop ? (float) -(top - mPaddingTop) /
            // fadeLength : fadeEdge;
            return top < getPaddingTop() ? (float) -(top - getPaddingTop()) / fadeLength : fadeEdge;
        }
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        final int count = getChildCount();
        final float fadeEdge = super.getBottomFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        } else {
            if (mFirstPosition + count - 1 < mItemCount - 1) {
                return 1.0f;
            }

            final int bottom = getChildAt(count - 1).getBottom();
            final int height = getHeight();
            final float fadeLength = (float) getVerticalFadingEdgeLength();
            // return bottom > height - mPaddingBottom ? (float) (bottom -
            // height + mPaddingBottom) / fadeLength : fadeEdge;
            return bottom > height - getPaddingBottom() ? (float) (bottom - height + getPaddingBottom()) / fadeLength : fadeEdge;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSelector == null) {
            useDefaultSelector();
        }
        final Rect listPadding = mListPadding;
        // listPadding.left = mSelectionLeftPadding + mPaddingLeft;
        // listPadding.top = mSelectionTopPadding + mPaddingTop;
        // listPadding.right = mSelectionRightPadding + mPaddingRight;
        // listPadding.bottom = mSelectionBottomPadding + mPaddingBottom;
        
        // 修正问题：为了特殊的需求，我们不需要将Selector的Padding作用到ListView的Padding上面
        //listPadding.left = getPaddingLeft();
        listPadding.top = getPaddingTop();
        listPadding.right = getPaddingRight();
        listPadding.bottom = getPaddingBottom();
    }

    /**
     * Subclasses should NOT override this method but
     *  {@link #layoutChildren()} instead.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mInLayout = true;
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
            mRecycler.markChildrenDirty();
        }
        layoutChildren();

        mInLayout = false;
    }

    /**
     * Subclasses must override this method to layout their children.
     */
    protected void layoutChildren() {
    }

    @Override
    @ViewDebug.ExportedProperty
    public View getSelectedView() {
        return null;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     *
     * @see android.view.View#getPaddingTop()
     * @see #getSelector()
     *
     * @return The top list padding.
     */
    public int getListPaddingTop() {
        return mListPadding.top;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     *
     * @see android.view.View#getPaddingBottom()
     * @see #getSelector()
     *
     * @return The bottom list padding.
     */
    public int getListPaddingBottom() {
        return mListPadding.bottom;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     *
     * @see android.view.View#getPaddingLeft()
     * @see #getSelector()
     *
     * @return The left list padding.
     */
    public int getListPaddingLeft() {
        return mListPadding.left;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     *
     * @see android.view.View#getPaddingRight()
     * @see #getSelector()
     *
     * @return The right list padding.
     */
    public int getListPaddingRight() {
        return mListPadding.right;
    }

    /**
     * Get a view and have it show the data associated with the specified
     * position. This is called when we have already discovered that the view is
     * not available for reuse in the recycle bin. The only choices left are
     * converting an old view or making a new one.
     *
     * @param position The position to display
     * @param isScrap Array of at least 1 boolean, the first entry will become true if
     *                the returned view was taken from the scrap heap, false if otherwise.
     * 
     * @return A view displaying the data associated with the specified position
     */
    View obtainView(int position, boolean[] isScrap) {
        isScrap[0] = false;
        View scrapView;

        scrapView = mRecycler.getScrapView(position);

        View child;
        if (scrapView != null) {
            if (ViewDebug.TRACE_RECYCLER) {
                ViewDebug.trace(scrapView, ViewDebug.RecyclerTraceType.RECYCLE_FROM_SCRAP_HEAP,
                        position, -1);
            }

            child = mAdapter.getView(position, scrapView, this);

            if (ViewDebug.TRACE_RECYCLER) {
                ViewDebug.trace(child, ViewDebug.RecyclerTraceType.BIND_VIEW,
                        position, getChildCount());
            }

            if (child != scrapView) {
                mRecycler.addScrapView(scrapView);
                if (mCacheColorHint != 0) {
                    child.setDrawingCacheBackgroundColor(mCacheColorHint);
                }
                if (ViewDebug.TRACE_RECYCLER) {
                    ViewDebug.trace(scrapView, ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP,
                            position, -1);
                }
            } else {
                isScrap[0] = true;
                // child.dispatchFinishTemporaryDetach();
                dispatchFinishTemporaryDetach(child);
            }
        } else {
            child = mAdapter.getView(position, null, this);
            if (mCacheColorHint != 0) {
                child.setDrawingCacheBackgroundColor(mCacheColorHint);
            }
            if (ViewDebug.TRACE_RECYCLER) {
                ViewDebug.trace(child, ViewDebug.RecyclerTraceType.NEW_VIEW,
                        position, getChildCount());
            }
        }

        return child;
    }

    void positionSelector(View sel) {
        final Rect selectorRect = mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        positionSelector(selectorRect.left, selectorRect.top, selectorRect.right,
                selectorRect.bottom);

        final boolean isChildViewEnabled = mIsChildViewEnabled;
        if (sel.isEnabled() != isChildViewEnabled) {
            mIsChildViewEnabled = !isChildViewEnabled;
            refreshDrawableState();
        }
    }

    private void positionSelector(int l, int t, int r, int b) {
        mSelectorRect.set(l - mSelectionLeftPadding, t - mSelectionTopPadding, r
                + mSelectionRightPadding, b + mSelectionBottomPadding);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        final boolean drawSelectorOnTop = mDrawSelectorOnTop;
        if (!drawSelectorOnTop) {
            drawSelector(canvas);
        }

        super.dispatchDraw(canvas);

        if (drawSelectorOnTop) {
            drawSelector(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getChildCount() > 0) {
            mDataChanged = true;
            rememberSyncState();
        }
    }

    /**
     * @return True if the current touch mode requires that we draw the selector in the pressed
     *         state.
     */
    boolean touchModeDrawsInPressedState() {
        // FIXME use isPressed for this
        switch (mTouchMode) {
        case TOUCH_MODE_TAP:
        case TOUCH_MODE_DONE_WAITING:
            return true;
        default:
            return false;
        }
    }

    /**
     * Indicates whether this view is in a state where the selector should be drawn. This will
     * happen if we have focus but are not in touch mode, or we are in the middle of displaying
     * the pressed state for an item.
     *
     * @return True if the selector should be shown
     */
    boolean shouldShowSelector() {
        return (hasFocus() && !isInTouchMode()) || touchModeDrawsInPressedState();
    }

    private void drawSelector(Canvas canvas) {
        if (shouldShowSelector() && mSelectorRect != null && !mSelectorRect.isEmpty()) {
            final Drawable selector = mSelector;
            selector.setBounds(mSelectorRect);
            selector.draw(canvas);
        }
    }

    /**
     * Controls whether the selection highlight drawable should be drawn on top of the item or
     * behind it.
     *
     * @param onTop If true, the selector will be drawn on the item it is highlighting. The default
     *        is false.
     *
     * @attr ref android.R.styleable#AbsListView_drawSelectorOnTop
     */
    public void setDrawSelectorOnTop(boolean onTop) {
        mDrawSelectorOnTop = onTop;
    }

    /**
     * Set a Drawable that should be used to highlight the currently selected item.
     *
     * @param resID A Drawable resource to use as the selection highlight.
     *
     * @attr ref android.R.styleable#AbsListView_listSelector
     */
    public void setSelector(int resID) {
        setSelector(getResources().getDrawable(resID));
    }

    public void setSelector(Drawable sel) {
        if (mSelector != null) {
            mSelector.setCallback(null);
            unscheduleDrawable(mSelector);
        }
        mSelector = sel;
        Rect padding = new Rect();
        sel.getPadding(padding);
        mSelectionLeftPadding = padding.left;
        mSelectionTopPadding = padding.top;
        mSelectionRightPadding = padding.right;
        mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        sel.setState(getDrawableState());
    }

    /**
     * Returns the selector {@link android.graphics.drawable.Drawable} that is used to draw the
     * selection in the list.
     *
     * @return the drawable used to display the selector
     */
    public Drawable getSelector() {
        return mSelector;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mSelector != null) {
            mSelector.setState(getDrawableState());
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // If the child view is enabled then do the default behavior.
        if (mIsChildViewEnabled) {
            // Common case
            return super.onCreateDrawableState(extraSpace);
        }

        // The selector uses this View's drawable state. The selected child view
        // is disabled, so we need to remove the enabled state from the drawable
        // states.
        final int enabledState = ENABLED_STATE_SET[0];

        // If we don't have any extra space, it will return one of the static state arrays,
        // and clearing the enabled state on those arrays is a bad thing!  If we specify
        // we need extra space, it will create+copy into a new array that safely mutable.
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        int enabledPos = -1;
        for (int i = state.length - 1; i >= 0; i--) {
            if (state[i] == enabledState) {
                enabledPos = i;
                break;
            }
        }

        // Remove the enabled state
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + 1, state, enabledPos,
                    state.length - enabledPos - 1);
        }

        return state;
    }

    @Override
    public boolean verifyDrawable(Drawable dr) {
        return mSelector == dr || super.verifyDrawable(dr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final ViewTreeObserver treeObserver = getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.addOnTouchModeChangeListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Detach any view left in the scrap heap
        mRecycler.clear();

        final ViewTreeObserver treeObserver = getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.removeOnTouchModeChangeListener(this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        final int touchMode = isInTouchMode() ? TOUCH_MODE_ON : TOUCH_MODE_OFF;

        if (!hasWindowFocus) {
            setChildrenDrawingCacheEnabled(false);
            if (mFlingRunnable != null) {
                removeCallbacks(mFlingRunnable);
                // let the fling runnable report it's new state which
                // should be idle
                mFlingRunnable.endFling();

                if (getScrollY() != 0) {
                    // mScrollY = 0;
                    finishGlows();
                    //scrollTo(getScrollX(), 0);
                    invalidate();
                }
            }
        } else {

            // If we changed touch mode since the last time we had focus
            if (touchMode != mLastTouchMode && mLastTouchMode != TOUCH_MODE_UNKNOWN) {
                // If we come back in trackball mode, we bring the selection back
                if (touchMode == TOUCH_MODE_OFF) {
                    // This will trigger a layout
                    //resurrectSelection();

                // If we come back in touch mode, then we want to hide the selector
                } else {
                    hideSelector();
                    mLayoutMode = LAYOUT_NORMAL;
                    layoutChildren();
                }
            }
        }

        mLastTouchMode = touchMode;
    }

    /**
     * Creates the ContextMenuInfo returned from {@link #getContextMenuInfo()}. This
     * methods knows the view, position and ID of the item that received the
     * long press.
     *
     * @param view The view that received the long press.
     * @param position The position of the item that received the long press.
     * @param id The ID of the item that received the long press.
     * @return The extra information that should be returned by
     *         {@link #getContextMenuInfo()}.
     */
    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }

    /**
     * A base class for Runnables that will check that their view is still attached to
     * the original window as when the Runnable was created.
     *
     */
    private class WindowRunnnable {
        private int mOriginalAttachCount;

        public void rememberWindowAttachCount() {
            mOriginalAttachCount = getWindowAttachCount();
        }

        public boolean sameWindow() {
            return hasWindowFocus() && getWindowAttachCount() == mOriginalAttachCount;
        }
    }

    private class PerformClick extends WindowRunnnable implements Runnable {
        View mChild;
        int mClickMotionPosition;

        public void run() {
            // The data has changed since we posted this action in the event queue,
            // bail out before bad things happen
            if (mDataChanged) return;

            final ListAdapter adapter = mAdapter;
            final int motionPosition = mClickMotionPosition;
            if (adapter != null && mItemCount > 0 && motionPosition != INVALID_POSITION && motionPosition < adapter.getCount()
                    && sameWindow()) {
                performItemClick(mChild, motionPosition, adapter.getItemId(motionPosition));
            }
        }
    }

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getPositionForView(originalView);
        if (longPressPosition >= 0) {
            final long longPressId = mAdapter.getItemId(longPressPosition);
            boolean handled = false;

            if (mOnItemLongClickListener != null) {
                handled = mOnItemLongClickListener.onItemLongClick(BdAbsListView.this, originalView, longPressPosition, longPressId);
            }
            if (!handled) {
                mContextMenuInfo = createContextMenuInfo(getChildAt(longPressPosition - mFirstPosition), longPressPosition, longPressId);
                handled = super.showContextMenuForChild(originalView);
            }

            return handled;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        // Don't dispatch setPressed to our children. We call setPressed on ourselves to
        // get the selector in the right state, but we don't want to press each child.
    }

    /**
     * Maps a point to a position in the list.
     *
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return mFirstPosition + i;
                }
            }
        }
        return INVALID_POSITION;
    }


    /**
     * Maps a point to a the rowId of the item which intersects that point.
     *
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The rowId of the item which contains the specified point, or {@link #INVALID_ROW_ID}
     *         if the point does not intersect an item.
     */
    public long pointToRowId(int x, int y) {
        int position = pointToPosition(x, y);
        if (position >= 0) {
            return mAdapter.getItemId(position);
        }
        return INVALID_ROW_ID;
    }

    final class CheckForTap implements Runnable {
        public void run() {
            if (mTouchMode == TOUCH_MODE_DOWN) {
                mTouchMode = TOUCH_MODE_TAP;
                final View child = getChildAt(mMotionPosition - mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    mLayoutMode = LAYOUT_NORMAL;

                    if (!mDataChanged) {
                        layoutChildren();
                        child.setPressed(true);
                        positionSelector(child);
                        setPressed(true);

                        final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                        final boolean longClickable = isLongClickable();

                        if (mSelector != null) {
                            Drawable d = mSelector.getCurrent();
                            if (d != null && d instanceof TransitionDrawable) {
                                if (longClickable) {
                                    ((TransitionDrawable) d).startTransition(longPressTimeout);
                                } else {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                        }

                        if (longClickable) {
                        } else {
                            mTouchMode = TOUCH_MODE_DONE_WAITING;
                        }
                    } else {
                        mTouchMode = TOUCH_MODE_DONE_WAITING;
                    }
                }
            }
        }
    }

    private boolean startScrollIfNeeded(int deltaY) {
        // Check if we have moved far enough that it looks more like a
        // scroll than a tap
        final int distance = Math.abs(deltaY);
        final boolean overscroll = getScrollY() != 0;
        if (overscroll || distance > mTouchSlop) {
            createScrollingCache();
            mTouchMode = overscroll ? TOUCH_MODE_OVERSCROLL : TOUCH_MODE_SCROLL;
            mMotionCorrection = deltaY;
            final Handler handler = getHandler();
            // Handler should not be null unless the AbsListView is not attached to a
            // window, which would make it very hard to scroll it... but the monkeys
            // say it's possible.
            if (handler != null) {
                //handler.removeCallbacks(mPendingCheckForLongPress);
            }
            setPressed(false);
            View motionView = getChildAt(mMotionPosition - mFirstPosition);
            if (motionView != null) {
                motionView.setPressed(false);
            }
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            // Time to start stealing events! Once we've stolen them, don't let anyone
            // steal from us
            requestDisallowInterceptTouchEvent(true);
            return true;
        }

        return false;
    }

    public void onTouchModeChanged(boolean isInTouchMode) {
        if (isInTouchMode) {
            // Get rid of the selection when we enter touch mode
            hideSelector();
            // Layout, but only if we already have done so previously.
            // (Otherwise may clobber a LAYOUT_SYNC layout that was requested to restore
            // state.)
            if (getHeight() > 0 && getChildCount() > 0) {
                // We do not lose focus initiating a touch (since AbsListView is focusable in
                // touch mode). Force an initial layout to get rid of the selection.
                layoutChildren();
            }
        } else {
            int touchMode = mTouchMode;
            if (touchMode == TOUCH_MODE_OVERSCROLL || touchMode == TOUCH_MODE_OVERFLING) {
                if (mFlingRunnable != null) {
                    mFlingRunnable.endFling();
                }

                if (getScrollY() != 0) {
                    //setScrollY(0);
                    finishGlows();
                    invalidate();
                }
            }
        }
    }
    
    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return isClickable() || isLongClickable();
        }

        final int action = ev.getAction();

        View v;
        int deltaY;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            switch (mTouchMode) {
            case TOUCH_MODE_OVERFLING: {
                mFlingRunnable.endFling();
                mTouchMode = TOUCH_MODE_OVERSCROLL;
                mMotionY = mLastY = (int) ev.getY();
                mMotionCorrection = 0;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            default: {
                mActivePointerId = ev.getPointerId(0);
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                int motionPosition = pointToPosition(x, y);
                if (!mDataChanged) {
                    if ((mTouchMode != TOUCH_MODE_FLING) && (motionPosition >= 0)
                            && (getAdapter().isEnabled(motionPosition))) {
                        // User clicked on an actual view (and was not stopping a fling). It might be a
                        // click or a scroll. Assume it is a click until proven otherwise
                        mTouchMode = TOUCH_MODE_DOWN;
                        // FIXME Debounce
                        if (mPendingCheckForTap == null) {
                            mPendingCheckForTap = new CheckForTap();
                        }
                        postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    } else {
                        if (ev.getEdgeFlags() != 0 && motionPosition < 0) {
                            // If we couldn't find a view to click on, but the down event was touching
                            // the edge, we will bail out and try again. This allows the edge correcting
                            // code in ViewRoot to try to find a nearby view to select
                            return false;
                        }

                        if (mTouchMode == TOUCH_MODE_FLING) {
                            // Stopped a fling. It is a scroll.
                            createScrollingCache();
                            mTouchMode = TOUCH_MODE_SCROLL;
                            mMotionCorrection = 0;
                            motionPosition = findMotionRow(y);
                            reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                        }
                    }
                }

                if (motionPosition >= 0) {
                    // Remember where the motion event started
                    v = getChildAt(motionPosition - mFirstPosition);
                    mMotionViewOriginalTop = v.getTop();
                }
                mMotionX = x;
                mMotionY = y;
                mMotionPosition = motionPosition;
                mLastY = Integer.MIN_VALUE;
                break;
            }
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final int y = (int) ev.getY(pointerIndex);
            deltaY = y - mMotionY;
            switch (mTouchMode) {
            case TOUCH_MODE_DOWN:
            case TOUCH_MODE_TAP:
            case TOUCH_MODE_DONE_WAITING:
                // Check if we have moved far enough that it looks more like a
                // scroll than a tap
                startScrollIfNeeded(deltaY);
                break;
            case TOUCH_MODE_SCROLL:
                if (PROFILE_SCROLLING) {
                    if (!mScrollProfilingStarted) {
                        Debug.startMethodTracing("AbsListViewScroll");
                        mScrollProfilingStarted = true;
                    }
                }
                
                if (y != mLastY) {
                    final int rawDeltaY = deltaY;
                    deltaY -= mMotionCorrection;
                    
                    int incrementalDeltaY = mLastY != Integer.MIN_VALUE ? y - mLastY : deltaY;
                    final int motionIndex;
                    if (mMotionPosition >= 0) {
                        motionIndex = mMotionPosition - mFirstPosition;
                    } else {
                        // If we don't have a motion position that we can reliably track,
                        // pick something in the middle to make a best guess at things below.
                        motionIndex = getChildCount() / 2;
                    }

                    int motionViewPrevTop = 0;
                    View motionView = this.getChildAt(motionIndex);
                    if (motionView != null) {
                        motionViewPrevTop = motionView.getTop();
                    }

                    // No need to do all this work if we're not going to move anyway
                    boolean atEdge = false;
                    if (incrementalDeltaY != 0) {
                        atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
                    }

                    // Check to see if we have bumped into the scroll limit
                    motionView = this.getChildAt(motionIndex);
                    if (motionView != null) {
                        // Check if the top of the motion view is where it is
                        // supposed to be
                        final int motionViewRealTop = motionView.getTop();
                        if (atEdge && getChildCount() > 0) {
                            int overscroll = -incrementalDeltaY -
                                    (motionViewRealTop - motionViewPrevTop);
                            if (hasGingerbread()) {
                                overScrollBy(0, overscroll, 0, getScrollY(), 0, 0,
                                        0, mOverscrollDistance, true);
                            }
                            
                            // Treat this like we're starting a new scroll from the current
                            // position. This will let the user start scrolling back
                            // into content immediately rather than needing to scroll
                            // back to the point where they hit the limit first.
                            int motionPosition = findMotionRow(y);
                            if (motionPosition >= 0) {
                                motionView = getChildAt(motionPosition - mFirstPosition);
                                mMotionViewOriginalTop = motionView.getTop();
                            }
                            
                            final int overscrollMode = getOverScrollMode();
                            if (overscrollMode == OVER_SCROLL_ALWAYS ||
                                    (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS &&
                                            !contentFits())) {
                                mDirection = 0; // Reset when entering overscroll.
                                mTouchMode = TOUCH_MODE_OVERSCROLL;
                                if (rawDeltaY > 0) {
                                    if (null != mEdgeGlowTop) {
                                        mEdgeGlowTop.onPull((float) overscroll / getHeight());
                                    }
                                    
                                    if (null != mEdgeGlowBottom) {
                                        if (!mEdgeGlowBottom.isFinished()) {
                                            mEdgeGlowBottom.onRelease();
                                        }
                                    }
                                } else if (rawDeltaY < 0) {
                                    if (null != mEdgeGlowBottom) {
                                        mEdgeGlowBottom.onPull((float) overscroll / getHeight());
                                    }
                                    
                                    if (null != mEdgeGlowTop) {
                                        if (!mEdgeGlowTop.isFinished()) {
                                            mEdgeGlowTop.onRelease();
                                        }
                                    }
                                }
                            }
                            
                            mMotionY = y;
                            mMotionPosition = motionPosition;
                            invalidate();
                        }
                    }
                    mLastY = y;
                }
                break;
                

            case TOUCH_MODE_OVERSCROLL:
                if (y != mLastY) {
                    final int rawDeltaY = deltaY;
                    deltaY -= mMotionCorrection;
                    int incrementalDeltaY = mLastY != Integer.MIN_VALUE ? y - mLastY : deltaY;

                    final int oldScroll = getScrollY();
                    final int newScroll = oldScroll - incrementalDeltaY;
                    int newDirection = y > mLastY ? 1 : -1;

                    if (mDirection == 0) {
                        mDirection = newDirection;
                    }

                    if (mDirection != newDirection) {
                        // Coming back to 'real' list scrolling
                        incrementalDeltaY = -newScroll;
                        //setScrollY(0);

                        // No need to do all this work if we're not going to move anyway
                        if (incrementalDeltaY != 0) {
                            trackMotionScroll(incrementalDeltaY, incrementalDeltaY);
                        }

                        // Check to see if we are back in
                        View motionView = this.getChildAt(mMotionPosition - mFirstPosition);
                        if (motionView != null) {
                            mTouchMode = TOUCH_MODE_SCROLL;

                            // We did not scroll the full amount. Treat this essentially like the
                            // start of a new touch scroll
                            final int motionPosition = findClosestMotionRow(y);

                            mMotionCorrection = 0;
                            motionView = getChildAt(motionPosition - mFirstPosition);
                            mMotionViewOriginalTop = motionView.getTop();
                            mMotionY = y;
                            mMotionPosition = motionPosition;
                        }
                    } else {
                        if (hasGingerbread()) {
                            overScrollBy(0, -incrementalDeltaY, 0, getScrollY(), 0, 0,
                                    0, mOverscrollDistance, true);
                        }
                        
                        final int overscrollMode = getOverScrollMode();
                        if (overscrollMode == OVER_SCROLL_ALWAYS ||
                                (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS &&
                                        !contentFits())) {
                            if (rawDeltaY > 0) {
                                if (null != mEdgeGlowTop) mEdgeGlowTop.onPull((float) -incrementalDeltaY / getHeight());
                                if (null != mEdgeGlowBottom && !mEdgeGlowBottom.isFinished()) {
                                    mEdgeGlowBottom.onRelease();
                                }
                            } else if (rawDeltaY < 0) {
                                if (null != mEdgeGlowBottom) mEdgeGlowBottom.onPull((float) -incrementalDeltaY / getHeight());
                                if (null != mEdgeGlowTop && !mEdgeGlowTop.isFinished()) {
                                    mEdgeGlowTop.onRelease();
                                }
                            }
                            invalidate();
                        }
                        if (Math.abs(mOverscrollDistance) == Math.abs(getScrollY())) {
                            // Don't allow overfling if we're at the edge.
                            mVelocityTracker.clear();
                        }
                    }
                    mLastY = y;
                    mDirection = newDirection;
                }
                break;
            }
            break;
        }

        case MotionEvent.ACTION_UP: {
            switch (mTouchMode) {
            case TOUCH_MODE_DOWN:
            case TOUCH_MODE_TAP:
            case TOUCH_MODE_DONE_WAITING:
                final int motionPosition = mMotionPosition;
                final View child = getChildAt(motionPosition - mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    if (mTouchMode != TOUCH_MODE_DOWN) {
                        child.setPressed(false);
                    }

                    if (mPerformClick == null) {
                        mPerformClick = new PerformClick();
                    }

                    final BdAbsListView.PerformClick performClick = mPerformClick;
                    performClick.mChild = child;
                    performClick.mClickMotionPosition = motionPosition;
                    performClick.rememberWindowAttachCount();

                    mResurrectToPosition = motionPosition;

                    if (mTouchMode == TOUCH_MODE_DOWN || mTouchMode == TOUCH_MODE_TAP) {
                        mLayoutMode = LAYOUT_NORMAL;
                        if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
                            mTouchMode = TOUCH_MODE_TAP;
                            layoutChildren();
                            child.setPressed(true);
                            positionSelector(child);
                            setPressed(true);
                            if (mSelector != null) {
                                Drawable d = mSelector.getCurrent();
                                if (d != null && d instanceof TransitionDrawable) {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                            postDelayed(new Runnable() {
                                public void run() {
                                    child.setPressed(false);
                                    setPressed(false);
                                    if (!mDataChanged) {
                                        post(performClick);
                                    }
                                    hideSelector();
                                    mTouchMode = TOUCH_MODE_REST;
                                }
                            }, ViewConfiguration.getPressedStateDuration());
                        } else {
                            mTouchMode = TOUCH_MODE_REST;
                        }
                        return true;
                    } else if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
                        post(performClick);
                    }
                }
                mTouchMode = TOUCH_MODE_REST;
                break;
            case TOUCH_MODE_SCROLL:
                final int childCount = getChildCount();
                if (childCount > 0) {
                    final int firstChildTop = getFillChildTop();
                    final int lastChildBottom = getFillChildBottom();
                    final int contentTop = mListPadding.top;
                    final int contentBottom = getHeight() - mListPadding.bottom;
                    if (mFirstPosition == 0 && firstChildTop >= contentTop &&
                            mFirstPosition + childCount < mItemCount &&
                            lastChildBottom <= getHeight() - contentBottom) {
                        mTouchMode = TOUCH_MODE_REST;
                        reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    } else {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        //final int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                        final int initialVelocity = hasEclairMR1() ? (int) velocityTracker.getYVelocity(mActivePointerId) : 0;

                        // Fling if we have enough velocity and we aren't at a boundary.
                        // Since we can potentially overfling more than we can overscroll, don't
                        // allow the weird behavior where you can scroll to a boundary then
                        // fling further.
                        if (Math.abs(initialVelocity) > mMinimumVelocity &&
                                !((mFirstPosition == 0 &&
                                        firstChildTop == contentTop - mOverscrollDistance) ||
                                  (mFirstPosition + childCount == mItemCount &&
                                        lastChildBottom == contentBottom + mOverscrollDistance))) {
                            if (mFlingRunnable == null) {
                                mFlingRunnable = new FlingRunnable();
                            }
                            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                            
                            mFlingRunnable.start(-initialVelocity);
                        } else {
                            mTouchMode = TOUCH_MODE_REST;
                            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                            if (mFlingRunnable != null) {
                                mFlingRunnable.endFling();
                            }
                            if (mPositionScroller != null) {
                                mPositionScroller.stop();
                            }
                        }
                    }
                } else {
                    mTouchMode = TOUCH_MODE_REST;
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                break;

            case TOUCH_MODE_OVERSCROLL:
                if (mFlingRunnable == null) {
                    mFlingRunnable = new FlingRunnable();
                }
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);

                reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                if (Math.abs(initialVelocity) > mMinimumVelocity) {
                    mFlingRunnable.startOverfling(-initialVelocity);
                } else {
                    mFlingRunnable.startSpringback();
                }

                break;
            }

            setPressed(false);

            if (mEdgeGlowTop != null) {
                mEdgeGlowTop.onRelease();
            }
            
            if (null != mEdgeGlowBottom) {
                mEdgeGlowBottom.onRelease();
            }

            // Need to redraw since we probably aren't drawing the selector anymore
            invalidate();


            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            
            mActivePointerId = INVALID_POINTER;

            if (PROFILE_SCROLLING) {
                if (mScrollProfilingStarted) {
                    Debug.stopMethodTracing();
                    mScrollProfilingStarted = false;
                }
            }
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
            switch (mTouchMode) {
            case TOUCH_MODE_OVERSCROLL:
                if (mFlingRunnable == null) {
                    mFlingRunnable = new FlingRunnable();
                }
                mFlingRunnable.startSpringback();
                break;

            case TOUCH_MODE_OVERFLING:
                // Do nothing - let it play out.
                break;

            default:
                mTouchMode = TOUCH_MODE_REST;
                setPressed(false);
                View motionView = this.getChildAt(mMotionPosition - mFirstPosition);
                if (motionView != null) {
                    motionView.setPressed(false);
                }
                clearScrollingCache();


                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            
            if (mEdgeGlowTop != null) {
                mEdgeGlowTop.onRelease();
            }
            
            if (null != mEdgeGlowBottom) {
                mEdgeGlowBottom.onRelease();
            }
            mActivePointerId = INVALID_POINTER;
            break;
        }
        
        case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(ev);
            final int x = mMotionX;
            final int y = mMotionY;
            final int motionPosition = pointToPosition(x, y);
            if (motionPosition >= 0) {
                // Remember where the motion event started
                v = getChildAt(motionPosition - mFirstPosition);
                mMotionViewOriginalTop = v.getTop();
                mMotionPosition = motionPosition;
            }
            mLastY = y;
            break;
        }
        }

        return true;
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mEdgeGlowTop != null) {
            final int scrollY = getScrollY();
            if (!mEdgeGlowTop.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth();

                canvas.translate(-width / 1, Math.min(0, scrollY + mFirstPositionDistanceGuess));
                mEdgeGlowTop.setSize(width * 1, getHeight());
                if (mEdgeGlowTop.draw(canvas)) {
                    invalidate();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
        
        if (null != mEdgeGlowBottom) {
            final int scrollY = getScrollY();
            if (!mEdgeGlowBottom.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth();
                final int height = getHeight();
    
                canvas.translate(-width / 1,
                        Math.max(height, scrollY + mLastPositionDistanceGuess));
                canvas.rotate(180, width, 0);
                mEdgeGlowBottom.setSize(width * 1, height);
                if (mEdgeGlowBottom.draw(canvas)) {
                    invalidate();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        View v;

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            int touchMode = mTouchMode;
            if (touchMode == TOUCH_MODE_OVERFLING || touchMode == TOUCH_MODE_OVERSCROLL) {
                mMotionCorrection = 0;
                return true;
            }
            
            final int x = (int) ev.getX();
            final int y = (int) ev.getY();
            mActivePointerId = ev.getPointerId(0);
            
            int motionPosition = findMotionRow(y);
            if (touchMode != TOUCH_MODE_FLING && motionPosition >= 0) {
                // User clicked on an actual view (and was not stopping a fling).
                // Remember where the motion event started
                v = getChildAt(motionPosition - mFirstPosition);
                mMotionViewOriginalTop = v.getTop();
                mMotionX = x;
                mMotionY = y;
                mMotionPosition = motionPosition;
                mTouchMode = TOUCH_MODE_DOWN;
                clearScrollingCache();
            }
            mLastY = Integer.MIN_VALUE;
            if (touchMode == TOUCH_MODE_FLING) {
                return true;
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            switch (mTouchMode) {
            case TOUCH_MODE_DOWN:
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final int y = (int) ev.getY(pointerIndex);
                if (startScrollIfNeeded(y - mMotionY)) {
                    return true;
                }
                break;
            }
            break;
        }

        case MotionEvent.ACTION_UP: {
            mTouchMode = TOUCH_MODE_REST;
            mActivePointerId = INVALID_POINTER;
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            break;
        }
        
        case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(ev);
            break;
        }
        }

        return false;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mMotionX = (int) ev.getX(newPointerIndex);
            mMotionY = (int) ev.getY(newPointerIndex);
            mMotionCorrection = 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTouchables(ArrayList<View> views) {
        final int count = getChildCount();
        final int firstPosition = mFirstPosition;
        final ListAdapter adapter = mAdapter;

        if (adapter == null) {
            return;
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (adapter.isEnabled(firstPosition + i)) {
                views.add(child);
            }
            child.addTouchables(views);
        }
    }

    /**
     * Fires an "on scroll state changed" event to the registered
     * {@link android.widget.AbsListView.OnScrollListener}, if any. The state change
     * is fired only if the specified state is different from the previously known state.
     *
     * @param newState The new scroll state.
     */
    void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(this, newState);
                mLastScrollState = newState;
            }
        }
    }

    /**
     * Responsible for fling behavior. Use {@link #start(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}.
     * A FlingRunnable will keep re-posting itself until the fling is done.
     *
     */
    private class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private final OverScroller mScroller;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;

        FlingRunnable() {
            mScroller = new OverScroller(getContext());
        }

        void start(int initialVelocity) {
            initialVelocity = modifyFlingInitialVelocity(initialVelocity);

            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            mScroller.fling(0, initialY, 0, initialVelocity,
                    0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            if (DEBUG)
                Log.d(TAG, String.format("String Fling: [%d, %d] to [%d]", initialY, initialVelocity, mScroller.getFinalY()));

            mTouchMode = TOUCH_MODE_FLING;
            post(this);

            if (PROFILE_FLINGING) {
                if (!mFlingProfilingStarted) {
                    Debug.startMethodTracing("AbsListViewFling");
                    mFlingProfilingStarted = true;
                }
            }
        }

        void startSpringback() {
            if (mScroller.springBack(0, getScrollY(), 0, 0, 0, 0)) {
                mTouchMode = TOUCH_MODE_OVERFLING;
                invalidate();
                post(this);
            } else {
                mTouchMode = TOUCH_MODE_REST;
            }
        }

        void startOverfling(int initialVelocity) {
            final int min = getScrollY() > 0 ? Integer.MIN_VALUE : 0;
            final int max = getScrollY() > 0 ? 0 : Integer.MAX_VALUE;
            mScroller.fling(0, getScrollY(), 0, initialVelocity, 0, 0, min, max, 0, getHeight());
            mTouchMode = TOUCH_MODE_OVERFLING;
            invalidate();
            post(this);
        }
        
        
        void edgeReached(int delta) {
            mScroller.notifyVerticalEdgeReached(getScrollY(), 0, mOverflingDistance);
            final int overscrollMode = getOverScrollMode();
            if (overscrollMode == OVER_SCROLL_ALWAYS ||
                    (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
                mTouchMode = TOUCH_MODE_OVERFLING;
                final int vel = (int) mScroller.getCurrVelocity();
                if (delta > 0) {
                    if (null != mEdgeGlowTop) mEdgeGlowTop.onAbsorb(vel);
                } else {
                    if (null != mEdgeGlowBottom) mEdgeGlowBottom.onAbsorb(vel);
                }
            }
            invalidate();
            post(this);
        }

        void startScroll(int distance, int duration) {
            int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            mScroller.startScroll(0, initialY, 0, distance, duration);
            mTouchMode = TOUCH_MODE_FLING;
            post(this);
        }
        
        private void endFling() {
            mLastFlingY = 0;
            mTouchMode = TOUCH_MODE_REST;

            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            clearScrollingCache();

            removeCallbacks(this);

            if (mPositionScroller != null) {
                removeCallbacks(mPositionScroller);
            }
            mScroller.forceFinished(true);
        }

        public void run() {
            switch (mTouchMode) {
            default:
                return;
                
            case TOUCH_MODE_FLING: {
                if (mItemCount == 0 || getChildCount() == 0) {
                    endFling();
                    return;
                }

                final OverScroller scroller = mScroller;
                boolean more = scroller.computeScrollOffset();
                final int y = scroller.getCurrY();

                // Flip sign to convert finger direction to list items direction
                // (e.g. finger moving down means list is moving towards the top)
                int delta = mLastFlingY - y;

                // Pretend that each frame of a fling scroll is a touch scroll
                if (delta > 0) {
                    // List is moving towards the top. Use first view as mMotionPosition
                    mMotionPosition = mFirstPosition;
                    // final View firstView = getChildAt(0);
                    // mMotionViewOriginalTop = firstView.getTop();
                    mMotionViewOriginalTop = getScrollChildTop();

                    // Don't fling more than 1 screen
                    // delta = Math.min(getHeight() - mPaddingBottom -
                    // mPaddingTop - 1, delta);
                    delta = Math.min(getHeight() - getPaddingBottom() - getPaddingTop() - 1, delta);
                } else {
                    // List is moving towards the bottom. Use last view as mMotionPosition
                    int offsetToLast = getChildCount() - 1;
                    mMotionPosition = mFirstPosition + offsetToLast;

                    // final View lastView = getChildAt(offsetToLast);
                    // mMotionViewOriginalTop = lastView.getTop();
                    mMotionViewOriginalTop = getScrollChildTop();//getScrollChildBottom();

                    // Don't fling more than 1 screen
                    // delta = Math.max(-(getHeight() - mPaddingBottom -
                    // mPaddingTop - 1), delta);
                    delta = Math.max(-(getHeight() - getPaddingBottom() - getPaddingTop() - 1), delta);
                }

                // Check to see if we have bumped into the scroll limit
                View motionView = getChildAt(mMotionPosition - mFirstPosition);
                int oldTop = 0;
                if (motionView != null) {
                    oldTop = motionView.getTop();
                }

                final boolean atEnd = trackMotionScroll(delta, delta);
                if (atEnd) {
                    if (motionView != null) {
                        // Tweak the scroll for how far we overshot
                        int overshoot = -(delta - (motionView.getTop() - oldTop));
                        if (hasGingerbread()) {
                            overScrollBy(0, overshoot, 0, getScrollY(), 0, 0,
                                    0, mOverflingDistance, false);
                        }
                    }
                    if (more) {
                        edgeReached(delta);
                    }
                    break;
                }

                if (more && !atEnd) {
                    invalidate();
                    mLastFlingY = y;
                    post(this);
                } else {
                    endFling();
                    
                    if (PROFILE_FLINGING) {
                        if (mFlingProfilingStarted) {
                            Debug.stopMethodTracing();
                            mFlingProfilingStarted = false;
                        }
                    }
                }
                break;
            }

            case TOUCH_MODE_OVERFLING: {
                final OverScroller scroller = mScroller;
                if (scroller.computeScrollOffset()) {
                    final int scrollY = getScrollY();
                    final int deltaY = scroller.getCurrY() - scrollY;
                    boolean scroll = hasGingerbread() ? overScrollBy(0, deltaY, 0, scrollY, 0, 0,
                                0, mOverflingDistance, false) : false;
                    if (scroll) {
                        startSpringback();
                    } else {
                        invalidate();
                        post(this);
                    }
                } else {
                    endFling();
                }
                break;
            }
            }

        }
    }
    
    
    class PositionScroller implements Runnable {
        private static final int SCROLL_DURATION = 400;
        
        private static final int MOVE_DOWN_POS = 1;
        private static final int MOVE_UP_POS = 2;
        private static final int MOVE_DOWN_BOUND = 3;
        private static final int MOVE_UP_BOUND = 4;
        
        private int mMode;
        private int mTargetPos;
        private int mBoundPos;
        private int mLastSeenPos;
        private int mScrollDuration;
        private final int mExtraScroll;
        
        PositionScroller() {
            mExtraScroll = ViewConfiguration.get(getContext()).getScaledFadingEdgeLength();
        }
        
        void start(int position) {
            final int firstPos = mFirstPosition;
            final int lastPos = firstPos + getChildCount() - 1;
            
            int viewTravelCount = 0;
            if (position <= firstPos) {                
                viewTravelCount = firstPos - position + 1;
                mMode = MOVE_UP_POS;
            } else if (position >= lastPos) {
                viewTravelCount = position - lastPos + 1;
                mMode = MOVE_DOWN_POS;
            } else {
                // Already on screen, nothing to do
                return;
            }
            
            if (viewTravelCount > 0) {
                mScrollDuration = SCROLL_DURATION / viewTravelCount;
            } else {
                mScrollDuration = SCROLL_DURATION;
            }
            mTargetPos = position;
            mBoundPos = INVALID_POSITION;
            mLastSeenPos = INVALID_POSITION;
            
            post(this);
        }
        
        void start(int position, int boundPosition) {
            if (boundPosition == INVALID_POSITION) {
                start(position);
                return;
            }
            
            final int firstPos = mFirstPosition;
            final int lastPos = firstPos + getChildCount() - 1;
            
            int viewTravelCount = 0;
            if (position <= firstPos) {
                final int boundPosFromLast = lastPos - boundPosition;
                if (boundPosFromLast < 1) {
                    // Moving would shift our bound position off the screen. Abort.
                    return;
                }
                
                final int posTravel = firstPos - position + 1;
                final int boundTravel = boundPosFromLast - 1;
                if (boundTravel < posTravel) {
                    viewTravelCount = boundTravel;
                    mMode = MOVE_UP_BOUND;
                } else {
                    viewTravelCount = posTravel;
                    mMode = MOVE_UP_POS;
                }
            } else if (position >= lastPos) {
                final int boundPosFromFirst = boundPosition - firstPos;
                if (boundPosFromFirst < 1) {
                    // Moving would shift our bound position off the screen. Abort.
                    return;
                }

                final int posTravel = position - lastPos + 1;
                final int boundTravel = boundPosFromFirst - 1;
                if (boundTravel < posTravel) {
                    viewTravelCount = boundTravel;
                    mMode = MOVE_DOWN_BOUND;
                } else {
                    viewTravelCount = posTravel;
                    mMode = MOVE_DOWN_POS;
                }
            } else {
                // Already on screen, nothing to do
                return;
            }
            
            if (viewTravelCount > 0) {
                mScrollDuration = SCROLL_DURATION / viewTravelCount;
            } else {
                mScrollDuration = SCROLL_DURATION;
            }
            mTargetPos = position;
            mBoundPos = boundPosition;
            mLastSeenPos = INVALID_POSITION;
            
            post(this);
        }
        
        void stop() {
            removeCallbacks(this);
        }
        
        public void run() {
            final int listHeight = getHeight();
            final int firstPos = mFirstPosition;
            
            switch (mMode) {
            case MOVE_DOWN_POS: {
                final int lastViewIndex = getChildCount() - 1;
                final int lastPos = firstPos + lastViewIndex;
                
                if (lastViewIndex < 0) {
                    return;
                }

                if (lastPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    post(this);
                    return;
                }

                final View lastView = getChildAt(lastViewIndex);
                final int lastViewHeight = lastView.getHeight();
                final int lastViewTop = lastView.getTop();
                final int lastViewPixelsShowing = listHeight - lastViewTop;
                final int extraScroll = lastPos < mItemCount - 1 ? mExtraScroll : mListPadding.bottom;

                smoothScrollBy(lastViewHeight - lastViewPixelsShowing + extraScroll,
                        mScrollDuration);

                mLastSeenPos = lastPos;
                if (lastPos < mTargetPos) {
                    post(this);
                }
                break;
            }
                
            case MOVE_DOWN_BOUND: {
                final int nextViewIndex = 1;
                final int childCount = getChildCount();
                
                if (firstPos == mBoundPos || childCount <= nextViewIndex
                        || firstPos + childCount >= mItemCount) {
                    return;
                }
                final int nextPos = firstPos + nextViewIndex;

                if (nextPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    post(this);
                    return;
                }

                final View nextView = getChildAt(nextViewIndex);
                final int nextViewHeight = nextView.getHeight();
                final int nextViewTop = nextView.getTop();
                final int extraScroll = mExtraScroll;
                if (nextPos < mBoundPos) {
                    smoothScrollBy(Math.max(0, nextViewHeight + nextViewTop - extraScroll),
                            mScrollDuration);

                    mLastSeenPos = nextPos;

                    post(this);
                } else  {
                    if (nextViewTop > extraScroll) { 
                        smoothScrollBy(nextViewTop - extraScroll, mScrollDuration);
                    }
                }
                break;
            }
                
            case MOVE_UP_POS: {
                if (firstPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    post(this);
                    return;
                }

                final View firstView = getChildAt(0);
                if (firstView == null) {
                    return;
                }
                final int firstViewTop = firstView.getTop();
                final int extraScroll = firstPos > 0 ? mExtraScroll : mListPadding.top;

                smoothScrollBy(firstViewTop - extraScroll, mScrollDuration);

                mLastSeenPos = firstPos;

                if (firstPos > mTargetPos) {
                    post(this);
                }
                break;
            }
                
            case MOVE_UP_BOUND: {
                final int lastViewIndex = getChildCount() - 2;
                if (lastViewIndex < 0) {
                    return;
                }
                final int lastPos = firstPos + lastViewIndex;

                if (lastPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    post(this);
                    return;
                }

                final View lastView = getChildAt(lastViewIndex);
                final int lastViewHeight = lastView.getHeight();
                final int lastViewTop = lastView.getTop();
                final int lastViewPixelsShowing = listHeight - lastViewTop;
                mLastSeenPos = lastPos;
                if (lastPos > mBoundPos) {
                    smoothScrollBy(-(lastViewPixelsShowing - mExtraScroll), mScrollDuration);
                    post(this);
                } else {
                    final int bottom = listHeight - mExtraScroll;
                    final int lastViewBottom = lastViewTop + lastViewHeight;
                    if (bottom > lastViewBottom) {
                        smoothScrollBy(-(bottom - lastViewBottom), mScrollDuration);
                    }
                }
                break;
            }

            default:
                break;
            }
        }
    }
    
    /**
     * Smoothly scroll to the specified adapter position. The view will
     * scroll such that the indicated position is displayed.
     * @param position Scroll to this adapter position.
     */
    public void smoothScrollToPosition(int position) {
        if (mPositionScroller == null) {
            mPositionScroller = new PositionScroller();
        }
        mPositionScroller.start(position);
    }
    
    /**
     * Smoothly scroll to the specified adapter position. The view will
     * scroll such that the indicated position is displayed, but it will
     * stop early if scrolling further would scroll boundPosition out of
     * view. 
     * @param position Scroll to this adapter position.
     * @param boundPosition Do not scroll if it would move this adapter
     *          position out of view.
     */
    public void smoothScrollToPosition(int position, int boundPosition) {
        if (mPositionScroller == null) {
            mPositionScroller = new PositionScroller();
        }
        mPositionScroller.start(position, boundPosition);
    }
    
    /**
     * Smoothly scroll by distance pixels over duration milliseconds.
     * @param distance Distance to scroll in pixels.
     * @param duration Duration of the scroll animation in milliseconds.
     */
    public void smoothScrollBy(int distance, int duration) {
        if (mFlingRunnable == null) {
            mFlingRunnable = new FlingRunnable();
        } else {
            mFlingRunnable.endFling();
        }
        mFlingRunnable.startScroll(distance, duration);
    }

    private void createScrollingCache() {
        if (mScrollingCacheEnabled && !mCachingStarted) {
            setChildrenDrawnWithCacheEnabled(true);
            setChildrenDrawingCacheEnabled(true);
            mCachingStarted = true;
        }
    }

    private void clearScrollingCache() {
        if (mClearScrollingCache == null) {
            mClearScrollingCache = new Runnable() {
                public void run() {
                    if (mCachingStarted) {
                        mCachingStarted = false;
                        setChildrenDrawnWithCacheEnabled(false);
                        final int mPersistentDrawingCache = getPersistentDrawingCache();
                        if ((mPersistentDrawingCache & PERSISTENT_SCROLLING_CACHE) == 0) {
                            setChildrenDrawingCacheEnabled(false);
                        }
                        if (!isAlwaysDrawnWithCacheEnabled()) {
                            invalidate();
                        }
                    }
                }
            };
        }
        post(mClearScrollingCache);
    }

    /**
     * Track a motion scroll
     *
     * @param deltaY Amount to offset mMotionView. This is the accumulated delta since the motion
     *        began. Positive numbers mean the user's finger is moving down the screen.
     * @param incrementalDeltaY Change in deltaY from the previous event.
     * @return true if we're already at the beginning/end of the list and have nothing to do.
     */
    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }

        final int firstTop = getScrollChildTop(); // check scroll.
        final int lastBottom = getScrollChildBottom(); // check scroll.
        final Rect listPadding = mListPadding;

        // FIXME account for grid vertical spacing too?
        final int end = getHeight() - listPadding.bottom;
        final int spaceAbove = listPadding.top - getFillChildTop(); // check
                                                                    // load more
        final int spaceBelow = getFillChildBottom() - end; // check load more

        // final int height = getHeight() - mPaddingBottom - mPaddingTop;
        final int height = getHeight() - getPaddingBottom() - getPaddingTop();
        if (deltaY < 0) {
            deltaY = Math.max(-(height - 1), deltaY);
        } else {
            deltaY = Math.min(height - 1, deltaY);
        }

        if (incrementalDeltaY < 0) {
            incrementalDeltaY = Math.max(-(height - 1), incrementalDeltaY);
        } else {
            incrementalDeltaY = Math.min(height - 1, incrementalDeltaY);
        }

        final int firstPosition = mFirstPosition;
        // Update our guesses for where the first and last views are
        if (firstPosition == 0) {
            mFirstPositionDistanceGuess = firstTop - mListPadding.top;
        } else {
            mFirstPositionDistanceGuess += incrementalDeltaY;
        }
        
        if (firstPosition + childCount == mItemCount) {
            mLastPositionDistanceGuess = lastBottom + mListPadding.bottom;
        } else {
            mLastPositionDistanceGuess += incrementalDeltaY;
        }
        
        //Log.i(TAG, " -------trackMotionScroll firstPosition = " + firstPosition + "    firstTop = " + firstTop + "   deltaY = " + deltaY + "     listPadding.top = " + listPadding.top);
        if (firstPosition == 0 && firstTop >= listPadding.top && deltaY >= 0) {
            // Don't need to move views down if the top of the first position
            // is already visible
            //Log.e(TAG, " -------trackMotionScroll   do NOT move down... ");
            return true;
        }

        if (firstPosition + childCount == mItemCount && lastBottom <= end && deltaY <= 0) {
            // Don't need to move views up if the bottom of the last position
            // is already visible
            //Log.e(TAG, " -------trackMotionScroll   do NOT move up... ");
            return true;
        }

        final boolean down = incrementalDeltaY < 0;

        final int headerViewsCount = getHeaderViewsCount();
        final int footerViewsStart = mItemCount - getFooterViewsCount();

        int start = 0;
        int count = 0;

        if (down) {
            final int top = listPadding.top - incrementalDeltaY;
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if (child.getBottom() >= top) {
                    break;
                } else {
                    count++;
                    int position = firstPosition + i;
                    if (position >= headerViewsCount && position < footerViewsStart) {
                        mRecycler.addScrapView(child);

                        if (ViewDebug.TRACE_RECYCLER) {
                            ViewDebug.trace(child, ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP, firstPosition + i, -1);
                        }
                    }
                }
            }
        } else {
            final int bottom = getHeight() - listPadding.bottom - incrementalDeltaY;
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getTop() <= bottom) {
                    break;
                } else {
                    start = i;
                    count++;
                    int position = firstPosition + i;
                    if (position >= headerViewsCount && position < footerViewsStart) {
                        mRecycler.addScrapView(child);

                        if (ViewDebug.TRACE_RECYCLER) {
                            ViewDebug.trace(child, ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP, firstPosition + i, -1);
                        }
                    }
                }
            }
        }

        mMotionViewNewTop = mMotionViewOriginalTop + deltaY;

        mBlockLayoutRequests = true;

        if (count > 0) {
            detachViewsFromParent(start, count);
        }

        // offsetChildrenTopAndBottom(incrementalDeltaY);
        tryOffsetChildrenTopAndBottom(incrementalDeltaY);

        if (down) {
            mFirstPosition += count;
        }

        invalidate();

        final int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
        if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
            fillGap(down);
        }

        mBlockLayoutRequests = false;
        invokeOnItemScrollListener();
        awakenScrollBars();

        return false;
    }

    protected void tryOffsetChildrenTopAndBottom(int offset) {
        if (DEBUG)
            Log.v(TAG, "tryOffsetChilderenTopAndBottom: " + offset);
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View v = getChildAt(i);
            v.offsetTopAndBottom(offset);
        }
    }
    /**
     * Returns the number of header views in the list. Header views are special views
     * at the top of the list that should not be recycled during a layout.
     *
     * @return The number of header views, 0 in the default implementation.
     */
    int getHeaderViewsCount() {
        return 0;
    }

    /**
     * Returns the number of footer views in the list. Footer views are special views
     * at the bottom of the list that should not be recycled during a layout.
     *
     * @return The number of footer views, 0 in the default implementation.
     */
    int getFooterViewsCount() {
        return 0;
    }

    void hideSelector() {
        /*
        if (mSelectedPosition != INVALID_POSITION) {
            if (mLayoutMode != LAYOUT_SPECIFIC) {
                mResurrectToPosition = mSelectedPosition;
            }
            if (mNextSelectedPosition >= 0 && mNextSelectedPosition != mSelectedPosition) {
                mResurrectToPosition = mNextSelectedPosition;
            }
            setSelectedPositionInt(INVALID_POSITION);
            setNextSelectedPositionInt(INVALID_POSITION);
            mSelectedTop = 0;
            mSelectorRect.setEmpty();
        }
        */
        mSelectedTop = 0;
        mSelectorRect.setEmpty();
        invalidate();
    }
    
    /**
     * Fills the gap left open by a touch-scroll. During a touch scroll,
     * children that remain on screen are shifted and the other ones are
     * discarded. The role of this method is to fill the gap thus created by
     * performing a partial layout in the empty space.
     * 
     * @param down
     *            true if the scroll is going down, false if it is going up
     */
    abstract void fillGap(boolean down);

    /**
     * Find the row closest to y. This row will be used as the motion row when scrolling
     *
     * @param y Where the user touched
     * @return The position of the first (or only) item in the row containing y
     */
    abstract int findMotionRow(int y);
    
    /**
     * Find the row closest to y. This row will be used as the motion row when scrolling.
     * 
     * @param y Where the user touched
     * @return The position of the first (or only) item in the row closest to y
     */
    int findClosestMotionRow(int y) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return INVALID_POSITION;
        }
        
        final int motionRow = findMotionRow(y);
        return motionRow != INVALID_POSITION ? motionRow : mFirstPosition + childCount - 1;
    }

    /**
     * Causes all the views to be rebuilt and redrawn.
     */
    public void invalidateViews() {
        mDataChanged = true;
        rememberSyncState();
        requestLayout();
        invalidate();
    }

    @Override
    protected void handleDataChanged() {
        int count = mItemCount;
        if (count > 0) {

            int newPos;

            int selectablePos;

            // Find the row we are supposed to sync to
            if (mNeedSync) {
                // Update this first, since setNextSelectedPositionInt inspects it
                mNeedSync = false;
                if (mTranscriptMode == TRANSCRIPT_MODE_ALWAYS_SCROLL
                        || (mTranscriptMode == TRANSCRIPT_MODE_NORMAL && mFirstPosition + getChildCount() >= mOldItemCount)) {
                    mLayoutMode = LAYOUT_FORCE_BOTTOM;
                    return;
                }

                switch (mSyncMode) {
                case SYNC_FIRST_POSITION:
                    // Leave mSyncPosition as it is -- just pin to available range
                    mLayoutMode = LAYOUT_SYNC;
                    mSyncPosition = Math.min(Math.max(0, mSyncPosition), count - 1);

                    return;
                }
            }

            if (!isInTouchMode()) {
                // We couldn't find matching data -- try to use the same position
                newPos = getSelectedItemPosition();

                // Pin position to the available range
                if (newPos >= count) {
                    newPos = count - 1;
                }
                if (newPos < 0) {
                    newPos = 0;
                }

                // Make sure we select something selectable -- first look down
                selectablePos = lookForSelectablePosition(newPos, true);

                // Looking down didn't work -- try looking up
                selectablePos = lookForSelectablePosition(newPos, false);
                if (selectablePos >= 0) {
                    return;
                }
            } else {

                // We already know where we want to resurrect the selection
                if (mResurrectToPosition >= 0) {
                    return;
                }
            }
        }

        // Nothing is selected. Give up and reset everything.
        mLayoutMode = mStackFromBottom ? LAYOUT_FORCE_BOTTOM : LAYOUT_FORCE_TOP;
        mNeedSync = false;
    }

    /**
     * adapter data is changed.. should keep current view layout information..
     * 
     * @param mSyncPosition
     */
    protected void onLayoutSync(int syncPosition) {
    }

    /**
     * adapter data is changed.. children layout manipulation is finished.
     * 
     * @param mSyncPosition
     */
    protected void onLayoutSyncFinished(int syncPosition) {
    }

    /**
     * What is the distance between the source and destination rectangles given
     * the direction of focus navigation between them? The direction basically
     * helps figure out more quickly what is self evident by the relationship
     * between the rects...
     * 
     * @param source
     *            the source rectangle
     * @param dest
     *            the destination rectangle
     * @param direction
     *            the direction
     * @return the distance between the rectangles
     */
    static int getDistance(Rect source, Rect dest, int direction) {
        int sX, sY; // source x, y
        int dX, dY; // dest x, y
        switch (direction) {
        case View.FOCUS_RIGHT:
            sX = source.right;
            sY = source.top + source.height() / 2;
            dX = dest.left;
            dY = dest.top + dest.height() / 2;
            break;
        case View.FOCUS_DOWN:
            sX = source.left + source.width() / 2;
            sY = source.bottom;
            dX = dest.left + dest.width() / 2;
            dY = dest.top;
            break;
        case View.FOCUS_LEFT:
            sX = source.left;
            sY = source.top + source.height() / 2;
            dX = dest.right;
            dY = dest.top + dest.height() / 2;
            break;
        case View.FOCUS_UP:
            sX = source.left + source.width() / 2;
            sY = source.top;
            dX = dest.left + dest.width() / 2;
            dY = dest.bottom;
            break;
        default:
            throw new IllegalArgumentException("direction must be one of "
                    + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return deltaY * deltaY + deltaX * deltaX;
    }

    @Override
    public void onGlobalLayout() {
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new BdAbsListView.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof BdAbsListView.LayoutParams;
    }
    
    private void finishGlows() {
        if (mEdgeGlowTop != null) {
            mEdgeGlowTop.finish();
        }
        
        if (mEdgeGlowBottom != null) {
            mEdgeGlowBottom.finish();
        }
    }

    /**
     * Puts the list or grid into transcript mode. In this mode the list or grid will always scroll
     * to the bottom to show new items.
     *
     * @param mode the transcript mode to set
     *
     * @see #TRANSCRIPT_MODE_DISABLED
     * @see #TRANSCRIPT_MODE_NORMAL
     * @see #TRANSCRIPT_MODE_ALWAYS_SCROLL
     */
    public void setTranscriptMode(int mode) {
        mTranscriptMode = mode;
    }

    /**
     * Returns the current transcript mode.
     *
     * @return {@link #TRANSCRIPT_MODE_DISABLED}, {@link #TRANSCRIPT_MODE_NORMAL} or
     *         {@link #TRANSCRIPT_MODE_ALWAYS_SCROLL}
     */
    public int getTranscriptMode() {
        return mTranscriptMode;
    }

    @Override
    public int getSolidColor() {
        return mCacheColorHint;
    }

    /**
     * When set to a non-zero value, the cache color hint indicates that this list is always drawn
     * on top of a solid, single-color, opaque background
     *
     * @param color The background color
     */
    public void setCacheColorHint(int color) {
        if (color != mCacheColorHint) {
            mCacheColorHint = color;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setDrawingCacheBackgroundColor(color);
            }
            mRecycler.setCacheColorHint(color);
        }
    }

    /**
     * When set to a non-zero value, the cache color hint indicates that this list is always drawn
     * on top of a solid, single-color, opaque background
     *
     * @return The cache color hint
     */
    public int getCacheColorHint() {
        return mCacheColorHint;
    }

    /**
     * Move all views (excluding headers and footers) held by this AbsListView into the supplied
     * List. This includes views displayed on the screen as well as views stored in AbsListView's
     * internal view recycler.
     *
     * @param views A list into which to put the reclaimed views
     */
    public void reclaimViews(List<View> views) {
        int childCount = getChildCount();
        RecyclerListener listener = mRecycler.mRecyclerListener;

        // Reclaim views on screen
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            BdAbsListView.LayoutParams lp = (BdAbsListView.LayoutParams) child.getLayoutParams();
            // Don't reclaim header or footer views, or views that should be ignored
            if (lp != null && mRecycler.shouldRecycleViewType(lp.viewType)) {
                views.add(child);
                if (listener != null) {
                    // Pretend they went through the scrap heap
                    listener.onMovedToScrapHeap(child);
                }
            }
        }
        mRecycler.reclaimScrapViews(views);
        removeAllViewsInLayout();
    }

    // TODO I'm not sure the purpose of onConsistencyCheck mehtod. it looks like
    // debug related.. so just comment out.
    // /**
    // * @hide
    // */
    // @Override
    // protected boolean onConsistencyCheck(int consistency) {
    // boolean result = super.onConsistencyCheck(consistency);
    //
    // final boolean checkLayout = (consistency & ViewDebug.CONSISTENCY_LAYOUT)
    // != 0;
    //
    // if (checkLayout) {
    // // The active recycler must be empty
    // final View[] activeViews = mRecycler.mActiveViews;
    // int count = activeViews.length;
    // for (int i = 0; i < count; i++) {
    // if (activeViews[i] != null) {
    // result = false;
    // Log.d("ViewDebug",
    // "AbsListView " + this + " has a view in its active recycler: " +
    // activeViews[i]);
    // }
    // }
    //
    // // All views in the recycler must NOT be on screen and must NOT have a
    // parent
    // final ArrayList<View> scrap = mRecycler.mCurrentScrap;
    // if (!checkScrap(scrap)) result = false;
    // final ArrayList<View>[] scraps = mRecycler.mScrapViews;
    // count = scraps.length;
    // for (int i = 0; i < count; i++) {
    // if (!checkScrap(scraps[i])) result = false;
    // }
    // }
    //
    // return result;
    // }

    // private boolean checkScrap(ArrayList<View> scrap) {
    // if (scrap == null) return true;
    // boolean result = true;
    //
    // final int count = scrap.size();
    // for (int i = 0; i < count; i++) {
    // final View view = scrap.get(i);
    // if (view.getParent() != null) {
    // result = false;
    // Log.d("ViewDebug", "AbsListView " + this +
    // " has a view in its scrap heap still attached to a parent: " + view);
    // }
    // if (indexOfChild(view) >= 0) {
    // result = false;
    // Log.d("ViewDebug", "AbsListView " + this +
    // " has a view in its scrap heap that is also a direct child: " + view);
    // }
    // }
    //
    // return result;
    // }


    /**
     * Sets the recycler listener to be notified whenever a View is set aside in
     * the recycler for later reuse. This listener can be used to free resources
     * associated to the View.
     *
     * @param listener The recycler listener to be notified of views set aside
     *        in the recycler.
     *
     * @see com.lee.sdk.widget.staggered.BdAbsListView.RecycleBin
     * @see android.widget.AbsListView.RecyclerListener
     */
    public void setRecyclerListener(RecyclerListener listener) {
        mRecycler.mRecyclerListener = listener;
    }

    /**
     * AbsListView extends LayoutParams to provide a place to hold the view type.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        /**
         * View type for this view, as returned by
         * {@link android.widget.Adapter#getItemViewType(int) }
         */
        @ViewDebug.ExportedProperty(mapping = { @ViewDebug.IntToString(from = ITEM_VIEW_TYPE_IGNORE, to = "ITEM_VIEW_TYPE_IGNORE"),
                @ViewDebug.IntToString(from = ITEM_VIEW_TYPE_HEADER_OR_FOOTER, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER") })
        public int viewType;

        /**
         * When this boolean is set, the view has been added to the AbsListView
         * at least once. It is used to know whether headers/footers have already
         * been added to the list view and whether they should be treated as
         * recycled views or not.
         */
        @ViewDebug.ExportedProperty
        public boolean recycledHeaderFooter;

        /**
         * When an AbsListView is measured with an AT_MOST measure spec, it needs
         * to obtain children views to measure itself. When doing so, the children
         * are not attached to the window, but put in the recycler which assumes
         * they've been attached before. Setting this flag will force the reused
         * view to be attached to the window rather than just attached to the
         * parent.
         */
        @ViewDebug.ExportedProperty
        public boolean forceAdd;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /**
     * A RecyclerListener is used to receive a notification whenever a View is placed
     * inside the RecycleBin's scrap heap. This listener is used to free resources
     * associated to Views placed in the RecycleBin.
     *
     * @see android.widget.AbsListView.RecycleBin
     * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
     */
    public static interface RecyclerListener {
        /**
         * Indicates that the specified View was moved into the recycler's scrap heap.
         * The view is not displayed on screen any more and any expensive resource
         * associated with the view should be discarded.
         *
         * @param view
         */
        void onMovedToScrapHeap(View view);
    }

    /**
     * The RecycleBin facilitates reuse of views across layouts. The RecycleBin has two levels of
     * storage: ActiveViews and ScrapViews. ActiveViews are those views which were onscreen at the
     * start of a layout. By construction, they are displaying current information. At the end of
     * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews are old views that
     * could potentially be used by the adapter to avoid allocating views unnecessarily.
     *
     * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
     * @see android.widget.AbsListView.RecyclerListener
     */
    class RecycleBin {
        private RecyclerListener mRecyclerListener;

        /**
         * The position of the first view stored in mActiveViews.
         */
        private int mFirstActivePosition;

        /**
         * Views that were on screen at the start of layout. This array is populated at the start of
         * layout, and at the end of layout all view in mActiveViews are moved to mScrapViews.
         * Views in mActiveViews represent a contiguous range of Views, with position of the first
         * view store in mFirstActivePosition.
         */
        private View[] mActiveViews = new View[0];

        /**
         * Unsorted views that can be used by the adapter as a convert view.
         */
        private ArrayList<View>[] mScrapViews;

        private int mViewTypeCount;

        private ArrayList<View> mCurrentScrap;

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            //noinspection unchecked
            @SuppressWarnings("unchecked")
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList<View>();
            }
            mViewTypeCount = viewTypeCount;
            mCurrentScrap = scrapViews[0];
            mScrapViews = scrapViews;
        }
        
        public void markChildrenDirty() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).forceLayout();
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).forceLayout();
                    }
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        /**
         * Clears the scrap heap.
         */
        void clear() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        removeDetachedView(scrap.remove(scrapCount - 1 - j), false);
                    }
                }
            }
        }

        /**
         * Fill ActiveViews with all of the children of the AbsListView.
         *
         * @param childCount The minimum number of views mActiveViews should hold
         * @param firstActivePosition The position of the first view that will be stored in
         *        mActiveViews
         */
        void fillActiveViews(int childCount, int firstActivePosition) {
            if (mActiveViews.length < childCount) {
                mActiveViews = new View[childCount];
            }
            mFirstActivePosition = firstActivePosition;

            final View[] activeViews = mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                BdAbsListView.LayoutParams lp = (BdAbsListView.LayoutParams) child.getLayoutParams();
                // Don't put header or footer views into the scrap heap
                if (lp != null && lp.viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
                    // Note:  We do place BdAdapterView.ITEM_VIEW_TYPE_IGNORE in active views.
                    //        However, we will NOT place them into scrap views.
                    activeViews[i] = child;
                }
            }
        }

        /**
         * Get the view corresponding to the specified position. The view will be removed from
         * mActiveViews if it is found.
         *
         * @param position The position to look up in mActiveViews
         * @return The view if it is found, null otherwise
         */
        View getActiveView(int position) {
            int index = position - mFirstActivePosition;
            final View[] activeViews = mActiveViews;
            if (index >=0 && index < activeViews.length) {
                final View match = activeViews[index];
                activeViews[index] = null;
                return match;
            }
            return null;
        }

        /**
         * @return A view from the ScrapViews collection. These are unordered.
         */
        View getScrapView(int position) {
            ArrayList<View> scrapViews;
            if (mViewTypeCount == 1) {
                scrapViews = mCurrentScrap;
                int size = scrapViews.size();
                if (size > 0) {
                    return scrapViews.remove(size - 1);
                } else {
                    return null;
                }
            } else {
                int whichScrap = mAdapter.getItemViewType(position);
                if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
                    scrapViews = mScrapViews[whichScrap];
                    int size = scrapViews.size();
                    if (size > 0) {
                        return scrapViews.remove(size - 1);
                    }
                }
            }
            return null;
        }

        /**
         * Put a view into the ScapViews list. These views are unordered.
         *
         * @param scrap The view to add
         */
        void addScrapView(View scrap) {
            BdAbsListView.LayoutParams lp = (BdAbsListView.LayoutParams) scrap.getLayoutParams();
            if (lp == null) {
                return;
            }

            // Don't put header or footer views or views that should be ignored
            // into the scrap heap
            int viewType = lp.viewType;
            if (!shouldRecycleViewType(viewType)) {
                if (viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
                    removeDetachedView(scrap, false);
                }
                return;
            }

            if (mViewTypeCount == 1) {
                // scrap.dispatchStartTemporaryDetach();
                dispatchFinishTemporaryDetach(scrap);
                mCurrentScrap.add(scrap);
            } else {
                // scrap.dispatchStartTemporaryDetach();
                dispatchFinishTemporaryDetach(scrap);
                mScrapViews[viewType].add(scrap);
            }

            if (mRecyclerListener != null) {
                mRecyclerListener.onMovedToScrapHeap(scrap);
            }
        }

        /**
         * Move all views remaining in mActiveViews to mScrapViews.
         */
        void scrapActiveViews() {
            final View[] activeViews = mActiveViews;
            final boolean hasListener = mRecyclerListener != null;
            final boolean multipleScraps = mViewTypeCount > 1;

            ArrayList<View> scrapViews = mCurrentScrap;
            final int count = activeViews.length;
            for (int i = count - 1; i >= 0; i--) {
                final View victim = activeViews[i];
                if (victim != null) {
                    int whichScrap = ((BdAbsListView.LayoutParams) victim.getLayoutParams()).viewType;

                    activeViews[i] = null;

                    if (!shouldRecycleViewType(whichScrap)) {
                        // Do not move views that should be ignored
                        if (whichScrap != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
                            removeDetachedView(victim, false);
                        }
                        continue;
                    }

                    if (multipleScraps) {
                        scrapViews = mScrapViews[whichScrap];
                    }
                    // victim.dispatchStartTemporaryDetach();
                    dispatchFinishTemporaryDetach(victim);
                    scrapViews.add(victim);

                    if (hasListener) {
                        mRecyclerListener.onMovedToScrapHeap(victim);
                    }

                    if (ViewDebug.TRACE_RECYCLER) {
                        ViewDebug.trace(victim,
                                ViewDebug.RecyclerTraceType.MOVE_FROM_ACTIVE_TO_SCRAP_HEAP,
                                mFirstActivePosition + i, -1);
                    }
                }
            }

            pruneScrapViews();
        }

        /**
         * Makes sure that the size of mScrapViews does not exceed the size of mActiveViews.
         * (This can happen if an adapter does not recycle its views).
         */
        private void pruneScrapViews() {
            final int maxViews = mActiveViews.length;
            final int viewTypeCount = mViewTypeCount;
            final ArrayList<View>[] scrapViews = mScrapViews;
            for (int i = 0; i < viewTypeCount; ++i) {
                final ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                final int extras = size - maxViews;
                size--;
                for (int j = 0; j < extras; j++) {
                    removeDetachedView(scrapPile.remove(size--), false);
                }
            }
        }

        /**
         * Puts all views in the scrap heap into the supplied list.
         */
        void reclaimScrapViews(List<View> views) {
            if (mViewTypeCount == 1) {
                views.addAll(mCurrentScrap);
            } else {
                final int viewTypeCount = mViewTypeCount;
                final ArrayList<View>[] scrapViews = mScrapViews;
                for (int i = 0; i < viewTypeCount; ++i) {
                    final ArrayList<View> scrapPile = scrapViews[i];
                    views.addAll(scrapPile);
                }
            }
        }

        /**
         * Updates the cache color hint of all known views.
         *
         * @param color The new cache color hint.
         */
        void setCacheColorHint(int color) {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).setDrawingCacheBackgroundColor(color);
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(i).setDrawingCacheBackgroundColor(color);
                    }
                }
            }
            // Just in case this is called during a layout pass
            final View[] activeViews = mActiveViews;
            final int count = activeViews.length;
            for (int i = 0; i < count; ++i) {
                final View victim = activeViews[i];
                if (victim != null) {
                    victim.setDrawingCacheBackgroundColor(color);
                }
            }
        }
    }

    // TODO: new added methods.
    // ///////////////////////////////////////////////////
    // Newly Added Methods.
    // ///////////////////////////////////////////////////

    private void dispatchFinishTemporaryDetach(View v) {
        if (v == null)
            return;

        v.onFinishTemporaryDetach();
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                dispatchFinishTemporaryDetach(group.getChildAt(i));
            }
        }
    }

    // ///////////////////////////////////////////////////
    // Check available space of list view.
    // ///////////////////////////////////////////////////
    
    /**
     * 设置top光晕效果可用还是不可用
     * 
     * @param enabled
     */
    public void setEdgeGlowTopEnabled(boolean enabled) {
        if (enabled) {
            if (null == mEdgeGlowTop) {
                mEdgeGlowTop = new EdgeEffect(getContext());
            }
        } else {
            mEdgeGlowTop = null;
        }
    }

    /**
     * 设置bottom光晕效果可用还是不可用
     * 
     * @param enabled
     */
    public void setEdgeGlowBottompEnabled(boolean enabled) {
        if (enabled) {
            if (null == mEdgeGlowBottom) {
                mEdgeGlowBottom = new EdgeEffect(getContext());
            }
        } else {
            mEdgeGlowBottom = null;
        }
    }
    
    /**
     * 这个方法可以更改fling的速度
     * 
     * @param initialVelocity
     * @return
     */
    protected int modifyFlingInitialVelocity(int initialVelocity) {
        return initialVelocity;
    }

    /**
     * used in order to determine fill list more or not.
     * 
     * @return
     */
    protected int getScrollChildTop() {
        final int count = getChildCount();
        if (count == 0)
            return 0;
        return getChildAt(0).getTop();
    }

    /**
     * 
     * @return
     */
    protected int getScrollChildBottom() {
        final int count = getChildCount();
        if (count == 0)
            return 0;
        return getChildAt(count - 1).getBottom();
    }
    
    protected int getFirstChildTop() {
        final int count = getChildCount();
        if (count == 0)
            return 0;
        return getChildAt(0).getTop();
    }

    /**
     * 得到填充child最下面，默认返回最后一个child的bottom，如果要支持多列的话，派生类需要重
     * 写这个方法，从而返回所有列中最下面的位
     * @return
     */
    protected int getFillChildBottom() {
        final int count = getChildCount();
        if (count == 0) {
            return 0;
        }
        
        return getChildAt(count - 1).getBottom();
    }
    
    /**
     * 得到填充child最上面，默认返回第一个child的top，如果要支持多列的话，派生类需要重
     * 写这个方法，从而返回所有列中最上面的位
     * @return
     */
    protected int getFillChildTop() {
        final int count = getChildCount();
        if (count == 0) {
            return 0;
        }
        
        return getChildAt(0).getTop();
    }

    public static boolean hasEclairMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1;
    }
    
    /**
     * If platform is Froyo (level 8) or above.
     * @return If platform SDK is above Froyo
     */
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * If platform is Gingerbread (level 9) or above.
     * @return If platform SDK is above Gingerbread
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }
    
    // Added by LiHong at 2013/08/01  begin =========
    /**
     * Set the selector padding
     * 
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setSelectorPadding(int left, int top, int right, int bottom) {
        mSelectionLeftPadding = left;
        mSelectionTopPadding = top;
        mSelectionRightPadding = right;
        mSelectionBottomPadding = bottom;
    }
    
    /**
     * @hide
     * Returns the current velocity.
     *
     * @return The original velocity less the deceleration, norm of the X and Y velocity vector.
     */
    public float getCurrVelocity() {
        if (null != mFlingRunnable) {
            OverScroller scroller = mFlingRunnable.mScroller;
            if (null != scroller) {
                return scroller.getCurrVelocity();
            }
        }
        
        return 0;
    }
    
    /**
     * 停止滚动
     */
    public void endFling() {
        if (null != mFlingRunnable) {
            mFlingRunnable.endFling();
        }
    }
    // Added by Li Hong at 2013/08/01  end ===========
}// end of class
//CHECKSTYLE:ON
