package com.sentio.gridlayout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private Random rnd = new Random();
    private CellLayout cellLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpCellLayout();
        setContentView(cellLayout);
        initView();
    }

    private void setUpCellLayout() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        cellLayout = new CellLayout(this,
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                getResources().getDimensionPixelSize(R.dimen.item_width),
                getResources().getDimensionPixelSize(R.dimen.item_height)
        );

    }

    private void setOnDragListener(final float deltaX, final float deltaY) {
        cellLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent event) {
                View target = (View) event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        cellLayout.removeCellView(target);
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        cellLayout.clearFillViews();
                        cellLayout.fillView(event.getX() - deltaX, event.getY() - deltaY,
                                ((CellLayout.LayoutParams) target.getLayoutParams()).copy());
                        break;
                    case DragEvent.ACTION_DROP:
                        try {
                            cellLayout.clearFillViews();
                            cellLayout.move(target, event.getX() - deltaX, event.getY() - deltaY);
                        } catch (InvalidPosition invalidPosition) {
                            Toast.makeText(MainActivity.this, "Invalid position", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initView() {
        addView(100, 0, 0, 1, 1);
        addView(200, 1, 1, 1, 1);
        addView(300, 4, 0, 2, 2);
        addView(400, 7, 2, 1, 2);
        addView(500, 4, 4, 2, 1);
    }


    //noinspection unchecked
    private View addView(int id, int x, int y, int spanH, int spanV) {
        final View view = new View(this);
        final Integer color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        view.setBackgroundColor(color);
        cellLayout.addViewToCellLayout(view, id, new CellLayout.LayoutParams(x, y, spanH, spanV));
        final GestureDetector longClickDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent event) {
                        ShadowBuilder shadowBuilder = new ShadowBuilder(view, color, event.getX(), event.getY());
                        setOnDragListener(event.getX() / 2, event.getY() / 2);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            view.startDragAndDrop(null, shadowBuilder, view, 0);
                        } else {
                            view.startDrag(null, shadowBuilder, view, 0);
                        }
                    }
                });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return longClickDetector.onTouchEvent(event);
            }
        });
        return view;
    }

    private class ShadowBuilder extends View.DragShadowBuilder {
        private final Drawable shadow;
        private final int xPointer;
        private final int yPointer;

        ShadowBuilder(View view, int color, float x, float y) {
            super(view);
            shadow = new ColorDrawable(color);
            xPointer = (int) x;
            yPointer = (int) y;
        }

        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            int width = getView().getWidth();
            int height = getView().getHeight();
            shadow.setBounds(0, 0, width, height);
            size.set(width, height);
            touch.set(xPointer, yPointer);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }
    }
}
