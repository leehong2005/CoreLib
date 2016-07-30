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

import android.os.IBinder;

/**
 * This interface defines the methods for drag root layout.
 * 
 * @author Li Hong
 * @date 2013/03/05
 */
public interface IDragLayout {
    /**
     * Called to start drag.
     * 
     * @param dragSource The IDragSource object.
     * 
     * @return true if succeed to start dragging, otherwise return false.
     */
    public boolean startDrag(IDragSource dragSource);

    /**
     * Set the window taken.
     * 
     * @param token The token of the window that is making the request .
     */
    public void setWindowToken(IBinder token);

    /**
     * Register a callBack that to be invoked when drag status changes. This function should called
     * after the onResume called.
     * 
     * @param listener The callBack that will run.
     */
    public void registerDragListener(IDragListener listener);

    /**
     * Unregister a callBack that to be invoked when drag status changes.
     * 
     * @param listener The callBack that will run.
     */
    public void unregisterDragListener(IDragListener listener);

    /**
     * Cancel the drag event.
     */
    public void cancelDrag();
}
