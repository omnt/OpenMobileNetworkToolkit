/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(requireContext());
        PreferenceManager pfm = getPreferenceManager();
        getPreferenceManager().setSharedPreferencesName(spg.getSharedPreferenceIdentifier(SPType.default_sp));
        pfm.setSharedPreferencesName(spg.getSharedPreferenceIdentifier(SPType.default_sp));
        pfm.setSharedPreferencesMode(Context.MODE_PRIVATE);
        setPreferencesFromResource(R.xml.preference, rootKey);
        ListPreference sub_select = pfm.findPreference("select_subscription");

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
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                Toast.makeText(requireContext().getApplicationContext(), "Subscription Changed, please restart OMNT", Toast.LENGTH_SHORT).show();
                //spg.getSharedPreference(SPType.default_sp).edit().putString("select_subscription", newValue.toString()).apply();
                return true;
            }
        });

        Preference button = pfm.findPreference("reset_modem");

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