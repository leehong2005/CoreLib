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

import java.util.ArrayList;
import java.util.List;

import android.content.res.ColorStateList;
import android.text.TextUtils;

/**
 * 这个类表示显示在ViewPager上面的Tab的数据结构，这里面包含了每一个tab项所需要的基本数据
 * 比如文字，文字色，字体大小，图标Id等，我们可以扩展这个类以支持更多形式的tab项形式。
 * 
 * @author LiHong
 * @since 2013-11-11
 */
public class BdPagerTab {
    /** id，唯一标识这个tab */
    private String mId;
    /** 标题 */
    private String mTitle;
    /** 图标的id */
    private int mIconResId;
    /** 字体大小 */
    private int mTextSize = 20; // SUPPRESS CHECKSTYLE
    /** 文字的色 */
    private int mTextColor = -1;
    /** 选中的文字的色 */
    private int mSelTextColor = -1;
    /** 文字的选中色 */
    private ColorStateList mColorStateList = null;
    /** Tab项的背景图片 */
    private int mTabBackgroundResId = 0;
    /** 父tab */
    private BdPagerTab mParentTab = null;
    /** 子Tab列表 */
    private ArrayList<BdPagerTab> mSubTabs = null;
    /** 选中的子菜单的索引 */
    private int mSelSubTabIndex = -1;
    
    /**
     * 设置ID
     * 
     * @param id ID
     * @return 当前对象
     */
    public BdPagerTab setId(String id) {
        mId = id;
        return this;
    }
    
    /**
     * 设置标题
     * 
     * @param title 标题
     * @return 当前对象
     */
    public BdPagerTab setTitle(String title) {
        mTitle = title;
        return this;
    }
    
    /**
     * 设置图标的资源ID
     * 
     * @param iconResId 资源Id
     * @return 当前对象
     */
    public BdPagerTab setIconResId(int iconResId) {
        mIconResId = iconResId;
        return this;
    }
    
    /**
     * 设置文字大小
     * 
     * @param textSize 标题文字大小
     * @return 当前对象
     */
    public BdPagerTab setTextSize(int textSize) {
        mTextSize = textSize;
        return this;
    }

    /**
     * 置文本的颜色
     * 
     * @param textColor the mTextColor to set
     * @return 当前对象
     */
    public BdPagerTab setTextColor(int textColor) {
        mTextColor = textColor;
        return this;
    }
    
    /**
     * 设置文本选中的颜色
     * 
     * @param selTextColor the mSelTextColor to set
     * @return 当前对象
     */
    public BdPagerTab setSelTextColor(int selTextColor) {
        mSelTextColor = selTextColor;
        return this;
    }
    
    /**
     * 设置文字的色，包含选中的色
     * 
     * @param colorStateList id
     * @return 当前对象
     */
    public BdPagerTab setColorStateList(ColorStateList colorStateList) {
        mColorStateList = colorStateList;
        return this;
    }
    
    /**
     * 设置tab项的背景图片
     * 
     * @param resId id
     * @return 当前对象
     */
    public BdPagerTab setTabBackgroundResId(int resId) {
        mTabBackgroundResId = resId;
        return this;
    }

    /**
     * 得到id，唯一标识这个tab
     * 
     * @return 当前tab的ID
     */
    public String getId() {
        return mId;
    }

    /**
     * 得到标题
     * 
     * @return 标题
     */
    public String getTitle() {
        return mTitle;
    }
    
    /**
     * 得到图标的资源ID
     * 
     * @return 图标的资源ID
     */
    public int getIconResId() {
        return mIconResId;
    }
    
    /**
     * 得到标题文字大小
     * 
     * @return 标题文字大小
     */
    public int getTextSize() {
        return mTextSize;
    }

    /**
     * 得到文本的颜色
     *  
     * @return the mTextColor
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * 得到文本选中的颜色
     * 
     * @return the mSelTextColor
     */
    public int getSelTextColor() {
        return mSelTextColor;
    }
    
    /**
     * 得到ColorStateList的id
     * 
     * @return id
     */
    public ColorStateList getColorStateList() {
        return mColorStateList;
    }
    
    /**
     * 添加子Tab
     * 
     * @param subTab 子Tab
     * @return 当前对象
     */
    public BdPagerTab addSubTab(BdPagerTab subTab) {
        if (null == mSubTabs) {
            mSubTabs = new ArrayList<BdPagerTab>();
        }
        
        if (null != subTab) {
            subTab.mParentTab = this;
            mSubTabs.add(subTab);
        }
        
        return this;
    }
    
    /**
     * 得到父tab
     * 
     * @return 父tab，可能为null
     */
    public BdPagerTab getParentTab() {
        return mParentTab;
    }
    
    /**
     * 得到子tab的个数
     * 
     * @return 子tab的个数
     */
    public int getSubTabCount() {
        return (null != mSubTabs) ? mSubTabs.size() : 0;
    }
    
    /**
     * 得到所有子菜单
     * 
     * @return list
     */
    public List<BdPagerTab> getSubTabs() {
        return mSubTabs;
    }
    
    /**
     * 设置选中的子菜单的索引
     * 
     * @param selSubTabIndex 索引
     */
    public void setSelSubTabIndex(int selSubTabIndex) {
        mSelSubTabIndex = selSubTabIndex;
    }
    
    /**
     * 得到选中的子菜单的索引
     * 
     * @return 索引
     */
    public int getSelSubTabIndex() {
        return mSelSubTabIndex;
    }
    
    /**
     * 得到指定位置的子tab
     * 
     * @param index 索引
     * @return 子tab，可能为null
     */
    public BdPagerTab getSubTabAt(int index) {
        if (null != mSubTabs) {
            if (index >= 0 && index < mSubTabs.size()) {
                return mSubTabs.get(index);
            }
        }
        
        return null;
    }
    
    /**
     * 得到tab的背景图片
     * 
     * @return id
     */
    public int getTabBackgroundResId() {
        return mTabBackgroundResId;
    }
    
    /**
     * 是否需要选中，根据指定的tab id，如果当前的tab或其子tab的id与指定的id相同的话，说明这个tab应该选中，返回true，否则
     * 返回false
     * 
     * @param tabId tab id
     * @return true/false
     */
    public boolean needSelected(String tabId) {
        if (TextUtils.isEmpty(tabId)) {
            return false;
        }
        
        if (tabId.equalsIgnoreCase(mId)) {
            return true;
        }
        
        if (null != mSubTabs) {
            for (BdPagerTab subTab : mSubTabs) {
                if (subTab.needSelected(tabId)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 格式是：父tab标题,当前tab标题
     * 
     * @return 格式化的字符串
     */
    public String getFormatTitle() {
        // 格式是：父tab标题,当前tab标题
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(mTitle)) {
            sb.append(mTitle);
        }
        
        BdPagerTab parent = mParentTab;
        if (null != parent) {
            while (null != parent) {
                sb.insert(0, (parent.getTitle() + ","));
                parent = parent.getParentTab();
            }
        }
        
        if (sb.length() == 0) {
            sb.append(toString());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "title = " + mTitle + ", id = " + mId + ", obj = " + super.toString();
    }
}
