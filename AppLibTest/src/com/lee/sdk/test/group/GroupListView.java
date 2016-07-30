package com.lee.sdk.test.group;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

import com.lee.sdk.test.utils.FPSUtil;

public class GroupListView extends ListView
{
    @Override
    protected void onDraw(Canvas canvas)
    {
        FPSUtil.trackFPS(this);
        
        super.onDraw(canvas);
    }

    public GroupListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public GroupListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public GroupListView(Context context)
    {
        super(context);
    }
    
//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//
//        int expandSpec = MeasureSpec.makeMeasureSpec(
//                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
//        super.onMeasure(widthMeasureSpec, expandSpec);
//    } 
}
