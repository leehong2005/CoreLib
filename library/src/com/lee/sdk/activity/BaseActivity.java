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

package com.lee.sdk.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.lee.sdk.R;
import com.lee.sdk.app.BaseApplication;

/**
 * The base activity, this activity is used to record the top activity.
 * 
 * @author Li Hong
 * @since 2011/12/14
 */
public class BaseActivity extends Activity {
    /** 
     * Use default pending transition
     */
    protected boolean mUseDefaultPendingTransition = true;

    /**
     * Use default pending transition
     * 
     * @param useDefaultPendingTransition useDefaultPendingTransition
     */
    public void setUseDefaultPendingTransition(boolean useDefaultPendingTransition) {
        mUseDefaultPendingTransition = useDefaultPendingTransition;
    }

    /**
     * Called when activity is created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. This is a good place to begin animations,
     * open exclusive-access devices (such as the camera), etc.
     * 
     * <p>
     * Keep in mind that onResume is not the best indicator that your activity is visible to the
     * user; a system window such as the keyguard may be in front. Use {@link #onWindowFocusChanged}
     * to know for certain that your activity is visible to the user (for example, to resume a
     * game).
     * 
     * <p>
     * <em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em>
     * </p>
     * 
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    protected void onResume() {
        Application app = this.getApplication();

        if (app instanceof BaseApplication) {
            ((BaseApplication) app).setTopActivity(this);
        }

        super.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to {@link #onResume}.
     */
    @Override
    protected void onPause() {
        Application app = this.getApplication();

        if (app instanceof BaseApplication) {
            Activity topActivity = ((BaseApplication) app).getTopActivity();
            if (null != topActivity && topActivity == this) {
                ((BaseApplication) app).setTopActivity(null);
            }
        }

        super.onPause();
    }

    /**
     * Launch a new activity. You will not receive any information about when the activity exits.
     * This implementation overrides the base version, providing information about the activity
     * performing the launch. Because of this additional information, the
     * {@link Intent#FLAG_ACTIVITY_NEW_TASK} launch flag is not required; if not specified, the new
     * activity will be added to the task of the caller.
     * 
     * <p>
     * This method throws {@link android.content.ActivityNotFoundException} if there was no Activity
     * found to run the given Intent.
     * 
     * @param intent The intent to start.
     * 
     * @throws android.content.ActivityNotFoundException
     * 
     * @see #startActivityForResult
     */
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);

        if (!mUseDefaultPendingTransition) {
            this.overridePendingTransition(R.anim.sdk_activity_in, R.anim.sdk_activity_out);
        }
    }

    /**
     * @see #startActivityForResult
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        if (!mUseDefaultPendingTransition) {
            this.overridePendingTransition(R.anim.sdk_activity_in, R.anim.sdk_activity_out);
        }
    }

    /**
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        super.finish();

        if (!mUseDefaultPendingTransition) {
            this.overridePendingTransition(R.anim.sdk_activity_enter_in, R.anim.sdk_activity_exit_out);
        }
    }
}
