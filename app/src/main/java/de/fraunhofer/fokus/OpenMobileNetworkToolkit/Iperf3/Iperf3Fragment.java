/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Fragment extends Fragment {
    public static final String FRAGMENT_ID = "FRAGMENT_ID";
    private static int idx;
    private static final String TAG = "iperf3InputFragment";
    private final int SHOW_PROGRESSBAR = 3000;
    private final String IPERF3LOGFILENAME = "iperf3LogFileName";
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
    private CheckBox iperf3BiDir;
    private CheckBox iperf3Reverse;
    private CheckBox iperf3Json;
    private CheckBox iperf3OneOff;
    private EditText iperf3EtLog;
    private EditText iperf3EtIp;
    private EditText iperf3EtPort;
    private EditText iperf3EtBandwidth;
    private EditText iperf3EtDuration;
    private EditText iperf3EtInterval;
    private EditText iperf3EtBytes;
    private EditText iperf3EtStreams;
    private Button sendBtn;
    private Button instancesBtn;
    private Spinner protocolSpinner;
    private Spinner iperf3ModeSpinner;
    private Iperf3RunResultDao iperf3RunResultDao;
    private LinearProgressIndicator progressIndicator;
    private int[] failedColors;
    private int[] runningColors;
    private int[] succesColors;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private LinkedList<EditText> editTexts;
    private String logFilePath;
    private String logFileDir;
    private String logFileName;
    private Iperf3ListFragment iperf3ListFragment;
    private View v;
    private SharedPreferences preferences;
    private Iperf3Input input;
    private WorkManager iperf3WM;
    private Iperf3ResultsDataBase db;
    private ArrayList<String> uids;
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
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
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
                .getAbsolutePath() + "/iperf3_logs/";
        this.iperf3RunResultDao = db.iperf3RunResultDao();
        File iperf3Path = new File(this.logFileDir);
        if (!iperf3Path.exists()) {
            iperf3Path.mkdir();
        }
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

                iperf3EtLog.setText(iperf3RunResult.input.measurementName.split("_")[0]);
                iperf3EtIp.setText(iperf3RunResult.input.iperf3IP);
                iperf3EtPort.setText(iperf3RunResult.input.iperf3Port);
                iperf3EtBandwidth.setText(iperf3RunResult.input.iperf3Bandwidth);
                iperf3EtDuration.setText(iperf3RunResult.input.iperf3Duration);
                iperf3EtInterval.setText(iperf3RunResult.input.iperf3Interval);
                iperf3EtBytes.setText(iperf3RunResult.input.iperf3Bytes);

                iperf3Reverse.setChecked(iperf3RunResult.input.iperf3Reverse);
                iperf3BiDir.setChecked(iperf3RunResult.input.iperf3BiDir);
                iperf3Json.setChecked(iperf3RunResult.input.iperf3Json);
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
                preferences.edit().putString(name+ idx, field.getText().toString()).apply();
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
                preferences.edit().putBoolean(name+idx, box.isChecked()).apply();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null) idx = args.getInt(FRAGMENT_ID);

        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        iperf3EtLog = v.findViewById(R.id.iperf3_logfile);
        iperf3EtIp = v.findViewById(R.id.iperf3_ip);
        iperf3EtPort = v.findViewById(R.id.iperf3_port);
        iperf3EtBandwidth = v.findViewById(R.id.iperf3_bandwidth);
        iperf3EtDuration = v.findViewById(R.id.iperf3_duration);
        iperf3EtInterval = v.findViewById(R.id.iperf3_interval);
        iperf3EtBytes = v.findViewById(R.id.iperf3_bytes);
        iperf3EtStreams = v.findViewById(R.id.iperf3_streams);
        progressIndicator = v.findViewById(R.id.iperf3_progress);




        saveTextInputToSharedPreferences(iperf3EtLog, IPERF3LOGFILENAME);
        saveTextInputToSharedPreferences(iperf3EtIp, IPERF3IP);
        saveTextInputToSharedPreferences(iperf3EtPort, IPERF3PORT);
        saveTextInputToSharedPreferences(iperf3EtBandwidth, IPERF3BANDWIDTH);
        saveTextInputToSharedPreferences(iperf3EtDuration, IPERF3DURATION);
        saveTextInputToSharedPreferences(iperf3EtInterval, IPERF3INTERVAL);
        saveTextInputToSharedPreferences(iperf3EtBytes, IPERF3BYTES);
        saveTextInputToSharedPreferences(iperf3EtStreams, IPERF3STREAMS);

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
        //editTexts.add(iperf3EtLog);
        editTexts.add(iperf3EtIp);
        editTexts.add(iperf3EtPort);
        editTexts.add(iperf3EtBandwidth);
        editTexts.add(iperf3EtDuration);
        editTexts.add(iperf3EtInterval);
        editTexts.add(iperf3EtBytes);
        editTexts.add(iperf3EtStreams);
        instancesBtn = v.findViewById(R.id.iperf3_instances_button);

        //sendBtn.setOnClickListener(this::executeIperfCommand);
        instancesBtn.setOnClickListener(this::showInstances);

        iperf3BiDir = v.findViewById(R.id.iperf_bidir);
        iperf3Reverse = v.findViewById(R.id.iperf3_reverse);
        iperf3Json = v.findViewById(R.id.iperf3_json);
        iperf3OneOff = v.findViewById(R.id.iperf3_one_off);

        saveCheckboxInputToSharedPreferences(iperf3BiDir, IPERF3BIDIR);
        saveCheckboxInputToSharedPreferences(iperf3Reverse, IPERF3REVERSE);
        saveCheckboxInputToSharedPreferences(iperf3Json, IPERF3JSON);
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

                        iperf3EtLog.setText(logFileName);
                        iperf3EtIp.setText(iperf3RunResult.input.iperf3IP);
                        iperf3EtPort.setText(iperf3RunResult.input.iperf3Port);
                        iperf3EtBandwidth.setText(iperf3RunResult.input.iperf3Bandwidth);
                        iperf3EtDuration.setText(iperf3RunResult.input.iperf3Duration);
                        iperf3EtInterval.setText(iperf3RunResult.input.iperf3Interval);
                        iperf3EtBytes.setText(iperf3RunResult.input.iperf3Bytes);
                        iperf3EtStreams.setText(iperf3RunResult.input.streams);

                        iperf3Reverse.setChecked(iperf3RunResult.input.iperf3Reverse);
                        iperf3BiDir.setChecked(iperf3RunResult.input.iperf3BiDir);
                        iperf3Json.setChecked(iperf3RunResult.input.iperf3Json);
                        iperf3OneOff.setChecked(iperf3RunResult.input.iperf3OneOff);
                        protocolSpinner.setSelection(iperf3RunResult.input.iperf3IdxProtocol);
                        iperf3ModeSpinner.setSelection(iperf3RunResult.input.iperf3IdxMode);

                        writeToSP();
                    }
                });


            iperf3EtLog.setText(preferences.getString(IPERF3LOGFILENAME+idx, null));
            iperf3EtIp.setText(preferences.getString(IPERF3IP+idx, null));
            iperf3EtPort.setText(preferences.getString(IPERF3PORT+idx, null));
            iperf3EtBandwidth.setText(preferences.getString(IPERF3BANDWIDTH+idx, null));
            iperf3EtDuration.setText(preferences.getString(IPERF3DURATION+idx, null));
            iperf3EtInterval.setText(preferences.getString(IPERF3INTERVAL+idx, null));
            iperf3EtBytes.setText(preferences.getString(IPERF3BYTES+idx, null));
            iperf3EtStreams.setText(preferences.getString(IPERF3STREAMS+idx, null));

            iperf3BiDir.setChecked(preferences.getBoolean(IPERF3BIDIR+idx, false));
            iperf3Reverse.setChecked(preferences.getBoolean(IPERF3REVERSE+idx, false));
            iperf3Json.setChecked(preferences.getBoolean(IPERF3JSON+idx, false));
            iperf3OneOff.setChecked(preferences.getBoolean(IPERF3ONEOFF+idx, false));
            protocolSpinner.setSelection(preferences.getInt(IPERF3IDXPROTOCOL+idx, 0));
            iperf3ModeSpinner.setSelection(preferences.getInt(IPERF3IDXMODE+idx, 0));

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

    public Iperf3Input getInput(){
        parseInput();
        return this.input;
    }

    public OneTimeWorkRequest getIperf3Worker(){
        String[] command = parseInput().split(" ");

        String iperf3WorkerID = UUID.randomUUID().toString();
        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", command);
        iperf3Data.putString("iperf3WorkerID", iperf3WorkerID);
        iperf3Data.putString("logfilepath", logFilePath);
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


        uids.add(0, iperf3WorkerID);
        iperf3Data.putInt("notificationID", uids.size());


        OneTimeWorkRequest iperf3WR =
            new OneTimeWorkRequest.Builder(Iperf3Worker.class).setInputData(iperf3Data.build())
                .addTag("iperf3Run").addTag(iperf3WorkerID).build();

        iperf3RunResultDao.insert(
            new Iperf3RunResult(iperf3WorkerID, -100, false, input, input.timestamp));


        return iperf3WR;
    }


    public void executeIperfCommand(View view) {
        String[] command = parseInput().split(" ");

        String iperf3WorkerID = UUID.randomUUID().toString();
        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", command);
        iperf3Data.putString("iperf3WorkerID", iperf3WorkerID);
        iperf3Data.putString("logfilepath", logFilePath);
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

        ListenableFuture<List<WorkInfo>> status = iperf3WM.getWorkInfosByTag("iperf3Run");

        try {
            for (WorkInfo workInfo : status.get()) {
                if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                    Toast.makeText(getContext(), "iperf3 Test is running!", Toast.LENGTH_SHORT)
                        .show();
                    return;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        uids.add(0, iperf3WorkerID);
        iperf3Data.putInt("notificationID", uids.size());


        OneTimeWorkRequest iperf3WR =
            new OneTimeWorkRequest.Builder(Iperf3Worker.class).setInputData(iperf3Data.build())
                .addTag("iperf3Run").addTag(iperf3WorkerID).build();
        OneTimeWorkRequest iperf3UP =
            new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class).setInputData(
                iperf3Data.build()).addTag("iperf3").build();

        iperf3RunResultDao.insert(
            new Iperf3RunResult(iperf3WorkerID, -100, false, input, input.timestamp));



        if (preferences.getBoolean("enable_influx", false) && input.iperf3Json) {
            iperf3WM.beginWith(iperf3WR).then(iperf3UP).enqueue();
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
            if (iperf3ListFragment != null) {
                iperf3ListFragment.updateIperf3ListAdapter();
            }
        });
        iperf3WM.getWorkInfoByIdLiveData(iperf3UP.getId()).observeForever(workInfo -> {
            boolean iperf3_upload;
            iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
            Log.d(TAG, "onChanged: iperf3_upload: " + iperf3_upload);
            iperf3RunResultDao.updateUpload(iperf3WorkerID, iperf3_upload);
            if (iperf3ListFragment != null) {
                iperf3ListFragment.updateIperf3ListAdapter();
            }
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
        String iperf3TS = "_" + input.timestamp.toString().replace(" ", "_").replace(":", "_");
        String logName = iperf3EtLog.getText().toString();
        input.measurementName = logName;

        input.uuid = UUID.randomUUID().toString();


        if (!logName.equals("")) {
            this.logFileName = logName + iperf3TS+input.uuid + ".txt";
        } else {
            this.logFileName = "iperf3" + iperf3TS+input.uuid + ".txt";
            input.measurementName = "Iperf3";
        }

        input.iperf3LogFileName = this.logFileName;
        this.logFilePath = this.logFileDir + this.logFileName;
        input.iperf3LogFilePath = this.logFilePath;

        stb.add("--logfile");
        stb.add(this.logFilePath);

        input.iperf3BiDir = false;
        input.iperf3Reverse = false;
        input.iperf3OneOff = false;
        input.iperf3Json = false;

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
        if (iperf3Json.isChecked()) {
            stb.add("--json");
            input.iperf3Json = true;
        }

        String joined = String.join(" ", stb);

        Log.d(TAG, "parseInput: joined command " + joined);
        input.iperf3Command = joined;


        return joined;
    }

    private void writeToSP() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(IPERF3IDXPROTOCOL+idx, protocolSpinner.getSelectedItemPosition());
        editor.putInt(IPERF3IDXMODE+idx, iperf3ModeSpinner.getSelectedItemPosition());
        editor.putString(IPERF3LOGFILENAME+idx, iperf3EtLog.getText().toString());
        editor.putString(IPERF3IP+idx, iperf3EtIp.getText().toString());
        editor.putString(IPERF3PORT+idx, iperf3EtPort.getText().toString());
        editor.putString(IPERF3BANDWIDTH+idx, iperf3EtBandwidth.getText().toString());
        editor.putString(IPERF3DURATION+idx, iperf3EtDuration.getText().toString());
        editor.putString(IPERF3INTERVAL+idx, iperf3EtInterval.getText().toString());
        editor.putString(IPERF3BYTES+idx, iperf3EtBytes.getText().toString());
        editor.putString(IPERF3STREAMS+idx, iperf3EtStreams.getText().toString());


        editor.putBoolean(IPERF3BIDIR+idx, iperf3BiDir.isChecked());
        editor.putBoolean(IPERF3REVERSE+idx, iperf3Reverse.isChecked());
        editor.putBoolean(IPERF3JSON+idx, iperf3Json.isChecked());
        editor.putBoolean(IPERF3ONEOFF+idx, iperf3OneOff.isChecked());
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        public String iperf3LogFilePath;
        public String iperf3LogFileName;
        public String measurementName;
        public String iperf3IP;
        public String iperf3Port;
        public String iperf3Bandwidth;
        public String iperf3Duration;
        public String iperf3Interval;
        public String iperf3Bytes;
        public Timestamp timestamp;
        public String streams;
    }
}