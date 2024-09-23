/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

//from https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Output;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3RecyclerViewAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Utils;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
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
    private Iperf3ResultsDataBase db;
    private Context context;
    public static Iperf3ListFragment newInstance() {
        Iperf3ListFragment fragment = new Iperf3ListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateIperf3ListAdapter() {
        if (this.adapter != null) this.adapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_iperf3_list, parent, false);
        this.context = requireContext();
        recyclerView = v.findViewById(R.id.runners_list);
        uploadBtn = v.findViewById(R.id.iperf3_upload_button);
        db = Iperf3ResultsDataBase.getDatabase(requireContext());

        linearLayoutManager =
            new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        this.adapter = new Iperf3RecyclerViewAdapter(getActivity(),
                new ArrayList<String>(db.iperf3RunResultDao().getIDs()),
                uploadBtn);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


        swipeController = new SwipeController(new SwipeControllerActions() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRightClicked(int position) {
                WorkManager iperf3WM = WorkManager.getInstance(getContext());
                String uid = new ArrayList<String>(db.iperf3RunResultDao().getIDs()).get(position);
                iperf3WM.cancelAllWorkByTag(uid);
                Iperf3RunResultDao iperf3RunResultDao = Iperf3ResultsDataBase.getDatabase(requireContext()).iperf3RunResultDao();
                Iperf3RunResult runResult = iperf3RunResultDao.getRunResult(uid);

                Data.Builder iperf3Data = new Data.Builder();

                iperf3Data.putString("rawIperf3file", runResult.input.getRawFile());
                iperf3Data.putString("iperf3LineProtocolFile", runResult.input.getLineProtocolFile());
                iperf3Data.putString("measurementName", runResult.input.getMeasurementName());
                iperf3Data.putString("ip", runResult.input.getIp());
                iperf3Data.putString("port", runResult.input.getPort());
                iperf3Data.putString("bandwidth", runResult.input.getBandwidth());
                iperf3Data.putString("duration", runResult.input.getDuration());
                iperf3Data.putString("interval", runResult.input.getInterval());
                iperf3Data.putString("bytes", runResult.input.getBytes());
                iperf3Data.putString("protocol", runResult.input.getProtocol().toString());
                iperf3Data.putString("direction", runResult.input.getDirection().toString());
                iperf3Data.putBoolean("oneOff", runResult.input.isOneOff());
                iperf3Data.putString("mode", runResult.input.getMode().toString());
                iperf3Data.putString("timestamp", runResult.input.getTimestamp().toString());

                OneTimeWorkRequest iperf3LP =
                        new OneTimeWorkRequest
                                .Builder(Iperf3ToLineProtocolWorker.class)
                                .setInputData(iperf3Data.build())
                                .build();
                iperf3WM.enqueue(iperf3LP);


                if(runResult.result != 0)
                    iperf3RunResultDao.updateResult(uid, 1);
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
                input.putString("uid", new ArrayList<String>(db.iperf3RunResultDao().getIDs()).get(position));
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
