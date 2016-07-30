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

package com.lee.sdk.cache.request;

/**
 * 定义了加载数据的接口
 *
 * @author lihong
 * @date 2016/03/10
 */
public interface DataFetcher<T> {
    /**
     * 数据加载的回调
     *
     * @param <T>
     */
    interface DataCallback<T> {
        void onDataReady(T data);
        void onLoadFailed(Exception e);
    }

    void loadData();
    void cleanup();
}
