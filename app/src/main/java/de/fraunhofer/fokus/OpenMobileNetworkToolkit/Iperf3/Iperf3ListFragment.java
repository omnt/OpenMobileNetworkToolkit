
//from https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SwipeController;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SwipeControllerActions;


public class Iperf3ListFragment extends Fragment {
    Iperf3RecyclerViewAdapter iperf3ListAdapter;
    private Iperf3ResultsDataBase db;
    private final String TAG = "Iperf3ListFragment";
    private SwipeController swipeController = null;
    private RecyclerView recyclerView;
    private Iperf3RecyclerViewAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iperf3ListAdapter = new Iperf3RecyclerViewAdapter(getActivity(),
                this.getArguments().getStringArrayList("iperf3List"));

//        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_graph);
//        navController = navHostFragment.getNavController();

        this.db = Iperf3ResultsDataBase.getDatabase(getActivity().getApplicationContext());
    }

    public Iperf3RecyclerViewAdapter getIperf3ListAdapter(){
        return this.iperf3ListAdapter;
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_iperf3_list, parent, false);
        ArrayList<String> uids = this.getArguments().getStringArrayList("iperf3List");

        recyclerView = v.findViewById(R.id.runners_list);

        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new Iperf3RecyclerViewAdapter(getActivity(), uids);
        recyclerView.setAdapter(adapter);
        swipeController = new SwipeController(new SwipeControllerActions() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRightClicked(int position) {
                WorkManager iperf3WM = WorkManager.getInstance(getContext());
                iperf3WM.cancelAllWorkByTag(uids.get(position));

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                }, 100);
            }

            @Override
            public void onLeftClicked(int position) {
                Bundle input = new Bundle();
                input.putString("uid", uids.get(position));
                getActivity().getSupportFragmentManager().setFragmentResult("input", input);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });



        return v;
    }
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);


         }

}
