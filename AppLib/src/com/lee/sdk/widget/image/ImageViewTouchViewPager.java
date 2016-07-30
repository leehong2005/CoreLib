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

package com.lee.sdk.widget.image;

import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.lee.sdk.cache.api.ImageLoader;

/**
 * 这个类扩展了ViewPager类，因为放在里面的图片可能被放大了，所以必须重写{@link ViewPager#canScroll()}方法。
 * 
 * @author Li Hong
 * @since 2013-7-25
 */
public class ImageViewTouchViewPager extends ViewPager {
    /** Adapter */
    private ImagePagerAdapter mAdapter;
    /** Listener */
    private OnPageChangeListener mPageChangeListener;
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public ImageViewTouchViewPager(Context context) {
        super(context);
        
        init(context);
    }

    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public ImageViewTouchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /**
     * Initialize
     * 
     * @param context
     */
    private void init(Context context) {
        // Should call super's method.
        super.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            // 前一个选中的索引
            private int mPreviousPosition = 0;
            
            @Override
            public void onPageSelected(int position) {
                if (null != mPageChangeListener) {
                    mPageChangeListener.onPageSelected(position);
                }
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
                if (ViewPager.SCROLL_STATE_SETTLING == state && mPreviousPosition != getCurrentItem()) {
                    View view = mAdapter.getCurrentView(mPreviousPosition);
                    if (view instanceof ImageBrowseView) {
                        ((ImageBrowseView) view).zoomTo(1f, 100); 
                    }
                    
                    mPreviousPosition = getCurrentItem();
                }
                
                if (null != mPageChangeListener) {
                    mPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });
        
        mAdapter = new ImagePagerAdapter(context);
        setAdapter(mAdapter);
    }
    
    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;
    }

    /**
     * Set image loader
     * 
     * @param imageLoader imageLoader
     */
    public void setImageLoader(ImageLoader imageLoader) {
        if (null != mAdapter) {
            mAdapter.setImageLoader(imageLoader);
        }
    }
    
    /***
     * Set the datas
     * 
     * @param datas datas
     */
    public void setDatas(List<String> datas) {
        if (null != mAdapter) {
            mAdapter.setDatas(datas);
        }
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ImageViewTouch) {
            return ((ImageViewTouch) v).canScroll(dx);
        }

        return super.canScroll(v, checkV, dx, x, y);
    }
}