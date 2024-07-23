/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

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
    private Iperf3ResultsDataBase db;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateIperf3ListAdapter() {
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_iperf3_list, parent, false);
        ArrayList<String> uids = this.getArguments().getStringArrayList("iperf3List");
        recyclerView = v.findViewById(R.id.runners_list);
        uploadBtn = v.findViewById(R.id.iperf3_upload_button);
        linearLayoutManager =
            new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Iperf3RecyclerViewAdapter(getActivity(), uids, uploadBtn);
        recyclerView.setAdapter(adapter);
        db = Iperf3ResultsDataBase.getDatabase(requireContext());


        swipeController = new SwipeController(new SwipeControllerActions() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRightClicked(int position) {
                WorkManager iperf3WM = WorkManager.getInstance(getContext());
                String uid = uids.get(position);
                iperf3WM.cancelAllWorkByTag(uid);
                Iperf3RunResultDao iperf3RunResultDao = Iperf3ResultsDataBase.getDatabase(requireContext()).iperf3RunResultDao();
                Iperf3RunResult runResult = iperf3RunResultDao.getRunResult(uid);


                Data.Builder iperf3Data = new Data.Builder();

                iperf3Data.putString("rawIperf3file", runResult.input.iperf3rawIperf3file);
                iperf3Data.putString("iperf3LineProtocolFile", runResult.input.iperf3LineProtocolFile);
                iperf3Data.putString("measurementName", runResult.input.measurementName);
                iperf3Data.putString("ip", runResult.input.iperf3IP);
                iperf3Data.putString("port", runResult.input.iperf3Port);
                iperf3Data.putString("bandwidth", runResult.input.iperf3Bandwidth);
                iperf3Data.putString("duration", runResult.input.iperf3Duration);
                iperf3Data.putString("interval", runResult.input.iperf3Interval);
                iperf3Data.putString("bytes", runResult.input.iperf3Bytes);
                iperf3Data.putString("protocol", Iperf3Utils.getProtocolString(runResult.input.iperf3IdxProtocol));
                iperf3Data.putBoolean("rev", runResult.input.iperf3Reverse);
                iperf3Data.putBoolean("biDir", runResult.input.iperf3BiDir);
                iperf3Data.putBoolean("oneOff", runResult.input.iperf3OneOff);
                iperf3Data.putString("client", Iperf3Utils.getModeString(runResult.input.iperf3IdxMode));
                iperf3Data.putString("timestamp", runResult.input.timestamp.toString());

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
