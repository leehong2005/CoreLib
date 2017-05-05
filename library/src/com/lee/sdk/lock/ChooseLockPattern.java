/*
 * Copyright (C) 2007 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lee.sdk.lock.LockPatternView.Cell;
import com.lee.sdk.lock.LockPatternView.DisplayMode;
import com.lee.sdk.res.R;

/**
 * If the user has a lock pattern set already, makes them confirm the existing one.
 *
 * Then, prompts the user to choose a lock pattern:
 * - prompts for initial pattern
 * - asks for confirmation / restart
 * - saves chosen password when confirmed
 */
public class ChooseLockPattern extends FrameLayout {
    /**
     * Save lock pattern listener
     */
    public interface OnSaveLockPatternListener {
        /**
         * Save the chosen pattern and then close the view if succeed.
         * 
         * @param pattern the chosen pattern
         */
        public void onSaveChosenPatternAndClose(List<LockPatternView.Cell> pattern);
    }
    
    /** how long we wait to clear a wrong pattern */
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 700;
    /** Lock pattern view */
    private LockPatternView mLockPatternView;
    /** Header text */
    private TextView mHeaderText;
    /** UI stage */
    private Stage mUiStage = Stage.Introduction;
    /** The choosen pattern */
    private List<LockPatternView.Cell> mChosenPattern = null;
    /** Utils */
    private LockPatternUtils mLockPatternUtils = null;
    /** Listener */
    private OnSaveLockPatternListener mSaveLockPatternListener = null;;
    /** The patten used during the help screen to show how to draw a pattern. */
    private final List<LockPatternView.Cell> mAnimatePattern =
            Collections.unmodifiableList(LockPatternUtils.newArrayList(
                    LockPatternView.Cell.of(0, 0),
                    LockPatternView.Cell.of(0, 1),
                    LockPatternView.Cell.of(1, 1),
                    LockPatternView.Cell.of(2, 1)
            ));
    /** Runnable */
    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    
    /** Listener */
    private LockPatternView.OnPatternListener mChooseNewLockPatternListener = 
            new LockPatternView.OnPatternListener() {
        
        @Override
        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        @Override
        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        @Override
        public void onPatternDetected(List<Cell> pattern) {
            if (mUiStage == Stage.NeedToConfirm || mUiStage == Stage.ConfirmWrong) {
                if (mChosenPattern == null) {
                    throw new IllegalStateException("null chosen pattern in stage 'need to confirm");
                }
                if (mChosenPattern.equals(pattern)) {
                    updateStage(Stage.ChoiceConfirmed);
                } else {
                    updateStage(Stage.ConfirmWrong);
                }
            } else if (mUiStage == Stage.Introduction || mUiStage == Stage.ChoiceTooShort){
                if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE) {
                    updateStage(Stage.ChoiceTooShort);
                } else {
                    mChosenPattern = new ArrayList<LockPatternView.Cell>(pattern);
                    updateStage(Stage.FirstChoiceValid);
                }
            } else {
                throw new IllegalStateException("Unexpected stage " + mUiStage + " when "
                        + "entering the pattern.");
            }
        }

        @Override
        public void onPatternCellAdded(List<Cell> pattern) {
            
        }
    };
    
    /**
     * Constructor
     * 
     * @param context context
     */
    public ChooseLockPattern(Context context) {
        super(context);
        
        init(context);
    }
    
    /**
     * Constructor
     * 
     * @param context context
     * @param attrs attrs
     */
    public ChooseLockPattern(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /**
     * Constructor
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public ChooseLockPattern(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }
    
    private void init(Context context) {
        mLockPatternUtils = new LockPatternUtils(context);
        
        View view = LayoutInflater.from(context).inflate(R.layout.sdk_choose_lock_pattern, this, true);
        
        mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mHeaderText = (TextView) view.findViewById(R.id.headerText);
//        // make it so unhandled touch events within the unlock screen go to the
//        // lock pattern view.
//        final LinearLayoutWithDefaultTouchRecepient topLayout
//                = (LinearLayoutWithDefaultTouchRecepient) view.findViewById(
//                R.id.topLayout);
//        topLayout.setDefaultTouchRecepient(mLockPatternView);
    }
    
    public void onCreate() {
        updateStage(Stage.Introduction);
    }
    
    public void setOnSaveLockPatternListener(OnSaveLockPatternListener listener) {
        mSaveLockPatternListener = listener;
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mUiStage == Stage.HelpScreen) {
                updateStage(Stage.Introduction);
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MENU && mUiStage == Stage.Introduction) {
            updateStage(Stage.HelpScreen);
            return true;
        }
        return false;
    }
    
    // clear the wrong pattern unless they have started a new one
    // already
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
    }
    
    private void patternInProgress() {
        mHeaderText.setText(R.string.lockpattern_recording_inprogress);
    }
    
    /**
     * Updates the messages and buttons appropriate to what stage the user
     * is at in choosing a view.  This doesn't handle clearing out the pattern;
     * the pattern is expected to be in the right state.
     * @param stage
     */
    private void updateStage(Stage stage) {
        final Stage previousStage = mUiStage;

        mUiStage = stage;

        // header text, footer text, visibility and
        // enabled state all known from the stage
        if (stage == Stage.ChoiceTooShort) {
            mHeaderText.setText(
                    getResources().getString(
                            stage.headerMessage,
                            LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
        } else {
            mHeaderText.setText(stage.headerMessage);
        }

        // same for whether the patten is enabled
        if (stage.patternEnabled) {
            mLockPatternView.enableInput();
        } else {
            mLockPatternView.disableInput();
        }

        // the rest of the stuff varies enough that it is easier just to handle
        // on a case by case basis.
        mLockPatternView.setDisplayMode(DisplayMode.Correct);

        switch (mUiStage) {
            case Introduction:
                mLockPatternView.clearPattern();
                break;
            case HelpScreen:
                mLockPatternView.setPattern(DisplayMode.Animate, mAnimatePattern);
                break;
            case ChoiceTooShort:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case FirstChoiceValid:
                // Continue to input
                onContinuePattern(stage);
                break;
            case NeedToConfirm:
                mLockPatternView.clearPattern();
                break;
            case ConfirmWrong:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case ChoiceConfirmed:
                // Save pattern here
                onConfirmPattern(stage);
                break;
        }

        // If the stage changed, announce the header for accessibility. This
        // is a no-op when accessibility is disabled.
        if (previousStage != stage) {
            mHeaderText.announceForAccessibility(mHeaderText.getText());
        }
    }
    
    private void onContinuePattern(Stage stage) {
        if (stage == Stage.FirstChoiceValid) {
            mLockPatternView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLockPatternView.clearPattern();
                    updateStage(Stage.NeedToConfirm);
                }
            }, 100);
        }
    }
    
    private void onConfirmPattern(Stage stage) {
        if (stage == Stage.ChoiceConfirmed) {
            saveChosenPatternAndClose();
        }
    }
    
    private void saveChosenPatternAndClose() {
        if (null != mSaveLockPatternListener) {
            mSaveLockPatternListener.onSaveChosenPatternAndClose(mChosenPattern);
        }
    }
}
