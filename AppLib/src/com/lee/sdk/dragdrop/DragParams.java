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
 * This class define the constructor of the dragEventParams.
 * 
 * @author Li Hong
 * @date 2013/03/04
 */
public class DragParams {
    /**
     * DragSource where the drag started.
     */
    private IDragSource mSource = null;

    /**
     * DragTarget where the drag ended.
     */
    private IDropTarget mTarget = null;

    /**
     * The data associated the view that being dragged.
     */
    private IDataObject mObject = null;

    /**
     * The raw x of the left of the drag view.
     */
    private int mRawX = 0;

    /**
     * The raw y of the top of the drag view.
     */
    private int mRawY = 0;

    /**
     * X offset from the upper-left corner of the cell to where we touched.
     */
    private int mX = 0;

    /**
     * Y offset from the upper-left corner of the cell to where we touched.
     */
    private int mY = 0;

    /**
     * Whether control the visibility of the drag view by outside.by default,this is true.
     */
    private boolean mIsDismissDragView = true;

    /**
     * The view that was dragged.
     */
    private DragView mView = null;

    /**
     * The constructor of the DragEventParams.
     * 
     * @param source DragSource where the drag started.
     * @param target DragTarget where the drag ended.
     * @param object The data associated the view that being dragged.
     * @param x The raw x of the left of the drag view.
     * @param y The raw y of the top of the drag view.
     * @param xOffset X offset from the upper-left corner of the cell to where we touched.
     * @param yOffset Y offset from the upper-left corner of the cell to where we touched.
     * @param view The view that was dragged.
     */
    public DragParams(IDragSource source, IDropTarget target, IDataObject object, int rawX, int rawY, int x, int y,
            DragView view) {
        this.mSource = source;
        this.mTarget = target;
        this.mObject = object;
        this.mRawX = rawX;
        this.mRawY = rawY;
        this.mX = x;
        this.mY = y;
        this.mView = view;
    }

    /**
     * Default constructor.
     */
    public DragParams() {
    }

    /**
     * Get the dragSource.
     * 
     * @return The dragSource object.
     */
    public IDragSource getDragSource() {
        return mSource;
    }

    /**
     * Set the drag source.
     * 
     * @param source The drag source object.
     */
    public void setDragSource(IDragSource source) {
        this.mSource = source;
    }

    /**
     * Get the data object.
     * 
     * @return The data object.
     */
    public IDataObject getDataObject() {
        return mObject;
    }

    /**
     * Set the data object.
     * 
     * @param object The data object.
     */
    public void setDataObject(IDataObject object) {
        this.mObject = object;
    }

    /**
     * Get the raw x coordinate.
     * 
     * @return The x coordinate.
     */
    public int getRawX() {
        return mRawX;
    }

    /**
     * Set the x coordinate.
     * 
     * @param x The x coordinate.
     */
    public void setRawX(int x) {
        this.mRawX = x;
    }

    /**
     * Get the raw y coordinate.
     * 
     * @return The y coordinate.
     */
    public int getRawY() {
        return mRawY;
    }

    /**
     * Set the y coordinate.
     * 
     * @param y The y coordinate.
     */
    public void setRawY(int y) {
        this.mRawY = y;
    }

    /**
     * Get the x in its parent.
     * 
     * @return The x in its parent.
     */
    public int getX() {
        return mX;
    }

    /**
     * Set the x in its parent.
     * 
     * @param xOffset The x in its parent.
     */
    public void setX(int x) {
        this.mX = x;
    }

    /**
     * Get the y in its parent.
     * 
     * @return The y in its parent.
     */
    public int getY() {
        return mY;
    }

    /**
     * Set the y in its parent.
     * 
     * @param yOffset The y in its parent.
     */
    public void setY(int y) {
        this.mY = y;
    }

    /**
     * Get the drag view.
     * 
     * @return The view being dragged.
     */
    public DragView getDragView() {
        return mView;
    }

    /**
     * Set the drag view.
     * 
     * @param view The view Being dragged.
     */
    public void setDragView(DragView view) {
        if (this.mView != view) {
            dismissDragView();
        }

        this.mView = view;
    }

    /**
     * Get the drop target.
     * 
     * @return The drop target.
     */
    public IDropTarget getDropTarget() {
        return mTarget;
    }

    /**
     * Set the drop target.
     * 
     * @param target The drop target.
     */
    public void setDropTarget(IDropTarget target) {
        this.mTarget = target;
    }

    /**
     * Whether the outside need control the visibility of the drag view.
     * 
     * @return return true if outside want to control the visibility of the drag view.
     */
    public boolean isAutoDismissDragView() {
        return mIsDismissDragView;
    }

    /**
     * Whether dismiss the drag view.If you set false,you should dismiss by yourself.
     */
    public void setAutoDismissDragView(boolean isDismiss) {
        this.mIsDismissDragView = isDismiss;
    }

    /**
     * Remove the drag view.
     */
    public void dismissDragView() {
        if (null != mView) {
            mView.removeFromWindow();
            mView = null;
        }
    }
}
