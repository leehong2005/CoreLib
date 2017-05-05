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

package com.lee.sdk.test;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.widget.image.ImageViewTouchViewPager;

/**
 * Image viewer
 * 
 * @author lihong06
 * @since 2014-3-6
 */
public class ImageBrowserActivity extends GABaseActivity {
    /** Temporary datas */
    private static List<String> sTempDatas = null;
    /** Temporary position */
    private static int sTempPosition = 0;
    
    /** View pager */
    private ImageViewTouchViewPager mViewPager = null;
    /**ImageLoader实例*/
    private ImageLoader mImageLoader = null;
    
    /**
     * Launch the image browser
     * 
     * @param context context
     * @param datas datas
     */
    public static void launchImageBrowser(Context context, List<String> datas, int postion) {
        sTempDatas = datas;
        sTempPosition = postion;
        Intent intent = new Intent(context, ImageBrowserActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = ImageLoader.Builder.newInstance(this)
                .setUseDiskCache(false)
                .setMaxCachePercent(0.2f)
                .build();
//        mImageLoader.setOnProcessBitmapListener(new OnProcessBitmapListener() {
//            @Override
//            public Bitmap onProcessBitmap(Object data) {
//                if (data instanceof String) {
//                    return ImageLoader.loadImageFromFile((String) data);
//                }
//                return null;
//            }
//        });
        
        mViewPager = new ImageViewTouchViewPager(this);
        mViewPager.setDatas(sTempDatas);
        mViewPager.setImageLoader(mImageLoader);
        mViewPager.setPageMargin((int) getResources().getDimension(R.dimen.pciture_view_pager_margin));
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(sTempPosition);
        
        setContentView(mViewPager);
        sTempDatas = null;
        sTempPosition = 0;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (null != mImageLoader) {
            // 取消图片加载的监听器
            mImageLoader.setOnLoadImageListener(null);
            // 清除缓存
            mImageLoader.clear();
            mImageLoader = null;
        }
    }
}
