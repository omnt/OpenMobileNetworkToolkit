package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SwitchPreferenceCompat carrier_permission;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        carrier_permission = (SwitchPreferenceCompat) getPreferenceManager().getPreferenceScreen().findPreference("carrier_Permission");

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}