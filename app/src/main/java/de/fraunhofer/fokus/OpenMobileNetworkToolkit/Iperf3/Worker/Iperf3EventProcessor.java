/*
 * SPDX-FileCopyrightText: 2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;

/**
 * Turns the iperf3 --json-stream events of one test into app state.
 *
 * <ul>
 *   <li>Every line is appended to the test's raw log file, exactly as iperf3's --logfile used
 *       to write it.</li>
 *   <li>Live state goes to the {@link Listener} once per event (streamed to the UI).</li>
 *   <li>Room gets "start" and "error" as they arrive, and the intervals, metrics and final
 *       result once in {@link #finish}. The line protocol is generated from exactly these
 *       stored objects, so the parsing here must stay as it was in the old monitor worker.</li>
 * </ul>
 *
 * Not thread safe; call it from a single thread.
 */
public class Iperf3EventProcessor {
    private static final String TAG = "Iperf3EventProcessor";
    /** iperf3Run() result when the worker process is still busy with another test (jni/iperf3_jni.c). */
    public static final int RESULT_BUSY = -2;

    public interface Listener {
        /**
         * iperf3 reported "start": as a client, the connection to the server is up; as a server,
         * a client connected (a new session).
         *
         * @param client description of the connected client (server mode), else null
         */
        void onStart(int session, @Nullable String client);

        /** A new interval was parsed; the metrics are those of the current session. */
        void onInterval(@NonNull Interval interval, @NonNull MetricCalculator ul,
                        @NonNull MetricCalculator dl, int intervalCount, @NonNull String intervalJson);

        /**
         * Server mode: the client of this session is gone ("end"); the server waits for the next.
         *
         * @param error why the session ended early, or null
         */
        void onSessionEnd(int session, @NonNull MetricCalculator ul, @NonNull MetricCalculator dl,
                          @Nullable String error);
    }

    private final Iperf3Input iperf3Input;
    private final String testUUID;
    private final Iperf3RunResultDao dao;
    private final Listener listener;

    private final Intervals intervals = new Intervals();
    private final MetricCalculator metricUL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
    private final MetricCalculator metricDL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
    @Nullable
    private OutputStream rawLog;

    private boolean startReceived = false;
    private boolean endReceived = false;
    private boolean errorReceived = false;

    /** As a server, every connected client is a session; a client test is one session. */
    private final boolean serverMode;
    private int session = 0;
    private int sessionIntervals = 0;
    private MetricCalculator sessionUL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
    private MetricCalculator sessionDL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
    @Nullable
    private String sessionError;

    public Iperf3EventProcessor(@NonNull Iperf3Input iperf3Input, @NonNull Iperf3RunResultDao dao,
                                @NonNull Listener listener) {
        this.iperf3Input = iperf3Input;
        this.testUUID = iperf3Input.getTestUUID();
        this.dao = dao;
        this.listener = listener;
        this.serverMode = iperf3Input.getParameter().getMode() == Iperf3Parameter.Iperf3Mode.SERVER;
        openRawLog();
    }

    private void openRawLog() {
        try {
            File dir = new File(iperf3Input.getParameter().getRawDirPath());
            if (!dir.exists() && !dir.mkdirs()) {
                Log.w(TAG, "could not create " + dir);
            }
            rawLog = new FileOutputStream(iperf3Input.getParameter().getRawLogFilePath(), true);
        } catch (IOException e) {
            Log.e(TAG, "cannot open raw log file", e);
            rawLog = null;
        }
    }

    private void appendRawLog(String line) {
        if (rawLog == null) return;
        try {
            rawLog.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            rawLog.flush();
        } catch (IOException e) {
            Log.e(TAG, "writing raw log failed", e);
        }
    }

    public boolean isStartReceived() {
        return startReceived;
    }

    /** Handles one iperf3 --json-stream line. */
    public void handle(@NonNull String line) {
        appendRawLog(line);

        JSONObject obj;
        String event;
        try {
            obj = new JSONObject(line);
            event = obj.getString("event");
        } catch (JSONException e) {
            Log.w(TAG, "not an iperf3 json stream event: " + line);
            return;
        }
        Log.d(TAG, "got event: " + event);

        switch (event) {
            case "start":
                handleStart(obj);
                break;
            case "interval":
                handleInterval(obj);
                break;
            case "end":
                endReceived = true;
                if (serverMode && session > 0) {
                    listener.onSessionEnd(session, sessionUL, sessionDL, sessionError);
                }
                break;
            case "error":
                handleError(obj);
                break;
            default:
                Log.d(TAG, "ignoring event " + event);
                break;
        }
    }

    private void handleStart(JSONObject obj) {
        Start start = new Start();
        JSONObject startData = null;
        try {
            startData = obj.getJSONObject("data");
        } catch (JSONException e) {
            Log.e(TAG, "parse: getting start failed", e);
        }
        try {
            start.parseStart(startData);
        } catch (JSONException e) {
            Log.e(TAG, "parse: parsing start failed", e);
        }
        dao.updateStart(testUUID, start.toString());
        startReceived = true;

        session++;
        sessionIntervals = 0;
        sessionUL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        sessionDL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        sessionError = null;
        listener.onStart(session, serverMode ? describeClient(startData) : null);
    }

