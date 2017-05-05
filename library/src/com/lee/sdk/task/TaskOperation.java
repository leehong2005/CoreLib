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

import java.util.ArrayList;

import com.lee.sdk.task.TaskManager.TaskManagerState;

/**
 * The task operation, it wraps the task parameter, etc.
 * 
 * @author Li Hong
 * 
 * @since 2012/10/30
 */
public class TaskOperation {
    /**
     * The task parameter.
     */
    private Object[] mNextTaskParams = null;

    /**
     * The task manager status.
     */
    private TaskManagerState mTaskManagerStatus = TaskManagerState.CONTINUE;

    /**
     * The constructor method.
     */
    public TaskOperation() {
    }

    /**
     * The constructor method.
     * 
     * @param nextTaskParams paramter
     */
    public TaskOperation(Object[] nextTaskParams) {
        mNextTaskParams = nextTaskParams;
    }

    /**
     * The constructor method.
     * 
     * @param operation operation
     */
    public TaskOperation(TaskOperation operation) {
        setTaskParams(operation);
    }

    /**
     * Get the task parameter.
     * 
     * @return parameters
     */
    public Object[] getTaskParams() {
        return mNextTaskParams;
    }

    /**
     * Set parameter to empty
     */
    public void setTaskParamsEmpty() {
        mNextTaskParams = null;
    }
    
    /**
     * Set the task parameter.
     * 
     * @param params params
     */
    public void setTaskParams(Object[] params) {
        mNextTaskParams = params;
    }

    /**
     * Set the task parameters.
     * 
     * @param operation operation
     */
    public void setTaskParams(TaskOperation operation) {
        if (operation == this) {
            throw new IllegalArgumentException("The argument can NOT be self.");
        }

        if (null == operation) {
            return;
        }

        Object[] params = operation.getTaskParams();
        if (null == params) {
            return;
        }

        ArrayList<Object> paramsList = new ArrayList<Object>();

        if (null != mNextTaskParams) {
            for (Object param : mNextTaskParams) {
                paramsList.add(param);
            }
        }

        for (Object param : params) {
            paramsList.add(param);
        }

        mNextTaskParams = paramsList.toArray();
    }

    /**
     * @param status the mTaskManagerStatus to set
     */
    public void setTaskManagerStatus(TaskManagerState status) {
        mTaskManagerStatus = status;
    }

    /**
     * @return the mTaskManagerStatus
     */
    public TaskManagerState getTaskManagerStatus() {
        return mTaskManagerStatus;
    }

    /**
     * Append the specified parameter to the end of the parameter list.
     * 
     * @param param param
     */
    public void appendTaskParam(Object param) {
        appendTaskParams(new Object[] { param });
    }

    /**
     * Append the specified parameter to the end of the parameter list.
     * 
     * @param params params
     */
    public void appendTaskParams(Object[] params) {
        if (null != params) {
            TaskOperation operation = new TaskOperation(params);
            setTaskParams(operation);
        }
    }
}
