package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MQTTSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(requireContext());
        getPreferenceManager().setSharedPreferencesName(spg.getSharedPreferenceIdentifier(SPType.logging_sp));
        setPreferencesFromResource(R.xml.preference_mqtt, rootKey);
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                .registerOnSharedPreferenceChangeListener(this);



    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {

    }
}
