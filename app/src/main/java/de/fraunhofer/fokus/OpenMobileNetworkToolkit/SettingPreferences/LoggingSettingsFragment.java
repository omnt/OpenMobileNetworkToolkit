package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class LoggingSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static String TAG = "PreferenceSettings";
    SwitchPreferenceCompat enable_influx_switch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_logging, rootKey);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        enable_influx_switch = findPreference("enable_influx");

        androidx.preference.EditTextPreference editTextPreference = getPreferenceManager().findPreference("logging_interval");
        editTextPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "enable_logging":
                boolean logger = sharedPreferences.getBoolean("enable_logging", false);
                Log.d(TAG, "Logger update: " + logger);
                break;
            case "enable_notification_update":
                boolean notification = sharedPreferences.getBoolean("enable_notification_update", false);
                Log.d(TAG, "Logger notification update: " + notification);
                break;
            case "enable_local_log":
                boolean local_log = sharedPreferences.getBoolean("enable_local_log", false);
                Log.d(TAG, "Local log update: " + local_log);
                break;

            case "enable_influx":
                Log.d(TAG, "Enabled Influx");
                break;

            case "influx_url":
                String influx_url = sharedPreferences.getString("influx_url", "");
                boolean flag;
                if (influx_url.equals("")) {
                    Log.d(TAG, "URL not specified!");
                    Toast.makeText(getContext(), "URL not specified!", Toast.LENGTH_SHORT).show();
                    flag = false;
                } else {
                    Log.d(TAG, "URL Found: " + influx_url);
                    flag = true;
                }
                Log.d(TAG, "Influx URL found: " + flag);
                break;
        }

        Log.d(TAG, "preference changed");
    }

}
