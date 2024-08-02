package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Pair;
import android.view.View;

public class ViewsMapManager implements DraggableGridLayout.OnViewSwapListener {
    private Context context;
    private DraggableGridLayout draggableGridLayout;
    private ViewsManager viewsManager;

    public ViewsMapManager(Context context) {
        this.context = context;
        this.draggableGridLayout = new DraggableGridLayout(context);
        this.draggableGridLayout.setOnViewSwapListener(this);
        this.viewsManager = new ViewsManager(context);
    }

    public DraggableGridLayout getDraggableGridLayout() {
        return draggableGridLayout;
    }

    public View[][] getViewsMap() {
        return viewsManager.getViewsMap();
    }

    public void addNewView(View view) {
        viewsManager.addNewView(view);
    }

    public void update() {
        viewsManager.filterAndRemovePlaceholderColumns();
        viewsManager.addPlaceholderBorder();
        this.draggableGridLayout.setViews(viewsManager);
    }

    private Pair<Integer, Integer> findViewPos(View view) {
        return viewsManager.findViewPos(view);
    }

    @Override
    public boolean onViewSwapped(View view1, View view2) {
        Pair<Integer, Integer> pos1 = findViewPos(view1);
        Pair<Integer, Integer> pos2 = findViewPos(view2);
        if (pos1 == null || pos2 == null) {
            return false;
        }
        View[][] views = viewsManager.getViewsMap();
        views[pos1.first][pos1.second] = view2;
        views[pos2.first][pos2.second] = view1;
        update();
        return true;
    }
}
