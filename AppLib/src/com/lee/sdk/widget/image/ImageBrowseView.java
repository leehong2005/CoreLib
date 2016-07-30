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

package com.lee.sdk.widget.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.lee.sdk.Configuration;
import com.lee.sdk.cache.ImageWorker.OnLoadImageListener;
import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.res.R;
import com.lee.sdk.widget.gif.GifDrawable;
import com.lee.sdk.widget.image.ImageViewTouch.OnImageViewTouchSingleTapListener;
import com.lee.sdk.widget.image.ImageViewTouchBase.DisplayType;
import com.lee.sdk.widget.viewpager.OnRecycleListener;

/**
 * 浏览图片的View，这个类里面会处理图片加载失败的情况，点击图片隐藏/显示工具栏
 * 
 * @author LiHong
 * @since 2013-11-22
 */
public class ImageBrowseView extends FrameLayout implements OnRecycleListener {
    /**DEBUG*/
    private static final boolean DEBUG = Configuration.DEBUG & true;
    /**TAG*/
    private static final String TAG = "ImageBrowseView";
    /**最小的缩放率，1表示不能缩放得比原始大小还要小*/
    public static final float MIN_ZOOM = 1f;
    /**最大的缩放率，最大放大3倍*/
    public static final float MAX_ZOOM = 3f;
    /**重试次数*/
    private static final int MAX_RETRY_LOAD_COUNT = 0;
    
    /**图片的URL*/
    private String mImageUrl = null;
    /**支持缩放的ImageView*/
    private ZoomImageView mZoomImageView = null;
    /**进度View*/
    private View mProgressBar = null;
    /**重新加载提示文本*/
    private View mReloadTextView = null;
    /**该Layout包含progress bar，默认图片，重新加载提示文本*/
    private View mLoadingLayout = null;
    /**表示加载图片失败功与否，true表示失败*/
    private boolean mLoadImageFail = false;
    /**重新加载图片的重试次数*/
    private int mRetryLoadImageTime = 0;
    /** Image loader */
    private ImageLoader mImageLoader = null;
    /**加载bitmap的监听器*/
    private OnLoadImageListener mListener = new OnLoadImageListener() {
        @Override
        public void onLoadImage(Object data, Object bitmap) {
            if (null != bitmap) {
                // 加载bitmap成功，隐藏加载界面
                mLoadingLayout.setVisibility(View.INVISIBLE);
            } else {
                if (DEBUG) {
                    Log.e(TAG, "Failed to load bitmap...");
                }
                
                if (null != mZoomImageView) {
                    mZoomImageView.setAsyncDrawable(null);
                }
                
                // 除所有缓存
                if (null != mImageLoader) {
                    mImageLoader.clear();
                }
                
                // GC
                System.gc();
                
                boolean retryLoad = (mRetryLoadImageTime < MAX_RETRY_LOAD_COUNT);
                if (retryLoad) {
                    if (DEBUG) {
                        Log.d(TAG, "Retry to load the bitmap...");
                    }
                    // 如果成功开始加载图片，则重试次数自增
                    boolean succeed = loadImageByUrl();
                    if (succeed) {
                        mRetryLoadImageTime++;
                    }
                } else {
                    onLoadImageFailed();
                }
            }
        }
    };
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public ImageBrowseView(Context context) {
        this(context, null);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public ImageBrowseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public ImageBrowseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }
    
    /**
     * 初始化
     * 
     * @param context context
     */
    private void init(Context context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.sdk_image_browse_view, this);
        mZoomImageView = (ZoomImageView) view.findViewById(R.id.zoom_imageview);
        mProgressBar = view.findViewById(R.id.picture_load_progressbar);
        mReloadTextView = view.findViewById(R.id.reload_textview);
        mLoadingLayout = view.findViewById(R.id.picture_loading_layout);
        
        mZoomImageView.setDisplayType(DisplayType.FIT_IF_BIGGER);
        // 缩放范围1~3，不能缩小到比原始大小还小，最大可以放大到原始大小的3倍
        mZoomImageView.setZoomRange(MIN_ZOOM, MAX_ZOOM);
        // 双击能放大
        mZoomImageView.setDoubleTapEnabled(true);
        
