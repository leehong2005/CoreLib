package com.lee.sdk.test.staggered;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.task.Task;
import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.utils.DensityUtils;
import com.lee.sdk.widget.staggered.BdAbsListView;
import com.lee.sdk.widget.staggered.BdAbsListView.OnScrollListener;
import com.lee.sdk.widget.staggered.BdAdapterView;
import com.lee.sdk.widget.staggered.BdAdapterView.OnItemClickListener;
import com.lee.sdk.widget.staggered.BdMultiColumnListView;

public class ImageAlbumActivity extends GABaseActivity {
    private static final boolean DEBUG = true;
    private static final String TAG = "ImageAlbumActivity";
    private BdMultiColumnListView mListView = null;
    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int leftPadding = DensityUtils.dip2px(this, 8);
        int rightPadding = leftPadding;
        int horizontalSpacing = leftPadding;
        int color = Color.argb(14, 0, 0, 0);
        mImageLoader = ImageLoader.Builder.newInstance(this).setUseDiskCache(true)
                .setDiskCacheDir(Utils.getImageCacheDirectory(this)).setMaxCachePercent(0.3f).build();
//        mImageLoader.setOnProcessBitmapListener(new OnProcessBitmapListener() {
//            @Override
//            public Bitmap onProcessBitmap(Object data) {
//                if (data instanceof String) {
//                    return Utils.getBitmapFromNet((String) data, 1, "", "");
//                }
//
//                return null;
//            }
//        });

        ImageAlbumAdapter adapter = new ImageAlbumAdapter(this);
        adapter.setImageLoader(mImageLoader);
        mListView = new BdMultiColumnListView(this);
        mListView.setAdapter(adapter);
        mListView.setSelector(new ColorDrawable(color));
        mListView.setDrawSelectorOnTop(true);
        mListView.setEdgeGlowTopEnabled(false);
        mListView.setColumnPadding(leftPadding, rightPadding);
        mListView.setHorizontalSpacing(horizontalSpacing);
        mListView.setOnScrollListener(new ScrollListenerImpl());
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BdAdapterView<?> parent, View view, int position, long id) {
                
            }
        });

        setContentView(mListView);

        loadImageAsync();
    }
    
    private void loadImageAsync() {
        final ArrayList<ImageAlbumItem> albums = new ArrayList<ImageAlbumItem>();
        final Context context = ImageAlbumActivity.this;
        // Start task to load image data.
        new TaskManager().next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Load image");
                progressDialog.setMessage("Load images, please wait...");
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
                albums.addAll(ImageAlbumLoader.getInstance(context).loadCacheAlbumsData(context, 0));
                return operation;
            }
        }).next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                ProgressDialog mProgressDialog = (ProgressDialog) params[0];
                mProgressDialog.dismiss();

                ImageAlbumAdapter adapter = (ImageAlbumAdapter) mListView.getAdapter();
                adapter.addDatas(albums);

                return operation;
            }
        }).execute();
    }
    
    class ScrollListenerImpl implements OnScrollListener {
        /** Previous first visible item */
        private int mPreviousFirstVisibleItem = 0;
        /** Image loader paused or not */
        private boolean mPauseWork = false;
        /** Is fling or not */
        private boolean mIsFling = false;
        
        @Override
        public void onScrollStateChanged(BdAbsListView view, int scrollState) {
            mIsFling = (scrollState == OnScrollListener.SCROLL_STATE_FLING);
            boolean isIdle = (scrollState == OnScrollListener.SCROLL_STATE_IDLE);
            // 停止后不暂停
            if (isIdle) {
                setImageLoaderPaused(false);
            }
            
            if (DEBUG) {
                Log.d(TAG, "PictureAlbumLayout#ScrollListenerImpl$onScrollStateChanged  scrollState = "
                        + scrollState
                        + ", isFling = " + mIsFling
                        + ", isIdle = " + isIdle);
            }
        }

        @Override
        public void onScroll(BdAbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mIsFling) {
                if (mPreviousFirstVisibleItem != firstVisibleItem) {
                    mPreviousFirstVisibleItem = firstVisibleItem;
                    float velocity = mListView.getCurrVelocity() / 1000; // SUPPRESS CHECKSTYLE
                    // 2.4数据是一个经验值
                    boolean paused = (velocity > 2.4); // SUPPRESS CHECKSTYLE
                    setImageLoaderPaused(paused);
                }
            }
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
