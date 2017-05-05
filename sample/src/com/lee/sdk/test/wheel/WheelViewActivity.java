package com.lee.sdk.test.wheel;

import android.content.Intent;

import com.lee.sdk.test.BaseListActivity;

public class WheelViewActivity extends BaseListActivity {
    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.WHEEL");

        return intent;
    }
}
