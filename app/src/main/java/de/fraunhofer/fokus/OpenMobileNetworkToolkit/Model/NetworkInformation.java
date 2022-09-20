package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model;

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

    public int getPreferredOpportunisticDataSubscriptionId() {
        return preferredOpportunisticDataSubscriptionId;
    }
}
