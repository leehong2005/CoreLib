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

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.lee.sdk.Configuration;
import com.lee.sdk.cache.RecyclePool;

/**
 * {@link #PagerAdapter}的实现者，内部使用了复用机制，通常用于显示相同View
 * 
 * @author lihong06
 * @since 2014-3-20
 */
public abstract class PagerAdapterImpl extends PagerAdapter {
    /**TAG*/
    private static final String TAG = "PagerAdapterImpl";
    /**DEBUG*/
    private static final boolean DEBUG = Configuration.DEBUG & true;
    /** Recycle pool */
    private RecyclePool<View> mViewPool = new RecyclePool<View>(5); // SUPPRESS CHECKSTYLE
    /** 显示在ViewPager中的View对象 */
    private SparseArray<View> mViewList = new SparseArray<View>();
    
    /**
     * 得到指定索引处的View
     * 
     * @param position 索引
     * @return View，可能为null
     */
    public View getCurrentView(int position) {
        return mViewList.get(position);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (DEBUG) {
            Log.i(TAG, "destroyItem    position = " + position + "    recycle a view~~~~~~~");
        }
        
        View view = (View) object;
        
        // 调用回调
        if (view instanceof OnRecycleListener) {
            ((OnRecycleListener) view).recycle();
        }
        
        container.removeView(view);
        mViewPool.recycle(view);
        mViewList.remove(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViewPool.get();
        if (null == view) {
            if (DEBUG) {
                Log.e(TAG, "instantiateItem    create view!!!");
            }
            view = onInstantiateItem(container, position);
        }
        
        mViewList.put(position, view);
        container.addView(view);
        
        // 配置View
        onConfigItem(view, position);
        
        return view;
    }
    
    /**
     * Create the new item, this method will be invoked in {@link #instantiateItem(ViewGroup, int)} method. this method
     * will only called when cached view is null.
     * 
     * @param container container
     * @param position position
     * @return view
     */
    protected abstract View onInstantiateItem(ViewGroup container, int position);
    
    /**
     * 重写这个方法来为指定的View绑定数据。
     * 
     * @param convertView 旧的view
     * @param position 数据的索引
     */
    protected abstract void onConfigItem(View convertView, int position);
}
