/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;


public class LoggingServiceOnBootReceiver extends BroadcastReceiver {
    SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (sp.getBoolean("start_logging_on_boot", false) && sp.getBoolean("enable_logging", false)) {
                Intent serviceIntent = new Intent(context, LoggingService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
