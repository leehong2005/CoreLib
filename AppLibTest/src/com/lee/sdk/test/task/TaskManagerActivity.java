package com.lee.sdk.test.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.task.ThreadWorker;
import com.lee.sdk.test.BaseListActivity;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.utils.Utils;

public class TaskManagerActivity extends BaseListActivity {
    static ThreadWorker mWorker = null;
    static Handler mHandler = null;
    static boolean SENDUDP_DEBUG = false;
    static String HOST_IP = "192.168.5.107";
    static int PORT = 1113;

    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.TASKMANAGER");

        return intent;
    }

    private static class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Object[] objs = (Object[]) msg.obj;
            Task task = (Task) objs[0];
            TaskManager taskManager = (TaskManager) objs[1];

            sendDatagram(task, taskManager);
        }
    }

    public static abstract class TaskManagerBaseActivity extends GABaseActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mWorker = new ThreadWorker("Log_Thread");
            mHandler = new MyHandler(mWorker.getLooper());

            int width = (int) Utils.pixelToDp(this, 200);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, -2);
            params.leftMargin = 10;
            params.topMargin = 10;
            Button btn = new Button(this);
            btn.setText("Run Tasks");
            btn.setLayoutParams(params);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performTest();
                }
            });

            LinearLayout layout = new LinearLayout(this);
            layout.addView(btn);

            setContentView(layout);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            if (null != mWorker) {
                mWorker.quit();
                mWorker = null;
                mHandler = null;
            }
        }

        public abstract void performTest();

        protected void sleep(long time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MyTaskManager extends TaskManager {
        public MyTaskManager(String name) {
            super(name);
        }

        @Override
        protected void printExecuteTaskState(Task task) {
            super.printExecuteTaskState(task);

            Object[] objs = new Object[] { new MyTask(task), this };

            mHandler.obtainMessage(1, objs).sendToTarget();
        }
    }

    private static void sendDatagram(Task task, TaskManager taskManager) {
        if (!SENDUDP_DEBUG) {
            return;
        }

        try {
            byte[] sendContents = String.format("%s##%s##%s##%s", taskManager.getName(), task.getTaskName(),
                    task.getStatus(), task.getRunningStatus().toString()).getBytes("ascii");

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(sendContents, sendContents.length,
                    InetAddress.getByName(HOST_IP), PORT);
            socket.send(packet);
            socket.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MyTask extends Task {
        public MyTask(Task task) {
            super(task);
        }

        @Override
        public TaskOperation onExecute(TaskOperation operation) {
            return null;
        }
    }
}
