/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;


import android.Manifest;
import android.os.Build;
import android.os.PersistableBundle;
import android.service.carrier.CarrierIdentifier;
import android.service.carrier.CarrierService;
import android.telephony.CarrierConfigManager;
import android.util.Log;

import java.security.Permission;

public class OpenMobileNetworkToolkit extends CarrierService {
    private static final String TAG = "OpenMobileNetworkToolkit";

    public OpenMobileNetworkToolkit() {
        SRLog.d(TAG, "OpenMobileNetworkToolkit Carrier Config Service created");
    }

    @Override
    public PersistableBundle onLoadConfig(CarrierIdentifier id) {
        int sdk_version = Build.VERSION.SDK_INT;
        SRLog.i(TAG, "CarrierIdentifier id " + id.toString());
        PersistableBundle configForSubId = new PersistableBundle();


        // handle things that need newer API versions
        if (sdk_version >= Build.VERSION_CODES.O_MR1) {
            configForSubId.putBoolean(CarrierConfigManager.KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL, true);
        } else {
            SRLog.d(TAG, "KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL not available with below api level 27");
        }
        if (sdk_version >= Build.VERSION_CODES.R) {
            configForSubId.putBoolean(CarrierConfigManager.KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL, false);
            configForSubId.putBoolean(CarrierConfigManager.KEY_WORLD_MODE_ENABLED_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_RCS_PROVISIONING_REQUIRED_BOOL, false);
            configForSubId.putBoolean(CarrierConfigManager.KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL, true);
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_OVERRIDE_WFC_PROVISIONING_BOOL, false);
            configForSubId.putStringArray(CarrierConfigManager.KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY, new String[]{""});
            configForSubId.putStringArray(CarrierConfigManager.KEY_APN_SETTINGS_DEFAULT_APN_TYPES_STRING_ARRAY, new String[]{""});
            configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL, true);
            configForSubId.putBoolean(Manifest.permission.READ_PRECISE_PHONE_STATE, true);
            configForSubId.putBoolean(Manifest.permission.READ_PHONE_STATE, true);


        } else {
            SRLog.d(TAG, " not available with below api level 30");
        }
        if (sdk_version >= Build.VERSION_CODES.S) {
            configForSubId.putIntArray(CarrierConfigManager.KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY, new int[]{CarrierConfigManager.CARRIER_NR_AVAILABILITY_SA, CarrierConfigManager.CARRIER_NR_AVAILABILITY_NSA});
            configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_TTY_HCO_VCO_WITH_RTT_BOOL, false);
            configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_ENABLE_2G, false);
            configForSubId.putBoolean(CarrierConfigManager.KEY_RTT_UPGRADE_SUPPORTED_FOR_DOWNGRADED_VT_CALL_BOOL, true);
            configForSubId.putBoolean(Manifest.permission.READ_PRECISE_PHONE_STATE, true);
            configForSubId.putBoolean(Manifest.permission.READ_PHONE_STATE, true);


        } else {
            SRLog.d(TAG, "KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY is not available with below api level 31");
        }
        configForSubId.putBoolean(CarrierConfigManager.KEY_PREFER_2G_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_SETTINGS_ENABLE_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_AVAILABLE_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_PROVISIONED_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VT_AVAILABLE_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_IMS_APN_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_PRESET_APN_DETAILS_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_HIDE_SIM_LOCK_SETTINGS_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_ALLOW_ADDING_APNS_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_APN_EXPAND_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_IMS_GBA_REQUIRED_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL, false);
        configForSubId.putInt(CarrierConfigManager.KEY_VOLTE_REPLACEMENT_RAT_INT, 18);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_AUTO_RETRY_ENABLED_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_WORLD_PHONE_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_IS_IMS_CONFERENCE_SIZE_ENFORCED_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL, false);
        configForSubId.putBoolean(CarrierConfigManager.KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL, true);
        configForSubId.putBoolean(CarrierConfigManager.KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL, true);
        configForSubId.putInt(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT, 1);
        configForSubId.putInt(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT, 1);
        configForSubId.putBoolean(CarrierConfigManager.KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL, true);
        configForSubId.putBoolean(Manifest.permission.READ_PRECISE_PHONE_STATE, true);
        configForSubId.putBoolean(Manifest.permission.READ_PHONE_STATE, true);
        //configForSubId.putBoolean(CarrierConfigManager., true);
        SRLog.d(TAG, "Carrier settings applied");
        return configForSubId;
    }
}
