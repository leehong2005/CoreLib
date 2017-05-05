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
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;

/**
 * 伪循环滚动的ViewPager
 * 
 * @author lihong06
 * @since 2014-10-8
 */
public class CircularViewPager extends WrapContentHeightViewPager {
    
    /** 是否循环开关 **/
    private boolean mCircularEnabled = true;
    /** 循环adapter **/
    private CircularPagerAdapter mCircularAdapter;
    /** PageChangeListener **/
    private OnPageChangeListener mExternalPageChangeListener;
    
    /**
     * 内部的PageChangeListener实现
     */
    private OnPageChangeListener mInternalPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (null != mExternalPageChangeListener) {
                if (null != mCircularAdapter) {
                    int pos = mCircularAdapter.toRealPosition(position);
                    mExternalPageChangeListener.onPageSelected(pos);
                }
            }
        }
        
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (null != mExternalPageChangeListener) {
                if (null != mCircularAdapter) {
                    int pos = mCircularAdapter.toRealPosition(position);
                    mExternalPageChangeListener.onPageScrolled(pos, positionOffset, positionOffsetPixels);
                }
            }
        }
        
        @Override
        public void onPageScrollStateChanged(int state) {
            if (null != mExternalPageChangeListener) {
                mExternalPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    };

    /**
     * 构造
     * @param context context
     */
    public CircularViewPager(Context context) {
        super(context);
    }
    
    /**
     * 构造
     * @param context context
     * @param attrs attrs
     */
    public CircularViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public int getCurrentItem() {
        int position = super.getCurrentItem();
        if (null != mCircularAdapter) {
            return mCircularAdapter.toRealPosition(position);
        }
        
        return position;
    }
    
    /**
     * 获取当前currentItem
     * @return currentItem
     */
    public int getAllCurrentItem() {
        return super.getCurrentItem();
    }
    
    /**
     * 设置是否循环
     * @param circularEnabled true:循环，false:不循环
     */
    public void setCircularEnabled(boolean circularEnabled) {
        mCircularEnabled = circularEnabled;
    }
    
    /**
     * 获取数量
     * @return count
     */
    public int getCount() {
        if (null != mCircularAdapter) {
            return mCircularAdapter.getRealCount();
        } else {
            PagerAdapter adapter = getAdapter();
            if (null != adapter) {
                return adapter.getCount();
            }
        }
        
        return 0;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }
    
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }
    
    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (mCircularEnabled) {
            if (!(adapter instanceof CircularPagerAdapter)) {
                //throw new IllegalArgumentException("The adapter MUST be a subclass of CircularPagerAdapter.");
                mCircularEnabled = false;
            }
        }
        
        if (mCircularEnabled) {
            setOnPageChangeListener(mInternalPageChangeListener);
            mCircularAdapter = (CircularPagerAdapter) adapter;
        }
        
        super.setAdapter(adapter);
    }
    
    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        if (mCircularEnabled) {
            if (listener != mInternalPageChangeListener) {
                mExternalPageChangeListener = listener;
            }
            super.setOnPageChangeListener(mInternalPageChangeListener);
        } else {
            super.setOnPageChangeListener(listener);
        }
    }
}
