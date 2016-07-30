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

package com.lee.sdk.widget.viewpager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lee.sdk.R;
import com.lee.sdk.widget.AdapterLinearLayout;

/**
 * 这个类扩展于{@link HorizontalScrollView}类，可以左右滑动，它内部封装了{@link AdapterLinearLayout}
 * 类，提供Adapter来设置其要显示的Item，它的使用方法与{@link GridView}和{@link ListView}的用法一样，都
 * 必须调用{@link #setAdapter(Adapter)}方法来绑定数据与View。
 * 
 * <p>
 * 这个类已经默认提供了文本形式的菜单项，文字为黑色，选中时为红色，在使用时，
 * 只需要调用{@link #addTab(BdPagerTab)}方法来添加tab项，
 * 如果默认的tab项的样式无法满足需求，可以调用{@link #setAdapter(Adapter)}来指定显示不同形式的tab项。
 * </p>
 * 
 * @author LiHong
 * @since 2013-11-11
 */
public class BdPagerTabBar extends HorizontalScrollView {

    /**
     * 当Tab选中时的事件监听接口
     * 
     * @author LiHong
     * @since 2013-11-12
     */
    public interface OnTabSelectedListener {
        /**
         * Signals that the given news category was selected.
         * 
         * @param pagerBar PagerBar instance.
         * @param index the selected category's index.
         */
        /*public*/ void onTabSelected(BdPagerTabBar pagerBar,  int index);
    }
    
    /**
     * LayoutParams对象，所有Adapter中的View都接收{@link AdapterLinearLayout.LayoutParams}这样类型的参数。
     * 
     * @author LiHong
     * @since 2013-11-11
     */
    public static class LayoutParams extends AdapterLinearLayout.LayoutParams {
        /**
         * 构造方法
         * 
         * @param width 宽
         * @param height 高
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
    
    /** 左右边缘的阴影色 */
    private static final int[] SHADOWS_COLORS =  { 
        0x99999999,
        0x00AAAAAA, 
        0x00AAAAAA
    };
    
    /** 左右边缘的阴影宽度 */
    private static final int SHADOWS_WIDTH = 45;
    /** 阴影是否禁用 */
    private boolean mShadowsEnable = false;
    /** 当前AdapterLinearLayout对象 */
    private AdapterLinearLayout mAdapterLayout = null;
    /** 左边的阴影  */
    private Drawable mLeftShadow = null;
    /** 右边的阴影 */
    private Drawable mRightShadow = null;
    /** Tab选中的listener */
    private OnTabSelectedListener mOnTabSelectedListener = null;
    /** Adapter */
    private Adapter mAdapter = null;
    /** Tab文字色 */
    private int mTabTextColor = -1;
    /** Tab选中文字色 */
    private int mTabSelTextColor = -1;
    /** Tab文字色State list */
    private ColorStateList mColorStateList = null;
    /** Tab文字字体大小 */
    private int mTabTextSize = -1;
    /** Tab项的背景图片 */
    private int mTabBackgroundId = 0;
    /** Min width */
    private int mMinTabWidth = 50; // SUPPRESS CHECKSTYLE
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public BdPagerTabBar(Context context) {
        this(context, null);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public BdPagerTabBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public BdPagerTabBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        // 最小的宽度
        mMinTabWidth = (int) (context.getResources().getDisplayMetrics().density * mMinTabWidth);
        init(context);
    }
    
    /**
     * 设置tab的最小宽度
     * 
     * @param minTabWidth 最小宽度
     */
    public void setTabMinWidth(int minTabWidth) {
        mMinTabWidth = minTabWidth;
    }
    
    /**
     * 初始化
     * 
     * @param context context
     */
    private void init(Context context) {
        mAdapterLayout = new AdapterLinearLayout(context);
        mAdapterLayout.setGravity(Gravity.CENTER);
        mAdapterLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        setAdapter(new TabAdapter(getContext()));
        
        FrameLayout.LayoutParams parmas = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mAdapterLayout, parmas);
        
        mLeftShadow = new GradientDrawable(Orientation.LEFT_RIGHT, SHADOWS_COLORS);
        mRightShadow = new GradientDrawable(Orientation.RIGHT_LEFT, SHADOWS_COLORS);
        
