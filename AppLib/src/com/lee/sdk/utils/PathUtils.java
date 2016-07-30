/*
 * Copyright (C) 2016 LiHong (https://github.com/leehong2005)
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
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lee.sdk.BuildConfig;
import com.lee.sdk.app.BaseApplication;

import java.io.File;

/**
 * 这个类负责提供得到SDCard、缓存的目录的接口
 *
 * @author lihong06
 * @since 2014-10-10
 */
public final class PathUtils {
    /**
     * Constructor method
     */
    private PathUtils() {

    }

    /**
     * DEBUG
     */
    private static final boolean DEBUG = BuildConfig.DEBUG & true;
    /**
     * TAG
     */
    private static final String TAG = "PathUtils";
    /**
     * TBREADER
     */
    private static final String DIRCTORY_TBREADER = "tbreader";
    /**
     * TBREADER2
     */
    private static final String DIRCTORY_TBREADER2 = "tbreader2";
    /**
     * Cache
     */
    private static final String DIRCTORY_DATA = "data";
    /**
     * Download
     */
    private static final String DIRCTORY_DOWNLOAD = "downloads";
    /**
     * 图片缓存目录
     */
    private static final String DIRCTORY_IMAGE_CACHE = "img_cache";
    /**
     * 数据目录：shuqi/data
     */
    private static final String DIRECTORY_DATA_CACHE = DIRCTORY_TBREADER + "/" + DIRCTORY_DATA;
    /**
     * Where we store downloaded files on the external storage
     */
    private static final String PATH_DEFAULT_DOWNLOAD = DIRCTORY_TBREADER + "/" + DIRCTORY_DOWNLOAD;
    /**
     * 下载目录的备份，在某些情况下，baidu目录可能被锁定导致不可写，因为需要新建立一下下载路径
     */
    private static final String PATH_DEFAULT_DOWNLOAD2 = DIRCTORY_TBREADER2 + "/" + DIRCTORY_DOWNLOAD;

    /**
     * 图片缓存的路径
     */
    private static String sImageCacheDir = null;
    /**
     * 应用的缓存目录
     */
    private static String sCacheDir = null;

