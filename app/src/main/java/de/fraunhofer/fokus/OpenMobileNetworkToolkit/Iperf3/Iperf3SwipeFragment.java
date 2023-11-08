package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.util.concurrent.ListenableFuture;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.units.qual.A;

public class Iperf3SwipeFragment extends Fragment {
    private static final String TAG = "Iperf3SwipeFragment";
    Iperf3FragmentStateAdapter iperf3FragmentStateAdapter;
    ViewPager2 viewPager;
    private Switch aSwitch;
    private Iperf3ResultsDataBase db;

    private WorkManager iperf3WM;
    Handler iperf3Looper;

    private Iperf3RunResultDao iperf3RunResultDao;
    private ArrayList<Iperf3Fragment.Iperf3Input> iperf3Inputs;
    private SharedPreferences sp;
    private String runID;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        this.db = Iperf3ResultsDataBase.getDatabase(getActivity().getApplicationContext());
        this.iperf3RunResultDao = db.iperf3RunResultDao();

        return inflater.inflate(R.layout.fragment_iperf3_swipe, container, false);
    }


    private String getProtocol(int protocolIdx){
        if(protocolIdx == 0) return "TCP";
        return "UDP";
    }

    private final Runnable iperf3 = new Runnable() {
        @Override
        public void run() {
            ArrayList<OneTimeWorkRequest> wr = new ArrayList<>();
            ArrayList<OneTimeWorkRequest> parses = new ArrayList<>();

            for (Iperf3Fragment.Iperf3Input iperf3Input: iperf3FragmentStateAdapter.getIperf3Inputs()){
                Data.Builder iperf3Data = new Data.Builder();
                iperf3Data.putStringArray("commands", iperf3Input.iperf3Command.split(" "));
                iperf3Data.putString("iperf3WorkerID", iperf3Input.uuid);
                iperf3Data.putString("logfilepath", iperf3Input.iperf3LogFilePath);
                iperf3Data.putString("measurementName", iperf3Input.measurementName);
                iperf3Data.putString("ip", iperf3Input.iperf3IP);
                iperf3Data.putString("port", iperf3Input.iperf3Port);
                iperf3Data.putString("bandwidth", iperf3Input.iperf3Bandwidth);
                iperf3Data.putString("duration", iperf3Input.iperf3Duration);
                iperf3Data.putString("interval", iperf3Input.iperf3Interval);
                iperf3Data.putString("bytes", iperf3Input.iperf3Bytes);
                iperf3Data.putBoolean("rev", iperf3Input.iperf3Reverse);
                iperf3Data.putBoolean("biDir", iperf3Input.iperf3BiDir);
                iperf3Data.putBoolean("oneOff", iperf3Input.iperf3OneOff);
                iperf3Data.putString("client", "client");
                iperf3Data.putString("timestamp", iperf3Input.timestamp.toString());
                iperf3Data.putString("protocol", getProtocol(iperf3Input.iperf3IdxProtocol));
                OneTimeWorkRequest workRequest =
                    new OneTimeWorkRequest.Builder(Iperf3Worker.class)
                        .setInputData(iperf3Data.build())
                        .addTag("Iperf3")
                        .addTag("Run IDX "+iperf3Inputs.indexOf(iperf3Input))
                        .addTag(iperf3Input.uuid).build();

                iperf3Data.putString("iperf3runID", runID);
                OneTimeWorkRequest parse =
                    new OneTimeWorkRequest.Builder(Iperf3ToLineProtocolWorker.class)
                        .setInputData(iperf3Data.build())
                        .addTag("Iperf3Parse")
                        .addTag("Run IDX "+iperf3Inputs.indexOf(iperf3Input))
                        .addTag(iperf3Input.uuid).build();
                wr.add(workRequest);
                parses.add(parse);
            }


            if(wr.size() < 2){
                Toast.makeText(requireContext(), "Only "+wr.size()+" iPerf specified, swipe!",Toast.LENGTH_SHORT).show();
                stopIperf3();
                return;
            }
            iperf3WM.getWorkInfoByIdLiveData(wr.get(1).getId()).observeForever(workInfo -> {
                WorkInfo.State state = workInfo.getState();

                if(state.equals(WorkInfo.State.FAILED)
                    || state.equals(WorkInfo.State.SUCCEEDED)){
                    iperf3Looper.post(iperf3);

                }
            });




            WorkContinuation workContinuation = iperf3WM.beginWith(wr.get(0));
            workContinuation.then(wr.get(1)).then(parses.get(1)).enqueue();
            workContinuation.then(parses.get(0)).enqueue();
        }
    };



    public void setUpIperf3(){
        iperf3Inputs = iperf3FragmentStateAdapter.getIperf3Inputs();
        iperf3Looper = new Handler(Objects.requireNonNull(Looper.myLooper()));
        iperf3Looper.post(iperf3);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        runID = formatter.format(now);

    }

    public void stopIperf3(){
        if(iperf3Looper == null) return;
        iperf3Looper.removeCallbacks(iperf3);
        iperf3WM.cancelAllWorkByTag("Iperf3");
        aSwitch.setChecked(false);
        sp.edit().putBoolean("iperf3_slider", false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        iperf3FragmentStateAdapter = new Iperf3FragmentStateAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(iperf3FragmentStateAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> tab.setText("iPerf3 Test " + (position + 1))
        ).attach();
        iperf3WM = WorkManager.getInstance(requireContext());
        iperf3WM.cancelAllWorkByTag("Iperf3");
        aSwitch = view.findViewById(R.id.iperf3_switch);
        aSwitch.setChecked(sp.getBoolean("iperf3_slider", false));
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) setUpIperf3();
                else stopIperf3();
                sp.edit().putBoolean("iperf3_slider", b).apply();
            }
        });

    }
}