        // ImageView放在布局的最上面，所以我们只需要注册ImageView的点击事件，不需要注册根View的点击事件。
        mZoomImageView.setSingleTapListener(new OnImageViewTouchSingleTapListener() {
            @Override
            public void onSingleTapConfirmed() {
                // 已经有图片了
                if (hasSetBitmap()) {
                    // Pass clicks on the ImageView to the parent activity to handle
                    if (OnClickListener.class.isInstance(getContext())) {
                        ((OnClickListener) getContext()).onClick(mZoomImageView);
                    }
                } else if (mLoadImageFail) {
                    // 重新加载图片
                    loadImageByUrl();
                }
            }
        });
    }
    
    /**
     * 设置URL
     * 
     * @param url url
     * @param imageLoader imageLoader
     */ 
    public void setData(String url, ImageLoader imageLoader) {
        mImageUrl = url;
        mImageLoader = imageLoader;
        loadImageByUrl();
    }
    
    /**
     * 得到可绽放的ImageView
     * 
     * @return ImageView对象
     */
    public View getImageView() {
        return mZoomImageView;
    }
    
    /**
     * 得到ImageView上面的bitmap
     * 
     * @return bitmap
     */
    public Bitmap getImageViewBitmap() {
        if (null != mZoomImageView) {
            Drawable drawable = mZoomImageView.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        
        return null;
    }
    
    /**
     * 缩放ImageView，这个方法会在ViewPager切换后调用，它会把之前已经放大的ImageView缩放回原始大小
     * 
     * @param scale 缩放率
     * @param durationMs 动画时间
     */
    public void zoomTo(float scale, float durationMs) {
        if (null != mZoomImageView) {
            mZoomImageView.zoomTo(scale, durationMs);
        }
    }
    
    /**
     * 回收内存
     */
    @Override
    public void recycle() {
        if (null != mZoomImageView) {
            Drawable drawable = mZoomImageView.getDrawable();
            if (DEBUG) {
                Log.d(TAG, "ImageBroweView#recycle(), drawable = " + drawable);
            }
            
            if (drawable instanceof GifDrawable) {
                ((GifDrawable) drawable).recycle();
            }
            
            mZoomImageView.setAsyncDrawable(null);
            mZoomImageView.setImageDrawable(null);
        }
    }
    
    /**
     * 指示当前的fragment上面是否已经有bitmap。
     * 
     * @return true表示已经有bitmap，否则返回false。
     */
    public boolean hasSetBitmap() {
        return (null != mZoomImageView) ? mZoomImageView.hasSetBitmap() : false;
    }
    
    /**
     * 当加载bitmap失败时，会被调用，这个方法里面我们需要隐藏progress bar，
     * 把“重新加载”提示文本显示出来。
     */
    private void onLoadImageFailed() {
        if (DEBUG) {
            Log.e(TAG, "onLoadImageFailed, show the error layout, url = " + mImageUrl);
        }
        
        mReloadTextView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mLoadImageFail = true;
    }
    
    /**
     * 根据URL加载bitmap
     * 
     * @return 如果成功开始加载图片，返回true，否则返回false
     */
    public boolean loadImageByUrl() {
        final String url = mImageUrl;
        boolean invalid = TextUtils.isEmpty(url);
        
        if (DEBUG) {
            Log.i(TAG, "loadImageByUrl   url = " + url + "   invalid = " + invalid);
        }
        
        mProgressBar.setVisibility(invalid ? View.INVISIBLE : View.VISIBLE);
        mReloadTextView.setVisibility(invalid ? View.VISIBLE : View.INVISIBLE);
        mLoadingLayout.setVisibility(View.VISIBLE);
        
        if (!invalid) {
            // 加载图片
            mLoadImageFail = false;
            if (null != mImageLoader) {
                mImageLoader.loadImage(url, mZoomImageView, mListener);
            }
        }
        
        return !invalid;
    }
}
