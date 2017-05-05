package com.lee.sdk.test.anim;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.sdk.anim.Flip3dAnimation.OnFlip3dAnimationListener;
import com.lee.sdk.cache.IAsyncView;
import com.lee.sdk.cache.api.ImageLoader;
import com.lee.sdk.task.Task;
import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.GABaseActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.test.utils.ImageSearchUtil;
import com.lee.sdk.test.utils.MediaInfo;
import com.lee.sdk.test.widget.ImageViewAdapter;
import com.lee.sdk.utils.Utils;
import com.lee.sdk.widget.Filp3dAnimatedView;

public class FlipAnimationActivity extends GABaseActivity {
    private int DIALOG_WIDTH = 500;
    private int DIALOG_HEIGHT = 500;
    private static final int COLUMN_NUM = 3;
    private static final int SPACE = 10;
    private static final int DURATION = 500;

    private GridView mGridView = null;
    private ItemDetailLayout mItemDetailLayout = null;
    private Filp3dAnimatedView mAnimatedView = null;
    private ImageSearchUtil mSearchUtil = null;
    private ArrayList<MediaInfo> mDatas = null;
    private Bitmap mToBitmap = null;
    private Bitmap mFromBitmap = null;
    private int mOpenPosition = -1;
    private boolean mIsAnimPlaying = false;
    private ImageLoader mImageLoader;

    private OnFlip3dAnimationListener mListener = new OnFlip3dAnimationListener() {
        @Override
        public void onAnimationPlaying(Animation anim, float time) {
            // float alpha = 180 * time;
            // mItemDetailLayout.setBackgroundColorAlpha((int)alpha);
        }
    };

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
//
//                return null;
//            }
//        });

