package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //getActivity().recreate();
    }
}