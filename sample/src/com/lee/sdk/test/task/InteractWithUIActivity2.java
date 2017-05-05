package com.lee.sdk.test.task;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskManager.IStateChangeListener;
import com.lee.sdk.task.TaskManager.State;
import com.lee.sdk.task.TaskOperation;

public class InteractWithUIActivity2 extends TaskManagerActivity.TaskManagerBaseActivity {
    @Override
    public void performTest() {
        interactWithUI2();
    }

    // ======================================================================

    ProgressDialog mProgressDialog = null;

    private void interactWithUI2() {
        final TaskManager taskManager = new TaskManager(" InteractWithUI2 ==========");

        taskManager.setStateChangeListener(new IStateChangeListener() {
            @Override
            public void onStateChanged(TaskManager taskManager, State oldState, State newState) {
                Toast.makeText(InteractWithUIActivity2.this, " onStateChanged state = " + newState, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        taskManager.next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                mProgressDialog = new ProgressDialog(InteractWithUIActivity2.this);
                mProgressDialog.setTitle("Download");
                mProgressDialog.setMessage("Downlonding data from server...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        taskManager.resume();
                        taskManager.cancelCurrentTask();
                    }
                });
                mProgressDialog.show();

                return null;
            }
        }).next(new Task(Task.RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                for (int i = 1; i <= 100; ++i) {
                    if (this.isCancelled()) {
                        break;
                    }

                    taskManager.publishProgress(i);

                    if (i == 20 || i == 50 || i == 90) {
                        // Post execute, the task is running in UI thread.
                        taskManager.postExecute(new Task(Task.RunningStatus.UI_THREAD) {
                            @Override
                            public TaskOperation onExecute(TaskOperation operation) {
                                createQuestionDialog2(taskManager).show();

                                return null;
                            }
                        });

                        taskManager.pause();
                    }

                    sleep(100);
                }

                return null;
            }

            @Override
            public void onProgressUpdate(Object progresses) {
                if (null != mProgressDialog) {
                    if (null != progresses) {
                        int value = (Integer) progresses;
                        mProgressDialog.setProgress(value);
                    }
                }
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                if (null != mProgressDialog && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

                return null;
            }
        }).execute();
    }

    // ================================================================

    protected Dialog createQuestionDialog2(final TaskManager taskManager) {
        Dialog dialog = new AlertDialog.Builder(this).setTitle("Replace File")
                .setMessage("Do you want to replace file?\n").setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        taskManager.resume();
                    }
                }).setNegativeButton("No", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        taskManager.resume();
                    }
                }).create();

        return dialog;
    }
}