        initialize();
        // Start task to load image data.
        loadImageAsync();
    }
    
    private void initCacheBitmap(int width, int height) {
        if (null == mFromBitmap || null == mFromBitmap) {
            try {
                mFromBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                mToBitmap = Bitmap.createBitmap(DIALOG_WIDTH, DIALOG_HEIGHT, Config.ARGB_8888);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize() {
        mSearchUtil = new ImageSearchUtil(this);
        mGridView = new GridView(this);
        mGridView.setVerticalSpacing(SPACE);
        mGridView.setHorizontalSpacing(SPACE);
        //mGridView.setColumnWidth(ITEM_WIDTH);
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
                onGridItemClick(view, position);
            }
        });

        mItemDetailLayout = new ItemDetailLayout(this);

        mAnimatedView = new Filp3dAnimatedView(this);
        mAnimatedView.setVisibility(View.INVISIBLE);
        mAnimatedView.setViewSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        mAnimatedView.setFlipAnimationListener(mListener);

        FrameLayout layout = new FrameLayout(this);
        layout.addView(mGridView);
        layout.addView(mItemDetailLayout, new FrameLayout.LayoutParams(-1, -1));
        layout.addView(mAnimatedView, new FrameLayout.LayoutParams(-1, -1));

        setContentView(layout);
    }

    private void onGridItemClick(View v, int position) {
        doOpenAnimation(v, position);
    }

    private void doOpenAnimation(View v, int position) {
        if (mIsAnimPlaying) {
            return;
        }

        mIsAnimPlaying = true;
        mOpenPosition = position;
        mItemDetailLayout.setItemDetailInfo(mDatas.get(position));

        initCacheBitmap(v.getWidth(), v.getHeight());
        
        View contentView = mItemDetailLayout.getContentView();
        mFromBitmap = getViewCapture(v, mFromBitmap);
        mToBitmap = getViewCapture(contentView, mToBitmap);
        mAnimatedView.setBitmaps(mFromBitmap, mToBitmap);
        mAnimatedView.setVisibility(View.VISIBLE);
        mAnimatedView.doAnimation(v, contentView, DURATION, false, new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimPlaying = true;
                mItemDetailLayout.interceptEvent(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimatedView.clearAnimation();
                mAnimatedView.setVisibility(View.INVISIBLE);
                mItemDetailLayout.showContent(true);

                mIsAnimPlaying = false;
            }
        });

        v.setVisibility(View.INVISIBLE);
    }

    private void doCloseAnimation() {
        if (mIsAnimPlaying) {
            return;
        }

        View v = mGridView.getChildAt(mOpenPosition - mGridView.getFirstVisiblePosition());
        if (null == v) {
            return;
        }

        mIsAnimPlaying = true;
        View contentView = mItemDetailLayout.getContentView();
        mItemDetailLayout.showContent(false);
        mAnimatedView.setVisibility(View.VISIBLE);
        mAnimatedView.doAnimation(v, contentView, DURATION, true, new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimPlaying = true;
                mItemDetailLayout.interceptEvent(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimatedView.clearAnimation();
                mAnimatedView.setVisibility(View.INVISIBLE);
                mIsAnimPlaying = false;
                mOpenPosition = -1;
                mItemDetailLayout.interceptEvent(false);

                ((GridViewAdapter) mGridView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void loadImageAsync() {
        // Start task to load image data.
        new TaskManager().next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(FlipAnimationActivity.this);
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
                    Toast.makeText(FlipAnimationActivity.this, "没有找到图片", Toast.LENGTH_SHORT).show();
                }

                return operation;
            }
        }).execute();
    }

    public static Bitmap getViewCapture(View child, Bitmap oldBitmap) {
        if (null == child) {
            return oldBitmap;
        }

        Bitmap bitmap = oldBitmap;

        try {
            // Draw the shelf layout to the canvas.
            if (null != bitmap) {
                Canvas canvas = new Canvas(bitmap);
                child.draw(canvas);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public class GridViewAdapter extends ImageViewAdapter {
        public GridViewAdapter(Context context, ImageLoader imageLoader) {
            super(context, imageLoader);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setVisibility((position == mOpenPosition) ? View.INVISIBLE : View.VISIBLE);

            return view;
        }
    }

    private class ItemDetailLayout extends FrameLayout implements IAsyncView {
        private View mPannel = null;
        private TextView mFileName = null;
        private TextView mFilePath = null;
        private TextView mFileUri = null;
        private TextView mFileSize = null;
        private ImageView mImageView = null;
        private Drawable mDrawable = null;
        private View mContentView = null;

        private OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCloseAnimation();
            }
        };

        public ItemDetailLayout(Context context) {
            super(context);

            LayoutInflater.from(context).inflate(R.layout.item_detail_layout, this);
            mPannel = findViewById(R.id.translate_panel);
            mFileName = (TextView) findViewById(R.id.file_name);
            mFilePath = (TextView) findViewById(R.id.file_path);
            mFileUri = (TextView) findViewById(R.id.file_uri);
            mFileSize = (TextView) findViewById(R.id.file_size);
            mImageView = (ImageView) findViewById(R.id.image_thumb);
            mContentView = this.findViewById(R.id.content);
            mImageView.setBackgroundResource(android.R.drawable.picture_frame);
            ViewGroup.LayoutParams params = mContentView.getLayoutParams();
            params.width = DIALOG_WIDTH;
            params.height = DIALOG_HEIGHT;

            setBackgroundColorAlpha(0);
            mPannel.setOnClickListener(mListener);
            mPannel.setClickable(false);
            mContentView.setClickable(true);

            showContent(false);
        }

        public void showContent(boolean show) {
            mContentView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            // mPannel.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mPannel.setClickable(show);
        }

        public void interceptEvent(boolean intercept) {
            mPannel.setClickable(intercept);
        }

        public void setBackgroundColorAlpha(int alpha) {
            mPannel.setBackgroundColor(Color.argb(alpha, 30, 30, 30));
        }

        public void setItemDetailInfo(MediaInfo info) {
            Uri uri = info.getImageUri();
            mFileName.setText(info.getDisplayName());
            mFilePath.setText(info.getFullPath());
            if (null != uri) {
                mFileUri.setText(uri.toString());
            }
            mFileSize.setText(String.valueOf(info.getSize()) + " bytes");
            mImageLoader.loadImage(info, this);
        }

        public View getContentView() {
            return mContentView;
        }

        public void setImageBitmap(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
        }

        @Override
        public void setImageDrawable(Drawable drawable) {
        }

        @Override
        public void setAsyncDrawable(Drawable drawable) {
            mDrawable = drawable;
        }

        @Override
        public Drawable getAsyncDrawable() {
            return mDrawable;
        }

        @Override
        public boolean isGifSupported() {
            return false;
        }
    }
}
