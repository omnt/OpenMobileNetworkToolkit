/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.system.ErrnoException;
import android.system.Os;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class Iperf3Fragment extends Fragment {
    private static final String TAG = "iperf3InputFragment";
    private final int SHOW_PROGRESSBAR = 3000;
    private final String IPERF3IP = "iperf3IP";
    private final String IPERF3PORT = "iperf3Port";
    private final String IPERF3BANDWIDTH = "iperf3Bandwidth";
    private final String IPERF3DURATION = "iperf3Duration";
    private final String IPERF3INTERVAL = "iperf3Interval";
    private final String IPERF3BYTES = "iperf3Bytes";
    private final String IPERF3STREAMS = "iperf3Streams";
    private final String IPERF3BIDIR = "iperf3BiDir";
    private final String IPERF3REVERSE = "iperf3Reverse";
    private final String IPERF3JSON = "iperf3Json";
    private final String IPERF3ONEOFF = "iperf3OneOff";
    private final String IPERF3IDXPROTOCOL = "iperf3IdxProtocol";
    private final String IPERF3IDXMODE = "iperf3IdxMode";
    private final String IPERF3CPORT = "iperf3cport";
    private CheckBox iperf3BiDir;
    private CheckBox iperf3Reverse;

    private CheckBox iperf3OneOff;
    private EditText iperf3EtIp;
    private EditText iperf3EtPort;
    private EditText iperf3EtBandwidth;
    private EditText iperf3EtDuration;
    private EditText iperf3EtInterval;
    private EditText iperf3EtBytes;
    private EditText iperf3EtStreams;
    private EditText iperf3Cport;
    private Button sendBtn;
    private Button instancesBtn;
    private Spinner protocolSpinner;
    private Spinner iperf3ModeSpinner;
    private Iperf3RunResultDao iperf3RunResultDao;
    private LinearProgressIndicator progressIndicator;
    private int[] failedColors;
    private int[] runningColors;
    private int[] succesColors;
    private LinkedList<EditText> editTexts;
    private String rawIperf3file;
    private String logFileDir;
    private String logFileName;
    private View v;
    private SharedPreferencesGrouper spg;
    private Iperf3Input input;
    private WorkManager iperf3WM;
    private Iperf3ResultsDataBase db;
    private ArrayList<String> uids;
    private Context ct;
    private final Runnable progressbarUpdate = new Runnable() {
        @Override
        public void run() {
            progressIndicator.setVisibility(LinearProgressIndicator.INVISIBLE);
            progressIndicator.setIndicatorColor(runningColors);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.input = new Iperf3Input();
        this.db = Iperf3ResultsDataBase.getDatabase(getActivity().getApplicationContext());
        this.uids = new ArrayList<>(this.db.iperf3RunResultDao().getIDs());
        this.iperf3WM = WorkManager.getInstance(getActivity().getApplicationContext());
        this.logFileDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .getAbsolutePath() + "/omnt/iperf3RawLogs/";
        this.iperf3RunResultDao = db.iperf3RunResultDao();
        File iperf3Path = new File(this.logFileDir);
        if (!iperf3Path.exists()) {
            iperf3Path.mkdir();
        }
        this.ct = requireContext();
        this.spg = SharedPreferencesGrouper.getInstance(this.ct);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        NavController navController = NavHostFragment.findNavController(this);
        MutableLiveData<String> liveData = navController.getCurrentBackStackEntry()
            .getSavedStateHandle()
            .getLiveData("uid");



        liveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Iperf3RunResult iperf3RunResult = db.iperf3RunResultDao().getRunResult(s);

                iperf3EtIp.setText(iperf3RunResult.input.iperf3IP);
                iperf3EtPort.setText(iperf3RunResult.input.iperf3Port);
                iperf3EtBandwidth.setText(iperf3RunResult.input.iperf3Bandwidth);
                iperf3EtDuration.setText(iperf3RunResult.input.iperf3Duration);
                iperf3EtInterval.setText(iperf3RunResult.input.iperf3Interval);
                iperf3EtBytes.setText(iperf3RunResult.input.iperf3Bytes);
                iperf3Cport.setText(iperf3RunResult.input.iperf3Cport);

                iperf3Reverse.setChecked(iperf3RunResult.input.iperf3Reverse);
                iperf3BiDir.setChecked(iperf3RunResult.input.iperf3BiDir);
                iperf3OneOff.setChecked(iperf3RunResult.input.iperf3OneOff);
                protocolSpinner.setSelection(iperf3RunResult.input.iperf3IdxProtocol);
                iperf3ModeSpinner.setSelection(iperf3RunResult.input.iperf3IdxMode);


            }
        });
    }


    private void saveTextInputToSharedPreferences(EditText field, String name) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                spg.getSharedPreference(SPType.iperf3_sp).edit().putString(name, field.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }



    private void saveCheckboxInputToSharedPreferences(CheckBox box, String name) {
        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spg.getSharedPreference(SPType.iperf3_sp).edit().putBoolean(name, box.isChecked()).apply();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);
        iperf3EtIp = v.findViewById(R.id.iperf3_ip);
        iperf3EtPort = v.findViewById(R.id.iperf3_port);
        iperf3EtBandwidth = v.findViewById(R.id.iperf3_bandwidth);
        iperf3EtDuration = v.findViewById(R.id.iperf3_duration);




        iperf3EtInterval = v.findViewById(R.id.iperf3_interval);
        iperf3EtBytes = v.findViewById(R.id.iperf3_bytes);
        iperf3EtStreams = v.findViewById(R.id.iperf3_streams);
        iperf3Cport = v.findViewById(R.id.iperf3_cport);
        progressIndicator = v.findViewById(R.id.iperf3_progress);

        iperf3EtDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iperf3EtBytes.setEnabled(s.length() <= 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        iperf3EtBytes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iperf3EtDuration.setEnabled(s.length() <= 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveTextInputToSharedPreferences(iperf3EtIp, IPERF3IP);
        saveTextInputToSharedPreferences(iperf3EtPort, IPERF3PORT);
        saveTextInputToSharedPreferences(iperf3EtBandwidth, IPERF3BANDWIDTH);
        saveTextInputToSharedPreferences(iperf3EtDuration, IPERF3DURATION);
        saveTextInputToSharedPreferences(iperf3EtInterval, IPERF3INTERVAL);
        saveTextInputToSharedPreferences(iperf3EtBytes, IPERF3BYTES);
        saveTextInputToSharedPreferences(iperf3EtStreams, IPERF3STREAMS);
        saveTextInputToSharedPreferences(iperf3Cport, IPERF3CPORT);

        failedColors = new int[] {getContext().getColor(R.color.crimson),
            getContext().getColor(R.color.crimson), getContext().getColor(R.color.crimson)};
        runningColors = new int[] {getContext().getColor(R.color.purple_500),
            getContext().getColor(R.color.crimson), getContext().getColor(R.color.forestgreen)};
        succesColors = new int[] {getContext().getColor(R.color.forestgreen),
            getContext().getColor(R.color.forestgreen), getContext().getColor(R.color.forestgreen)};

        progressIndicator.setIndicatorColor(runningColors);
        progressIndicator.setIndeterminateAnimationType(
            LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS);
        progressIndicator.setVisibility(LinearProgressIndicator.INVISIBLE);

        editTexts = new LinkedList<>();
        editTexts.add(iperf3EtIp);
        editTexts.add(iperf3EtPort);
        editTexts.add(iperf3EtBandwidth);
        editTexts.add(iperf3EtDuration);
        editTexts.add(iperf3EtInterval);
        editTexts.add(iperf3EtBytes);
        editTexts.add(iperf3EtStreams);
        editTexts.add(iperf3Cport);
        sendBtn = v.findViewById(R.id.iperf3_send);
        instancesBtn = v.findViewById(R.id.iperf3_instances_button);

        sendBtn.setOnClickListener(this::executeIperfCommand);
        instancesBtn.setOnClickListener(this::showInstances);

        iperf3BiDir = v.findViewById(R.id.iperf_bidir);
        iperf3Reverse = v.findViewById(R.id.iperf3_reverse);
        iperf3OneOff = v.findViewById(R.id.iperf3_one_off);

        saveCheckboxInputToSharedPreferences(iperf3BiDir, IPERF3BIDIR);
        saveCheckboxInputToSharedPreferences(iperf3Reverse, IPERF3REVERSE);
        saveCheckboxInputToSharedPreferences(iperf3OneOff, IPERF3ONEOFF);

        protocolSpinner = v.findViewById(R.id.iperf3_protocol_spinner);
        ArrayAdapter<CharSequence> adapter =
            ArrayAdapter.createFromResource(getContext(), R.array.iperf_protocol,
                R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(adapter);

        iperf3ModeSpinner = v.findViewById(R.id.iperf3_mode_spinner);
        ArrayAdapter<CharSequence> mode_adapter =
            ArrayAdapter.createFromResource(getContext(), R.array.iperf_mode,
                R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        iperf3ModeSpinner.setAdapter(mode_adapter);


        getActivity().getSupportFragmentManager()
            .setFragmentResultListener("input", getViewLifecycleOwner(),
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey,
                                                 @NonNull Bundle result) {

                        Iperf3RunResult iperf3RunResult =
                            db.iperf3RunResultDao().getRunResult(result.getString("uid"));
                        String logFileName = iperf3RunResult.input.iperf3LogFileName.split("_")[0];
                        if (logFileName.equals("iperf3")) {
                            logFileName = "";
                        }

                        iperf3EtIp.setText(iperf3RunResult.input.iperf3IP);
                        iperf3EtPort.setText(iperf3RunResult.input.iperf3Port);
                        iperf3EtBandwidth.setText(iperf3RunResult.input.iperf3Bandwidth);
                        iperf3EtDuration.setText(iperf3RunResult.input.iperf3Duration);
                        iperf3EtInterval.setText(iperf3RunResult.input.iperf3Interval);
                        iperf3EtBytes.setText(iperf3RunResult.input.iperf3Bytes);
                        iperf3EtStreams.setText(iperf3RunResult.input.streams);
                        iperf3Cport.setText(iperf3RunResult.input.iperf3Cport);

                        iperf3Reverse.setChecked(iperf3RunResult.input.iperf3Reverse);
                        iperf3BiDir.setChecked(iperf3RunResult.input.iperf3BiDir);
                        iperf3OneOff.setChecked(iperf3RunResult.input.iperf3OneOff);
                        protocolSpinner.setSelection(iperf3RunResult.input.iperf3IdxProtocol);
                        iperf3ModeSpinner.setSelection(iperf3RunResult.input.iperf3IdxMode);

                        writeToSP();
                    }
                });
        iperf3EtIp.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3IP, null));
        iperf3EtPort.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3PORT, null));
        iperf3EtBandwidth.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3BANDWIDTH, null));
        iperf3EtDuration.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3DURATION, null));
        iperf3EtInterval.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3INTERVAL, null));
        iperf3EtBytes.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3BYTES, null));
        iperf3EtStreams.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3STREAMS, null));
        iperf3Cport.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(IPERF3CPORT, null));

        iperf3BiDir.setChecked(spg.getSharedPreference(SPType.iperf3_sp).getBoolean(IPERF3BIDIR, false));
        iperf3Reverse.setChecked(spg.getSharedPreference(SPType.iperf3_sp).getBoolean(IPERF3REVERSE, false));
        iperf3OneOff.setChecked(spg.getSharedPreference(SPType.iperf3_sp).getBoolean(IPERF3ONEOFF, false));
        protocolSpinner.setSelection(spg.getSharedPreference(SPType.iperf3_sp).getInt(IPERF3IDXPROTOCOL, 0));
        iperf3ModeSpinner.setSelection(spg.getSharedPreference(SPType.iperf3_sp).getInt(IPERF3IDXMODE, 0));


        try {
            Os.setenv("TMPDIR", String.valueOf(getActivity().getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        return v;
    }

    public void showInstances(View view) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("iperf3List", uids);

        NavController navController;

        NavHostFragment navHostFragment =
            (NavHostFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        navController = navHostFragment.getNavController();

        navController.navigate(R.id.runners_list, bundle);
    }

    private boolean isModeSpinnerClient() {
        String status = iperf3ModeSpinner.getSelectedItem().toString();
        return status.equals("Client");
    }

    public void executeIperfCommand(View view) {
        String[] command = parseInput().split(" ");

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/iperf3LP/";

        if(input.iperf3Json){
            try {
                Files.createDirectories(Paths.get(path));
            } catch (IOException e) {
                Toast.makeText(requireContext(),"Could not create Dir files!", Toast.LENGTH_SHORT).show();
            }
        }

        // create the log file;

        String iperf3WorkerID = input.uuid;

        input.iperf3LineProtocolFile = path + iperf3WorkerID + ".txt";
        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", command);
        iperf3Data.putString("iperf3WorkerID", iperf3WorkerID);
        iperf3Data.putString("rawIperf3file", rawIperf3file);
        iperf3Data.putString("iperf3LineProtocolFile", input.iperf3LineProtocolFile);
        iperf3Data.putString("measurementName", input.measurementName);
        iperf3Data.putString("ip", input.iperf3IP);
        iperf3Data.putString("port", input.iperf3Port);
        iperf3Data.putString("bandwidth", input.iperf3Bandwidth);
        iperf3Data.putString("duration", input.iperf3Duration);
        iperf3Data.putString("interval", input.iperf3Interval);
        iperf3Data.putString("bytes", input.iperf3Bytes);
        iperf3Data.putString("protocol", protocolSpinner.getSelectedItem().toString());
        iperf3Data.putBoolean("rev", input.iperf3Reverse);
        iperf3Data.putBoolean("biDir", input.iperf3BiDir);
        iperf3Data.putBoolean("oneOff", input.iperf3OneOff);
        iperf3Data.putString("client", iperf3ModeSpinner.getSelectedItem().toString());
        iperf3Data.putString("timestamp", input.timestamp.toString());
        iperf3Data.putString("protocol", protocolSpinner.getSelectedItem().toString());
        iperf3Data.putString("cport", input.iperf3Cport);

        ListenableFuture<List<WorkInfo>> status = iperf3WM.getWorkInfosByTag("iperf3Run");

/*        try {
            for (WorkInfo workInfo : status.get()) {
                if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                    Toast.makeText(getContext(), "iperf3 Test is running!", Toast.LENGTH_SHORT)
                        .show();
                    return;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }*/

        uids.add(0, iperf3WorkerID);
        iperf3Data.putInt("notificationID", uids.size());


        OneTimeWorkRequest iperf3WR =
            new OneTimeWorkRequest
                .Builder(Iperf3Worker.class)
                .setInputData(iperf3Data.build())
                .addTag("iperf3Run")
                .addTag(iperf3WorkerID)
                .build();
        OneTimeWorkRequest iperf3LP =
            new OneTimeWorkRequest
                .Builder(Iperf3ToLineProtocolWorker.class)
                .setInputData(iperf3Data.build())
                .build();
        OneTimeWorkRequest iperf3UP =
            new OneTimeWorkRequest
                .Builder(Iperf3UploadWorker.class)
                .setInputData(iperf3Data.build())
                .addTag("iperf3")
                .build();

        iperf3RunResultDao.insert(
            new Iperf3RunResult(iperf3WorkerID, -100, false, input, input.timestamp));



        if (spg.getSharedPreference(SPType.iperf3_sp).getBoolean("enable_influx", false) && input.iperf3Json) {
            iperf3WM.beginWith(iperf3WR).then(iperf3LP).then(iperf3UP).enqueue();
        } else if(input.iperf3Json) {
            iperf3WM.beginWith(iperf3WR).then(iperf3LP).enqueue();
        } else {
            iperf3WM.beginWith(iperf3WR).enqueue();
        }



        Handler progressbarHandler = new Handler(Looper.myLooper());

        iperf3WM.getWorkInfoByIdLiveData(iperf3WR.getId()).observeForever(workInfo -> {
            int iperf3_result;
            iperf3_result = workInfo.getOutputData().getInt("iperf3_result", -100);
            if (workInfo.getState().equals(WorkInfo.State.CANCELLED)) {
                iperf3_result = -1;
            }
            iperf3RunResultDao.updateResult(iperf3WorkerID, iperf3_result);
            Log.d(TAG, "onChanged: iperf3_result: " + iperf3_result);
            if (iperf3_result == -100) {
                progressIndicator.setVisibility(LinearProgressIndicator.VISIBLE);
                if (!isModeSpinnerClient()) {
                    progressbarHandler.postDelayed(progressbarUpdate, SHOW_PROGRESSBAR);
                }
            } else if (iperf3_result != 0) {
                progressIndicator.setIndicatorColor(failedColors);
                progressbarHandler.postDelayed(progressbarUpdate, SHOW_PROGRESSBAR);
            } else {
                progressIndicator.setIndicatorColor(succesColors);
                progressbarHandler.postDelayed(progressbarUpdate, SHOW_PROGRESSBAR);
            }
        });
        iperf3WM.getWorkInfoByIdLiveData(iperf3UP.getId()).observeForever(workInfo -> {
            boolean iperf3_upload;
            iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
            Log.d(TAG, "onChanged: iperf3_upload: " + iperf3_upload);
            iperf3RunResultDao.updateUpload(iperf3WorkerID, iperf3_upload);
        });


    }

    private String getKeyFromId(String s, String value) {
        String key = "";
        switch (s) {
            case "iperf3_logfile":
                key = "--logfile";
                input.measurementName = value;
                break;
            case "iperf3_streams":
                key = "-P";
                input.streams = value;
                break;
            case "iperf3_ip":
                key = "-c";
                input.iperf3IP = value;
                break;
            case "iperf3_port":
                key = "-p";
                input.iperf3Port = value;
                break;
            case "iperf3_bandwidth":
                key = "-b";
                input.iperf3Bandwidth = value;
                break;
            case "iperf3_duration":
                key = "-t";
                input.iperf3Duration = value;
                break;
            case "iperf3_interval":
                key = "-i";
                input.iperf3Interval = value;
                break;
            case "iperf3_bytes":
                key = "-n";
                input.iperf3Bytes = value;
                break;
            case "iperf3_cport":
                key = "--cport";
                input.iperf3Cport = value;
                break;
        }
        return key;
    }

    private String parseInput() {
        List<String> stb = new LinkedList<>();
        for (EditText et : editTexts) {
            String value = et.getText().toString();
            if (!value.equals("")) {
                String s = getResources().getResourceEntryName(et.getId());
                String key = getKeyFromId(s, value);
                if (s.equals("iperf3_bandwidth")) {
                    value += "M";
                }
                stb.add(key);
                stb.add(value);
            }
        }

        String protocol = protocolSpinner.getSelectedItem().toString();
        if (!protocol.equals("TCP")) {
            stb.add("--" + protocol.toLowerCase());
        }
        input.iperf3IdxProtocol = protocolSpinner.getSelectedItemPosition();

        input.timestamp = new Timestamp(System.currentTimeMillis());
        String iperf3TS = input.timestamp.toString().replace(" ", "_").replace(":", "_");

        input.uuid = UUID.randomUUID().toString();

        input.measurementName = "Iperf3";
        this.logFileName = String.format("iperf3_%s_%s.json", iperf3TS, input.uuid);

        input.iperf3LogFileName = this.logFileName;
        this.rawIperf3file = this.logFileDir + this.logFileName;
        input.iperf3rawIperf3file = this.rawIperf3file;

        stb.add("--logfile");
        stb.add(this.rawIperf3file);

        input.iperf3BiDir = false;
        input.iperf3Reverse = false;
        input.iperf3OneOff = false;
        input.iperf3Json = true;

        if (!isModeSpinnerClient()) {
            stb.add("-s");
            input.iperf3IdxMode = iperf3ModeSpinner.getSelectedItemPosition();
        }
        if (iperf3BiDir.isChecked()) {
            stb.add("--bidir");
            input.iperf3BiDir = true;
        }
        if (iperf3Reverse.isChecked()) {
            stb.add("--reverse");
            input.iperf3Reverse = true;
        }
        if (iperf3OneOff.isChecked()) {
            stb.add("--one-off");
            input.iperf3OneOff = true;
        }
        stb.add("--json-stream");

        stb.add("--connect-timeout");
        stb.add("500");


        String joined = String.join(" ", stb);

        Log.d(TAG, "parseInput: joined command " + joined);
        input.iperf3Command = joined;


        return joined;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void writeToSP() {
        SharedPreferences.Editor editor = spg.getSharedPreference(SPType.iperf3_sp).edit();
        editor.putInt(IPERF3IDXPROTOCOL, protocolSpinner.getSelectedItemPosition());
        editor.putInt(IPERF3IDXMODE, iperf3ModeSpinner.getSelectedItemPosition());
        editor.putString(IPERF3IP, iperf3EtIp.getText().toString());
        editor.putString(IPERF3PORT, iperf3EtPort.getText().toString());
        editor.putString(IPERF3BANDWIDTH, iperf3EtBandwidth.getText().toString());
        editor.putString(IPERF3DURATION, iperf3EtDuration.getText().toString());
        editor.putString(IPERF3INTERVAL, iperf3EtInterval.getText().toString());
        editor.putString(IPERF3BYTES, iperf3EtBytes.getText().toString());
        editor.putString(IPERF3STREAMS, iperf3EtStreams.getText().toString());
        editor.putString(IPERF3CPORT, iperf3Cport.getText().toString());

        editor.putBoolean(IPERF3BIDIR, iperf3BiDir.isChecked());
        editor.putBoolean(IPERF3REVERSE, iperf3Reverse.isChecked());
        editor.putBoolean(IPERF3ONEOFF, iperf3OneOff.isChecked());
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.isResumed())
            writeToSP();
    }



    public static class Iperf3Input {
        public boolean iperf3BiDir;
        public boolean iperf3Reverse;
        public boolean iperf3Json;
        public boolean iperf3OneOff;
        public int iperf3IdxMode;
        public int iperf3IdxProtocol;
        public String uuid;
        public String iperf3Command;
        public String iperf3rawIperf3file;
        public String iperf3LogFileName;
        public String measurementName;
        public String iperf3IP;
        public String iperf3Port;
        public String iperf3Bandwidth;
        public String iperf3LineProtocolFile;
        public String iperf3Duration;
        public String iperf3Interval;
        public String iperf3Bytes;
        public Timestamp timestamp;
        public String streams;
        public String iperf3Cport;
        private List<Field> getFields(){
            List<Field> fields = Arrays.asList(Iperf3Input.class.getDeclaredFields());
            fields.sort((o1, o2) -> {
                return o1.toGenericString().compareTo(o2.toGenericString());
            });
            return fields;
        }
        private LinearLayout getTextView(String name, String value, Context ct){
            LinearLayout mainLL = new LinearLayout(ct);
            mainLL.setOrientation(LinearLayout.HORIZONTAL);


            LinearLayout.LayoutParams parameterLayoutName = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            parameterLayoutName.weight = 1F;
            TextView parameterName = new TextView(ct);
            parameterName.setTextIsSelectable(true);
            parameterName.setText(String.format("%s", name));
            parameterName.setLayoutParams(parameterLayoutName);
            TextView parameterValue = new TextView(ct);
            parameterValue.setTextIsSelectable(true);
            parameterValue.setText(String.format("%s", value));
            LinearLayout.LayoutParams parameterLayoutValue = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            parameterLayoutValue.weight = 3F;
            parameterValue.setLayoutParams(parameterLayoutValue);

            mainLL.addView(parameterName);
            mainLL.addView(parameterValue);
            return mainLL;
        }
        private LinearLayout getTextViewValue(String key, String value, Context ct){
            LinearLayout mainLL = new LinearLayout(ct);
            mainLL.setOrientation(LinearLayout.HORIZONTAL);
            mainLL.setFocusable(false);
            mainLL.setFocusedByDefault(false);

            TextView parameterValue = new TextView(ct);

            parameterValue.setTextIsSelectable(true);
            parameterValue.setText(String.format("%s", value));
            LinearLayout.LayoutParams parameterLayoutValue = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            parameterValue.setPadding(5, 5, 5, 5);
            parameterLayoutValue.setMargins(0, 0, 10, 10);
            parameterLayoutValue.weight = 1F;
            parameterValue.setLayoutParams(parameterLayoutValue);

            mainLL.addView(parameterValue);
            return mainLL;
        }
        public LinearLayout getInputAsLinearLayoutKeyValue(LinearLayout mainLL, Context ct){
            mainLL.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 8F;
            mainLL.setLayoutParams(layoutParams);
            String[] protocol =
                ct.getResources().getStringArray(R.array.iperf_protocol);
            String[] mode = ct.getResources().getStringArray(R.array.iperf_mode);
            for(Field parameter: getFields()){
                try {
                    Object parameterValueObj = parameter.get(this);
                    if(parameterValueObj == null){
                        continue;
                    }

                    String parameterName = parameter.getName().replace("iperf3", "");
                    if(parameterName.equals("measurementName")
                        || parameterName.equals("rawIperf3file")
                        || parameterName.equals("LogFileName")
                        || parameterName.equals("Command")
                        || parameterName.equals("LineProtocolFile")) continue;

                    String parameterValue = parameter.get(this).toString();
                    if(parameterValue.equals("false")){
                        continue;
                    }
                    if(parameterName.equals("IdxProtocol")){
                        parameterName = "Protocol";
                        parameterValue = protocol[Integer.parseInt(parameterValue)];
                    }

                    if(parameterName.equals("IdxMode")){
                        parameterName = "Mode";
                        parameterValue = mode[Integer.parseInt(parameterValue)];
                    }
                    mainLL.addView(getTextView(
                        parameterName,
                        parameterValue,
                        ct));

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return mainLL;
        }

        public LinearLayout getInputAsLinearLayoutValue(LinearLayout mainLL, Context ct){
            mainLL.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 10F;
            mainLL.setLayoutParams(layoutParams);
            String[] protocol =
                ct.getResources().getStringArray(R.array.iperf_protocol);
            String[] mode = ct.getResources().getStringArray(R.array.iperf_mode);
            for(Field parameter: getFields()){
                try {
                    Object parameterValueObj = parameter.get(this);
                    if(parameterValueObj == null){
                        continue;
                    }

                    String parameterName = parameter.getName().replace("iperf3", "");
                    if(parameterName.equals("measurementName")
                        || parameterName.equals("rawIperf3file")
                        || parameterName.equals("LogFileName")
                        || parameterName.equals("Command")
                        || parameterName.equals("LineProtocolFile")
                        || parameterName.equals("timestamp")
                        || parameterName.equals("uuid")) continue;

                    String parameterValue = parameter.get(this).toString();
                    if(parameterValue.equals("false")){
                        continue;
                    }
                    if(parameterName.equals("IdxProtocol")){
                        parameterName = "Protocol";
                        parameterValue = protocol[Integer.parseInt(parameterValue)];
                    }

                    if(parameterName.equals("IdxMode")){
                        parameterName = "Mode";
                        parameterValue = mode[Integer.parseInt(parameterValue)];
                    }

                    if(parameterValue.equals("true")){
                        parameterValue = parameterName;
                    }

                    mainLL.addView(getTextViewValue(
                        parameterName,
                        parameterValue,
                        ct));

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return mainLL;
        }
    }
}