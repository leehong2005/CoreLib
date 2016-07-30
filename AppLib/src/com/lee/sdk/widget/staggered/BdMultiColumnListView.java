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

package com.lee.sdk.widget.staggered;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;

import com.lee.sdk.R;
import com.lee.sdk.utils.FPSUtil;
//CHECKSTYLE:OFF
/**
 * 这个类支持瀑布流的显示
 * 
 * @author Li Hong
 * 
 * @since 2013-07-26
 */
public class BdMultiColumnListView extends BdListView {

    @SuppressWarnings("unused")
    private static final String TAG = "MultiColumnListView";

    private static final int DEFAULT_COLUMN_NUMBER = 2;

    private int mColumnNumber = 2;
    private Column[] mColumns = null;
    private Column mFixedColumn = null; // column for footers & headers.
    private SparseIntArray mItems = new SparseIntArray();

    private int mColumnPaddingLeft = 0;
    private int mColumnPaddingRight = 0;
    private int mHorizontalSpacing = 0;
    private int mVerticalSpacing = 0;
    private int mColumnWidth;
    
    public BdMultiColumnListView(Context context) {
        super(context);
        init(null);
    }

    public BdMultiColumnListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BdMultiColumnListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private Rect mFrameRect = new Rect();

    private void init(AttributeSet attrs) {
        getWindowVisibleDisplayFrame(mFrameRect);

        if (attrs == null) {
            mColumnNumber = (DEFAULT_COLUMN_NUMBER); // default column number is
                                                     // 2.
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BdAdapterView);

            int landColNumber = a.getInteger(R.styleable.BdAdapterView_plaLandscapeColumnNumber, 3);
            int defColNumber = a.getInteger(R.styleable.BdAdapterView_plaColumnNumber, 2);

            if (mFrameRect.width() > mFrameRect.height() && landColNumber != -1) {
                mColumnNumber = (landColNumber);
            } else if (defColNumber != -1) {
                mColumnNumber = (defColNumber);
            } else {
                mColumnNumber = (DEFAULT_COLUMN_NUMBER);
            }

            mColumnPaddingLeft = a.getDimensionPixelSize(R.styleable.BdAdapterView_plaColumnPaddingLeft, 0);
            mColumnPaddingRight = a.getDimensionPixelSize(R.styleable.BdAdapterView_plaColumnPaddingRight, 0);
            a.recycle();
        }

        // 初始化列
        initColumn();

