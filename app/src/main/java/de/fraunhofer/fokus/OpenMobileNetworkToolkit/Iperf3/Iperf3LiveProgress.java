/*
 * SPDX-FileCopyrightText: 2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;

/**
 * Live state of a running iPerf3 test, streamed from the executor worker to the UI as WorkManager
 * progress (one update per iperf3 event). Only fixed-size summaries go in here: WorkManager's
 * {@link Data} is limited to 10 KB. The full history (intervals, final metrics) goes to Room once
 * the test is finished.
 */
public final class Iperf3LiveProgress {
    public static final String TEST_UUID = "live_test_uuid";
    public static final String STATE = "live_state";
    public static final String STATE_CONNECTING = "CONNECTING";
    public static final String STATE_RUNNING = "RUNNING";
    /** Server mode: listening, no client connected (UL/DL then hold the last session's values). */
    public static final String STATE_WAITING = "WAITING";

    /** Server mode: number of the current (or last) client session, starting at 1. */
    public static final String SESSION = "live_session";
    /** Server mode: the connected client ("192.168.1.5 · TCP · upload · 1 stream · 10 s"). */
    public static final String CLIENT = "live_client";
    /** Server mode, WAITING: why the last session ended early, if it did. */
    public static final String SESSION_ERROR = "live_session_error";

    /** Number of intervals received so far. */
    public static final String INTERVALS = "live_intervals";
    /** Raw iperf3 JSON of the last interval; only set when it is small enough for {@link Data}. */
    public static final String INTERVAL_JSON = "interval";
    private static final int MAX_INTERVAL_JSON_LENGTH = 6 * 1024;

    private static final String UL = "live_ul_";
    private static final String DL = "live_dl_";
    private static final String COUNT = "count";
    private static final String LAST = "last";
    private static final String MEAN = "mean";
    private static final String MEDIAN = "median";
    private static final String MIN = "min";
    private static final String MAX = "max";

    private Iperf3LiveProgress() {
    }

    public static Data connecting(@NonNull String testUUID) {
        return new Data.Builder()
                .putString(TEST_UUID, testUUID)
                .putString(STATE, STATE_CONNECTING)
                .build();
    }

    /** Server mode: listening for the next client; ul/dl are the last session's (or null). */
    public static Data waiting(@NonNull String testUUID, int lastSession, @Nullable MetricCalculator ul,
                               @Nullable MetricCalculator dl, @Nullable String lastError) {
        Data.Builder b = new Data.Builder()
                .putString(TEST_UUID, testUUID)
                .putString(STATE, STATE_WAITING)
                .putInt(SESSION, lastSession);
        if (ul != null) putMetric(b, UL, ul);
        if (dl != null) putMetric(b, DL, dl);
        if (lastError != null) b.putString(SESSION_ERROR, lastError);
        return b.build();
    }

    public static Data running(@NonNull String testUUID, int intervals, @NonNull MetricCalculator ul,
                               @NonNull MetricCalculator dl, @Nullable String intervalJson) {
        return running(testUUID, 0, null, intervals, ul, dl, intervalJson);
    }

    public static Data running(@NonNull String testUUID, int session, @Nullable String client,
                               int intervals, @NonNull MetricCalculator ul,
                               @NonNull MetricCalculator dl, @Nullable String intervalJson) {
        Data.Builder b = new Data.Builder()
                .putString(TEST_UUID, testUUID)
                .putString(STATE, STATE_RUNNING)
                .putInt(SESSION, session)
                .putInt(INTERVALS, intervals);
        putMetric(b, UL, ul);
        putMetric(b, DL, dl);
        if (client != null) b.putString(CLIENT, client);
        if (intervalJson != null && intervalJson.length() <= MAX_INTERVAL_JSON_LENGTH) {
            b.putString(INTERVAL_JSON, intervalJson);
        }
        return b.build();
    }

    public static boolean isLive(@Nullable Data data) {
        return data != null && data.getString(STATE) != null && data.getString(TEST_UUID) != null;
    }

    @Nullable
    public static String getTestUUID(@NonNull Data data) {
        return data.getString(TEST_UUID);
    }

    public static boolean isRunning(@Nullable Data data) {
        return data != null && STATE_RUNNING.equals(data.getString(STATE));
    }

    /** Server mode: listening, no client connected right now. */
    public static boolean isWaiting(@Nullable Data data) {
        return data != null && STATE_WAITING.equals(data.getString(STATE));
    }

    public static int getSession(@NonNull Data data) {
        return data.getInt(SESSION, 0);
    }

    @Nullable
    public static String getClient(@NonNull Data data) {
        return data.getString(CLIENT);
    }

    @Nullable
    public static String getSessionError(@NonNull Data data) {
        return data.getString(SESSION_ERROR);
    }

    public static int getIntervals(@NonNull Data data) {
        return data.getInt(INTERVALS, 0);
    }

    /** Upload metric as shown while the test runs, or null if none was received yet. */
    @Nullable
    public static MetricCalculator getUL(@NonNull Data data) {
        return getMetric(data, UL);
    }

    /** Download metric as shown while the test runs, or null if none was received yet. */
    @Nullable
    public static MetricCalculator getDL(@NonNull Data data) {
        return getMetric(data, DL);
    }

    private static void putMetric(Data.Builder b, String prefix, MetricCalculator m) {
        int count = m.getMeanList().size();
        b.putInt(prefix + COUNT, count);
        if (count == 0) return;
        m.calcAll();
        b.putDouble(prefix + LAST, m.getLast());
        b.putDouble(prefix + MEAN, m.getMean());
        b.putDouble(prefix + MEDIAN, m.getMedian());
        b.putDouble(prefix + MIN, m.getMin());
        b.putDouble(prefix + MAX, m.getMax());
    }

    @Nullable
    private static MetricCalculator getMetric(Data data, String prefix) {
        if (data.getInt(prefix + COUNT, 0) == 0) return null;
        MetricCalculator m = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        m.setSnapshot(
                data.getDouble(prefix + LAST, 0),
                data.getDouble(prefix + MEAN, 0),
                data.getDouble(prefix + MEDIAN, 0),
                data.getDouble(prefix + MIN, 0),
                data.getDouble(prefix + MAX, 0));
        return m;
    }
}
