package com.lee.sdk.test.pullrefresh;

import android.content.Intent;

import com.lee.sdk.test.BaseListActivity;

public class PullRefreshActivity extends BaseListActivity {
    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.PULLREFRESH");

        return intent;
    }
}
