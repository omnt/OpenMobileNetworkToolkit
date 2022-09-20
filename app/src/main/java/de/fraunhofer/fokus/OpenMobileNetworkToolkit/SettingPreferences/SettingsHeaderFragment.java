package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceHeaderFragmentCompat;

public class SettingsHeaderFragment extends PreferenceHeaderFragmentCompat {

    @Override
    public PreferenceFragmentCompat onCreatePreferenceHeader() {
        SettingsFragment settingsFragment = new SettingsFragment();
        return settingsFragment;
    }
}
