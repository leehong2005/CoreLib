package com.lee.sdk.test.staggered;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

public final class ImageAlbumLoader {
    /**
     * 网络请求的状态
     * 
     * @author Li Hong
     * @since 2013-8-1
     */
    public enum State {
        /**
         * 无状态，当没有请求时就处于无状态
         */
        STATE_NONE,
        
        /**
         * 成功
         */
        STATE_SUCCEED,
        
        /**
         * 网络异常
         */
        STATE_NETWORK_ERROR,
        
        /**
         * 服务器异常
         */
        STATE_SERVER_ERROR,
    }
    
    /**TAG*/
    private static final String TAG = "ImageAlbumLoader";
    /**DEBUG flag*/
    private static final boolean DEBUG = true;
    
    /**一次显示图集最大个数*/
    public static final int MAX_ALBUM_COUNT = 100;
    /**缓存中图集最大个数*/
    public static final int MAX_ALBUM_COUNT_IN_CACHE = 100;
    /**界面上显示图集最大个数*/
    public static final int MAX_ALBUM_COUNT_IN_UI = 1000;
    /**保存的文件名*/
    private static final String PERF_NAME = "picture_shared_prefs";
    /**最后更新时间*/
    private static final String KEY_LAST_UPDATE_TIME = "last_update_time";
    /**复制预置数据到缓存目录下*/
    private static final String KEY_COPY_RRESET_DATE = "copy_preset_data";
    /**显示用户引导*/
    private static final String KEY_SHOW_USER_GUIDE = "show_user_guide";
    /**缓存数据更新*/
    private static final String KEY_CACHE_UPDATE = "cache_update";
    /**服务器下发的签名*/
    private static final String KEY_REQUEST_SIGN = "request_sign";
    /**Assets中的预置数据的路径*/
    private static final String ASSETS_PRESET_FILE_NAME = "data";
    /**缓存数据文件名*/
    private static final String CACHE_DATA_FILE_NAME = "beauty_data.json";
    /**单实例*/
    private static ImageAlbumLoader sInstance = null;
    
    /**当前加载的图集数据，这只是作一个缓存，用于在Activity之间传递数据*/
    private List<ImageAlbumItem> mPictureAlbums = null;
    /**1-100个图集的缓存，这个缓存始终存前100个图集，这样做是为了提高性能，加载前100个时不用每次从文件中解析*/
    private List<ImageAlbumItem> mCacheAlbumItems = null;
    /**数据缓存路径*/
    private String mDataCacheDir = null;
    /**是否有更多的数据*/
    private boolean mHasMoreData = true;
    /**请求状态*/
    private State mState = State.STATE_SUCCEED;
    
