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
 * Interface defining an object that can receive a view at the end of a drag operation.
 * 
 * @author Li Hong
 * @date 2013/03/05
 */
public interface IDropTarget {
    /**
     * Handle an object being dropped on the DropTarget
     * 
     * @param params The drag event params.
     */
    public void onDrop(DragParams params);

    /**
     * React to something started to be dragged in the drop target.
     * 
     * @param params The drag event params.
     */
    public void onDragEnter(DragParams params);

    /**
     * React to something being dragged over the drop target.
     * 
     * @param params The drag event params.
     */
    public void onDragOver(DragParams params);

    /**
     * React to something drag leave off the drop target.
     */
    public void onDragLeave(DragParams params);

    /**
     * Check if a drop action can occur at the drop target.
     * 
     * @param params The drag event params.
     * 
     * @return True if the drop will be accepted, false otherwise.
     */
    public boolean acceptDrop(DragParams params);

    /**
     * Called when being dragging.
     * 
     * @param params The drag event params.
     */
    public void onDragging(DragParams params);
}
