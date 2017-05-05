package com.lee.sdk.test.anim;

import android.content.Intent;

import com.lee.sdk.test.BaseListActivity;

public class AnimationActivity extends BaseListActivity {
    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.ANIMATION");

        return intent;
    }
}
