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

package com.lee.sdk.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 屏幕密度相关的API
 * 
 * @author liuxinjian
 * @since 2013-12-19
 */
public final class DensityUtils {
    
    /** 四舍五入 */
    private static final float DOT_FIVE = 0.5f;

    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private DensityUtils() {
    }
    
    /**
     * dip转换成px
     * 
     * @param context   Context
     * @param dip       dip Value
     * @return 换算后的px值
     */
    public static int dip2px(Context context, float dip) {
        float density = getDensity(context);
        return (int) (dip * density + DensityUtils.DOT_FIVE);
    }

    /**
     * px转换成dip
     * 
     * @param context   Context
     * @param px        px Value  
     * @return  换算后的dip值
     */
    public static int px2dip(Context context, float px) {
        float density = getDensity(context);
        return (int) (px / density + DOT_FIVE);
    }
    
    /**
     * DisplayMetrics 对象
     */
    private static DisplayMetrics sDisplayMetrics;
    
    /**
     * 得到显示宽度
     * 
     * @param context Context
     * 
     * @return 宽度
     */
    public static int getDisplayWidth(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.widthPixels;
    }
    
    /**
     * 得到显示高度
     * 
     * @param context Context
     * 
     * @return 高度
     */
    public static int getDisplayHeight(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.heightPixels;
    }
    
    /**
     * 得到显示密度
     * 
     * @param context Context
     * 
     * @return 密度
     */
    public static float getDensity(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.density;
    }
    
    /**
     * 得到DPI
     * 
     * @param context Context
     * 
     * @return DPI
     */
    public static int getDensityDpi(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.densityDpi;
    }
    
    /**
     * 初始化DisplayMetrics
     * 
     * @param context Context
     */
    private static synchronized void initDisplayMetrics(Context context) {
        if (null == sDisplayMetrics) {
            sDisplayMetrics = context.getResources().getDisplayMetrics();
        }
    }
}