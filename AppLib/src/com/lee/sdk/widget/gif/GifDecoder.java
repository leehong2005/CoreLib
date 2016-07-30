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

//CHECKSTYLE:OFF
package com.lee.sdk.widget.gif;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.lee.sdk.NoProGuard;

/**
 * This class provides some native methods to decode the gif.
 * 
 * @author lihong06
 * @since 2014-10-11
 */
/*public*/ class GifDecoder implements NoProGuard {
    public static native void renderFrame(int[] pixels, int gifFileInPtr, int[] metaData);

    public static native int openFd(int[] metaData, FileDescriptor fd, long offset) throws GifIOException;

    public static native int openByteArray(int[] metaData, byte[] bytes) throws GifIOException;

    public static native int openDirectByteBuffer(int[] metaData, ByteBuffer buffer) throws GifIOException;

    public static native int openStream(int[] metaData, InputStream stream) throws GifIOException;

    public static native int openFile(int[] metaData, String filePath) throws GifIOException;

    public static native void free(int gifFileInPtr);

    public static native void reset(int gifFileInPtr);

    public static native void setSpeedFactor(int gifFileInPtr, float factor);

    public static native String getComment(int gifFileInPtr);

    public static native int getLoopCount(int gifFileInPtr);

    public static native int getDuration(int gifFileInPtr);

    public static native int getCurrentPosition(int gifFileInPtr);

    public static native int seekToTime(int gifFileInPtr, int pos, int[] pixels);

    public static native int seekToFrame(int gifFileInPtr, int frameNr, int[] pixels);

    public static native int saveRemainder(int gifFileInPtr);

    public static native int restoreRemainder(int gifFileInPtr);

    public static native long getAllocationByteCount(int gifFileInPtr);
}
//CHECKSTYLE:ON
