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

package com.lee.sdk.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * The base application class.
 * 
 * @author Li Hong
 * @date 2012/11/12
 */
public class BaseApplication extends Application {
    /**The application context.*/
    private static Context mAppContext = null;
    /**The top activity.*/
    private Activity mTopActivity = null;
    /** 主线程的Handler */
    private static Handler sMainHandler;

    /**
     * This static method returns the application context.
     * 
     * @return The application context.
     */
    public static Context getAppContext() {
        return mAppContext;
    }

    /**
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mAppContext = this.getApplicationContext();
    }

    /**
     * @param topActivity the m_topActivity to set
     */
    public void setTopActivity(Activity topActivity) {
        this.mTopActivity = topActivity;
    }

    /**
     * @return the m_topActivity
     */
    public Activity getTopActivity() {
        return mTopActivity;
    }

    /**
     * 得到关联到主线程的Handler
     * 
     * <p>
     * 注意：这个Handler通常只是用来执行post操作。
     * </p>
     * 
     * @return handler
     */
    public static Handler getMainHandler() {
        if (null == sMainHandler) {
            synchronized (BaseApplication.class) {
                if (null == sMainHandler) {
                    sMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }

        return sMainHandler;
    }
}
