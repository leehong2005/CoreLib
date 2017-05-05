package com.lee.sdk.test;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.task.Task;
import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.utils.ImageSearchUtil;
import com.lee.sdk.test.utils.MediaInfo;
import com.lee.sdk.test.widget.ImageViewAdapter;
import com.lee.sdk.utils.Utils;

public class CacheTestActivity extends GABaseActivity {
    GridView mGridView = null;
    ImageLoader mImageLoader = null;
    ImageSearchUtil mSearchUtil = null;
    ArrayList<MediaInfo> mDatas = null;
    private int mWidth = 100;
    
    public static class FPSGridView extends GridView {
        public FPSGridView(Context context) {
            super(context);
        }

        public FPSGridView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public FPSGridView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        private long mFpsStartTime = -1;
        private long mFpsPrevTime = -1;
        private int mFpsNumFrames;
        String TAG = "guok";

        private void trackFPS() {
            // Tracks frames per second drawn. First value in a series of draws may be bogus
            // because it down not account for the intervening idle time
            long nowTime = System.currentTimeMillis();
            if (mFpsStartTime < 0) {
                mFpsStartTime = mFpsPrevTime = nowTime;
                mFpsNumFrames = 0;
            } else {
                ++mFpsNumFrames;
                String thisHash = Integer.toHexString(System.identityHashCode(this));
                long frameTime = nowTime - mFpsPrevTime;
                long totalTime = nowTime - mFpsStartTime;
                Log.v(TAG, "0x" + thisHash + "\tFrame time:\t" + frameTime);
                mFpsPrevTime = nowTime;
                if (totalTime > 1000) {
                    float fps = (float) mFpsNumFrames * 1000 / totalTime;
                    Log.v(TAG, "0x" + thisHash + "\tFPS:\t" + fps);
                    mFpsStartTime = nowTime;
                    mFpsNumFrames = 0;
                }
            }
        }

        protected void onDraw(Canvas canvas) {
            trackFPS();
            super.onDraw(canvas);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cache_test);
        
        mWidth = (int) Utils.pixelToDp(CacheTestActivity.this, mWidth);
        mGridView = (GridView) findViewById(R.id.gridview1);
        mGridView.setVerticalSpacing(ImageViewAdapter.SPACE);
        mGridView.setHorizontalSpacing(ImageViewAdapter.SPACE);
        mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mGridView.setPadding(5, 0, 5, 0);
        mGridView.setColumnWidth(mWidth);
        
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
        mGridView.setAdapter(new GridViewAdapter(this, mImageLoader));
        mGridView.setOnScrollListener(new ScrollListenerImpl());
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo info = mDatas.get(position);
                Toast.makeText(CacheTestActivity.this, "Item " + position + " clicked!   path = " + info.getFullPath(),
                        Toast.LENGTH_SHORT).show();
                
                ImageBrowserActivity.launchImageBrowser(CacheTestActivity.this, converData(mDatas), position);
            }
        });

        // Start task to load image data.
        loadImageAsync();
    }

    private void loadImageAsync() {
        // Start task to load image data.
        new TaskManager().next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(CacheTestActivity.this);
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
                    Toast.makeText(CacheTestActivity.this, "没有找到图片", Toast.LENGTH_SHORT).show();
                }

                return operation;
            }
        }).execute();
    }
    
    List<String> mFiles = null;
    private List<String> converData(List<MediaInfo> datas) {
        if (null != mFiles) {
            return mFiles;
        }
        
        mFiles = new ArrayList<String>();
        for (MediaInfo info : datas) {
            mFiles.add(info.getFullPath());
        }
        
        return mFiles;
    }

    class GridViewAdapter extends ImageViewAdapter {
        public GridViewAdapter(Context context, ImageLoader imageLoader) {
            super(context, imageLoader);
        }
    }
    
    class ScrollListenerImpl implements OnScrollListener {
        /** Previous first visible item */
        private int mPreviousFirstVisibleItem = 0;
        /** Image loader paused or not */
        private boolean mPauseWork = false;
        /** Is fling or not */
        private boolean mIsFling = false;
        
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mIsFling = (scrollState == OnScrollListener.SCROLL_STATE_FLING);
            boolean isIdle = (scrollState == OnScrollListener.SCROLL_STATE_IDLE);
            // 停止后不暂停
            if (isIdle) {
                setImageLoaderPaused(false);
            } else {
                setImageLoaderPaused(true);
            }
            
            if (true) {
                Log.d("CacheTestActivity#ScrollListenerImpl", "PictureAlbumLayout#ScrollListenerImpl$onScrollStateChanged  scrollState = "
                        + scrollState
                        + ", isFling = " + mIsFling
                        + ", isIdle = " + isIdle);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            if (mIsFling) {
//                if (mPreviousFirstVisibleItem != firstVisibleItem) {
//                    mPreviousFirstVisibleItem = firstVisibleItem;
//                    float velocity = mGridView.getCurrVelocity() / 1000; // SUPPRESS CHECKSTYLE
//                    // 2.4数据是一个经验值
//                    boolean paused = (velocity > 2.4); // SUPPRESS CHECKSTYLE
//                    setImageLoaderPaused(paused);
//                }
//            }
        }

        /**
         * Pause the image loader to load bitmap from disk or network.
         * 
         * @param paused paused or not.
         */
        private void setImageLoaderPaused(boolean paused) {
            if (null != mImageLoader) {
                if (mPauseWork != paused) {
                    mPauseWork = paused;
                    mImageLoader.setPauseWork(paused);
                }
            }
        }
    };
}