    /**
     * Server mode: "192.168.1.5 · UDP · both directions · 4 streams · 10 s", from the start
     * event's "connected" and "test_start" (directions from this device's point of view).
     */
    @Nullable
    static String describeClient(@Nullable JSONObject startData) {
        if (startData == null) return null;
        StringBuilder sb = new StringBuilder();
        JSONArray connected = startData.optJSONArray("connected");
        JSONObject first = connected != null ? connected.optJSONObject(0) : null;
        sb.append(first != null ? first.optString("remote_host", "client") : "client");
        JSONObject ts = startData.optJSONObject("test_start");
        if (ts != null) {
            sb.append(" · ").append(ts.optString("protocol", "?"));
            if (ts.optInt("bidir", 0) != 0) sb.append(" · both directions");
            else if (ts.optInt("reverse", 0) != 0) sb.append(" · upload"); // the server sends
            else sb.append(" · download"); // the server receives
            int streams = ts.optInt("num_streams", 1);
            sb.append(" · ").append(streams).append(streams == 1 ? " stream" : " streams");
            int duration = ts.optInt("duration", 0);
            if (duration > 0) sb.append(" · ").append(duration).append(" s");
        }
        return sb.toString();
    }

    private void handleInterval(JSONObject obj) {
        Interval interval = new Interval();
        JSONObject intervalData = null;
        try {
            intervalData = obj.getJSONObject("data");
            interval.parse(intervalData);
        } catch (JSONException e) {
            Log.e(TAG, "parse: parsing interval failed", e);
        }
        try {
            // Upload = sent by this device, download = received, decided by each sum's own sender
            // flag: as a server, "sum" is what it receives and "sum_bidir_reverse" what it sends.
            addToMetric(interval.getSum());
            if (interval.getSumBidirReverse() != null) {
                addToMetric(interval.getSumBidirReverse());
            }
        } catch (RuntimeException e) {
            // Same outcome as in the old monitor: an interval without a usable sum is dropped.
            Log.e(TAG, "interval without sum, dropping it", e);
            return;
        }
        intervals.addInterval(interval);
        sessionIntervals++;
        listener.onInterval(interval, sessionUL, sessionDL, sessionIntervals,
                intervalData != null ? intervalData.toString() : "");
    }

    private void addToMetric(Sum sum) {
        if (sum.getSender()) {
            metricUL.update(sum.getBits_per_second());
            sessionUL.update(sum.getBits_per_second());
        } else {
            metricDL.update(sum.getBits_per_second());
            sessionDL.update(sum.getBits_per_second());
        }
    }

    private void handleError(JSONObject obj) {
        if (serverMode && session > 0) {
            // A client's session failed (e.g. "the client has terminated"); the server itself
            // keeps running and waits for the next client.
            sessionError = obj.optString("data", null);
            Log.d(TAG, "session " + session + " ended with error: " + sessionError);
            return;
        }
        errorReceived = true;
        dao.updateResult(testUUID, -1);
        Error error = new Error();
        try {
            error.parse(obj.getString("data"));
        } catch (JSONException e) {
            Log.e(TAG, "run: getting error failed!", e);
            return;
        }
        Log.d(TAG, "got error! " + error);
        dao.updateError(testUUID, error);
    }

    /**
     * Stores the run in Room once iperf3 is done. Called exactly once, after the last event.
     *
     * @param returnCode iperf3's exit code
     * @param stopped    the user (or the system) stopped the test
     * @return the result code stored (0 = success, -1 = failure)
     */
    public int finish(int returnCode, boolean stopped) {
        if (rawLog != null) {
            try {
                rawLog.close();
            } catch (IOException ignored) {
            }
            rawLog = null;
        }

        dao.updateIntervals(testUUID, intervals);
        if (!intervals.getIntervalArrayList().isEmpty()) {
            metricUL.calcAll();
            metricDL.calcAll();
            dao.updateMetricUL(testUUID, metricUL);
            dao.updateMetricDL(testUUID, metricDL);
        }

        int result;
        if (serverMode && stopped) {
            // Stopping is how a server normally ends.
            result = 0;
        } else if (stopped) {
            result = -1;
            Error stopError = new Error();
            stopError.setError("User Terminated");
            dao.updateError(testUUID, stopError);
        } else if (errorReceived) {
            result = -1; // the error itself is already stored
        } else if (endReceived && returnCode == 0) {
            result = 0;
        } else {
            result = -1;
            Error noResult = new Error();
            noResult.setError(returnCode == RESULT_BUSY
                    ? "The previous iPerf3 test is still stopping. Try again in a few seconds."
                    : "iperf3 exited without result (return code " + returnCode + ")");
            dao.updateError(testUUID, noResult);
        }
        dao.updateResult(testUUID, result);
        Log.d(TAG, "finish: returnCode=" + returnCode + " stopped=" + stopped + " end=" + endReceived
                + " error=" + errorReceived + " intervals=" + intervals.getIntervalArrayList().size()
                + " -> result " + result);
        return result;
    }
}
