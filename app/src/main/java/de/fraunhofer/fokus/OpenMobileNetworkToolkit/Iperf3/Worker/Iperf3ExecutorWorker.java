/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteListenableWorker;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LibLoader;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LiveProgress;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

/**
 * Runs one iPerf3 test through libiperf (JNI) in this worker's own process.
 *
 * <p>The native library calls {@link #onIperf3Event} for every iperf3 --json-stream event while
 * the test runs. Events are queued and handled on a separate thread by an
 * {@link Iperf3EventProcessor}, so iperf3's own timing loop never waits for disk, database or
 * IPC work. Live state is streamed to the UI as WorkManager progress
 * ({@link Iperf3LiveProgress}); the finished run is stored in Room.
 */
public class Iperf3ExecutorWorker extends RemoteListenableWorker {
    public static final String TAG = "Iperf3ExecutorWorker";

    static {
        Iperf3LibLoader.load();
    }

    /** Queued after the last event, once the native call has returned. */
    private static final byte[] END_OF_EVENTS = new byte[0];
    /** How long iperf3 may take to finish after Stop before the process is restarted. */
    private static final long STOP_TIMEOUT_MS = 5000;

    private final String channelId = "OMNT_notification_channel";
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private final Iperf3Input iperf3Input;
    private int notificationID = 2000;
    private final NotificationCompat.Builder notificationBuilder;
    private final Iperf3RunResultDao iperf3RunResultDao;

    private final LinkedBlockingQueue<byte[]> events = new LinkedBlockingQueue<>();
    private final AtomicInteger returnCode = new AtomicInteger(-1);
    private volatile boolean stopped = false;
    private volatile Thread nativeThread;

