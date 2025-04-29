/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;
import androidx.work.WorkQuery;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;


public class RemoteWorkInfoChecker implements Runnable {
    private static final String TAG = "WorkInfoChecker";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final HashMap<UUID, WorkInfo> workInfors = new HashMap<>();
    private final RemoteWorkManager remoteWorkManager;
    private final ArrayList<UUID> workIdGroups;
    private boolean isDone = false;
    private final Executor executor;
    private CustomEventListener listener;

    private int workCount = 0;
    public RemoteWorkInfoChecker(RemoteWorkManager remoteWorkManager, ArrayList<UUID> workIdGroups) {
        this.remoteWorkManager = remoteWorkManager;
        this.workIdGroups = workIdGroups;
        this.executor = Runnable::run;
    }


    private void update(WorkInfo workInfo) {
        workInfors.put(workInfo.getId(), workInfo);
        if(listener != null) {
            listener.onChange(new HashMap<>(workInfors));
        }
    }

    private void checkWorkInfos(UUID workIds) {
        ListenableFuture<List<WorkInfo>> future = remoteWorkManager.getWorkInfos(WorkQuery.fromIds(workIds));
        Futures.addCallback(future, new com.google.common.util.concurrent.FutureCallback<List<WorkInfo>>() {
            @Override
            public void onSuccess(List<WorkInfo> workInfos) {
                Log.d(TAG, "onSuccess: WorkInfos: " + workInfos.size());
                for (WorkInfo workInfo : workInfos) {
                    Log.d(TAG, "onSuccess: WorkInfo: " + workInfo.getId() + " State: " + workInfo.getState());
                    update(workInfo);
                }
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                Log.e(TAG, " executeWork: Error getting work info: " + throwable.getMessage());
            }
        }, executor);
    }

    private void isAllWorkDone() {
        for (WorkInfo info : workInfors.values()) {
            WorkInfo.State state = info.getState();
            isDone = (state != WorkInfo.State.RUNNING) && (state != WorkInfo.State.ENQUEUED);
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < workIdGroups.size(); i++) {
            checkWorkInfos(workIdGroups.get(i));
        }

        executor.execute(this::isAllWorkDone);

        if (isDone || workIdGroups.isEmpty()) {
            Log.d(TAG, "run: All work done");
            handler.removeCallbacks(this);
        } else {
            handler.postDelayed(this, 1000);
        }
    }

    public void setListener(CustomEventListener listener) {
        this.listener = listener;
    }

    public void start() {
        handler.postDelayed(this, 200);
    }

}
