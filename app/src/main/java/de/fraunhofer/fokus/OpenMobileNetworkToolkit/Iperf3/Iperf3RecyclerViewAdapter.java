/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3RecyclerViewAdapter
    extends RecyclerView.Adapter<Iperf3RecyclerViewAdapter.ViewHolder> {
    private final String TAG = "Iperf3RecyclerViewAdapter";
    private final Iperf3ResultsDataBase db;
    private ArrayList<String> uids;
    private Context context;
    private final FragmentActivity c;
    private final HashMap<String, Integer> selectedRuns;
    private final HashMap<CardView, Boolean> selectedCardViews;
    private final boolean isEnabled;
    private final FloatingActionButton uploadBtn;
    private Iperf3RunResultDao iperf3RunResultDao;

    private final boolean hasSelection;

    public Iperf3RecyclerViewAdapter(FragmentActivity c, ArrayList<String> uids,
                                     FloatingActionButton uploadBtn) {
        this.c = c;
        this.uids = uids;
        this.db = Iperf3ResultsDataBase.getDatabase(context);
        this.selectedRuns = new HashMap<>();
        this.selectedCardViews = new HashMap<>();
        this.isEnabled = false;
        this.uploadBtn = uploadBtn;
        this.hasSelection = false;
        this.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
                Iperf3ResultsDataBase db = Iperf3ResultsDataBase.getDatabase(context);
                Iperf3RunResultDao iperf3RunResultDao = db.iperf3RunResultDao();
                if (preferences.getBoolean("enable_influx", false)) {
                } else {
                    Toast.makeText(context, "Influx Disabled!", Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayList<OneTimeWorkRequest> uploads = new ArrayList<>();
                WorkManager iperf3WM = WorkManager.getInstance(context);
                HashMap<String, Integer> cpySelectedRuns =
                    (HashMap<String, Integer>) selectedRuns.clone();
                for (Map.Entry<String, Integer> stringIntegerEntry : cpySelectedRuns.entrySet()) {
                    String uid = stringIntegerEntry.getKey();
                    Iperf3RunResult runResult = iperf3RunResultDao.getRunResult(uid);

                    String[] protocol =
                        context.getResources().getStringArray(R.array.iperf_protocol);
                    String[] mode = context.getResources().getStringArray(R.array.iperf_mode);


                    Data.Builder iperf3Data = new Data.Builder();
                    iperf3Data.putString("iperf3WorkerID", uid);
                    iperf3Data.putString("logfilepath", runResult.input.iperf3LogFilePath);
                    iperf3Data.putString("measurementName", runResult.input.measurementName);
                    iperf3Data.putString("ip", runResult.input.iperf3IP);
                    iperf3Data.putString("port", runResult.input.iperf3Port);
                    iperf3Data.putString("bandwidth", runResult.input.iperf3Bandwidth);
                    iperf3Data.putString("duration", runResult.input.iperf3Duration);
                    iperf3Data.putString("interval", runResult.input.iperf3Interval);
                    iperf3Data.putString("bytes", runResult.input.iperf3Bytes);


                    iperf3Data.putString("protocol", protocol[runResult.input.iperf3IdxProtocol]);
                    iperf3Data.putBoolean("rev", runResult.input.iperf3Reverse);
                    iperf3Data.putBoolean("biDir", runResult.input.iperf3BiDir);
                    iperf3Data.putBoolean("oneOff", runResult.input.iperf3OneOff);
                    iperf3Data.putString("client", mode[runResult.input.iperf3IdxMode]);
                    iperf3Data.putString("timestamp", runResult.input.timestamp.toString());

                    //todo && runResult.input.iperf3Json

                    OneTimeWorkRequest iperf3UP =
                        new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class).setInputData(
                            iperf3Data.build()).addTag("iperf3_upload").build();
                    uploads.add(iperf3UP);
                    iperf3WM.getWorkInfoByIdLiveData(iperf3UP.getId()).observeForever(workInfo -> {
                        boolean iperf3_upload;
                        iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
                        if (iperf3_upload) {
                            iperf3RunResultDao.updateUpload(uid, iperf3_upload);
                            selectedRuns.remove(stringIntegerEntry.getKey());
                            notifyItemChanged(stringIntegerEntry.getValue());
                        }

                    });
                    uploadBtn.setVisibility(View.INVISIBLE);
                }

                iperf3WM.beginWith(uploads).enqueue();

            }
        });

    }


    @NonNull
    @Override
    public Iperf3RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                   int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        CardView v = (CardView) inflater.inflate(R.layout.fragment_iperf3_row_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        selectedCardViews.put(v, false);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Iperf3RunResult test = getItemByPosition(position);


        TextView command = holder.command;
        TextView uploaded = holder.uploaded;
        TextView timestamp = holder.timestamp;
        TextView iperf3State = holder.iperf3State;
        ImageView icon = holder.icon;

        holder.itemView.setTag(uids.get(position));

        if (selectedRuns.containsKey(test.uid)) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.forestgreen));
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.cardview_dark_background));
        }


        command.setText(test.input.measurementName);
        iperf3State.setText("" + test.result);
        if (test.result == -100) {
            iperf3State.setText("RUN");
        }
        timestamp.setText(test.input.timestamp.toString());
        uploaded.setText("Uploaded: " + test.uploaded);

        Drawable drawable = Iperf3Utils.getDrawable(context, test.result);
        icon.setImageDrawable(drawable);
        this.iperf3RunResultDao = db.iperf3RunResultDao();
    }

    public int getPositionFromUid(String uid) {
        return uids.indexOf(uid);
    }

    public void setUids(ArrayList<String> uids) {
        this.uids = uids;
    }

    private Iperf3RunResult getItemByPosition(int position) {

        return this.db.iperf3RunResultDao().getRunResult(this.uids.get(position));
    }

    private Iperf3RunResult getItemByUID(String uid) {
        return this.db.iperf3RunResultDao().getRunResult(uid);
    }


    @Override
    public int getItemCount() {
        return uids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView command = itemView.findViewById(R.id.firstLine);
        public TextView uploaded = itemView.findViewById(R.id.secondLine);
        public TextView timestamp = itemView.findViewById(R.id.thirdLine);
        public TextView iperf3State = itemView.findViewById(R.id.iperf3State);
        public ImageView icon = itemView.findViewById(R.id.iperf3RunningIndicator);
        private String uid;
        private int position;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            Log.d(TAG, "ViewHolder: " + itemView);
            command = itemView.findViewById(R.id.firstLine);
            uploaded = itemView.findViewById(R.id.secondLine);
            timestamp = itemView.findViewById(R.id.thirdLine);
            iperf3State = itemView.findViewById(R.id.iperf3State);
            icon = itemView.findViewById(R.id.iperf3RunningIndicator);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int viewPosition = getLayoutPosition();
                    Log.d(TAG, "onLongClick: " + v.toString());
                    Log.d(TAG, "onLongClick: " + viewPosition);
                    if (!selectedRuns.isEmpty()) {
                        return true;
                    }
                    if (viewPosition == RecyclerView.NO_POSITION) {
                        return false;
                    }
                    String uid = uids.get(viewPosition);
                    selectedRuns.put(uid, viewPosition);
                    notifyItemChanged(viewPosition);
                    uploadBtn.setVisibility(View.VISIBLE);
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int viewPosition = getLayoutPosition();
                    String uid = uids.get(viewPosition);
                    if (!selectedRuns.isEmpty() && selectedRuns.containsKey(uid)) {
                        selectedRuns.remove(uid);
                        notifyItemChanged(viewPosition);
                        return;
                    } else if (!selectedRuns.isEmpty()) {
                        selectedRuns.put(uid, viewPosition);
                        notifyItemChanged(viewPosition);
                        uploadBtn.setVisibility(View.VISIBLE);
                        return;
                    }
                    Log.d(TAG, "onCreateView: CLICKED!");
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    Iperf3LogFragment test = new Iperf3LogFragment();
                    test.setArguments(bundle);
                    c.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, test, "iperf3LogFragment")
                        .addToBackStack("findThisFragment").commit();

                }
            });

        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }
}
