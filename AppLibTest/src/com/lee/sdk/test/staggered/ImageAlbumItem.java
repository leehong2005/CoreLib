/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.staggered;

import java.util.ArrayList;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lee.sdk.cache.ILoadImage;

/**
 * 这个类定义了图集的数结结构
 * 
 * @author Li Hong
 * 
 * @since 2013-7-23
 */
public class ImageAlbumItem implements ILoadImage {
    /**图片的高度*/
    private int mHeight = 359; // SUPPRESS CHECKSTYLE
    /**图片的宽度*/
    private int mWidth = 240; // SUPPRESS CHECKSTYLE
    /**图集的ID*/
    private String mAlbumId = "";
    /**图集的标题*/
    private String mTitle = "";
    /**图集的封面的URL*/
    private String mThumbUrl = "";
    /**图集中的图片*/
    private ArrayList<ImageDetailItem> mPictureDetails = new ArrayList<ImageDetailItem>();

    @Override
    public Bitmap loadImage() {
        // TODO:具体的加载图片的逻辑可以在这里实现
        return null;
    }

    @Override
    public String getUrl() {
        return getThumbUrl();
    }

    @Override
    public int getSampleSize(BitmapFactory.Options options) {
        return 0;
    }

    @Override
    public Map<String, String> getHeader() {
        return null;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        if (0 != height) {
            this.mHeight = height;
        }
    }

    /**
     * 得到图片的宽度
     * 
     * @return 宽度
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * 设置图片的宽度
     * 
     * @param width 宽度
     */
    public void setWidth(int width) {
        if (0 != width) {
            this.mWidth = width;
        }
    }

    /**
     * 得到图集ID
     * 
     * @return ID
     */
    public String getAlbumId() {
        return mAlbumId;
    }

    /**
     * 设置图集ID
     * 
     * @param albumId ID
     */
    public void setAlbumId(String albumId) {
        this.mAlbumId = albumId;
    }

    /**
     * 得到图集的标题
     * 
     * @return 标题
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * 设置图集的标题
     * 
     * @param title 标题
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * 得到图集封面的URL
     * 
     * @return URL
     */
    public String getThumbUrl() {
        return mThumbUrl;
    }

    /**
     * 设置图集封面的URL
     * 
     * @param url URL
     */
    public void setThumbUrl(String url) {
        this.mThumbUrl = url;
    }

    /**
     * 得到图集中图片的数量
     * 
     * @return 图片个数
     */
    public int getPictureCount() {
        return mPictureDetails.size();
    }

    /**
     * 设置图集中的图片的数量
     * 
     * @param pictureCount 图片个数
     */
    public void setPictureCount(int pictureCount) {
    }
    
    /**
     * 向图片中添加一个图片数据
     * 
     * @param pictureDetailItem 图片数据
     */
    public void addPictureDetailItem(ImageDetailItem pictureDetailItem) {
        if (null != pictureDetailItem) {
            if (!mPictureDetails.contains(pictureDetailItem)) {
                mPictureDetails.add(pictureDetailItem);
            }
        }
    }
    
    /**
     * 得到指定的图片的URL
     * 
     * @param index 索引
     * @return URL
     */
    public String getPictureDetailUrl(int index) {
        if (index >= 0 && index < mPictureDetails.size()) {
            return mPictureDetails.get(index).getImageUrl();
        }
        
        return null;
    }
}
