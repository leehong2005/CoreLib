package com.lee.sdk.test.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

public class TosScrollBar extends TosScrollBarDrawable {
    
    public interface OnScrollListener {
        public void onScrolling(TosScrollBar scrollBar);
    }
    
    public static final int STATE_NONE       = 0;
    public static final int STATE_FADING     = 1;
    public static final int STATE_ON         = 2;
    public static final int STATE_OFF        = 3;
    public static final int STATE_DRAGGING   = 4;
    
    private int         mScrollState         = STATE_ON;
    private AbsListView mListView            = null;
    private ScrollView  mScrollView          = null;
    private GestureDetector mGestureDetector = null; 
    private OnScrollListener mListener       = null;
    
    public TosScrollBar(Context context, AbsListView listView) {
        mListView = listView;
        
        initialize(context);
    }
    
    public TosScrollBar(Context context, ScrollView scrollView) {
        mScrollView = scrollView;
        
        initialize(context);
    }
    
    private void initialize(Context context) {
        // NOTE: If the list view is in the dialog, the ListView#setSelection method may
        // not work well, so we add View#requestFocusFromTouch method before calling
        // ListView#setSelection method, if so, this will lead the list view item has
        // focus, the child will presents the focus state, this effect is not expected, 
        // so we add these line to solve this problem.
        if (null != mListView) {
            mListView.setFocusable(false);
        }
        
        if (null != mScrollView) {
            mScrollView.setFocusable(false);
        }
        
        mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                return TosScrollBar.this.onScroll(e2, distanceX, distanceY);
            }
            
            public boolean onDown(MotionEvent e) {
                return TosScrollBar.this.onDown(e);
            }
        });
    }
    
    public void setOnScrollListener(OnScrollListener listener) {
        mListener = listener;
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        
        boolean handled = mGestureDetector.onTouchEvent(event);
        
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            // Helper method for lifted finger
            onUp(event);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            //onUp(event);
        }
        
        return handled;
    }
    
    private void setState(int state) {
        mScrollState = state;
        
        if (STATE_DRAGGING == state) {
            setPressed(true);
        }
        else {
            setPressed(false);
        }
    }
    
    /**
     * Cancel the fling action.
     */
    private void cancelFling() {
        if (null != mListView) {
            MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            mListView.onTouchEvent(cancelFling);
            cancelFling.recycle();
        }
        
        if (null != mScrollView) {
            MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            mScrollView.onTouchEvent(cancelFling);
            cancelFling.recycle();
        }
    }
    
    private void scrollScrollView(int dx, int dy, boolean smoothScroll) {
        if (null == mScrollView) {
            return;
        }
        
        final Rect thumbBounds = mThumbBounds;
        final Rect bounds = getBounds();
        int size = bounds.height();
        int length = thumbBounds.height();
        //int thumbOffset = (thumbBounds.top - mOffset) + dy;
        int thumbOffset = dy;
        int range = mRange;
        int extent = mExtent;
        int newOffset = 0;
        
        if (size != length) {
            newOffset = Math.round(thumbOffset * (range - extent) / (size - length));
        }

        if (smoothScroll) {
            mScrollView.smoothScrollBy(0, newOffset);
        }
        else {
            mScrollView.scrollBy(0, newOffset);
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    private void scrollListView(int dy, boolean isDown)
    {
        if (null == mListView) {
            return;
        }
        
        final Rect thumbBounds = mThumbBounds;
        final Rect bounds = getBounds();
        int size = bounds.height();
        int length = thumbBounds.height();
        //int thumbOffset = (thumbBounds.top - mOffset) + dy;
        int thumbOffset = dy;
        int range = mRange;
        int extent = mExtent;
        int newOffset = 0;
        
        if (size != length) {
            newOffset = Math.round(thumbOffset * (range - extent) / (size - length));
            newOffset += mOffset;
        }
        
        int h = 0;
        int t = 0;
        int newPos = 0;
        View v = mListView.getChildAt(0);
        if (null != v) {
            h = v.getHeight();
            t = v.getTop();
            if (h > 0) {
                newPos = ((int)(newOffset * h + t * 100) / (100 * h));
            }
        }

        // Send the dragging message to the handler to notify somebody the scroll bar is scrolling. 
        
        // NOTE: If the list view is in dialog, the ListView#setSelection method may be
        // work well, so we add the View#requestFocusFromTouch before calling setSelection.
        mListView.requestFocusFromTouch();
        
        int position = newPos + (isDown ? 2 : -2);
        position = Math.max(position, 0);
        
        mListView.setSelection(position);
    }
    
    private void setPressed(boolean pressed) {
    }
    
    private boolean onDown(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY() + mOffset;
        
        boolean isPtInThumb = mThumbBounds.contains(x, y);
        boolean isPtInTrack = mTrackBounds.contains(x, y);
        
        Log.d("leehong2", "onDown----- m_isPtInTrack = " + isPtInTrack);
        
        if (isPtInThumb) {
            setState(STATE_DRAGGING);
            cancelFling();
        }
        
        if (isPtInTrack) {
            if (!isPtInThumb) {
                if (null != mScrollView) {
                    int thumbTop = mThumbBounds.top;
                    int size = mThumbBounds.height();
                    int dy = (y - thumbTop) - size / 2;
                    scrollScrollView(0, dy, true);
                }
                
                if (null != mListView) {
                    
                }
            }
        }
        
        return isPtInTrack;
    }
    
    private boolean onScroll(MotionEvent event, float distanceX, float distanceY) {
        
        if (STATE_DRAGGING == mScrollState) {
            
            // Send the dragging message to the handler to notify somebody the scroll bar is scrolling.
            if (null != mListener) {
                mListener.onScrolling(this);
            }
            
            float deltaY = distanceY;
            if (Math.abs(deltaY) > 1.0f) {
                if (null != mScrollView) {
                    scrollScrollView(0, -(int)deltaY, false);
                }
                
                if (null != mListView) {
                    //boolean isDown = (distanceY < 0);
                    //scrollListView(-(int)deltaY, isDown);
                }
            }
        }
        
        return false;
    }
    
    private boolean onUp(MotionEvent event) {
        setState(STATE_ON);
        return false;
    }
}
