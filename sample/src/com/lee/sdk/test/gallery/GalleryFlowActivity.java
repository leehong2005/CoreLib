package com.lee.sdk.test.gallery;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.lee.sdk.task.Task;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.utils.BitmapUtil;
import com.lee.sdk.widget.BdGallery;
import com.lee.sdk.widget.GalleryFlow;

@SuppressWarnings("deprecation")
public class GalleryFlowActivity extends GABaseActivity {
    GalleryFlow mGallery = null;
    ArrayList<BitmapDrawable> mBitmaps = new ArrayList<BitmapDrawable>();

    View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.space_confirm_btn:
                onSpaceBtnClick(v);
                break;

            case R.id.max_zoom_confirm_btn:
                onMaxZoomBtnClick(v);
                break;
                
            case R.id.max_rotate_angle_confirm_btn:
                onMaxAngleBtnClick(v);
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_flow_main);

        mGallery = (GalleryFlow) findViewById(R.id.gallery_flow);
        mGallery.setBackgroundColor(Color.GRAY);
        mGallery.setSpacing(-50);
        mGallery.setFadingEdgeLength(0);
        mGallery.setGravity(Gravity.CENTER_VERTICAL);
        mGallery.setAdapter(new GalleryAdapter());

        findViewById(R.id.space_confirm_btn).setOnClickListener(mListener);
        findViewById(R.id.max_zoom_confirm_btn).setOnClickListener(mListener);
        findViewById(R.id.max_rotate_angle_confirm_btn).setOnClickListener(mListener);

        // Create a task to execute the tasks.
        new TaskManager()
        // Show progress dialog
        .next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(GalleryFlowActivity.this);
                progressDialog.setTitle("Load image");
                progressDialog.setMessage("Load image from resource, please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                operation.setTaskParams(new Object[] { progressDialog });

                return operation;
            }
        })
        // Create the reflected images.
        .next(new Task(Task.RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                generateBitmaps();

                return operation;
            }
        })
        // Update the adapter.
        .next(new Task(Task.RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                ProgressDialog progressDialog = (ProgressDialog) params[0];
                progressDialog.dismiss();

                mGallery.setAdapter(new GalleryAdapter());

                return operation;
            }
        })
        // Begin to execute the tasks.
        .execute();
    }

    private void onSpaceBtnClick(View v) {
        EditText editText = (EditText) findViewById(R.id.space_edittext);
        String text = editText.getText().toString();

        try {
            int spacing = Integer.parseInt(text);
            if (spacing >= -60 && spacing <= 60) {
                mGallery.setSpacing(spacing);
                ((GalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
            } else {
                Toast.makeText(this, getResources().getString(R.string.gallery_space_text_hint), Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMaxZoomBtnClick(View v) {
        EditText editText = (EditText) findViewById(R.id.max_zoom_edittext);
        String text = editText.getText().toString();

        try {
            int maxZoom = Integer.parseInt(text);
            if (maxZoom >= -120 && maxZoom <= 120) {
                mGallery.setMaxZoom(maxZoom);
                ((GalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
            } else {
                Toast.makeText(this, getResources().getString(R.string.gallery_max_zoom_text_hint), Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMaxAngleBtnClick(View v) {
        EditText editText = (EditText) findViewById(R.id.max_rotate_angle_edittext);
        String text = editText.getText().toString();

        try {
            int maxRotationAngle = Integer.parseInt(text);
            if (maxRotationAngle >= -60 && maxRotationAngle <= 60) {
                mGallery.setMaxRotationAngle(maxRotationAngle);
                ((GalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
            } else {
                Toast.makeText(this, getResources().getString(R.string.gallery_max_rotate_angle_text_hint),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateBitmaps() {
        int[] ids = { R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g,
                R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k, R.drawable.l, R.drawable.m, R.drawable.n,
                R.drawable.o, R.drawable.p, R.drawable.q, R.drawable.r, R.drawable.s, R.drawable.t, R.drawable.u,
                R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y, R.drawable.z, R.drawable.aa, };

        for (int id : ids) {
            Bitmap bitmap = createReflectedBitmapById(id);
            if (null != bitmap) {
                BitmapDrawable drawable = new BitmapDrawable(bitmap);
                drawable.setAntiAlias(true);
                mBitmaps.add(drawable);
            }
        }
    }

    private Bitmap createReflectedBitmapById(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap reflectedBitmap = BitmapUtil.createReflectedBitmap(bitmap);

            return reflectedBitmap;
        }

        return null;
    }

    private class GalleryAdapter extends BaseAdapter {
        private int mWidth = 110;
        private int mHeight = 184;

        public GalleryAdapter() {
//            mWidth = (int) Utils.pixelToDp(GalleryFlowActivity.this, mWidth);
//            mHeight = (int) Utils.pixelToDp(GalleryFlowActivity.this, mHeight);
            
            mWidth = (int) getResources().getDimension(R.dimen.gallery_flow_item_width);
            mHeight = (int) getResources().getDimension(R.dimen.gallery_flow_item_height);
        }

        @Override
        public int getCount() {
            return mBitmaps.size();
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
                convertView = new MyImageView(GalleryFlowActivity.this);
                convertView.setLayoutParams(new BdGallery.LayoutParams(mWidth, mHeight));
            }

            ImageView imageView = (ImageView) convertView;
            imageView.setImageDrawable(mBitmaps.get(position));
            imageView.setScaleType(ScaleType.FIT_XY);

            return imageView;
        }
    }

    private class MyImageView extends ImageView {
        public MyImageView(Context context) {
            this(context, null);
        }

        public MyImageView(Context context, AttributeSet attrs) {
            super(context, attrs, 0);
        }

        public MyImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
    }
}