        // 让LinearLayout填充满当前视图
        setFillViewport(true);
        setTabTextSize((int) getResources().getDimension(R.dimen.pager_tab_item_textsize));
    }
    
    /**
     * 设置字体大小
     * 
     * @param textSize textSize
     */
    public void setTabTextSize(int textSize) {
        mTabTextSize = textSize;
    }
    
    /**
     * Tab文字色的状态变化
     * 
     * @param colorStateList colorStateList
     */
    public void setTabTextColor(ColorStateList colorStateList) {
        mColorStateList = colorStateList;
    }
    
    /**
     * 设置Tab文字的颜色
     * 
     * @param textColor 文本色
     * @param selTextColor 选中的色
     */
    public void setTabTextColor(int textColor, int selTextColor) {
        mTabTextColor = textColor;
        mTabSelTextColor = selTextColor;
    }
    
    /**
     * 设置tab项的背景图片
     * 
     * @param resId resId
     */
    public void setTabBackground(int resId) {
        mTabBackgroundId = resId;
    }

    /**
     * 设置Tab选中的listener
     * 
     * @param listener OnTabSelectedListener对象
     */
    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        mOnTabSelectedListener = listener;
        
        mAdapterLayout.setOnItemClickListener(new AdapterLinearLayout.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterLinearLayout adapterView, View view, int position) {
                if (null != mOnTabSelectedListener) {
                    if (mAdapterLayout.getSelectedPosition() != position) {
                        mOnTabSelectedListener.onTabSelected(BdPagerTabBar.this, position);
                    }
                }
            }
        });
    }
    
    /**
     * 设置Adapter来绑定数据，类似于{@link GridView}或{@link ListView}
     * 
     * @param adapter adapter对象
     */
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mAdapterLayout.setAdapter(adapter);
    }
    
    /**
     * 得到当前设置的Adapter对象
     * 
     * @return Adapter对象
     */
    public Adapter getAdapter() {
        return mAdapterLayout.getAdapter();
    }
    
    /**
     * 得到当前选中的索引
     * 
     * @return 选中tab的索引
     */
    public int getSelectedIndex() {
        return mAdapterLayout.getSelectedPosition();
    }
    
    /**
     * 选中某一tab
     * 
     * @param index 选中的索引
     */
    public void selectTab(final int index) {
        if (null != mAdapterLayout) {
            mAdapterLayout.selectChild(index);
        }
    }
    
    /**
     * 设置tab的间隔
     * 
     * @param space tab问题
     */
    public void setTabSpace(int space) {
        if (null != mAdapterLayout) {
            mAdapterLayout.setSpace(space);
        }
    }
    
    /**
     * 设置间隔图片
     * 
     * @param divider 问题图片
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setDividerDrawable(Drawable divider) {
        if (null != mAdapterLayout) {
            mAdapterLayout.setDividerDrawable(divider);
        }
    }
    
    /**
     * 设置分隔线的宽度
     * 
     * @param width 宽度
     */
    public void setDividerWidth(int width) {
        if (null != mAdapterLayout) {
            mAdapterLayout.setDividerSize(width);
        }
    }
    
    /**
     * 添加item，添加的item会平均分配当前action bar的宽度
     * 
     * @param tab ActionItem对象
     */
    public void addTab(BdPagerTab tab) {
        if (null != tab) {
            tab.setTextSize((int) getResources().getDimension(R.dimen.pager_tab_item_textsize));
            Adapter adapter = getAdapter();
            if (adapter instanceof TabAdapter) {
                ((TabAdapter) adapter).addTab(tab);
            }
        }
    }
    
    /**
     * 添加tab
     * 
     * @param tabs tabs
     */
    public void addTabs(List<BdPagerTab> tabs) {
        if (null != tabs) {
            Adapter adapter = getAdapter();
            if (adapter instanceof TabAdapter) {
                ((TabAdapter) adapter).addTabs(tabs);
            }
        }
    }
    
    /**
     * 删除所有的tab
     */
    public void removeAllTabs() {
        Adapter adapter = getAdapter();
        if (adapter instanceof TabAdapter) {
            ((TabAdapter) adapter).removeAllTabs();
        }
    }
    
    /**
     * 得点到点的Tab信息
     * 
     * @param index 索引
     * @return BdPagerTab对象
     */
    public BdPagerTab getTabAt(int index) {
        int count = getTabCount();
        if (index >= 0 && index < count) {
            if (null != mAdapter) {
                return (BdPagerTab) mAdapter.getItem(index);
            }
        }
        
        return null;
    }
    
    /**
     * 得到当前Tab的个数
     * 
     * @return tab的个数
     */
    public int getTabCount() {
        return (null != mAdapter) ? mAdapter.getCount() : 0;
    }
    
    /**
     * 更新Tabs
     */
    public void updateTabs() {
        post(new Runnable() {
            @Override
            public void run() {
                Adapter adapter = getAdapter();
                // 更新tab的UI设置
                if (adapter instanceof TabAdapter) {
                    TabAdapter tabAdapter = (TabAdapter) adapter;
                    ArrayList<BdPagerTab> tabs = tabAdapter.mTabs;
                    if (null != tabs) {
                        for (BdPagerTab tab : tabs) {
                            tab.setColorStateList(mColorStateList);
                            tab.setTextColor(mTabTextColor);
                            tab.setSelTextColor(mTabSelTextColor);
                            tab.setTextSize(mTabTextSize);
                            tab.setTabBackgroundResId(mTabBackgroundId);
                        }
                    }
                    
                    tabAdapter.setWidthParams(mMinTabWidth, getWidth());
                    tabAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    /**
     * 设置阴影可用或禁用
     * 
     * @param enable 是否可用，true可用，false禁用
     */
    public void setShadowsEnabled(boolean enable) {
        mShadowsEnable = enable;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        float density = getResources().getDisplayMetrics().density;
        int width = (int) (SHADOWS_WIDTH * density);
        mLeftShadow.setBounds(0, 0, width, h);
        mRightShadow.setBounds(w - width, 0, w, h);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        
        // 绘制阴影
        if (mShadowsEnable) {
            drawShadows(canvas);
        }
    }
    
    /**
     * 绘制阴影效果
     * 
     * @param canvas 画布
     */
    private void drawShadows(Canvas canvas) {
        int count = getChildCount();
        if (0 == count) {
            return;
        }
        
        int width   = getWidth();
        int scrollX = this.getScrollX();
        int total = mAdapterLayout.getMeasuredWidth();
        
        boolean drawLeft  = (scrollX > 0);
        boolean drawRight = (total > width && (width + scrollX) < total);
        if (!drawLeft && !drawRight) {
            return;
        }
        
        canvas.save();
        canvas.translate(scrollX, 0);
        if (drawLeft) {
            mLeftShadow.draw(canvas);
        }
        
        if (drawRight) {
            mRightShadow.draw(canvas);
        }
        
        canvas.restore();
    }
    
    /**
     * 默认的tab的Adapter.
     * 
     * <p>注意：
     * 派生类可以重写{@link #onCreateView(Context, ViewGroup)}来创建不同的View。
     * 
     * @author LiHong
     * @since 2013-11-12
     */
    public static class TabAdapter extends BaseAdapter {
        /** Tabs */
        ArrayList<BdPagerTab> mTabs = new ArrayList<BdPagerTab>();
        /** Context */
        Context mContext;
        /** 最小的tab的宽度 */
        int mMinTabWidth;
        /** 最大的tab的宽度 */
        int mMaxTabWidth;
        /** Tab宽度 */
        int mTabWidth;
        
        /**
         * 构造方法
         * 
         * @param context context
         */
        public TabAdapter(Context context) {
            mContext = context;
        }
        
        /**
         * 设置一此宽度的参数
         * 
         * @param minTabWidth minTabWidth
         * @param containerWidth containerWidth
         */
        public void setWidthParams(int minTabWidth, int containerWidth) {
            mMinTabWidth = minTabWidth;

            if (0 == containerWidth) {
                mTabWidth = 0;
                return;
            }
            
            int count = getCount();
            if (0 != count) {
                int width = containerWidth / count;
                if (width < minTabWidth) {
                    mTabWidth = minTabWidth;
                    mMaxTabWidth = mTabWidth;
                } else {
                    mTabWidth = 0;
                    mMaxTabWidth = width;
                }
            }
        }
        
        /**
         * 添加item，添加的item会平均分配当前action bar的宽度
         * 
         * @param tab ActionItem对象
         */
        public void addTab(BdPagerTab tab) {
            mTabs.add(tab);
        }
        
        /**
         * 添加tab
         * 
         * @param tabs tabs
         */
        public void addTabs(List<BdPagerTab> tabs) {
            if (null != tabs) {
                mTabs.addAll(tabs);
            }
        }
        
        /**
         * 删除所有的tab
         */
        public void removeAllTabs() {
            mTabs.clear();
        }
        
        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Object getItem(int position) {
            return mTabs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BdPagerTab tab = mTabs.get(position);
            
            if (null == convertView) {
                BdPagerTabBar.LayoutParams params = new BdPagerTabBar.LayoutParams(
                        mTabWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                if (0 == mTabWidth) {
                    params.weight = 1;
                }
                convertView = onCreateView(mContext, parent);
                convertView.setLayoutParams(params);
                
                final int resId = tab.getTabBackgroundResId();
                if (0 != resId) {
                    convertView.setBackgroundResource(resId);
                }
            } else {
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                if (null != params) {
                    params.width = mTabWidth;
                    if (0 == mTabWidth && params instanceof BdPagerTabBar.LayoutParams) {
                        ((BdPagerTabBar.LayoutParams) params).weight = 1;
                    }
                }
            }
            
            onConfigConvertView(mContext, position, convertView);
            
            return convertView;
        }
        
        /**
         * 创建显示在界面中的View，这个方法会在{@link TabAdapter#getView(int, View, ViewGroup)}中调用。这个方法
         * 默认返回的View对象的类型是{@link PagerTabBarItem}。
         * 
         * @param context context
         * @param parent parent
         * @return not null view
         */
        protected View onCreateView(Context context, ViewGroup parent) {
            return new PagerTabBarItem(context, mMinTabWidth, mMaxTabWidth);
        }
        
        /**
         * 为指定的{@link convertView}绑定数据，如果派生类重写了{@link #onConfigConvertView(Context, int, View)}方法的话，
         * 通常情况下，需要同时重写这个方法。该方法内部默认是将convertView强转成{@link #PagerTabBarItem}类型后，再绑定数据。
         * 
         * @param context context
         * @param position 数据的索引
         * @param convertView 旧的view
         */
        protected void onConfigConvertView(Context context, int position, View convertView) {
            BdPagerTab tab = mTabs.get(position);
            PagerTabBarItem item = (PagerTabBarItem) convertView;
            item.setMinWidth(mMinTabWidth);
            item.setMaxWidth(mMaxTabWidth);
            item.setBdPagerTab(tab);
        }
    }
    
    /**
     * 扩展于TextView
     * 
     * @author LiHong
     * @since 2013-11-11
     */
    public static class PagerTabBarItem extends TextView {
        /**文字色*/ 
        private int mTextColor = -1;
        /**选中色*/
        private int mSelTextColor = -1;
        
        /**
         * 构造方法
         * 
         * @param context context
         * @param minWidth minWidth
         * @param maxWidth maxWidth
         */
        public PagerTabBarItem(Context context, int minWidth, int maxWidth) {
            super(context);
            init(context);
            
            this.setMinWidth(minWidth);
            this.setMaxWidth(maxWidth);
        }
        
        /**
         * 初始化
         * 
         * @param context context
         */
        private void init(Context context) {
            this.setGravity(Gravity.CENTER);
            this.setSingleLine(true);
            this.setEllipsize(TruncateAt.END);
        }
        
        /**
         * 设置tab数据
         * 
         * @param tab tab
         */
        public void setBdPagerTab(BdPagerTab tab) {
            setText(tab.getTitle());
            setTextSize(TypedValue.COMPLEX_UNIT_PX, tab.getTextSize());
            
            // 设置Color，优先使用ColorStateList
            ColorStateList colorList = tab.getColorStateList();
            if (null != colorList) {
                setTextColor(colorList);
                setTextColor(-1, -1);
            } else {
                setTextColor(tab.getTextColor(), tab.getSelTextColor());
            }
        }
        
        /**
         * 设置文本色
         * 
         * @param color 正常色
         * @param selColor 选中的色
         */
        private void setTextColor(int color, int selColor) {
            mTextColor = color;
            mSelTextColor = selColor;
        }
        
        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            // 当选中时，显示选中的颜色
            if (-1 != mSelTextColor && -1 != mTextColor) {
                setTextColor(selected ? mSelTextColor : mTextColor);
            }
            
            invalidate();
        }
    }
}
