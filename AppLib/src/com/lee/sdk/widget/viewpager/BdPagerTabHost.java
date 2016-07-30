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

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;

import com.lee.sdk.R;
import com.lee.sdk.widget.viewpager.BdPagerTabBar.OnTabSelectedListener;

/**
 * 这个是一个共同的用来显示多Tab的布局，内部的tab显示这个类已经提供了默认的adapter，显示一个文本，使用都也可以调用
 * {@link #setTabAdapter(Adapter)}方法来设置tab的具体显示。
 * 
 * 这个类内部封装了ViewPager类，支持左右滑动。
 * 
 * <h2>用法如下:</h2>
 * 
 * <pre class="prettyprint">
 * // 得到BdPagerTabHost对象，可以new创建，也可从XML中加载
 * final BdPagerTabHost tabHostView = new BdPagerTabHost(this);
 * // 添加tab
 * tabHostView.addTab(new BdPagerTab().setTitle("轻应用消息"));
 * tabHostView.addTab(new BdPagerTab().setTitle("百度消息"));
 * tabHostView.selectTab(0);   // 默认第一个tab选中
 * tabHostView.setTabBarBackground(R.drawable.picture_action_bar_bg);  // 设置tab bar的背景色，通常不需要
 * tabHostView.setTabTextSize(20);  // 设置tab字体大小，这块可以不用设置，到时候统一调成默认的
 * tabHostView.setPageIndicatorDrawable(R.drawable.picture_tab_indicator); // 设置tab的indicator，通常需要设置，默认是红色的。
 * tabHostView.layoutTabs();   // 布局tab
 *       
 * FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
 *     public int getCount() {
 *         return tabHostView.getTabCount();
 *     }
 *           
 *     public Fragment getItem(int position) {
 *         // 返回相应的fragment
 *         return null;
 *     }
 * };
 * tabHostView.setPagerAdapter(adapter, 0);    // 设置adapter，默认选中第1个tab。
 * </pre>
 * 
 * @author LiHong
 * @since 2013-11-11
 */
public class BdPagerTabHost extends FrameLayout {
    /** View pager */
    private ViewPager mViewPager;
    /** 头部indicator **/
    private DrawablePageIndicator mPageIndicator;
    /** 显示tab的工具栏 */
    private BdPagerTabBar mPagerTabBar;
    /** tab切换的listener */
    private OnTabHostChangeListener mListener;

    /** 构造方法
     * 
     * @param context context
     */
    public BdPagerTabHost(Context context) {
        super(context);
        
        init(context);
    }
    
