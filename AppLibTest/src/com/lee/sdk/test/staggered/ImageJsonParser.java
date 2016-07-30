package com.lee.sdk.test.staggered;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

/**
 * 这个类负责解析美图数据JSON字符串
 * 
 * @author Li Hong
 * 
 * @since 2013-7-27
 */
public final class ImageJsonParser {
    
    /*****************************************************************
     * JSON的格式如下：
     * 
     *   [
     *       {
     *           "id":"2515",
     *           "title":"曾曾知性杂志大片",
     *           "img":"http://xxx.jpg",
     *           "count":2,
     *           "width":481,
     *           "height":636,
     *           "detail":[
     *               {
     *                   "img":"http://cdn01.baidu-img.cn/de0de39e3198538e89a95e0220bb1e8a.jpg"
     *               },
     *               {
     *                   "img":"http://cdn01.baidu-img.cn/2fc4bc6dc75b35e2df66927184007b58.jpg"
     *               },
     *           ]
     *       }
     *   ] 
     *******************************************************************/
    
    /**键: 图集ID*/
    private static final String KEY_ID = "id";
    /**键: 图集标题*/
    private static final String KEY_TITLE = "title";
    /**键: 图集封面URL*/
    private static final String KEY_IMG = "img";
    /**键: 图集中的图片数量*/
    private static final String KEY_COUNT = "count";
    /**键: 图集封面宽度*/
    private static final String KEY_WIDTH = "width";
    /**键: 图集封面高度*/
    private static final String KEY_HEIGHT = "height";
    /**键: 图集中图片*/
    private static final String KEY_DETAIL = "detail";

    /**
     * 构造方法
     */
    private ImageJsonParser() {
        
    }
    
    /**
     * 解析美图数据JSON字符串。
     *  
     * @param jsonString JSON字符串
     * @param albumList 填充的数据列表
     * @return true表示成功，false表示失败
     */
    public static boolean parseJson(String jsonString, List<ImageAlbumItem> albumList) {
        if (TextUtils.isEmpty(jsonString)) {
            return false;
        }
        
        boolean succeed = false;
        
        try {
            JSONArray albumsArray = new JSONArray(jsonString);
            int length = albumsArray.length();
            for (int i = 0; i < length; ++i) {
                JSONObject albumObj = (JSONObject) albumsArray.get(i);
                if (null == albumObj) {
                    continue;
                }
                
                String id = albumObj.has(KEY_ID) ? albumObj.getString(KEY_ID) : "0";
                String title = albumObj.has(KEY_TITLE) ? albumObj.getString(KEY_TITLE) : "";
                String url = albumObj.has(KEY_IMG) ? albumObj.getString(KEY_IMG) : "";
                int count = albumObj.has(KEY_COUNT) ? parseInt(albumObj.getString(KEY_COUNT)) : 0;
                int width = albumObj.has(KEY_WIDTH) ? parseInt(albumObj.getString(KEY_WIDTH)) : 0;
                int height = albumObj.has(KEY_HEIGHT) ? parseInt(albumObj.getString(KEY_HEIGHT)) : 0;
                
                ImageAlbumItem albumItem = new ImageAlbumItem();
                albumItem.setAlbumId(id);
                albumItem.setTitle(title);
                albumItem.setThumbUrl(url);
                albumItem.setWidth(width);
                albumItem.setPictureCount(count);
                albumItem.setHeight(height);
                
                // 解析图集中的图片数据
                parsePictureDetailItems(albumObj, albumItem);
                
                // 如果图集中图片的个数为0，抛弃这个图集
                if (albumItem.getPictureCount() <= 0) {
                    continue;
                }
                
                // TODO: for test
                url = url.replace("b10000_10000", "b360_10000");
                albumItem.setThumbUrl(url);
                
                albumList.add(albumItem);
            }
            
            succeed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return succeed;
    }
    
    /**
     * 解析图集中的图片
     * 
     * @param albumObj JSON对象
     * @param albumItem 图集对象
     */
    private static void parsePictureDetailItems(JSONObject albumObj, ImageAlbumItem albumItem) {
        try {
            JSONArray detailArray = albumObj.has(KEY_DETAIL) ? albumObj.getJSONArray(KEY_DETAIL) : null;
            if (null != detailArray) {
                int detailCount = detailArray.length();
                for (int index = 0; index < detailCount; ++index) {
                    JSONObject detailObj = (JSONObject) detailArray.get(index);
                    if (null == detailObj) {
                        continue;
                    }
                    
                    if (detailObj.has(KEY_IMG)) {
                        String detailUrl = detailObj.getString(KEY_IMG);
                        ImageDetailItem detailItem = new ImageDetailItem();
                        detailItem.setImageUrl(detailUrl);
                        albumItem.addPictureDetailItem(detailItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 字符串转成整型。
     * 
     * @param value 字符串
     * @return 返回转换后的数字
     */
    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}
