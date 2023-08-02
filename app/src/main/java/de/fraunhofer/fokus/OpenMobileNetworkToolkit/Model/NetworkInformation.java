package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model;

import android.telephony.TelephonyManager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class NetworkInformation {
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

    public NetworkInformation(String networkOperatorName, String simOperatorName, String networkSpecifier, int dataState, int dataNetworkType, int phoneType, int preferredOpportunisitcDataSubscitptionId, long timeStamp) {
        this.networkOperatorName = networkOperatorName;
        this.simOperatorName = simOperatorName;
        this.networkSpecifier = networkSpecifier;
        this.dataState = dataState;
        this.dataNetworkType = dataNetworkType;
        this.phoneType = phoneType;
        this.preferredOpportunisticDataSubscriptionId = preferredOpportunisitcDataSubscitptionId;
        this.timeStamp = timeStamp;
    }
    @PrimaryKey
    private long timeStamp;
    private final String networkOperatorName;
    private final String simOperatorName;
    private final String networkSpecifier;
    private final int dataState;
    private final int dataNetworkType;
    private final int phoneType;
    private final int preferredOpportunisticDataSubscriptionId;

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

    public int getPreferredOpportunisticDataSubscriptionId() {
        return preferredOpportunisticDataSubscriptionId;
    }
}
