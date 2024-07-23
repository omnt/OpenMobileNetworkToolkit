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
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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

    // Declare the toggle group and buttons
    private MaterialButtonToggleGroup iperf3ModeToggleGroup;
    private MaterialButton iperf3DownloadButton;
    private MaterialButton iperf3UploadButton;
    private MaterialButton iperf3BiDirButton;

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
        ViewPager2 viewPager2 = v.findViewById(R.id.viewPager2);
        int numPages = 3; // Set this to the number of pages you want
        Iperf3CardAdapter adapter = new Iperf3CardAdapter(getActivity(), numPages);
        viewPager2.setAdapter(adapter);

        //initializeViews(v);
        //setupTextWatchers();
        //restoreTextInput();
        //setupProgressIndicator();
        //setupEditTextList();
        //setupButtons();
        //setupFragmentResultListener();
        //restorePreviousInputs();
        //setTemporaryDirectory();

        return v;
    }

    private void initializeViews(View v) {
        iperf3EtIp = v.findViewById(R.id.iperf3_ip);
        iperf3EtPort = v.findViewById(R.id.iperf3_port);
        iperf3EtBandwidth = v.findViewById(R.id.iperf3_bandwidth);
        iperf3EtDuration = v.findViewById(R.id.iperf3_duration);
        iperf3EtInterval = v.findViewById(R.id.iperf3_interval);
        iperf3EtBytes = v.findViewById(R.id.iperf3_bytes);
        iperf3EtStreams = v.findViewById(R.id.iperf3_streams);
        iperf3Cport = v.findViewById(R.id.iperf3_cport);
        progressIndicator = v.findViewById(R.id.iperf3_progress);
    }

    private void setupTextWatchers() {
        iperf3EtDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iperf3EtBytes.setEnabled(s.length() <= 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        iperf3EtBytes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iperf3EtDuration.setEnabled(s.length() <= 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void restoreTextInput() {
        saveTextInputToSharedPreferences(iperf3EtIp, IPERF3IP);
        saveTextInputToSharedPreferences(iperf3EtPort, IPERF3PORT);
        saveTextInputToSharedPreferences(iperf3EtBandwidth, IPERF3BANDWIDTH);
        saveTextInputToSharedPreferences(iperf3EtDuration, IPERF3DURATION);
        saveTextInputToSharedPreferences(iperf3EtInterval, IPERF3INTERVAL);
        saveTextInputToSharedPreferences(iperf3EtBytes, IPERF3BYTES);
        saveTextInputToSharedPreferences(iperf3EtStreams, IPERF3STREAMS);
        saveTextInputToSharedPreferences(iperf3Cport, IPERF3CPORT);
    }

    private void setupProgressIndicator() {
        failedColors = new int[]{getContext().getColor(R.color.crimson), getContext().getColor(R.color.crimson), getContext().getColor(R.color.crimson)};
        runningColors = new int[]{getContext().getColor(R.color.purple_500), getContext().getColor(R.color.crimson), getContext().getColor(R.color.forestgreen)};
        succesColors = new int[]{getContext().getColor(R.color.forestgreen), getContext().getColor(R.color.forestgreen), getContext().getColor(R.color.forestgreen)};

        progressIndicator.setIndicatorColor(runningColors);
        progressIndicator.setIndeterminateAnimationType(LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS);
        progressIndicator.setVisibility(LinearProgressIndicator.INVISIBLE);
    }

    private void setupEditTextList() {
        editTexts = new LinkedList<>();
        editTexts.add(iperf3EtIp);
        editTexts.add(iperf3EtPort);
        editTexts.add(iperf3EtBandwidth);
        editTexts.add(iperf3EtDuration);
        editTexts.add(iperf3EtInterval);
        editTexts.add(iperf3EtBytes);
        editTexts.add(iperf3EtStreams);
        editTexts.add(iperf3Cport);
    }

    private void setupButtons() {
        sendBtn = v.findViewById(R.id.iperf3_send);
        instancesBtn = v.findViewById(R.id.iperf3_instances_button);

        sendBtn.setOnClickListener(this::executeIperfCommand);
        instancesBtn.setOnClickListener(this::showInstances);
    }

    private void setupFragmentResultListener() {
        getActivity().getSupportFragmentManager().setFragmentResultListener("input", getViewLifecycleOwner(), new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Iperf3RunResult iperf3RunResult = db.iperf3RunResultDao().getRunResult(result.getString("uid"));
                populateFieldsFromRunResult(iperf3RunResult);
                writeToSP();
            }
        });
    }

    private void populateFieldsFromRunResult(Iperf3RunResult iperf3RunResult) {
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

    }

    private void restorePreviousInputs() {
        SharedPreferences sp = spg.getSharedPreference(SPType.iperf3_sp);

        iperf3EtIp.setText(sp.getString(IPERF3IP, null));
        iperf3EtPort.setText(sp.getString(IPERF3PORT, null));
        iperf3EtBandwidth.setText(sp.getString(IPERF3BANDWIDTH, null));
        iperf3EtDuration.setText(sp.getString(IPERF3DURATION, null));
        iperf3EtInterval.setText(sp.getString(IPERF3INTERVAL, null));
        iperf3EtBytes.setText(sp.getString(IPERF3BYTES, null));
        iperf3EtStreams.setText(sp.getString(IPERF3STREAMS, null));
        iperf3Cport.setText(sp.getString(IPERF3CPORT, null));
    }

    private void setTemporaryDirectory() {
        try {
            Os.setenv("TMPDIR", String.valueOf(getActivity().getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
    }


    public void showInstances(View view) {
        Bundle bundle = createIperf3Bundle();
        NavController navController = getNavController();
        navigateToRunnersList(navController, bundle);
    }

    private Bundle createIperf3Bundle() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("iperf3List", uids);
        return bundle;
    }

    private NavController getNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        return navHostFragment.getNavController();
    }

    private void navigateToRunnersList(NavController navController, Bundle bundle) {
        navController.navigate(R.id.runners_list, bundle);
    }


    private boolean isModeSpinnerClient() {
        String status = iperf3ModeSpinner.getSelectedItem().toString();
        return status.equals("Client");
    }

    public void executeIperfCommand(View view) {
        String[] command = parseInput().split(" ");
        String path = getPath();

        createDirectoryIfNeeded(path);
        setLogFileNames(path);
        uids.add(0, input.uuid);

        Data iperf3Data = buildIperf3Data(command);

        ListenableFuture<List<WorkInfo>> status = iperf3WM.getWorkInfosByTag("iperf3Run");


        OneTimeWorkRequest iperf3WR = createWorkRequest(Iperf3Worker.class, iperf3Data, "iperf3Run", input.uuid);
        OneTimeWorkRequest iperf3LP = createWorkRequest(Iperf3ToLineProtocolWorker.class, iperf3Data, null, null);
        OneTimeWorkRequest iperf3UP = createWorkRequest(Iperf3UploadWorker.class, iperf3Data, "iperf3", null);

        saveIperf3RunResult(input.uuid);

        enqueueWorkRequests(iperf3WR, iperf3LP, iperf3UP);
        observeWorkStatus(iperf3WR, iperf3UP);
    }

    private String getPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/iperf3LP/";
    }

    private void createDirectoryIfNeeded(String path) {
        if (input.iperf3Json) {
            try {
                Files.createDirectories(Paths.get(path));
            } catch (IOException e) {
                Toast.makeText(requireContext(), "Could not create Dir files!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLogFileNames(String path) {
        String iperf3WorkerID = input.uuid;
        input.iperf3LineProtocolFile = path + iperf3WorkerID + ".txt";
    }

    private Data buildIperf3Data(String[] command) {
        return new Data.Builder()
                .putStringArray("commands", command)
                .putString("iperf3WorkerID", input.uuid)
                .putString("rawIperf3file", rawIperf3file)
                .putString("iperf3LineProtocolFile", input.iperf3LineProtocolFile)
                .putString("measurementName", input.measurementName)
                .putString("ip", input.iperf3IP)
                .putString("port", input.iperf3Port)
                .putString("bandwidth", input.iperf3Bandwidth)
                .putString("duration", input.iperf3Duration)
                .putString("interval", input.iperf3Interval)
                .putString("bytes", input.iperf3Bytes)
                .putString("protocol", protocolSpinner.getSelectedItem().toString())
                .putBoolean("rev", input.iperf3Reverse)
                .putBoolean("biDir", input.iperf3BiDir)
                .putBoolean("oneOff", input.iperf3OneOff)
                .putString("client", iperf3ModeSpinner.getSelectedItem().toString())
                .putString("timestamp", input.timestamp.toString())
                .putString("cport", input.iperf3Cport)
                .putInt("notificationID", uids.size())
                .build();
    }


    private OneTimeWorkRequest createWorkRequest(Class<? extends Worker> workerClass, Data data, String... tags) {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(workerClass).setInputData(data);
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null) {
                    builder.addTag(tag);
                }
            }
        }
        return builder.build();
    }

    private void saveIperf3RunResult(String iperf3WorkerID) {
        iperf3RunResultDao.insert(new Iperf3RunResult(iperf3WorkerID, -100, false, input, input.timestamp));
    }

    private void enqueueWorkRequests(OneTimeWorkRequest iperf3WR, OneTimeWorkRequest iperf3LP, OneTimeWorkRequest iperf3UP) {
        if (spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false) && input.iperf3Json) {
            iperf3WM.beginWith(iperf3WR).then(iperf3LP).then(iperf3UP).enqueue();
        } else if (input.iperf3Json) {
            iperf3WM.beginWith(iperf3WR).then(iperf3LP).enqueue();
        } else {
            iperf3WM.beginWith(iperf3WR).enqueue();
        }
    }

    private void observeWorkStatus(OneTimeWorkRequest iperf3WR, OneTimeWorkRequest iperf3UP) {
        Handler progressbarHandler = new Handler(Looper.myLooper());

        iperf3WM.getWorkInfoByIdLiveData(iperf3WR.getId()).observeForever(workInfo -> {
            int iperf3_result = workInfo.getOutputData().getInt("iperf3_result", -100);
            if (workInfo.getState().equals(WorkInfo.State.CANCELLED)) {
                iperf3_result = -1;
            }
            iperf3RunResultDao.updateResult(input.uuid, iperf3_result);
            updateProgressIndicator(iperf3_result, progressbarHandler);
        });

        iperf3WM.getWorkInfoByIdLiveData(iperf3UP.getId()).observeForever(workInfo -> {
            boolean iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
            iperf3RunResultDao.updateUpload(input.uuid, iperf3_upload);
        });
    }

    private void updateProgressIndicator(int iperf3_result, Handler progressbarHandler) {
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
    }


    private String getKeyFromId(String id, String value) {
        Map<String, CommandInfo> commandMap = createCommandMap();

        if (commandMap.containsKey(id)) {
            CommandInfo commandInfo = commandMap.get(id);
            commandInfo.setInput(value);
            return commandInfo.getKey();
        }

        return "";
    }

    private Map<String, CommandInfo> createCommandMap() {
        Map<String, CommandInfo> commandMap = new HashMap<>();

        commandMap.put("iperf3_logfile", new CommandInfo("--logfile", value -> input.measurementName = value));
        commandMap.put("iperf3_streams", new CommandInfo("-P", value -> input.streams = value));
        commandMap.put("iperf3_ip", new CommandInfo("-c", value -> input.iperf3IP = value));
        commandMap.put("iperf3_port", new CommandInfo("-p", value -> input.iperf3Port = value));
        commandMap.put("iperf3_bandwidth", new CommandInfo("-b", value -> input.iperf3Bandwidth = value));
        commandMap.put("iperf3_duration", new CommandInfo("-t", value -> input.iperf3Duration = value));
        commandMap.put("iperf3_interval", new CommandInfo("-i", value -> input.iperf3Interval = value));
        commandMap.put("iperf3_bytes", new CommandInfo("-n", value -> input.iperf3Bytes = value));
        commandMap.put("iperf3_cport", new CommandInfo("--cport", value -> input.iperf3Cport = value));

        return commandMap;
    }

    private static class CommandInfo {
        private final String key;
        private final Consumer<String> inputSetter;

        public CommandInfo(String key, Consumer<String> inputSetter) {
            this.key = key;
            this.inputSetter = inputSetter;
        }

        public String getKey() {
            return key;
        }

        public void setInput(String value) {
            inputSetter.accept(value);
        }
    }


    private String parseInput() {
        List<String> commandParts = new LinkedList<>();

        addEditTextValues(commandParts);
        addProtocolOption(commandParts);
        setInputMetadata();
        addLogFileOptions(commandParts);
        addModeOptions(commandParts);
        addAdditionalOptions(commandParts);

        String command = String.join(" ", commandParts);

        Log.d(TAG, "parseInput: joined command " + command);
        input.iperf3Command = command;

        return command;
    }

    private void addEditTextValues(List<String> commandParts) {
        for (EditText et : editTexts) {
            String value = et.getText().toString();
            if (!value.isEmpty()) {
                String resourceName = getResources().getResourceEntryName(et.getId());
                String key = getKeyFromId(resourceName, value);
                if (resourceName.equals("iperf3_bandwidth")) {
                    value += "M";
                }
                commandParts.add(key);
                commandParts.add(value);
            }
        }
    }

    private void addProtocolOption(List<String> commandParts) {
        String protocol = protocolSpinner.getSelectedItem().toString();
        if (!protocol.equals("TCP")) {
            commandParts.add("--" + protocol.toLowerCase());
        }
        input.iperf3IdxProtocol = protocolSpinner.getSelectedItemPosition();
    }

    private void setInputMetadata() {
        input.timestamp = new Timestamp(System.currentTimeMillis());
        String iperf3TS = input.timestamp.toString().replace(" ", "_").replace(":", "_");

        input.uuid = UUID.randomUUID().toString();
        input.measurementName = "Iperf3";
        this.logFileName = String.format("iperf3_%s_%s.json", iperf3TS, input.uuid);
        input.iperf3LogFileName = this.logFileName;

        this.rawIperf3file = this.logFileDir + this.logFileName;
        input.iperf3rawIperf3file = this.rawIperf3file;
    }

    private void addLogFileOptions(List<String> commandParts) {
        commandParts.add("--logfile");
        commandParts.add(this.rawIperf3file);
    }

    private void addModeOptions(List<String> commandParts) {

        input.iperf3OneOff = iperf3OneOff.isChecked();
        input.iperf3Json = true;

        if (!isModeSpinnerClient()) {
            commandParts.add("-s");
            input.iperf3IdxMode = iperf3ModeSpinner.getSelectedItemPosition();
        }
    }

    private void addAdditionalOptions(List<String> commandParts) {

        if (iperf3OneOff.isChecked()) {
            commandParts.add("--one-off");
        }
        commandParts.add("--json-stream");

        commandParts.add("--connect-timeout");
        commandParts.add("500");
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