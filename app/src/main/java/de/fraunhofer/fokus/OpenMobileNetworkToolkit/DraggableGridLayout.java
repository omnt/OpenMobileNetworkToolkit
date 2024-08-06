package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.HashMap;




public class DraggableGridLayout extends GridLayout {
    public interface OnViewSwapListener {
        boolean onViewSwapped(View view1, View view2);
    }
    private View draggedView;
    private OnViewSwapListener onViewSwapListener;
    private Context context;
    public DraggableGridLayout(Context context) {
        super(context);
        init(context);
        this.context = context;
        this.setUseDefaultMargins(true);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.setBackgroundColor(0x00000000);
    }

    public DraggableGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DraggableGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    public void setOnViewSwapListener(OnViewSwapListener listener) {
        this.onViewSwapListener = listener;
    }
    private void init(Context context) {
        setColumnCount(10); // Set default column count, can be adjusted
        setRowCount(10); // Set default row count, can be adjusted
        setOnDragListener(new DragListener());
    }

    private void resize(View view, float scaleX, float scaleY) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) (view.getWidth() * scaleX);
        layoutParams.height = (int) (view.getHeight() * scaleY);
        view.setLayoutParams(layoutParams);
    }

    public void setViews(ViewsManager viewsManager) {
        View[][] views = viewsManager.getViewsMap();
        removeAllViews();
        setColumnCount(viewsManager.lastFilledNonNullColumn()+1);
        setRowCount(viewsManager.lastFilledMaxNonNullRow()+1);
        for(int i = 0; i < getColumnCount(); i++) {
            for(int j = 0; j < getRowCount(); j++) {
                if(views[i][j] == null) continue;
                LayoutParams params = new LayoutParams();
                params.width = 800;
                params.height = 800;
                params.columnSpec = GridLayout.spec(i, 1f);
                params.rowSpec = GridLayout.spec(j, 1f);
                params.setGravity(Gravity.FILL);
                views[i][j].setLayoutParams(params);
                views[i][j].setPadding(10, 10, 10, 10);
                views[i][j].setOnLongClickListener(new LongClickListener());
                addView(views[i][j]);
            }
        }
    }

    private class LongClickListener implements OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            draggedView = v;
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
            if (onViewSwapListener == null) return;

            boolean isValid = onViewSwapListener.onViewSwapped(view1, view2);
            if (!isValid) return;

            //if(view1 == null || view2 == null) return;
            //if(view1.getTag().equals("placeholder") && view2.getTag().equals("placeholder")) return;
            //if(view1.getTag().equals("placeholder")) return;

            //LayoutParams params1 = (LayoutParams) view1.getLayoutParams();
            //LayoutParams params2 = (LayoutParams) view2.getLayoutParams();

            //view1.setLayoutParams(params2);
            //view2.setLayoutParams(params1);


        }
    }
}
