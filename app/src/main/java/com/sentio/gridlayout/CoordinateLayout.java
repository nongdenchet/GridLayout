package com.sentio.gridlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ViewConstructor")
public class CoordinateLayout extends ViewGroup {
    private final int width;
    private final int height;

    private int numberOfColumns;
    private int numberOfRows;
    private int cellWidth;
    private int cellHeight;

    public CoordinateLayout(Context context, int width, int height, int estimateWidth, int estimateHeight) {
        super(context);
        this.width = width;
        this.height = height;
        this.calculateSizes(estimateWidth, estimateHeight);
    }

    public void calculateSizes(int estimateWidth, int estimateHeight) {
        numberOfColumns = Math.round(width / estimateWidth);
        numberOfRows = Math.round(height / estimateHeight);
        cellWidth = width / numberOfColumns;
        cellHeight = height / numberOfRows;
    }

    int getNumberOfColumns() {
        return numberOfColumns;
    }

    int getNumberOfRows() {
        return numberOfRows;
    }

    int getCellWidth() {
        return cellWidth;
    }

    int getCellHeight() {
        return cellHeight;
    }

    @Nullable
    @SuppressWarnings("unused")
    public View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) child.getLayoutParams();
            if ((layoutParams.cellX <= x) && (x < layoutParams.cellX + layoutParams.cellHSpan) &&
                    (layoutParams.cellY <= y) && (y < layoutParams.cellY + layoutParams.cellVSpan)) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) child.getLayoutParams();
                int childLeft = layoutParams.x;
                int childTop = layoutParams.y;
                child.layout(childLeft, childTop, childLeft + layoutParams.width, childTop + layoutParams.height);

                if (layoutParams.dropped) {
                    layoutParams.dropped = false;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child);
            }
        }
    }

    public void measureChild(View child) {
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) child.getLayoutParams();
        layoutParams.setup(cellWidth, cellHeight);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Nullable
    View getViewOccupied(CellLayout.LayoutParams layoutParams) {
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (layoutParams.intersect((CellLayout.LayoutParams) child.getLayoutParams())) {
                return child;
            }
        }
        return null;
    }

    boolean isValid(CellLayout.LayoutParams layoutParams) {
        if (layoutParams.cellX + layoutParams.cellHSpan > getNumberOfColumns()) {
            return false;
        }
        if (layoutParams.cellY + layoutParams.cellVSpan > getNumberOfRows()) {
            return false;
        }
        View occupiedView = getViewOccupied(layoutParams);
        return occupiedView == null;
    }
}
