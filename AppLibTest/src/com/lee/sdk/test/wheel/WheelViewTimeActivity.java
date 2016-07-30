package com.lee.sdk.test.wheel;

import java.util.Calendar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.widget.BdTimePicker;
import com.lee.sdk.widget.BdTimePicker.OnTimeChangedListener;
import com.lee.sdk.widget.WheelView;

public class WheelViewTimeActivity extends GABaseActivity {
    TextView mSelDateTxt = null;
    WheelView mMinuteWheel = null;
    WheelView mHourWheel = null;
    int mCurHour = 0;
    int mCurMinute = 0;
    BdTimePicker mTimePicker;

    private String formatDate() {
        int hour = mTimePicker.getHour();
        int min = mTimePicker.getMinute();
        return String.format("Time: %02d:%02d", hour, min);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wheel_time);
        
        mTimePicker = (BdTimePicker) findViewById(R.id.time_picker);
        mTimePicker.setOnTimeChangeListener(new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(BdTimePicker view, int hourOfDay, int minute) {
                mSelDateTxt.setText(formatDate());
            }
        });
        
        mSelDateTxt = (TextView) findViewById(R.id.sel_time);
        mSelDateTxt.setText(formatDate());

        findViewById(R.id.btn_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                mTimePicker.setHour(hour);
                mTimePicker.setMinute(minute);
                mSelDateTxt.setText(formatDate());
            }
        });
    }
}
