package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

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
        }
    }
}
