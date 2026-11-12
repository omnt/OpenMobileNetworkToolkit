
/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Executor;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LiveProgress;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3RecyclerViewAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3SparklineView;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

/**
 * iPerf3 screen: a live panel for the running test at the top, the test form (quick settings
 * plus a collapsible "Advanced" section) below, and the history of all tests in a bottom sheet.
 */
public class Iperf3Fragment extends Fragment implements Iperf3RecyclerViewAdapter.Actions {

    private static final String TAG = "Iperf3Fragment";
    private static final int MAX_HISTORY = 4;
    private static final Pattern BYTES_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?[KMGT]?$", Pattern.CASE_INSENSITIVE);
    private static final String USER_TERMINATED = "User Terminated";

    // Core data
    private Iperf3Input iperf3Input;
    private Context ct;
    /** Test started from (or re-attached by) this screen and still running; null when idle. */
    private String currentTestUUID;
    private boolean stopRequested = false;

    // Form
    private View view;
    private NestedScrollView scrollView;
    private LinearLayout form;
    private MaterialButton sendBtn;
    private MaterialAutoCompleteTextView ip, port, bitrate, duration, interval, bytes, parallel, cport;
    private TextInputLayout ipLayout, portLayout, bitrateLayout, durationLayout, intervalLayout,
            bytesLayout, parallelLayout, cportLayout;
    private MaterialButtonToggleGroup mode, protocol, direction;
    private View directionLabel, protocolDurationRow;
    private View advancedHeader, advanced;
    private ImageView advancedChevron;
    private TextView advancedSummary;

    // Live panel
    private View liveCard;
    private ImageView liveStatusIcon;
    private TextView liveStatus, liveTarget, liveUL, liveDL, liveULMean, liveDLMean, liveTime, liveError;
    private View liveULBox, liveDLBox;
    private MaterialButton liveClose, stopBtn;
    private Iperf3SparklineView liveChart;
    private LinearProgressIndicator liveProgress;
    /** Test the live panel shows (also after it finished, until closed); null = hidden. */
    private String panelUUID;
    @Nullable
    private Iperf3Parameter panelParameter;
    private int panelChartIntervals;
    /** Server mode: client session the panel currently shows. */
    private int panelSession;

    // History
    private RecyclerView recyclerView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView historyCount, historyEmpty;

