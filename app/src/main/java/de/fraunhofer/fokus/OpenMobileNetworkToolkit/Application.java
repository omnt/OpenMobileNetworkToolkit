/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

public class Application extends android.app.Application implements Configuration.Provider {
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        Configuration.Builder configurationBuilder = new Configuration.Builder()
                .setDefaultProcessName(getPackageName());


        if(BuildConfig.DEBUG) {
            return configurationBuilder.setMinimumLoggingLevel(android.util.Log.DEBUG).build();
        }
        return configurationBuilder.setMinimumLoggingLevel(android.util.Log.ERROR).build();
    }
}
