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

import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * This class defines method to do drag
 * 
 * @author Li Hong
 * @date 2013/03/04
 */
public class DragHelper {
    /**
     * Call this method to start drag.
     * 
     * @param context The Context object.
     * @param dragSource The drag source.
     * 
     * @return True if succeed otherwise false.
     */
    public static boolean startDragDrop(Context context, IDragSource dragSource) {
        if (context instanceof Activity) {
            View docorView = ((Activity) context).getWindow().getDecorView();
            if (docorView instanceof IDragLayout) {
                ((IDragLayout) docorView).startDrag(dragSource);
                return true;
            }
        }

        return false;
    }
}
