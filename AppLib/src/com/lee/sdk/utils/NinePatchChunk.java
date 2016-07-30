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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Rect;


/** ********************************************************************
 *  PNG Extensions
 *
 *  New private chunks that may be placed in PNG images.
 *
 *********************************************************************** */

/**
 * This chunk specifies how to split an image into segments for
 * scaling.
 *
 * There are J horizontal and K vertical segments.  These segments divide
 * the image into J*K regions as follows (where J=4 and K=3):
 *
 *      F0   S0    F1     S1
 *   +-----+----+------+-------+
 * S2|  0  |  1 |  2   |   3   |
 *   +-----+----+------+-------+
 *   |     |    |      |       |
 *   |     |    |      |       |
 * F2|  4  |  5 |  6   |   7   |
 *   |     |    |      |       |
 *   |     |    |      |       |
 *   +-----+----+------+-------+
 * S3|  8  |  9 |  10  |   11  |
 *   +-----+----+------+-------+
 *
 * Each horizontal and vertical segment is considered to by either
 * stretchable (marked by the Sx labels) or fixed (marked by the Fy
 * labels), in the horizontal or vertical axis, respectively. In the
 * above example, the first is horizontal segment (F0) is fixed, the
 * next is stretchable and then they continue to alternate. Note that
 * the segment list for each axis can begin or end with a stretchable
 * or fixed segment.
 *
 * The relative sizes of the stretchy segments indicates the relative
 * amount of stretchiness of the regions bordered by the segments.  For
 * example, regions 3, 7 and 11 above will take up more horizontal space
 * than regions 1, 5 and 9 since the horizontal segment associated with
 * the first set of regions is larger than the other set of regions.  The
 * ratios of the amount of horizontal (or vertical) space taken by any
 * two stretchable slices is exactly the ratio of their corresponding
 * segment lengths.
 *
 * xDivs and yDivs point to arrays of horizontal and vertical pixel
 * indices.  The first pair of Divs (in either array) indicate the
 * starting and ending points of the first stretchable segment in that
 * axis. The next pair specifies the next stretchable segment, etc. So
 * in the above example xDiv[0] and xDiv[1] specify the horizontal
 * coordinates for the regions labeled 1, 5 and 9.  xDiv[2] and
 * xDiv[3] specify the coordinates for regions 3, 7 and 11. Note that
 * the leftmost slices always start at x=0 and the rightmost slices
 * always end at the end of the image. So, for example, the regions 0,
 * 4 and 8 (which are fixed along the X axis) start at x value 0 and
 * go to xDiv[0] and slices 2, 6 and 10 start at xDiv[1] and end at
 * xDiv[2].
 *
 * The array pointed to by the colors field lists contains hints for
 * each of the regions.  They are ordered according left-to-right and
 * top-to-bottom as indicated above. For each segment that is a solid
 * color the array entry will contain that color value; otherwise it
 * will contain NO_COLOR.  Segments that are completely transparent
 * will always have the value TRANSPARENT_COLOR.
 *
 * The PNG chunk type is "npTc".
 * 
 * See "frameworks/base/include/androidfw/ResourceTypes.h" 
 * for the format of struct Res_png_9patch
 * 
 * @author luxiaoyu01@baidu.com
 * @since 2014-5-6
 * @todo
 */
class NinePatchChunk {
    /** mPaddings */
    public Rect  mPaddings = new Rect();

    /** mDivX */
    public int[] mDivX;
    /** mDivY */
    public int[] mDivY;
    /** mColor */
    public int[] mColor;

    /**
     * @param data 解析后的int数组
     * @param buffer 解析前的buffer
     */
    private static void readIntArray(int[] data, ByteBuffer buffer) {
        for (int i = 0, n = data.length; i < n; ++i) {
            data[i] = buffer.getInt();
        }
    }

    /**
     * @param length length
     */
    private static void checkDivCount(int length) {
        if (length == 0 || (length & 0x01) != 0) {
            throw new RuntimeException("invalid nine-patch: " + length);
        }
    }

    /**
     * 将chunk解析成NinePatchChunk对象
     * 
     * @param data chunk data
     * @return NinePatchChunk object
     */
    public static NinePatchChunk deserialize(byte[] data) {
        if (null == data || data.length == 0) {
            return null;
        }
        // png的chunk为BIG_ENDIAN，此处小端序读chunk，因为pb的解析顺序为LITTLE_ENDIAN。
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        //必须先get
        byteBuffer.get();
        // if (wasSerialized == 0) return null;

        NinePatchChunk chunk = new NinePatchChunk();
        chunk.mDivX = new int[byteBuffer.get()];
        chunk.mDivY = new int[byteBuffer.get()];
        chunk.mColor = new int[byteBuffer.get()];

        checkDivCount(chunk.mDivX.length);
        checkDivCount(chunk.mDivY.length);

        // skip 8 bytes
        byteBuffer.getInt();
        byteBuffer.getInt();

        chunk.mPaddings.left = byteBuffer.getInt();
        chunk.mPaddings.right = byteBuffer.getInt();
        chunk.mPaddings.top = byteBuffer.getInt();
        chunk.mPaddings.bottom = byteBuffer.getInt();

        // skip 4 bytes
        byteBuffer.getInt();

        readIntArray(chunk.mDivX, byteBuffer);
        readIntArray(chunk.mDivY, byteBuffer);
        readIntArray(chunk.mColor, byteBuffer);

        return chunk;
    }
}