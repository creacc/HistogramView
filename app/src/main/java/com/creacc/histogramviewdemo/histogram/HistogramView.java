package com.creacc.histogramviewdemo.histogram;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.creacc.histogramviewdemo.R;

import java.util.ArrayList;
import java.util.List;

public class HistogramView extends View {

    private HistogramAdapter mHistogramAdapter;
    private boolean mNeedPrepare;

    private List<String> mRowTitles;
    private List<ColumnGroup> mColumnGroups;

    private Paint mTableLinePaint;
    private Paint mTableSelectedPaint;
    private Paint mColumnPaint;
    private Paint mRowTitlePaint;
    private Paint mColumnTitlePaint;
    private RectF mRenderRect = new RectF();

    private float mViewWidth;
    private float mViewHeight;

    private float mRowHeight;
    private float mColumnWidth;

    private float mTableLeftMargin;
    private float mTableBottomMargin;

    private float mRowTextOffset;
    private float mColumnTextOffset;

    private int mRowCount;
    private int mColumnCount;

    private float mColumnItemPadding;
    private float mColumnItemWidth;
    private int mColumnItemCount;
    private int[] mItemColors;

    private int mRowTitleColor;
    private float mRowTitleSize;
    private float mRowTitlePadding;
    private int mColumnTitleColor;
    private float mColumnTitleSize;
    private float mColumnTitlePadding;
    private int mBaseLineColor;
    private int mSelectedColor;
    private float mBaseLineWidth;

    private RectF mTouchArea = new RectF();
    private boolean mIsTouchDown;
    private OnItemSelectListener mOnItemSelectListener;
    private Point mSelectPosition = new Point(-1, -1);


