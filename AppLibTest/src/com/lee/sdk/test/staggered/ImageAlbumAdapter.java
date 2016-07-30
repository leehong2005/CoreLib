package com.lee.sdk.test.staggered;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lee.sdk.cache.api.ImageLoader;

public class ImageAlbumAdapter extends BaseAdapter {
    final private LinkedList<ImageAlbumItem> mDatas = new LinkedList<ImageAlbumItem>();
    final private Context mContext;
    private ImageLoader mImageLoader;
    
    public ImageAlbumAdapter(Context context) {
        mContext = context;
    }
    
    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }
    
    public void addDatas(List<ImageAlbumItem> datas) {
        if (null != datas) {
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }
    
    
    
    public List<ImageAlbumItem> getDatas() {
        return mDatas;
    }
    
    public void clear() {
        mDatas.clear();
    }
    
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < getCount()) {
            return mDatas.get(position);
        }
        
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageAlbumView listItem = null;
        
        if (null == convertView) {
            listItem = new ImageAlbumView(mContext);
            convertView = listItem;
        } else {
            listItem = (ImageAlbumView) convertView;
        }
        
        ImageAlbumItem data = mDatas.get(position);
        listItem.setData(data, mImageLoader);
        
        // 如果缓存中的图片数量大于15个，就清除内存中的图片缓存，因为封面可能太大，根本连15张都无法容纳
        if (null != mImageLoader && mImageLoader.getBitmapSizeInMemCache() > 15) {
            mImageLoader.clear();
        }
        
        return convertView;
    }
}