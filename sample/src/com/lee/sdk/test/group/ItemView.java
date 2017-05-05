package com.lee.sdk.test.group;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lee.sdk.test.R;
import com.lee.sdk.utils.DrawTextUtil;
import com.lee.sdk.utils.Utils;

public class ItemView extends LinearLayout {
    private static final boolean ONLY_DRAW_FLAG = false;
    private static final boolean USE_STATICLAYOUT = false;
    private static final boolean USE_TEXTVIEW = true;
    public int ITEM_WIDTH = 200;
    public int ITEM_HEIGHT = 200;

    public static class LineItem {
        private boolean isLastInGroup = false;
        private boolean isFirstInGroup = false;
        private String groupText = "";
        private List<String> datas = null;

        public LineItem() {
            isFirstInGroup = true;
            isLastInGroup = true;
        }

        public LineItem(ArrayList<String> srcData, int start, int end) {
            datas = srcData.subList(start, end);

            if (0 == start) {
                isFirstInGroup = true;
            }

            if (end == srcData.size()) {
                isLastInGroup = true;
            }
        }

        public void setGroupTitle(String title) {
            groupText = title;
        }
    }

    private Drawable m_listDivider = null;

    private int mTextSize = 22;
    private LineItem m_data = null;
    private Paint m_paint = new Paint();
    private Rect m_rect = null;
    private int mGroupTitleTop = 30;
    private int mGroupTitleLeft = 5;
    private GroupGridViewActivity mActivity = null;

    public ItemView(Context context) {
        this(context, null);
    }

    public ItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ITEM_WIDTH = (int) Utils.pixelToDp(context, ITEM_WIDTH);
        ITEM_HEIGHT = (int) Utils.pixelToDp(context, ITEM_HEIGHT);
        mTextSize = (int) Utils.pixelToDp(context, mTextSize);
        mGroupTitleTop = (int) Utils.pixelToDp(context, mGroupTitleTop);
        mGroupTitleLeft = (int) Utils.pixelToDp(context, mGroupTitleLeft);

        this.m_listDivider = context.getResources().getDrawable(R.drawable.shelt_mid);
        this.m_rect = new Rect(0, 0, ITEM_WIDTH, ITEM_HEIGHT);
        this.m_rect.offset(0, 10);
        this.setWillNotDraw(false);
        this.mActivity = (GroupGridViewActivity) context;
    }

    public void setData(LineItem item) {
        m_data = item;

        if (ONLY_DRAW_FLAG) {
            invalidate();
        } else {
            layoutChildren();
        }
    }

    private void layoutChildren() {
        if (null == m_data) {
            this.removeAllViews();
            return;
        }

        List<String> datas = m_data.datas;
        if (null == datas || datas.size() == 0) {
            this.removeAllViews();
            return;
        }

        int childCount = this.getChildCount();
        int size = datas.size();
        if (size < childCount) {
            this.removeViews(0, childCount - size);
        }

        for (int i = 0; i < size; ++i) {
            View v = this.getChildAt(i);
            if (null == v) {
                v = createView();
                if (null != v) {
                    addView(v);
                }
            }

            if (null != v) {
                ProgramItemView itemView = (ProgramItemView) v;
                itemView.setData(datas.get(i));
            }
        }
    }

    private View createView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT);
        int margin = (int) Utils.pixelToDp(this.getContext(), 10);
        params.rightMargin = margin;
        params.topMargin = margin;
        View v = new ProgramItemView(this.getContext());
        v.setLayoutParams(params);

        return v;
    }

    /**
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null == m_data) {
            return;
        }

        if (ONLY_DRAW_FLAG) {
            List<String> datas = m_data.datas;
            if (null != datas) {
                for (int i = 0; i < datas.size(); ++i) {
                    onDrawItem(canvas, datas.get(i), i);
                }
            }
        }

        m_paint.setTextSize(mTextSize);
        m_paint.setColor(Color.WHITE);
        m_paint.setStyle(Style.STROKE);

        boolean isFirst = m_data.isFirstInGroup;
        boolean isLast = m_data.isLastInGroup;
        if (isFirst) {
            m_paint.setColor(Color.WHITE);
            canvas.drawText(m_data.groupText, mGroupTitleLeft, mGroupTitleTop, m_paint);
        }

        if (isLast) {
            m_listDivider.setBounds(0, getHeight() - 20, getWidth(), getHeight());
            m_listDivider.draw(canvas);
        } else {
            int leftPadding = this.getPaddingLeft();
            canvas.drawRect(leftPadding, getHeight() - 2, leftPadding + getWidth(), getHeight(), m_paint);
        }
    }

    private void onDrawItem(Canvas canvas, String data, int index) {
        int leftPadding = this.getPaddingLeft();
        int dx = leftPadding + index * (ITEM_WIDTH + 10);

        m_paint.setColor(Color.GRAY);
        m_paint.setStyle(Style.FILL);
        canvas.drawRect(m_rect.left + dx, m_rect.top, m_rect.right + dx, m_rect.bottom, m_paint);

        m_paint.setTextSize(mTextSize);
        m_paint.setColor(Color.WHITE);
        m_paint.setStyle(Style.STROKE);
        canvas.drawText(data, dx + mGroupTitleLeft, mGroupTitleTop, m_paint);
    }

    private class ProgramItemView extends View {
        private TextPaint m_paint = new TextPaint();
        private String m_data = "";
        private StaticLayout m_layout = null;
        private Rect m_bounds = new Rect();

        public ProgramItemView(Context context) {
            super(context);

            m_paint.setTextSize(mTextSize);
            m_paint.setColor(Color.WHITE);
            m_paint.setStyle(Style.STROKE);
            m_paint.setAntiAlias(true);

            this.setBackgroundColor(Color.GRAY);
        }

        public void setData(String data) {
            m_data = data;

            if (!TextUtils.isEmpty(data)) {
                CharSequence source = data;
                TextPaint paint = m_paint;
                int width = ITEM_WIDTH;
                Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
                float spacingmult = 1.0f;
                float spacingadd = 0.0f;
                boolean includepad = true;

                m_layout = new StaticLayout(source, 0, source.length(), paint, width - 10, align, spacingmult,
                        spacingadd, includepad);
            }

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            onDrawItem(canvas, m_data);
        }

        private void onDrawItem(Canvas canvas, String data) {
            if (USE_STATICLAYOUT) {
                if (null != m_layout) {
                    canvas.save();
                    canvas.translate(10, 0);
                    m_layout.draw(canvas);
                    canvas.restore();
                }
            } else {
                if (USE_TEXTVIEW) {
                    TextView textView = mActivity.getTextView();
                    if (null != textView) {
                        textView.setText(data);
                        textView.draw(canvas);
                    }
                } else {
                    m_bounds.set(0, 0, getWidth(), getHeight());
                    DrawTextUtil.drawText(canvas, m_paint, m_bounds, m_data, false, false);
                }
            }
        }
    }
}
