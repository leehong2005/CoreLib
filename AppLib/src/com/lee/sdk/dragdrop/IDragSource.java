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

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Interface defining an object where drag operations originate.
 * 
 * @author Li Hong
 * @date 2013/03/05
 */
public interface IDragSource {
    /**
     * Whether the object can be dragged.
     * 
     * @param params The drag event params.
     * @return true if the object can be dragged, otherwise return false.
     */
    public boolean canBeDragged(DragParams params);

    /**
     * Whether control the starting drag by itself. The derived class typically call
     * {@link DragHelper#startDragDrop(android.content.Context, IDragSource)} method to start the
     * drag runtime.
     * 
     * @return True if control the starting by yourself, otherwise false.
     */
    public boolean startDragBySelf();

    /**
     * Create the drag view bitmap.
     * 
     * @param point The left and top point that bitmap will show in the IDragSource coordinate.
     * @return The bitmap from the IDragsource.
     */
    public Bitmap getDragBitmap(Point point, DragParams params);

    /**
     * Called when the drop was completed. Call this method to set a IDragStartControl for Drag
     * Source.
     * 
     * @param target The drop target.
     * @param info The drag event params.
     * @param success Whether the drop is successful.
     */
    public void onDropCompleted(IDropTarget target, DragParams params, boolean success);
}
