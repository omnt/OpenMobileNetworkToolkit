/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
        ListPreference sub_select = findPreference("select_subscription");
        ArrayList<String> entries = new ArrayList<>();
        ArrayList<String> entryValues = new ArrayList<>();
        List<SubscriptionInfo> subscriptions = GlobalVars.getInstance().get_dp().getSubscriptions();
        for (SubscriptionInfo info : subscriptions) {
            entries.add(info.getDisplayName().toString());
            entryValues.add(String.valueOf(info.getSubscriptionId()));
        }
        CharSequence[] entries_char = entries.toArray(new CharSequence[entries.size()]);
        CharSequence[] entryValues_char = entryValues.toArray(new CharSequence[entryValues.size()]);
        sub_select.setEntries(entries_char);
        sub_select.setEntryValues(entryValues_char);
        sub_select.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(requireContext().getApplicationContext(), "Subscription Changed, please restart OMNT", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference button = getPreferenceManager().findPreference("reset_modem");

        if (button != null) {
            if (GlobalVars.getInstance().isCarrier_permissions()) {
                button.setEnabled(true);
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Toast.makeText(getActivity(), "rebooting modem",
                                    Toast.LENGTH_SHORT).show();
                            GlobalVars.getInstance().getTm().rebootModem();
                        }
                        return true;
                    }
                });
            } else {
                button.setEnabled(false);
            }
        }


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}