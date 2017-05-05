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
 * Receive notifications when a drag starts or dragging or stops.
 * 
 * @author Li Hong
 * @date 2013/03/05
 */
public class DragListener implements IDragListener {
    /**
     * Indicate the listener can be notified or not. If you sometimes do not want to get
     * notification from the drag runtime, you can return false to reject the notification.
     * 
     * @param source The drag source.
     * @param target The drag target.
     * @param data The drag data.
     * 
     * @return true if the listener implementation wants to notify, otherwise return false.
     */
    @Deprecated
    @Override
    public boolean disableNotify(IDragSource source, IDropTarget target, IDataObject data) {
        return false;
    }

    /**
     * Called when the dragging operation is starting.
     * 
     * @param source An object representing where the drag originated.
     * @param info The data associated with the object that is being dragged.
     * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE} or
     *            {@link DragController#DRAG_ACTION_COPY}.
     */
    @Override
    public void onDragStart(IDragSource source, IDataObject info, int dragAction) {
        // Do nothing.
    }

    /**
     * Called when being dragging.
     * 
     * @param params The drag event parameter.
     */
    @Override
    public void onDragging(DragParams params) {
        // Do nothing.
    }

    /**
     * The drag has ended.
     */
    @Override
    public void onDragEnd() {
        // Do nothing.
    }
}
