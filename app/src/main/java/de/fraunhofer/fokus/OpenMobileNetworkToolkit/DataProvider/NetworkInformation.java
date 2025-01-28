/*
 *  SPDX-FileCopyrightText: 2024 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2024 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2024 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.annotation.SuppressLint;
import android.os.Build;
import android.telephony.AccessNetworkConstants;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class NetworkInformation extends Information {
    private final String networkOperatorName;
    private final String simOperatorName;
    private final String networkSpecifier;
    private final int dataState;
    private final int dataNetworkType;
    private final int phoneType;
    private final int preferredOpportunisticDataSubscriptionId;

    public NetworkInformation(String networkOperatorName, String simOperatorName, String networkSpecifier, int dataState, int dataNetworkType, int phoneType, int preferredOpportunisitcDataSubscitptionId) {
        super(System.currentTimeMillis());
        this.networkOperatorName = networkOperatorName;
        this.simOperatorName = simOperatorName;
        this.networkSpecifier = networkSpecifier;
        this.dataState = dataState;
        this.dataNetworkType = dataNetworkType;
        this.phoneType = phoneType;
        this.preferredOpportunisticDataSubscriptionId = preferredOpportunisitcDataSubscitptionId;
    }

    public NetworkInformation() {
        this.networkOperatorName = "N/A";
        this.simOperatorName = "N/A";
        this.networkSpecifier = "N/A";
        this.dataState = TelephonyManager.DATA_UNKNOWN;
        this.dataNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        this.phoneType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        this.preferredOpportunisticDataSubscriptionId = SubscriptionManager.DEFAULT_SUBSCRIPTION_ID;
    }

    /**
     * Get the Network Operator Name
     * @return Operator Name string
     */
    public String getNetworkOperatorName() {
        return networkOperatorName;
    }

    /**
     * Get the SIM Operator Name
     * @return Operator Name
     */
    public String getSimOperatorName() {
        return simOperatorName;
    }

    /**
     * Get the network specifier
     * @return Network specifier
     */
    public String getNetworkSpecifier() {
        return networkSpecifier;
    }

    /**
     * Get the network data state id
     * @return ID of the current network data state
     */
    public int getDataState() {
        return dataState;
    }

    /**
     * Get the Phone type id
     * @return Phone type id
     */
    public int getPhoneType() {
        return phoneType;
    }

    /**
     * Get the phone type string representation
     * @return Phone type string representation
     */
    public String getPhoneTypeString() {
        String phoneTypeString;
        switch (phoneType) {
            case 0:
                phoneTypeString = "None";
                break;
            case 1:
                phoneTypeString = "GSM";
                break;
            case 2:
                phoneTypeString = "CDMA";
                break;
            case 3:
                phoneTypeString = "SIP";
                break;
            default:
                phoneTypeString = "N/A";
                break;
        }
        return phoneTypeString;
    }

    /**
     * Get the current data state as string name
     * @return String representation of the data state
     */
    public String getDataStateString() {
        String dataStateString;
        switch (dataState) {
            case TelephonyManager.DATA_DISCONNECTED:
                dataStateString = "Disconnected";
                break;
            case TelephonyManager.DATA_CONNECTING:
                dataStateString = "Connecting";
                break;
            case TelephonyManager.DATA_CONNECTED:
                dataStateString = "Connected";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                dataStateString = "Suspended";
                break;
            case TelephonyManager.DATA_DISCONNECTING:
                dataStateString = "Disconnecting";
                break;
            case TelephonyManager.DATA_HANDOVER_IN_PROGRESS:
                dataStateString = "Handover in progress";
                break;
            default:
                dataStateString = "N/A";
                break;
        }
        return dataStateString;
    }

    /**
     * Get the current data network type string
     * @return Data network type string representation
     */
    public String getDataNetworkTypeString() {
        String dataNetworkTypeString;
        switch (dataNetworkType) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                dataNetworkTypeString = "Unknown";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                dataNetworkTypeString = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                dataNetworkTypeString = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                dataNetworkTypeString = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                dataNetworkTypeString = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                dataNetworkTypeString = "EVDO 0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                dataNetworkTypeString = "EVDO A";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                dataNetworkTypeString = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                dataNetworkTypeString = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                dataNetworkTypeString = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                dataNetworkTypeString = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                dataNetworkTypeString = "EVDO B";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                dataNetworkTypeString = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                dataNetworkTypeString = "EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                dataNetworkTypeString = "HSPAP";
                break;
            case TelephonyManager.NETWORK_TYPE_GSM:
                dataNetworkTypeString = "GSM";
                break;
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                dataNetworkTypeString = "SCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                dataNetworkTypeString = "IWLAN";
                break;
            case TelephonyManager.NETWORK_TYPE_NR:
                dataNetworkTypeString = "NR";
                break;
            default:
                dataNetworkTypeString = "N/A";
                break;
        }
        return dataNetworkTypeString;
    }

    /**
     *
     * @param accessNetworkID Get Network access type string from ID
     * @return Network access type String
     */
    public static String getAccessNetworkType(int accessNetworkID) {
        String accessNetworkType;
        switch (accessNetworkID) {
            case AccessNetworkConstants.AccessNetworkType.CDMA2000:
                accessNetworkType = "CDMA2000";
                break;
            case AccessNetworkConstants.AccessNetworkType.GERAN:
                accessNetworkType = "GERAN";
                break;
            case AccessNetworkConstants.AccessNetworkType.EUTRAN:
                accessNetworkType = "EUTRAN";
                break;
            case AccessNetworkConstants.AccessNetworkType.IWLAN:
                accessNetworkType = "IWLAN";
                break;
            case AccessNetworkConstants.AccessNetworkType.NGRAN:
                accessNetworkType = "NGRAN";
                break;
            case AccessNetworkConstants.AccessNetworkType.UNKNOWN:
                accessNetworkType = "Unknown";
                break;
            case AccessNetworkConstants.AccessNetworkType.UTRAN:
                accessNetworkType = "UTRAN";
                break;
            default:
                accessNetworkType = "N/A";
        }
        return accessNetworkType;
    }

    /**
     * Get the Network access type id from string
     * @param accessNetworkType Network access type string
     * @return Network access type id
     */
    @SuppressLint("ObsoleteSdkInt")
    public static int getAccessNetworkID(String accessNetworkType) {
        switch (accessNetworkType) {
            case "CDMA2000":
                return AccessNetworkConstants.AccessNetworkType.CDMA2000;
            case "GERAN":
                return AccessNetworkConstants.AccessNetworkType.GERAN;
            case "EUTRAN":
                return AccessNetworkConstants.AccessNetworkType.EUTRAN;
            case "IWLAN":
                return AccessNetworkConstants.AccessNetworkType.IWLAN;
            case "NGRAN":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    return AccessNetworkConstants.AccessNetworkType.NGRAN;
                }
            case "UTRAN":
                return AccessNetworkConstants.AccessNetworkType.UTRAN;
            default:
                return AccessNetworkConstants.AccessNetworkType.UNKNOWN;
        }
    }

    /**
     * Get the preferred opportunistic data subscription id
     * @return preferred opportunistic data subscription id
     */
    public int getPreferredOpportunisticDataSubscriptionId() {
        return preferredOpportunisticDataSubscriptionId;
    }

}
