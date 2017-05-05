/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.widget;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.lee.sdk.res.R;
import com.lee.sdk.utils.Utils;
import com.lee.sdk.widget.BdAdapterView.OnItemSelectedListener;

/**
 * This class wraps the wheel view for time picker, the time format default is 24-hour clock.
 *  
 * @author lihong06
 * @since 2014-5-22
 */
public class BdTimePicker extends LinearLayout {
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {
        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(BdTimePicker view, int hourOfDay, int minute);
    }
    
    /**
     * The current hour.
     */
    private int mHour = 0;
    
    /**
     * The current minute.
     */
    private int mMinute = 0;
    
    /**
     * The hour wheel view.
     */
    private WheelView mHourWheelView;
    
    /**
     * The minute wheel view
     */
    private WheelView mMinuteWheelView;
    
    /**
     * Time change listener.
     */
    private OnTimeChangedListener mTimeChangeListener;
    
    /**
     * The item selected listener.
     */
    private OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(BdAdapterView<?> parent, View view, int position, long id) {
            if (parent == mHourWheelView) {
                mHour = position;
            } else if (parent == mMinuteWheelView) {
                mMinute = position;
            }
            
            if (null != mTimeChangeListener) {
                mTimeChangeListener.onTimeChanged(BdTimePicker.this, mHour, mMinute);
            }
        }

        @Override
        public void onNothingSelected(BdAdapterView<?> parent) {
            
        }
    };
    
    /**
     * Constructor method.
     * 
     * @param context context
     */
    public BdTimePicker(Context context) {
        super(context);
        
        init(context);
    }
    
    /**
     * Constructor method.
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public BdTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    /**
     * Constructor method.
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BdTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }

    /**
     * Initialize
     * 
     * @param context context
     */
    private void init(Context context) {
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.sdk_timepicker_layout, this);
        
        mHourWheelView = (WheelView) findViewById(R.id.wheel_hour);
        mHourWheelView.setOnItemSelectedListener(mItemSelectedListener);
        mHourWheelView.setAdapter(new TimePickerAdapter(context));
        
        mMinuteWheelView = (WheelView) findViewById(R.id.wheel_minute);
        mMinuteWheelView.setOnItemSelectedListener(mItemSelectedListener);
        mMinuteWheelView.setAdapter(new TimePickerAdapter(context));
        
        // 初始化数据
        initDatas();
    }
    
    /**
     * Bind data.
     */
    private void initDatas() {
        Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        
        ArrayList<String> hours = new ArrayList<String>(24);
        ArrayList<String> minutes = new ArrayList<String>(60);
        for (int i = 0; i < 24; ++i) {
            hours.add(String.format("%02d", i));
        }

        for (int i = 0; i < 60; ++i) {
            minutes.add(String.format("%02d", i));
        }

        ((TimePickerAdapter) mHourWheelView.getAdapter()).setData(hours);
        ((TimePickerAdapter) mMinuteWheelView.getAdapter()).setData(minutes);

        mHourWheelView.setSelection(mHour);
        mMinuteWheelView.setSelection(mMinute);
    }
    
    /**
     * Set the time changed listener.
     * 
     * @param listener listener.
     */
    public void setOnTimeChangeListener(OnTimeChangedListener listener) {
        mTimeChangeListener = listener;
    }
    
    /**
     * Set the hour.
     * 
     * @param hour hour
     */
    public void setHour(int hour) {
        if (hour < 0 || hour >= 24) {
            throw new IllegalArgumentException("The hour must be between 0 and 23.");
        }
        
        mHour = hour;
        mHourWheelView.setSelection(hour);
    }
    
    /**
     * Get the selected hour.
     * 
     * @return the hour
     */
    public int getHour() {
        return mHour;
    }
    
    /**
     * Set the minute.
     * 
     * @param minute minute.
     */
    public void setMinute(int minute) {
        if (minute < 0 || minute >= 60) {
            throw new IllegalArgumentException("The hour must be between 0 and 59.");
        }
        
        mMinute = minute;
        mMinuteWheelView.setSelection(minute);
    }
    
    /**
     * Get the selected minute.
     * 
     * @return the minute
     */
    public int getMinute() {
        return mMinute;
    }
    
    /**
     * Set the hour adapter.
     * 
     * @param adapter adapter
     */
    public void setHourAdapter(SpinnerAdapter adapter) {
        mHourWheelView.setAdapter(adapter);
    }
    
    /**
     * Set the hour adapter.
     * 
     * @param adapter adapter
     */
    public void setMinuteAdapter(SpinnerAdapter adapter) {
        mMinuteWheelView.setAdapter(adapter);
    }
    
    /**
     * The time picker adapter, in this adapter, the default style is showing text.
     * 
     * @author lihong06
     * @since 2014-5-22
     */
    public static class TimePickerAdapter extends BaseAdapter {
        /**
         * Datas
         */
        private ArrayList<String> mData = null;
        
        /**
         * Width
         */
        private int mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
        
        /**
         * Default height
         */
        private int mHeight = 70;
        
        /**
         * Context
         */
        private Context mContext = null;

        /**
         * Constructor.
         * 
         * @param context context
         */
        public TimePickerAdapter(Context context) {
            mContext = context;
            mHeight = (int) Utils.pixelToDp(context, mHeight);
        }

        /**
         * Set data.
         * 
         * @param data
         */
        public void setData(ArrayList<String> data) {
            mData = data;
            notifyDataSetChanged();
        }

        /**
         * Set the item size.
         * 
         * @param width width of item.
         * @param height height of item.
         */
        public void setItemSize(int width, int height) {
            mWidth  = width;
            mHeight = height;
        }

        @Override
        public int getCount() {
            return (null != mData) ? mData.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return (null != mData) ? mData.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                // Create view
                convertView = createView(mContext, position, parent);
            }
            
            // Build the views
            buildView(position, convertView);

            return convertView;
        }
        
        /**
         * Create the new instance of the view.
         * 
         * @param context context
         * @param position position
         * @param parent parent
         * @return view
         */
        protected View createView(Context context, int position, ViewGroup parent) {
            View convertView = new TextView(context);
            convertView.setLayoutParams(new BdGallery.LayoutParams(mWidth, mHeight));
            TextView textView = (TextView) convertView;
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            textView.setTextColor(Color.BLACK);
            
            return convertView;
        }
        
        /**
         * Build the view.
         * 
         * @param position position
         * @param convertView convertView
         */
        protected void buildView(int position, View convertView) {
            TextView textView = (TextView) convertView;
            String text = mData.get(position);
            textView.setText(text);
        }
    }
}
