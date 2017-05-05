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


package com.lee.sdk.widget.state;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 这个类是对页面的抽象，定义了页面的生命周期方法等，类似于Android系统的{@link android.app.Fragment}。
 * 
 * @author lihong06
 * @since 2014-6-9
 */
public abstract class ActivityState implements StateContext {
    /** DEBUG */
    private static final boolean DEBUG = com.lee.sdk.Configuration.DEBUG & true;
    /** TAG */
    private static final String TAG = "ActivityState";
    
    /** 是否初始化 */
    private boolean mIsInited = false;
    /** 是否已经销毁 */
    private boolean mDestroyed = false;
    /** 是否已经调用了{@link #finish()}方法 */
    private boolean mIsFinishing = false;
    /** 是否已经回到前台 */
    private boolean mResumed = false;
    /** Activity是否已经销毁 */
    private boolean mActivityDestroy = false;
    /** 传递的数据 */
    private Bundle mData;
    /** ActivityContext对象 */
    private ActivityContext mContext;
    /** 根View */
    private View mRootView;
    /** 设置的Intent */
    private Intent mIntent;
    /** 接收到的结果数据 */
    /*package*/ ResultEntry mReceivedResults;
    /** 结果数据 */
    /*package*/ ResultEntry mResult;
    
    /**
     * 对结果的封装
     * 
     * @author lihong06
     * @since 2014-7-6
     */
    protected static class ResultEntry {
        /** 请求code */
        public int requestCode;
        /** 结果code */
        public int resultCode = Activity.RESULT_CANCELED;
        /** 结果数据 */
        public Intent resultData;
    }
    
    /**
     * 创建View，通常这个方法中{@link Activity#onCreate()}或{@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}中调用。
     * 
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return view
     */
    protected abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
    
    /**
     * 得到当前页面的根View
     * 
     * @return 根View对旬
     */
    public View getRootView() {
        return mRootView;
    }
    
    /**
     * 设置根View
     *  
     * @param rootView 根View
     */
    protected void setRootView(View rootView) {
        mRootView = rootView;
    }
    
    /**
     * 设置页面切换的动画效果
     * 
     * @param enterAnim 进入页面的动画id
     * @param exitAnim 退出页面的动画id
     */
    public final void overridePendingTransition(int enterAnim, int exitAnim) {
        if (null == mContext) {
            if (DEBUG) {
                throw new IllegalStateException("The context is null, do you forget initialize this state?");
            }
            return;
        }
        
        mContext.getStateManager().overridePendingTransition(enterAnim, exitAnim);
    }
    
    @Override
    public void startState(Class<? extends ActivityState> klass) {
        startState(klass, null);
    }
    
