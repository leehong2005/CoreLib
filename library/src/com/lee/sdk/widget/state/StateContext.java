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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lee.sdk.NoProGuard;

/**
 * 定义了每一个页面提供的基础接口能力
 * 
 * @author lihong06
 * @since 2014-6-10
 */
public interface StateContext extends NoProGuard {
    /**
     * 得到Context对象
     * 
     * @return Context
     */
    Context getContext();
    
    /**
     * 得到当前页面的Intent实例
     * 
     * @return intent
     * @see #setIntent(Intent)
     */
    Intent getIntent();
    
    /**
     * 给当前页面设置设置intent
     * 
     * @param intent intent
     * @see #getIntent()
     */
    void setIntent(Intent intent);
    
    /**
     * 设置当面的结果。
     * 
     * @param resultCode resultCode
     * @param data data
     * @see #startStateForResult(Class, int)
     */
    void setStateResult(int resultCode, Intent data);
    
    /**
     * 启动指定的页面
     * 
     * @param klass klass
     */
    void startState(Class<? extends ActivityState> klass);
    
    /**
     * 启动指定的页面
     * 
     * @param klass klass
     * @param data 传递的数据
     */
    void startState(Class<? extends ActivityState> klass, Bundle data);
    
    /**
     * 启动指定的页面，在这个页面结束后，前一个页面将会收到数据，
     * 类似于{@link android.app.Activity#startActivityForResult(Intent, int)}。
     * 
     * @param klass klass
     * @param requestCode requestCode
     */
    void startStateForResult(Class<? extends ActivityState> klass, int requestCode);
    
    /**
     * 启动指定的页面，在这个页面结束后，前一个页面将会收到数据，
     * 类似于{@link android.app.Activity#startActivityForResult(Intent, int)}。
     * 
     * @param klass klass
     * @param requestCode requestCode
     * @param data 传递的数据
     */
    void startStateForResult(Class<? extends ActivityState> klass, int requestCode, Bundle data);
    
    /**
     * 在调用了startState方法之后会被调用，表示当前页面已经创建
     * 
     * @param data data
     * @param storedState storedState
     */
    void onCreate(Bundle data, Bundle storedState);
    
    /**
     * 页面显示时调用
     */
    void onResume();
    
    /**
     * 页面隐藏时调用
     */
    void onPause();
    
    /**
     * 页面被销毁，派生类重写时super的方法在最前面调用
     */
    void onDestroy();
    
    /**
     * 当Activity被销毁时调用，典型的情况是{@link android.app.Activity#onDestroy()}被调用时。
     */
    void onActivityDestroy();
}
