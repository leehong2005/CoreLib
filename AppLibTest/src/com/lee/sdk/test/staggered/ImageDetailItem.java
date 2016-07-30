/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.staggered;

/**
 * 这个类定义了图集中每一图片的数据结构
 * 
 * @author Li Hong
 * 
 * @since 2013-7-27
 */
public class ImageDetailItem {
    
    /**图片的URL*/
    private String mImageUrl;
    /**图片的宽度*/
    private int mWidth;
    /**图片的高度*/
    private int mHeight;
    
    /**
     * 设置图片URL
     * 
     * @param imageUrl 图片URL
     */
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
    
    /**
     * 得到图片URL
     * 
     * @return 图片URL
     */
    public String getImageUrl() {
        return mImageUrl;
    }
    
    /**
     * 设置图片宽度
     * 
     * @param width 图片宽度
     */
    public void setWidth(int width) {
        mWidth = width;
    }
    
    /**
     * 返回图片宽度
     * 
     * @return 图片宽度
     */
    public int getWidth() {
        return mWidth;
    }
    
    /**
     * 设置图片高度
     * 
     * @param height 图片高度
     */
    public void setHeight(int height) {
        mHeight = height;
    }
    
    /**
     * 得到图片高度
     * 
     * @return 图片高度
     */
    public int getHeight() {
        return mHeight;
    }
}
