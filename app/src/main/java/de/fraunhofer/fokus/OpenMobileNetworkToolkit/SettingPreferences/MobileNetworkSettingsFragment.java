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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.AccessNetworkConstants;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import androidx.preference.SwitchPreference;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MobileNetworkSettingsFragment extends PreferenceFragmentCompat
    implements OnSharedPreferenceChangeListener {

    public static String TAG = "version";
    private Context ct;
    private String plmnId;
    private String accessNetworkType;
    public static String SELECTNETWORKTYPE = "select_network_type";
    public static String ADDPLMN = "add_plmn";
    public static String PERSISTREBOOT = "persist_boot";
    TelephonyManager tm;
    PackageManager pm;
    SharedPreferences preferences;
    private boolean setNetworkSelection(){
        String networkType = preferences.getString(SELECTNETWORKTYPE, "");
        String plmn = preferences.getString(ADDPLMN, "");
        boolean persist = preferences.getBoolean(PERSISTREBOOT, false);
        if(networkType.equals("")){
            Toast.makeText(ct, "Please specify PLMN", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_RADIO_ACCESS)){
            Toast.makeText(ct, "App doesn't have the rights", Toast.LENGTH_SHORT).show();
            return false;
        };
        int networkTypeId = NetworkInformation.getAccessNetworkID(networkType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return tm.setNetworkSelectionModeManual(plmn, persist,
                networkTypeId);
        }
        return tm.setNetworkSelectionModeManual(plmn, persist);
    }

    private void handleSetNetwork(){
        boolean result = setNetworkSelection();
        Toast.makeText(ct, String.valueOf(result), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DropDownPreference selectNetworkType = findPreference(SELECTNETWORKTYPE);
        EditTextPreference inputPLMN = findPreference(ADDPLMN);
        SwitchPreference reboot = findPreference(PERSISTREBOOT);
        ct = requireContext();
        plmnId = ct.getString(R.string.select_plmn);
        accessNetworkType = ct.getString(R.string.access_networktype);
        preferences = PreferenceManager.getDefaultSharedPreferences(ct);


        int sdk_version = Build.VERSION.SDK_INT;

        // we require at least android 29 but if run below 31 we disable settings not available
        if (sdk_version < 31) {
            findPreference("buildVersionS").setEnabled(false);
        }
        if (sdk_version < 30) {
            findPreference("buildVersionR").setEnabled(false);
        }

        List<String> list = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            list = Arrays.asList(NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.CDMA2000),
                NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.GERAN),
                NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.IWLAN),
                NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.EUTRAN),
                NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UNKNOWN),
                NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.NGRAN),
                NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UTRAN));
        } else {
            list = Arrays.asList(NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.CDMA2000),
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

        };
        GlobalVars gv = GlobalVars.getInstance();
        tm = gv.getTm();
        pm = gv.getPm();



        selectNetworkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String updatedValue = (String) newValue;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SELECTNETWORKTYPE, updatedValue);
                editor.apply();
                selectNetworkType.setTitle(String.format("%s Current:%s", accessNetworkType, newValue));
                handleSetNetwork();
                return true;
            }
        });
        inputPLMN.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String updatedValue = (String) newValue;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(ADDPLMN, updatedValue);
                editor.apply();
                inputPLMN.setTitle(String.format("%s Current:%s", plmnId, newValue));
                handleSetNetwork();
                return true;
            }
        });

        reboot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean updatedValue = (boolean) newValue;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PERSISTREBOOT, updatedValue);
                editor.apply();
                reboot.setChecked(updatedValue);
                handleSetNetwork();
                return true;
            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_mobile_network, rootKey);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "selected_plmn":
                Toast.makeText(requireContext(), sharedPreferences.getString(key, "null"), Toast.LENGTH_LONG).show();
                break;
            case "select_network_type":
                Toast.makeText(requireContext(), sharedPreferences.getString(key, "null"), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
