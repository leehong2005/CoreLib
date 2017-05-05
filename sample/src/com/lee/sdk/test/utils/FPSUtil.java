/*
 * Project: MediaGuideJP
 * 
 * Copyright (C) 2011, TOSHIBA Corporation.
 */

package com.lee.sdk.test.utils;

import android.util.Log;

/**
 * This class is for calculating the FPT. The {@link #trackFPS(Object)} method should be called in
 * View#onDraw() method.
 * 
 * @author Li Hong
 * 
 * @date 2013/01/14
 */
public class FPSUtil {
    private static long mFpsStartTime = -1;
    private static long mFpsPrevTime = -1;
    private static int mFpsNumFrames;
    private static String TAG = "leehong2";

    public static void trackFPS(Object obj) {
        // Tracks frames per second drawn. First value in a series of draws may be bogus
        // because it down not account for the intervening idle time
        long nowTime = System.currentTimeMillis();
        if (mFpsStartTime < 0) {
            mFpsStartTime = mFpsPrevTime = nowTime;
            mFpsNumFrames = 0;
        } else {
            ++mFpsNumFrames;
            String thisHash = Integer.toHexString(System.identityHashCode(obj));
            long frameTime = nowTime - mFpsPrevTime;
            long totalTime = nowTime - mFpsStartTime;
            Log.d(TAG, "0x" + thisHash + "\tFrame time:\t" + frameTime);
            mFpsPrevTime = nowTime;
            if (totalTime > 1000) {
                float fps = (float) mFpsNumFrames * 1000 / totalTime;
                Log.d(TAG, "0x" + thisHash + "\tFPS:\t" + fps);
                mFpsStartTime = nowTime;
                mFpsNumFrames = 0;
            }
        }
    }
}