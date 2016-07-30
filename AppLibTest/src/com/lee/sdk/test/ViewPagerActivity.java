package com.lee.sdk.test;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lee.sdk.widget.viewpager.BdPagerTab;
import com.lee.sdk.widget.viewpager.BdPagerTabHost;
import com.lee.sdk.widget.viewpager.PagerAdapterImpl;

/**
 * 
 * @author lihong06
 * @since 2014-2-21
 */
public class ViewPagerActivity extends BaseFragmentActivity {
ArrayList<String> mDatas = new ArrayList<String>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_layout);
        
        initDatas();
        initActionBar();
        initTabHost();
    }
    
    private void initDatas() {
        mDatas.add("百度");
        mDatas.add("网易");
        mDatas.add("SINA");
        mDatas.add("腾讯");
        mDatas.add("阿里巴巴");
    }

    private void initActionBar() {
    }
    
    private void initTabHost() {
        // 得到BdPagerTabHost对象，可以new创建，也可从XML中加载
        final BdPagerTabHost tabHostView = (BdPagerTabHost) findViewById(R.id.tabhost);
        // 添加tab
        for (String str : mDatas) {
            tabHostView.addTab(new BdPagerTab().setTitle(str));
        }
        
        tabHostView.selectTab(0);   // 默认第一个tab选中
        //tabHostView.setTabTextSize(32);  // 设置tab字体大小，这块可以不用设置，到时候统一调成默认的
        //tabHostView.setTabBarBackground(R.drawable.picture_action_bar_bg);  // 设置tab bar的背景色，通常不需要
        //tabHostView.setPageIndicatorDrawable(R.drawable.picture_tab_indicator); // 设置tab的indicator，通常需要设置，默认是红色的。
        tabHostView.layoutTabs();   // 布局tab
              
//        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
//            public int getCount() {
//                return tabHostView.getTabCount();
//            }
//                  
//            public Fragment getItem(int position) {
//                // 返回相应的fragment
//                ContentFragement fragment = new ContentFragement();
//                String text = mDatas.get(position);
//                fragment.setText(text);
//                
//                return fragment;
//            }
//        };
        
        PagerAdapterImpl adapter = new PagerAdapterImpl() {
            @Override
            public int getCount() {
                return tabHostView.getTabCount();
            }
            
            @Override
            protected View onInstantiateItem(ViewGroup container, int position) {
                TextView textView = new TextView(ViewPagerActivity.this);
                textView.setTextSize(30);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }

            @Override
            protected void onConfigItem(View convertView, int position) {
                if (convertView instanceof TextView) {
                    String text = mDatas.get(position);
                    TextView textView = (TextView) convertView;
                    textView.setText(text);
                }
            }
        };
        
        // 设置adapter，默认选中第1个tab。
        tabHostView.setPagerAdapter(adapter, 0);
    }
    
//    private static class ContentFragement extends Fragment {
//        private TextView mTextView = null;
//        private String mText = null;
//        
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            if (null == mTextView) {
//                mTextView = new TextView(getActivity());
//                mTextView.setTextSize(30);
//                mTextView.setGravity(Gravity.CENTER);
//            }
//            
//            View view = getView();
//            if (view == null) {
//                view = mTextView;
//                ViewGroup parent = (ViewGroup) view.getParent();
//                if (parent != null) {
//                    parent.removeView(view);
//                }
//            }
//            
//            setText(mText);
//            
//            return mTextView;
//        }
//        
//        public void setText(String text) {
//            mText = text;
//            if (null != mTextView) {
//                mTextView.setText(text);
//            }
//        }
//    }
}
