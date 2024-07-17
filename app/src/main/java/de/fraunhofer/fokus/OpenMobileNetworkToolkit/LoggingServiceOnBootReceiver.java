/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;


public class LoggingServiceOnBootReceiver extends BroadcastReceiver {
    SharedPreferencesGrouper spg;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            spg = SharedPreferencesGrouper.getInstance(context);
            if (spg.getSharedPreference(SPType.logging_sp).getBoolean("start_logging_on_boot", false) &&
                spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_logging", false)) {
                Intent serviceIntent = new Intent(context, LoggingService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
