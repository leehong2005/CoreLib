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

import android.graphics.Path;
import android.graphics.PathMeasure;

public class InhaleMesh extends Mesh {
    public enum InhaleDir {
        UP, DOWN, LEFT, RIGHT,
    }

    private Path mFirstPath = new Path();
    private Path mSecondPath = new Path();
    private PathMeasure mFirstPathMeasure = new PathMeasure();
    private PathMeasure mSecondPathMeasure = new PathMeasure();
    private InhaleDir mInhaleDir = InhaleDir.DOWN;

    public InhaleMesh(int width, int height) {
        super(width, height);
    }

    public void setInhaleDir(InhaleDir inhaleDir) {
        mInhaleDir = inhaleDir;
    }

    public InhaleDir getInhaleDir() {
        return mInhaleDir;
    }

    @Override
    public void buildPaths(float endX, float endY) {
        if (mBmpWidth <= 0 || mBmpHeight <= 0) {
            throw new IllegalArgumentException("Bitmap size must be > 0, do you call setBitmapSize(int, int) method?");
        }

        switch (mInhaleDir) {
        case UP:
            buildPathsUp(endX, endY);
            break;

        case DOWN:
            buildPathsDown(endX, endY);
            break;

        case RIGHT:
            buildPathsRight(endX, endY);
            break;

        case LEFT:
            buildPathsLeft(endX, endY);
            break;
        }
    }

    @Override
    public void buildMeshes(int index) {
        if (mBmpWidth <= 0 || mBmpHeight <= 0) {
            throw new IllegalArgumentException("Bitmap size must be > 0, do you call setBitmapSize(int, int) method?");
        }

        switch (mInhaleDir) {
        case UP:
        case DOWN:
            buildMeshByPathOnVertical(index);
            break;

        case RIGHT:
        case LEFT:
            buildMeshByPathOnHorizontal(index);
            break;
        }
    }

    public Path[] getPaths() {
        return new Path[] { mFirstPath, mSecondPath };
    }

    private void buildPathsDown(float endX, float endY) {
        mFirstPathMeasure.setPath(mFirstPath, false);
        mSecondPathMeasure.setPath(mSecondPath, false);

        float w = mBmpWidth;
        float h = mBmpHeight;

        mFirstPath.reset();
        mSecondPath.reset();
        mFirstPath.moveTo(0, 0);
        mSecondPath.moveTo(w, 0);

        mFirstPath.lineTo(0, h);
        mSecondPath.lineTo(w, h);

        mFirstPath.quadTo(0, (endY + h) / 2, endX, endY);
        mSecondPath.quadTo(w, (endY + h) / 2, endX, endY);
    }

    private void buildPathsUp(float endX, float endY) {
        mFirstPathMeasure.setPath(mFirstPath, false);
        mSecondPathMeasure.setPath(mSecondPath, false);

        float w = mBmpWidth;
        float h = mBmpHeight;

        mFirstPath.reset();
        mSecondPath.reset();
        mFirstPath.moveTo(0, h);
        mSecondPath.moveTo(w, h);

        mFirstPath.lineTo(0, 0);
        mSecondPath.lineTo(w, 0);

        mFirstPath.quadTo(0, (endY - h) / 2, endX, endY);
        mSecondPath.quadTo(w, (endY - h) / 2, endX, endY);
    }

    private void buildPathsRight(float endX, float endY) {
        mFirstPathMeasure.setPath(mFirstPath, false);
        mSecondPathMeasure.setPath(mSecondPath, false);

        float w = mBmpWidth;
        float h = mBmpHeight;

        mFirstPath.reset();
        mSecondPath.reset();

        mFirstPath.moveTo(0, 0);
        mSecondPath.moveTo(0, h);

        mFirstPath.lineTo(w, 0);
        mSecondPath.lineTo(w, h);

        mFirstPath.quadTo((endX + w) / 2, 0, endX, endY);
        mSecondPath.quadTo((endX + w) / 2, h, endX, endY);
    }

    private void buildPathsLeft(float endX, float endY) {
        mFirstPathMeasure.setPath(mFirstPath, false);
        mSecondPathMeasure.setPath(mSecondPath, false);

        float w = mBmpWidth;
        float h = mBmpHeight;

        mFirstPath.reset();
        mSecondPath.reset();

        mFirstPath.moveTo(w, 0);
        mSecondPath.moveTo(w, h);

        mFirstPath.lineTo(0, 0);
        mSecondPath.lineTo(0, h);

        mFirstPath.quadTo((endX - w) / 2, 0, endX, endY);
        mSecondPath.quadTo((endX - w) / 2, h, endX, endY);
    }

