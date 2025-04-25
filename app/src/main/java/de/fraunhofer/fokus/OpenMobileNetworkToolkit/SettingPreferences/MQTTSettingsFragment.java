package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MQTTSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MQTTSettingsFragment";
    SwitchPreferenceCompat enable_mqtt_switch;
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(requireContext());
        getPreferenceManager().setSharedPreferencesName(spg.getSharedPreferenceIdentifier(SPType.mqtt_sp));
        setPreferencesFromResource(R.xml.preference_mqtt, rootKey);
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                .registerOnSharedPreferenceChangeListener(this);
        enable_mqtt_switch = findPreference("enable_mqtt");


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if(key == null) return;
        if (key.equals("enable_mqtt")) {
            boolean logger = sharedPreferences.getBoolean("enable_mqtt", false);
            Log.d(TAG, "Logger update: " + logger);
        }



    }
}