    static {
        sImageCacheDir = generateImageCacheDir(BaseApplication.getAppContext());

        if (DEBUG) {
            Log.d(TAG, "PathUtils init image cache dir, sImageCacheDir = " + sImageCacheDir);
        }

        // 确保文件夹存在
        if (!TextUtils.isEmpty(sImageCacheDir)) {
            File file = new File(sImageCacheDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    /**
     * 得到当前SDCard上书旗的主目录：sdcard/shuqi
     *
     * @param context context
     * @return string
     */
    public static String getExternalStorageMainDir(Context context) {
        return  new File(getExternalStorageDir(context), DIRCTORY_TBREADER).getAbsolutePath();
    }

    /**
     * 得到外部存储SDCard的路径
     *
     * @param context context
     * @return 路径
     */
    public static String getExternalStorageDir(Context context) {
        boolean sdcardWriteable = isExternalStorageWritable();
        File file;
        // 判断SDCard是否可写
        if (sdcardWriteable) {
            // 如果SD卡可以使用
            file = Environment.getExternalStorageDirectory();
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
     * 得到图片的缓存目录:
     * <li>API Level >= 8时，路径是/mnt/sdcard/Android/data/[package-name]/cache/img_cache，这个目录会自动删除
     * <li>API Level < 8时，/sdcard/baidu/searchbox/img_cache，当应用删除时，这个目录不会被删除
     * <li>如果这个目录不可用，存在/data/data/[package-name]/cache下面
     * <li>如果这个目录不可用，存在/data/data/[package-name]/files下面
     *
     * @param context context
     * @return 缓存目录
     */
    public static String getImageCacheDirectory(Context context) {
        return sImageCacheDir;
    }

    /**
     * 得到当前应用的缓存目录，这个目录在应用删除后会自动被删除。
     *
     * <li>API Level >= 8时，路径是/mnt/sdcard/Android/data/[package-name]/cache/，这个目录会自动删除
     * <li>API Level < 8时，/sdcard/baidu/searchbox/，当应用删除时，这个目录不会被删除
     * <li>如果这个目录不可用，存在/data/data/[package-name]/cache下面
     * <li>如果这个目录不可用，存在/data/data/[package-name]/files下面
     *
     * @param context context
     * @return 缓存目录，可能是空，在使用的时候需要判断合法性。
     */
    @SuppressLint("NewApi")
    public static String getCacheDirectory(Context context) {
        // 如果不为空，直接返回
        if (!TextUtils.isEmpty(sCacheDir)) {
            return sCacheDir;
        }

        File cacheDir = null;
        // 如果SDK版本大于等于8
        if (APIUtils.hasFroyo()) {
            // getExternalCacheDir是在API Level 8引入的，所有必须加以判断
            cacheDir = context.getExternalCacheDir();
        }

        if (null == cacheDir) {
            // 如果SDCard可写，得到SDCard路径
            boolean sdcardWriteable = isExternalStorageWritable();
            if (sdcardWriteable) {
                cacheDir = Environment.getExternalStorageDirectory();
                if (null != cacheDir) {
                    cacheDir = new File(cacheDir, DIRECTORY_DATA_CACHE);
                }
            }
        }

        if (null == cacheDir) {
            cacheDir = context.getCacheDir();
        }

        if (null == cacheDir) {
            cacheDir = context.getFilesDir();
        }

        if (null != cacheDir) {
            // 如果目录不存在，则创建
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            sCacheDir = cacheDir.getAbsolutePath();
        }

        return sCacheDir;
    }

    /**
     * 删除所有的图片缓存文件，不会删除文件夹
     *
     * @param context context
     * @return true if succeed, otherwise false.
     */
    public static boolean deleteImageCacheFile(Context context) {
        String cacheDir = sImageCacheDir;
        if (TextUtils.isEmpty(cacheDir)) {
            return false;
        }

        try {
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
     * 得到下载文件的路径，使用下载模块中使用，通常形式是"sdcard/shuqi/downloads"。
     *
     * <p>这个方法会根据当前目录的状态来返回不同的路径，如果"baidu"目录不可用，将返回另一个目录</p>
     *
     * @param context context
     * @return path
     */
    public static File getDownloadDirectory(Context context) {
        boolean sdcardWriteable = isExternalStorageWritable();
        if (sdcardWriteable) {
            // 判断"baidu/searchbox"根目录是否可用
            File root = Environment.getExternalStorageDirectory();
            File downloads = null;
            if (isShuqiDirectoryWritable()) {
                // sdcard/shuqi/downloads
                downloads = new File(root, PATH_DEFAULT_DOWNLOAD);
            } else {
                // sdcard/shuqi2/downloads
                downloads = new File(root, PATH_DEFAULT_DOWNLOAD2);
            }

            if (null != downloads) {
                boolean createDirs = false;
                if (!downloads.exists()) {
                    createDirs = true;
                } else if (!downloads.isDirectory()) { // 如果不是文件夹，删除掉，再创建文件夹
                    deleteFile(downloads);
                    createDirs = true;
                }

                if (createDirs) {
                    // 创建目录
                    boolean succeed = downloads.mkdirs();
                    if (DEBUG) {
                        Log.d(TAG, "PathUtils#getDownloadDirectory(),  create download directory, succeed = "
                            + succeed + ",  directory = " + downloads);
                    }
                }
            }

            if (DEBUG) {
                Log.d(TAG, "PathUtils#getDownloadDirectory(), download directory = " + downloads);
            }

            return downloads;
        }

        return null;
    }

    /**
     * 判断SDCard是否可用，该方法不是线程安全的。
     *
     * @return true/false
     */
    public static boolean isExternalStorageWritable() {
        boolean writable = false;

        if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
            if (!TextUtils.isEmpty(sCacheDir)) {
                try {
                    // 在/sdcard/Android/data/com.tbreader.android/cache/目录下创建一个文件
                    File tempFile = new File(sCacheDir, ".696E5309-E4A7-27C0-A787-0B2CEBF1F1AB");
                    if (tempFile.exists()) {
//                        // 通过设置修改时间是否靠谱？
//                        long lastModified = tempFile.lastModified();
//                        writable = tempFile.setLastModified(lastModified);
                        writable = true;
                    } else {
                        writable = tempFile.createNewFile();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (DEBUG) {
            Log.d(TAG, "PathUtils#isExternalStorageWritable(),  writable = " + writable);

            if (!writable) {
                Utils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BaseApplication.getAppContext(),
                            "外部存储路径不可用，请检查设置", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        return writable;
    }

    /**
     * 判断"baidu/searchbox"目录是否可用。
     *
     * @return true/false
     */
    private static boolean isShuqiDirectoryWritable() {
        File esd = Environment.getExternalStorageDirectory();
        File file = new File(esd, DIRCTORY_TBREADER + "/" + DIRCTORY_DATA);
        boolean writable = isDirectoryWritable(file);

        if (DEBUG) {
            Log.d(TAG, "PathUtils#isShuqiDirectoryWritable(),  path = " + file + ",  writable = " + writable);
        }

        return writable;
    }

    /**
     * 判断指定的目录是否可写
     *
     * @param file file
     * @return true/false
     */
    private static boolean isDirectoryWritable(File file) {
        long start = System.currentTimeMillis();

        boolean writable = false;
        if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
            File esd = Environment.getExternalStorageDirectory();
            if (esd.exists() && esd.canWrite()) {
                if (null != file && file.exists()) {
                    try {
                        if (file.isDirectory()) {
                            File newFile = new File(file, ".116E5309-E4A7-27C0-A787-0B2CEBF1F1AB");
                            if (newFile.exists()) {
                                File newFile2 = new File(file, ".116E5309-E4A7-27C0-A787-0B2CEBF1F1AB__temp");
                                writable = newFile.renameTo(newFile2);
                                if (writable) {
                                    newFile2.renameTo(newFile);
                                }
                            } else {
                                writable = newFile.createNewFile();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        long end = System.currentTimeMillis();

        if (DEBUG) {
            Log.d(TAG, "PathUtils#isDirectoryWritable(),  time = "
                + (end - start)
                + " ms,  file" + file
                + ",  writable = " + writable);
        }

        return writable;
    }

    /**
     * 生成图片的缓存目录:
     * <li>API Level >= 8时，路径是/sdcard/Android/data/com.shuqi.controller/cache/img_cache，这个目录会自动删除。
     * <li>API Level <  8时，路径是/sdcard/shuqi/img_cache，当应用删除时，这个目录不会被删除。
     * <li>如果这个目录不可用，存在/data/data/[package-name]/cache/img_cache下面
     * <li>如果这个目录不可用，存在/data/data/[package-name]/files/img_cache下面
     * @param context context
     * @return 缓存目录
     */
    @SuppressLint("NewApi")
    private static String generateImageCacheDir(Context context) {
        String cacheDir = getCacheDirectory(context);
        if (!TextUtils.isEmpty(cacheDir)) {
            File dir = new File(cacheDir, DIRCTORY_IMAGE_CACHE);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            return dir.getAbsolutePath();
        }

        return "";
    }

    /**
     * 删除指定的文件
     *
     * @param file file
     * @return true/false
     */
    private static boolean deleteFile(File file) {
        // 由于在某些手机上面File#delete()方法是异步的，会导致后续的创建文件夹失败，
        // 推荐的做法是对要删除的文件重命名，然后再删除，这样就不会影响后续创建文件夹。
        try {
            String filePath = file.getAbsolutePath();
            File newFile = new File(filePath);
            // 构造一个不存在的文件名
            long time = System.currentTimeMillis();
            File tempFile = new File(filePath + time + ".tmp");
            newFile.renameTo(tempFile);
            boolean succeed = tempFile.delete();
            return succeed;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 获取内存中files文件夹路径
     * @param context context
     * @return 路径
     */
    public static String getInternalFilesDirPath(Context context) {
        File dataDir = context.getFilesDir();
        return dataDir.getAbsolutePath();
    }
}
