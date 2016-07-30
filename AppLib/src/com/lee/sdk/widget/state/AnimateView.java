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


package com.lee.sdk.widget.state;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作动画的View
 * 
 * @author lihong06
 * @since 2014-9-4
 */
public class AnimateView extends View {
    /** 绘制的View */
    private View mDrawingView;
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public AnimateView(Context context) {
        super(context);
        
        init(context);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public AnimateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public AnimateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }
    
    /**
     * 初始化
     * 
     * @param context context
     */
    private void init(Context context) {
        setDrawingCacheEnabled(true);
    }
    
    /**
     * 设置真实绘制的View
     * 
     * @param view view
     */
    public void setDrawingView(View view) {
        mDrawingView = view;
    }
    
    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        mDrawingView = null;
    }
    
    @Override
    public void draw(Canvas canvas) {
        if (null != mDrawingView) {
            mDrawingView.draw(canvas);
        } else {
            super.draw(canvas);
        }
    }
}
