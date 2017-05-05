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

import java.util.HashMap;
import java.util.LinkedList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.Task.Status;

/**
 * This class is used to manager the tasks so that you can add many tasks into the task manger and
 * these tasks will be running one by one.
 * 
 * <h2>Example:</h2>
 * 
 * <pre class="prettyprint">
 * private void showProgressDialog() {
 *     final ProgressDialog mProgressDialog = null;
 *     final TaskManager taskManager = new TaskManager(&quot;ShowProgressDlg&quot;);
 * 
 *     // Set the state change listener.
 *     taskManager.setStateChangeListener(new IStateChangeListener() {
 *         public void onStateChanged(TaskManager taskManager, State oldState, State newState) {
 *             Toast.makeText(ShowProgressDlgActivity.this,
 *              &quot; onStateChanged state = &quot; + newState, Toast.LENGTH_SHORT)
 *                     .show();
 *         }
 *     });
 * 
 *     taskManager.next(new Task(Task.RunningStatus.UI_THREAD) {
 *         public TaskOperation onExecute(TaskOperation operation) {
 *             mProgressDialog = new ProgressDialog(ShowProgressDlgActivity.this);
 *             mProgressDialog.setTitle(&quot;Download&quot;);
 *             mProgressDialog.setMessage(&quot;Downlonding data from server...&quot;);
 *             mProgressDialog.setCancelable(false);
 *             mProgressDialog.show();
 * 
 *             return null;
 *         }
 *     }).next(new Task(Task.RunningStatus.WORK_THREAD) {
 *         public TaskOperation onExecute(TaskOperation operation) {
 *             // Simulate the work thread.
 *             sleep(5000);
 * 
 *             return null;
 *         }
 *     }).next(new Task(Task.RunningStatus.UI_THREAD) {
 *         public TaskOperation onExecute(TaskOperation operation) {
 *             if (null != mProgressDialog &amp;&amp; mProgressDialog.isShowing()) {
 *                 mProgressDialog.dismiss();
 *                 mProgressDialog = null;
 *             }
 * 
 *             return null;
 *         }
 *     }).execute(); // Call this method to execute these tasks.
 * }
 * </pre>
 * 
 * <h2>Note:</h2>
 * 
 * <pre>
 * The {@link Task} class must be specified the task running state, one of the enum {@link Task#RunningStatus}.
 * </pre>
 * 
 * @author LeeHong
 * 
 * @since 2012/10/30
 * 
 * @see {@link Task}
 * @see {@link TaskOperation}
 */
public class TaskManager {
    /**
     * Execute task message.
     */
    private static final int MESSAGE_POST_EXECUTE = 0x01;

    /**
     * Update progress message.
     */
    private static final int MESSAGE_POST_PROGRESS = 0x02;

    /**
     * The state change listener.
     */
    public interface IStateChangeListener {
        /**
         * Called when the task manager's state is changed. This method will be called in UI thread.
         * 
         * @param taskManager Which task manager's state changed.
         * @param oldState The old state.
         * @param newState The new state.
         */
        void onStateChanged(TaskManager taskManager, State oldState, State newState);
    }

    /**
     * A representation of a task manager's state. A given thread may only be in one state at a
     * time.
     */
    public enum State {
        /**
         * The task manager has been created, but has never been started.
         */
        NEW,

        /**
         * Indicate the task manager is running one task.
         */
        RUNNING,

        /**
         * Indicate the task manager is paused, typically call {@link #pause()} method.
         */
        PAUSED,

        /**
         * All tasks are finished.
         */
        FINISHED,
        
        /**
         * Ready
         */
        READY,
    }

    /**
     * The status of the {@link TaskManager} class.
     */
    public enum TaskManagerState {
        /**
         * Continue the task manager to run next task.
         */
        CONTINUE,

        /**
         * Indicate the task manager pause to run next task.
         */
        PAUSE,
    }
    
