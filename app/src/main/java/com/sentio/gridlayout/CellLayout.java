package com.sentio.gridlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ViewConstructor")
public class CellLayout extends ViewGroup {
    private final View[][] fillViews;
    private final CoordinateLayout coordinateLayout;

    public CellLayout(Context context, int width, int height, int estimateWidth, int estimateHeight) {
        super(context);
        this.coordinateLayout = new CoordinateLayout(context, width, height, estimateWidth, estimateHeight);
        this.addView(coordinateLayout);
        this.fillViews = new View[coordinateLayout.getCellWidth()][coordinateLayout.getCellHeight()];
    }

    public boolean addViewToCellLayout(View child, int id, LayoutParams layoutParams) {
        child.setId(id);
        child.setLayoutParams(layoutParams);
        coordinateLayout.addView(child, layoutParams);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        coordinateLayout.layout(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        coordinateLayout.measure(widthMeasureSpec, heightMeasureSpec);
    }

    void move(View view, float x, float y) throws InvalidPosition {
        coordinateLayout.removeView(view);
        LayoutParams layoutParams = ((LayoutParams) view.getLayoutParams()).copy();
        layoutParams.cellX = (int) (x / coordinateLayout.getCellWidth());
        layoutParams.cellY = (int) (y / coordinateLayout.getCellHeight());
        if (!coordinateLayout.isValid(layoutParams)) {
            coordinateLayout.addView(view);
            throw new InvalidPosition();
        }
        view.setLayoutParams(layoutParams);
        coordinateLayout.addView(view, layoutParams);
    }

    void fillView(float x, float y, LayoutParams layoutParams) {
        int cellX = (int) (x / coordinateLayout.getCellWidth());
        int cellY = (int) (y / coordinateLayout.getCellHeight());
        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#50ffffff"));
        layoutParams.cellX = cellX;
        layoutParams.cellY = cellY;
        view.setLayoutParams(layoutParams);
        fillViews[cellX][cellY] = view;
        coordinateLayout.addView(view, layoutParams);
    }

    void clearFillViews() {
        for (int i = 0; i < fillViews.length; i++) {
            for (int j = 0; j < fillViews.length; j++) {
                if (fillViews[i][j] != null) {
                    coordinateLayout.removeView(fillViews[i][j]);
                    fillViews[i][j] = null;
                }
            }
        }
    }

    void removeCellView(View view) {
        coordinateLayout.removeView(view);
    }

    @SuppressWarnings("WeakerAccess")
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         Horizontal location of the item in the grid.
         */
        public int cellX;

        /**
         Vertical location of the item in the grid.
         */
        public int cellY;

        /**
         Temporary horizontal location of the item in the grid during reorder
         */
        public int tmpCellX;

        /**
         Temporary vertical location of the item in the grid during reorder
         */
        public int tmpCellY;

        /**
         Indicates that the temporary coordinates should be used to layout the items
         */
        public boolean useTmpCoords;

        /**
         Number of cells spanned horizontally by the item.
         */
        public int cellHSpan;

        /**
         Number of cells spanned vertically by the item.
         */
        public int cellVSpan;

        /**
         X coordinate of the view in the layout.
         */
        public int x;

        /**
         Y coordinate of the view in the layout.
         */
        public int y;

        /**
         Indicate if the view has dropped
         */
        public boolean dropped = true;

        public LayoutParams copy() {
            return new LayoutParams(this);
        }

        public LayoutParams(LayoutParams source) {
            this(source.cellX, source.cellY, source.cellHSpan, source.cellVSpan);
        }

        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        private Rect toRect() {
            return new Rect(cellX, cellY, cellX + cellHSpan, cellY + cellVSpan);
        }

        public boolean intersect(LayoutParams layoutParams) {
            return toRect().intersect(layoutParams.toRect());
        }

        public void setup(int cellWidth, int cellHeight) {
            setup(cellWidth, cellHeight, 1.0f, 1.0f);
        }

        public void setup(int cellWidth, int cellHeight, float cellScaleX, float cellScaleY) {
            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            int myCellX = useTmpCoords ? tmpCellX : cellX;
            int myCellY = useTmpCoords ? tmpCellY : cellY;

            width = (int) (myCellHSpan * cellWidth / cellScaleX - leftMargin - rightMargin);
            height = (int) (myCellVSpan * cellHeight / cellScaleY - topMargin - bottomMargin);

            x = (myCellX * cellWidth + leftMargin);
            y = (myCellY * cellHeight + topMargin);
        }
    }
}