    /** 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public BdPagerTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /** 构造方法
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public BdPagerTabHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }
    
    /**
     * 初始化
     * 
     * @param context context
     */
    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.sdk_tabhost_layout, this);
        
        // TabBar
        mPagerTabBar = (BdPagerTabBar) root.findViewById(R.id.pager_tab_bar);
        mPagerTabBar.setOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(BdPagerTabBar pagerBar, int index) {
                if (null != mViewPager) {
                    mViewPager.setCurrentItem(index);
                }
            }
        });
        
        // ViewPager
        mViewPager = (ViewPager) root.findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(3);    //SUPPRESS CHECKSTYLE
        // Indicator
        mPageIndicator = (DrawablePageIndicator) root.findViewById(R.id.indicator);
        mPageIndicator.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (null != mListener) {
                    mListener.onPageScrollStateChanged(state);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectTab(position);
                if (mListener != null) {
                    mListener.onPageSelected(position);
                }
            }
        });
        
        // 设置tab文字的颜色
        setTabTextColor(getResources().getColorStateList(R.color.sdk_tab_item_color));
        // 设置文字大小
        setTabTextSize((int) getResources().getDimension(R.dimen.pager_tab_item_textsize));
    }
    
    /**
     * 得到Tab bar的对象
     * 
     * @return BdPagerTabBar
     */
    public BdPagerTabBar getPagerTabBar() {
        return mPagerTabBar;
    }
    
    /**
     * 设置滑动页的指示图片
     * 
     * @param resId 资源id
     */
    public void setPageIndicatorDrawable(int resId) {
        if (null != mPageIndicator) {
            mPageIndicator.setIndicatorDrawable(getResources().getDrawable(resId));
        }
    }
    
    /**
     * Tab文字色的状态变化
     * 
     * @param colorStateList colorStateList
     */
    public void setTabTextColor(ColorStateList colorStateList) {
        if (null != mPagerTabBar) {
            mPagerTabBar.setTabTextColor(colorStateList);
        }
    } 
    
    /**
     * 设置Tab文字的颜色
     * 
     * @param textColor 文本色
     * @param selTextColor 选中的色
     */
    public void setTabTextColor(int textColor, int selTextColor) {
        if (null != mPagerTabBar) {
            mPagerTabBar.setTabTextColor(textColor, selTextColor);
        }
    }

    /**
     * 设置字体大小
     * 
     * @param textSize textSize
     */
    public void setTabTextSize(int textSize) {
        if (null != mPagerTabBar) {
            mPagerTabBar.setTabTextSize(textSize);
        }
    }
    
    /**
     * 设置tab bar的高度
     * 
     * @param height 高度
     */
    public void setTabBarHeight(int height) {
        View container = findViewById(R.id.pager_tab_bar_container);
        if (null != container) {
            ViewGroup.LayoutParams params = container.getLayoutParams();
            if (null != params) {
                params.height = height;
                container.setLayoutParams(params);
                requestLayout();
            }
        }
    }
    
    /**
     * 选中某一个指定索引处的tab
     * 
     * @param index 索引
     */
    public void selectTab(final int index) {
        if (null != mPagerTabBar) {
            mPagerTabBar.selectTab(index);
        }
    }
    
    /**
     * 选中某一个指定索引处的tab,并且切换过去
     * 
     * @param index 索引
     */
    public void selectTabAndScroll(final int index) {
        if (null != mPagerTabBar) {
            mPagerTabBar.selectTab(index);
            if (mViewPager != null) {
                mViewPager.setCurrentItem(index);
            }
        }
    }
    
    /**
     * 设置Fragment的adapter
     * 
     * @param initPosition 初始的选中的位置
     * @param adapter adapter
     */
    public void setPagerAdapter(PagerAdapter adapter, int initPosition) {
        if (null !=  mViewPager) {
            mViewPager.setAdapter(adapter);
            mPageIndicator.setViewPager(mViewPager, initPosition);
        }
        
        selectTab(initPosition);
    }
    
    /**
     * 设置tab的Adapter
     * 
     * @param adapter adapter
     */
    public void setTabAdapter(Adapter adapter) {
        if (null != mPagerTabBar) {
            mPagerTabBar.setAdapter(adapter);
        }
    }
    
    /**
     * 添加item，添加的item会平均分配当前action bar的宽度
     * 
     * @param tab ActionItem对象
     * @return this object.
     */
    public BdPagerTabHost addTab(BdPagerTab tab) {
        mPagerTabBar.addTab(tab);
        return this;
    }
    
    /**
     * 得到当前Tab的个数
     * 
     * @return tab的个数
     */
    public int getTabCount() {
        return mPagerTabBar.getTabCount();
    }
    
    /**
     * 得到当前选中的item的索引
     * 
     * @return index
     */
    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }
    
    /**
     * 开始布局tabs
     */
    public void layoutTabs() {
        mPagerTabBar.updateTabs();
    }
    
    /**
     * 设置tab bar的背景
     * 
     * @param resid 资源ID
     */
    public void setTabBarBackground(int resid) {
        if (null != mPagerTabBar) {
            mPagerTabBar.setBackgroundResource(resid);
        }
    }
    
    /**
     * 设置tab切换回调
     * @param listener OnTabHostChangeListener
     */
    public void setTabChangeListener(OnTabHostChangeListener listener) {
        mListener = listener;
    }
    
    /**
     * tab切换回调
     * @author haiyang
     *
     */
    public interface OnTabHostChangeListener {
        /**
         * 选择某个tab的回调
         * @param position viewpager里view的index
         */
        void onPageSelected(int position);
        
        /**
         * Pager状态改变的时候
         * @param state state
         */
        void onPageScrollStateChanged(int state);
    }
}