    private void buildMeshByPathOnVertical(int timeIndex) {
        mFirstPathMeasure.setPath(mFirstPath, false);
        mSecondPathMeasure.setPath(mSecondPath, false);

        int index = 0;
        float[] pos1 = { 0.0f, 0.0f };
        float[] pos2 = { 0.0f, 0.0f };
        float firstLen = mFirstPathMeasure.getLength();
        float secondLen = mSecondPathMeasure.getLength();

        float len1 = firstLen / HEIGHT;
        float len2 = secondLen / HEIGHT;

        float firstPointDist = timeIndex * len1;
        float secondPointDist = timeIndex * len2;
        float height = mBmpHeight;

        mFirstPathMeasure.getPosTan(firstPointDist, pos1, null);
        mFirstPathMeasure.getPosTan(firstPointDist + height, pos2, null);
        float x1 = pos1[0];
        float x2 = pos2[0];
        float y1 = pos1[1];
        float y2 = pos2[1];
        float FIRST_DIST = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        float FIRST_H = FIRST_DIST / HEIGHT;

        mSecondPathMeasure.getPosTan(secondPointDist, pos1, null);
        mSecondPathMeasure.getPosTan(secondPointDist + height, pos2, null);
        x1 = pos1[0];
        x2 = pos2[0];
        y1 = pos1[1];
        y2 = pos2[1];

        float SECOND_DIST = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        float SECOND_H = SECOND_DIST / HEIGHT;

        if (mInhaleDir == InhaleDir.DOWN) {
            for (int y = 0; y <= HEIGHT; ++y) {
                mFirstPathMeasure.getPosTan(y * FIRST_H + firstPointDist, pos1, null);
                mSecondPathMeasure.getPosTan(y * SECOND_H + secondPointDist, pos2, null);

                float w = pos2[0] - pos1[0];
                float fx1 = pos1[0];
                float fx2 = pos2[0];
                float fy1 = pos1[1];
                float fy2 = pos2[1];
                float dy = fy2 - fy1;
                float dx = fx2 - fx1;

                for (int x = 0; x <= WIDTH; ++x) {
                    // y = x * dy / dx
                    float fx = x * w / WIDTH;
                    float fy = fx * dy / dx;

                    mVerts[index * 2 + 0] = fx + fx1;
                    mVerts[index * 2 + 1] = fy + fy1;

                    index += 1;
                }
            }
        } else if (mInhaleDir == InhaleDir.UP) {
            for (int y = HEIGHT; y >= 0; --y) {
                mFirstPathMeasure.getPosTan(y * FIRST_H + firstPointDist, pos1, null);
                mSecondPathMeasure.getPosTan(y * SECOND_H + secondPointDist, pos2, null);

                float w = pos2[0] - pos1[0];
                float fx1 = pos1[0];
                float fx2 = pos2[0];
                float fy1 = pos1[1];
                float fy2 = pos2[1];
                float dy = fy2 - fy1;
                float dx = fx2 - fx1;

                for (int x = 0; x <= WIDTH; ++x) {
                    // y = x * dy / dx
                    float fx = x * w / WIDTH;
                    float fy = fx * dy / dx;

                    mVerts[index * 2 + 0] = fx + fx1;
                    mVerts[index * 2 + 1] = fy + fy1;

                    index += 1;
                }
            }
        }
    }

    private void buildMeshByPathOnHorizontal(int timeIndex) {
        mFirstPathMeasure.setPath(mFirstPath, false);
        mSecondPathMeasure.setPath(mSecondPath, false);

        int index = 0;
        float[] pos1 = { 0.0f, 0.0f };
        float[] pos2 = { 0.0f, 0.0f };
        float firstLen = mFirstPathMeasure.getLength();
        float secondLen = mSecondPathMeasure.getLength();

        float len1 = firstLen / WIDTH;
        float len2 = secondLen / WIDTH;

        float firstPointDist = timeIndex * len1;
        float secondPointDist = timeIndex * len2;
        float width = mBmpWidth;

        mFirstPathMeasure.getPosTan(firstPointDist, pos1, null);
        mFirstPathMeasure.getPosTan(firstPointDist + width, pos2, null);
        float x1 = pos1[0];
        float x2 = pos2[0];
        float y1 = pos1[1];
        float y2 = pos2[1];
        float FIRST_DIST = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        float FIRST_X = FIRST_DIST / WIDTH;

        mSecondPathMeasure.getPosTan(secondPointDist, pos1, null);
        mSecondPathMeasure.getPosTan(secondPointDist + width, pos2, null);
        x1 = pos1[0];
        x2 = pos2[0];
        y1 = pos1[1];
        y2 = pos2[1];

        float SECOND_DIST = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        float SECOND_X = SECOND_DIST / WIDTH;

        if (mInhaleDir == InhaleDir.RIGHT) {
            for (int x = 0; x <= WIDTH; ++x) {
                mFirstPathMeasure.getPosTan(x * FIRST_X + firstPointDist, pos1, null);
                mSecondPathMeasure.getPosTan(x * SECOND_X + secondPointDist, pos2, null);

                float h = pos2[1] - pos1[1];
                float fx1 = pos1[0];
                float fx2 = pos2[0];
                float fy1 = pos1[1];
                float fy2 = pos2[1];
                float dy = fy2 - fy1;
                float dx = fx2 - fx1;

                for (int y = 0; y <= HEIGHT; ++y) {
                    // x = y * dx / dy
                    float fy = y * h / HEIGHT;
                    float fx = fy * dx / dy;

                    index = y * (WIDTH + 1) + x;

                    mVerts[index * 2 + 0] = fx + fx1;
                    mVerts[index * 2 + 1] = fy + fy1;
                }
            }
        } else if (mInhaleDir == InhaleDir.LEFT) {
            for (int x = WIDTH; x >= 0; --x)
            // for (int x = 0; x <= WIDTH; ++x)
            {
                mFirstPathMeasure.getPosTan(x * FIRST_X + firstPointDist, pos1, null);
                mSecondPathMeasure.getPosTan(x * SECOND_X + secondPointDist, pos2, null);

                float h = pos2[1] - pos1[1];
                float fx1 = pos1[0];
                float fx2 = pos2[0];
                float fy1 = pos1[1];
                float fy2 = pos2[1];
                float dy = fy2 - fy1;
                float dx = fx2 - fx1;

                for (int y = 0; y <= HEIGHT; ++y) {
                    // x = y * dx / dy
                    float fy = y * h / HEIGHT;
                    float fx = fy * dx / dy;

                    index = y * (WIDTH + 1) + WIDTH - x;

                    mVerts[index * 2 + 0] = fx + fx1;
                    mVerts[index * 2 + 1] = fy + fy1;
                }
            }
        }
    }
}
