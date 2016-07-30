/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.widget.viewpager;

/**
 * 定义了当某一个View被回收的接口，典型的用法是某一个View放在基于Adapter的容器中
 * 例如<code>ViewPager</code>等。
 * 
 * @author lihong06
 * @since 2014-5-13
 */
public interface OnRecycleListener {
    /**
     * 当对象被回收的时候会调用这个方法
     */
    public void recycle();
}
