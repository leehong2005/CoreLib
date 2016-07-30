package com.lee.sdk.test.task;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.Toast;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskManager.IStateChangeListener;
import com.lee.sdk.task.TaskManager.State;
import com.lee.sdk.task.TaskOperation;

public class UpdateProgressDlgActivity extends TaskManagerActivity.TaskManagerBaseActivity {
    @Override
    public void performTest() {
        updateProgressDialog();
    }

    ProgressDialog mProgressDialog = null;

    private void updateProgressDialog() {
        final TaskManager taskManager = new TaskManager("   UpdateProgressDlg =======");

        taskManager.setStateChangeListener(new IStateChangeListener() {
            @Override
            public void onStateChanged(TaskManager taskManager, State oldState, State newState) {
                Toast.makeText(UpdateProgressDlgActivity.this, " onStateChanged state = " + newState,
                        Toast.LENGTH_SHORT).show();
            }
        });

        taskManager.next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                mProgressDialog = new ProgressDialog(UpdateProgressDlgActivity.this);
                mProgressDialog.setTitle("Download");
                mProgressDialog.setMessage("Downlonding data from server...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                        Log.d("leehong2", " ******** task cancel: " + this.toString());
                        break;
                    }

                    taskManager.publishProgress(i);

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
}
