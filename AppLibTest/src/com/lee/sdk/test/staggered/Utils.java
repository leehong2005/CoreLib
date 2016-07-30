/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.staggered;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml.Encoding;

/**
 * 工具类
 * 
 * @author Li Hong
 * 
 * @since 2013-7-9
 */
public final class Utils {
    /**DEBUG*/
    private static final boolean DEBUG = true;
    /**TAG*/
    public static final String TAG = "Utils";
    /**美图的图片缓存路径*/
    private static final String BEAUTY_IMAGE_CACHE_DIR = "image/img_cache";
    /**美图数据缓存目录*/
    private static final String BEAUTY_CACHE_DATA_DIR = "beauty/data";
    
    /**
     * 构造方法
     */
    private Utils() {
    }

    /**
     * 将URL转换成一个唯一的值
     * 
     * @param url URL
     * @return 经过MD5过后的字符串
     */
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
     * 得到文件名的后缀
     * 
     * @param fileName 文件名
     * @return 后缀
     */
    private static String getSuffix(String fileName) {
        int dotPoint = fileName.lastIndexOf(".");
        int slPoint = fileName.lastIndexOf("/");
        if (dotPoint < slPoint) {
            return "";
        }
        
        if (dotPoint != -1) {
            return fileName.substring(dotPoint + 1);
        }
        
        return null;
    }
    
    /**
     * 检测网络连接
     * 
     * @param context context
     * @return true表示网络连接，否则返回false。
     */
    public static boolean checkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 从网上获取内容get方式
     * 
     * @param url url
     * @return string
     * @throws IOException IOException
     * @throws ClientProtocolException ClientProtocolException
     */
    public static String getStringFromUrl(String url) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
    
    /**
     * 从网络上下载Bitmap
     * 
     * @param url URL
     * @param inSample inSample
     * @param username 请求用户名
     * @param password 请求密码
     * 
     * @return Bitmap
     */
    public static Bitmap getBitmapFromNet(String url, int inSample, String username, String password) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        InputStream is = null;

        try {
            long start = System.currentTimeMillis();
            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) (imageUrl.openConnection());
            conn.setConnectTimeout(10000);// SUPPRESS CHECKSTYLE
            conn.setReadTimeout(10000);// SUPPRESS CHECKSTYLE
            conn.connect();

            is = conn.getInputStream();
            
            long end = System.currentTimeMillis();
            if (DEBUG) {
                Log.e(TAG, "fetch image from network, time = " + (end - start) + " ms,    length = " 
                        + conn.getContentLength() + "         url = " + url);
            }
            