    public HistogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
        initPaints();
        if (isInEditMode()) {
            // for test
            setAdapter(new HistogramAdapter() {
                @Override
                public int getColumnGroupCount() {
                    return 6;
                }

                @Override
                public String getColumnGroupTitle(int position) {
                    return "测试文本";
                }

                @Override
                public int getColumnItemCount() {
                    return 3;
                }

                @Override
                public int getColumnColor(int itemPosition) {
                    return Color.parseColor("#338899");
                }

                @Override
                public float getColumnValue(int groupPosition, int itemPosition) {
                    return 0.6f;
                }

                @Override
                public int getRowCount() {
                    return 6;
                }

                @Override
                public String getRowTitle(int position) {
                    return "测试文本";
                }
            });
            mSelectPosition.set(1, 3);
        }
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        mOnItemSelectListener = onItemSelectListener;
    }

    private void parseAttrs(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HistogramView);
        try {
            mRowTitleColor = typedArray.getColor(R.styleable.HistogramView_rowTitleColor, Color.BLACK);
            mRowTitleSize = typedArray.getDimensionPixelSize(R.styleable.HistogramView_rowTitleSize, 20);
            mRowTitlePadding = typedArray.getDimensionPixelSize(R.styleable.HistogramView_rowTitlePadding, 10);

            mColumnTitleColor = typedArray.getColor(R.styleable.HistogramView_columnTitleColor, Color.BLACK);
            mColumnTitleSize = typedArray.getDimensionPixelSize(R.styleable.HistogramView_columnTitleSize, 20);
            mColumnTitlePadding = typedArray.getDimensionPixelSize(R.styleable.HistogramView_columnTitlePadding, 10);

            mBaseLineColor = typedArray.getColor(R.styleable.HistogramView_baseLineColor, Color.GRAY);
            mSelectedColor = typedArray.getColor(R.styleable.HistogramView_selectedColor, Color.GRAY);

            mBaseLineWidth = typedArray.getDimensionPixelSize(R.styleable.HistogramView_baseLineWidth, 1);
            mColumnItemWidth = typedArray.getDimensionPixelSize(R.styleable.HistogramView_columnItemWidth, 10);
            mColumnItemPadding = typedArray.getDimensionPixelSize(R.styleable.HistogramView_columnItemPadding, 10);
        } finally {
            typedArray.recycle();
        }
    }

    private void initPaints() {
        mTableLinePaint = new Paint();
        mTableLinePaint.setColor(mBaseLineColor);
        mTableLinePaint.setStrokeWidth(mBaseLineWidth);

        mRowTitlePaint = new Paint();
        mRowTitlePaint.setColor(mRowTitleColor);
        mRowTitlePaint.setTextSize(mRowTitleSize);
        mRowTitlePaint.setAntiAlias(true);
        Paint.FontMetrics fontMetrics = mRowTitlePaint.getFontMetrics();
        mRowTextOffset = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;

        mColumnTitlePaint = new Paint();
        mColumnTitlePaint.setColor(mColumnTitleColor);
        mColumnTitlePaint.setTextSize(mColumnTitleSize);
        mColumnTitlePaint.setAntiAlias(true);
        mColumnTitlePaint.setTextAlign(Paint.Align.CENTER);
        fontMetrics = mRowTitlePaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        mColumnTextOffset = textHeight / 2 - fontMetrics.bottom;
        mTableBottomMargin = textHeight + mColumnTitlePadding * 2;

        mColumnPaint = new Paint();
        mColumnPaint.setStrokeWidth(mColumnItemWidth);
        mColumnPaint.setStrokeJoin(Paint.Join.ROUND);
        mColumnPaint.setStrokeCap(Paint.Cap.ROUND);
        mColumnPaint.setAntiAlias(true);

        mTableSelectedPaint = new Paint();
        mTableSelectedPaint.setColor(mSelectedColor);
    }

    public void setAdapter(HistogramAdapter adapter) {
        mHistogramAdapter = adapter;

        mColumnCount = adapter.getColumnGroupCount();
        mColumnItemCount = adapter.getColumnItemCount();
        mColumnGroups = new ArrayList<>(mColumnCount);
        mItemColors =  new int[mColumnCount];
        for (int i = 0; i < mColumnCount; i++) {
            float[] values = new float[mColumnItemCount];
            for (int itemIndex = 0; itemIndex < mColumnItemCount; itemIndex++) {
                if (i == 0) {
                    mItemColors[itemIndex] = adapter.getColumnColor(itemIndex);
                }
                values[itemIndex] = adapter.getColumnValue(i, itemIndex);
            }
            mColumnGroups.add(new ColumnGroup(adapter.getColumnGroupTitle(i), values));
        }

        mRowCount = adapter.getRowCount();
        mRowTitles = new ArrayList<>(mRowCount);
        for (int i = 0; i < mRowCount; i++) {
            String rowTitle = adapter.getRowTitle(i);
            mTableLeftMargin = Math.max(mTableLeftMargin, mRowTitlePaint.measureText(rowTitle));
            mRowTitles.add(rowTitle);
        }
        mTableLeftMargin += mRowTitlePadding * 2;
        mNeedPrepare = true;
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mNeedPrepare = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mHistogramAdapter ==  null) {
            return;
        }

        if (mNeedPrepare) {
            mNeedPrepare = false;
            prepare();
        }
        drawSelectedItem(canvas);
        drawTable(canvas);
        drawTitle(canvas);
        drawItem(canvas);
    }

    private void prepare() {
        mRowHeight = (mViewHeight - mTableBottomMargin) / mRowCount;
        mColumnWidth = (mViewWidth - mTableLeftMargin) / mColumnCount;
        mTouchArea.set(mTableLeftMargin, mRowHeight, mViewWidth, mViewHeight - mTableBottomMargin);
    }

    private void drawTable(Canvas canvas) {
        float rowRenderY;
        for (int i = 0; i < mRowCount + 1; i++) {
            rowRenderY = mRowHeight * i;
            canvas.drawLine(mTableLeftMargin, rowRenderY, mViewWidth, rowRenderY, mTableLinePaint);
        }

        float columnRenderX;
        float renderBottomY = mViewHeight - mTableBottomMargin;
        for (int i = 0; i < mColumnCount; i++) {
            columnRenderX = mTableLeftMargin + mColumnWidth * i;
            canvas.drawLine(columnRenderX, mRowHeight, columnRenderX, renderBottomY, mTableLinePaint);
        }
    }

    private void drawSelectedItem(Canvas canvas) {
        if (mSelectPosition.x == -1 || mSelectPosition.y == -1) {
            return;
        }
        float selectLeft = mTableLeftMargin + mColumnWidth * mSelectPosition.x;
        mRenderRect.set(selectLeft, mRowHeight, selectLeft + mColumnWidth, mViewHeight - mTableBottomMargin);
        canvas.drawRect(mRenderRect, mTableSelectedPaint);
        float selectTop = mRowHeight * (mSelectPosition.y + 1);
        mRenderRect.set(mTableLeftMargin, selectTop, mViewWidth, selectTop + mRowHeight);
        canvas.drawRect(mRenderRect, mTableSelectedPaint);
    }

    private void drawTitle(Canvas canvas) {
        for (int i = 0; i < mRowCount; i++) {
            canvas.drawText(mRowTitles.get(i), mRowTitlePadding, mRowHeight * (i + 1) + mRowTextOffset, mRowTitlePaint);
        }

        float columnTitleRenderY = mViewHeight - mTableBottomMargin / 2 - mColumnTitlePadding / 2 + mColumnTextOffset;
        for (int i = 0; i < mColumnCount; i++) {
            canvas.drawText(mColumnGroups.get(i).getTitle(), mTableLeftMargin + mColumnWidth / 2 + (mColumnWidth * i), columnTitleRenderY, mColumnTitlePaint);
        }
    }

    private void drawItem(Canvas canvas) {
        float itemLeft = (mColumnWidth - (mColumnItemWidth * mColumnItemCount + mColumnItemPadding * (mColumnItemCount - 1))) / 2 + mColumnItemWidth / 2;
        float itemBottom = mViewHeight - mTableBottomMargin - mColumnItemPadding / 2;

        for (int i = 0; i < mColumnCount; i++) {
            ColumnGroup columnGroup = mColumnGroups.get(i);
            float[] columnValues = columnGroup.getColumnValues();
            for (int itemIndex = 0; itemIndex < mColumnItemCount; itemIndex++) {
                mColumnPaint.setColor(mItemColors[itemIndex]);
                float itemRenderX = mTableLeftMargin + itemLeft + (mColumnItemWidth + mColumnItemPadding) * itemIndex + mColumnWidth * i;
                canvas.drawLine(itemRenderX, itemBottom, itemRenderX, itemBottom - (itemBottom - mRowHeight - mColumnItemPadding) * columnValues[itemIndex], mColumnPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouchDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (mIsTouchDown) {
                    mIsTouchDown = false;
                    float x = event.getX();
                    float y = event.getY();
                    if (mTouchArea.contains(x, y)) {
                        int column = (int) ((x - mTableLeftMargin) / mColumnWidth);
                        int row = (int) (y / mRowHeight - 1);
                        if (mSelectPosition.equals(column, row) == false) {
                            mSelectPosition.set(column, row);
                            if (mOnItemSelectListener != null) {
                                mOnItemSelectListener.onItemSelected(row, column);
                            }
                            invalidate();
                        }
                    } else {
                        if (mSelectPosition.equals(-1, -1) == false) {
                            mSelectPosition.set(-1, -1);
                            if (mOnItemSelectListener != null) {
                                mOnItemSelectListener.onCancelSelection();
                            }
                            invalidate();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsTouchDown = false;
                mSelectPosition.set(-1, -1);
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }
}
