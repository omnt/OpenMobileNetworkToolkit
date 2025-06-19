/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class LoggingSettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "PreferenceSettings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(requireContext());
        getPreferenceManager().setSharedPreferencesName(spg.getSharedPreferenceIdentifier(SPType.logging_sp));
        setPreferencesFromResource(R.xml.preference_logging, rootKey);
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
            .registerOnSharedPreferenceChangeListener(this);

        androidx.preference.EditTextPreference editTextPreference =
            getPreferenceManager().findPreference("logging_interval");
        editTextPreference.setOnBindEditTextListener(
            editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
    }
    @Override
    public void onResume() {
        super.onResume();
        // Register the listener
        Objects.requireNonNull(getPreferenceManager().getSharedPreferences())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener to prevent memory leaks
        Objects.requireNonNull(getPreferenceManager().getSharedPreferences())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s == null) return;
        Log.d(TAG, "onSharedPreferenceChanged: " + s);
        if (s.equals("enable_logging")) {
            boolean logger = sharedPreferences.getBoolean("enable_logging", false);
            Log.d(TAG, "onSharedPreferenceChanged: " + logger);
        }

        switch (s){
            case "enable_logging":
            case "start_logging_on_boot":
            case "enable_local_influx_log":
            case "enable_local_file_log":
            case "enable_influx":
            case "fake_location":
            case "influx_network_data":
            case "log_signal_data":
            case "influx_cell_data":
            case "log_neighbour_cells":
            case "influx_throughput_data":
            case "log_wifi_data":
            case "influx_battery_data":
            case "influx_ip_address_data":
                boolean booleanValue = sharedPreferences.getBoolean(s, false);
                SwitchPreferenceCompat switchPreferenceCompat = findPreference(s);
                if (switchPreferenceCompat != null) {
                    switchPreferenceCompat.setChecked(booleanValue);
                }
                break;
            case "logging_interval":
            case "influx_URL":
            case "influx_token":
            case "influx_org":
            case "influx_bucket":
            case "measurement_name":
            case "tags":
                String stringValue = sharedPreferences.getString(s, "");
                EditTextPreference editTextPreference =  findPreference(s);
                if (editTextPreference != null) {
                    editTextPreference.setText(stringValue);
                }

                break;

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceScreen prefScreen = getPreferenceScreen();
        if (prefScreen != null) {
            SharedPreferences prefsSharedPref = prefScreen.getSharedPreferences();
            if (prefsSharedPref != null) {
                prefsSharedPref.unregisterOnSharedPreferenceChangeListener(this);
            }
        }
    }
}