    /**
     * TAG
     */
    private static final String TAG = "TaskManager";

    /**
     * DEBUG
     */
    private static final boolean DEBUG = true;
    
    /**
     * The running task manager collection.
     */
    private static HashMap<String, TaskManager> sTaskManagers = new HashMap<String, TaskManager>();

    /**
     * The task list.
     */
    private LinkedList<Task> mTaskList = new LinkedList<Task>();

    /**
     * The task operation, it will pass from first task to the last task.
     */
    private TaskOperation mTaskOperation = new TaskOperation();

    /**
     * The running thread worker, it own a looper which will be alive until you call
     * {@link ThreadWorker#quit()} method.
     */
    private ThreadWorker mThreadWorker = null;

    /**
     * The current perform task, may be null.
     */
    private Task mCurTask = null;

    /**
     * The state of the task manager.
     */
    private State mState = State.NEW;

    /**
     * The name of the task manager.
     */
    private String mName = null;

    /**
     * The listener.
     */
    private IStateChangeListener mListener = null;

    /**
     * The background thread handler, which is associated to a background thread looper.
     */
    private Handler mThreadHandler = null;
    
    /**
     * Indicate quit thread looper if finishing all tasks.
     */
    private boolean mAutoQuit = true;

    /**
     * The UI thread handler.
     */
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_POST_EXECUTE:
                Task task = (Task) msg.obj;
                executeTask(task);
                // Try to run next task if possible.
                runNextTask();
                break;

            case MESSAGE_POST_PROGRESS:
                postProgress(msg.obj);
                break;
            
