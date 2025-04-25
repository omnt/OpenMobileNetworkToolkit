package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

public class Application extends android.app.Application implements Configuration.Provider {
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setDefaultProcessName(getPackageName())
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build();
    }
}
