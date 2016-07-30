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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.widget.viewpager.PagerAdapterImpl;

/**
 * 创建图片浏览的View的Adapter
 * 
 * @author lihong06
 * @since 2013-11-22
 */
public class ImagePagerAdapter extends PagerAdapterImpl {
    /** Context */
    private final Context mContext;
    /** ImageLoader */
    private ImageLoader mImageLoader;
    /** Url list */
    private ArrayList<String> mDatas = new ArrayList<String>();
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public ImagePagerAdapter(Context context) {
        mContext = context;
    }

    /**
     * Set image loader
     * 
     * @param imageLoader imageLoader
     */
    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    /***
     * Set the datas
     * 
     * @param datas datas
     */
    public void setDatas(List<String> datas) {
        if (null != datas) {
            mDatas.clear();
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }
    
    /**
     * Get the url at the position
     * 
     * @param position position
     * @return
     */
    private String getUrl(int position) {
        if (null != mDatas) {
            if (position >= 0 && position < mDatas.size()) {
                return mDatas.get(position);
            }
        }
        
        return null;
    }

    @Override
    public int getCount() {
        return (null != mDatas) ? mDatas.size() : 0;
    }

    /**
     * @see com.lee.sdk.widget.viewpager.PagerAdapterImpl#onInstantiateItem(android.view.ViewGroup, int)
     */
    @Override
    protected View onInstantiateItem(ViewGroup container, int position) {
        return new ImageBrowseView(mContext);
    }

    /**
     * @see com.lee.sdk.widget.viewpager.PagerAdapterImpl#onConfigItem(android.view.View, int)
     */
    @Override
    protected void onConfigItem(View convertView, int position) {
        String url = getUrl(position);
        ImageBrowseView pictureView = (ImageBrowseView) convertView;
        pictureView.setData(url, mImageLoader);
    }
}
