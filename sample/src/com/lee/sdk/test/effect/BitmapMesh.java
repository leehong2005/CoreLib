/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.lee.sdk.test.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import com.lee.sdk.mesh.InhaleMesh;
import com.lee.sdk.mesh.InhaleMesh.InhaleDir;
import com.lee.sdk.test.R;

public class BitmapMesh {

    public static class SampleView extends View {

        private static final int WIDTH = 40;
        private static final int HEIGHT = 40;

        private final Bitmap mBitmap;
        private final Matrix mMatrix = new Matrix();
        private final Matrix mInverse = new Matrix();
        private boolean mIsDebug = false;

        private Paint mPaint = new Paint();
        private float[] mInhalePt = new float[] { 0, 0 };
        private InhaleMesh mInhaleMesh = null;

        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beach);

            mInhaleMesh = new InhaleMesh(WIDTH, HEIGHT);
            mInhaleMesh.setBitmapSize(mBitmap.getWidth(), mBitmap.getHeight());
            mInhaleMesh.setInhaleDir(InhaleDir.DOWN);
        }

        public void setIsDebug(boolean isDebug) {
            mIsDebug = isDebug;
        }

        public void setInhaleDir(InhaleMesh.InhaleDir dir) {
            mInhaleMesh.setInhaleDir(dir);

            float w = mBitmap.getWidth();
            float h = mBitmap.getHeight();
            float endX = 0;
            float endY = 0;
            float dx = 10;
            float dy = 10;
            mMatrix.reset();

            switch (dir) {
            case DOWN:
                endX = w / 2;
                endY = getHeight() - 20;
                break;

            case UP:
                dy = getHeight() - h - 20;
                endX = w / 2;
                endY = -dy + 10;
                break;

            case LEFT:
                dx = getWidth() - w - 20;
                endX = -dx + 10;
                endY = h / 2;
                break;

            case RIGHT:
                endX = getWidth() - 20;
                endY = h / 2;
                break;
            }

            mMatrix.setTranslate(dx, dy);
            mMatrix.invert(mInverse);
            buildPaths(endX, endY);
            buildMesh(w, h);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            float bmpW = mBitmap.getWidth();
            float bmpH = mBitmap.getHeight();

            mMatrix.setTranslate(10, 10);
            // mMatrix.setTranslate(10, 10);
            mMatrix.invert(mInverse);

            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(2);
            mPaint.setAntiAlias(true);

            buildPaths(bmpW / 2, h - 20);
            buildMesh(bmpW, bmpH);
        }

        public boolean startAnimation(boolean reverse) {
            Animation anim = this.getAnimation();
            if (null != anim && !anim.hasEnded()) {
                return false;
            }

            PathAnimation animation = new PathAnimation(0, HEIGHT + 1, reverse,
                    new PathAnimation.IAnimationUpdateListener() {
                        @Override
                        public void onAnimUpdate(int index) {
                            mInhaleMesh.buildMeshes(index);
                            invalidate();
                        }
                    });

            if (null != animation) {
                animation.setDuration(1000);
                this.startAnimation(animation);
            }

            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Log.i("leehong2", "onDraw  =========== ");
            canvas.drawColor(0xFFCCCCCC);

            canvas.concat(mMatrix);

            canvas.drawBitmapMesh(mBitmap, mInhaleMesh.getWidth(), mInhaleMesh.getHeight(), mInhaleMesh.getVertices(),
                    0, null, 0, mPaint);

            // ===========================================
            // Draw the target point.
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Style.FILL);
            canvas.drawCircle(mInhalePt[0], mInhalePt[1], 5, mPaint);

            if (mIsDebug) {
                // ===========================================
                // Draw the mesh vertices.
                canvas.drawPoints(mInhaleMesh.getVertices(), mPaint);

                // ===========================================
                // Draw the paths
                mPaint.setColor(Color.BLUE);
                mPaint.setStyle(Style.STROKE);
                Path[] paths = mInhaleMesh.getPaths();
                for (Path path : paths) {
                    canvas.drawPath(path, mPaint);
                }
            }
        }

        private void buildMesh(float w, float h) {
            mInhaleMesh.buildMeshes(w, h);
        }

        private void buildPaths(float endX, float endY) {
            mInhalePt[0] = endX;
            mInhalePt[1] = endY;
            mInhaleMesh.buildPaths(endX, endY);
        }

        int mLastWarpX = 0;
        int mLastWarpY = 0;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float[] pt = { event.getX(), event.getY() };
            mInverse.mapPoints(pt);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                int x = (int) pt[0];
                int y = (int) pt[1];
                if (mLastWarpX != x || mLastWarpY != y) {
                    mLastWarpX = x;
                    mLastWarpY = y;
                    buildPaths(pt[0], pt[1]);
                    invalidate();
                }
            }
            return true;
        }
    }

    private static class PathAnimation extends Animation {
        public interface IAnimationUpdateListener {
            public void onAnimUpdate(int index);
        }

        private int mFromIndex = 0;
        private int mEndIndex = 0;
        private boolean mReverse = false;
        private IAnimationUpdateListener mListener = null;

        public PathAnimation(int fromIndex, int endIndex, boolean reverse, IAnimationUpdateListener listener) {
            mFromIndex = fromIndex;
            mEndIndex = endIndex;
            mReverse = reverse;
            mListener = listener;
        }

        public boolean getTransformation(long currentTime, Transformation outTransformation) {

            boolean more = super.getTransformation(currentTime, outTransformation);
            Log.d("leehong2", "getTransformation    more = " + more);
            return more;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int curIndex = 0;
            Interpolator interpolator = this.getInterpolator();
            if (null != interpolator) {
                float value = interpolator.getInterpolation(interpolatedTime);
                interpolatedTime = value;
            }

            if (mReverse) {
                interpolatedTime = 1.0f - interpolatedTime;
            }

            curIndex = (int) (mFromIndex + (mEndIndex - mFromIndex) * interpolatedTime);

            if (null != mListener) {
                Log.i("leehong2", "onAnimUpdate  =========== curIndex = " + curIndex);
                mListener.onAnimUpdate(curIndex);
            }
        }
    }
}
