/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;


import android.os.Build;
import android.os.PersistableBundle;
import android.service.carrier.CarrierIdentifier;
import android.service.carrier.CarrierService;
import android.telephony.CarrierConfigManager;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class OpenMobileNetworkToolkit extends CarrierService {
    private static final String TAG = "OpenMobileNetworkToolkit";
    public OpenMobileNetworkToolkit() {
        Log.d(TAG, "OpenMobileNetworkToolkit Carrier Config Service created");
    }

    // we keep this for older SDK version
    @Override
    public PersistableBundle onLoadConfig(CarrierIdentifier id) {
        Log.i(TAG, "CarrierIdentifier id " + id.toString());
        return applyCarrierSettings();
    }

    // from api 33 on we use
    @Override
    public PersistableBundle onLoadConfig(int subscription, CarrierIdentifier id) {
        Log.i(TAG, "CarrierIdentifier id: " + id.toString() + " for subscription: " + subscription);
        return applyCarrierSettings();
    }


    public PersistableBundle applyCarrierSettings() {
        int sdk_version = Build.VERSION.SDK_INT;
        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(this);
        PersistableBundle configForSubId = new PersistableBundle();
        Log.d(TAG, "applying carrier config");
        // API 27
        if (sdk_version >= Build.VERSION_CODES.O_MR1) {
            configForSubId.putBoolean(CarrierConfigManager.KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL",false));
        }

        // API 30
        if (sdk_version >= Build.VERSION_CODES.R) {
            configForSubId.putBoolean(CarrierConfigManager.KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL", true));
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL", false));
            configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL", false));
            configForSubId.putBoolean(CarrierConfigManager.KEY_WORLD_MODE_ENABLED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_WORLD_MODE_ENABLED_BOOL", true));
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_RCS_PROVISIONING_REQUIRED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_RCS_PROVISIONING_REQUIRED_BOOL", false));
            configForSubId.putBoolean(CarrierConfigManager.KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL", true));
            configForSubId.putBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_EDITABLE_WFC_MODE_BOOL", true));
            configForSubId.putBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_EDITABLE_WFC_ROAMING_MODE_BOOL", true));
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL", false));
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_OVERRIDE_WFC_PROVISIONING_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL", false));
            configForSubId.putStringArray(CarrierConfigManager.KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY, new String[] {spg.getSharedPreference(SPType.carrier_sp).getString("edit_text_KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY", "")});
            configForSubId.putStringArray(CarrierConfigManager.KEY_APN_SETTINGS_DEFAULT_APN_TYPES_STRING_ARRAY, new String[] {spg.getSharedPreference(SPType.carrier_sp).getString("edit_text_KEY_APN_SETTINGS_DEFAULT_APN_TYPES_STRING_ARRAY", "")});
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL", true));
        }

        // API 31
        if (sdk_version >= Build.VERSION_CODES.S) {
            int[] nr_av = new int[2];
            int i = 0;
            for (String value: spg.getSharedPreference(SPType.carrier_sp).getStringSet("multi_select_KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY", new HashSet<>(Arrays.asList("1", "2")))){
                nr_av[i] = Integer.parseInt(value);
            }
            configForSubId.putIntArray(CarrierConfigManager.KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY, nr_av);
            configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_TTY_HCO_VCO_WITH_RTT_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_TTY_HCO_VCO_WITH_RTT_BOOL", false));
            //configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_ENABLE_2G, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_ENABLE_2G", false));
            configForSubId.putBoolean(CarrierConfigManager.KEY_RTT_UPGRADE_SUPPORTED_FOR_DOWNGRADED_VT_CALL_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_RTT_UPGRADE_SUPPORTED_FOR_DOWNGRADED_VT_CALL_BOOL", true));

            // VoLTE
            // check if a VoLTE / VoNR EPDG address is configured and apply it.
            String epdg = spg.getSharedPreference(SPType.carrier_sp).getString("edit_text_EPDG_STATIC_ADDRESS", "");
            if (!epdg.isEmpty()) {
                configForSubId.putString(CarrierConfigManager.Iwlan.KEY_EPDG_STATIC_ADDRESS_STRING, epdg);
                configForSubId.putString(CarrierConfigManager.Iwlan.KEY_EPDG_STATIC_ADDRESS_ROAMING_STRING, epdg);
                configForSubId.putInt(CarrierConfigManager.Iwlan.KEY_EPDG_ADDRESS_PRIORITY_INT_ARRAY, CarrierConfigManager.Iwlan.EPDG_ADDRESS_STATIC);
            }
        }

        // API <= 29
        configForSubId.putBoolean(CarrierConfigManager.KEY_FORCE_HOME_NETWORK_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_FORCE_HOME_NETWORK_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_PREFER_2G_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_PREFER_2G_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_SETTINGS_ENABLE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_SETTINGS_ENABLE_BOOL", true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL", true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL", true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_EDITABLE_ENHANCED_4G_LTE_BOOL", true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_AVAILABLE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_VOLTE_AVAILABLE_BOOL", true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_PROVISIONED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_VOLTE_PROVISIONED_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VT_AVAILABLE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_VT_AVAILABLE_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_ENHANCED_4G_LTE_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_IMS_APN_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_IMS_APN_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_PRESET_APN_DETAILS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_PRESET_APN_DETAILS_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_SIM_LOCK_SETTINGS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_HIDE_SIM_LOCK_SETTINGS_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_ALLOW_ADDING_APNS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_ALLOW_ADDING_APNS_BOOL", true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_APN_EXPAND_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_APN_EXPAND_BOOL",true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_IMS_GBA_REQUIRED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_IMS_GBA_REQUIRED_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL", false));
        configForSubId.putInt(CarrierConfigManager.KEY_VOLTE_REPLACEMENT_RAT_INT, Integer.parseInt(spg.getSharedPreference(SPType.carrier_sp).getString("list_KEY_VOLTE_REPLACEMENT_RAT_INT9","18")));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL",false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_AUTO_RETRY_ENABLED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_AUTO_RETRY_ENABLED_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_WORLD_PHONE_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_WORLD_PHONE_BOOL",true));
        configForSubId.putBoolean(CarrierConfigManager.KEY_IS_IMS_CONFERENCE_SIZE_ENFORCED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL", false));
        configForSubId.putBoolean(CarrierConfigManager.KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT", false));
        configForSubId.putInt(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT, Integer.parseInt(spg.getSharedPreference(SPType.carrier_sp).getString("list_KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT", "1")));
        configForSubId.putInt(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT, Integer.parseInt(spg.getSharedPreference(SPType.carrier_sp).getString("list_KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT","1")));
        configForSubId.putBoolean(CarrierConfigManager.KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL, spg.getSharedPreference(SPType.carrier_sp).getBoolean("switch_KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL",false));
        Log.d(TAG, "Carrier settings applied");
        return configForSubId;
    }
}
