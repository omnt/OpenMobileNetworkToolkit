/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class FlagSettingFragment extends PreferenceFragmentCompat
    implements OnSharedPreferenceChangeListener {

    public static String TAG = "version";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_flag, rootKey);

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
}
