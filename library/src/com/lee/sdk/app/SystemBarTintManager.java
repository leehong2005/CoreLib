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

//CHECKSTYLE:OFF

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import com.lee.sdk.utils.APIUtils;

/**
 * 这个类定义了设置系统状态栏的颜色的接口，通常用于沉浸式的用户体验，例如当前界面的ActionBar是红色，那么状态栏也是红色，或者
 * 是与红色相近的颜色。
 * 
 * <p>请调用{@link #setStatusBarTintColor(int)}接口来设置状态栏的颜色</p>
 * 
 * @author lihong06
 * @since 2015-4-25
 */
public abstract class SystemBarTintManager {
    /**
     * Window flag: request a translucent status bar with minimal system-provided
     * background protection.
     *
     * <p>This flag can be controlled in your theme through the
     * {@link android.R.attr#windowTranslucentStatus} attribute; this attribute
     * is automatically set for you in the standard translucent decor themes
     * such as
     * {@link android.R.style#Theme_Holo_NoActionBar_TranslucentDecor},
     * {@link android.R.style#Theme_Holo_Light_NoActionBar_TranslucentDecor},
     * {@link android.R.style#Theme_DeviceDefault_NoActionBar_TranslucentDecor}, and
     * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor}.</p>
     *
     * <p>When this flag is enabled for a window, it automatically sets
     * the system UI visibility flags {@link View#SYSTEM_UI_FLAG_LAYOUT_STABLE} and
     * {@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN}.</p>
     */
    public static final int FLAG_TRANSLUCENT_STATUS = 0x04000000;
    
    /**
     * Window flag: request a translucent navigation bar with minimal system-provided
     * background protection.
     *
     * <p>This flag can be controlled in your theme through the
     * {@link android.R.attr#windowTranslucentNavigation} attribute; this attribute
     * is automatically set for you in the standard translucent decor themes
     * such as
     * {@link android.R.style#Theme_Holo_NoActionBar_TranslucentDecor},
     * {@link android.R.style#Theme_Holo_Light_NoActionBar_TranslucentDecor},
     * {@link android.R.style#Theme_DeviceDefault_NoActionBar_TranslucentDecor}, and
     * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor}.</p>
     *
     * <p>When this flag is enabled for a window, it automatically sets
     * the system UI visibility flags {@link View#SYSTEM_UI_FLAG_LAYOUT_STABLE} and
     * {@link View#SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION}.</p>
     */
    public static final int FLAG_TRANSLUCENT_NAVIGATION = 0x08000000;
    
    /**
     * Flag indicating whether this window requests a translucent status bar. Corresponds to FLAG_TRANSLUCENT_STATUS.
     */
    public static final int windowTranslucentStatus = 16843759;
    
    /**
     * Flag indicating whether this window requests a translucent navigation bar. 
     * Corresponds to FLAG_TRANSLUCENT_NAVIGATION
     */
    public static final int windowTranslucentNavigation = 16843760;

    
    /**
     * Apply the specified color tint to the system status bar.
     *
     * @param color The color of the background tint.
     */
    public abstract void setStatusBarTintColor(int color);
    
    /**
     * Enable tinting of the system status bar.
     *
     * If the platform is running Jelly Bean or earlier, or translucent system
     * UI modes have not been enabled in either the theme or via window flags,
     * then this method does nothing.
     *
     * @param enabled True to enable tinting, false to disable it (default).
     */
    public abstract void setStatusBarTintEnabled(boolean enabled);
    
    /**
     * 创建一个管理系统状态栏颜色的管理类
     * 
     * @param activity 当前activity
     * @return SystemBarTintManager类实例
     */
    public static SystemBarTintManager newInstance(Activity activity) {
        SystemBarTintManager instance;
        
        // Lollipop
        if (Build.VERSION.SDK_INT >= 21) {
            instance = new SystemBarTintManagerLollipop(activity);
        } else {
            setTranslucentStatus(activity, true);
            SystemBarTintManagerKitKat tintManagerKitKat = new SystemBarTintManagerKitKat(activity);
            tintManagerKitKat.setStatusBarTintEnabled(true);
            instance = tintManagerKitKat;
        }
        
        return instance;
    }
    
