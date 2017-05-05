package com.lee.sdk.test.scrollbar;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.test.widget.TosScrollView;
import com.lee.sdk.utils.Utils;

public class ScrollViewBarActivity extends GABaseActivity
{
    private TosScrollView mScrollView = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.scrollview_layout);

        final int width = (int)Utils.pixelToDp(this, 54);
        
        mScrollView = (TosScrollView)findViewById(R.id.scrollview_container);
        mScrollView.addView(createTextView());
        mScrollView.setScrollBarWidth(width);
        mScrollView.setScrollBarDragEnable(true);
        
        findViewById(R.id.btn_dialog).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Context context = ScrollViewBarActivity.this;
                View view = LayoutInflater.from(context).inflate(R.layout.scrollview_layout, null);
                view.findViewById(R.id.btn_dialog).setVisibility(View.GONE);
                TosScrollView scrollView = (TosScrollView)view.findViewById(R.id.scrollview_container);
                scrollView.addView(createTextView());
                scrollView.setScrollBarWidth(width);
                scrollView.setScrollBarDragEnable(true);
                
                new AlertDialog.Builder(context)
                .setTitle("Dialog")
                .setView(view)
                .show();
            }
        });
    }
    
    private TextView createTextView() {
        TextView textView = new TextView(this);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 200; ++i) {
            sb.append(String.format(" %03d", i)).append("\n");
        }
        
        textView.setText(sb.toString());
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        
        return textView;
    }
}
