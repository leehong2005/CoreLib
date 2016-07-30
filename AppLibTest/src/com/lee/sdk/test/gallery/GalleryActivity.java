package com.lee.sdk.test.gallery;

import android.content.Intent;

import com.lee.sdk.test.BaseListActivity;

public class GalleryActivity extends BaseListActivity {
    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.GALLERY");

        return intent;
    }
}
