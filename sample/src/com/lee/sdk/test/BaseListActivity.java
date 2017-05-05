package com.lee.sdk.test;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lee.sdk.utils.APIUtils;

public abstract class BaseListActivity extends ListActivity {
    public static final String CORELIT_TEST_CAGEGORY = "com.lee.sdk.test.intent.category.ANDROIDDEMO";

    public static final String KEY_LABEL = "lable";
    public static final String KEY_INTENT = "intent";
    public static final String EXTRA_TITLE = "com.lee.demo.extra.TITLE";

    /** Use default pending transition */
    protected boolean mUseDefaultPendingTransition = true;

    /**
     * Use default pending transition
     * 
     * @param useDefaultPendingTransition useDefaultPendingTransition
     */
    public void setUseDefaultPendingTransition(boolean useDefaultPendingTransition) {
        mUseDefaultPendingTransition = useDefaultPendingTransition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleAdapter adapter = new SimpleAdapter(this, getListViewData(this), android.R.layout.simple_list_item_1,
                new String[] { KEY_LABEL }, new int[] { android.R.id.text1 });

        this.setListAdapter(adapter);
        this.getListView().setTextFilterEnabled(true);

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

    private List<Map<String, Object>> getListViewData(Context context) {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        Intent intent = getQueryIntent();
        if (null == intent) {
            return data;
        }

        PackageManager packageMgr = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageMgr.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            CharSequence labelSeq = resolveInfo.loadLabel(packageMgr);
            String label = (null != labelSeq) ? labelSeq.toString() : resolveInfo.activityInfo.name;
            addDataItem(data, label, resolveInfo);
        }

        // Sort the collection.
        Collections.sort(data, new Comparator<Map<String, Object>>() {
            private Collator m_collator = Collator.getInstance();

            @Override
            public int compare(Map<String, Object> object1, Map<String, Object> object2) {
                return m_collator.compare(object1.get(KEY_LABEL), object2.get(KEY_LABEL));
            }
        });

        return data;
    }

    public abstract Intent getQueryIntent();

    protected void addDataItem(List<Map<String, Object>> list, String label, ResolveInfo info) {
        Intent intent = new Intent();
        intent.setClassName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put(KEY_LABEL, label);
        temp.put(KEY_INTENT, intent);

        list.add(temp);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, Object> map = (Map<String, Object>) l.getItemAtPosition(position);
        Intent intent = (Intent) map.get(KEY_INTENT);
        intent.putExtra(EXTRA_TITLE, this.getTitle());
        this.startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        if (!mUseDefaultPendingTransition) {
            this.overridePendingTransition(R.anim.sdk_activity_enter_in, R.anim.sdk_activity_exit_out);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (!mUseDefaultPendingTransition) {
            this.overridePendingTransition(R.anim.sdk_activity_in, R.anim.sdk_activity_out);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (!mUseDefaultPendingTransition) {
            this.overridePendingTransition(R.anim.sdk_activity_in, R.anim.sdk_activity_out);
        }
    }

    /**
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();

        // GoogleAnalyticsBL.getInstance().activityStart(this);
        // String name = GoogleAnalyticsBL.getInstance().getActivityName(this);
        // GoogleAnalyticsBL.getInstance().sendView(name);
    }

    /**
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();

        // GoogleAnalyticsBL.getInstance().activityStop(this);
    }
}
