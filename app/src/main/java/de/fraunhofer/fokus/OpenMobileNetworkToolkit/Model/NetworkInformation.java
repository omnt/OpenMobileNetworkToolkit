package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model;

public class NetworkInformation {

    public NetworkInformation(String networkOperatorName, String simOperatorName, String network_specifier, int data_state, int data_network_type, int phone_type, int preferred_opportunisitc_data_subscitption_id) {
        this.networkOperatorName = networkOperatorName;
        this.simOperatorName = simOperatorName;
        this.networkSpecifier = network_specifier;
        this.dataState = data_state;
        this.dataNetworkType = data_network_type;
        this.phoneType = phone_type;
        this.preferredOpportunisticDataSubscriptionId = preferred_opportunisitc_data_subscitption_id;
        //this.interface_name = interface_name;
        //this.current_network = current_network;
        //this.default_dns = default_dns;
        //this.enterprice_capability = enterprice_capability;
        //this.internet_capabilty = internet_capabilty;
        //this.ims_capability = ims_capability;
    }

    private final String networkOperatorName;
    private final String simOperatorName;
    private final String networkSpecifier;
    private final int dataState;
    private final int dataNetworkType;
    private final int phoneType;
    private final int preferredOpportunisticDataSubscriptionId;
    //private final String interface_name;
    //private final Network current_network;
    //private final List<String> default_dns;
    //private final boolean enterprice_capability;
    //private final boolean internet_capabilty;
    //private final boolean ims_capability;


    /*public String getNetwork_operator_name() {
        return networkOperatorName;
    }*/

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

/*    public String getInterface_name() {
        return interface_name;
    }*/

/*    public Network getCurrent_network() {
        return current_network;
    }*/
/*
    public List<String> getDefault_dns() {
        return default_dns;
    }

    public boolean isEnterprice_capability() {
        return enterprice_capability;
    }

    public boolean isInternet_capabilty() {
        return internet_capabilty;
    }

    public boolean isIms_capability() {
        return ims_capability;
    }*/

}
