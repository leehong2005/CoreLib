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

package com.lee.sdk.dragdrop;

/**
 * This interface define some method of the data associated with the drag view. The view want to
 * implements the drag and drop should implements this interface to transfer data.
 * 
 * @author Li Hong
 * @date 2013/03/05
 */
public interface IDataObject {
    /**
     * Set the useful data to the view.
     * 
     * @param obj The data will be set;
     * @param dataType The data type to indicate the data's property.
     */
    public void setData(Object obj, int dataType);

    /**
     * Get the data.
     * 
     * @return The data object.
     */
    public Object getData(int dataType);

    /**
     * Get the data type.
     * 
     * @return The data type which is set by calling {@link #setData(Object, int)} method.
     */
    public int getDataType();
}
