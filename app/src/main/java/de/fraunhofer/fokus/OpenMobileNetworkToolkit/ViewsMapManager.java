package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Pair;
import android.view.View;

import androidx.cardview.widget.CardView;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;

public class ViewsMapManager implements DraggableGridLayout.OnViewSwapListener {
    private Context context;
    private DraggableGridLayout draggableGridLayout;
    private ViewsManager viewsManager;
    private Iperf3Input[][] inputMap;
    public ViewsMapManager(Context context) {
        this.context = context;
        this.draggableGridLayout = new DraggableGridLayout(context);
        this.draggableGridLayout.setOnViewSwapListener(this);
        this.viewsManager = new ViewsManager(context);
        this.inputMap = new Iperf3Input[10][10];
    }

    public ViewsManager getViewsManager() {
        return viewsManager;
    }

    public DraggableGridLayout getDraggableGridLayout() {
        return draggableGridLayout;
    }

    public View[][] getViewsMap() {
        return viewsManager.getViewsMap();
    }

    public void addNewView(Iperf3Input input, View v) {
        viewsManager.addNewView(v);
        Pair<Integer, Integer> pos = viewsManager.findViewPos(v);
        if (pos != null) {
            inputMap[pos.first][pos.second] = input;
        }
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
        Iperf3Input temp = inputMap[pos1.first][pos1.second];
        inputMap[pos1.first][pos1.second] = inputMap[pos2.first][pos2.second];
        inputMap[pos2.first][pos2.second] = temp;
        update();
        return true;
    }
}
