package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.work.WorkInfo;

import java.util.EventListener;
import java.util.HashMap;
import java.util.UUID;

public interface CustomEventListener extends EventListener {
    void onChange(HashMap<UUID, WorkInfo> workInfos);
}