        mFixedColumn = new FixedColumn();
    }
    
    private void initColumn() {
        mColumns = new Column[getColumnNumber()];
        for (int i = 0; i < getColumnNumber(); ++i) {
            mColumns[i] = new Column(i);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Override Methods...
    // /////////////////////////////////////////////////////////////////////

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // TODO the adapter status may be changed. what should i do here...
    }

    /**
     * 得到列宽
     * 
     * @return 列宽
     */
    public int getColumnWidth() {
        return mColumnWidth;
    }
    
    /**
     * 设置水平间隔
     * 
     * @param horizontalSpacing 水平间隔
     */
    public void setHorizontalSpacing(int horizontalSpacing) {
        if (mHorizontalSpacing != horizontalSpacing) {
            mHorizontalSpacing = horizontalSpacing;
            
            mItems.clear();
            requestLayoutIfNecessary();
        }
    }
    
    /**
     * 设置垂直间隔
     * 
     * @param verticalSpacing 垂直间隔
     */
    public void setVerticalSpacing(int verticalSpacing) {
        if (mVerticalSpacing != verticalSpacing) {
            mVerticalSpacing = verticalSpacing;
            
            mItems.clear();
            requestLayoutIfNecessary();
        }
    }
    
    @Override
    @Deprecated
    public void setDividerHeight(int height) {
        // 为了兼容ListView的divider height，我们在多列的情况下，不再使用divider height了，
        // 而是使用vertical spacing，所以在这里需要把divider height设置为0，并把这个值设置
        // 为vertical spacing的值。
        super.setDividerHeight(0);
        
        setVerticalSpacing(height);
    }
    
    /**
     * 设置列的左右padding
     * 
     * @param left 左padding
     * @param right 右padding
     */
    public void setColumnPadding(int left, int right) {
        boolean request = (mColumnPaddingLeft != left || mColumnPaddingRight != right);
        
        mColumnPaddingLeft = left;
        mColumnPaddingRight = right;
        
        if (request) {
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mColumnWidth = (getMeasuredWidth() - mListPadding.left - mListPadding.right - 
                mColumnPaddingLeft - mColumnPaddingRight - mHorizontalSpacing)
                / getColumnNumber();

        for (int index = 0; index < getColumnNumber(); ++index) {
            mColumns[index].mColumnWidth = mColumnWidth;
            mColumns[index].mColumnLeft = mListPadding.left + mColumnPaddingLeft +
                    (mColumnWidth + mHorizontalSpacing) * index;
        }

        mFixedColumn.mColumnLeft = mListPadding.left;
        mFixedColumn.mColumnWidth = getMeasuredWidth();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (DEBUG) {
            FPSUtil.trackFPS(this);
        }
    }

    @Override
    protected void onMeasureChild(View child, int position, int widthMeasureSpec, int heightMeasureSpec) {
        if (isFixedView(child))
            child.measure(widthMeasureSpec, heightMeasureSpec);
        else
            child.measure(MeasureSpec.EXACTLY | getColumnWidth(position), heightMeasureSpec);
    }

    @Override
    protected int modifyFlingInitialVelocity(int initialVelocity) {
        //return initialVelocity / getColumnNumber();
        return initialVelocity;
    }

    @Override
    protected void onItemAddedToList(int position, boolean flow) {
        super.onItemAddedToList(position, flow);

        if (isHeaderOrFooterPosition(position) == false) {
            Column col = getNextColumn(flow, position);
            mItems.append(position, col.getIndex());
        }
    }

    @Override
    protected void onLayoutSync(int syncPos) {
        for (Column c : mColumns) {
            c.save();
        }
    }

    @Override
    protected void onLayoutSyncFinished(int syncPos) {
        for (Column c : mColumns) {
            c.clear();
        }
    }

    @Override
    protected void onAdjustChildViews(boolean down) {

        int firstItem = getFirstVisiblePosition();
        if (down == false && firstItem == 0) {
            final int firstColumnTop = mColumns[0].getTop();
            for (Column c : mColumns) {
                final int top = c.getTop();
                // align all column's top to 0's column.
                c.offsetTopAndBottom(firstColumnTop - top);
            }
        }
        super.onAdjustChildViews(down);
    }

    @Override
    protected int getFillChildBottom() {
        // return smallest bottom value.
        // in order to determine fill down or not... (calculate below space)
        int result = Integer.MAX_VALUE;
        for (Column c : mColumns) {
            int bottom = c.getBottom();
            result = result > bottom ? bottom : result;
        }
        return result;
    }

    @Override
    protected int getFillChildTop() {
        // find largest column.
        int result = Integer.MIN_VALUE;
        for (Column c : mColumns) {
            int top = c.getTop();
            result = result < top ? top : result;
        }
        return result;
    }

    @Override
    protected int getScrollChildBottom() {
        // return largest bottom value.
        // for checking scrolling region...
        int result = Integer.MIN_VALUE;
        for (Column c : mColumns) {
            int bottom = c.getBottom();
            result = result < bottom ? bottom : result;
        }
        
        return result;
    }

    @Override
    protected int getScrollChildTop() {
        // 修正一个BUG，如果只有一个child时，同时这个child的高度超过屏幕高度，
        // 在这种情况下，可以无限向下滑动child，而且松手后，不能自动滑回去。
        if (1 == getChildCount()) {
            return super.getScrollChildTop();
        }
        
        // find largest column.
        int result = Integer.MAX_VALUE;
        for (Column c : mColumns) {
            int top = c.getTop();
            result = result > top ? top : result;
        }
        
        return result;
    }

    @Override
    protected int getItemLeft(int pos) {

        if (isHeaderOrFooterPosition(pos))
            return mFixedColumn.getColumnLeft();

        return getColumnLeft(pos);
    }

    @Override
    protected int getItemTop(int pos) {

        if (isHeaderOrFooterPosition(pos))
            return mFixedColumn.getBottom(); // footer view should be placed
                                             // below the last column.

        int bottom = 0;
        int colIndex = mItems.get(pos, -1);
        if (colIndex == -1) {
            bottom = getFillChildBottom() + mVerticalSpacing;
        } else {
            bottom = mColumns[colIndex].getBottom();
            if (bottom != mColumns[colIndex].mSynchedBottom) {
                bottom += mVerticalSpacing;
            }
        }
        
        return bottom;
    }
    
    @Override
    protected int getItemBottom(int pos) {

        if (isHeaderOrFooterPosition(pos))
            return mFixedColumn.getTop(); // header view should be place above
                                          // the first column item.

        int top = 0;
        int colIndex = mItems.get(pos, -1);
        if (colIndex == -1) {
            top = getFillChildTop() - mVerticalSpacing;
        } else {
            top = mColumns[colIndex].getTop();
            if (top != mColumns[colIndex].mSynchedTop) {
                top -= mVerticalSpacing;
            }
        }

        return top;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Private Methods...
    // ////////////////////////////////////////////////////////////////////////////

    // flow If flow is true, align top edge to y. If false, align bottom edge to
    // y.
    private Column getNextColumn(boolean flow, int position) {

        // we already have this item...
        int colIndex = mItems.get(position, -1);
        if (colIndex != -1) {
            return mColumns[colIndex];
        }

        // adjust position (exclude headers...)
        position = Math.max(0, position - getHeaderViewsCount());

        final int lastVisiblePos = Math.max(0, position);
        if (lastVisiblePos < getColumnNumber())
            return mColumns[lastVisiblePos];

        if (flow) {
            // find column which has the smallest bottom value.
            return gettBottomColumn();
        } else {
            // find column which has the smallest top value.
            return getTopColumn();
        }
    }

    private boolean isHeaderOrFooterPosition(int pos) {
        int type = mAdapter.getItemViewType(pos);
        return type == ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
    }

    private Column getTopColumn() {
        Column result = mColumns[0];
        for (Column c : mColumns) {
            result = result.getTop() > c.getTop() ? c : result;
        }
        return result;
    }

    private Column gettBottomColumn() {
        Column result = mColumns[0];
        for (Column c : mColumns) {
            result = result.getBottom() > c.getBottom() ? c : result;
        }

        if (DEBUG) {
            Log.d("Column", "get Shortest Bottom Column: " + result.getIndex());
        }
        
        return result;
    }

    private int getColumnLeft(int pos) {
        int colIndex = mItems.get(pos, -1);

        if (colIndex == -1)
            return 0;

        return mColumns[colIndex].getColumnLeft();
    }

    private int getColumnWidth(int pos) {
        int colIndex = mItems.get(pos, -1);

        if (colIndex == -1)
            return 0;

        return mColumns[colIndex].getColumnWidth();
    }

    // /////////////////////////////////////////////////////////////
    // Inner Class.
    // /////////////////////////////////////////////////////////////

    public int getColumnNumber() {
        return mColumnNumber;
    }
    
    public void setColumnNumber(int columnNumber) {
        // If the column number is zero, throw exception.
        if (0 == columnNumber) {
            throw new IllegalArgumentException("Column number can not be zero.");
        }
        
        // Default column number.
        if (columnNumber < 0) {
            columnNumber = DEFAULT_COLUMN_NUMBER;
        }
        
        if (mColumnNumber != columnNumber) {
            mColumnNumber = columnNumber;
            
            mItems.clear();
            initColumn();
            requestLayoutIfNecessary();
        }
    }
    
    /**
     * 重置position-column index的列表
     */
    public void reset() {
        mItems.clear();
        requestLayoutIfNecessary();
    }

    private class Column {

        private int mIndex;
        private int mColumnWidth;
        private int mColumnLeft;
        private int mSynchedTop = 0;
        private int mSynchedBottom = 0;

        // TODO is it ok to use item position info to identify item??

        public Column(int index) {
            mIndex = index;
        }

        public int getColumnLeft() {
            return mColumnLeft;
        }

        public int getColumnWidth() {
            return mColumnWidth;
        }

        public int getIndex() {
            return mIndex;
        }

        public int getBottom() {
            // find biggest value.
            int bottom = Integer.MIN_VALUE;
            int childCount = getChildCount();

            for (int index = 0; index < childCount; ++index) {
                View v = getChildAt(index);

                if (v.getLeft() != mColumnLeft && !isFixedView(v))
                    continue;
                
                bottom = bottom < v.getBottom() ? v.getBottom() : bottom;
            }

            if (bottom == Integer.MIN_VALUE)
                return mSynchedBottom; // no child for this column..
            
            return bottom;
        }

        public void offsetTopAndBottom(int offset) {
            if (offset == 0)
                return;

            // find biggest value.
            int childCount = getChildCount();

            for (int index = 0; index < childCount; ++index) {
                View v = getChildAt(index);
                if (v.getLeft() != mColumnLeft && !isFixedView(v))
                    continue;

                v.offsetTopAndBottom(offset);
            }
        }

        public int getTop() {
            // find smallest value.
            int top = Integer.MAX_VALUE;
            int childCount = getChildCount();
            for (int index = 0; index < childCount; ++index) {
                View v = getChildAt(index);
                if (v.getLeft() != mColumnLeft && !isFixedView(v))
                    continue;

                top = top > v.getTop() ? v.getTop() : top;
            }

            if (top == Integer.MAX_VALUE)
                return mSynchedTop; // no child for this column. just return
                                    // saved sync top.
            return top;
        }

        public void save() {
            mSynchedTop = 0;
            mSynchedBottom = getTop(); // getBottom();
        }

        public void clear() {
            mSynchedTop = 0;
            mSynchedBottom = 0;
        }
    }// end of inner class Column

    private class FixedColumn extends Column {

        public FixedColumn() {
            super(Integer.MAX_VALUE);
        }

        @Override
        public int getBottom() {
            return getScrollChildBottom();
        }

        @Override
        public int getTop() {
            return getScrollChildTop();
        }

    }// end of class

}// end of class
//CHECKSTYLE:ON
