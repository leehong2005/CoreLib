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

package com.lee.sdk.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Map;

/**
 * 该接口定义了加载Bitmap的行为，一般情况下，对应的Mode类可以去实现这个接口。
 * 
 * @author Li Hong
 * 
 * @since 2013-7-23
 */
public interface ILoadImage {
    /**
     * 加载一个bitmap
     * 
     * @return 加载的bitmap
     */
    Bitmap loadImage();
    
    /**
     * 返回当前数据的URL
     * 
     * @return URL
     */
    String getUrl();
    
    /**
     * Compute the sample size as a function of minSideLength and maxNumOfPixels. minSideLength is
     * used to specify that minimal width or height of a bitmap. maxNumOfPixels is used to specify
     * the maximal size in pixels that is tolerable in terms of memory usage.
     * 
     * The function returns a sample size based on the constraints. Both size and minSideLength can
     * be passed in as IImage.UNCONSTRAINED, which indicates no care of the corresponding
     * constraint. The functions prefers returning a sample size that generates a smaller bitmap,
     * unless minSideLength = IImage.UNCONSTRAINED.
     * 
     * Also, the function rounds up the sample size to a power of 2 or multiple of 8 because
     * BitmapFactory only honors sample size this way. For example, BitmapFactory downsamples an
     * image by 2 even though the request is 3. So we round up the sample size to avoid OOM.
     * 
     * @param options options
     * @return sample size
     */
    int getSampleSize(BitmapFactory.Options options);
    
    /**
     * 获取下载时需要添加到http header中的头信息
     * 
     * @return header头信息map
     */
    Map<String, String> getHeader();
}