    /**
     * 返回该类的单实例
     * 
     * @param context context
     * @return PictureAlbumLoader对象
     */
    public static synchronized ImageAlbumLoader getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new ImageAlbumLoader(context);
        }
        
        return sInstance;
    }
    
    /**
     * 释放当前单实例对象
     */
    public static synchronized void releaseIntance() {
        if (null != sInstance) {
            sInstance.mCacheAlbumItems = null;
            sInstance.mPictureAlbums = null;
        }
        
        sInstance = null;
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    private ImageAlbumLoader(Context context) {
        initialize(context);
    }
    
    /**
     * 初始化
     * 
     * @param context context
     */
    private void initialize(Context context) {
        if (null == mDataCacheDir) {
            String cacheDir = Utils.getBeautyDataCacheDirectory(context);
            if (null != cacheDir) {
                File file = new File(cacheDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                
                mDataCacheDir = file.getAbsolutePath();
            }
            
            if (DEBUG) {
                if (TextUtils.isEmpty(mDataCacheDir)) {
                    Log.e(TAG, "initialize   create cache directory failed.");
                } else {
                    Log.i(TAG, "initialize   create cache directory succeed, cache dir = " + mDataCacheDir);
                }
            }
        }
    }
    
    /**
     * 得到请求的状态，这个状态只一次有效
     * 
     * @return 状态
     */
    public synchronized State getState() {
        State state = mState;
        mState = State.STATE_NONE;
        return state;
    }
    
    /**
     * 从内存中得到缓存，这个缓存只是为前100个图集准备，因此，如果传入的index大于100，返回为空
     * 
     * @param index 图集索引
     * @return 缓存的图集集合
     */
    public List<ImageAlbumItem> getAlbumDataFromMemCache(final int index) {
        if (index >= 0 && index < MAX_ALBUM_COUNT_IN_CACHE) {
            return getSubAlbumDatas(mCacheAlbumItems, index);
        }
        
        return null;
    }
    
    
    /**
     * 加载图集数据
     * 
     * @param context context
     * @param index 索引 
     * @return 数据集合
     */
    public List<ImageAlbumItem> loadCacheAlbumsData(Context context, int index) {
        long start = System.currentTimeMillis();
        List<ImageAlbumItem> datas = loadCacheAlbumsData(context, index, formatCacheName(index));
        // 将取得到的图集保存到内存中
        setMemCacheAlbumDatas(datas, index);
        long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.i(TAG, "loadCacheAlbumsData: load data from cache time:  + " + (end - start) + " ms");
        }
        
        return datas;
    }
    
    /**
     * 加载图集数据
     * 
     * @param context context
     * @param index 索引 
     * @param fileName 缓存文件名
     * @return 数据集合
     */
    public synchronized List<ImageAlbumItem> loadCacheAlbumsData(Context context, int index, String fileName) {
        List<ImageAlbumItem> datas = new ArrayList<ImageAlbumItem>();
        InputStream is = null;
        
        try {
            // 如果缓存文件不存在或者未记录复制过预置数据
            if (0 == index) {
                if (!checkCacheDataExist() || !hasCopyPresetData(context)) {
                    // 复制预置数据
                    copyPresetToCache(context);
                    setCopyPresetData(context, true);
                }
            }
            
            File file = new File(mDataCacheDir, fileName);
            if (file.exists()) {
                is = new FileInputStream(file);
                parseData(is, datas);
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.close(is);
        }
        
        return getSubAlbumDatas(datas, index);
    }
    
    /**
     * 返回保存在这个类里面的图集数据。
     * 
     * @return 图集数据
     */
    public List<ImageAlbumItem> getPictureAlbumDatas() {
        return mPictureAlbums;
    }
    
    /**
     * 设置数据
     * 
     * @param datas datas
     */
    public void setPictureAlbumDatas(List<ImageAlbumItem> datas) {
        mPictureAlbums = datas;
    }
    
    /**
     * 清除图集数据，在不使用图集数据时，应该清除。
     */
    public void clearPictureAlbumsDatas() {
        mPictureAlbums = null;
    }
    
    /**
     * 是否有更多的数据
     * 
     * @param curCount 当前的数据
     * @return true表示有更多数据，否则false
     */
    public boolean hasMoreData(int curCount) {
        return mHasMoreData && curCount < MAX_ALBUM_COUNT_IN_UI;
    }
    
    /**
     * 清除所有的加载更多的数据
     * 
     * @return true如果成功，否则false
     */
    public synchronized boolean clearCacheData() {
        // 从索引为100开始，删除所有的加载更多的缓存数据
        boolean succeed = false;
//        final int step = MAX_ALBUM_COUNT_IN_CACHE;
//        for (int index = step; index < MAX_ALBUM_COUNT_IN_UI; index += step) {
//            String fileName = formatCacheName(index);
//            if (!TextUtils.isEmpty(fileName)) {
//                File file = new File(mDataCacheDir, fileName);
//                if (file.exists()) {
//                    succeed |= file.delete();
//                }
//            }
//        }
        
        return succeed;
    }
    
    /**
     * 设置缓存
     * 
     * @param cacheAlbumItems 缓存数据
     * @param index 索引
     */
    private void setMemCacheAlbumDatas(List<ImageAlbumItem> cacheAlbumItems, int index) {
        if (0 == index) {
            // 只保存合法的数据
            if (null != cacheAlbumItems && cacheAlbumItems.size() > 0) {
                mCacheAlbumItems = cacheAlbumItems;
            }
        }
    }
    
    /**
     * 得到所取得图集的子集
     * 
     * @param datas 所有图集
     * @param index 索引 
     * @return 子集
     */
    private List<ImageAlbumItem> getSubAlbumDatas(List<ImageAlbumItem> datas, int index) {
        index = index % MAX_ALBUM_COUNT_IN_CACHE;
        if (null != datas && datas.size() > 0 && index < datas.size()) {
            int start = index;
            int end = Math.min(index + MAX_ALBUM_COUNT, datas.size());
            return datas.subList(start, end);
        }
        
        return datas;
    }
    
    /**
     * 格式化缓存的名字，根据索引，会给缓存添加上一定的前缀。类似0_XXX.json
     * 
     * @param index 索引
     * @return 文件外
     */
    private String formatCacheName(int index) {
        int value = index / MAX_ALBUM_COUNT_IN_CACHE;
        return String.format("%d_%s", value, CACHE_DATA_FILE_NAME);
    }
    
    /**
     * 从输入流中解析数据。
     * 
     * @param is 输入流对象
     * @param datas 被填充的图集集合
     */
    private void parseData(InputStream is, List<ImageAlbumItem> datas) {
        if (null == is) {
            return;
        }
        
        String jsonData = Utils.streamToString(is);
        ImageJsonParser.parseJson(jsonData, datas);
    }
    
    /***
     * 从Command对象中得到数据，并解析JSON字符串
     * 
     * @param command 数据对象
     * @param datas 数据集合
     * @param fileName 缓存文件名字
     * @param saveDataToCache 标示是否将JSON数据存到缓存中
     */
//    private void parseData(Command command, List<ImageAlbumItem> datas, boolean saveDataToCache, String fileName) {
//        if (null == command) {
//            return;
//        }
//        
//        BeautyInfo beautyInfo = null;
//        DataSet dataSet = command.getDataSet();
//        if (null != dataSet && dataSet.size() > 0) {
//            Data data = dataSet.get(0);
//            if (data instanceof BeautyInfo) {
//                beautyInfo = (BeautyInfo) data;
//            }
//        }
//        
//        if (null != beautyInfo) {
//            mHasMoreData = beautyInfo.hasMore();
//            int index = beautyInfo.getIndex();
//            // 我们只需要当请求的索引为0时，才存签名字符串
//            if (0 == index) {
//                String sign = beautyInfo.getSign();
//                setSign(mAppContext, sign);
//            }
//            
//            String jsonData = beautyInfo.getDataString();
//            // 解析图集数据
//            boolean succeed = ImageJsonParser.parseJson(jsonData, datas);
//            // 如果数据不为空并且需要存数据
//            if (succeed && datas.size() > 0 && saveDataToCache) {
//                // 将JSON字符串存入缓存中
//                saveStringToCacheFile(jsonData, fileName);
//            }
//        }
//    }
    
    /**
     * 检查缓存数据是否存在
     * 
     * @return true表示存在，false不存在
     */
    private boolean checkCacheDataExist() {
        File file = new File(mDataCacheDir, formatCacheName(0));
        return file.exists();
    }
    
    /**
     * 复制预置数据到缓存目录 
     * 
     * @param context context
     */
    private void copyPresetToCache(Context context) {
        AssetManager assetMgr = context.getAssets();
        try {
            String[] fileNames = assetMgr.list(ASSETS_PRESET_FILE_NAME);
            if (null != fileNames) {
                for (String fileName : fileNames) {
                    File dstFile = new File(mDataCacheDir, fileName);
                    InputStream is = assetMgr.open(ASSETS_PRESET_FILE_NAME + "/" + fileName);
                    Utils.streamToFile(is, dstFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 得到最后更新的时间
     * 
     * @param context context
     * @return 更新时间
     */
    public static long getLastUpdateTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            return prefs.getLong(KEY_LAST_UPDATE_TIME, 0);
        }
        
        return 0;
    }
    
    /**
     * 保存最后更新时间
     * 
     * @param context context
     * @param lastUpdateTime 更新时间
     */
    public static void setLastUpdateTime(Context context, long lastUpdateTime) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            Editor editor = prefs.edit();
            if (null != editor) {
                editor.putLong(KEY_LAST_UPDATE_TIME, lastUpdateTime);
                editor.commit();
            }
        }
    }
    
    /**
     * 设置复制预置数据的标志量
     * 
     * @param context context
     * @param flag flag
     */
    public static void setCopyPresetData(Context context, boolean flag) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            Editor editor = prefs.edit();
            if (null != editor) {
                editor.putBoolean(KEY_COPY_RRESET_DATE, flag);
                editor.commit();
            }
        }
    }
    
    /**
     * 得到标志量
     * 
     * @param context context
     * @return 标志量
     */
    public static boolean hasCopyPresetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            return prefs.getBoolean(KEY_COPY_RRESET_DATE, false);
        }
        
        return false;
    }
    
    /**
     * 设置请求得到的签名
     * 
     * @param context context
     * @param sign sign
     */
    public static void setSign(Context context, String sign) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            Editor editor = prefs.edit();
            if (null != editor) {
                editor.putString(KEY_REQUEST_SIGN, sign);
                editor.commit();
            }
        }
    }
    
    /**
     * 得到签名
     * 
     * @param context context
     * @return 签名
     */
    public static String getSign(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            return prefs.getString(KEY_REQUEST_SIGN, "");
        }
        
        return "";
    }
    
    /**
     * 是否显示用户引导
     * 
     * @param context context
     * @return true表示显示，false表示不显示
     */
    public static boolean showUserGuide(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            return prefs.getBoolean(KEY_SHOW_USER_GUIDE, true);
        }
        
        return true;
    }
    
    /**
     * 设置是否显示用户引导
     * 
     * @param context context
     * @param show true表示显示，false表示不显示
     */
    public static void setShowUserGuide(Context context, boolean show) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            Editor editor = prefs.edit();
            if (null != editor) {
                editor.putBoolean(KEY_SHOW_USER_GUIDE, show);
                editor.commit();
            }
        }
    }
    
    /**
     * 设置缓存更新的标志量，当美图首页显示时，程序会自动检查更新，如果更新的话，会将数据存在缓存中
     * 当下一次进入界面时，再从缓存中加载出最新的数据，因此，我们需要记录一下缓存数据更新的flag。
     * 
     * @param context context
     * @param update 是否更新
     */
    public static void setCacheUpdateFlag(Context context, boolean update) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            Editor editor = prefs.edit();
            if (null != editor) {
                editor.putBoolean(KEY_CACHE_UPDATE, update);
                editor.commit();
            }
        }
    }
    
    /**
     * 指示缓存是否更新
     * 
     * @param context context
     * @return 更新标志量
     */
    public static boolean hasCacheUpdated(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PERF_NAME, 0);
        if (null != prefs) {
            return prefs.getBoolean(KEY_CACHE_UPDATE, false);
        }
        
        return false;
    }
}