    public Iperf3ExecutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Gson gson = new Gson();
        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);
        int notificationNumber = getInputData().getInt(PingInput.NOTIFICATIONUMBER, 0);
        notificationID += notificationNumber;
        iperf3RunResultDao = Iperf3ResultsDataBase.getDatabase(context).iperf3RunResultDao();
        notificationBuilder = new NotificationCompat.Builder(context, channelId);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Log.d(TAG, "startRemoteWork: tags: " + this.getTags());
            if (isServer()) {
                setForegroundAsync(createForegroundInfo(title(), waitingText(0)));
                setProgressAsync(Iperf3LiveProgress.waiting(iperf3Input.getTestUUID(), 0, null, null, null));
            } else {
                setForegroundAsync(createForegroundInfo("Running iPerf3 test...", connectingText()));
                setProgressAsync(Iperf3LiveProgress.connecting(iperf3Input.getTestUUID()));
            }

            // This is called on the worker process' main thread: don't block it for the test.
            new Thread(() -> {
                try {
                    completer.set(runTest());
                } catch (Throwable t) {
                    Log.e(TAG, "iPerf3 test crashed", t);
                    completer.set(Result.failure());
                }
            }, "iperf3-events").start();
            return TAG;
        });
    }

    /** Runs the test and handles its events on the calling thread until iperf3 is done. */
    private Result runTest() throws InterruptedException {
        String[] argv = iperf3Input.getParameter().getInputAsCommand();
        Iperf3EventProcessor processor =
                new Iperf3EventProcessor(iperf3Input, iperf3RunResultDao, liveListener);

        nativeThread = new Thread(() -> {
            int rc = -1;
            try {
                rc = iperf3Run(argv);
            } catch (Throwable t) {
                // e.g. UnsatisfiedLinkError: libiperf3.so missing or too old.
                Log.e(TAG, "iperf3Run failed", t);
            }
            returnCode.set(rc);
            events.offer(END_OF_EVENTS);
        }, "iperf3-native");
        Log.d(TAG, "starting iperf3");
        nativeThread.start();

        while (true) {
            byte[] event = events.take();
            if (event == END_OF_EVENTS) break;
            processor.handle(new String(event, StandardCharsets.UTF_8));
        }

        int result = processor.finish(returnCode.get(), stopped);
        Log.d(TAG, "iperf3 completed, return code " + returnCode.get() + ", result " + result);
        // Success even if the test failed, as before: the line protocol / upload workers that
        // follow decide themselves whether there is data, and the result is in Room.
        return Result.success(new Data.Builder()
                .putInt("result", result)
                .putString("testUUID", iperf3Input.getTestUUID())
                .build());
    }

    private int session = 0;
    private String client = null;

    private final Iperf3EventProcessor.Listener liveListener = new Iperf3EventProcessor.Listener() {
        @Override
        public void onStart(int session, String client) {
            Iperf3ExecutorWorker.this.session = session;
            Iperf3ExecutorWorker.this.client = client;
            setProgressAsync(Iperf3LiveProgress.running(iperf3Input.getTestUUID(), session, client, 0,
                    new MetricCalculator(METRIC_TYPE.THROUGHPUT),
                    new MetricCalculator(METRIC_TYPE.THROUGHPUT),
                    null));
            setForegroundAsync(createForegroundInfo(title(),
                    client != null ? "Client connected: " + client : "Connected"));
        }

        @Override
        public void onSessionEnd(int session, @NonNull MetricCalculator ul,
                                 @NonNull MetricCalculator dl, String error) {
            Iperf3ExecutorWorker.this.client = null;
            setProgressAsync(Iperf3LiveProgress.waiting(iperf3Input.getTestUUID(), session,
                    ul.getMeanList().isEmpty() ? null : ul,
                    dl.getMeanList().isEmpty() ? null : dl, error));
            setForegroundAsync(createForegroundInfo(title(), waitingText(session)));
        }

        @Override
        public void onInterval(@NonNull Interval interval, @NonNull MetricCalculator ul,
                               @NonNull MetricCalculator dl, int intervalCount,
                               @NonNull String intervalJson) {
            setProgressAsync(Iperf3LiveProgress.running(iperf3Input.getTestUUID(), session, client,
                    intervalCount, ul, dl, intervalJson));

            String text = String.format("%s: %d Mbit/s", interval.getSum().getSumType(),
                    Math.round(interval.getSum().getBits_per_second() / 1e6));
            if (interval.getSumBidirReverse() != null) {
                text += String.format("\n%s: %d Mbit/s", interval.getSumBidirReverse().getSumType(),
                        Math.round(interval.getSumBidirReverse().getBits_per_second() / 1e6));
            }
            setForegroundAsync(createForegroundInfo(title(), text));
        }
    };

    private boolean isServer() {
        return iperf3Input.getParameter().getMode() == de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter.Iperf3Mode.SERVER;
    }

    private int port() {
        int p = iperf3Input.getParameter().getPort();
        return p > 0 ? p : 5201;
    }

    private String title() {
        if (isServer()) return "iPerf3 server on port " + port();
        return String.format("iPerf3 %s:%s", iperf3Input.getParameter().getHost(), port());
    }

    private String waitingText(int sessions) {
        return sessions == 0 ? "Waiting for a client"
                : "Waiting for the next client (" + sessions + (sessions == 1 ? " client so far)" : " clients so far)");
    }

    private String connectingText() {
        return "Connecting to " + iperf3Input.getParameter().getHost() + "...";
    }

    /** Called from native code (any thread) for every iperf3 --json-stream event. */
    @Keep
    private void onIperf3Event(byte[] utf8Json) {
        if (utf8Json != null && utf8Json.length > 0) {
            events.offer(utf8Json);
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped: stopping iperf3 test");
        stopped = true;
        try {
            iperf3Stop();
        } catch (Throwable t) {
            Log.e(TAG, "iperf3Stop failed", t);
        }
        startStopWatchdog();
    }

    /**
     * libiperf keeps per-process state and can't be aborted from outside. If it does not finish
     * after Stop, store the run as stopped and end this worker process (it serves only iPerf3
     * tests), so the next test starts in a fresh one instead of finding this test still running.
     */
    private void startStopWatchdog() {
        Thread t = nativeThread;
        if (t == null) return;
        new Thread(() -> {
            try {
                t.join(STOP_TIMEOUT_MS);
                if (!t.isAlive()) return;
                Log.e(TAG, "iperf3 did not stop within " + STOP_TIMEOUT_MS + " ms; restarting the worker process");
                events.offer(END_OF_EVENTS); // the event thread stores the run as stopped
                Thread.sleep(1000);
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (InterruptedException ignored) {
            }
        }, "iperf3-stop-watchdog").start();
    }

    /**
     * Runs one iperf3 test (argv without the program name, must contain --json-stream) and
     * returns iperf3's exit code. Blocks until the test is finished.
     */
    private native int iperf3Run(String[] argv);

    /** Asks the running test to finish early. */
    private native void iperf3Stop();

    /**
     * Standard (non-custom) notification. A custom RemoteViews layout failed to inflate in
     * SystemUI on some devices, and that kills the whole app with
     * BadForegroundServiceNotificationException.
     */
    private ForegroundInfo createForegroundInfo(String title, String text) {
        Notification notification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build();

        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }
}
