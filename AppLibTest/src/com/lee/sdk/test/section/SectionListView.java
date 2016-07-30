package com.lee.sdk.test.section;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.widget.PinnedHeaderListView;
import com.lee.sdk.widget.PinnedHeaderListView.PinnedHeaderAdapter;

public class SectionListView extends GABaseActivity {

    private int mItemHeight = 55;
    private int mSecHeight = 25;
    private PinnedHeaderListView mListView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        float density = getResources().getDisplayMetrics().density;
        mItemHeight = (int) (density * mItemHeight);
        mSecHeight = (int) (density * mSecHeight);
        
        mListView = new PinnedHeaderListView(this);
        mListView.setAdapter(new ListViewAdapter());
        mListView.setPinnedHeaderView(getHeaderView());
        mListView.setBackgroundColor(Color.argb(255, 20, 20, 20));
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewAdapter adapter = ((ListViewAdapter) parent.getAdapter());
                Contact data = (Contact) adapter.getItem(position);
                Toast.makeText(SectionListView.this, data.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        setContentView(mListView);
        
        loadImageAsync();
    }
    
    private void loadImageAsync() {
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        // Start task to load image data.
        new TaskManager().next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(SectionListView.this);
                progressDialog.setTitle("Load image");
                progressDialog.setMessage("Load image from SD Card, please wait...");
                progressDialog.setCancelable(false);

                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }

                operation.setTaskParams(new Object[] { progressDialog });

                return operation;
            }
        }).next(new Task(RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                contacts.addAll(ContactLoader.getInstance().getContacts(SectionListView.this));
                return operation;
            }
        }).next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                ProgressDialog mProgressDialog = (ProgressDialog) params[0];
                mProgressDialog.dismiss();
                
                ListViewAdapter adapter = (ListViewAdapter) mListView.getAdapter();
                adapter.setDatas(contacts);

                return operation;
            }
        }).execute();
    }
    
    private View getHeaderView() {
        TextView itemView = new TextView(SectionListView.this);
        itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mSecHeight));
        itemView.setGravity(Gravity.CENTER_VERTICAL);
        itemView.setBackgroundColor(Color.WHITE);
        itemView.setTextSize(20);
        itemView.setTextColor(Color.GRAY);
        itemView.setBackgroundResource(R.drawable.section_listview_header_bg);
        itemView.setPadding(10, 0, 0, itemView.getPaddingBottom());
        
        return itemView;
    }

    private class ListViewAdapter extends BaseAdapter implements PinnedHeaderAdapter {
        
        private ArrayList<Contact> mDatas = new ArrayList<Contact>();
        private static final int TYPE_CATEGORY_ITEM = 0;  
        private static final int TYPE_ITEM = 1;  
        
        public void setDatas(ArrayList<Contact> datas) {
            if (null != datas) {
                mDatas.clear();
                mDatas.addAll(datas);
                notifyDataSetChanged();
            }
        }
        
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
        
        @Override
        public boolean isEnabled(int position) {
            // 异常情况处理  
            if (null == mDatas || position <  0|| position > getCount()) {
                return true;
            } 
            
            Contact item = mDatas.get(position);
            if (item.isSection) {
                return false;
            }
            
            return true;
        }
        
        @Override
        public int getCount() {
            return mDatas.size();
        }
        
        @Override
        public int getItemViewType(int position) {
            // 异常情况处理  
            if (null == mDatas || position <  0|| position > getCount()) {
                return TYPE_ITEM;
            } 
            
            Contact item = mDatas.get(position);
            if (item.isSection) {
                return TYPE_CATEGORY_ITEM;
            }
            
            return TYPE_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public Object getItem(int position) {
            return (position >= 0 && position < mDatas.size()) ? mDatas.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int itemViewType = getItemViewType(position);
            Contact data = (Contact) getItem(position);
            TextView itemView;
            
            switch (itemViewType) {
            case TYPE_ITEM:
                if (null == convertView) {
                    itemView = new TextView(SectionListView.this);
                    itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            mItemHeight));
                    itemView.setTextSize(16);
                    itemView.setPadding(10, 0, 0, 0);
                    itemView.setGravity(Gravity.CENTER_VERTICAL);
                    //itemView.setBackgroundColor(Color.argb(255, 20, 20, 20));
                    convertView = itemView;
                }
                
                itemView = (TextView) convertView;
                itemView.setText(data.toString());
                break;
                
            case TYPE_CATEGORY_ITEM:
                if (null == convertView) {
                    convertView = getHeaderView();
                }
                itemView = (TextView) convertView;
                itemView.setText(data.toString());
                break;
            }
            
            return convertView;
        }

        @Override
        public int getPinnedHeaderState(int position) {
            if (position < 0 || getCount() == 0) {
                return PINNED_HEADER_GONE;
            }
            
            Contact item = (Contact) getItem(position);
            Contact itemNext = (Contact) getItem(position + 1);
            boolean isSection = (null != item) ? item.isSection : false;
            boolean isNextSection = (null != itemNext) ? itemNext.isSection : false;
            if (!isSection && isNextSection) {
                return PINNED_HEADER_PUSHED_UP;
            }
            
            return PINNED_HEADER_VISIBLE;
        }

        @Override
        public void configurePinnedHeader(View header, int position, int alpha) {
            Contact item = (Contact) getItem(position);
            if (null != item) {
                if (header instanceof TextView) {
                    ((TextView) header).setText(item.sectionStr);
                }
            }
        }
    }
}
