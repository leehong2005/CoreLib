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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import com.lee.sdk.mesh.CycleMesh;
import com.lee.sdk.mesh.Mesh;
import com.lee.sdk.test.R;

public class BitmapMesh2 {
    
    public static class SampleView extends View {
        
        private static final int WIDTH  = 50;
        private static final int HEIGHT = 50;

        private final Bitmap mBitmap;

        private final Matrix mMatrix = new Matrix();
        private final Matrix mInverse = new Matrix();
        private Paint mPaint = new Paint();
        private Mesh mMesh = null;
        
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mBitmap = BitmapFactory.decodeResource(getResources(),
                                                     R.drawable.beach);

            float w = mBitmap.getWidth();
            float h = mBitmap.getHeight();
            
            mMatrix.setTranslate(10, 10);
            mMatrix.invert(mInverse);
            
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(2);
            mPaint.setStyle(Style.STROKE);
            mPaint.setAntiAlias(true);
            
            mMesh = new CycleMesh(WIDTH, HEIGHT);
            mMesh.setBitmapSize((int)w, (int)h);
            mMesh.buildMeshes(w, h);
        }
        
        public boolean startAnimation(boolean reverse)
        {
            Animation anim = this.getAnimation();
            if (null != anim && !anim.hasEnded())
            {
                return false;
            }
            
            PathAnimation animation = new PathAnimation(0, CycleMesh.ANIM_STEPS, reverse, 
                    new PathAnimation.IAnimationUpdateListener()
            {
                @Override
                public void onAnimUpdate(int index)
                {
                    mMesh.buildMeshes(index);
                    invalidate();
                }
            });
            
            if (null != animation)
            {
                animation.setDuration(1000);
                this.startAnimation(animation);
            }
            
            return true;
        }
        
        @Override 
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

            canvas.concat(mMatrix);
            
            // =======================
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            canvas.translate(500, 150);
            
            canvas.drawBitmapMesh(mBitmap,
                    mMesh.getWidth(),
                    mMesh.getHeight(),
                    mMesh.getVertices(), 0,
                    null, 0, mPaint);
        }
        
        int mLastWarpX = 0;
        int mLastWarpY = 0;
        
        @Override 
        public boolean onTouchEvent(MotionEvent event) {
            float[] pt = { event.getX(), event.getY() };
            mInverse.mapPoints(pt);

            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                int x = (int)pt[0];
                int y = (int)pt[1];
                if (mLastWarpX != x || mLastWarpY != y) {
                    mLastWarpX = x;
                    mLastWarpY = y;
                    startAnimation(false);
                }
            }
            return true;
        }
    }
    
    public static class PathAnimation extends Animation
    {
        public interface IAnimationUpdateListener
        {
            public void onAnimUpdate(int index);
        }
        
        private int mFromIndex = 0;
        private int mEndIndex = 0;
        private boolean mReverse = false;
        private IAnimationUpdateListener mListener = null;
        
        public PathAnimation(int fromIndex, int endIndex, boolean reverse, IAnimationUpdateListener listener)
        {
            mFromIndex = fromIndex;
            mEndIndex = endIndex;
            mReverse = reverse;
            mListener = listener;
        }
        
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) 
        {
            int curIndex = 0;
            Interpolator interpolator = this.getInterpolator();
            if (null != interpolator)
            {
                float value = interpolator.getInterpolation(interpolatedTime);
                interpolatedTime = value;
            }
            
            if (mReverse)
            {
                interpolatedTime = 1.0f - interpolatedTime;
            }
            
            curIndex = (int)(mFromIndex + (mEndIndex - mFromIndex) * interpolatedTime);
            
            if (null != mListener)
            {
                mListener.onAnimUpdate(curIndex);
            }
        }
    }
}
