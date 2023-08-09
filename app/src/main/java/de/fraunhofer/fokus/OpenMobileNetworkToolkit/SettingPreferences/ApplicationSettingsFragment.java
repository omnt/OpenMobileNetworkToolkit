package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.PreferenceFragmentCompat;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class ApplicationSettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static String TAG = "PreferenceSettings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_application, rootKey);
        getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("carrierPermission")) {
            boolean cp = sharedPreferences.getBoolean("carrierPermission", false);
            Log.d(TAG, "Carrier Permission update: " + cp);
        }
    }
}