            if (null != is) {
                start = System.currentTimeMillis();
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inSampleSize = inSample;
                bitmap = BitmapFactory.decodeStream(is, null, ops);
                end = System.currentTimeMillis();
                
                if (DEBUG) {
                    if (null != bitmap) {
                        Log.d(TAG, "        decode image, time = " + (end - start) + "ms,    width = " 
                            + bitmap.getWidth() + "    height = " + bitmap.getHeight() + "          url = " + url);
                    }
                }
            }
        } catch (InterruptedIOException e) {
            e.printStackTrace();
            if (DEBUG) {
                Log.w(TAG, "Error InterruptedIOException   url = " + url, e);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (DEBUG) {
                Log.w(TAG, "Error MalformedURLException   url = " + url, e);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (DEBUG) {
                Log.w(TAG, "Error OutOfMemoryError   url = " + url, e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG) {
                Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (DEBUG) {
                Log.w(TAG, "Error while retrieving bitmap from " + url, e);
            }
        } finally {
            close(is);

            if (null != conn) {
                conn.disconnect();
            }
        }

        return bitmap;
    }
    
    /**
     * 从网络请求图片
     * 
     * @param context context
     * @param inSampleSize inSampleSize
     * @param url URL
     * @return 返回的bitmap
     */
//    public static Bitmap getBitmapFromNet(Context context, int inSampleSize, String url) {
//        Bitmap bitmap = null;
//        long start = System.currentTimeMillis();
//        // 从网络获取图片
//        final ProxyHttpClient httpClient = Utility.createHttpClient(context);
//        HttpGet getRequest = null;
//    
//        try {
//            getRequest = new HttpGet(url);
//            HttpResponse httpResponse = httpClient.executeSafely(getRequest);
//            final int statusCode = httpResponse.getStatusLine().getStatusCode();
//            if (statusCode != HttpStatus.SC_OK) {
//                if (DEBUG) {
//                    Log.w(TAG, "Error " + statusCode + " while retrieving bitmap from " + url);
//                }
//                return null;
//            }
//    
//            final HttpEntity httpEntity = httpResponse.getEntity();
//            if (httpEntity != null) {
//                InputStream inputStream = null;
//                try {
//                    inputStream = httpEntity.getContent();
//                    long end = System.currentTimeMillis();
//                    if (DEBUG) {
//                        Log.e(TAG, "fetch image from network, time = " + (end - start) + " ms,    length = " 
//                                + httpEntity.getContentLength() + "         url = " + url);
//                    }
//                    
//                    if (null != inputStream) {
//                        start = System.currentTimeMillis();
//                        BitmapFactory.Options ops = new BitmapFactory.Options();
//                        ops.inSampleSize = inSampleSize;
//                        bitmap = BitmapFactory.decodeStream(inputStream, null, ops);
//                        
//                        end = System.currentTimeMillis();
//                        if (DEBUG) {
//                            if (null != bitmap) {
//                                Log.d(TAG, "        decode image, time = " + (end - start) + "ms,    width = " 
//                                    + bitmap.getWidth() + "    height = " + bitmap.getHeight() + " url = " + url);
//                            }
//                        }
//                    }
//                    // 将图片写入文件
//                } catch (Exception e) {
//                    // 结束前需要有异常回调
//                    e.printStackTrace();
//                } catch (OutOfMemoryError e) {
//                    e.printStackTrace();
//                } finally {
//                    close(inputStream);
//                    httpEntity.consumeContent();
//                }
//            }
//        } catch (IOException e) {
//            // 结束前需要有异常回调
//            if (getRequest != null) {
//                getRequest.abort();
//            }
//            
//            if (DEBUG) {
//                Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
//            }
//        } catch (IllegalStateException e) {
//            // 结束前需要有异常回调
//            if (getRequest != null) {
//                getRequest.abort();
//            }
//            
//            if (DEBUG) {
//                Log.w(TAG, "Incorrect URL: " + url);
//            }
//        } catch (Exception e) {
//            // 结束前需要有异常回调
//            if (getRequest != null) {
//                getRequest.abort();
//            }
//            if (DEBUG) {
//                Log.w(TAG, "Error while retrieving bitmap from " + url, e);
//            }
//        } finally {
//            if (null != httpClient) {
//                httpClient.close();
//            }
//        }
//        
//        return bitmap;
//    }
    
    /**
     * 把Bitmap保存到文件中
     * 
     * @param bmp Bitmap
     * @param destFile 目标文件
     */
    public static void saveBitmapToFile(Bitmap bmp, File destFile) {
        if (null != bmp && null != destFile && !destFile.isDirectory()) {
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(destFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);// SUPPRESS CHECKSTYLE
                fos.flush();
                fos = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } finally {
                close(fos);
            }
        }
    }
    
    /**
     * 关闭输入流
     * 
     * @param is 输入流
     */
    public static void close(InputStream is) {
        try {
            if (null != is) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭输出流
     * 
     * @param os 输出流
     */
    public static void close(OutputStream os) {
        try {
            if (null != os) {
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 得到缓存的根路径
     * 
     * @param context context
     * @return 路径
     */
    @SuppressLint("NewApi")
    public static String getExternalCacheDir(Context context) {
        File cacheDir = null;
        // 如果SDK版本大于等于8
        if (hasFroyo()) {
            // getExternalCacheDir是在API Level 8引入的，所有必须加以判断
            cacheDir = context.getExternalCacheDir();
        }

        if (null == cacheDir) {
            cacheDir = context.getCacheDir();
        }
        
        return (null != cacheDir) ? cacheDir.getAbsolutePath() : null;
    }
    
    /**
     * 得到外部存储SDCard的路径
     * 
     * @param context context
     * @return 路径
     */
    public static String getExternalSavedDir(Context context) {
        boolean sdcardWriteable = isExternalStorageWriteable();
        File file;
        // 判断SDCard是否可写
        if (sdcardWriteable) {
            // 如果SD卡可以使用
            file = android.os.Environment.getExternalStorageDirectory();
        } else {
            // SD卡不能使用的时候，选择使用应用的缓存文件夹，这里可能会有问题
            file = context.getCacheDir();
        }
        
        if (null == file) {
            return "";
        }
        
        if (!file.exists()) {
            // 如果不存在文件夹，创建文件夹
            file.mkdirs();
        }
        
        return file.getAbsolutePath();
    }
    
    /**
     * 拷贝文件
     * 
     * @param src 源文件
     * @param dst 目标文件
     * @return 拷贝的字节数
     */
    public static long copyFile(File src, File dst) {
        if (null == src || null == dst) {
            return 0;
        }

        if (!src.exists()) {
            return 0;
        }

        long size = 0;

        try {
            FileInputStream is = new FileInputStream(src);
            FileOutputStream os = new FileOutputStream(dst);
            size = copyStream(is, os);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }
    
    /**
     * 从网络下载数据流
     * 
     * @param dir 存储路径
     * @param name 存储文件名
     * @param url URL
     * @return 返回字节大小
     */
    public static long downloadStream(String dir, String name, String url) {
        return downloadStream(new File(dir, name), url);
    }
    
    /**
     * 从网络下载数据流
     * 
     * @param file 文件名
     * @param url URL
     * @return 返回字节大小
     */
    public static long downloadStream(File file, String url) {
        long size = 0;
        HttpURLConnection conn = null;
        InputStream is = null;

        try {
            if (null == file) {
                return 0;
            }

            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) (imageUrl.openConnection());
            conn.setConnectTimeout(10000); // SUPPRESS CHECKSTYLE
            conn.setReadTimeout(10000); // SUPPRESS CHECKSTYLE
            // conn.setDoInput(true);
            conn.connect();

            is = conn.getInputStream();
            if (null != is) {
                File imageFile = file;
                FileOutputStream os = new FileOutputStream(imageFile);
                size = Utils.copyStream(is, os);
            }
        } catch (InterruptedIOException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(is);

            if (null != conn) {
                conn.disconnect();
            }
        }

        return size;
    }

    /**
     * 从输入流中读取字节写入输出流
     * 
     * @param is 输入流
     * @param os 输出流
     * @return 复制大字节数
     */
    public static long copyStream(InputStream is, OutputStream os) {
        if (null == is || null == os) {
            return 0;
        }

        try {
            final int DEFAULT_BUFFER_SIZE = 1024 * 3; // SUPPRESS CHECKSTYLE
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            long size = 0;
            int len = 0;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
                size += len;
            }
            os.flush();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    /**
     * 得到美图数据的缓存目录
     * 
     * @param context context
     * @return 缓存目录
     */
    public static String getBeautyDataCacheDirectory(Context context) {
        return getBeautyCacheDirectory(context, BEAUTY_CACHE_DATA_DIR);
    }
    
    /**
     * 得到美图图片的缓存目录
     * 
     * @param context context
     * @return 缓存目录
     */
    public static String getImageCacheDirectory(Context context) {
        return getBeautyCacheDirectory2(context, BEAUTY_IMAGE_CACHE_DIR);
    }
    
    /**
     * 删除所有的图片缓存文件，不会删除文件夹
     * 
     * @param context context
     * @return true if succeed, otherwise false.
     */
    public static boolean deleteImageCacheFile(Context context) {
        try {
            String cacheDir = getImageCacheDirectory(context);
            File file = new File(cacheDir);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (null != files) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        
        return true;
    }
    
    /**
     * 得到美图的缓存目录, /sdcard/Android/data/package-name/cache/
     * 
     * @param context context
     * @param name 文件夹名字
     * @return 缓存目录
     */
    private static String getBeautyCacheDirectory(Context context, String name) {
        String cacheDir = Utils.getExternalCacheDir(context);
        if (!TextUtils.isEmpty(cacheDir)) {
            File file = new File(cacheDir, name);
            if (!file.exists()) {
                file.mkdirs();
            }
            
            return file.getAbsolutePath();
        }
        
        return "";
    }
    
    /**
     * 得到美图的缓存目录, /sdcard/baidu/searchbox/
     * 
     * @param context context
     * @param name 文件夹名字
     * @return 缓存目录
     */
    private static String getBeautyCacheDirectory2(Context context, String name) {
        File cacheDir = null;
        boolean sdcardWriteable = isExternalStorageWriteable();
        if (sdcardWriteable) {
            // 如果SD卡可以使用
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), name);
        } else {
            // SD卡不能使用的时候，选择使用应用的缓存文件夹，这里可能会有问题
            cacheDir = new File(context.getCacheDir(), name);
        }
        
        if (null != cacheDir) {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }
        
        return cacheDir.getAbsolutePath();
    }
    
    public static boolean isWifiNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable()
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        if (DEBUG) {
            Log.d(TAG, "isWifiNetworkConnected, rtn: " + flag);
        }
        return flag;
        
    }
    
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }
    
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
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
        if (TextUtils.equals(Environment.MEDIA_MOUNTED,
                Environment.getExternalStorageState())) {
            File esd = Environment.getExternalStorageDirectory();
            if (esd.exists() && esd.canWrite()) {
                File file = new File(esd,
                        ".696E5309-E4A7-27C0-A787-0B2CEBF1F1AB");
                if (file.exists()) {
                    writealbe = true;
                } else {
                    try {
                        writealbe = file.createNewFile();
                    } catch (IOException e) {
                        if (DEBUG) {
                            Log.w(TAG,
                                    "isExternalStorageWriteable() can't create test file.");
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.i(TAG, "Utility.isExternalStorageWriteable(" + writealbe
                    + ") cost " + (end - start) + "ms.");
        }
        return writealbe;
    }
    
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable();
        if (DEBUG) {
            Log.d(TAG, "isNetworkConnected, rtn: " + flag);
        }
        return flag;
    }
    
    public static String streamToString(InputStream is) {
        return streamToString(is, Encoding.UTF_8.toString());
    }
    
    /**
     * 按照特定的编码格式转换Stream成string
     * 
     * @param is
     *            Stream源
     * @param enc
     *            编码格式
     * @return 目标String
     */
    public static String streamToString(InputStream is, String enc) {
        if (null == is) {
            return null;
        }
        
        StringBuilder buffer = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, enc), 2048);
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer.toString();
    }
    
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
            byte[] buffer = new byte[2048];
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
    
    public static boolean extractFileFromAsset(AssetManager amgr, String src,
            String dst) {
        boolean bRet = false;
        try {
            bRet = streamToFile(amgr.open(src, Context.MODE_PRIVATE), new File(
                    dst));
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return bRet;
    }
    
    public static void closeSafely(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
