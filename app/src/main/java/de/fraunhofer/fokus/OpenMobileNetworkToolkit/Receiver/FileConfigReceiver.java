/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesIO;

public class FileConfigReceiver extends BroadcastReceiver {
    private static final String TAG = "FileConfigReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String filePath = intent.getStringExtra("filePath");
        Log.e(TAG, "onReceive: got following path"+ filePath);
        if (filePath == null) {
            Log.e(TAG, "onReceive: filePath is null");
            return;
        }
        try {
            String jsonString = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
            if (jsonString.isEmpty()) {
                Log.e(TAG, "onReceive: JSON string is empty");
                return;
            }
            Log.i(TAG, "onReceive: Successfully read JSON from file: " + filePath);
            SharedPreferencesIO.importPreferences(context, jsonString);
        } catch (java.io.IOException e) {
            Log.e(TAG, "Failed to read JSON from file: " + filePath, e);
        }

    }
}
