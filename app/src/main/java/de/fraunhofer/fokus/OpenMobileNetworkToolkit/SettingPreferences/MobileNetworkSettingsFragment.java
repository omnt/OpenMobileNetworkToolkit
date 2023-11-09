/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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

import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MobileNetworkSettingsFragment extends PreferenceFragmentCompat
    implements OnSharedPreferenceChangeListener {

    public static String TAG = "version";
    private Context ct;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DropDownPreference selectNetworkType = findPreference("select_network_type");
        EditTextPreference inputPLMN = findPreference("add_plmn");
        ct = requireContext();
        String inputString = ct.getString(R.string.select_plmn);
        String dropDownText = ct.getString(R.string.access_networktype);



        List<String> list = Arrays.asList(NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.CDMA2000),
            NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.GERAN),
            NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.IWLAN),
            NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.EUTRAN),
            NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UNKNOWN),
            NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.NGRAN),
            NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.UTRAN));
        CharSequence[] cs = list.toArray(new CharSequence[list.size()]);

        if (selectNetworkType != null) {
            selectNetworkType.setEntries(cs);
            selectNetworkType.setEntryValues(cs);
            selectNetworkType.setDefaultValue(NetworkInformation.getAccessNetworkType(AccessNetworkConstants.AccessNetworkType.NGRAN));
        
        };
        GlobalVars gv = GlobalVars.getInstance();
        TelephonyManager tm = gv.getTm();

        selectNetworkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // newValue is the updated value of the preference
                String updatedValue = (String) newValue;
                // You can perform actions based on the updated value here
                // For example, you can save the value to SharedPreferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("select_network_type", updatedValue);
                editor.apply();
                selectNetworkType.setTitle(String.format("%s Current:%s", dropDownText, newValue));
                return true; // Return true to allow the change to be saved
            }
        });
        inputPLMN.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // newValue is the updated value of the preference
                String updatedValue = (String) newValue;
                // You can perform actions based on the updated value here
                // For example, you can save the value to SharedPreferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("selected_plmn", updatedValue);
                editor.apply();
                inputPLMN.setTitle(String.format("%s Current:%s", inputString, newValue));
                return true; // Return true to allow the change to be saved
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