    @Override
    public void startState(Class<? extends ActivityState> klass, Bundle data) {
        if (null == mContext) {
            if (DEBUG) {
                throw new IllegalStateException("The context is null, do you forget initialize this state?");
            }
            return;
        }
        
        mContext.getStateManager().startState(klass, data);
        //overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }
    
    @Override
    public void startStateForResult(Class<? extends ActivityState> klass, int requestCode) {
        startStateForResult(klass, requestCode, null);
    }
    
    @Override
    public void startStateForResult(Class<? extends ActivityState> klass, int requestCode, Bundle data) {
        if (null == mContext) {
            if (DEBUG) {
                throw new IllegalStateException("The context is null, do you forget initialize this state?");
            }
            return;
        }
        
        mContext.getStateManager().startStateForResult(klass, requestCode, data);
        //overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public void setStateResult(int resultCode, Intent data) {
        if (mResult == null) {
            return;
        }
        
        mResult.resultCode = resultCode;
        mResult.resultData = data;
    }
  
    @Override
    public void onCreate(Bundle data, Bundle storedState) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onCreate()----- class = " + this.getClass().getCanonicalName());
        }
    }
    
    /**
     * 页面创建成功，在这个方法里面，可能调用{@link #getRootView()}方法来得到当前页面的根View了。
     * 
     * @param storedState storedState
     */
    public void onStateCreated(Bundle storedState) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onStateCreated()----- class = " + this.getClass().getCanonicalName());
        }
    }
    
    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onResume()----- class = " + this.getClass().getCanonicalName());
        }
        
        mResumed = true;
        mDestroyed = false;
    }
    
    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onPause()----- class = " + this.getClass().getCanonicalName());
        }
        
        mResumed = false;
    }
    
    /**
     * 销毁View时调用
     */
    public void onDestroyView() {
        
    }
    
    @Override
    public void onDestroy() {
        mIsInited = false;
        mDestroyed = true;
        mResumed = false;
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onDestroy()----- class = " + this.getClass().getCanonicalName());
        }
    }
    
    @Override
    public void onActivityDestroy() {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onActivityDestroy()----- class = " + this.getClass().getCanonicalName());
        }
    }
    
    /**
     * 指定当前页面的启动模式。默认值为{@link android.content.pm.ActivityInfo#LAUNCH_MULTIPLE}
     * 
     * @return 返回值请参考
     * <li>{@link android.content.pm.ActivityInfo#LAUNCH_MULTIPLE}
     * <li>{@link android.content.pm.ActivityInfo#LAUNCH_SINGLE_TOP}
     * <li>{@link android.content.pm.ActivityInfo#LAUNCH_SINGLE_TASK}
     */
    public int getLaunchMode() {
        return ActivityInfo.LAUNCH_MULTIPLE;
    }
    
    /**
     * 当启动模式为{@link android.content.pm.ActivityInfo#LAUNCH_SINGLE_TOP}和
     * {@link android.content.pm.ActivityInfo#LAUNCH_SINGLE_TASK}时，再次启动当前页面时，不会再次创建实例，此时，这个方法
     * 将会被调用。
     * 
     * @param intent intent
     */
    public void onNewIntent(Intent intent) {
        
    }
    
    /**
     * 内存低时被调用
     */
    public void onLowMemory() {
        
    }
    
    /**
     * 结束当前页面
     */
    public void finish() {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#finish()----- class = " + this.getClass().getCanonicalName());
        }
        
        if (null == mContext) {
            throw new IllegalStateException("The context is null, do you forget initialize this state?");
        }
        
        mIsFinishing = true;
        mContext.getStateManager().finishState(this);
        //overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }
    
    /**
     * 返回键按下时调用
     */
    public void onBackPressed() {
        View rootView = getRootView();
        if (null != rootView) {
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        } else {
            finish();
        }
    }
    
    /**
     * 当前页面是否已经初始化
     * 
     * @return true/false
     */
    public boolean isInitialized() {
        return mIsInited;
    }
    
    /**
     * 当前页面是否已经销毁
     * 
     * @return true/false
     */
    public boolean isDestroyed() {
        return mDestroyed;
    }
    
    /**
     * 当前页面是否已经显示
     * 
     * @return true/false
     */
    public boolean isResumed() {
        return mResumed;
    }
    
    /**
     * 当前页面是否已经结束
     * 
     * @return true/false
     */
    public boolean isFinishing() {
        return mIsFinishing;
    }
    
    /**
     * 得到设置的数据
     * 
     * @return bundle
     */
    public Bundle getData() {
        return mData;
    }
    
    @Override
    public Intent getIntent() {
        return mIntent;
    }
    
    @Override
    public void setIntent(Intent intent) {
        mIntent = intent;
    }
    
    /**
     * 设置当前页面的可见性
     * 
     * @param visibility 
     * <li>{@link android.view.View#VISIBLE}
     * <li>{@link android.view.View#INVISIBLE}
     * <li>{@link android.view.View#GONE}
     */
    public void setVisibility(int visibility) {
        View rootView = getRootView();
        if (null != rootView) {
            rootView.setVisibility(visibility);
        }
    }
    
    @Override
    public Context getContext() {
        if (null == mContext) {
            if (DEBUG) {
                throw new IllegalStateException("The context is null, do you forget initialize this state?");
            }
            return null;
        }
        
        return mContext.getAndroidContext();
    }
    
    /**
     * 得到{@link ActivityContext}实例
     * 
     * @return {@link ActivityContext}实例
     */
    public ActivityContext getActivityContext() {
        return mContext;
    }
    
    /**
     * 得到当前页面的管理器
     * 
     * @return StateManager
     */
    public StateManager getStateManager() {
        if (null != mContext) {
            return mContext.getStateManager();
        }
        
        return null;
    }
    
    /**
     * 焦点变化时调用
     * 
     * @param hasFocus hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onWindowFocusChanged()----- class = " + this.getClass().getCanonicalName() 
                    + ", hasFocus = " + hasFocus);
        }
    }
    
    /**
     * 状态改变时调用
     * 
     * @param config config
     */
    public void onConfigurationChanged(Configuration config) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onConfigurationChanged()----- class = " + this.getClass().getCanonicalName());
        }
    }

    /**
     * 保存当前页面的状态
     * 
     * @param outState outState
     */
    public void onSaveState(Bundle outState) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onSaveState()----- class = " + this.getClass().getCanonicalName());
        }
    }
    
    /**
     * 恢复当前页面的状态
     * 
     * @param inState inState
     */
    public void onRestoreState(Bundle inState) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onRestoreState()----- class = " + this.getClass().getCanonicalName());
        }
    }

    /**
     * 收到结果时调用
     * 
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data
     */
    public void onStateResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#onStateResult()----- class = " + this.getClass().getCanonicalName());
        }
    }
    
    /**
     * 按键按下时调用
     * 
     * @param keyCode keyCode
     * @param event event
     * @return true/false
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            onBackPressed();
            return true;
        }
        
        return false;
    }
    
    /**
     * 按键释放时调用
     * 
     * @param keyCode keyCode
     * @param event event
     * @return true/false
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }
    
    /**
     * 暂停
     */
    /*package*/ void pause() {
        setVisibility(View.GONE);
        onPause();
    }
    
    /**
     * 恢复
     */
    /*package*/ void resume() {
        ResultEntry entry = mReceivedResults;
        if (entry != null) {
            mReceivedResults = null;
            onStateResult(entry.requestCode, entry.resultCode, entry.resultData);
        }
        
        setVisibility(View.VISIBLE);
        onResume();
    }
    
    /**
     * Activity销毁时调用
     */
    public void activityDestroy() {
        if (!mActivityDestroy) {
            mActivityDestroy = true;
            onActivityDestroy();
        }
    }
    
    /**
     * 初始化
     * 
     * @param context context
     * @param data data
     */
    public void initialize(ActivityContext context, Bundle data) {
        if (null == context) {
            if (DEBUG) {
                throw new IllegalStateException("The context is null, do you forget initialize this state?");
            }
            return;
        }
        
        mIsInited = true;
        mContext = context;
        mData = data;
    }
    
    /**
     * 创建View，方法内部会调用{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}方法。
     * 
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return 根view
     */
    public View createView(ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG, "ActivityState#createView ===== class = " + this.getClass().getCanonicalName());
        }
        
        if (null == mContext) {
            throw new IllegalStateException("The context is null, do you forget initialize this state?");
        }
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = onCreateView(inflater, container, savedInstanceState);
        if (null == view) {
            throw new IllegalStateException("The onCreateView can NOT return null view.");
        }
        
        setRootView(view);
        
        return view;
    }
}
