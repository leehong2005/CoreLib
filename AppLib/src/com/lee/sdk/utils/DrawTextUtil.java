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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;

/**
 * Draw the text utility.
 * 
 * @author Li Hong
 * @date 2013/01/24
 */
public class DrawTextUtil {
    /**
     * Draw the text. This method will break text automatically and show the ellipsis if possible.
     * 
     * @param canvas
     * @param textPaint
     * @param bounds
     * @param text
     * @param marginLR
     * @param marginTop
     * @param lineSace
     * @param centerH
     * @param centerV
     */
    public static void drawText(Canvas canvas, Paint textPaint, Rect bounds, String text, int marginLR, int marginTop,
            int lineSace, boolean centerH, boolean centerV) {
        if (null == text || 0 == text.length() || null == bounds || bounds.isEmpty()) {
            return;
        }

        float[] measuredWidth = new float[1];
        int start = 0;
        int end = text.length();
        int index = 0;
        int width = bounds.width() - marginLR * 2;
        int height = bounds.height();
        int lines = getTextLines(text, width, textPaint);

        FontMetrics fm = textPaint.getFontMetrics();
        int lineH = (int) (fm.bottom - fm.top);

        int startX = bounds.left;
        int startY = centerV ? (height - lineH * lines) / 2 : 0;
        if (startY < 0) {
            startY = bounds.bottom - height;
        } else {
            startY += bounds.bottom - height;
        }

        if (Math.abs(bounds.top - startY) < marginTop) {
            startY = bounds.top + marginTop;
        }

        int saveCount = canvas.save();
        canvas.clipRect(bounds);

        while (true) {
            // The line height is bigger than the bounds height.
            if (lineH > height) {
                break;
            }

            startY += lineH;
            // Break text to measure.
            index = textPaint.breakText(text, start, end, true, width, measuredWidth);
            // The start X for one line text.
            startX = bounds.left + marginLR;
            // Make the text be center in the bounds.
            if (centerH) {
                startX += (width - (int) measuredWidth[0]) / 2;
            }

            // The next line can not be display fully.
            if (startY + lineH >= bounds.bottom) {
                // The last line.
                if (start + index == end) {
                    canvas.drawText(text, start, start + index, startX, startY, textPaint);
                } else {
                    // Draw text with the ellipsis.
                    String str = text.substring(start, start + index - 1) + "...";
                    canvas.drawText(str, 0, str.length(), startX, startY, textPaint);
                }

                break;
            } else {
                canvas.drawText(text, start, start + index, startX, startY, textPaint);
            }

            start += index;

            // The end of text.
            if (start == end) {
                break;
            }
        }

        canvas.restoreToCount(saveCount);
    }

    /**
     * Draw text.
     * 
     * @param canvas
     * @param textPaint
     * @param bounds
     * @param text
     * @param centerH
     * @param centerV
     */
    public static void drawText(Canvas canvas, Paint textPaint, Rect bounds, String text, boolean centerH,
            boolean centerV) {
        drawText(canvas, textPaint, bounds, text, 0, 0, 0, centerH, centerV);
    }

    /**
     * Get the text lines in specified maximum width.
     * 
     * @param width The width of text bounds.
     * 
     * @return the lines of text which breaks by the maximum bounds.
     */
    public static int getTextLines(String text, int width, Paint textPaint) {
        if (null == text || 0 == text.length()) {
            return 0;
        }

        float[] measuredWidth = new float[1];
        int start = 0;
        int end = text.length();
        int index = 0;
        int lines = 0;

        while (true) {
            index = textPaint.breakText(text, start, end, true, width, measuredWidth);
            lines++;
            start += index;

            // The end of text.
            if (start == end) {
                break;
            }
        }

        return lines;
    }
}
