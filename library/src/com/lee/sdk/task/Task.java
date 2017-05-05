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

package com.lee.sdk.task;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * This method define the task used to do something. Typically you should override
 * {@link #onExecute(TaskOperation)} method to do you things, on the other hand, you also can
 * override the {@link #onProgressUpdate(Object)} method to get the progress of you things.
 * </p>
 * 
 * <p>
 * NOTE: There is an very important thing you should pay attention to, you must specify the task is
 * running on background thread or UI thread, the default flag is true ---- running on background
 * thread.
 * </p>
 * 
 * @author LeeHong
 * 
 * @since 2012/10/30
 */
public abstract class Task {
    /**
     * The id of the task, typically you need NOT set it, if will set automatically when you add
     * this task into {@link TaskManager} class.
     */
    private int mId = 0;

    /**
     * The task name.
     */
    private String mName = null;

    /**
     * Indicate this task is canceled or not.
     */
    private AtomicBoolean mCancelled = new AtomicBoolean(false);

    /**
     * The task status, default value is {@link Status#PENDING}.
     */
    private volatile Status mStatus = Status.PENDING;

    /**
     * The running status, default value is {@link RunningStatus#UI_THREAD}.
     */
    private volatile RunningStatus mRunStatus = RunningStatus.UI_THREAD;

    /**
     * Indicates the current status of the task. Each status will be set only once during the
     * lifetime of a task.
     */
    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,

        /**
         * Indicates that the task is running.
         */
        RUNNING,

        /**
         * Indicates that {@link Task#onExecute} has finished.
         */
        FINISHED,
    }

    /**
     * Indicate the task running status.
     */
    public enum RunningStatus {
        /**
         * Indicate the task is running in the background thread.
         */
        WORK_THREAD,

        /**
         * Indicate the task is running in the UI thread.
         */
        UI_THREAD,
    }

    /**
     * The constructor method.
     * 
     * @param task Task object.
     */
    public Task(Task task) {
        this.mRunStatus = task.mRunStatus;
        this.mName = task.mName;
        this.mStatus = task.mStatus;
    }

    /**
     * The constructor method.
     * 
     * @param status indicate the task is running in background thread or not.
     */
    public Task(RunningStatus status) {
        this(status, null);
    }

    /**
     * The constructor method.
     * 
     * @param status status
     * @param name task name
     */
    public Task(RunningStatus status, String name) {
        mRunStatus = status;
        mName = name;
    }

    /**
     * Override this method to do you works.
     * 
     * @param operation The operation is passed from previous task.
     * 
     * @return Typically you should return the {@link #operation}.
     */
    public abstract TaskOperation onExecute(TaskOperation operation);

    /**
     * Called when change the progress, this method is running in UI thread.
     * 
     * @param progresses progress
     */
    public void onProgressUpdate(Object progresses) {
    }

    /**
     * Cancel the task.
     */
    public void cancel() {
        mCancelled.set(true);
    }

    /**
     * Indicate the task is canceled or not.
     * 
     * @return true is cancelled, other false.
     */
    public boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * Get the running status.
     * 
     * @return status
     */
    public RunningStatus getRunningStatus() {
        return mRunStatus;
    }

    /**
     * Set the name of the task.
     * 
     * @param name The task name.
     */
    public void setTaskName(String name) {
        mName = name;
    }

    /**
     * Get the task name.
     * 
     * @return the task name.
     */
    public String getTaskName() {
        return mName;
    }

    /**
     * Set the status of the task.
     * 
     * @param status status
     */
    public void setStatus(Status status) {
        mStatus = status;
    }

    /**
     * Get the status of the task.
     * 
     * @return status
     */
    public Status getStatus() {
        return mStatus;
    }

    /**
     * Set the id of the task.
     * 
     * @param id id
     */
    public void setTaskId(int id) {
        mId = id;
    }

    /**
     * Get the task id.
     * 
     * @return id.
     */
    public int getTaskId() {
        return mId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name = ").append(mName).append("  ");
        sb.append("id = ").append(mId).append("  ");
        sb.append(super.toString());

        return sb.toString();
    }
}
