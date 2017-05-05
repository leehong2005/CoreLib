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

import com.lee.sdk.NoProGuard;

import android.content.Context;

/**
 * 页面管理框架的上下文.
 * 
 * <p>
 * 通过它能得到当前Activity的Context，还可以得到StateManager的实例，它与{@link #ActivityState}类建立联系，就与Android系统中
 * 的Context一样，只是功能被弱化了。
 * </p>
 * 
 * @author lihong06
 * @since 2014-6-10
 */
public interface ActivityContext extends NoProGuard {
    /**
     * 得到Android系统的Context实例
     * 
     * @return context
     */
    /*public*/ Context getAndroidContext();
    
    /**
     * 得到页面管理器对象
     * 
     * @return {@link #StateManager}实例
     */
    /*public*/ StateManager getStateManager();
}
