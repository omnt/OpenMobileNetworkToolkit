
//from https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.OperationMonitor;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SwipeController;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SwipeControllerActions;


public class Iperf3ListFragment extends Fragment {
    private final String TAG = "Iperf3ListFragment";
    private SwipeController swipeController = null;
    private RecyclerView recyclerView;
    private Iperf3RecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private FloatingActionButton uploadBtn;
    private SelectionTracker selectionTracker;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateIperf3ListAdapter(){
        if(this.adapter != null)
            this.adapter.notifyDataSetChanged();
    }
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_iperf3_list, parent, false);
        ArrayList<String> uids = this.getArguments().getStringArrayList("iperf3List");
        recyclerView = v.findViewById(R.id.runners_list);
        uploadBtn = v.findViewById(R.id.iperf3_upload_button);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Iperf3RecyclerViewAdapter(getActivity(), uids, uploadBtn);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
