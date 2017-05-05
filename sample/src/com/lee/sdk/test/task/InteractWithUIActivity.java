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
import com.lee.sdk.test.utils.DownloadUtil;
import com.lee.sdk.test.utils.DownloadUtil.IDownloadListener;

public class InteractWithUIActivity extends TaskManagerActivity.TaskManagerBaseActivity {
    @Override
    public void performTest() {
        interactWithUI();
    }

    ProgressDialog mProgressDialog = null;
    DownloadUtil mDownloadUtil = null;
    int mDownloadErrorCode = 0;
    Dialog mAskDialog = null;
    boolean mAgree = false;

    private void interactWithUI() {
        final TaskManager taskManager = new TaskManager(" InteractWithUI ==========");

        taskManager.setStateChangeListener(new IStateChangeListener() {
            @Override
            public void onStateChanged(TaskManager taskManager, State oldState, State newState) {
                Toast.makeText(InteractWithUIActivity.this, " onStateChanged state = " + newState, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        taskManager.next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                mProgressDialog = new ProgressDialog(InteractWithUIActivity.this);
                mProgressDialog.setTitle("Download");
                mProgressDialog.setMessage("Downlonding data from server...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        taskManager.cancelCurrentTask();
                        if (null != mDownloadUtil) {
                            mDownloadUtil.cancelDownload();
                        }
                        taskManager.removeTasks();
                    }
                });
                mProgressDialog.show();

                final String DOWNLOAD_URL = "http://192.168.5.107/20111105.zip";
                final String DOWNLOAD_DIR = "/data/data/com.lee.sdk.test/downloads";

                return new TaskOperation(new Object[] { DOWNLOAD_URL, DOWNLOAD_DIR });
            }
        }).next(new Task(Task.RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                if (null != params) {
                    if (params.length >= 2) {
                        String url = (String) params[0];
                        String dir = (String) params[1];

                        doDownload(taskManager, url, dir);
                    }
                }

                operation.setTaskParams(new Object[] { mDownloadErrorCode });

                return operation;
            }

            @Override
            public void onProgressUpdate(Object progresses) {
                if (null != mProgressDialog) {
                    if (null != progresses) {
                        mProgressDialog.setProgress((Integer) progresses);
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

                Object[] params = operation.getTaskParams();
                int errorCode = (Integer) params[0];
                if (errorCode < 0) {
                    createConfirmDialog(taskManager).show();
                    taskManager.removeTasks();
                } else {
                    mAskDialog = createQuestionDialog(taskManager);
                    mAskDialog.show();
                    operation.setTaskManagerStatus(TaskManager.TaskManagerState.PAUSE);
                }

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                mProgressDialog = new ProgressDialog(InteractWithUIActivity.this);
                mProgressDialog.setTitle("UnZip");
                mProgressDialog.setMessage("Unzip the file to the folder...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                operation.setTaskManagerStatus(TaskManager.TaskManagerState.CONTINUE);

                return operation;
            }
        }).next(new Task(Task.RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                sleep(5000);

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

    // ==================================================================

    protected void doDownload(final TaskManager taskManager, String url, String dir) {
        mDownloadErrorCode = 0;
        mDownloadUtil = new DownloadUtil(this);
        mDownloadUtil.setDownloadDir(dir);
        mDownloadUtil.setDownloadListener(new IDownloadListener() {
            @Override
            public void onInProgress(String url, int percent) {
                taskManager.publishProgress(percent);
            }

            @Override
            public void onDownloadStart(String url) {
            }

            @Override
            public void onDownloadFailed(String url, int errorCode) {
                mDownloadErrorCode = errorCode;
                sleep(2000);
            }

            @Override
            public void onDownloadComplete(String url) {
            }
        });

        mDownloadUtil.startDownload(url);
    }

    // =================================================================

    protected Dialog createConfirmDialog(final TaskManager taskManager) {
        Dialog dialog = null;

        dialog = new AlertDialog.Builder(this).setTitle("Download Error")
                .setMessage("There is an error while downloading.\n").setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        return dialog;
    }

    // =====================================================================

    protected Dialog createQuestionDialog(final TaskManager taskManager) {
        Dialog dialog = null;

        dialog = new AlertDialog.Builder(this).setTitle("Unzip").setMessage("Do you want to unzip?\n")
                .setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAgree = true;
                        dialog.dismiss();
                        taskManager.execute();
                    }
                }).setNegativeButton("No", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAgree = false;
                        dialog.dismiss();
                        taskManager.removeTasks();
                    }
                }).create();

        return dialog;
    }
}
