package com.lee.sdk.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import com.lee.sdk.test.R;
import com.lee.sdk.test.widget.TosScrollBar.OnScrollListener;

public class TosScrollView extends ScrollView {
    private TosScrollBar mScrollBar = null;
    private int mScrollBarWidth = 5;
    private boolean mScrollBarDragEnable = false;

    public TosScrollView(Context context) {
        super(context);

        initialize(context);
    }

    public TosScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize(context);
    }

    public TosScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(context);
    }

    @SuppressWarnings("deprecation")
    private void initialize(Context context) {
        Drawable track = context.getResources().getDrawable(R.drawable.conf_scroll_base);
        Drawable thumb = context.getResources().getDrawable(R.drawable.conf_scroll_bar);
        mScrollBar = new TosScrollBar(context, this);
        mScrollBar.setVerticalTrackDrawable(track);
        mScrollBar.setVerticalThumbDrawable(thumb);
        mScrollBar.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolling(TosScrollBar scrollBar) {
                awakenScrollBars();
            }
        });

        this.mScrollBarWidth = ViewConfiguration.getScrollBarSize();
        // Enable vertical scroll bar.
        this.setVerticalScrollBarEnabled(true);
        // The scroll bar is always visible.
        this.setScrollbarFadingEnabled(false);
    }

    public void setScrollBarDragEnable(boolean scrollBarDragEnable) {
        mScrollBarDragEnable = scrollBarDragEnable;
    }

    public void setScrollBarWidth(int width) {
        if (mScrollBarWidth != width) {
            mScrollBarWidth = width;
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScrollBarDragEnable) {
            if (null != mScrollBar) {
                if (mScrollBar.onTouchEvent(event)) {
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        l -= mScrollBarWidth;
        super.invalidate(l, t, r, b);
    }

    void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        if (mScrollBarWidth > 0) {
            l = r - mScrollBarWidth;
        }

        if (mScrollBarDragEnable) {
            if (null != mScrollBar) {
                mScrollBar.setParameters(computeVerticalScrollRange(), computeVerticalScrollOffset(),
                        computeVerticalScrollExtent(), true);

                mScrollBar.setBounds(l, t, r, b);
                mScrollBar.draw(canvas);
            }
        } else {
            scrollBar.setBounds(l, t, r, b);
            scrollBar.draw(canvas);
        }
    }
}
