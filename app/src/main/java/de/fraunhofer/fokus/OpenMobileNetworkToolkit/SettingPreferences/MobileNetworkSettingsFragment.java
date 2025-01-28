/*
 *  SPDX-FileCopyrightText: 2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.AccessNetworkConstants;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MobileNetworkSettingsFragment extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener {

    public static final String SELECTNETWORKTYPE = "select_network_type";
    public static final String ADDPLMN = "add_plmn";
    public static final String PERSISTREBOOT = "persist_boot";
    public static String TAG = "MobileNetworkSettingsFragment";
    TelephonyManager tm;
    PackageManager pm;
    GlobalVars gv;
    SharedPreferences preferences;
    private Context ct;
    private String plmnId;
    private String accessNetworkType;

    @SuppressLint("ObsoleteSdkInt")
    private boolean setNetworkSelection() {
        String networkType = preferences.getString(SELECTNETWORKTYPE, "");
        String plmn = preferences.getString(ADDPLMN, "");
        boolean persist = preferences.getBoolean(PERSISTREBOOT, false);

        if (networkType.isEmpty()) {
            Toast.makeText(ct, "Please specify a PLMN", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_RADIO_ACCESS)) {
            Toast.makeText(ct, "App doesn't have the permission to alter radio settings", Toast.LENGTH_SHORT).show();
            return false;
        }

        int networkTypeId = NetworkInformation.getAccessNetworkID(networkType);

        if (gv.isCarrier_permissions() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return tm.setNetworkSelectionModeManual(plmn, persist, networkTypeId);
        }

        return tm.setNetworkSelectionModeManual(plmn, persist);
    }

    private void handleSetNetwork() {
        if (gv.isCarrier_permissions()) {
            if (setNetworkSelection()) {
                Toast.makeText(ct, "Network Selection Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ct, "Network Selection Failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ct, "OMNT doesn't have Carrier Permissions", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ct = requireContext();
        plmnId = ct.getString(R.string.select_plmn);
        accessNetworkType = ct.getString(R.string.access_networktype);
        preferences = SharedPreferencesGrouper.getInstance(ct).getSharedPreference(SPType.mobile_network_sp);
        tm = gv.getTm();
        pm = gv.getPm();
        int sdk_version = Build.VERSION.SDK_INT;

        DropDownPreference selectNetworkType = findPreference(SELECTNETWORKTYPE);
        EditTextPreference inputPLMN = findPreference(ADDPLMN);
        SwitchPreference reboot = findPreference(PERSISTREBOOT);

        if (!gv.isCarrier_permissions()) {
            selectNetworkType.setEnabled(false);
            inputPLMN.setEnabled(false);
            reboot.setEnabled(false);
        }

        // we require at least android 29 but if run below 31 we disable settings not available
        if (sdk_version < 31) {
            findPreference("buildVersionS").setEnabled(false);
        }
        if (sdk_version < 30) {
            findPreference("buildVersionR").setEnabled(false);
        }

        List<String> list;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            list = Arrays.asList(
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.CDMA2000),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.GERAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.IWLAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.EUTRAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UNKNOWN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.NGRAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UTRAN));
        } else {
            list = Arrays.asList(
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.CDMA2000),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.GERAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.IWLAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.EUTRAN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UNKNOWN),
                    NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UTRAN));
        }

        CharSequence[] cs = list.toArray(new CharSequence[list.size()]);

        if (selectNetworkType != null) {
            selectNetworkType.setEntries(cs);
            selectNetworkType.setEntryValues(cs);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                selectNetworkType.setDefaultValue(NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.NGRAN));
            }
        }

        selectNetworkType.setOnPreferenceChangeListener((preference, newValue) -> {
            String updatedValue = (String) newValue;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SELECTNETWORKTYPE, updatedValue);
            editor.apply();
            selectNetworkType.setTitle(String.format("%s Current:%s", accessNetworkType, newValue));
            handleSetNetwork();
            return true;
        });

        inputPLMN.setOnPreferenceChangeListener((preference, newValue) -> {
            String updatedValue = (String) newValue;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ADDPLMN, updatedValue);
            editor.apply();
            inputPLMN.setTitle(String.format("%s Current:%s", plmnId, newValue));
            handleSetNetwork();
            return true;
        });

        reboot.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean updatedValue = (boolean) newValue;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PERSISTREBOOT, updatedValue);
            editor.apply();
            reboot.setChecked(updatedValue);
            handleSetNetwork();
            return true;
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        gv = GlobalVars.getInstance();
        getPreferenceManager().setSharedPreferencesName(SharedPreferencesGrouper.getInstance(requireContext()).getSharedPreferenceIdentifier(SPType.carrier_sp));
        setPreferencesFromResource(R.xml.preference_mobile_network, rootKey);
        Preference button = getPreferenceManager().findPreference("apply_cs_settings");
        if (button != null) {
            if (gv.isCarrier_permissions()) {
                button.setOnPreferenceClickListener(arg0 -> {
                    apply_settings();
                    return true;
                });
            } else {
                button.setEnabled(false);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (Objects.requireNonNull(key)) {
            case "selected_plmn":
            case "select_network_type":
                Toast.makeText(requireContext(), sharedPreferences.getString(key, "null"), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void apply_settings() {
        CarrierConfigManager cs = (CarrierConfigManager) requireContext().getSystemService(Context.CARRIER_CONFIG_SERVICE);
        cs.notifyConfigChangedForSubId(tm.getSubscriptionId());
    }
}
