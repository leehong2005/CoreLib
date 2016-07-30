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

package com.lee.sdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

import com.lee.sdk.Configuration;

/**
 * 监听scrollview size的变化
 * 
 * @author suixin
 * @since 2014-12-31
 */
public class BdScrollView extends ScrollView {
    /**
     * DEBUG
     */
    private static final boolean DEBUG = Configuration.DEBUG;

    /**
     * TAG
     */
    private static final String TAG = "BdScrollView";

    /**
     * 最大高度
     */
    private int mMaxHeight = -1;

    /**
     * 构造函数
     * 
     * @param context context
     */
    public BdScrollView(Context context) {
        super(context);
    }

    /**
     * 构造函数
     * 
     * @param context context
     * @param attrs attrs
     */
    public BdScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造函数
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public BdScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (DEBUG) {
            Log.d(TAG, "onMeasure( " + widthMeasureSpec + ", " + heightMeasureSpec + ")");
        }
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxHeight > 0) {
            heightSize = Math.min(heightSize, mMaxHeight);
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int childHeight = getChildAt(0).getMeasuredHeight();
        int childWidth = getChildAt(0).getMeasuredWidth();
        if (childHeight > 0) {
            heightSize = Math.min(childHeight, heightSize);
        }
        if (childWidth > 0) {
            widthSize = Math.min(childWidth, widthSize);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (DEBUG) {
            Log.d(TAG, "onLayout( " + changed + ", " + l + ", " + t + ", " + r + ", " + b + ")");
        }
    }

    /**
     * 设置最大高度
     * 
     * @param height height
     */
    public void setMaxHeight(int height) {
        mMaxHeight = height;
    }
}
