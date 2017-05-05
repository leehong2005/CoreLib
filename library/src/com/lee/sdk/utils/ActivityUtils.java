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

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lee.sdk.Configuration;

/**
 * ActivityUtils
 * 
 * @author lihong06
 * @since 2013-12-19
 */
public final class ActivityUtils {
    /** Log TAG */
    private static final String TAG = "ActivityUtils";
    /** Log switch */
    private static final boolean DEBUG = Configuration.DEBUG & true;

    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private ActivityUtils() {
    }

    /**
     * 安全启动应用程序，截获Exception。
     * 
     * @param activity Activity
     * @param intent Intent
     * @return true: Start Activity success; false: Start Activity failed.
     * */
    public static boolean startActivitySafely(Activity activity, Intent intent) {
        return startActivitySafely(activity, intent, true);
    }

    /**
     * 安全启动应用程序，截获Exception。
     * 
     * @param activity activity
     * @param intent Intent
     * @param newTask 是否添加Intent.FLAG_ACTIVITY_NEW_TASK
     * @return true: Start Activity success; false: Start Activity failed.
     */
    public static boolean startActivitySafely(Activity activity, Intent intent, boolean newTask) {
        boolean ret = false;
        try {
            if (newTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            activity.startActivity(intent);
            ret = true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "ActivityNotFoundException: ", e);
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                Log.e(TAG, "SecurityException: ", e);
            }
            if (DEBUG) {
                Log.e(TAG, "Launcher does not have the permission to launch " + intent
                        + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                        + "or use the exported attribute for this activity.", e);
            }
        }
        return ret;
    }

    /**
     * 安全启动应用程序，截获Exception。 必须在主线程被调用
     * 
     * @param context context
     * @param intent Intent
     * @return 是否成功启动Activity。
     * */
    public static boolean startActivitySafely(Context context, Intent intent) {
        boolean ret = false;
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ret = true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "ActivityNotFoundException: ", e);
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Exception: ", e);
            }
        }
        return ret;
    }

    /**
     * 安全启动应用程序，截获Exception，并返回是否成功启动。
     * 
     * @param context Context.
     * @param packageName 包名.
     * @param activityName Activity全名（加上包名前缀）.
     * @return 是否成功启动Activity。
     */
    public static boolean startActivitySafely(Context context, String packageName, String activityName) {
        boolean result = false;
        if (packageName != null && activityName != null) {
            ComponentName component = new ComponentName(packageName, activityName);
            result = startActivitySafely(context, component);
        }

        return result;
    }

    /**
     * @Description: startActivityForResult的安全方法，找不到Activity提示toast
     * @param context context
     * @param intent intent，不能有NEW_TASK的flag
     * @param requestCode intent的请求码
     */
    public static boolean startActivityForResultSafely(Context context, Intent intent, int requestCode) {
        boolean ret = false;
        try {
            ((Activity) context).startActivityForResult(intent, requestCode);
            ret = true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "Exception: ", e);
            }
        }
        return ret;
    }

    /**
     * 安全启动应用程序，截获Exception，并返回是否成功启动。
     * 
     * @param context Context.
     * @param component 组件名，由包名和Activity全名（加上包名前缀）共同生成.
     * @return 是否成功启动Activity。
     */
    public static boolean startActivitySafely(Context context, ComponentName component) {
        boolean result = false;
        if (component != null) {
            Intent intent = new Intent();
            intent.setComponent(component);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                result = true;
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Exception: ", e);
                }
            }
        }

        return result;
    }

    /**
     * 启动不确定的Activity。该函数主要用来启动诸如闹钟日历这种无标准intent的Activity。
     * 如果通过默认的包名和Activity名启动失败，则扫描应用程序，通过包名后缀和Activity后缀来匹配， 直到启动成功或者扫描结束为止。 注意，为了安全起见，目前只扫描内置的应用程序
     * 。
     * 
     * @param context Context.
     * @param defaultPackageNames 默认的包名集合。
     * @param defaultActivityNames 对应的默认Activity名集合。
     * @param packagePostfix 包名后缀。
     * @param activityPostfix Activity后缀（请加上"."）。
     * @param packageSaveKey 用来保存成功启动的组件包名。
     * @param activitySaveKey 用来保存成功启动的组件Activity名。
     * @return 如果能成功启动某个Activity，则返回它的组件名；否则返回null.
     */
    public static ComponentName startUncertainActivitySafely(Context context, String[] defaultPackageNames,
            String[] defaultActivityNames, String packagePostfix, String activityPostfix, String packageSaveKey,
            String activitySaveKey) {
        ComponentName result = null;

        if (defaultPackageNames == null || defaultActivityNames == null) {
            return result;
        }

        int length = defaultPackageNames.length < defaultActivityNames.length ? defaultPackageNames.length
                : defaultActivityNames.length;

        ComponentName[] components = new ComponentName[length];
        for (int i = 0; i < length; i++) {
            components[i] = new ComponentName(defaultPackageNames[i], defaultActivityNames[i]);
        }

        result = startUncertainActivitySafely(context, components, packagePostfix, activityPostfix, packageSaveKey,
                activitySaveKey);

        return result;
    }

    /**
     * 启动不确定的Activity。该函数主要用来启动诸如闹钟日历这种无标准intent的Activity。
     * 如果通过默认的包名和Activity名启动失败，则扫描应用程序，通过包名后缀和Activity后缀来匹配， 直到启动成功或者扫描结束为止。 注意，为了安全起见，目前只扫描内置的应用程序
     * 。
     * 
     * @param context Context.
     * @param defaultComponents 默认的组件名集合。
     * @param packagePostfix 包名后缀。
     * @param activityPostfix Activity后缀（请加上"."）。
     * @param packageSaveKey 用来保存成功启动的组件包名。
     * @param activitySaveKey 用来保存成功启动的组件Activity名。
     * @return 如果能成功启动某个Activity，则返回它的组件名；否则返回null.
     */
    public static ComponentName startUncertainActivitySafely(Context context, ComponentName[] defaultComponents,
            String packagePostfix, String activityPostfix, String packageSaveKey, String activitySaveKey) {
        ComponentName result = null;

        String savePackageName = null;
        String saveActivityName = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        savePackageName = preferences.getString(packageSaveKey, null);
        saveActivityName = preferences.getString(activitySaveKey, null);

        if (savePackageName != null && saveActivityName != null
                && startActivitySafely(context, savePackageName, saveActivityName)) {
            result = new ComponentName(savePackageName, saveActivityName);
            return result;
        }

        if (defaultComponents != null) {
            for (ComponentName componentName : defaultComponents) {
                if (startActivitySafely(context, componentName)) {
                    result = componentName;
                    break;
                }
            }
        }

        if (result == null) {
            List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);
            for (PackageInfo pi : packageInfos) { // 是内置应用，且包名以"clock"结束
                if (((pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP || (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                        && pi.packageName.endsWith(packagePostfix)) {

                    ComponentName componentName = new ComponentName(pi.packageName, pi.packageName + activityPostfix);
                    // 有Activity名以"AlarmClock"结束，并能成功启动
                    if (startActivitySafely(context, componentName)) {
                        result = componentName;
                        break;
                    }
                }
            }
        }

        if (result != null) {
            Editor editor = preferences.edit();
            editor.putString(packageSaveKey, result.getPackageName());
            editor.putString(activitySaveKey, result.getClassName());
            editor.commit();
        }
        return result;
    }
}
