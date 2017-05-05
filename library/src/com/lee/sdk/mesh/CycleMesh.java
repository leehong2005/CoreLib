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

package com.lee.sdk.mesh;

public class CycleMesh extends Mesh {
    public static final int ANIM_STEPS = 10;

    public CycleMesh(int width, int height) {
        super(width, height);
    }

    @Override
    public void buildPaths(float endX, float endY) {
        // Do nothing.
    }

    @Override
    public void buildMeshes(int index) {
        if (mBmpWidth <= 0 || mBmpHeight <= 0) {
            throw new IllegalArgumentException("Bitmap size must be > 0, do you call setBitmapSize(int, int) method?");
        }

        int nIndex = 0;

        float W = mBmpWidth / 2;
        float H = mBmpHeight / 2;
        float a = (float) (W * Math.sqrt((H * H + W * W)) / H);
        float b = (float) (H * Math.sqrt((H * H + W * W)) / W);

        a -= index * (a / (ANIM_STEPS * 2));
        b -= index * (b / (ANIM_STEPS * 2));

        float totalH = Math.min(b, H);
        float h = 2 * totalH / HEIGHT;
        float offsetX = W;
        float offsetY = H;

        for (int y = 0; y <= HEIGHT; ++y) {
            float fy = y * h - totalH;
            float dx = (a / b) * (float) Math.sqrt(Math.abs(b * b - fy * fy));
            float fx2 = dx;
            float fx1 = -dx;

            fx2 = Math.min(fx2, W);
            fx1 = Math.max(fx1, -W);

            fy = Math.min(fy, H);
            fy = Math.max(fy, -H);

            float w = (fx2 - fx1) / WIDTH;

            for (int x = 0; x <= WIDTH; ++x) {
                float fx = x * w + fx1;

                setXY(mVerts, nIndex, fx + offsetX, fy + offsetY);

                nIndex += 1;
            }
        }
    }
}
