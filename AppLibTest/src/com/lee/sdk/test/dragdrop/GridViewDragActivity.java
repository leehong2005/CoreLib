package com.lee.sdk.test.dragdrop;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.dragdrop.DragLayout;
import com.lee.sdk.dragdrop.DragParams;
import com.lee.sdk.dragdrop.IDragSource;
import com.lee.sdk.dragdrop.IDropTarget;
import com.lee.sdk.task.Task;
import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.utils.ImageSearchUtil;
import com.lee.sdk.test.utils.MediaInfo;
import com.lee.sdk.test.widget.ImageViewAdapter;
import com.lee.sdk.test.widget.ImageViewAdapter.GridViewImageView;
import com.lee.sdk.utils.Utils;
import com.lee.sdk.widget.DragGridView;
import com.lee.sdk.widget.DragGridView.IDragAdapter;

public class GridViewDragActivity extends GABaseActivity {
    private static final int COLUMN_NUM = 3;
    private static final int SPACE = 10;
    
    private DragGridView mGridView = null;
    private ImageSearchUtil mSearchUtil = null;
    private ArrayList<MediaInfo> mDatas = null;
    private DragLayout mDragLayout = null;
    ImageLoader mImageLoader = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = ImageLoader.Builder.newInstance(this)
                .setMaxCachePercent(0.3f).build();
//        mImageLoader.setOnProcessBitmapListener(new OnProcessBitmapListener() {
//            @Override
//            public Bitmap onProcessBitmap(Object data) {
//                if (data instanceof MediaInfo) {
//                    return mSearchUtil.getImageThumbnail2((MediaInfo) data);
//                }
//                return null;
//            }
//        });
        
        mSearchUtil = new ImageSearchUtil(this);
        mGridView = new DragGridView(this);
        mGridView.setVerticalSpacing(SPACE);
        mGridView.setHorizontalSpacing(SPACE);
        mGridView.setGravity(Gravity.CENTER);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setNumColumns(COLUMN_NUM);
        mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        int padding = (int) Utils.pixelToDp(this, 5);
        mGridView.setPadding(padding, 0, padding, 0);
        mGridView.setAdapter(new GridViewAdapter(this, mImageLoader));
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo info = mDatas.get(position);
                Toast.makeText(GridViewDragActivity.this,
                        "Item " + position + " clicked!   path = " + info.getFullPath(), Toast.LENGTH_SHORT).show();
            }
        });

        mDragLayout = new DragLayout(this);
        mDragLayout.addView(mGridView);

        mGridView.setDragLayout(mDragLayout);

        setContentView(mDragLayout, new ViewGroup.LayoutParams(-1, -1));
        
        // Start task to load image data.
        loadImageAsync();
    }

    private void loadImageAsync() {
        // Start task to load image data.
        new TaskManager().next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(GridViewDragActivity.this);
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
                ArrayList<MediaInfo> datas = mSearchUtil.getImagesFromSDCard(false);

                if (null != datas) {
                    mDatas = new ArrayList<MediaInfo>();
                    mDatas.addAll(datas);
                }

                return operation;
            }
        }).next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                ProgressDialog mProgressDialog = (ProgressDialog) params[0];
                mProgressDialog.dismiss();
                
                GridViewAdapter adapter = (GridViewAdapter) mGridView.getAdapter();
                adapter.setData(mDatas);
                adapter.notifyDataSetChanged();
                
                if (mDatas.size() == 0) {
                    Toast.makeText(GridViewDragActivity.this, "没有找到图片", Toast.LENGTH_SHORT).show();
                }

                return operation;
            }
        }).execute();
    }

    private static class GridViewAdapter extends ImageViewAdapter implements IDragAdapter {
        private int mHidePosition = -1;
        
        public GridViewAdapter(Context context, ImageLoader imageLoader) {
            super(context, imageLoader);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setVisibility((mHidePosition == position) ? View.INVISIBLE : View.VISIBLE);
            return view;
        }
        
        @Override
        protected View createView(Context context, ViewGroup parent) {
            return new GridViewImageViewEx(context);
        }
        
        @Override
        public void swap(int dragPosition, int curPosition) {
            MediaInfo data = mDatas.get(dragPosition);
            mDatas.remove(dragPosition);
            mDatas.add(curPosition, data);

            update();
        }

        @Override
        public void setHidePositioin(int position) {
            mHidePosition = position;
        }

        @Override
        public void update() {
            notifyDataSetChanged();
        }
    }

    private static class GridViewImageViewEx extends GridViewImageView implements IDragSource {
        public GridViewImageViewEx(Context context) {
            super(context);
        }

        public GridViewImageViewEx(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public GridViewImageViewEx(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public boolean canBeDragged(DragParams params) {
            return true;
        }

        @Override
        public Bitmap getDragBitmap(Point point, DragParams params) {
            return null;
        }

        @Override
        public void onDropCompleted(IDropTarget target, DragParams params, boolean success) {
        }

        @Override
        public boolean startDragBySelf() {
            return true;
        }
    }
}
