package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Pair;
import android.view.View;

import androidx.cardview.widget.CardView;

import org.checkerframework.checker.units.qual.A;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Inflater;


public class ViewsMapManager implements DraggableGridLayout.OnViewSwapListener{
    private Context context;
    private DraggableGridLayout draggableGridLayout;
    private HashMap<Integer, ArrayList<View>> viewsMap;
    private HashMap<View, Pair<Integer, Integer>> viewPositionMap;
    private View[][] views;
    public ViewsMapManager(Context context) {
        this.context = context;
        this.draggableGridLayout = new DraggableGridLayout(context);
        this.draggableGridLayout.setOnViewSwapListener(this);
        this.viewsMap = new HashMap<>();
        this.viewPositionMap = new HashMap<>();

    }
    public DraggableGridLayout getDraggableGridLayout() {
        return draggableGridLayout;
    }
    public HashMap<Integer, ArrayList<View>> getViewsMap() {
        return viewsMap;
    }

    public void addNewView(View view) {
        // Add new view to viewsMap and draggableGridLayout
        int nColumns = viewsMap.size();
        ArrayList<View> views = new ArrayList<>();
        views.add(view);
        viewsMap.put(nColumns, views);
        viewPositionMap.put(view, new Pair<>(nColumns, 0));
    }

    private CardView createCardView() {
        CardView cardView = new CardView(context);
        cardView.setRadius(10);
        cardView.setCardElevation(10);
        cardView.setContentPadding(10, 10, 10, 10);
        cardView.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT // Change to match parent height
        ));
        cardView.setBackgroundResource(R.drawable.dashed_outline);
        cardView.setTag("placeholder");
        return cardView;
    }

    public void update() {
        collapse();
        for(HashMap.Entry<Integer, ArrayList<View>> entry : viewsMap.entrySet()) {
            Integer column = entry.getKey();
            ArrayList<View> views = entry.getValue();
            if (views == null) continue;
            CardView cardView = createCardView();
            views.add(views.size(), cardView);
            viewPositionMap.put(cardView, new Pair<>(column, views.size() - 1));
        }
        draggableGridLayout.setViews(viewsMap);
    }
    public void collapse() {
        for(HashMap.Entry<Integer, ArrayList<View>> entry : viewsMap.entrySet()) {
            Integer column = entry.getKey();
            ArrayList<View> views = entry.getValue();
            if (views == null) continue;
            views.removeIf(view -> view.getTag().equals("placeholder"));
        }
        draggableGridLayout.setViews(viewsMap);
    }
    public void addView(View view) {
        // Add view to viewsMap and draggableGridLayout
    }

    public void deleteView(View view) {
        // Remove view from viewsMap and draggableGridLayout
    }

    @Override
    public boolean onViewSwapped(View view1, View view2) {
        swapViews(view1, view2);
        removePlaceholders();
        addPlaceholderBorders();
        addNewPlaceholderColumn();

        draggableGridLayout.setViews(viewsMap);
        return true;
    }

    private void swapViews(View view1, View view2) {
        Pair<Integer, Integer> view1Pos = viewPositionMap.get(view1);
        Pair<Integer, Integer> view2Pos = viewPositionMap.get(view2);

        viewPositionMap.put(view1, view2Pos);
        viewPositionMap.put(view2, view1Pos);

        viewsMap.get(view1Pos.first).set(view1Pos.second, view2);
        viewsMap.get(view2Pos.first).set(view2Pos.second, view1);
    }

    private void removePlaceholders() {
        viewPositionMap.keySet().removeIf(view -> "placeholder".equals(view.getTag()));
        for (Iterator<Map.Entry<Integer, ArrayList<View>>> it = viewsMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, ArrayList<View>> entry = it.next();
            Integer column = entry.getKey();
            ArrayList<View> views = entry.getValue();

            if (views == null || views.isEmpty()) {
                it.remove();
                continue;
            }

            views.removeIf(view -> "placeholder".equals(view.getTag()));

            if (views.isEmpty()) {
                it.remove();
            }
        }
    }

    private void addPlaceholderBorders() {
        for (Map.Entry<Integer, ArrayList<View>> entry : viewsMap.entrySet()) {
            Integer column = entry.getKey();
            ArrayList<View> views = entry.getValue();

            if (views != null && !views.isEmpty()) {
                CardView cardView = createCardView();
                views.add(views.size(), cardView);
                viewPositionMap.put(cardView, new Pair<>(column, views.size() - 1));
            }
        }
    }

    private void addNewPlaceholderColumn() {
        ArrayList<View> lastColumn = new ArrayList<>();
        CardView cardView = createCardView();
        lastColumn.add(cardView);
        viewsMap.put(viewsMap.size(), lastColumn);
        viewPositionMap.put(cardView, new Pair<>(viewsMap.size() - 1, 0));
    }

}