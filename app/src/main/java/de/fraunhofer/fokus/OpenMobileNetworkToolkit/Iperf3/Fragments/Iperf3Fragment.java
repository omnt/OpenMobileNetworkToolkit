
/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;
import androidx.work.WorkQuery;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Executor;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3RecyclerViewAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3MonitorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Fragment extends Fragment {

    private static final String TAG = "Iperf3Fragment";
    private static final String ARG_POSITION = "position";
    private static final int POLLING_INTERVAL_MS = 500;
    private static final int DB_WRITE_DELAY_MS = 1000;
    private static final int MAX_HISTORY = 4;

    // Core data
    private Iperf3Input iperf3Input;
    private Context ct;
    private String currentTestUUID;

    // UI Components
    private View view;
    private MaterialButton sendBtn;
    private MaterialButton stopBtn;
    private MaterialAutoCompleteTextView ip;
    private MaterialAutoCompleteTextView port;
    private MaterialAutoCompleteTextView bitrate;
    private MaterialAutoCompleteTextView duration;
    private MaterialAutoCompleteTextView interval;
    private MaterialAutoCompleteTextView bytes;
    private MaterialAutoCompleteTextView parallel;
    private MaterialAutoCompleteTextView cport;

    private MaterialButtonToggleGroup mode;
    private MaterialButtonToggleGroup protocol;
    private MaterialButtonToggleGroup direction;

    private MaterialButton modeClient;
    private MaterialButton modeServer;
    private MaterialButton protocolTCP;
    private MaterialButton protocolUDP;
    private MaterialButton directionUp;
    private MaterialButton directionDown;
    private MaterialButton directonBidir;

    private RecyclerView recyclerView;
    private BottomSheetBehavior bottomSheetBehavior;
    private FloatingActionButton fab;

    // Database and adapters
    private Iperf3RecyclerViewAdapter adapter;
    private Iperf3RunResultDao iperf3RunResultDao;
    private Iperf3ResultsDataBase iperf3ResultsDataBase;
    private SharedPreferencesGrouper spg;

    // Handler
    private Handler handler;
    private Runnable monitoringRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ct = requireContext();
        setupBackPressedHandler();
    }

    /**     * Setup back pressed handler to navigate home     */
    private void setupBackPressedHandler() {
        NavController navController = NavHostFragment.findNavController(this);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.HomeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**     * Create a text watcher for input fields     */
    private TextWatcher createTextWatcher(Consumer<String> consumer, String prefKey) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                consumer.accept(s.toString());
                spg.getSharedPreference(SPType.IPERF3).edit().putString(prefKey, s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    /**     * Setup text watchers for all input fields     */
    private void setupTextWatchers() {
        ip.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setHost(s),
                Iperf3Parameter.HOST));
        port.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setPort(Integer.parseInt("0" + s)),
                Iperf3Parameter.PORT));
        bitrate.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setBandwidth(s),
                Iperf3Parameter.BITRATE));
        duration.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setTime(Integer.parseInt("0" + s)),
                Iperf3Parameter.TIME));
        interval.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setInterval(Double.parseDouble("0" + s)),
                Iperf3Parameter.INTERVAL));
        bytes.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setBytes(s),
                Iperf3Parameter.BYTES));
        parallel.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setParallel(Integer.parseInt("0" + s)),
                Iperf3Parameter.PARALLEL));
        cport.addTextChangedListener(createTextWatcher(
                s -> iperf3Input.getParameter().setCport(Integer.parseInt("0" + s)),
                Iperf3Parameter.CPORT));
    }

    private void saveHistory(String key, String value) {
        if (value == null || value.isEmpty()) return;
        String historyKey = key + "_history";
        String historyStr = spg.getSharedPreference(SPType.IPERF3).getString(historyKey, "");
        List<String> history = new ArrayList<>(Arrays.asList(historyStr.split(",")));
        history.remove("");
        history.remove(value);
        history.add(0, value);
        if (history.size() > MAX_HISTORY) {
            history = history.subList(0, MAX_HISTORY);
        }
        spg.getSharedPreference(SPType.IPERF3).edit().putString(historyKey, String.join(",", history)).apply();
    }

    private void loadHistory(MaterialAutoCompleteTextView textView, String key) {
        String historyKey = key + "_history";
        String historyStr = spg.getSharedPreference(SPType.IPERF3).getString(historyKey, "");
        if (!historyStr.isEmpty()) {
            String[] history = historyStr.split(",");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, history);
            textView.setAdapter(adapter);
            textView.setThreshold(0);
        }
    }

    private void saveAllHistories() {
        saveHistory(Iperf3Parameter.HOST, ip.getText().toString());
        saveHistory(Iperf3Parameter.PORT, port.getText().toString());
        saveHistory(Iperf3Parameter.BITRATE, bitrate.getText().toString());
        saveHistory(Iperf3Parameter.TIME, duration.getText().toString());
        saveHistory(Iperf3Parameter.INTERVAL, interval.getText().toString());
        saveHistory(Iperf3Parameter.BYTES, bytes.getText().toString());
        saveHistory(Iperf3Parameter.PARALLEL, parallel.getText().toString());
        saveHistory(Iperf3Parameter.CPORT, cport.getText().toString());

        // Reload histories to update adapters
        loadAllHistories();
    }

    private void loadAllHistories() {
        loadHistory(ip, Iperf3Parameter.HOST);
        loadHistory(port, Iperf3Parameter.PORT);
        loadHistory(bitrate, Iperf3Parameter.BITRATE);
        loadHistory(duration, Iperf3Parameter.TIME);
        loadHistory(interval, Iperf3Parameter.INTERVAL);
        loadHistory(bytes, Iperf3Parameter.BYTES);
        loadHistory(parallel, Iperf3Parameter.PARALLEL);
        loadHistory(cport, Iperf3Parameter.CPORT);
    }

    /**     * Set text from shared preferences for a specific field     */
    private void setTextFromSharedPreferences(MaterialAutoCompleteTextView editText, String key) {
        if (spg.getSharedPreference(SPType.IPERF3).contains(key)) {
            editText.setText(spg.getSharedPreference(SPType.IPERF3).getString(key, ""), false);
        }
    }

    /**     * Load all text fields from shared preferences     */
    private void loadPreferences() {
        setTextFromSharedPreferences(ip, Iperf3Parameter.HOST);
        setTextFromSharedPreferences(port, Iperf3Parameter.PORT);
        setTextFromSharedPreferences(bitrate, Iperf3Parameter.BITRATE);
        setTextFromSharedPreferences(duration, Iperf3Parameter.TIME);
        setTextFromSharedPreferences(interval, Iperf3Parameter.INTERVAL);
        setTextFromSharedPreferences(bytes, Iperf3Parameter.BYTES);
        setTextFromSharedPreferences(parallel, Iperf3Parameter.PARALLEL);
        setTextFromSharedPreferences(cport, Iperf3Parameter.CPORT);
    }

    /**     * Setup button listeners     */
    private void setupButtonListeners() {
        sendBtn.setOnClickListener(v -> startTest());
        stopBtn.setOnClickListener(v -> stopCurrentTest());
    }

    /**
     * Update button visibility based on test state
     */
    private void updateActionButtons(boolean isTestRunning) {
        if (isTestRunning) {
            sendBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            stopBtn.setEnabled(true);
            stopBtn.setAlpha(1.0f);
        } else {
            sendBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            stopBtn.setEnabled(false);
            stopBtn.setAlpha(0.5f);
        }
    }

    private void startTest() {
        saveAllHistories();
        currentTestUUID = UUID.randomUUID().toString();
        iperf3Input.setTestUUID(currentTestUUID);
        iperf3Input.getParameter().setTestUUID(currentTestUUID);
        iperf3Input.getParameter().updatePaths();
        iperf3Input.setTimestamp(new Timestamp(System.currentTimeMillis()));

        createLogFile();
        executeTest();
        insertTestRecord();
        startMonitoring();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        updateActionButtons(true);  // Show stop button, hide send button
    }

    /**     * Create log file and directories     */
    private void createLogFile() {
        File logFile = new File(iperf3Input.getParameter().getRawLogFilePath());
        File rawPath = new File(iperf3Input.getParameter().getRawDirPath());

        if (!rawPath.exists()) {
            rawPath.mkdirs();
        }
        try {
            logFile.createNewFile();
            Log.d(TAG, "Created file: " + logFile);
        } catch (Exception e) {
            Log.e(TAG, "Error creating file: ", e);
        }
    }

    /**     * Execute the Iperf3 test     */
    private void executeTest() {
        Iperf3Executor iperf3Executor = new Iperf3Executor(iperf3Input, getContext());
        iperf3Executor.execute();
        Log.d(TAG, "Test duration: " + iperf3Input.getParameter().getTime() + "s");
    }

    /**     * Insert test record into database     */
    private void insertTestRecord() {
        Iperf3RunResult iperf3RunResult = new Iperf3RunResult(
                iperf3Input.getTestUUID(),
                -100,
                false,
                iperf3Input,
                new Timestamp(System.currentTimeMillis())
        );
        iperf3RunResultDao.insert(iperf3RunResult);
    }

    /**     * Start monitoring test progress     */
    private void startMonitoring() {
        monitoringRunnable = createMonitoringRunnable();
        handler.post(monitoringRunnable);
    }

    /**     * Create a runnable for monitoring test progress     */
    private Runnable createMonitoringRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                queryAndUpdateTestStatus();
            }
        };
    }

    /**     * Query work status and update UI     */
    private void queryAndUpdateTestStatus() {
        if (currentTestUUID == null) return;
        RemoteWorkManager remoteWorkManager = RemoteWorkManager.getInstance(ct);
        WorkQuery workQuery = WorkQuery.Builder
                .fromTags(Arrays.asList(currentTestUUID))
                .build();

        ListenableFuture<List<WorkInfo>> workInfoFuture = remoteWorkManager.getWorkInfos(workQuery);
        Futures.addCallback(
                workInfoFuture,
                new FutureCallback<List<WorkInfo>>() {
                    @Override
                    public void onSuccess(List<WorkInfo> result) {
                        handleWorkUpdates(result);
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        Log.e(TAG, "Error querying work info: ", t);
                    }
                },
                getContext().getMainExecutor()
        );
    }

    /**     * Handle work status updates     */
    private void handleWorkUpdates(List<WorkInfo> workInfoList) {
        if (workInfoList == null || workInfoList.isEmpty()) {
            if (currentTestUUID != null) {
                testFinished();
            }
            return;
        }
        
        boolean allFinished = true;
        for (WorkInfo workInfo : workInfoList) {
            if (!workInfo.getState().isFinished()) {
                allFinished = false;
            }
            if (workInfo.getTags().contains(Iperf3MonitorWorker.class.getCanonicalName())) {
                handleMonitorWorkerUpdate(workInfo);
            } else if (workInfo.getTags().contains(Iperf3ExecutorWorker.class.getCanonicalName())) {
                handleExecutorWorkerUpdate(workInfo);
            }
        }
        
        if (allFinished) {
            testFinished();
        } else {
            scheduleNextCheck();
        }
        adapter.notifyDataSetChanged();
    }

    /**     * Handle monitor worker status update     */
    private void handleMonitorWorkerUpdate(WorkInfo workInfo) {
        Log.d(TAG, "Monitor worker state: " + workInfo.getState());

        switch (workInfo.getState()) {
            case CANCELLED:
            case FAILED:
                handleTestCompletion();
                break;
        }
    }

    /**     * Handle executor worker status update     */
    private void handleExecutorWorkerUpdate(WorkInfo workInfo) {
        Log.d(TAG, "Executor worker state: " + workInfo.getState());

        switch (workInfo.getState()) {
            case CANCELLED:
            case FAILED:
                markTestFailed();
                break;
            case RUNNING:
                String progressLine = workInfo.getProgress().getString("interval");
                Log.d(TAG, "Progress: " + progressLine);
                break;
        }
    }

    /**     * Handle test completion with database write delay     */
    private void handleTestCompletion() {
        try {
            Thread.sleep(DB_WRITE_DELAY_MS);
            Iperf3RunResult result = iperf3RunResultDao.getRunResult(currentTestUUID);
            if (result != null) {
                Log.d(TAG, "Test result error: " + result.error);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for DB write: ", e);
        } finally {
            testFinished();
        }
    }

    /**     * Mark test as failed in database     */
    private void markTestFailed() {
        iperf3RunResultDao.updateResult(currentTestUUID, -1);
        RemoteWorkManager.getInstance(ct).cancelAllWorkByTag(currentTestUUID);
        testFinished();
    }

    /**     * Schedule next monitoring check     */
    private void scheduleNextCheck() {
        if (handler != null && monitoringRunnable != null && currentTestUUID != null) {
            handler.removeCallbacks(monitoringRunnable);
            handler.postDelayed(monitoringRunnable, POLLING_INTERVAL_MS);
        }
    }

    /**     * Stop the current test     */
    private void stopCurrentTest() {
        if (currentTestUUID == null) {
            Log.w(TAG, "No test running to stop");
            return;
        }

        Log.d(TAG, "Stopping test: " + currentTestUUID);
        RemoteWorkManager remoteWorkManager = RemoteWorkManager.getInstance(ct);
        remoteWorkManager.cancelAllWorkByTag(currentTestUUID);

        iperf3RunResultDao.updateResult(currentTestUUID, -1);

        stopMonitoring();
        testFinished();
        adapter.notifyDataSetChanged();
    }

    /**     * Stop monitoring     */
    private void stopMonitoring() {
        if (handler != null && monitoringRunnable != null) {
            handler.removeCallbacks(monitoringRunnable);
        }
    }

    /**     * Called when test finishes (success, failure, or manual stop)     */
    private void testFinished() {
        updateActionButtons(false);  // Show send button, hide stop button
        currentTestUUID = null;
    }

    /**     * Enable/disable send button     */
    private void enableSendButton(boolean enabled) {
        sendBtn.setEnabled(enabled);
        sendBtn.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**     * Enable/disable stop button     */
    private void enableStopButton(boolean enabled) {
        stopBtn.setEnabled(enabled);
        stopBtn.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**     * Setup UI components     */
    private void setupUIComponents(View rootView) {
        sendBtn = rootView.findViewById(R.id.iperf3_send);
        stopBtn = rootView.findViewById(R.id.iperf3_stop); // Ensure this button exists in layout

        ip = rootView.findViewById(R.id.iperf3_ip);
        port = rootView.findViewById(R.id.iperf3_port);
        bitrate = rootView.findViewById(R.id.iperf3_bandwidth);
        duration = rootView.findViewById(R.id.iperf3_duration);
        interval = rootView.findViewById(R.id.iperf3_interval);
        bytes = rootView.findViewById(R.id.iperf3_bytes);
        parallel = rootView.findViewById(R.id.iperf3_parallel);
        cport = rootView.findViewById(R.id.iperf3_cport);

        mode = rootView.findViewById(R.id.iperf3_mode_toggle_group);
        protocol = rootView.findViewById(R.id.iperf3_protocol_toggle_group);
        direction = rootView.findViewById(R.id.iperf3_direction_toggle_group);

        modeClient = rootView.findViewById(R.id.iperf3_client_button);
        modeServer = rootView.findViewById(R.id.iperf3_server_button);
        protocolTCP = rootView.findViewById(R.id.iperf3_tcp_button);
        protocolUDP = rootView.findViewById(R.id.iperf3_udp_button);
        directionDown = rootView.findViewById(R.id.iperf3_download_button);
        directionUp = rootView.findViewById(R.id.iperf3_upload_button);
        directonBidir = rootView.findViewById(R.id.iperf3_bidir_button);

        recyclerView = rootView.findViewById(R.id.runners_list);
    }

    /**     * Setup bottom sheet     */
    private void setupBottomSheet(View rootView) {
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.standard_bottom_sheet));
        bottomSheetBehavior.setPeekHeight(16);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);
    }

    /**     * Setup database     */
    private void setupDatabase() {
        iperf3ResultsDataBase = Iperf3ResultsDataBase.getDatabase(ct);
        iperf3RunResultDao = iperf3ResultsDataBase.iperf3RunResultDao();
    }

    /**     * Setup recycler view     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        adapter = new Iperf3RecyclerViewAdapter(fab);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d(TAG, "Selected UUID: " + adapter.getSelectedUUID());
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        
        // Observe all results to trigger updates
        iperf3RunResultDao.getAll().observe(getViewLifecycleOwner(), results -> {
            adapter.notifyDataSetChanged();
        });
    }

    /**     * Setup toggle group listeners     */
    private void setupToggleGroupListeners() {
        mode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.iperf3_client_button) {
                    updateModeState(modeClient, modeServer, Iperf3Parameter.Iperf3Mode.CLIENT);
                } else if (checkedId == R.id.iperf3_server_button) {
                    updateModeState(modeServer, modeClient, Iperf3Parameter.Iperf3Mode.SERVER);
                }
            }
        });

        protocol.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.iperf3_tcp_button) {
                    updateProtocolState(protocolTCP, protocolUDP, Iperf3Parameter.Iperf3Protocol.TCP);
                } else if (checkedId == R.id.iperf3_udp_button) {
                    updateProtocolState(protocolUDP, protocolTCP, Iperf3Parameter.Iperf3Protocol.UDP);
                }
            }
        });

        direction.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.iperf3_upload_button) {
                    updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Parameter.Iperf3Direction.UP);
                } else if (checkedId == R.id.iperf3_download_button) {
                    updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Parameter.Iperf3Direction.DOWN);
                } else if (checkedId == R.id.iperf3_bidir_button) {
                    updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Parameter.Iperf3Direction.BIDIR);
                }
            }
        });
    }

    /**     * Load saved mode state     */
    private void loadModeState() {
        try {
            Iperf3Parameter.Iperf3Mode savedMode = Iperf3Parameter.Iperf3Mode.valueOf(
                    spg.getSharedPreference(SPType.IPERF3)
                            .getString(Iperf3Parameter.MODE, Iperf3Parameter.Iperf3Mode.UNDEFINED.toString())
            );
            switch (savedMode) {
                case CLIENT:
                    updateModeState(modeClient, modeServer, Iperf3Parameter.Iperf3Mode.CLIENT);
                    break;
                case SERVER:
                    updateModeState(modeServer, modeClient, Iperf3Parameter.Iperf3Mode.SERVER);
                    break;
                default:
                    modeClient.setBackgroundColor(Color.TRANSPARENT);
                    modeServer.setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.IPERF3).edit()
                            .putString(Iperf3Parameter.MODE, Iperf3Parameter.Iperf3Mode.UNDEFINED.toString()).apply();
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error loading mode state: ", e);
        }
    }

    /**     * Load saved protocol state     */
    private void loadProtocolState() {
        try {
            Iperf3Parameter.Iperf3Protocol savedProtocol = Iperf3Parameter.Iperf3Protocol.valueOf(
                    spg.getSharedPreference(SPType.IPERF3)
                            .getString(Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.UNDEFINED.toString())
            );
            switch (savedProtocol) {
                case TCP:
                    updateProtocolState(protocolTCP, protocolUDP, Iperf3Parameter.Iperf3Protocol.TCP);
                    break;
                case UDP:
                    updateProtocolState(protocolUDP, protocolTCP, Iperf3Parameter.Iperf3Protocol.UDP);
                    break;
                default:
                    protocolTCP.setBackgroundColor(Color.TRANSPARENT);
                    protocolUDP.setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.IPERF3).edit()
                            .putString(Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.UNDEFINED.toString()).apply();
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error loading protocol state: ", e);
        }
    }

    /**     * Load saved direction state     */
    private void loadDirectionState() {
        try {
            Iperf3Parameter.Iperf3Direction savedDirection = Iperf3Parameter.Iperf3Direction.valueOf(
                    spg.getSharedPreference(SPType.IPERF3)
                            .getString(Iperf3Parameter.DIRECTION, Iperf3Parameter.Iperf3Direction.UNDEFINED.toString())
            );
            switch (savedDirection) {
                case UP:
                    updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Parameter.Iperf3Direction.UP);
                    break;
                case DOWN:
                    updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Parameter.Iperf3Direction.DOWN);
                    break;
                case BIDIR:
                    updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Parameter.Iperf3Direction.BIDIR);
                    break;
                default:
                    directionUp.setBackgroundColor(Color.TRANSPARENT);
                    directionDown.setBackgroundColor(Color.TRANSPARENT);
                    directonBidir.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error loading direction state: ", e);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_iperf3_input, container, false);

        // Initialize core components
        initializeCoreComponents();
        setupUIComponents(view);
        setupDatabase();
        setupBottomSheet(view);
        setupRecyclerView();

        // Load preferences and state
        setupTextWatchers();
        loadPreferences();
        loadModeState();
        loadProtocolState();
        loadDirectionState();
        loadAllHistories();

        // Setup listeners
        setupButtonListeners();
        setupToggleGroupListeners();

        // Check for running tests
        checkForRunningTests();

        return view;
    }

    private void checkForRunningTests() {
        RemoteWorkManager remoteWorkManager = RemoteWorkManager.getInstance(ct);
        WorkQuery workQuery = WorkQuery.Builder
                .fromStates(Arrays.asList(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED))
                .build();

        ListenableFuture<List<WorkInfo>> workInfoFuture = remoteWorkManager.getWorkInfos(workQuery);
        Futures.addCallback(
                workInfoFuture,
                new FutureCallback<List<WorkInfo>>() {
                    @Override
                    public void onSuccess(List<WorkInfo> result) {
                        for (WorkInfo workInfo : result) {
                            if (workInfo.getTags().contains(Iperf3ExecutorWorker.class.getCanonicalName())) {
                                // Extract the test UUID from tags (it was added as a tag during startTest)
                                for (String tag : workInfo.getTags()) {
                                    if (tag.length() == 36) { // Simple check for UUID string length
                                        currentTestUUID = tag;
                                        startMonitoring();
                                        updateActionButtons(true);
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        Log.e(TAG, "Error checking for running tests: ", t);
                    }
                },
                getContext().getMainExecutor()
        );
    }

    /**     * Initialize core components     */
    private void initializeCoreComponents() {
        String iperf3UUID = UUID.randomUUID().toString();
        Iperf3Parameter iperf3Parameter = new Iperf3Parameter(iperf3UUID);
        iperf3Input = new Iperf3Input(iperf3Parameter, "");
        spg = SharedPreferencesGrouper.getInstance(ct);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        super.onResume();
        view.requestLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
    }

    /**     * Update mode button state     */
    private void updateModeState(MaterialButton activeButton, MaterialButton inactiveButton,
                                 Iperf3Parameter.Iperf3Mode modeValue) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setMode(modeValue);
        spg.getSharedPreference(SPType.IPERF3).edit()
                .putString(Iperf3Parameter.MODE, modeValue.toString()).apply();
    }

    /**     * Update protocol button state     */
    private void updateProtocolState(MaterialButton activeButton, MaterialButton inactiveButton,
                                     Iperf3Parameter.Iperf3Protocol protocolValue) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setProtocol(protocolValue);
        spg.getSharedPreference(SPType.IPERF3).edit()
                .putString(Iperf3Parameter.PROTOCOL, protocolValue.toString()).apply();
    }

    /**     * Update direction button state     */
    private void updateDirectionState(MaterialButton activeButton, MaterialButton inactiveButton1,
                                      MaterialButton inactiveButton2, Iperf3Parameter.Iperf3Direction directionValue) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton1.setBackgroundColor(Color.TRANSPARENT);
        inactiveButton2.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setDirection(directionValue);
        spg.getSharedPreference(SPType.IPERF3).edit()
                .putString(Iperf3Parameter.DIRECTION, directionValue.toString()).apply();
    }
}