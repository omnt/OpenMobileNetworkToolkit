package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceHeaderFragmentCompat;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
    }
}