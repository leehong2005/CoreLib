package com.lee.sdk.test.gallery;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.utils.Utils;
import com.lee.sdk.widget.BdAdapterView;
import com.lee.sdk.widget.BdAdapterView.OnItemClickListener;
import com.lee.sdk.widget.BdGallery;

public class GalleryCycleActivity extends GABaseActivity {
    BdGallery mGallery = null;
    ArrayList<Integer> mGalleryData = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_cycle);

        initGalleryData();
        initialize();

        ToggleButton toggleBtn = (ToggleButton) findViewById(R.id.toggleButton1);
        ToggleButton toggleBtn2 = (ToggleButton) findViewById(R.id.toggleButton2);

        toggleBtn.setChecked(mGallery.isScrollCycle());
        toggleBtn2.setChecked(mGallery.isSlotInCenter());

        toggleBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGallery.setScrollCycle(isChecked);
            }
        });

        toggleBtn2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGallery.setSlotInCenter(isChecked);
            }
        });

        mGallery.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BdAdapterView<?> parent, View view, int position, long id) {
                int data = mGalleryData.get(position);

                Toast.makeText(GalleryCycleActivity.this, "Item click, text = " + data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initialize() {
        mGallery = (BdGallery) findViewById(R.id.gallery1);

        mGallery.setScrollBarBottomMargin(0);
        mGallery.setSpacing(5);
        mGallery.setScrollBarSize(7);
        mGallery.setUnselectedAlpha(1.0f);
        mGallery.setSlotInCenter(true);
        mGallery.setScrollCycle(false);
        mGallery.setAdapter(new GalleryAdapter(mGalleryData));
        mGallery.bringToFront();
    }

    private class GalleryAdapter extends BaseAdapter {
        ArrayList<Integer> datas = null;
        int mSize = 110;

        public GalleryAdapter(ArrayList<Integer> datas) {
            this.datas = datas;
            mSize = (int) getResources().getDimension(R.dimen.gallery_item_size);
        }

        @Override
        public int getCount() {
            return (null != datas) ? datas.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = new MyImageView(GalleryCycleActivity.this);
                convertView.setLayoutParams(new BdGallery.LayoutParams(mSize, mSize));
            }

            MyImageView imgView = (MyImageView) convertView;
            imgView.setString(String.valueOf(datas.get(position)));
            imgView.setBackgroundResource(R.drawable.icon_result);
            imgView.setPadding(0, 0, 0, 0);

            return convertView;
        }
    }

    private void initGalleryData() {
        for (int i = 0; i < 100; ++i) {
            mGalleryData.add(i + 1);
        }
    }

    // The custom image view
    public class MyImageView extends ImageView {
        public MyImageView(Context context) {
            this(context, null);
        }

        public MyImageView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MyImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            mPaint.setTextSize((int) Utils.pixelToDp(GalleryCycleActivity.this, 25));
            mPaint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            onDrawText(canvas);
        }

        String mText = null;
        Paint mPaint = new Paint();

        public void setString(String text) {
            mText = text;
        }

        protected void onDrawText(Canvas canvas) {
            if (null == mText) {
                return;
            }

            float length = mPaint.measureText(mText);
            float x = ((float) getWidth() - length) / 2;
            float y = getHeight() / 2;

            canvas.drawText(mText, x, y, mPaint);
        }
    }
}
