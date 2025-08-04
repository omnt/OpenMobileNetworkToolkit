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

public class JsonConfigReceiver extends BroadcastReceiver {
    private final String TAG = "JsonConfigReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String jsonString = intent.getStringExtra("jsonData");
        Log.e(TAG, "onReceive: got jsonString:"+jsonString );
        if (jsonString == null) {
            return;
        }
        SharedPreferencesIO.importPreferences(context, jsonString);
   }
}
