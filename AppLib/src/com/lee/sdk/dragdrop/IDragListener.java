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
 * Drag listener
 * 
 * @author Li Hong
 * @since 2014-5-22
 */
public interface IDragListener {
    /**
     * Indicate the listener can be notified or not. If you sometimes do not want to get
     * notification from the drag runtime, you can return false to reject the notification.
     * 
     * @param source The drag source.
     * @param target The drag target.
     * @param data The drag data.
     * 
     * @return true if the listener implementation does not want to notify, otherwise return false.
     */
    @Deprecated
    public boolean disableNotify(IDragSource source, IDropTarget target, IDataObject data);

    /**
     * Called when the dragging operation is starting.
     * 
     * @param source An object representing where the drag originated.
     * @param info The data associated with the object that is being dragged.
     * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE} or
     *            {@link DragController#DRAG_ACTION_COPY}.
     */
    public void onDragStart(IDragSource source, IDataObject info, int dragAction);

    /**
     * Called when being dragging.
     * 
     * @param params The drag event parameter.
     */
    public void onDragging(DragParams params);

    /**
     * The drag has ended.
     */
    public void onDragEnd();
}
