package com.lee.sdk.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.lee.sdk.utils.Utils;

public class CaptureActivity extends GABaseActivity {
    private MyImageView mImageView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout layout = new FrameLayout(this);

        Button btn = new Button(this);
        int width = (int) Utils.pixelToDp(this, 150);
        btn.setLayoutParams(new FrameLayout.LayoutParams(width, -2));
        btn.setText("Clip Bitmap");

        Button btn2 = new Button(this);
        btn2.setLayoutParams(new FrameLayout.LayoutParams(width, -2));
        btn2.setText("Reset");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCaptureBitmap();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReset();
            }
        });

        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.addView(btn);
        btnLayout.addView(btn2);
        btnLayout.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));

        mImageView = new MyImageView(this);
        mImageView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mImageView.setImageResource(R.drawable.android);
        mImageView.setScaleType(ScaleType.CENTER_INSIDE);

        layout.addView(mImageView);
        layout.addView(btnLayout);

        setContentView(layout);
    }

    private void doCaptureBitmap() {
        mImageView.clipByPath();
    }

    private void doReset() {
        mImageView.setDrawPath(true);
        mImageView.setImageResource(R.drawable.android);
    }

    protected class MyImageView extends ImageView {
        private Path mPath = new Path();
        private Paint mPaint = new Paint();
        private boolean mInit = false;
        private boolean mDrawPath = true;
        private float mX = 0;
        private float mY = 0;
        private GestureDetector mDetector = new GestureDetector(CaptureActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                        movePath(-distanceX, -distanceY);

                        return false;
                    }

                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });

        public MyImageView(Context context) {
            super(context);

            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(5);
            mPaint.setStyle(Style.STROKE);
            mPaint.setAntiAlias(true);
            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (null != mDetector) {
                return mDetector.onTouchEvent(event);
            }

            return super.onTouchEvent(event);
        }

        public void movePath(float dx, float dy) {
            mX += dx;
            mY += dy;
            mPath.reset();
            mPath.addCircle(mX, mY, getWidth() / 3, Direction.CCW);

            invalidate();
        }

        public Path getPath() {
            return mPath;
        }

        public void setDrawPath(boolean drawPath) {
            mDrawPath = drawPath;
        }

        public void draw(Canvas canvas) {

            if (!mInit) {
                mInit = true;
                float x = getLeft() / 2 + getWidth() / 2;
                float y = getTop() / 2 + getHeight() / 2;
                mX = x;
                mY = y;
                mPath.addCircle(x, y, getWidth() / 3, Direction.CCW);
            }

            // canvas.clipPath(mPath, Region.Op.REPLACE);

            super.draw(canvas);

            if (mDrawPath) {
                canvas.drawPath(mPath, mPaint);
            }
        }

        public Bitmap clipByPath() {
            Path path = mImageView.getPath();
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            int width = (int) bounds.width();
            int height = (int) bounds.height();

            try {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.translate(-bounds.left, -bounds.top);
                canvas.clipPath(path, Region.Op.REPLACE);
                this.setDrawPath(false);
                this.draw(canvas);
                this.setImageBitmap(bitmap);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public void saveClipBitmap() {

        }
    }
}
