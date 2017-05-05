package com.lee.sdk.test.task;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskManager.IStateChangeListener;
import com.lee.sdk.task.TaskManager.State;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.R;
import com.lee.sdk.test.task.TaskManagerActivity.MyTaskManager;
import com.lee.sdk.utils.BitmapUtil;
import com.lee.sdk.utils.Utils;

public class NestedTaskManagerActivity extends TaskManagerActivity.TaskManagerBaseActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_enter_ip_host:
            Dialog dlg = createIpEnterDialog();
            dlg.show();
            break;

        case R.id.menu_enable_romote_monitor:
            onEnableMonitorItemClick(item);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_manager_menu, menu);

        return true;
    }

    private static final String LOGO_URL1 = "http://www.sinaimg.cn/ty/sportslive/kits/chelsea_logo1.png";
    private static final String LOGO_URL2 = "http://192.168.5.107/UEFA/chelsea_logo.png";
    private static final String HOME_URL1 = "http://www.sinaimg.cn/ty/livecast/kits/premier2012/chelsea_1.png";
    private static final String HOME_URL2 = "http://192.168.5.107/UEFA/chelsea_1.png";
    private static final String GUEST_URL1 = "http://www.sinaimg.cn/ty/livecast/kits/premier2012/chelsea_2.png";
    private static final String GUEST_URL2 = "http://192.168.5.107/UEFA/chelsea_2.png";

    @Override
    public void performTest() {
        taskManagerNested();
    }

    // ======================================================================

    private void taskManagerNested() {
        final TaskManager taskManager = new MyTaskManager(" NestedTaskManager ========= ");

        taskManager.setStateChangeListener(new IStateChangeListener() {
            @Override
            public void onStateChanged(TaskManager taskManager, State oldState, State newState) {
                Toast.makeText(NestedTaskManagerActivity.this, " onStateChanged state = " + newState,
                        Toast.LENGTH_SHORT).show();
            }
        });

        taskManager.next(new Task(Task.RunningStatus.UI_THREAD, "Show_Progress_Dialog_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(NestedTaskManagerActivity.this);
                progressDialog.setTitle("Check connection");
                progressDialog.setMessage("Connect to server, please wait...");
                // progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();

                operation.setTaskParams(new Object[] { progressDialog });

                return operation;
            }
        }).next(new Task(Task.RunningStatus.WORK_THREAD, "Check_Network_Connection_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                sleep(2500);

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD, "Close_Progress_Dialog_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                ProgressDialog progressDialog = (ProgressDialog) params[0];
                progressDialog.dismiss();

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD, "Show_Custome_Dialog_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Dialog dlg = createSquardDialog();
                dlg.show();

                operation.setTaskParams(new Object[] { dlg });

                return operation;
            }
        })
        // Fetch data from network.
                .next(new Task(Task.RunningStatus.WORK_THREAD, "Fetch_Club_Data_Task") {
                    @Override
                    public TaskOperation onExecute(TaskOperation operation) {
                        TaskManager[] nestedTaskManagers = dipatchTaskManager();

                        for (TaskManager eachTaskManager : nestedTaskManagers) {
                            eachTaskManager.execute(new TaskOperation(operation));
                        }

                        for (TaskManager eachTaskManager : nestedTaskManagers) {
                            eachTaskManager.join();
                        }

                        return operation;
                    }
                }).next(new Task(Task.RunningStatus.UI_THREAD, "Hide_ProgressBar_Task") {
                    @Override
                    public TaskOperation onExecute(TaskOperation operation) {
                        Object[] params = operation.getTaskParams();

                        if (params.length >= 1) {
                            Dialog dlg = (Dialog) params[0];
                            dlg.findViewById(R.id.dlg_progressbar).setVisibility(View.INVISIBLE);
                        }

                        return operation;
                    }
                }).execute();
    }

    // ======================================================================

    protected TaskManager[] dipatchTaskManager() {
        final TaskManager taskManager1 = new MyTaskManager(" Task_Fetch Club_Icon ");
        taskManager1.next(new Task(Task.RunningStatus.WORK_THREAD, "Fetch Club_Icon_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = fetchBitmaps();
                operation.setTaskParams(new TaskOperation(params));

                sleep(1000);

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD, "Update_ImageView_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                if (params.length >= 4) {
                    Dialog dlg = (Dialog) params[0];
                    Bitmap bmp = (Bitmap) params[1];
                    if (null != bmp) {
                        ((ImageView) dlg.findViewById(R.id.club_icon)).setImageBitmap(bmp);
                    }

                    bmp = (Bitmap) params[2];
                    ((ImageView) dlg.findViewById(R.id.club_home)).setImageBitmap(bmp);

                    bmp = (Bitmap) params[3];
                    ((ImageView) dlg.findViewById(R.id.club_guest)).setImageBitmap(bmp);
                }

                operation.setTaskParams(new Object[] { null });

                return operation;
            }
        });

        final TaskManager taskManager2 = new MyTaskManager(" Task_Fetch Club_Name ");
        taskManager2.next(new Task(Task.RunningStatus.WORK_THREAD, "Fetch_Club_Name_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                sleep(1000);

                operation.setTaskParams(new TaskOperation(new Object[] { "Club: Chelsea" }));

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD, "Update_Club_Name_TextView_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                if (params.length >= 2) {
                    Dialog dlg = (Dialog) params[0];
                    String clubName = (String) params[1];
                    ((TextView) dlg.findViewById(R.id.club_name)).setText(clubName);
                }

                return operation;
            }
        });

        final TaskManager taskManager3 = new MyTaskManager(" Task_Fetch National_Name ");
        taskManager3.next(new Task(Task.RunningStatus.WORK_THREAD, "Fetch_National_Name_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                sleep(500);

                operation.setTaskParams(new TaskOperation(new Object[] { "Nation: England" }));

                return operation;
            }
        }).next(new Task(Task.RunningStatus.UI_THREAD, "Update_National_Name_TextView_Task") {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                if (params.length >= 2) {
                    Dialog dlg = (Dialog) params[0];
                    String nationalName = (String) params[1];
                    ((TextView) dlg.findViewById(R.id.national_name)).setText(nationalName);
                }

                return operation;
            }
        });

        return new TaskManager[] { taskManager1, taskManager2, taskManager3 };
    }

    // ======================================================================

    protected Dialog createSquardDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.task_manager_dlg_layout);
        dialog.setTitle("UEFA Club");
        Window win = dialog.getWindow();
        WindowManager.LayoutParams param = win.getAttributes();
        param.width = -2;

        dialog.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    // ==========================================================================

    private Object[] fetchBitmaps() {
        String logoUrl = LOGO_URL1;
        String homeUrl = HOME_URL1;
        String guestUrl = GUEST_URL1;
        Bitmap logo = null;
        Bitmap home = null;
        Bitmap guest = null;

        logo = BitmapUtil.getBitmapFromNet(logoUrl);
        if (null == logo) {
            logoUrl = LOGO_URL2;
            logo = BitmapUtil.getBitmapFromNet(logoUrl);

            if (null == logo) {
                logo = BitmapUtil.drawableToBitmap(getResources().getDrawable(R.drawable.chelsea_logo));
                sleep(500);
            }
        }

        // ==============================================

        home = BitmapUtil.getBitmapFromNet(homeUrl);
        if (null == home) {
            homeUrl = HOME_URL2;
            home = BitmapUtil.getBitmapFromNet(homeUrl);

            if (null == home) {
                home = BitmapUtil.drawableToBitmap(getResources().getDrawable(R.drawable.chelsea_1));
                sleep(500);
            }
        }

        // ==============================================

        guest = BitmapUtil.getBitmapFromNet(guestUrl);
        if (null == guest) {
            guestUrl = GUEST_URL2;
            guest = BitmapUtil.getBitmapFromNet(guestUrl);

            if (null == guest) {
                guest = BitmapUtil.drawableToBitmap(getResources().getDrawable(R.drawable.chelsea_2));
                sleep(500);
            }
        }

        return new Object[] { logo, home, guest };
    }

    // =======================================================

    private Dialog createIpEnterDialog() {
        final Dialog dlg = new Dialog(this);
        dlg.setContentView(R.layout.ip_host_layout);
        dlg.setTitle("Input IP Host");
        WindowManager.LayoutParams params = dlg.getWindow().getAttributes();
        params.width = (int) Utils.pixelToDp(this, 400);
        AutoCompleteTextView textView = (AutoCompleteTextView) dlg.findViewById(R.id.ip_host_edittext);
        textView.setText(TaskManagerActivity.HOST_IP);

        dlg.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClick(dlg);

                dlg.dismiss();
            }
        });

        return dlg;
    }

    private void onDoneButtonClick(Dialog dlg) {
        AutoCompleteTextView textView = (AutoCompleteTextView) dlg.findViewById(R.id.ip_host_edittext);

        String ipHost = textView.getText().toString();
        if (!TextUtils.isEmpty(ipHost)) {
            TaskManagerActivity.HOST_IP = ipHost;
        }
    }

    private void onEnableMonitorItemClick(MenuItem item) {
        TaskManagerActivity.SENDUDP_DEBUG = !TaskManagerActivity.SENDUDP_DEBUG;

        if (TaskManagerActivity.SENDUDP_DEBUG) {
            item.setTitle("Disable Romote Monitor");
        } else {
            item.setTitle("Enable Romote Monitor");
        }
    }
}
