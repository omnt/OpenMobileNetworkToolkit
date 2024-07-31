package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.GridLayout;

import java.util.HashMap;
import java.util.List;

public class DraggableGridLayout extends GridLayout {

    private View draggedView;

    public DraggableGridLayout(Context context) {
        super(context);
        init();
    }

    public DraggableGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setColumnCount(4); // Set default column count, can be adjusted
        setRowCount(4); // Set default row count, can be adjusted
        setOnDragListener(new DragListener());
    }

    public void setViews(HashMap<Integer, List<View>> viewsMap) {
        removeAllViews();
        for (HashMap.Entry<Integer, List<View>> entry : viewsMap.entrySet()) {
            Integer column = entry.getKey();
            List<View> views = entry.getValue();
            if (views == null) continue;

            for (View view : views) {
                if (view == null) continue;

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(column);
                params.rowSpec = GridLayout.spec(0); // Default to row 0
                view.setLayoutParams(params);
                view.setOnLongClickListener(new LongClickListener());
                addView(view);
            }
        }
    }

    private class LongClickListener implements OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            draggedView = v;
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) v.getLayoutParams();
            DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
            v.startDragAndDrop(null, shadowBuilder, v, 0);
            return true;
        }
    }

    private class DragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    View targetView = findViewAtPosition((int) event.getX(), (int) event.getY());
                    if (targetView == null || targetView == draggedView) {
                        return false;
                    }

                    swapViews(draggedView, targetView);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    draggedView = null;
                    return true;
                default:
                    return false;
            }
        }

        private View findViewAtPosition(int x, int y) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getLeft() <= x && child.getRight() >= x && child.getTop() <= y && child.getBottom() >= y) {
                    return child;
                }
            }
            return null;
        }

        private void swapViews(View view1, View view2) {
            GridLayout.LayoutParams params1 = (GridLayout.LayoutParams) view1.getLayoutParams();
            GridLayout.LayoutParams params2 = (GridLayout.LayoutParams) view2.getLayoutParams();

            view1.setLayoutParams(params2);
            view2.setLayoutParams(params1);
        }
    }
}
