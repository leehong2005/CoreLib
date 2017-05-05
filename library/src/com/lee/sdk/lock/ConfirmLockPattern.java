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

package com.lee.sdk.lock;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lee.sdk.lock.LockPatternView.Cell;
import com.lee.sdk.res.R;
import com.lee.sdk.widget.LinearLayoutWithDefaultTouchRecepient;

/**
 * 
 * @author lihong06
 * @since 2014-10-27
 */
public class ConfirmLockPattern extends FrameLayout  {
    public interface OnConfirmLockPatternListener {
        public void onConfirmLockPatternAndClose(boolean confirm);
    }
    
    // how long we wait to clear a wrong pattern
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 700;

    private final static String LOCKOUT_ATTEMPT_DEADLINE = "lockscreen.lockoutattemptdeadline";
    
    private LockPatternView mLockPatternView;
    private LockPatternUtils mLockPatternUtils;
    private int mNumWrongConfirmAttempts;
    private CountDownTimer mCountdownTimer;

    private TextView mHeaderTextView;

    // caller-supplied text for various prompts
    private CharSequence mHeaderText;
    private CharSequence mHeaderWrongText;
    
    private OnConfirmLockPatternListener mConfirmLockPatternListener;

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    
    /**
     * The pattern listener that responds according to a user confirming
     * an existing lock pattern.
     */
    private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener
            = new LockPatternView.OnPatternListener()  {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternCellAdded(List<Cell> pattern) {

        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (mLockPatternUtils.checkPattern(pattern)) {
                onConfirmLockPatternAndClose();
            } else {
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL &&
                        ++mNumWrongConfirmAttempts
                        >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    long deadline = setLockoutAttemptDeadline();
                    handleAttemptLockout(deadline);
                } else {
                    updateStage(Stage.NeedToUnlockWrong);
                    postClearPatternRunnable();
                }
            }
        }
    };
    
    /**
     * Constructor method
     * 
     * @param context context
     */
    public ConfirmLockPattern(Context context) {
        super(context);
        
        init(context);
    }
    
    /**
     * Constructor method
     * 
     * @param context context
     * @param attrs attrs
     */
    public ConfirmLockPattern(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /**
     * Constructor method
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public ConfirmLockPattern(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }
    
    public void setOnConfirmLockPatternListener(OnConfirmLockPatternListener listener) {
        mConfirmLockPatternListener = listener;
    }

    private void init(Context context) {
        mLockPatternUtils = new LockPatternUtils(context);
        View view = LayoutInflater.from(context).inflate(R.layout.sdk_choose_lock_pattern, this);
        mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
        mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);

        // make it so unhandled touch events within the unlock screen go to the
        // lock pattern view.
        final LinearLayoutWithDefaultTouchRecepient topLayout
                = (LinearLayoutWithDefaultTouchRecepient) view.findViewById(R.id.topLayout);
        topLayout.setDefaultTouchRecepient(mLockPatternView);


        mLockPatternView.setTactileFeedbackEnabled(true);
        mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
        updateStage(Stage.NeedToUnlock);

        // on first launch, if no lock pattern is set, then finish with
        // success (don't want user to get stuck confirming something that
        // doesn't exist).
        if (!mLockPatternUtils.savedPatternExists()) {
            
        }
    }

    public void onPause() {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
    }

    public void onResume() {
        // if the user is currently locked out, enforce it.
        long deadline = getLockoutAttemptDeadline();
        if (deadline != 0) {
            handleAttemptLockout(deadline);
        } else if (!mLockPatternView.isEnabled()) {
            // The deadline has passed, but the timer was cancelled...
            // Need to clean up.
            mNumWrongConfirmAttempts = 0;
            updateStage(Stage.NeedToUnlock);
        }
    }

    private void updateStage(Stage stage) {
        switch (stage) {
            case NeedToUnlock:
                if (mHeaderText != null) {
                    mHeaderTextView.setText(mHeaderText);
                } else {
                    mHeaderTextView.setText(R.string.lockpattern_need_to_unlock);
                }

                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case NeedToUnlockWrong:
                if (mHeaderWrongText != null) {
                    mHeaderTextView.setText(mHeaderWrongText);
                } else {
                    String text = getContext().getString(
                            R.string.lockpattern_need_to_unlock_wrong,
                            (LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mNumWrongConfirmAttempts));
                    mHeaderTextView.setText(text);
                }

                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case LockedOut:
                mLockPatternView.clearPattern();
                // enabled = false means: disable input, and have the
                // appearance of being disabled.
                mLockPatternView.setEnabled(false); // appearance of being disabled
                break;
        }
    }

    // clear the wrong pattern unless they have started a new one
    // already
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
    }

    private void handleAttemptLockout(long elapsedRealtimeDeadline) {
        updateStage(Stage.LockedOut);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        mCountdownTimer = new CountDownTimer(
                elapsedRealtimeDeadline - elapsedRealtime,
                LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {

            @Override
            public void onTick(long millisUntilFinished) {
                final int secondsCountdown = (int) (millisUntilFinished / 1000);
                String text = getContext().getString(
                        R.string.lockpattern_too_many_failed_confirmation_attempts,
                        secondsCountdown);
                mHeaderTextView.setText(text);
            }

            @Override
            public void onFinish() {
                mNumWrongConfirmAttempts = 0;
                updateStage(Stage.NeedToUnlock);
            }
        }.start();
    }
    
    private long setLockoutAttemptDeadline() {
        final long deadline = SystemClock.elapsedRealtime() + LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS;
        mLockPatternUtils.setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline);
        return deadline;
    }
    
    private long getLockoutAttemptDeadline() {
        final long deadline = mLockPatternUtils.getLong(LOCKOUT_ATTEMPT_DEADLINE);
        final long now = SystemClock.elapsedRealtime();
        if (deadline < now || deadline > (now + LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS)) {
            return 0L;
        }
        return deadline;
    }
    
    private void onConfirmLockPatternAndClose() {
        if (null != mConfirmLockPatternListener) {
            mConfirmLockPatternListener.onConfirmLockPatternAndClose(true);
        }
    }
}
