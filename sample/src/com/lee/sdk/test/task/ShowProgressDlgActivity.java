package com.lee.sdk.test.task;

import android.app.ProgressDialog;
import android.widget.Toast;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskManager.IStateChangeListener;
import com.lee.sdk.task.TaskManager.State;
import com.lee.sdk.task.TaskOperation;

public class ShowProgressDlgActivity extends TaskManagerActivity.TaskManagerBaseActivity {
    @Override
    public void performTest() {
        showProgressDialog();
    }

    ProgressDialog mProgressDialog = null;
    boolean mFirstTime = true;

    private void postShowDialog() {
        this.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.setTitle("Download");
                    mProgressDialog.setMessage("Downlonding data from server...");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }
            }
        });
    }

    private void showProgressDialog() {
        final TaskManager taskManager = new TaskManager("  ShowProgressDlg =============");

        taskManager.setStateChangeListener(new IStateChangeListener() {
            @Override
            public void onStateChanged(TaskManager taskManager, State oldState, State newState) {
                Toast.makeText(ShowProgressDlgActivity.this, " onStateChanged state = " + newState, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        taskManager.next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                mProgressDialog = new ProgressDialog(ShowProgressDlgActivity.this);

                // The test case:
                // 1, if the work thread finishes quickly, user does NOT want to see
                // the progress dialog show and dismiss quick, so, we post to show dialog.
                // 2, if the work thread needs some time to perform, so, the progress
                // dialog may show to user, this is a very good UI experience to tell user
                // some time-consuming things are doing.
                postShowDialog();

                return operation;
            }
        }).next(new Task(Task.RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                if (!mFirstTime) {
                    sleep(2000);
                }
                mFirstTime = false;

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                if (null != mProgressDialog && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

                return operation;
            }
        }).execute();
    }
}
