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

package com.lee.sdk.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

/**
 * ClipboardManager在API 11前后接口发生改变且无法兼容，WrappedClipboardManager类 用于屏蔽此差异性
 * 
 * @author lihong06
 */
public abstract class WrappedClipboardManager {
    /** 程序上下文，用于获取系统service */
    protected static Context sTheApp;

    /**
     * 将文本拷贝至剪贴板
     * 
     * @param text 文本
     */
    public abstract void setText(CharSequence text);

    /**
     * 根据平台版本获取可用的ClipboardManager
     * 
     * @param context 程序实例
     * @return 当前平台的剪贴板实例
     */
    public static WrappedClipboardManager newInstance(Context context) {
        sTheApp = context.getApplicationContext();

        if (APIUtils.hasHoneycomb()) {
            return new HoneycombClipboardManager();
        } else {
            return new OldClipboardManager();
        }
    }

    /**
     * Android 3.0(API 11)以下平台的剪贴板
     */
    private static class OldClipboardManager extends WrappedClipboardManager {

        /** 单实例引用 */
        @SuppressWarnings("deprecation")
        private static android.text.ClipboardManager sInstance = null;

        /** 构造方法 */
        @SuppressWarnings("deprecation")
        public OldClipboardManager() {
            sInstance = (android.text.ClipboardManager) sTheApp
                    .getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void setText(CharSequence text) {
            sInstance.setText(text);
        }

    }

    /**
     * Android 3.0(API 11)以上平台的剪贴板
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class HoneycombClipboardManager extends WrappedClipboardManager {
        /** 单实例引用 */
        private static android.content.ClipboardManager sInstance = null;
        /** 单实例引用数据 */
        private static android.content.ClipData sClipData = null;

        /** 构造方法 */
        @SuppressLint("ServiceCast")
        public HoneycombClipboardManager() {
            sInstance = (android.content.ClipboardManager) sTheApp
                    .getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        }

        @Override
        public void setText(CharSequence text) {
            sClipData = android.content.ClipData
                    .newPlainText(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN, text);
            sInstance.setPrimaryClip(sClipData);
        }

    }
}
