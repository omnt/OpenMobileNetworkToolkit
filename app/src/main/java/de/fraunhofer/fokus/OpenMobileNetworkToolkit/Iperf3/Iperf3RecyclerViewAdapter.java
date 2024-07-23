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
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Output.Iperf3LogFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3UploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3RecyclerViewAdapter
    extends RecyclerView.Adapter<Iperf3RecyclerViewAdapter.ViewHolder> {
    private final String TAG = "Iperf3RecyclerViewAdapter";
    private final Iperf3ResultsDataBase db;
    private final ArrayList<String> uids;
    private Context context;
    private final FragmentActivity c;
    private final HashMap<String, Integer> selectedRuns;
    private final HashMap<CardView, Boolean> selectedCardViews;
    private final FloatingActionButton uploadBtn;

    public Iperf3RecyclerViewAdapter(FragmentActivity c, ArrayList<String> uids,
                                     FloatingActionButton uploadBtn) {
        this.c = c;
        this.uids = uids;
        this.db = Iperf3ResultsDataBase.getDatabase(context);
        this.selectedRuns = new HashMap<>();
        this.selectedCardViews = new HashMap<>();
        this.uploadBtn = uploadBtn;
        this.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = SharedPreferencesGrouper.getInstance(context).getSharedPreference(SPType.logging_sp);
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

                    Data.Builder data = new Data.Builder();
                    data.putString("iperf3LineProtocolFile", runResult.input.iperf3LineProtocolFile);
                    OneTimeWorkRequest iperf3UP =
                        new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class)
                            .setInputData(data.build())
                            .addTag("iperf3_upload")
                            .build();

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

                }
                uploadBtn.setVisibility(View.INVISIBLE);
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
        v.setFocusable(false);
        v.setClickable(false);
        ViewHolder viewHolder = new ViewHolder(v);
        selectedCardViews.put(v, false);
        return viewHolder;
    }

    public boolean isNightMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.iPerf3Parameters.removeAllViews();
        Iperf3RunResult test = getItemByPosition(position);
        holder.itemView.setTag(uids.get(position));
        if (selectedRuns.containsKey(test.uid)) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.forestgreen));
        } else {
            if(isNightMode(context))
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.cardview_dark_background));
            else
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.ic_launcher_background));

        }
        holder.measurement.setText("iPerf3");
        holder.timestamp.setText(test.input.timestamp.toString());

        holder.runIcon.setImageDrawable(Iperf3Utils.getDrawableResult(context, test.result));
        holder.uploadIcon.setImageDrawable(Iperf3Utils.getDrawableUpload(context, test.result, test.uploaded));
        holder.iPerf3Parameters = test.input.getInputAsLinearLayoutValue(holder.iPerf3Parameters, context);
    }
    private Iperf3RunResult getItemByPosition(int position) {
        return this.db.iperf3RunResultDao().getRunResult(this.uids.get(position));
    }

    @Override
    public int getItemCount() {
        return uids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView measurement;
        public TextView timestamp;
        public TextView iperf3State;
        public ImageView runIcon;
        public ImageView uploadIcon;
        private final LinearLayout linearLayout;
        private LinearLayout iPerf3Parameters;

        private LinearLayout firstRow(LinearLayout ll){
            measurement.setLayoutParams(
                Iperf3Utils.getLayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 8F));


            runIcon.setLayoutParams(
                Iperf3Utils.getLayoutParams(0, 80, 1F));

            uploadIcon.setLayoutParams(
                Iperf3Utils.getLayoutParams(0, 80, 1F));


            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.addView(measurement);
            ll.addView(runIcon);
            ll.addView(uploadIcon);
            return ll;
        }
        private LinearLayout secondRow(LinearLayout ll){
            ll.setOrientation(LinearLayout.HORIZONTAL);
            timestamp.setLayoutParams(
                Iperf3Utils.getLayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1F));
            ll.addView(timestamp);
            return ll;
        }
        private LinearLayout thirdRow(LinearLayout ll){
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.addView(iPerf3Parameters);
            iPerf3Parameters.setLayoutParams(
                Iperf3Utils.getLayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1F));
            return ll;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder: " + itemView);
            measurement = new TextView(context);
            timestamp = new TextView(context);
            iperf3State = new TextView(context);
            runIcon = new ImageView(context);
            runIcon.setPadding(0, 10, 0, 0);
            uploadIcon = new ImageView(context);
            uploadIcon.setPadding(0, 10, 0, 0);
            timestamp = new TextView(context);
            iPerf3Parameters = new LinearLayout(context);
            linearLayout = itemView.findViewById(R.id.iperf3_main_layout);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(firstRow(new LinearLayout(context)));
            linearLayout.addView(secondRow(new LinearLayout(context)));
            linearLayout.addView(thirdRow(new LinearLayout(context)));
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
                        if (selectedRuns.size() == 1 && selectedRuns.containsKey(uid)) {
                            uploadBtn.setVisibility(View.INVISIBLE);
                        }
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

    }
}
