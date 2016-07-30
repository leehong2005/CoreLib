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

import android.annotation.SuppressLint;
import java.util.HashMap;

/**
 * This class implements the interface IDataObject to provide data for drag and drop.
 * 
 * @author Li Hong
 * @date 2011/08/31
 */
@SuppressLint("UseSparseArrays")
public class DataObject implements IDataObject {
    /**
     * The data type pair.
     */
    private HashMap<Integer, Object> mDataTypeObjPair = new HashMap<Integer, Object>();

    /**
     * Set the useful data to the view.
     * 
     * @param obj The data will be setted;
     * @param dataType The data type such as {@link IDataObject#DATA_TYPE} or
     *            {@link IDataObject#DATA_BOOKSHELF}
     */
    @Override
    public void setData(Object obj, int dataType) {
        Integer key = Integer.valueOf(dataType);

        mDataTypeObjPair.put(key, obj);
    }

    /**
     * Get the data type.
     * 
     * @return The data type such as {@link IDataObject#DATA_TYPE} or
     *         {@link IDataObject#DATA_BOOKSHELF}
     */
    @Override
    public int getDataType() {
        return 0;
    }

    /**
     * Get the data.
     * 
     * @return The data object.
     */
    @Override
    public Object getData(int dataType) {
        return mDataTypeObjPair.get(Integer.valueOf(dataType));
    }
}