            default:
                break;
            }
        }
    };

    /**
     * The constructor method.
     */
    public TaskManager() {
    }

    /**
     * The constructor method.
     * 
     * @param name The name of the task manager.
     */
    public TaskManager(String name) {
        this(name, true);
    }
    
    /**
     * The constructor method.
     * 
     * @param name  The name of the task manager.
     * @param autoQuit Quit or not after all task being performed.
     */
    public TaskManager(String name, boolean autoQuit) {
        mName = name;
        mAutoQuit = autoQuit;
    }

    /**
     * Add the task to {@link TaskManager} class.
     * 
     * @param task The task.
     * 
     * @return the {@link TaskManager} object.
     */
    public TaskManager next(Task task) {
        if (null != task) {
            synchronized (mTaskList) {
                int id = mTaskList.size() + 1;
                task.setTaskId(id);
                mTaskList.add(task);
            }
        } else {
            throw new NullPointerException("task is null");
        }

        return this;
    }

    /**
     * Start to execute the tasks in the task manager.
     */
    public void execute() {
        if (mTaskList.size() > 0) {
            startThread();

            // Set the task to RUNNING.
            setState(State.RUNNING);

            // Perform the runnable in the handler which is associated to the background thread.
            mThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    doInBackground();
                }
            });
        } else {
            if (mAutoQuit) {
                quitLooper();
            } else {
                // Set the task to READY.
                setState(State.READY);
            }
        }
    }

    /**
     * Start to execute the tasks in the task manager with the specified parameter.
     * 
     * @param operation The task operation contains the task parameter.
     */
    public void execute(TaskOperation operation) {
        if (null != operation) {
            mTaskOperation = operation;
        }

        execute();
    }

    /**
     * Post execute a task which will be running in UI thread.
     * 
     * @param task the task to be running.
     */
    public void postExecute(Task task) {
        if (null == task) {
            throw new NullPointerException("Task can NOT be null.");
        }

        final Task runTask = task;

        // If the task running status is UI_THREAD.
        if (RunningStatus.UI_THREAD == runTask.getRunningStatus()) {
            // The task is running in UI thread.
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    executeTask(runTask);
                }
            });
        }
    }

    /**
     * Publish the task progress, if you call this method, the {@link Task#onProgressUpdate(Object)}
     * method will be called, which is running in the UI thread.
     * 
     * @param progresses The progress.
     */
    public void publishProgress(Object progresses) {
        mUIHandler.obtainMessage(MESSAGE_POST_PROGRESS, progresses).sendToTarget();
    }

    /**
     * Cancel the current running task.
     */
    public void cancelCurrentTask() {
        if (null != mCurTask) {
            mCurTask.cancel();
        }
    }

    /**
     * Remove the tasks in the list.
     */
    public void removeTasks() {
        synchronized (mTaskList) {
            if (mTaskList.size() > 0) {
                mTaskList.clear();
                quitLooper();
            }
        }
    }

    /**
     * Remove the specified task.
     * 
     * @param task The task to be removed.
     */
    public void removeTask(Task task) {
        synchronized (mTaskList) {
            mTaskList.remove(task);

            if (mTaskList.isEmpty()) {
                quitLooper();
            }
        }
    }

    /**
     * Set the state change listener.
     * 
     * @param listener listener
     */
    public void setStateChangeListener(IStateChangeListener listener) {
        mListener = listener;
    }

    /**
     * Get the task operation.
     * 
     * @return task operation object.
     */
    public TaskOperation getTaskOperation() {
        return mTaskOperation;
    }

    /**
     * @return the mName
     */
    public String getName() {
        return mName;
    }

    /**
     * Pause the worker thread.
     */
    public void pause() {
        if (null != mThreadWorker) {
            setState(State.PAUSED);

            mThreadWorker.pause();
        }
    }

    /**
     * Resume the worker thread from the waiting status.
     */
    public void resume() {
        if (null != mThreadWorker) {
            setState(State.RUNNING);

            mThreadWorker.restart();
        }
    }

    /**
     * Quit the looper so that the thread can finish correctly.
     */
    public void quitLooper() {
        if (null != mThreadWorker) {
            mThreadWorker.quit();
            mThreadWorker = null;
        }

        mThreadHandler = null;

        // Set the task to FINISHED.
        setState(State.FINISHED);
    }

    /**
     * Blocks the current thread ({@link Thread#currentThread()}) until the receiver finishes its
     * execution and dies.
     */
    public final void join() {
        if (null != mThreadWorker) {
            mThreadWorker.join();
        }
    }

    /**
     * Get the task manager state.
     * 
     * @return State
     */
    public State getState() {
        return mState;
    }

    /**
     * 指示任务是否结束
     * 
     * @return true结束，false未结束
     */
    public boolean isFinished() {
        return (mState == State.FINISHED);
    }
    
    /**
     * Get the running task manager.
     * 
     * @return HashMap<String, TaskManager>, the task manager's name is the key, and the task
     *         manager object is the value.
     */
    public static HashMap<String, TaskManager> getTaskManagers() {
        return sTaskManagers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name = ").append(mName).append("  ");
        sb.append("State = ").append(mState).append("  ");
        sb.append(super.toString());

        return sb.toString();
    }

    /**
     * print task execute status
     * 
     * @param task task.
     */
    protected void printExecuteTaskState(Task task) {
        if (DEBUG) {
            Log.d(TAG, "    Executer the task: " + task.toString());
        }
    }

    /**
     * Set the state.
     * 
     * @param state state
     */
    private void setState(State state) {
        final State oldState = mState;
        final State newState = state;
        mState = state;

        if (mState == State.FINISHED) {
            popTaskManager(this);
        } else {
            pushTaskManager(this);
        }

        if (oldState != newState) {
            printTaskManagerState(oldState, newState);
            performStateChange(oldState, newState);
        }
    }

    /**
     * Call this method to start the work thread if can.
     */
    private void startThread() {
        if (null == mThreadWorker) {
            String name = TextUtils.isEmpty(mName) ? this.toString() : mName;
            String threadName = "TaskManager_Thread_" + name;
            mThreadWorker = new ThreadWorker(threadName);
            mThreadHandler = new Handler(mThreadWorker.getLooper());
            // Set the task to READY.
            setState(State.READY);
        }
    }

    /**
     * This method is running in the background thread.
     */
    private void doInBackground() {
        mCurTask = null;

        if (mTaskList.isEmpty()) {
            return;
        }

        Task task = mTaskList.get(0);
        mCurTask = task;

        // Remove the first item in the list.
        synchronized (mTaskList) {
            mTaskList.remove(0);
        }

        // If the task is allowed to be running in background thread, we execute the task
        // now, the doInBackground() method is running in the background thread.
        switch (task.getRunningStatus()) {
        case WORK_THREAD:
            executeTask(task);
            // Try to run next task if possible.
            runNextTask();
            break;

        case UI_THREAD:
            // Send a message to the UI handler to executer the task.
            mUIHandler.obtainMessage(MESSAGE_POST_EXECUTE, task).sendToTarget();
            break;
            
        default:
            break;
        }
    }

    /**
     * Run the next task.
     */
    private void runNextTask() {
        // If run next, call the execute() method again.
        if (isRunNext()) {
            execute();
        }
    }

    /**
     * Check whether run the next task if has one.
     * 
     * @return true if run next task, otherwise false.
     */
    private boolean isRunNext() {
        boolean isRunNext = true;
        boolean hasNext = false;

        if (null != mTaskOperation) {
            isRunNext = (mTaskOperation.getTaskManagerStatus() == TaskManagerState.CONTINUE);
        }

        if (null != mTaskList) {
            hasNext = mTaskList.size() > 0;
        }

        // No next task, quit the thread.
        if (!hasNext) {
            if (mAutoQuit) {
                quitLooper();
            } else {
                // Set state to READY.
                setState(State.READY);
            }
        }

        return (isRunNext && hasNext);
    }

    /**
     * Execute the task, if will call {@link Task#onExecute(TaskOperation)} method.
     * 
     * @param task The task object.
     */
    private void executeTask(Task task) {
        if (null != task) {

            // Set the status of the task.
            task.setStatus(Status.RUNNING);

            // Print the task state.
            this.printExecuteTaskState(task);

            try {
                // Avoid the exception from task interrupting the task manager works.
                mTaskOperation = task.onExecute(mTaskOperation);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set the status of the task.
            task.setStatus(Status.FINISHED);

            // Print the task state.
            this.printExecuteTaskState(task);
        }
    }

    /**
     * Post the progress, it will call {@link Task#onProgressUpdate(Object progresses)} method.
     * 
     * @param progresses progresses
     */
    private void postProgress(Object progresses) {
        if (null != mCurTask) {
            mCurTask.onProgressUpdate(progresses);
        }
    }

    /**
     * Perform the state change.
     * 
     * @param oldState old state
     * @param newState new state
     */
    private void performStateChange(final State oldState, final State newState) {
        if (null != mListener) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onStateChanged(TaskManager.this, oldState, newState);
                }
            });
        }
    }

    /**
     * Print the task manager information.
     * 
     * @param oldState old state
     * @param newState new state
     */
    private void printTaskManagerState(final State oldState, final State newState) {
        if (DEBUG) {
            Log.d(TAG, "TaskManager state changed, task manager = " + this.toString());
        }
    }

    /**
     * Push the task manager to the list.
     * 
     * @param taskManager task manager
     */
    private static void pushTaskManager(TaskManager taskManager) {
        if (null != taskManager) {
            String name = taskManager.getName();
            if (!TextUtils.isEmpty(name)) {
                sTaskManagers.put(name, taskManager);
            }
        }
    }

    /**
     * Pop the task manager from the list.
     * 
     * @param taskManager task manager
     */
    private static void popTaskManager(TaskManager taskManager) {
        if (null != taskManager) {
            String name = taskManager.getName();
            sTaskManagers.remove(name);
        }
    }
}
