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

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class LoggingSettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "PreferenceSettings";
    SwitchPreferenceCompat enable_influx_switch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(requireContext());
        getPreferenceManager().setSharedPreferencesName(spg.getSharedPreferenceIdentifier(SPType.logging_sp));
        setPreferencesFromResource(R.xml.preference_logging, rootKey);
        getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);

        enable_influx_switch = findPreference("enable_influx");


        androidx.preference.EditTextPreference editTextPreference =
            getPreferenceManager().findPreference("logging_interval");
        editTextPreference.setOnBindEditTextListener(
            editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s == null) return;
        if (s.equals("enable_logging")) {
            boolean logger = sharedPreferences.getBoolean("enable_logging", false);
            Log.d(TAG, "Logger update: " + logger);
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
