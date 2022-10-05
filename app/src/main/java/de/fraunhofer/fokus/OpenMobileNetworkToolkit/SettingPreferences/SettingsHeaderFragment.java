package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceHeaderFragmentCompat;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;

public class SettingsHeaderFragment extends PreferenceHeaderFragmentCompat {

    @Override
    public PreferenceFragmentCompat onCreatePreferenceHeader() {
        SettingsFragment settingsFragment = new SettingsFragment();
        return settingsFragment;
    }

}
