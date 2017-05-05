package com.lee.sdk.test;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

import com.lee.sdk.activity.BaseActivity;
import com.lee.sdk.utils.APIUtils;

public class GABaseActivity extends BaseActivity {
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

    /**
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //GoogleAnalyticsBL.getInstance().activityStart(this);
        //String name = GoogleAnalyticsBL.getInstance().getActivityName(this);
        //GoogleAnalyticsBL.getInstance().sendView(name);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //GoogleAnalyticsBL.getInstance().activityStop(this);
    }
}
