package com.lee.sdk.test.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.lee.sdk.cache.IAsyncView;
import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.test.utils.MediaInfo;

/**
 * 
 * @author lihong06
 * @since 2014-1-23
 */
public class ImageViewAdapter extends BaseAdapter {
    public static final int COLUMN_NUM = 3;
    public static final int SPACE = 10;
    
    protected ArrayList<MediaInfo> mDatas = new ArrayList<MediaInfo>();
    private int mWidth = 180;
    private int mHeight = 180;
    private boolean mCalced = false;
    private Context mContext;
    private ImageLoader mImageLoader;
    
    public ImageViewAdapter(Context context, ImageLoader imageLoader) {
        mContext = context;
        mImageLoader = imageLoader;
    }
    
    public void setData(ArrayList<MediaInfo> datas) {
        if (null != datas) {
            mDatas.clear();
            mDatas.addAll(datas);
        }
    }
    
    @Override
    public int getCount() {
        return (null != mDatas) ? mDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridViewImageView imageView = null;

        if (null == convertView) {
            calc(parent.getWidth());
            imageView = (GridViewImageView) createView(mContext, parent);
            imageView.setBackgroundColor(Color.WHITE);
            imageView.setLayoutParams(new AbsListView.LayoutParams(mHeight, mHeight));
            imageView.setScaleType(ScaleType.CENTER_CROP);
            convertView = imageView;
        }

        imageView = (GridViewImageView) convertView;
        if (null != mImageLoader) {
            mImageLoader.loadImage(mDatas.get(position), imageView);
            
            // 如果缓存中的图片数量大于15个，就清除内存中的图片缓存，因为封面可能太大，根本连15张都无法容纳
            if (mImageLoader.getBitmapSizeInMemCache() > 15) {
                mImageLoader.clear();
            }
        }

        return convertView;
    }
    
    protected View createView(Context context, ViewGroup parent) {
        return new GridViewImageView(context);
    }
    
    private void calc(int parentWidth) {
        if (mCalced) {
            return;
        }
        
        mCalced = true;
        
        mWidth = (parentWidth - SPACE * (COLUMN_NUM )) / COLUMN_NUM;
        mHeight = mWidth;
    }
    
    public static class GridViewImageView extends ImageView implements IAsyncView {
        public GridViewImageView(Context context) {
            super(context);
        }

        public GridViewImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public GridViewImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        Drawable mDrawable = null;

        @Override
        public Drawable getAsyncDrawable() {
            return mDrawable;
        }

        @Override
        public boolean isGifSupported() {
            return false;
        }

        @Override
        public void setAsyncDrawable(Drawable drawable) {
            mDrawable = drawable;
        }
    }
}
