package com.lee.sdk.test.group;

import java.util.ArrayList;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.group.ItemView.LineItem;
import com.lee.sdk.utils.Utils;

public class GroupGridViewActivity extends GABaseActivity
{
    private int COLUMN_NUM = 3;
    private ArrayList<LineItem> m_lineItems = new ArrayList<LineItem>();
    private TextView mTextView = null;
    
    /**
     * @see com.nj1s.lib.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        initialize();
        
        FrameLayout layout = new FrameLayout(this);
        
        mTextView = new TextView(this);
        mTextView.setVisibility(View.INVISIBLE);
        
        int width = (int)Utils.pixelToDp(this, 200);
        int height = (int)Utils.pixelToDp(this, 200);
        
        mTextView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        
        ListView listView = new GroupListView(this);
        listView.setDivider(null);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setAdapter(new ListViewAdapter());
        
        layout.addView(mTextView);
        layout.addView(listView);
        
        mTextView.setEllipsize(TruncateAt.END);
        mTextView.setMaxLines(2);
        mTextView.setTextSize(18);
        mTextView.setPadding(5, 5, 5, 5);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setTextColor(Color.WHITE);
        
        setContentView(layout);
    }
    
    public TextView getTextView()
    {
        return mTextView;
    }
    
    private void initialize()
    {
        boolean isLandscape = (Configuration.ORIENTATION_LANDSCAPE == this.getResources().getConfiguration().orientation);
        COLUMN_NUM = isLandscape ? 5 : 3;
        
        ArrayList<String> datas = new ArrayList<String>();
        
        // Group A
        for (int i = 1; i < 18; ++i)
        {
            datas.add(String.format("Group A Item %d", i));
        }
        m_lineItems.addAll(createLineItems(datas, "Group A"));
        
        // Group B
        datas = new ArrayList<String>();
        for (int i = 1; i <= 27; ++i)
        {
            datas.add(String.format("Group B Item %d", i));
        }
        m_lineItems.addAll(createLineItems(datas, "Group B"));
        
        // Group C
        datas = new ArrayList<String>();
        for (int i = 1; i < 12; ++i)
        {
            datas.add(String.format("Group C Item %d", i));
        }
        m_lineItems.addAll(createLineItems(datas, "Group C"));
        
        // Group E
        datas = new ArrayList<String>();
        m_lineItems.addAll(createLineItems(datas, "Group E"));
        
        // Group D
        datas = new ArrayList<String>();
        for (int i = 1; i < 5; ++i)
        {
            datas.add(String.format("Group D Item Very long long long long long text, %d", i));
        }
        m_lineItems.addAll(createLineItems(datas, "Group D"));
    }
    
    private ArrayList<LineItem> createLineItems(ArrayList<String> datas, String groupTitle)
    {
        ArrayList<LineItem> lineItems = new ArrayList<LineItem>();
        
        int size = datas.size();
        if (size > 0)
        {
            int line = 1;
            if (size >= COLUMN_NUM)
            {
                line = (int)(size / COLUMN_NUM);
                if (size % COLUMN_NUM > 0)
                {
                    line += 1;
                }
            }
            
            for (int j = 0; j < line; ++j)
            {
                int start = j * COLUMN_NUM;
                int end = start + COLUMN_NUM;
                end = Math.min(end, size);
                
                LineItem lineItem = new LineItem(datas, start, end);
                lineItem.setGroupTitle(groupTitle);
                lineItems.add(lineItem);
            }
        }
        
        if (lineItems.isEmpty())
        {
            LineItem lineItem = new LineItem();
            lineItem.setGroupTitle(groupTitle);
            lineItems.add(lineItem);
        }
        
        return lineItems;
    }
    
    private class ListViewAdapter extends BaseAdapter
    {
        private int mHeight = 250;
        
        public ListViewAdapter() {
            mHeight = (int)Utils.pixelToDp(GroupGridViewActivity.this, mHeight);
        }
        
        @Override
        public int getCount()
        {
            return m_lineItems.size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LineItem lineItem = m_lineItems.get(position);
            
            if (null == convertView)
            {
                int padding = (int)Utils.pixelToDp(GroupGridViewActivity.this, 150);
                convertView = new ItemView(GroupGridViewActivity.this);
                convertView.setPadding(padding, 0, 0, 0);
                convertView.setLayoutParams(new AbsListView.LayoutParams(-1, mHeight));
            }
            
            ItemView itemView = (ItemView)convertView;
            itemView.setData(lineItem);
            
            return convertView;
        }
    }
}
