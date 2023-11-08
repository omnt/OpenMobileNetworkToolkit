/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.telephony.AccessNetworkConstants;
import android.telephony.TelephonyManager;

public class NetworkInformation {
    private String networkOperatorName;
    private String simOperatorName;
    private String networkSpecifier;
    private int dataState;
    private int dataNetworkType;
    private int phoneType;
    private int preferredOpportunisticDataSubscriptionId;
    private long timeStamp;
    public NetworkInformation(String networkOperatorName, String simOperatorName, String networkSpecifier, int dataState, int dataNetworkType, int phoneType, int preferredOpportunisitcDataSubscitptionId) {
        this.networkOperatorName = networkOperatorName;
        this.simOperatorName = simOperatorName;
        this.networkSpecifier = networkSpecifier;
        this.dataState = dataState;
        this.dataNetworkType = dataNetworkType;
        this.phoneType = phoneType;
        this.preferredOpportunisticDataSubscriptionId = preferredOpportunisitcDataSubscitptionId;
        this.timeStamp = System.currentTimeMillis();
    }
    public NetworkInformation() {
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNetworkOperatorName() {
        return networkOperatorName;
    }

    public String getSimOperatorName() {
        return simOperatorName;
    }

    public String getNetworkSpecifier() {
        return networkSpecifier;
    }

    public int getDataState() {
        return dataState;
    }

    public int getDataNetworkType() {
        return dataNetworkType;
    }

    public int getPhoneType() {
        return phoneType;
    }

    public String getPhoneTypeString() {
        String phoneTypeString = "N/A";
        switch (phoneType){
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
        }
        return phoneTypeString;
    }

    public String getDataStateString() {
        String dataStateString = "N/A";
        switch (dataState){
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
        }
        return dataStateString;
    }

    public String getDataNetworkTypeString() {
        String dataNetworkTypeString = "N/A";
        switch (dataNetworkType){
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                dataNetworkTypeString =  "Unknown";
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
                dataNetworkTypeString = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                dataNetworkTypeString = "IDEN";
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
        }
        return dataNetworkTypeString;
    }

    public static String getAccessNetworkType(int acceesNetworkID) {
        String accessNetworkType = "N/A";
        switch (acceesNetworkID){
            case AccessNetworkConstants.AccessNetworkType.CDMA2000:
                accessNetworkType =  "CDMA2000";
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
                accessNetworkType = "Unknow";
                break;
            case AccessNetworkConstants.AccessNetworkType.UTRAN:
                accessNetworkType = "UTRAN";
                break;
            default:
                accessNetworkType = "Unknown";
        }
        return accessNetworkType;
    }




    public int getPreferredOpportunisticDataSubscriptionId() {
        return preferredOpportunisticDataSubscriptionId;
    }
}