    private static void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = FLAG_TRANSLUCENT_STATUS; //WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    
    /**
     * For lollipop
     */
    public static class SystemBarTintManagerLollipop extends SystemBarTintManager {
        private Activity mActivity;
        private boolean mStatusBarTintEnabled;
        
        public SystemBarTintManagerLollipop(Activity activity) {
            mActivity = activity;
        }
        
        @Override
        public void setStatusBarTintColor(int color) {
            if (!mStatusBarTintEnabled) {
                return;
            }
            final Activity activity = mActivity;
            
            activity.getWindow().addFlags(0x80000000);
            try {
                Method method = Window.class.getDeclaredMethod("setStatusBarColor", int.class);
                method.invoke(activity.getWindow(), color);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setStatusBarTintEnabled(boolean enabled) {
            mStatusBarTintEnabled = enabled;
        }
    }
    
    /**
     * Class to manage status and navigation bar tint effects when using KitKat 
     * translucent system UI modes.
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static class SystemBarTintManagerKitKat extends SystemBarTintManager {

        static {
            // Android allows a system property to override the presence of the navigation bar.
            // Used by the emulator.
            // See https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L1076
            if (APIUtils.hasKitKat()) {
                try {
                    Class c = Class.forName("android.os.SystemProperties");
                    Method m = c.getDeclaredMethod("get", String.class);
                    m.setAccessible(true);
                    sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
                } catch (Exception e) {
                    sNavBarOverride = null;
                }
            }
        }

        /**
         * The default system bar tint color value.
         */
        public static final int DEFAULT_TINT_COLOR = 0x99000000;

        private static String sNavBarOverride;

        private final SystemBarConfig mConfig;
        private boolean mStatusBarAvailable;
        private boolean mNavBarAvailable;
        private boolean mStatusBarTintEnabled;
        private boolean mNavBarTintEnabled;
        private View mStatusBarTintView;
        private View mNavBarTintView;
        /**
         * Constructor. Call this in the host activity onCreate method after its
         * content view has been set. You should always create new instances when
         * the host activity is recreated.
         *
         * @param activity The host activity.
         */
        @TargetApi(19)
        public SystemBarTintManagerKitKat(Activity activity) {

            Window win = activity.getWindow();
            ViewGroup decorViewGroup = (ViewGroup) win.getDecorView();

            if (APIUtils.hasKitKat()) {
                // check theme attrs
                int[] attrs = {SystemBarTintManager.windowTranslucentStatus,
                        SystemBarTintManager.windowTranslucentNavigation};
                TypedArray a = activity.obtainStyledAttributes(attrs);
                try {
                    mStatusBarAvailable = a.getBoolean(0, false);
                    mNavBarAvailable = a.getBoolean(1, false);
                } finally {
                    a.recycle();
                }

                // check window flags
                WindowManager.LayoutParams winParams = win.getAttributes();
                int bits = FLAG_TRANSLUCENT_STATUS; //WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                if ((winParams.flags & bits) != 0) {
                    mStatusBarAvailable = true;
                }
                bits = FLAG_TRANSLUCENT_NAVIGATION; //WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                if ((winParams.flags & bits) != 0) {
                    mNavBarAvailable = true;
                }
            }

            mConfig = new SystemBarConfig(activity, mStatusBarAvailable, mNavBarAvailable);
            // device might not have virtual navigation keys
            if (!mConfig.hasNavigtionBar()) {
                mNavBarAvailable = false;
            }

            if (mStatusBarAvailable) {
                setupStatusBarView(activity, decorViewGroup);
            }
            if (mNavBarAvailable) {
                setupNavBarView(activity, decorViewGroup);
            }

        }

