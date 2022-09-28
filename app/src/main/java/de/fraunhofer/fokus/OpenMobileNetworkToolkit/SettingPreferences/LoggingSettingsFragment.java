package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.tabs.TabLayout;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SRLog;

public class LoggingSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static String TAG = "PreferenceSettings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_logging, rootKey);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s){
            case "enable_logging":
                Boolean logger = sharedPreferences.getBoolean("enable_logging", false);
                SRLog.d(TAG,"Logger update: " + logger);
                break;
            case "enable_notification_update":
                Boolean notification = sharedPreferences.getBoolean("enable_notification_update", false);
                SRLog.d(TAG,"Logger notification update: " + notification);
                break;
            case "enable_local_log":
                Boolean local_log = sharedPreferences.getBoolean("enable_local_log", false);
                SRLog.d(TAG,"Local log update: " + local_log);
                break;

        }
        SRLog.d(TAG,"preference changed");
    }
}
