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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lee.sdk.Configuration;
import com.lee.sdk.app.BaseApplication;

/**
 * 
 * @author lihong06
 * @since 2013-7-9
 */
public class Utils {
    private static final boolean DEBUG = Configuration.DEBUG;
    private static final String TAG = "Utils";
    /** Stream buffer size. */
    public static final int STREAM_BUFFER_SIZE = 8192;

    /**
     * 为程序创建桌面快捷方式。
     * 
     * @param activity 指定当前的Activity为快捷方式启动的对象
     * @param nameId 快捷方式的名称
     * @param iconId 快捷方式的图标
     * @param appendFlags 需要在快捷方式启动应用的Intent中附加的Flag
     */
    public static void addShortcut(Activity activity, int nameId, int iconId, int appendFlags) {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(nameId));
        shortcut.putExtra("duplicate", false); // 不允许重复创建

        // 指定当前的Activity为快捷方式启动的对象
        ComponentName comp = new ComponentName(activity.getPackageName(), activity.getClass().getName());
        Intent intent = new Intent(Intent.ACTION_MAIN).setComponent(comp);
        if (appendFlags != 0) {
            intent.addFlags(appendFlags);
        }
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        // 快捷方式的图标
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(activity, iconId);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        activity.sendBroadcast(shortcut);
    }

    /**
     * 通过应用名称和包体名称判断桌面是否已经有对应的快捷方式
     * 
     * @param context {@link Context}
     * @param shortcutName 应用名称
     * @param packageName 包体名称
     * 
     * @return 是否已有快捷方式，在系统DB读取中出现异常时，默认返回true
     */
    public static boolean hasShortcut(Context context, String shortcutName, String packageName) {
        boolean res = true;
        if (context != null && !TextUtils.isEmpty(shortcutName) && !TextUtils.isEmpty(packageName)) {
            Uri uri = getShortcutUri();
            res = hasShortcut(context, shortcutName, packageName, uri);
        }
        return res;
    }

    /**
     * 通过应用名称和包体名称判断桌面是否已经有对应的快捷方式
     * 
     * @param context {@link Context}
     * @param shortcutName 应用名称
     * @param packageName 包体名称
     * @param uri {@link Uri}
     * 
     * @return 是否已有快捷方式，在系统DB读取中出现异常时，默认返回true
     */
    private static boolean hasShortcut(Context context, String shortcutName, String packageName, Uri uri) {
        if (context == null || TextUtils.isEmpty(shortcutName) || TextUtils.isEmpty(packageName) || uri == null) {
            return true;
        }

        boolean res = false;

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[] { "title", "intent" }, "title=?",
                    new String[] { shortcutName }, null);

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("intent");
                if (index >= 0 && index < cursor.getColumnCount()) {
                    do {
                        String intentString = cursor.getString(index);
                        if (intentString != null && intentString.contains(packageName)) {
                            res = true;
                            break;
                        }
                    } while (cursor.moveToNext());
                }
            }

        } catch (Exception e) {
            res = true;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return res;
    }

    /**
     * 得到快捷方式的URI
     * 
     * @return URI
     */
    private static Uri getShortcutUri() {
        String authority = "com.android.launcher.settings";
        if (APIUtils.hasFroyo()) {
            authority = "com.android.launcher2.settings";
        }

        final Uri contentUri = Uri.parse("content://" + authority + "/favorites?notify=true");
        return contentUri;
    }

    /**
     * Hides the input method.
     * 
     * @param context context
     * @param view The currently focused view
     * @return success or not.
     */
    public static boolean hideInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return false;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        return false;
    }

    /**
     * Show the input method.
     * 
     * @param context context
     * @param view The currently focused view, which would like to receive soft keyboard input
     * @return success or not.
     */
    public static boolean showInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return false;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.showSoftInput(view, 0);
        }

        return false;
    }

    public static float pixelToDp(Context context, float val) {
        float density = context.getResources().getDisplayMetrics().density;
        return val * density;
    }

    public static String getHashedFileName(String url) {
        if (url == null || url.endsWith("/")) {
            return null;
        }

        String suffix = getSuffix(url);
        StringBuilder sb = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] dstbytes = digest.digest(url.getBytes("UTF-8")); // GMaFroid uses UTF-16LE
            sb = new StringBuilder();
            for (int i = 0; i < dstbytes.length; i++) {
                sb.append(Integer.toHexString(dstbytes[i] & 0xff));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != sb && null != suffix) {
            return sb.toString() + "." + suffix;
        }

        return null;
    }

    private static String getSuffix(String fileName) {
        int dot_point = fileName.lastIndexOf(".");
        int sl_point = fileName.lastIndexOf("/");
        if (dot_point < sl_point) {
            return "";
        }

        if (dot_point != -1) {
            return fileName.substring(dot_point + 1);
        }

        return null;
    }

    /**
     * Indicates whether the specified action can be used as an intent. This method queries the
     * package manager for installed packages that can respond to an intent with the specified
     * action. If no suitable package is found, this method returns false.
     * 
     * @param context The application's environment.
     * @param intent The Intent action to check for availability.
     * 
     * @return True if an Intent with the specified action can be sent and responded to, false
     *         otherwise.
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();

        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;
    }

    /**
     * DisplayMetrics 对象
     */
    private static DisplayMetrics sDisplayMetrics;

    /**
     * 得到显示宽度
     * 
     * @param context Context
     * 
     * @return 宽度
     */
    public static int getDisplayWidth(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.widthPixels;
    }

    /**
     * 得到显示高度
     * 
     * @param context Context
     * 
     * @return 高度
     */
    public static int getDisplayHeight(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.heightPixels;
    }

    /**
     * 得到显示密度
     * 
     * @param context Context
     * 
     * @return 密度
     */
    public static float getDensity(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.density;
    }

    /**
     * 得到DPI
     * 
     * @param context Context
     * 
     * @return DPI
     */
    public static int getDensityDpi(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.densityDpi;
    }

    /**
     * 初始化DisplayMetrics
     * 
     * @param context Context
     */
    private static void initDisplayMetrics(Context context) {
        if (null == sDisplayMetrics) {
            if (null != context) {
                sDisplayMetrics = context.getResources().getDisplayMetrics();
            }
        }
    }

    /**
     * 判断一个字符串是否为合法url
     * 
     * @param query String
     * @return true: 是合法url
     */
    public static boolean isUrl(String query) {
        Matcher matcher = Patterns.WEB_URL.matcher(query);
        if (matcher.matches()) {
            return true;
        }

        return false;
    }

    /**
     * 网络是否可用。(
     * 
     * @param context context
     * @return 连接并可用返回 true
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable();
        return flag;
    }

    /**
     * 获取活动的连接。
     * 
     * @param context context
     * @return 当前连接
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a disk
     * filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 获取设备上某个volume对应的存储路径
     * 
     * @param volume 存储介质
     * @return 存储路径
     */
    public static String getVolumePath(Object volume) {
        String result = "";
        Object o = invokeHideMethodForObject(volume, "getPath", null, null);
        if (o != null) {
            result = (String) o;
        }

        return result;
    }

    /**
     * 获取设备上所有volume
     * 
     * @param context context
     * @return Volume数组
     */
    public static Object[] getVolumeList(Context context) {
        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Object[] result = null;
        Object o = invokeHideMethodForObject(manager, "getVolumeList", null, null);
        if (o != null) {
            result = (Object[]) o;
        }

        return result;
    }

    /**
     * 获取设备上某个volume的状态
     * 
     * @param context context
     * @param volumePath volumePath
     * @return result
     */
    public static String getVolumeState(Context context, String volumePath) {
        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String result = "";
        Object o = invokeHideMethodForObject(manager, "getVolumeState", new Class[] { String.class },
                new Object[] { volumePath });
        if (o != null) {
            result = (String) o;
        }

        return result;
    }

    /**
     * invoke object's method including private method
     * 
     * @param owner : target object
     * @param methodName : name of the target method
     * @param parameterTypes : types of the target method's parameters
     * @param parameters : parameters of the target method
     * @return result of invoked method
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static Object invokePublicMethod(Object owner, String methodName, Class<?>[] parameterTypes,
            Object[] parameters) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        if (null == owner) {
            return null;
        }
        // 获取所有public方法，包括父类的
        Method method = owner.getClass().getMethod(methodName, parameterTypes);
        Object result = method.invoke(owner, parameters);
        return result;
    }

    /**
     * 调用一个对象的隐藏方法。
     * 
     * @param obj 调用方法的对象.
     * @param methodName 方法名。
     * @param types 方法的参数类型。
     * @param args 方法的参数。
     * @return 隐藏方法调用的返回值。
     */
    public static Object invokeHideMethodForObject(Object obj, String methodName, Class<?>[] types, Object[] args) {
        Object o = null;
        try {
            Class<?> cls;
            if (obj instanceof Class<?>) { // 静态方法
                cls = (Class<?>) obj;
            } else { // 非静态方法
                cls = obj.getClass();
            }
            Method method = cls.getMethod(methodName, types);
            o = method.invoke(obj, args);
            if (Configuration.DEBUG) {
                Log.d("Utils", "Method \"" + methodName + "\" invoked success!");
            }
        } catch (Exception e) {
            if (Configuration.DEBUG) {
                Log.d("Utils", "Method \"" + methodName + "\" invoked failed: " + e.getMessage());
            }
        }
        return o;
    }

    /**
     * 判断外部存储是否可写
     * 
     * 此方法内采用文件读写操作来检测，所以相对比较耗时，请谨慎使用。
     * 
     * @return true:可写; false 不存在/没有mounted/不可写
     */
    public static boolean isExternalStorageWriteable() {
        boolean writealbe = false;
        long start = System.currentTimeMillis();
        if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
            File esd = Environment.getExternalStorageDirectory();
            if (esd.exists() && esd.canWrite()) {
                File file = new File(esd, ".696E5309-E4A7-27C0-A787-0B2CEBF1F1AB");
                if (file.exists()) {
                    writealbe = true;
                } else {
                    try {
                        writealbe = file.createNewFile();
                    } catch (IOException e) {
                        if (DEBUG) {
                            Log.w(TAG, "isExternalStorageWriteable() can't create test file.");
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.i(TAG, "Utility.isExternalStorageWriteable(" + writealbe + ") cost " + (end - start) + "ms.");
        }
        return writealbe;
    }

    /**
     * 检测component是否可用
     * 
     * @param ctx context
     * @param className class name
     * @return 该组件是否可用
     */
    public static boolean isComponentEnable(Context ctx, String className) {
        PackageManager pm = ctx.getPackageManager();
        ComponentName cn = new ComponentName(ctx.getPackageName(), className);
        int ret = pm.getComponentEnabledSetting(cn);
        if (ret == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || ret == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            return true;
        }
        return false;
    }

    /**
     * 为了防止大家不给线程起名字，写此静态函数.目的是当发生线程泄漏后能够快速定位问题.
     * 
     * @param r Runnable
     * @param name 线程名
     * @return Thread
     */
    public static Thread newThread(Runnable r, String name) {
        if (TextUtils.isEmpty(name)) {
            throw new RuntimeException("thread name should not be empty");
        }
        return new Thread(r, getStandardThreadName(name));
    }

    /**
     * 获取标准线程名
     * 
     * @param name 线程名
     * @return 处理过的线程名
     */
    public static String getStandardThreadName(String name) {
        if (name != null) {
            final String PREFIX = "THREAD_";
            if (!name.startsWith(PREFIX)) {
                return PREFIX + name;
            }
        }
        return name;
    }

    /**
     * 缓存文件
     * 
     * @param context Context Object
     * @param file 本地文件名
     * @param data 要保存的数据
     * @param mode 打开文件的方式
     * @return 是否保存成功
     */
    public static boolean cache(Context context, String file, String data, int mode) {
        return cache(context, file, data.getBytes(), mode);
    }

    /**
     * 缓存文件
     * 
     * @param context Context Object
     * @param file 本地文件名
     * @param data 要保存的数据
     * @param mode 打开文件的方式
     * @return 是否保存成功
     */
    public static boolean cache(Context context, String file, byte[] data, int mode) {
        boolean bResult = false;
        if (null == data) {
            data = new byte[0];
        }

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(file, mode);
            fos.write(data);
            fos.flush();
            bResult = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bResult;
    }

    /**
     * 删除缓存文件，通常这个文件是存在应用程序的系统数据目录里面，典型的目录是data/data/package-name/files
     * 
     * @param context context
     * @param name 本地文件名，不要包含路径分隔符
     * @return true：成功，false：失败
     */
    public static boolean deleteCache(Context context, String name) {
        boolean succeed = false;

        try {
            succeed = context.deleteFile(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return succeed;
    }

    /**
     * 将输入流存储到指定文件
     * 
     * @param inputStream 输入流
     * @param file 存储的文件
     */
    public static void saveToFile(InputStream inputStream, File file) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            copyStream(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "catch FileNotFoundException");
            }
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.d(TAG, "catch IOException");
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 流拷贝
     * 
     * @param is 输入流
     * @param os 输出流
     */
    private static void copyStream(InputStream is, OutputStream os) {
        final int BUFFER_SIZE = 1024;
        try {
            byte[] bytes = new byte[BUFFER_SIZE];

            for (;;) {
                int count = is.read(bytes, 0, BUFFER_SIZE);

                if (count == -1) {
                    break;
                }

                os.write(bytes, 0, count);
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.d(TAG, "copyStream: catch IOException");
            }
            e.printStackTrace();
        }
    }

    /**
     * 安全关闭.
     * 
     * @param closeable Closeable.
     */
    public static void closeSafely(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * stream to bytes
     * 
     * @param is inputstream
     * @return bytes
     */
    public static byte[] streamToBytes(InputStream is) {
        if (null == is) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int n = 0;
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSafely(is);
        }
        return output.toByteArray();
    }

    /**
     * 转换Stream成string
     * 
     * @param is Stream源
     * @return 目标String
     */
    public static String streamToString(InputStream is) {
        return streamToString(is, "UTF-8");
    }

    /**
     * 按照特定的编码格式转换Stream成string
     * 
     * @param is Stream源
     * @param enc 编码格式
     * @return 目标String
     */
    public static String streamToString(InputStream is, String enc) {
        if (null == is) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, enc), STREAM_BUFFER_SIZE);
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSafely(is);
        }
        return buffer.toString();
    }

    /**
     * 将输入流中的数据保存到文件
     * 
     * @param is 输入流
     * @param file 目标文件
     * @return true:保存成功，false:保存失败
     */
    public static boolean streamToFile(InputStream is, File file) {
        boolean bRet = false;
        if (null == is || null == file) {
            return bRet;
        }

        // 下载后文件存在临时目录中
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int length = -1;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            bRet = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSafely(fos);
            closeSafely(is);
        }
        return bRet;
    }
    
    /**
    * 创建一个 http client。
    * 
    * @param context
    *            Context.
    * @return ProxyHttpClient
    */
   public static DefaultHttpClient createHttpClient(Context context) {
       DefaultHttpClient httpclient = new DefaultHttpClient();
       // httpclient.getParams().setParameter("Accept-Encoding", "gzip");
       
       final int httpTimeout = 30000;
       final int socketTimeout = 50000;
       HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),
               httpTimeout);
       HttpConnectionParams
               .setSoTimeout(httpclient.getParams(), socketTimeout);
       return httpclient;
   }
   
   /**
    * 根据server下发的Content-Encoding，获取适当的inputstream.当content-encoding为gzip时，返回GzipInputStream
    * 否则返回原有的inputStream
    * 
    * @param resEntity
    *            {@link HttpEntity}
    * @return InputStream or null
    * @throws IOException
    *             {@link IOException}
    */
   public static InputStream getSuitableInputStream(HttpEntity resEntity) throws IOException {
       if (resEntity == null) {
           return null;
       }
       InputStream inputStream = resEntity.getContent();
       if (inputStream != null) {
           Header header = resEntity.getContentEncoding();
           if (header != null) {
               String contentEncoding = header.getValue();
               if (!TextUtils.isEmpty(contentEncoding) && contentEncoding.toLowerCase().indexOf("gzip") > -1) {
                   inputStream = new GZIPInputStream(inputStream);
               }
           }
       }
       return inputStream;
   }
   
   /**
    * 将URL转换成一个唯一的值，返回的字符串会带上后缀（如果URL中有的话）
    * 
    * @param url URL
    * @return 经过MD5过后的字符串
    */
   public static String getHashedString(String url) {
       return getHashedString(url, true);
   }
   
   /**
    * 将URL转换成一个唯一的值，返回的字符串会带上后缀（如果URL中有的话）
    * 
    * @param url URL
    * @param appendSuffix 是否追加URL中截取的后缀
    * @return 经过MD5过后的字符串
    */
   public static String getHashedString(String url, boolean appendSuffix) {
       if (url == null || url.endsWith("/")) {
           return null;
       }

       String suffix = appendSuffix ? getSuffix(url) : null;
       StringBuilder sb = null;
       
       try {
           MessageDigest digest = MessageDigest.getInstance("MD5");
           byte[] dstbytes = digest.digest(url.getBytes("UTF-8")); // GMaFroid uses UTF-16LE
           sb = new StringBuilder();
           for (int i = 0; i < dstbytes.length; i++) {
               sb.append(Integer.toHexString(dstbytes[i] & 0xff));// SUPPRESS CHECKSTYLE
           }
       } catch (Exception e) {
           e.printStackTrace();
       }

       if (null != sb && null != suffix) {
           return sb.toString() + "." + suffix;
       }
      
       return (null != sb) ? sb.toString() : null;
   }

    /**
     * Runs the specified action on the UI thread. If the current thread is the UI thread, then the action is executed
     * immediately.If the current thread is not the UI thread, the action is posted to the event queue of the UI thread.
     * <p/> <p> 该功能与{@link Activity#runOnUiThread(Runnable)}一样 </p>
     *
     * @param action the action to run on the UI thread
     */
    public static void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Handler handler = BaseApplication.getMainHandler();
            handler.post(action);
        } else {
            action.run();
        }
    }

    /**
     * Runs the specified action on the UI thread. If the current thread is the UI thread, then the action is executed
     * immediately.If the current thread is not the UI thread, the action is posted to the event queue of the UI thread.
     * <p/> <p> 该功能与{@link Activity#runOnUiThread(Runnable)}一样 </p>
     *
     * @param action      the action to run on the UI thread
     * @param delayMillis 延时
     */
    public static void runOnUiThread(Runnable action, long delayMillis) {
        if (delayMillis > 0) {
            Handler handler = BaseApplication.getMainHandler();
            handler.postDelayed(action, delayMillis);
        } else {
            runOnUiThread(action);
        }
    }
}
