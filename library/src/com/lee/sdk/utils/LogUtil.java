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

package com.lee.sdk.utils;

import android.util.Log;

/**
 * The Log Utility, we can control the output of log.
 * 
 * @author Li Hong
 * 
 * @date 2011/12/13
 */
public class LogUtil {
    /**
     * This flag to indicate the log is enabled or disabled.
     */
    private static boolean s_isLogEnable = true;

    /**
     * Disable the log output.
     */
    public static void disableLog() {
        s_isLogEnable = false;
    }

    /**
     * Enable the log output.
     */
    public static void enableLog() {
        s_isLogEnable = true;
    }

    /**
     * Send a {@link #DEBUG} log message.
     * 
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (s_isLogEnable) {
            // Call the method of Log class directory.
            StackTraceElement stackTrace = java.lang.Thread.currentThread().getStackTrace()[3];
            String fileInfo = stackTrace.getFileName() + "(" + stackTrace.getLineNumber() + ") "
                    + stackTrace.getMethodName();
            Log.d(tag, fileInfo + ": " + msg);
        }
    }

    /**
     * Send an {@link #INFO} log message.
     * 
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if (s_isLogEnable) {
            StackTraceElement stackTrace = java.lang.Thread.currentThread().getStackTrace()[3];
            String fileInfo = stackTrace.getFileName() + "(" + stackTrace.getLineNumber() + ") "
                    + stackTrace.getMethodName();
            Log.i(tag, fileInfo + ": " + msg);
        }
    }

    /**
     * Send an {@link #ERROR} log message.
     * 
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (s_isLogEnable) {
            StackTraceElement stackTrace = java.lang.Thread.currentThread().getStackTrace()[3];
            String fileInfo = stackTrace.getFileName() + "(" + stackTrace.getLineNumber() + ") "
                    + stackTrace.getMethodName();
            Log.e(tag, fileInfo + ": " + msg);
        }
    }

    /**
     * Send a {@link #WARN} log message.
     * 
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if (s_isLogEnable) {
            StackTraceElement stackTrace = java.lang.Thread.currentThread().getStackTrace()[3];
            String fileInfo = stackTrace.getFileName() + "(" + stackTrace.getLineNumber() + ") "
                    + stackTrace.getMethodName();
            Log.w(tag, fileInfo + ": " + msg);
        }
    }

    /**
     * Send a {@link #VERBOSE} log message.
     * 
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void v(String tag, String msg) {
        if (s_isLogEnable) {
            StackTraceElement stackTrace = java.lang.Thread.currentThread().getStackTrace()[3];
            String fileInfo = stackTrace.getFileName() + "(" + stackTrace.getLineNumber() + ") "
                    + stackTrace.getMethodName();
            Log.v(tag, fileInfo + ": " + msg);
        }
    }

    /**
     * Get the line number in current file.
     * 
     * @return the line number.
     */
    public static String getFileLineMethod() {
        StackTraceElement strFileInfo = java.lang.Thread.currentThread().getStackTrace()[3];
        String fileLineMethod = strFileInfo.getFileName() + "(" + strFileInfo.getLineNumber() + ") "
                + strFileInfo.getMethodName();

        return fileLineMethod;
    }
}
