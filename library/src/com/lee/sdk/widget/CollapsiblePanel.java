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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.lee.sdk.Configuration;
import com.lee.sdk.res.R;

/**
 * 可以收缩和展开的控件，默认向垂直方向展开
 * 
 * @author LiHong
 * @since 2014/08/19
 */
public class CollapsiblePanel extends LinearLayout {
    /** TAG */
    private static final String TAG = "CollapsiblePanel";
    /** DEBUG */
    private static final boolean DEBUG = Configuration.DEBUG & true;
    /** 内容View */
    private View mContentView;
    /** 可收缩的View */
    private View mCollapsibleView;
    /** 可收缩的大小 */
    private int mCollapsibleSize;
    /** 收缩的监听器 */
    private OnCollapsibleListener mCollapsibleListener;
    /** 收缩动画的时间 */
    private int mAnimDuration = 0;  //SUPPRESS CHECKSTYLE
    /** 判断当前可收缩View是否是打开状态 */
    private boolean mIsOpened = false;
    /** 可收缩View默认是否可见 */
    private boolean mCollapsibleViewDefaultVisible = false;
    /** Toggle是否可用 */
    private boolean mToggleEnable = true;

    /**
     * 可收缩监听器
     * 
     * @author lihong06
     * @since 2014-8-20
     */
    public interface OnCollapsibleListener {
        /**
         * 动画结束监听
         * <p>
         * 与{@linkplain com.example.widget.isStretchViewOpened isStretchViewOpened()}的返回值是一致的
         * </p>
         * @param isOpened 当前的stretchView是否是打开的,
         */
        void onCollapsibleFinished(boolean isOpened);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public CollapsiblePanel(Context context) {
        super(context);
        
        init(context, null);
    }

    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public CollapsiblePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context, attrs);
    }

    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public CollapsiblePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context, attrs);
    }

    /**
     * 初始化
     * 
     * @param context context
     * @param attrs attrs
     */
    private void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        mAnimDuration = context.getResources().getInteger(R.integer.slide_anim_duration);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCollapsibleSize == 0 && mCollapsibleView != null) {
            mCollapsibleView.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
            
            if (LinearLayout.VERTICAL == getOrientation()) {
                mCollapsibleSize = mCollapsibleView.getMeasuredHeight();
                if (!mCollapsibleViewDefaultVisible) {
                    mCollapsibleView.getLayoutParams().height = 0;
                }
            } else {
                mCollapsibleSize = mCollapsibleView.getMeasuredWidth();
                if (!mCollapsibleViewDefaultVisible) {
                    mCollapsibleView.getLayoutParams().width = 0;
                }
            }
            
            if (DEBUG) {
                Log.i(TAG, "stretchview height = " + mCollapsibleSize);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
     * 设置控件初始时是否可见
     * 
     * @param visible 是否可见
     */
    public void setCollapsibleViewDefaultVisible(boolean visible) {
        mCollapsibleViewDefaultVisible = visible;
        // 默认可见的话，则认为是展开的
        mIsOpened = visible;
    }
    
    /**
     * 控件初始时是否可见
     * 
     * @return visible
     */
    public boolean getCollapsibleViewDefaultVisible() {
        return mCollapsibleViewDefaultVisible;
    }
    
    /**
     * 设置toggle是否可用，如果设置为false，则{@link #toggle()接口无效
     * 
     * @param enable enable
     */
    public void setToggleEnable(boolean enable) {
        mToggleEnable = enable;
    }

    /**
     * 获取当前的主View
     * 
     * @return view
     */
    public View getContentView() {
        return mContentView;
    }

    /**
     * 获取当前的扩展View
     * 
     * @return view
     */
    public View getStretchView() {
        return mCollapsibleView;
    }

    /**
     * 设置主View
     * 
     * @param view view
     */
    public void setContentView(View view) {
        if (view != null) {
            if (mContentView != null) {
                removeView(this.mContentView);
            }

            mContentView = view;
            addView(mContentView, 0);
        }
    }

    /**
     * 设置收缩的View
     * 
     * @param collapsibleView 可以收缩的View
     */
    public void setCollapsibleView(View collapsibleView) {
        if (collapsibleView != null) {
            if (mCollapsibleView != null) {
                removeView(mCollapsibleView);
                // 在重新设置时，将该值置为0，否则新view将不能显示正确的高度
                mCollapsibleSize = 0;
            }
            
            mCollapsibleView = collapsibleView;
            addView(mCollapsibleView);
        }
    }
    
    /**
     * 得到可收缩View的大小
     * 
     * @return 可收缩View的大小
     */
    public int getCollapsibleSize() {
        return mCollapsibleSize;
    }

    /**
     * 设置收缩的监听
     * 
     * @param listener listener
     */
    public void setOnCollapsibleListener(OnCollapsibleListener listener) {
        mCollapsibleListener = listener;
    }

    /**
     * 当前的视图是否已经展开
     * 
     * @return true/false
     */
    public boolean isCollapsibleViewOpened() {
        return mIsOpened;
    }

    /**
     * 设置展开（或者收缩）动画的时间，默认280ms
     * 
     * @param durationMs durationMs
     */
    public void setCollapsibleAnimDuration(int durationMs) {
        if (durationMs >= 0) {
            mAnimDuration = durationMs;
        } else {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
    }

    /**
     * 展开/收起View
     * 
     * @return true/false
     */
    public boolean toggle() {
        // 如果不允许展开
        if (!mToggleEnable) {
            return false;
        }
        
        // 如果动画正在进行，不执行任何操作
        if (isAnimationPlaying()) {
            return false;
        }
        
        if (mIsOpened) {
            closeCollapsibleView();
        } else {
            openCollapsibleView();
        }
        
        return true;
    }

    /**
     * 展开视图
     */
    public void openCollapsibleView() {
        if (mCollapsibleView == null) {
            return;
        }
        
        post(new Runnable() {
            @Override
            public void run() {
                CollapsibleAnimation animation = new CollapsibleAnimation(0, mCollapsibleSize, 0.0f, 1.0f);
                animation.setDuration(mAnimDuration);
                animation.setAnimationListener(mCollapsibleAnimListener);
                mCollapsibleView.startAnimation(animation);
                invalidate();
            }
        });
    }

    /**
     * 收起视图
     */
    public void closeCollapsibleView() {
        if (mCollapsibleView == null) {
            return;
        }
        
        post(new Runnable() {
            @Override
            public void run() {
                CollapsibleAnimation animation = new CollapsibleAnimation(mCollapsibleSize, 0, 1.0f, 0.0f);
                animation.setDuration(mAnimDuration);
                animation.setAnimationListener(mCollapsibleAnimListener);
                mCollapsibleView.startAnimation(animation);
                invalidate();
            }
        });
    }
    
    /**
     * 收缩View展开或收缩时调用
     * 
     * @param isOpened isOpened
     */
    protected void onCollapsibleFinished(boolean isOpened) {
        
    }
    
    /**
     * 设置收缩View的大小
     * 
     * @param size size
     */
    private void setCollapsibleViewSize(int size) {
        if (null == mCollapsibleView) {
            return;
        }
        
        LayoutParams params = (LayoutParams) mCollapsibleView.getLayoutParams();
        if (null != params) {
            if (LinearLayout.VERTICAL == getOrientation()) {
                params.height = size;
            } else {
                params.width = size;
            }

            mCollapsibleView.setLayoutParams(params);
        }
    }
    
    /**
     * 判断动画是否正在播放
     * 
     * @return true/false
     */
    private boolean isAnimationPlaying() {
        if (null != mCollapsibleView) {
            Animation anim = mCollapsibleView.getAnimation();
            if (null != anim && !anim.hasEnded()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 动画的监听器
     */
    private AnimationListener mCollapsibleAnimListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mIsOpened = !mIsOpened;
            if (mCollapsibleListener != null) {
                mCollapsibleListener.onCollapsibleFinished(mIsOpened);
            }
            
            if (null != mCollapsibleView) {
                mCollapsibleView.setAnimation(null);
            }
            
            onCollapsibleFinished(mIsOpened);
        }
    };

    /**
     * 伸缩动画
     */
    private class CollapsibleAnimation extends Animation {
        /** 开始的大小 */
        private int mFromSize;
        /** 结束的大小 */
        private int mToSize;
        /** 开始的Alpha */
        private float mFromAlpha;
        /** 结束的Alpha */
        private float mToAlpha;

        /**
         * 构造方法
         * 
         * @param fromSize 初始的大小
         * @param toSize 结束的大小
         * @param fromAlpha 初始的透明度
         * @param toAlpha 结束的透明度
         */
        public CollapsibleAnimation(int fromSize, int toSize, float fromAlpha, float toAlpha) {
            mFromSize = fromSize;
            mToSize = toSize;
            mFromAlpha = fromAlpha;
            mToAlpha = toAlpha;
        }
        
        @Override
        public boolean willChangeBounds() {
            return true;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (mCollapsibleView != null) {
                // 改变透明度
                final float alpha = mFromAlpha;
                t.setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
                
                // 改变大小
                final int fromSize = mFromSize;
                int size = (int) (fromSize + (mToSize - fromSize) * interpolatedTime);
                setCollapsibleViewSize(size);
                
                if (DEBUG) {
                    Log.d(TAG, "CollapsiblePanel#applyTransformation  mCollapsibleView size = " + size);
                }
            }
        }
    }
}
