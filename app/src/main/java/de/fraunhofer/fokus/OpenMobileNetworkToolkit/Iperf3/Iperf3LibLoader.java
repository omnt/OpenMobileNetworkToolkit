/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.util.Log;

import java.util.ArrayList;

public class Iperf3LibLoader {


    private static boolean done = false;
    private static final String TAG = "Iperf3LibLoader";

    protected static synchronized void load() {
        if (done) {
            return;
        }
        ArrayList<Integer> loadedLibs = new ArrayList<>();
        try {
            System.loadLibrary("iperf3.10.1");
            Log.i(TAG, "iperf3.10.1 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {
        }

        try {
            System.loadLibrary("iperf3.11");
            Log.i(TAG, "iperf3.11 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {

        }

        try {
            System.loadLibrary("iperf3.12");
            Log.i(TAG, "iperf3.12 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {
            return;
        }
        try {
            System.loadLibrary("iperf3.15");
            Log.i(TAG, "iperf3.15 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {
        }

        if (loadedLibs.size() > 1) {
            Log.d(TAG, "static initializer: multiple libiperfs loaded!");
        }
        done = true;
    }

}