    // Database and adapters
    private Iperf3RecyclerViewAdapter adapter;
    private Iperf3RunResultDao iperf3RunResultDao;
    private SharedPreferencesGrouper spg;
    /** Room writes off the main thread; shared so an Undo after leaving the screen still works. */
    private static final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    // WorkManager state of the running test (pushed, no polling)
    private LiveData<List<WorkInfo>> currentWorkInfos;

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
                if (bottomSheetBehavior != null
                        && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                }
                navController.navigate(R.id.HomeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // ------------------------------------------------------------------ form

    private static int parseInt(String s) {
        try {
            return s == null || s.trim().isEmpty() ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1; // flagged by validateForm()
        }
    }

    private static double parseDouble(String s) {
        try {
            return s == null || s.trim().isEmpty() ? 0 : Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return -1; // flagged by validateForm()
        }
    }

    /**     * Create a text watcher for input fields     */
    private TextWatcher createTextWatcher(TextInputLayout layout, Consumer<String> consumer, String prefKey) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                consumer.accept(s.toString());
                spg.getSharedPreference(SPType.IPERF3).edit().putString(prefKey, s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
                layout.setError(null);
                updateFormDependencies();
            }
        };
    }

    /**     * Setup text watchers for all input fields     */
    private void setupTextWatchers() {
        Iperf3Parameter p = iperf3Input.getParameter();
        ip.addTextChangedListener(createTextWatcher(ipLayout, s -> p.setHost(s.trim()), Iperf3Parameter.HOST));
        port.addTextChangedListener(createTextWatcher(portLayout, s -> p.setPort(Math.max(0, parseInt(s))), Iperf3Parameter.PORT));
        bitrate.addTextChangedListener(createTextWatcher(bitrateLayout, s -> p.setBandwidth(s.trim()), Iperf3Parameter.BITRATE));
        duration.addTextChangedListener(createTextWatcher(durationLayout, s -> p.setTime(Math.max(0, parseInt(s))), Iperf3Parameter.TIME));
        interval.addTextChangedListener(createTextWatcher(intervalLayout, s -> p.setInterval(Math.max(0, parseDouble(s))), Iperf3Parameter.INTERVAL));
        bytes.addTextChangedListener(createTextWatcher(bytesLayout, s -> p.setBytes(s.trim()), Iperf3Parameter.BYTES));
        parallel.addTextChangedListener(createTextWatcher(parallelLayout, s -> p.setParallel(Math.max(0, parseInt(s))), Iperf3Parameter.PARALLEL));
        cport.addTextChangedListener(createTextWatcher(cportLayout, s -> p.setCport(Math.max(0, parseInt(s))), Iperf3Parameter.CPORT));
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

    private void setTextFromSharedPreferences(MaterialAutoCompleteTextView editText, String key) {
        if (spg.getSharedPreference(SPType.IPERF3).contains(key)) {
            editText.setText(spg.getSharedPreference(SPType.IPERF3).getString(key, ""), false);
        }
    }

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

    /** Toggle groups use Material's own checked state; listeners only store the choice. */
    private void setupToggleGroupListeners() {
        mode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            Iperf3Parameter.Iperf3Mode m = checkedId == R.id.iperf3_server_button
                    ? Iperf3Parameter.Iperf3Mode.SERVER : Iperf3Parameter.Iperf3Mode.CLIENT;
            iperf3Input.getParameter().setMode(m);
            spg.getSharedPreference(SPType.IPERF3).edit().putString(Iperf3Parameter.MODE, m.toString()).apply();
            updateFormDependencies();
        });
        protocol.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            Iperf3Parameter.Iperf3Protocol pr = checkedId == R.id.iperf3_udp_button
                    ? Iperf3Parameter.Iperf3Protocol.UDP : Iperf3Parameter.Iperf3Protocol.TCP;
            iperf3Input.getParameter().setProtocol(pr);
            spg.getSharedPreference(SPType.IPERF3).edit().putString(Iperf3Parameter.PROTOCOL, pr.toString()).apply();
            updateFormDependencies();
        });
        direction.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            Iperf3Parameter.Iperf3Direction d;
            if (checkedId == R.id.iperf3_download_button) d = Iperf3Parameter.Iperf3Direction.DOWN;
            else if (checkedId == R.id.iperf3_bidir_button) d = Iperf3Parameter.Iperf3Direction.BIDIR;
            else d = Iperf3Parameter.Iperf3Direction.UP;
            iperf3Input.getParameter().setDirection(d);
            spg.getSharedPreference(SPType.IPERF3).edit().putString(Iperf3Parameter.DIRECTION, d.toString()).apply();
        });
    }

    private <E extends Enum<E>> E savedEnum(Class<E> type, String key, E fallback) {
        try {
            E e = Enum.valueOf(type, spg.getSharedPreference(SPType.IPERF3).getString(key, fallback.name()));
            return e.name().equals("UNDEFINED") ? fallback : e;
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /** Restores mode, protocol and direction; a fresh install starts as TCP upload client. */
    private void loadToggleStates() {
        checkMode(savedEnum(Iperf3Parameter.Iperf3Mode.class, Iperf3Parameter.MODE, Iperf3Parameter.Iperf3Mode.CLIENT));
        checkProtocol(savedEnum(Iperf3Parameter.Iperf3Protocol.class, Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.TCP));
        checkDirection(savedEnum(Iperf3Parameter.Iperf3Direction.class, Iperf3Parameter.DIRECTION, Iperf3Parameter.Iperf3Direction.UP));
    }

    private void checkMode(Iperf3Parameter.Iperf3Mode m) {
        mode.check(m == Iperf3Parameter.Iperf3Mode.SERVER ? R.id.iperf3_server_button : R.id.iperf3_client_button);
    }

    private void checkProtocol(Iperf3Parameter.Iperf3Protocol pr) {
        protocol.check(pr == Iperf3Parameter.Iperf3Protocol.UDP ? R.id.iperf3_udp_button : R.id.iperf3_tcp_button);
    }

    private void checkDirection(Iperf3Parameter.Iperf3Direction d) {
        switch (d) {
            case DOWN: direction.check(R.id.iperf3_download_button); break;
            case BIDIR: direction.check(R.id.iperf3_bidir_button); break;
            default: direction.check(R.id.iperf3_upload_button); break;
        }
    }

    private void setupAdvancedSection() {
        advancedHeader.setOnClickListener(v -> setAdvancedExpanded(advanced.getVisibility() != View.VISIBLE));
    }

    private void setAdvancedExpanded(boolean expand) {
        advanced.setVisibility(expand ? View.VISIBLE : View.GONE);
        advancedChevron.animate().rotation(expand ? 180 : 0).setDuration(150).start();
    }

    private boolean isServerMode() {
        return mode.getCheckedButtonId() == R.id.iperf3_server_button;
    }

    /** Shows only what applies to the current choices, and explains what is ignored. */
    private void updateFormDependencies() {
        if (form == null) return;
        boolean server = isServerMode();
        boolean udp = protocol.getCheckedButtonId() == R.id.iperf3_udp_button;
        boolean bytesSet = !bytes.getText().toString().trim().isEmpty();

        // A server only listens: the target and all test settings come from the client.
        int clientOnly = server ? View.GONE : View.VISIBLE;
        ipLayout.setVisibility(clientOnly);
        directionLabel.setVisibility(clientOnly);
        direction.setVisibility(clientOnly);
        protocolDurationRow.setVisibility(clientOnly);
        parallelLayout.setVisibility(clientOnly);
        bitrateLayout.setVisibility(clientOnly);
        bytesLayout.setVisibility(clientOnly);
        cportLayout.setVisibility(clientOnly);

        // iperf3 has one end condition: a bytes limit replaces the duration.
        durationLayout.setEnabled(!bytesSet);
        bytesLayout.setHelperText(bytesSet ? "Replaces the duration" : "e.g. 100M, 1G");

        bitrateLayout.setHelperText(udp ? "UDP sends 1 Mbit/s if empty" : null);

        sendBtn.setText(server ? "Start server" : "Start test");
        updateAdvancedSummary();
    }

    private void updateAdvancedSummary() {
        List<String> parts = new ArrayList<>();
        if (isServerMode()) parts.add("Server mode");
        String portText = port.getText().toString().trim();
        parts.add("Port " + (portText.isEmpty() ? "5201" : portText));
        if (!isServerMode()) {
            int streams = parseInt(parallel.getText().toString());
            parts.add((streams > 1 ? streams : 1) + (streams > 1 ? " streams" : " stream"));
            String bw = bitrate.getText().toString().trim();
            if (!bw.isEmpty()) parts.add(bw + " Mbit/s");
            String limit = bytes.getText().toString().trim();
            if (!limit.isEmpty()) parts.add(limit.toUpperCase(Locale.ROOT) + " limit");
        }
        String iv = interval.getText().toString().trim();
        if (!iv.isEmpty()) parts.add(iv + " s interval");
        advancedSummary.setText(String.join(" · ", parts));
    }

    /** Checks the form; marks every invalid field. Returns false if the test can't start. */
    private boolean validateForm() {
        boolean ok = true;
        boolean advancedError = false;
        boolean server = isServerMode();

        if (!server && ip.getText().toString().trim().isEmpty()) {
            ipLayout.setError("Enter the iperf3 server");
            ok = false;
        }
        int portValue = parseInt(port.getText().toString());
        if (portValue < 0 || portValue > 65535) {
            portLayout.setError("1–65535");
            ok = false;
            advancedError = true;
        }
        String iv = interval.getText().toString().trim();
        double intervalValue = parseDouble(iv);
        if (!iv.isEmpty() && (intervalValue < 0.1 || intervalValue > 60)) {
            intervalLayout.setError("0.1–60 s");
            ok = false;
            advancedError = true;
        }
        if (!server) {
            boolean bytesSet = !bytes.getText().toString().trim().isEmpty();
            String d = duration.getText().toString().trim();
            int durationValue = parseInt(d);
            if (!bytesSet && !d.isEmpty() && durationValue < 1) {
                durationLayout.setError("At least 1 s");
                ok = false;
            }
            if (bytesSet && !BYTES_PATTERN.matcher(bytes.getText().toString().trim()).matches()) {
                bytesLayout.setError("Number with K, M, G or T");
                ok = false;
                advancedError = true;
            }
            String par = parallel.getText().toString().trim();
            int parallelValue = parseInt(par);
            if (!par.isEmpty() && (parallelValue < 1 || parallelValue > 128)) {
                parallelLayout.setError("1–128");
                ok = false;
                advancedError = true;
            }
            String bw = bitrate.getText().toString().trim();
            if (!bw.isEmpty() && parseDouble(bw) <= 0) {
                bitrateLayout.setError("Greater than 0");
                ok = false;
                advancedError = true;
            }
            String cp = cport.getText().toString().trim();
            int cportValue = parseInt(cp);
            if (!cp.isEmpty() && (cportValue < 1 || cportValue > 65535)) {
                cportLayout.setError("1–65535");
                ok = false;
                advancedError = true;
            }
        }
        if (advancedError) setAdvancedExpanded(true);
        return ok;
    }

    /** Puts a test's parameters into the form (used by Rerun). */
    private void applyParameterToForm(Iperf3Parameter p) {
        checkMode(p.getMode() == Iperf3Parameter.Iperf3Mode.UNDEFINED ? Iperf3Parameter.Iperf3Mode.CLIENT : p.getMode());
        checkProtocol(p.getProtocol());
        checkDirection(p.getDirection());
        ip.setText(p.getHost() == null ? "" : p.getHost(), false);
        port.setText(p.getPort() > 0 ? String.valueOf(p.getPort()) : "", false);
        bitrate.setText(p.getBitrate() == null || p.getBitrate().equals("null") ? "" : p.getBitrate(), false);
        duration.setText(p.getTime() != null && p.getTime() > 0 ? String.valueOf(p.getTime()) : "", false);
        interval.setText(p.getInterval() > 0 ? formatNumber(p.getInterval()) : "", false);
        bytes.setText(p.getBytes() == null || p.getBytes().equals("null") ? "" : p.getBytes(), false);
        parallel.setText(p.getParallel() != null && p.getParallel() > 0 ? String.valueOf(p.getParallel()) : "", false);
        cport.setText(p.getCport() != null && p.getCport() > 0 ? String.valueOf(p.getCport()) : "", false);
    }

    private static String formatNumber(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    // ------------------------------------------------------------------ running a test

    private void startTest() {
        if (currentTestUUID != null) {
            Snackbar.make(view, "A test is already running", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!validateForm()) return;
        saveAllHistories();
        currentTestUUID = UUID.randomUUID().toString();
        stopRequested = false;
        iperf3Input.setTestUUID(currentTestUUID);
        iperf3Input.getParameter().setTestUUID(currentTestUUID);
        iperf3Input.getParameter().updatePaths();
        iperf3Input.setTimestamp(new Timestamp(System.currentTimeMillis()));

        createLogFile();
        // Insert first: the worker updates this row, and the card should exist right away.
        insertTestRecord();
        executeTest();
        showLivePanel(currentTestUUID, iperf3Input.getParameter());
        observeTest(currentTestUUID);
        setRunningUi(true);
        scrollView.smoothScrollTo(0, 0);
    }

    private void createLogFile() {
        File logFile = new File(iperf3Input.getParameter().getRawLogFilePath());
        File rawPath = new File(iperf3Input.getParameter().getRawDirPath());
        if (!rawPath.exists()) {
            rawPath.mkdirs();
        }
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            Log.e(TAG, "Error creating file: ", e);
        }
    }

    private void executeTest() {
        Iperf3Executor iperf3Executor = new Iperf3Executor(iperf3Input, getContext());
        iperf3Executor.execute();
        Log.d(TAG, "Test duration: " + iperf3Input.getParameter().getTime() + "s");
    }

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

    /**
     * Observes the test's workers. WorkManager pushes every state change and every progress
     * update the executor streams (Iperf3LiveProgress) to this observer.
     */
    private void observeTest(String testUUID) {
        if (getView() == null) return; // view already gone (async callers)
        stopObserving();
        currentWorkInfos = WorkManager.getInstance(ct).getWorkInfosByTagLiveData(testUUID);
        currentWorkInfos.observe(getViewLifecycleOwner(), workInfos -> handleWorkUpdates(testUUID, workInfos));
    }

    private void stopObserving() {
        if (currentWorkInfos != null) {
            currentWorkInfos.removeObservers(getViewLifecycleOwner());
            currentWorkInfos = null;
        }
    }

    private void handleWorkUpdates(String testUUID, List<WorkInfo> workInfoList) {
        // Right after enqueueing, the work may not be persisted yet: wait for the next update.
        if (workInfoList == null) return;
        for (WorkInfo workInfo : workInfoList) {
            if (workInfo.getTags().contains(Iperf3ExecutorWorker.class.getCanonicalName())) {
                handleExecutorWorkerUpdate(testUUID, workInfo);
            }
        }
    }

    private void handleExecutorWorkerUpdate(String testUUID, WorkInfo workInfo) {
        switch (workInfo.getState()) {
            case RUNNING:
                Data progress = workInfo.getProgress();
                if (Iperf3LiveProgress.isLive(progress)) {
                    Log.d(TAG, "Progress: " + progress.getString(Iperf3LiveProgress.INTERVAL_JSON));
                    if (testUUID.equals(panelUUID)) updateLivePanel(progress);
                }
                break;
            case SUCCEEDED:
                // The test is done; line protocol / upload run on without blocking the form.
                adapter.clearLiveProgress(testUUID);
                stopObserving();
                testFinished();
                break;
            case CANCELLED:
            case FAILED:
                Log.d(TAG, "Executor worker state: " + workInfo.getState());
                adapter.clearLiveProgress(testUUID);
                markTestFailed(testUUID);
                stopObserving();
                testFinished();
                break;
        }
    }

    private void markTestFailed(String testUUID) {
        iperf3RunResultDao.updateResult(testUUID, -1);
        RemoteWorkManager.getInstance(ct).cancelAllWorkByTag(testUUID);
    }

    private void stopCurrentTest() {
        if (currentTestUUID == null) {
            Log.w(TAG, "No test running to stop");
            return;
        }
        Log.d(TAG, "Stopping test: " + currentTestUUID);
        stopRequested = true;
        RemoteWorkManager.getInstance(ct).cancelAllWorkByTag(currentTestUUID);
        iperf3RunResultDao.updateResult(currentTestUUID, -1);
        adapter.clearLiveProgress(currentTestUUID);
        stopObserving();
        testFinished();
    }

    /** Called when test finishes (success, failure, or manual stop). */
    private void testFinished() {
        currentTestUUID = null;
        setRunningUi(false);
    }

    /** While a test runs the form is hidden and the live panel shows the Stop button. */
    private void setRunningUi(boolean running) {
        form.setVisibility(running ? View.GONE : View.VISIBLE);
        stopBtn.setVisibility(running ? View.VISIBLE : View.GONE);
        liveClose.setVisibility(running || panelUUID == null ? View.GONE : View.VISIBLE);
    }

    // ------------------------------------------------------------------ live panel

    private void showLivePanel(String testUUID, @Nullable Iperf3Parameter p) {
        panelUUID = testUUID;
        panelParameter = p;
        panelChartIntervals = 0;
        panelSession = 0;
        liveChart.clear();
        liveCard.setVisibility(View.VISIBLE);
        liveError.setVisibility(View.GONE);
        liveUL.setText("–");
        liveDL.setText("–");
        liveULMean.setText("");
        liveDLMean.setText("");
        liveTime.setText("");
        setLiveStatus(R.drawable.ic_iperf3_running, androidx.appcompat.R.attr.colorPrimary,
                p != null && p.getMode() == Iperf3Parameter.Iperf3Mode.SERVER ? "LISTENING" : "CONNECTING");
        liveProgress.setVisibility(View.GONE);
        liveProgress.setIndeterminate(true);
        liveProgress.setVisibility(View.VISIBLE);
        bindLiveTarget();
    }

    private void bindLiveTarget() {
        if (panelParameter == null) {
            liveTarget.setText("");
            return;
        }
        Iperf3Parameter p = panelParameter;
        if (p.getMode() == Iperf3Parameter.Iperf3Mode.SERVER) {
            // The client chooses protocol and direction: show both directions.
            liveTarget.setText("Server listening on port " + (p.getPort() > 0 ? p.getPort() : 5201)
                    + " · waiting for a client");
            liveULBox.setVisibility(View.VISIBLE);
            liveDLBox.setVisibility(View.VISIBLE);
            return;
        }
        String target = String.format(Locale.getDefault(), "%s:%d", p.getHost(), p.getPort() > 0 ? p.getPort() : 5201);
        String dir = p.getDirection() == Iperf3Parameter.Iperf3Direction.BIDIR ? "both directions"
                : p.getDirection() == Iperf3Parameter.Iperf3Direction.DOWN ? "download" : "upload";
        liveTarget.setText(String.format(Locale.getDefault(), "%s · %s · %s", target, p.getProtocol().name(), dir));
        liveULBox.setVisibility(p.getDirection() != Iperf3Parameter.Iperf3Direction.DOWN ? View.VISIBLE : View.GONE);
        liveDLBox.setVisibility(p.getDirection() != Iperf3Parameter.Iperf3Direction.UP ? View.VISIBLE : View.GONE);
    }

    private void hideLivePanel() {
        panelUUID = null;
        panelParameter = null;
        liveCard.setVisibility(View.GONE);
    }

    private void setLiveStatus(int icon, int colorAttr, String text) {
        int color = MaterialColors.getColor(liveCard, colorAttr);
        setLiveStatusColor(icon, color, text);
    }

    private void setLiveStatusColor(int icon, int color, String text) {
        liveStatusIcon.setImageResource(icon);
        liveStatusIcon.setImageTintList(ColorStateList.valueOf(color));
        liveStatus.setText(text);
    }

    /** One progress update of the running test. */
    private void updateLivePanel(Data progress) {
        if (Iperf3LiveProgress.isWaiting(progress)) {
            showServerWaiting(progress);
            return;
        }
        if (!Iperf3LiveProgress.isRunning(progress)) return; // still connecting
        boolean server = isPanelServer();
        if (server) {
            // A client connected: a new session starts with an empty chart.
            int session = Iperf3LiveProgress.getSession(progress);
            if (session != panelSession) {
                panelSession = session;
                panelChartIntervals = 0;
                liveChart.clear();
                liveUL.setText("–");
                liveDL.setText("–");
                liveULMean.setText("");
                liveDLMean.setText("");
            }
            String client = Iperf3LiveProgress.getClient(progress);
            liveTarget.setText(String.format(Locale.getDefault(), "Client %d: %s", session,
                    client != null ? client : "connected"));
            liveError.setVisibility(View.GONE);
            setLiveStatus(R.drawable.ic_iperf3_running, androidx.appcompat.R.attr.colorPrimary, "CLIENT CONNECTED");
        } else {
            setLiveStatus(R.drawable.ic_iperf3_running, androidx.appcompat.R.attr.colorPrimary, "RUNNING");
        }
        MetricCalculator ul = Iperf3LiveProgress.getUL(progress);
        MetricCalculator dl = Iperf3LiveProgress.getDL(progress);
        if (ul != null) {
            liveUL.setText(mbps(ul.getLast()));
            liveULMean.setText("mean " + mbps(ul.getMean()) + " · max " + mbps(ul.getMax()));
        }
        if (dl != null) {
            liveDL.setText(mbps(dl.getLast()));
            liveDLMean.setText("mean " + mbps(dl.getMean()) + " · max " + mbps(dl.getMax()));
        }
        int intervals = Iperf3LiveProgress.getIntervals(progress);
        if (intervals > panelChartIntervals) {
            panelChartIntervals = intervals;
            liveChart.add(ul != null ? ul.getLast() : null, dl != null ? dl.getLast() : null);
        }
        if (panelParameter == null) return;
        double step = panelParameter.getInterval() > 0 ? panelParameter.getInterval() : 1.0;
        boolean bytesLimit = panelParameter.getBytes() != null && !panelParameter.getBytes().trim().isEmpty();
        int total = panelParameter.getTime() != null && panelParameter.getTime() > 0 ? panelParameter.getTime() : 10;
        if (server) {
            // The client decides how long the test runs.
            liveTime.setText(String.format(Locale.getDefault(), "%s s", formatNumber(intervals * step)));
        } else if (bytesLimit) {
            liveTime.setText(String.format(Locale.getDefault(), "%s s · until %s are sent",
                    formatNumber(intervals * step), panelParameter.getBytes()));
        } else {
            int expected = (int) Math.ceil(total / step);
            if (liveProgress.isIndeterminate()) {
                liveProgress.setVisibility(View.GONE);
                liveProgress.setIndeterminate(false);
                liveProgress.setVisibility(View.VISIBLE);
            }
            liveProgress.setMax(expected);
            liveProgress.setProgressCompat(Math.min(intervals, expected), true);
            liveTime.setText(String.format(Locale.getDefault(), "%s / %d s",
                    formatNumber(Math.min(intervals * step, total)), total));
        }
    }

    private boolean isPanelServer() {
        return panelParameter != null && panelParameter.getMode() == Iperf3Parameter.Iperf3Mode.SERVER;
    }

    /** Server mode: no client connected; shows the last client's mean values, if any. */
    private void showServerWaiting(Data progress) {
        int sessions = Iperf3LiveProgress.getSession(progress);
        panelSession = sessions;
        setLiveStatus(R.drawable.ic_iperf3_running, androidx.appcompat.R.attr.colorPrimary, "LISTENING");
        int port = panelParameter != null && panelParameter.getPort() > 0 ? panelParameter.getPort() : 5201;
        liveTarget.setText(sessions == 0
                ? "Waiting for a client on port " + port
                : String.format(Locale.getDefault(), "Waiting for the next client on port %d · %d %s so far",
                        port, sessions, sessions == 1 ? "client" : "clients"));
        MetricCalculator ul = Iperf3LiveProgress.getUL(progress);
        MetricCalculator dl = Iperf3LiveProgress.getDL(progress);
        liveUL.setText(ul != null ? mbps(ul.getMean()) : "–");
        liveDL.setText(dl != null ? mbps(dl.getMean()) : "–");
        liveULMean.setText(ul != null ? "last client · mean" : "");
        liveDLMean.setText(dl != null ? "last client · mean" : "");
        String error = Iperf3LiveProgress.getSessionError(progress);
        if (error != null) {
            liveError.setText("Last client: " + error);
            liveError.setVisibility(View.VISIBLE);
        } else {
            liveError.setVisibility(View.GONE);
        }
        liveTime.setText("");
        if (!liveProgress.isIndeterminate()) {
            liveProgress.setVisibility(View.GONE);
            liveProgress.setIndeterminate(true);
        }
        liveProgress.setVisibility(View.VISIBLE);
    }

    /** The panel's test has a final result in Room. */
    private void showLivePanelResult(Iperf3RunResult run) {
        if (panelParameter == null) {
            panelParameter = run.input.getParameter();
            bindLiveTarget();
        }
        liveProgress.setVisibility(View.GONE);
        boolean stopped = stopRequested
                || (run.error != null && USER_TERMINATED.equals(run.error.getError()));
        boolean server = run.input.getParameter().getMode() == Iperf3Parameter.Iperf3Mode.SERVER;
        if (server && run.result == 0) {
            // Stopping is how a server ends; the values are the mean over all its clients.
            setLiveStatus(R.drawable.ic_iperf3_stop, com.google.android.material.R.attr.colorOnPrimaryContainer, "SERVER STOPPED");
            liveError.setVisibility(View.GONE);
            liveTarget.setText(panelSession == 0 ? "No client connected"
                    : String.format(Locale.getDefault(), "%d %s served", panelSession, panelSession == 1 ? "client" : "clients"));
        } else if (run.result == 0) {
            setLiveStatusColor(R.drawable.ic_iperf3_success, ct.getColor(R.color.iperf3_success), "DONE");
            liveError.setVisibility(View.GONE);
        } else if (stopped) {
            setLiveStatus(R.drawable.ic_iperf3_stop, com.google.android.material.R.attr.colorOnPrimaryContainer, "STOPPED");
            liveError.setVisibility(View.GONE);
        } else {
            setLiveStatus(R.drawable.ic_iperf3_failed, androidx.appcompat.R.attr.colorError, "FAILED");
            liveError.setText(run.error != null && run.error.getError() != null ? run.error.getError() : "The test failed.");
            liveError.setVisibility(View.VISIBLE);
        }
        // Final values: the mean over the whole test.
        if (run.metricUL != null && !run.metricUL.getMeanList().isEmpty()) {
            run.metricUL.calcAll();
            liveUL.setText(mbps(run.metricUL.getMean()));
            liveULMean.setText("mean · max " + mbps(run.metricUL.getMax()));
        }
        if (run.metricDL != null && !run.metricDL.getMeanList().isEmpty()) {
            run.metricDL.calcAll();
            liveDL.setText(mbps(run.metricDL.getMean()));
            liveDLMean.setText("mean · max " + mbps(run.metricDL.getMax()));
        }
        if (run.result == 0 && !server && run.intervals != null) {
            liveTime.setText(run.intervals.getIntervalArrayList().size() + " intervals");
        }
        setRunningUi(currentTestUUID != null);
    }

    private static String mbps(double bitsPerSecond) {
        double m = bitsPerSecond / 1e6;
        if (m >= 100) return String.format(Locale.getDefault(), "%.0f", m);
        if (m >= 10) return String.format(Locale.getDefault(), "%.1f", m);
        return String.format(Locale.getDefault(), "%.2f", m);
    }

    // ------------------------------------------------------------------ history actions

    @Override
    public void onRerun(@NonNull Iperf3RunResult run) {
        if (currentTestUUID != null) {
            Snackbar.make(view, "Stop the running test first", Snackbar.LENGTH_SHORT).show();
            return;
        }
        applyParameterToForm(run.input.getParameter());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        startTest();
    }

    @Override
    public void onDelete(@NonNull Iperf3RunResult run) {
        if (run.result == -100) {
            Snackbar.make(view, "Stop the test before deleting it", Snackbar.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged(); // undo a swipe
            return;
        }
        if (run.uid.equals(panelUUID)) hideLivePanel();
        dbExecutor.execute(() -> iperf3RunResultDao.delete(run.uid));
        Snackbar.make(view, "Test deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> dbExecutor.execute(() -> iperf3RunResultDao.insert(run)))
                .show();
    }

    @Override
    public void onCopyCommand(@NonNull Iperf3RunResult run) {
        StringBuilder cmd = new StringBuilder("iperf3");
        for (String arg : run.input.getParameter().getInputAsCommand()) {
            cmd.append(' ').append(arg.matches("[A-Za-z0-9_./:=,@%+-]+") ? arg : "'" + arg.replace("'", "'\\''") + "'");
        }
        ClipboardManager clipboard = (ClipboardManager) ct.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("iperf3 command", cmd.toString()));
        // Android 13+ shows its own confirmation.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Snackbar.make(view, "Command copied", Snackbar.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------------ setup

    private void setupUIComponents(View rootView) {
        scrollView = rootView.findViewById(R.id.iperf3_scroll);
        form = rootView.findViewById(R.id.iperf3_form);
        sendBtn = rootView.findViewById(R.id.iperf3_send);

        ip = rootView.findViewById(R.id.iperf3_ip);
        port = rootView.findViewById(R.id.iperf3_port);
        bitrate = rootView.findViewById(R.id.iperf3_bandwidth);
        duration = rootView.findViewById(R.id.iperf3_duration);
        interval = rootView.findViewById(R.id.iperf3_interval);
        bytes = rootView.findViewById(R.id.iperf3_bytes);
        parallel = rootView.findViewById(R.id.iperf3_parallel);
        cport = rootView.findViewById(R.id.iperf3_cport);
        ipLayout = rootView.findViewById(R.id.iperf3_ip_layout);
        portLayout = rootView.findViewById(R.id.iperf3_port_layout);
        bitrateLayout = rootView.findViewById(R.id.iperf3_bandwidth_layout);
        durationLayout = rootView.findViewById(R.id.iperf3_duration_layout);
        intervalLayout = rootView.findViewById(R.id.iperf3_interval_layout);
        bytesLayout = rootView.findViewById(R.id.iperf3_bytes_layout);
        parallelLayout = rootView.findViewById(R.id.iperf3_parallel_layout);
        cportLayout = rootView.findViewById(R.id.iperf3_cport_layout);

        mode = rootView.findViewById(R.id.iperf3_mode_toggle_group);
        protocol = rootView.findViewById(R.id.iperf3_protocol_toggle_group);
        direction = rootView.findViewById(R.id.iperf3_direction_toggle_group);
        directionLabel = rootView.findViewById(R.id.iperf3_direction_label);
        protocolDurationRow = rootView.findViewById(R.id.iperf3_protocol_duration_row);
        advancedHeader = rootView.findViewById(R.id.iperf3_advanced_header);
        advanced = rootView.findViewById(R.id.iperf3_advanced);
        advancedChevron = rootView.findViewById(R.id.iperf3_advanced_chevron);
        advancedSummary = rootView.findViewById(R.id.iperf3_advanced_summary);

        liveCard = rootView.findViewById(R.id.iperf3_live_card);
        liveStatusIcon = rootView.findViewById(R.id.iperf3_live_status_icon);
        liveStatus = rootView.findViewById(R.id.iperf3_live_status);
        liveClose = rootView.findViewById(R.id.iperf3_live_close);
        liveTarget = rootView.findViewById(R.id.iperf3_live_target);
        liveULBox = rootView.findViewById(R.id.iperf3_live_ul_box);
        liveDLBox = rootView.findViewById(R.id.iperf3_live_dl_box);
        liveUL = rootView.findViewById(R.id.iperf3_live_ul);
        liveDL = rootView.findViewById(R.id.iperf3_live_dl);
        liveULMean = rootView.findViewById(R.id.iperf3_live_ul_mean);
        liveDLMean = rootView.findViewById(R.id.iperf3_live_dl_mean);
        liveChart = rootView.findViewById(R.id.iperf3_live_chart);
        liveProgress = rootView.findViewById(R.id.iperf3_live_progress);
        liveTime = rootView.findViewById(R.id.iperf3_live_time);
        liveError = rootView.findViewById(R.id.iperf3_live_error);
        stopBtn = rootView.findViewById(R.id.iperf3_stop);

        recyclerView = rootView.findViewById(R.id.runners_list);
        historyCount = rootView.findViewById(R.id.iperf3_history_count);
        historyEmpty = rootView.findViewById(R.id.iperf3_history_empty);

        sendBtn.setOnClickListener(v -> startTest());
        stopBtn.setOnClickListener(v -> stopCurrentTest());
        liveClose.setOnClickListener(v -> hideLivePanel());
    }

    private void setupBottomSheet(View rootView) {
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.standard_bottom_sheet));
        // Drag handle + "History" header stay visible.
        bottomSheetBehavior.setPeekHeight((int) (80 * getResources().getDisplayMetrics().density));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);
        rootView.findViewById(R.id.iperf3_history_header).setOnClickListener(v ->
                bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                        ? BottomSheetBehavior.STATE_COLLAPSED : BottomSheetBehavior.STATE_EXPANDED));
    }

    private void setupDatabase() {
        Iperf3ResultsDataBase db = Iperf3ResultsDataBase.getDatabase(ct);
        iperf3RunResultDao = db.iperf3RunResultDao();
    }

    private void setupRecyclerView() {
        adapter = new Iperf3RecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Swipe a finished test away to delete it (undo via the snackbar).
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || adapter.getItem(pos).result == -100) return 0;
                return super.getSwipeDirs(rv, vh);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) onDelete(adapter.getItem(pos));
            }
        }).attachToRecyclerView(recyclerView);

        // History from Room; changes only when a run is inserted or updated (start/end).
        iperf3RunResultDao.getAll().observe(getViewLifecycleOwner(), runs -> {
            adapter.setItems(runs);
            int n = runs == null ? 0 : runs.size();
            historyCount.setText(n == 0 ? "" : n + (n == 1 ? " test" : " tests"));
            historyEmpty.setVisibility(n == 0 ? View.VISIBLE : View.GONE);
            if (panelUUID != null && runs != null) {
                for (Iperf3RunResult run : runs) {
                    if (!run.uid.equals(panelUUID)) continue;
                    if (run.result != -100) {
                        showLivePanelResult(run);
                    } else if (panelParameter == null) {
                        panelParameter = run.input.getParameter();
                        bindLiveTarget();
                    }
                    break;
                }
            }
        });
        observeAllExecutors();
    }

    /**
     * Live values for every running iPerf3 test, also those started via MQTT: each executor
     * streams its state as WorkManager progress (Iperf3LiveProgress), which WorkManager pushes
     * here. WorkManager tags every request with its worker class name.
     */
    private void observeAllExecutors() {
        WorkManager.getInstance(ct)
                .getWorkInfosByTagLiveData(Iperf3ExecutorWorker.class.getName())
                .observe(getViewLifecycleOwner(), workInfos -> {
                    for (WorkInfo workInfo : workInfos) {
                        if (workInfo.getState() == WorkInfo.State.RUNNING) {
                            Data progress = workInfo.getProgress();
                            if (Iperf3LiveProgress.isLive(progress)) {
                                adapter.setLiveProgress(Iperf3LiveProgress.getTestUUID(progress), progress);
                            }
                        } else if (workInfo.getState().isFinished()) {
                            for (String tag : workInfo.getTags()) {
                                if (tag.length() == 36) adapter.clearLiveProgress(tag); // test UUID tag
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_iperf3_input, container, false);

        initializeCoreComponents();
        setupUIComponents(view);
        setupDatabase();
        setupBottomSheet(view);
        setupRecyclerView();

        setupTextWatchers();
        setupToggleGroupListeners();
        setupAdvancedSection();
        loadPreferences();
        loadToggleStates();
        loadAllHistories();
        updateFormDependencies();
        setRunningUi(false);

        checkForRunningTests();
        return view;
    }

    /** Re-attaches the live panel to a test that was started before this screen was opened. */
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
                        if (getView() == null) return;
                        for (WorkInfo workInfo : result) {
                            // MQTT tests (tagged with Iperf3ExecutorWorker.TAG) only show in the history.
                            if (workInfo.getTags().contains(Iperf3ExecutorWorker.TAG)) continue;
                            if (workInfo.getState() != WorkInfo.State.RUNNING) continue;
                            if (workInfo.getTags().contains(Iperf3ExecutorWorker.class.getCanonicalName())) {
                                for (String tag : workInfo.getTags()) {
                                    if (tag.length() == 36) { // the test UUID tag
                                        currentTestUUID = tag;
                                        showLivePanel(tag, null);
                                        observeTest(tag);
                                        setRunningUi(true);
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
                ct.getMainExecutor()
        );
    }

    private void initializeCoreComponents() {
        String iperf3UUID = UUID.randomUUID().toString();
        Iperf3Parameter iperf3Parameter = new Iperf3Parameter(iperf3UUID);
        iperf3Input = new Iperf3Input(iperf3Parameter, "");
        spg = SharedPreferencesGrouper.getInstance(ct);
    }

    @Override
    public void onResume() {
        super.onResume();
        view.requestLayout();
    }
}