        /**
         * Enable tinting of the system status bar.
         *
         * If the platform is running Jelly Bean or earlier, or translucent system
         * UI modes have not been enabled in either the theme or via window flags,
         * then this method does nothing.
         *
         * @param enabled True to enable tinting, false to disable it (default).
         */
        public void setStatusBarTintEnabled(boolean enabled) {
            mStatusBarTintEnabled = enabled;
            if (mStatusBarAvailable) {
                mStatusBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
            }
        }

        /**
         * Enable tinting of the system navigation bar.
         *
         * If the platform does not have soft navigation keys, is running Jelly Bean
         * or earlier, or translucent system UI modes have not been enabled in either
         * the theme or via window flags, then this method does nothing.
         *
         * @param enabled True to enable tinting, false to disable it (default).
         */
        public void setNavigationBarTintEnabled(boolean enabled) {
            mNavBarTintEnabled = enabled;
            if (mNavBarAvailable) {
                mNavBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
            }
        }

        /**
         * Apply the specified color tint to all system UI bars.
         *
         * @param color The color of the background tint.
         */
        public void setTintColor(int color) {
            setStatusBarTintColor(color);
            setNavigationBarTintColor(color);
        }

        /**
         * Apply the specified drawable or color resource to all system UI bars.
         *
         * @param res The identifier of the resource.
         */
        public void setTintResource(int res) {
            setStatusBarTintResource(res);
            setNavigationBarTintResource(res);
        }

        /**
         * Apply the specified drawable to all system UI bars.
         *
         * @param drawable The drawable to use as the background, or null to remove it.
         */
        public void setTintDrawable(Drawable drawable) {
            setStatusBarTintDrawable(drawable);
            setNavigationBarTintDrawable(drawable);
        }

        /**
         * Apply the specified alpha to all system UI bars.
         *
         * @param alpha The alpha to use
         */
        public void setTintAlpha(float alpha) {
            setStatusBarAlpha(alpha);
            setNavigationBarAlpha(alpha);
        }

        /**
         * Apply the specified color tint to the system status bar.
         *
         * @param color The color of the background tint.
         */
        public void setStatusBarTintColor(int color) {
            if (mStatusBarAvailable) {
                mStatusBarTintView.setBackgroundColor(color);
            }
        }

        /**
         * Apply the specified drawable or color resource to the system status bar.
         *
         * @param res The identifier of the resource.
         */
        public void setStatusBarTintResource(int res) {
            if (mStatusBarAvailable) {
                mStatusBarTintView.setBackgroundResource(res);
            }
        }

        /**
         * Apply the specified drawable to the system status bar.
         *
         * @param drawable The drawable to use as the background, or null to remove it.
         */
        @SuppressWarnings("deprecation")
        public void setStatusBarTintDrawable(Drawable drawable) {
            if (mStatusBarAvailable) {
                mStatusBarTintView.setBackgroundDrawable(drawable);
            }
        }

        /**
         * Apply the specified alpha to the system status bar.
         *
         * @param alpha The alpha to use
         */
        @TargetApi(11)
        public void setStatusBarAlpha(float alpha) {
            if (mStatusBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mStatusBarTintView.setAlpha(alpha);
            }
        }

        /**
         * Apply the specified color tint to the system navigation bar.
         *
         * @param color The color of the background tint.
         */
        public void setNavigationBarTintColor(int color) {
            if (mNavBarAvailable) {
                mNavBarTintView.setBackgroundColor(color);
            }
        }

        /**
         * Apply the specified drawable or color resource to the system navigation bar.
         *
         * @param res The identifier of the resource.
         */
        public void setNavigationBarTintResource(int res) {
            if (mNavBarAvailable) {
                mNavBarTintView.setBackgroundResource(res);
            }
        }

        /**
         * Apply the specified drawable to the system navigation bar.
         *
         * @param drawable The drawable to use as the background, or null to remove it.
         */
        @SuppressWarnings("deprecation")
        public void setNavigationBarTintDrawable(Drawable drawable) {
            if (mNavBarAvailable) {
                mNavBarTintView.setBackgroundDrawable(drawable);
            }
        }

