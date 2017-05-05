package com.lee.sdk.test.anim;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.lee.sdk.anim.Rotate3dAnimation;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;

public class Rotate3dAnimActivity extends GABaseActivity {
    ImageView mImageView1 = null;
    ImageView mImageView2 = null;
    ImageView mStartAnimView = null;
    View mContainer = null;
    int mDuration = 500;
    float mCenterX = 0.0f;
    float mCenterY = 0.0f;
    float mDepthZ = 0.0f;
    int mIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rotate_anim);

        mImageView1 = (ImageView) findViewById(R.id.imageView1);
        mImageView2 = (ImageView) findViewById(R.id.imageView2);
        mContainer = findViewById(R.id.container);
        mStartAnimView = mImageView1;

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCenterX = mContainer.getWidth() / 2;
                mCenterY = mContainer.getHeight() / 2;

                getDepthZ();

                applyRotation(mStartAnimView, 0, 90);
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void getDepthZ() {
        EditText editText = (EditText) findViewById(R.id.edit_depthz);
        String string = editText.getText().toString();

        try {
            mDepthZ = (float) Integer.parseInt(string);
            // mDepthZ = Math.min(mDepthZ, 300.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyRotation(View animView, float startAngle, float toAngle) {
        float centerX = mCenterX;
        float centerY = mCenterY;
        Rotate3dAnimation rotation = new Rotate3dAnimation(startAngle, toAngle, centerX, centerY, mDepthZ, true);
        rotation.setDuration(mDuration);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView());

        animView.startAnimation(rotation);
    }

    /**
     * This class listens for the end of the first half of the animation. It
     * then posts a new action that effectively swaps the views when the
     * container is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {

            mContainer.post(new SwapViews());
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private final class SwapViews implements Runnable {
        @Override
        public void run() {
            mImageView1.setVisibility(View.GONE);
            mImageView2.setVisibility(View.GONE);

            mIndex++;
            if (0 == mIndex % 2) {
                mStartAnimView = mImageView1;
            } else {
                mStartAnimView = mImageView2;
            }

            mStartAnimView.setVisibility(View.VISIBLE);
            mStartAnimView.requestFocus();

            Rotate3dAnimation rotation = new Rotate3dAnimation(-90, 0, mCenterX, mCenterY, mDepthZ, false);

            rotation.setDuration(mDuration);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());
            mStartAnimView.startAnimation(rotation);
        }
    }
}
