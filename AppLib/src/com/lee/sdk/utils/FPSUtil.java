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
 * This class is for calculating the FPT. The {@link #trackFPS(Object)} method should be called in
 * View#onDraw() method.
 * 
 * @author Li Hong
 * 
 * @date 2013/01/14
 */
public class FPSUtil {
    private static boolean mTrackFps = true;
    private static long mFpsStartTime = -1;
    private static long mFpsPrevTime = -1;
    private static int mFpsNumFrames;
    private static String TAG = "FPSUtil";

    public static void trackFPS(Object obj) {
        if (!mTrackFps) {
            return;
        }
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