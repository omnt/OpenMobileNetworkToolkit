/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Iperf3LibLoader {

    private static boolean done = false;
    private static final String TAG = "Iperf3LibLoader";

    private static final List<String> IPERF_LIBRARIES = List.of(
        "iperf3.10.1",
        "iperf3.11",
        "iperf3.12",
        "iperf3.15",
        "iperf3.16",
        "iperf3.17.1"
    );

    public static synchronized void load() {
        if (done) {
            return;
        }

        List<String> loadedLibs = new ArrayList<>();

        for (String library : IPERF_LIBRARIES) {
            try {
                System.loadLibrary(library);
                Log.i(TAG, library + " loaded!");
                loadedLibs.add(library);
                done = true;
                return;
            } catch (UnsatisfiedLinkError ignored) {
            }
        }

        if (loadedLibs.size() > 1) {
            Log.d(TAG, "static initializer: multiple libiperfs loaded!");
        } else if (loadedLibs.isEmpty()) {
            Log.d(TAG, "failed to load any iPerf3 libs!");
        }
        done = true;
    }
}
