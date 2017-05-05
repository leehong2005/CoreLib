package com.lee.sdk.test.staggered;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lee.sdk.cache.IAsyncView;
import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.utils.DensityUtils;
import com.lee.sdk.widget.staggered.ScaleImageView;

/**
 * 
 * @author lihong06
 * @since 2014-3-7
 */
public class ImageAlbumView extends FrameLayout implements IAsyncView {

    private ScaleImageView mImageView = null;
    private TextView mTextView = null;
    
    public ImageAlbumView(Context context) {
        super(context);
        
        init(context);
    }
    
    public ImageAlbumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    private void init(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = DensityUtils.dip2px(context, 6);
        int height = DensityUtils.dip2px(context, 30);
        mImageView = new ScaleImageView(context);
        
        mTextView = new TextView(context);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        mTextView.setPadding(padding, 0, padding, 0);
        mTextView.setEllipsize(TruncateAt.END);
        mTextView.setTextColor(Color.BLACK);
        mTextView.setSingleLine();
        
        container.addView(mImageView, new LinearLayout.LayoutParams(-1, -2));
        container.addView(mTextView, new LinearLayout.LayoutParams(-1, height));
        
        container.setBackgroundColor(Color.WHITE);
        container.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams containerParam = new FrameLayout.LayoutParams(-1, -1);
        containerParam.bottomMargin = DensityUtils.dip2px(context, 8);
        addView(container, containerParam);
    }
    
    /**
     * 设置图集数据
     * 
     * @param data 图集数据对象
     * @param imageLoader 图片加载器
     */
    public void setData(ImageAlbumItem data, ImageLoader imageLoader) {
        if (null == data) {
            mTextView.setText(null);
            mImageView.setImageDrawable(null);
            return;
        }
        
        mTextView.setText(data.getTitle());
        mImageView.setImageHeight(data.getHeight());
        mImageView.setImageWidth(data.getWidth());
        
        if (null != imageLoader) {
            imageLoader.loadImage(data.getUrl(), this);
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (null != mImageView) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        
    }

    private Drawable mDrawable = null;
    
    public void setAsyncDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public Drawable getAsyncDrawable() {
        return mDrawable;
    }

    @Override
    public boolean isGifSupported() {
        return false;
    }
}
