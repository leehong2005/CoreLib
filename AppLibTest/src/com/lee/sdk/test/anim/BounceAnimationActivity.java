package com.lee.sdk.test.anim;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;

import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.utils.APIUtils;
import com.lee.sdk.utils.Utils;

@TargetApi(11)
public class BounceAnimationActivity extends GABaseActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bounce_anim_layout);
        
        // Current API is not over API Level 11.
        if (!APIUtils.hasHoneycomb()) {
            return;
        }

        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);

        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animView.startAnimation();
            }
        });
    }

    @TargetApi(11)
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener,
            Animator.AnimatorListener {

        public static final int RED = 0xffFF8080;
        public static final int BLUE = 0xff8080FF;
        public static final int CYAN = 0xff80ffff;
        public static final int GREEN = 0xff80ff80;
        private float BALL_SIZE = 100f;

        public final ArrayList<ShapeHolder> balls = new ArrayList<ShapeHolder>();
        AnimatorSet animation = null;
        Animator bounceAnim = null;
        ShapeHolder ball = null;

        public MyAnimationView(Context context) {
            super(context);
            int width = (int) Utils.pixelToDp(context, 200);
            BALL_SIZE = (int) Utils.pixelToDp(context, BALL_SIZE);
            ball = addBall(width, 0);
        }

        private Animator createAnimation() {
            if (bounceAnim == null) {

                ObjectAnimator bounceAnim1 = ObjectAnimator.ofFloat(ball, "y", ball.getY(), getHeight() - BALL_SIZE);

                ObjectAnimator bounceAnim2 = ObjectAnimator.ofFloat(ball, "x", ball.getX(), getWidth() - BALL_SIZE);

                bounceAnim1.setDuration(1500);
                bounceAnim1.setInterpolator(new BounceInterpolator());
                bounceAnim1.addUpdateListener(this);

                bounceAnim2.setDuration(1500);
                bounceAnim2.setInterpolator(new BounceInterpolator());
                bounceAnim2.addUpdateListener(this);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(bounceAnim1, bounceAnim2);

                bounceAnim = animatorSet;
            }

            return bounceAnim;
        }

        public void startAnimation() {
            Animator animator = createAnimation();
            if (null != animator) {
                animator.start();
            }
        }

        public void seek(long seekTime) {
            createAnimation();
            // bounceAnim.setCurrentPlayTime(seekTime);
        }

        private ShapeHolder addBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(BALL_SIZE, BALL_SIZE);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x);
            shapeHolder.setY(y);
            int red = (int) (100 + Math.random() * 155);
            int green = (int) (100 + Math.random() * 155);
            int blue = (int) (100 + Math.random() * 155);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint();
            int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f, 50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            balls.add(shapeHolder);
            return shapeHolder;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.translate(ball.getX(), ball.getY());
            ball.getShape().draw(canvas);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
            // long playtime = bounceAnim.getCurrentPlayTime();
            // mSeekBar.setProgress((int)playtime);
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            balls.remove(((ObjectAnimator) animation).getTarget());

        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationStart(Animator animation) {
        }
    }

    public class ShapeHolder {
        private float x = 0, y = 0;
        private ShapeDrawable shape;
        private int color;
        private RadialGradient gradient;
        @SuppressWarnings("unused")
        private float alpha = 1f;
        private Paint paint;

        public void setPaint(Paint value) {
            paint = value;
        }

        public Paint getPaint() {
            return paint;
        }

        public void setX(float value) {
            x = value;
        }

        public float getX() {
            return x;
        }

        public void setY(float value) {
            y = value;
        }

        public float getY() {
            return y;
        }

        public void setShape(ShapeDrawable value) {
            shape = value;
        }

        public ShapeDrawable getShape() {
            return shape;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int value) {
            shape.getPaint().setColor(value);
            color = value;
        }

        public void setGradient(RadialGradient value) {
            gradient = value;
        }

        public RadialGradient getGradient() {
            return gradient;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
            shape.setAlpha((int) ((alpha * 255f) + .5f));
        }

        public float getWidth() {
            return shape.getShape().getWidth();
        }

        public void setWidth(float width) {
            Shape s = shape.getShape();
            s.resize(width, s.getHeight());
        }

        public float getHeight() {
            return shape.getShape().getHeight();
        }

        public void setHeight(float height) {
            Shape s = shape.getShape();
            s.resize(s.getWidth(), height);
        }

        public ShapeHolder(ShapeDrawable s) {
            shape = s;
        }
    }
}
