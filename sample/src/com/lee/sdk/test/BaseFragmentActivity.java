package com.lee.sdk.test;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.lee.sdk.utils.APIUtils;

public class BaseFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APIUtils.hasHoneycomb()) {
            ActionBar actionBar = getActionBar();
            if (null != actionBar) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(this.getTitle());
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        //this.overridePendingTransition(R.anim.sdk_activity_in, R.anim.sdk_activity_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        //this.overridePendingTransition(R.anim.sdk_activity_in, R.anim.sdk_activity_out);
    }

    @Override
    public void finish() {
        super.finish();
        //this.overridePendingTransition(R.anim.sdk_activity_enter_in, R.anim.sdk_activity_exit_out);
    }
}
