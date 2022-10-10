package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.tabs.TabLayout;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SRLog;

public class LoggingSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static String TAG = "PreferenceSettings";
    SwitchPreferenceCompat enable_influx_switch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_logging, rootKey);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        enable_influx_switch = (SwitchPreferenceCompat) findPreference("enable_influx");
        EditTextPreference influx_url_editText = (EditTextPreference) findPreference("influx_url");
        EditTextPreference influx_org_editText = (EditTextPreference) findPreference("influx_org");
        EditTextPreference influx_token_editText = (EditTextPreference) findPreference("influx_token");
        EditTextPreference influx_bucket_editText = (EditTextPreference) findPreference("infoux_bucket");

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s){
            case "enable_logging":
                Boolean logger = sharedPreferences.getBoolean("enable_logging", false);
                SRLog.d(TAG,"Logger update: " + logger);
                break;
            case "enable_notification_update":
                Boolean notification = sharedPreferences.getBoolean("enable_notification_update", false);
                SRLog.d(TAG,"Logger notification update: " + notification);
                break;
            case "enable_local_log":
                Boolean local_log = sharedPreferences.getBoolean("enable_local_log", false);
                SRLog.d(TAG,"Local log update: " + local_log);
                break;

            case "enable_influx":
                SRLog.d(TAG,"Enabled Influx");
                break;

            case "influx_url":
                String influx_url = sharedPreferences.getString("influx_url","");
                boolean flag = false;
                if(influx_url == "") {
                    SRLog.d(TAG,"URL not specified!");
                    Toast.makeText(getContext(),"URL not specified!", Toast.LENGTH_SHORT).show();
                    flag = false;
                }
                else {
                    SRLog.d(TAG,"URL Found: " + influx_url);
                    flag = true;
                }
                SRLog.d(TAG,"Influx URL found: "+flag);
                break;
        }

        SRLog.d(TAG,"preference changed");
    }

}