        /**
         * Apply the specified alpha to the system navigation bar.
         *
         * @param alpha The alpha to use
         */
        @TargetApi(11)
        public void setNavigationBarAlpha(float alpha) {
            if (mNavBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mNavBarTintView.setAlpha(alpha);
            }
        }

        /**
         * Get the system bar configuration.
         *
         * @return The system bar configuration for the current device configuration.
         */
        public SystemBarConfig getConfig() {
            return mConfig;
        }

        /**
         * Is tinting enabled for the system status bar?
         *
         * @return True if enabled, False otherwise.
         */
        public boolean isStatusBarTintEnabled() {
            return mStatusBarTintEnabled;
        }

        /**
         * Is tinting enabled for the system navigation bar?
         *
         * @return True if enabled, False otherwise.
         */
        public boolean isNavBarTintEnabled() {
            return mNavBarTintEnabled;
        }

        private void setupStatusBarView(Context context, ViewGroup decorViewGroup) {
            mStatusBarTintView = new View(context);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getStatusBarHeight());
            params.gravity = Gravity.TOP;
            if (mNavBarAvailable && !mConfig.isNavigationAtBottom()) {
                params.rightMargin = mConfig.getNavigationBarWidth();
            }
            mStatusBarTintView.setLayoutParams(params);
            mStatusBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
            mStatusBarTintView.setVisibility(View.GONE);
            decorViewGroup.addView(mStatusBarTintView);
        }

        @SuppressLint("RtlHardcoded")
        private void setupNavBarView(Context context, ViewGroup decorViewGroup) {
            mNavBarTintView = new View(context);
            LayoutParams params;
            if (mConfig.isNavigationAtBottom()) {
                params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getNavigationBarHeight());
                params.gravity = Gravity.BOTTOM;
            } else {
                params = new LayoutParams(mConfig.getNavigationBarWidth(), LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.RIGHT;
            }
            mNavBarTintView.setLayoutParams(params);
            mNavBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
            mNavBarTintView.setVisibility(View.GONE);
            decorViewGroup.addView(mNavBarTintView);
        }
        
        /**
         * Class which describes system bar sizing and other characteristics for the current
         * device configuration.
         *
         */
        public static class SystemBarConfig {

            private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
            private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
            private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
            private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
            private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

            private final boolean mTranslucentStatusBar;
            private final boolean mTranslucentNavBar;
            private final int mStatusBarHeight;
            private final int mActionBarHeight;
            private final boolean mHasNavigationBar;
            private final int mNavigationBarHeight;
            private final int mNavigationBarWidth;
            private final boolean mInPortrait;
            private final float mSmallestWidthDp;

            private SystemBarConfig(Activity activity, boolean translucentStatusBar, boolean traslucentNavBar) {
                Resources res = activity.getResources();
                mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                mSmallestWidthDp = getSmallestWidthDp(activity);
                mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
                mActionBarHeight = getActionBarHeight(activity);
                mNavigationBarHeight = getNavigationBarHeight(activity);
                mNavigationBarWidth = getNavigationBarWidth(activity);
                mHasNavigationBar = (mNavigationBarHeight > 0);
                mTranslucentStatusBar = translucentStatusBar;
                mTranslucentNavBar = traslucentNavBar;
            }

            @TargetApi(14)
            private int getActionBarHeight(Context context) {
                int result = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    TypedValue tv = new TypedValue();
                    context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
                    result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
                }
                return result;
            }

            @TargetApi(14)
            private int getNavigationBarHeight(Context context) {
                Resources res = context.getResources();
                int result = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (hasNavBar(context)) {
                        String key;
                        if (mInPortrait) {
                            key = NAV_BAR_HEIGHT_RES_NAME;
                        } else {
                            key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                        }
                        return getInternalDimensionSize(res, key);
                    }
                }
                return result;
            }

            @TargetApi(14)
            private int getNavigationBarWidth(Context context) {
                Resources res = context.getResources();
                int result = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (hasNavBar(context)) {
                        return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
                    }
                }
                return result;
            }

            @TargetApi(14)
            private boolean hasNavBar(Context context) {
                Resources res = context.getResources();
                int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
                if (resourceId != 0) {
                    boolean hasNav = res.getBoolean(resourceId);
                    // check override flag (see static block)
                    if ("1".equals(sNavBarOverride)) {
                        hasNav = false;
                    } else if ("0".equals(sNavBarOverride)) {
                        hasNav = true;
                    }
                    return hasNav;
                } else { // fallback
                    return !ViewConfiguration.get(context).hasPermanentMenuKey();
                }
            }

            private int getInternalDimensionSize(Resources res, String key) {
                int result = 0;
                int resourceId = res.getIdentifier(key, "dimen", "android");
                if (resourceId > 0) {
                    result = res.getDimensionPixelSize(resourceId);
                }
                return result;
            }

            @SuppressLint("NewApi")
            private float getSmallestWidthDp(Activity activity) {
                DisplayMetrics metrics = new DisplayMetrics();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                } else {
                    // TODO this is not correct, but we don't really care pre-kitkat
                    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                }
                float widthDp = metrics.widthPixels / metrics.density;
                float heightDp = metrics.heightPixels / metrics.density;
                return Math.min(widthDp, heightDp);
            }

            /**
             * Should a navigation bar appear at the bottom of the screen in the current
             * device configuration? A navigation bar may appear on the right side of
             * the screen in certain configurations.
             *
             * @return True if navigation should appear at the bottom of the screen, False otherwise.
             */
            public boolean isNavigationAtBottom() {
                return (mSmallestWidthDp >= 600 || mInPortrait);
            }

            /**
             * Get the height of the system status bar.
             *
             * @return The height of the status bar (in pixels).
             */
            public int getStatusBarHeight() {
                return mStatusBarHeight;
            }

            /**
             * Get the height of the action bar.
             *
             * @return The height of the action bar (in pixels).
             */
            public int getActionBarHeight() {
                return mActionBarHeight;
            }

            /**
             * Does this device have a system navigation bar?
             *
             * @return True if this device uses soft key navigation, False otherwise.
             */
            public boolean hasNavigtionBar() {
                return mHasNavigationBar;
            }

            /**
             * Get the height of the system navigation bar.
             *
             * @return The height of the navigation bar (in pixels). If the device does not have
             * soft navigation keys, this will always return 0.
             */
            public int getNavigationBarHeight() {
                return mNavigationBarHeight;
            }

            /**
             * Get the width of the system navigation bar when it is placed vertically on the screen.
             *
             * @return The width of the navigation bar (in pixels). If the device does not have
             * soft navigation keys, this will always return 0.
             */
            public int getNavigationBarWidth() {
                return mNavigationBarWidth;
            }

            /**
             * Get the layout inset for any system UI that appears at the top of the screen.
             *
             * @param withActionBar True to include the height of the action bar, False otherwise.
             * @return The layout inset (in pixels).
             */
            public int getPixelInsetTop(boolean withActionBar) {
                return (mTranslucentStatusBar ? mStatusBarHeight : 0) + (withActionBar ? mActionBarHeight : 0);
            }

            /**
             * Get the layout inset for any system UI that appears at the bottom of the screen.
             *
             * @return The layout inset (in pixels).
             */
            public int getPixelInsetBottom() {
                if (mTranslucentNavBar && isNavigationAtBottom()) {
                    return mNavigationBarHeight;
                } else {
                    return 0;
                }
            }

            /**
             * Get the layout inset for any system UI that appears at the right of the screen.
             *
             * @return The layout inset (in pixels).
             */
            public int getPixelInsetRight() {
                if (mTranslucentNavBar && !isNavigationAtBottom()) {
                    return mNavigationBarWidth;
                } else {
                    return 0;
                }
            }
        }
    }
}
//CHECKSTYLE:ON
