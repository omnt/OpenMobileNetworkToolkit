/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.OutcomeReceiver;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;

public class NetworkCallback {
    private static final String TAG = "NETWORK_CALLBACK";
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final TelephonyManager tm;
    private final PackageManager pm;

    /**
     * WIP: Implements different types of network related callbacks and provides access to network related APIs
     * in a more safe manner.
     * @param context Context to be used by the NetworkCallback
     */
    public NetworkCallback(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        tm = GlobalVars.getInstance().getTm();
        pm = GlobalVars.getInstance().getPm();
    }

    /**
     * Check if Slicing is supported by the radio
     * @return Boolean of slicing is supported
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean getCapabilitySlicing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED);
        } else {
            return false;
        }
    }

    /**
     * Check if the current connection has the capability Enterprise
     * @return Boolean of Enterprise capability
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean getCapabilityEnterprise() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    flag = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                }
            }
        }
        return flag;
    }

    /**
     * WIP:
     * @return Boolean of
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean getNetworkRegistrationInfo() {
        boolean flag = false;
        List<NetworkRegistrationInfo> networkRegistrationInfo = new ArrayList<>();
        List<String> networkRegistrationInfoList = new ArrayList<>();
        @SuppressLint("MissingPermission")
        ServiceState serviceState = tm.getServiceState();
        Log.d(TAG, "Service State: " + serviceState.getState());
        if (serviceState.getState() == 1) {  //2 for DATA_CONNECTED 1 FOR DATA_CONNECTING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                networkRegistrationInfo = serviceState.getNetworkRegistrationInfoList();
                for (NetworkRegistrationInfo nri: networkRegistrationInfo) {
                    Log.d(TAG, "Network Registration Info " + nri);
                    String netwrokRegistrationInfoString = nri.toString();
                    networkRegistrationInfoList.add(netwrokRegistrationInfoString);
                }
                flag = true;
            }
        } else {
            Log.d(TAG, "Network Registration Info Unavailable! Check Network State");
        }
        return flag;
    }

    /**
     * Get a list of System features as String list
     * @return String list of system features
     */
    public List<String> getFeatureList() {
        FeatureInfo[] feature;
        FeatureInfo featureInfo;
        List<String> featureString = new ArrayList<>();
        feature = pm.getSystemAvailableFeatures();
        for (FeatureInfo info : feature) {
            featureInfo = info;
            Log.d(TAG, "Feature: " + Arrays.toString(feature));
            featureString.add(featureInfo.toString());
        }
        return featureString;
    }

    /**
     * WIP:
     * @return nothing useful jet
     */
    @SuppressLint("ObsoleteSdkInt")
    @RequiresPermission(value = "android.permission.READ_PRIVILEGED_PHONE_STATE")
    public boolean getConfigurationTM() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                    flag = true;
                    tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                        new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                            @Override
                            public void onResult(@NonNull NetworkSlicingConfig result) {
                                //List<UrspRule> urspRuleList = result.getUrspRules();
                                //Log.d(TAG, "Slice config works!!");
                                //Log.d(TAG, "URSP List: " + urspRuleList);
                                //Log.d(TAG, "URSP received: " + urspRuleList.size());

                                //for (int i = 0; i < urspRuleList.size(); i++) {
                                //    UrspRule urspRule =
                                //            result.getUrspRules().get(i);
                                //    List<TrafficDescriptor> trafficDescriptor =
                                //            urspRule.getTrafficDescriptors();
                                //    List<RouteSelectionDescriptor> routeSelectionDescriptor =
                                //            urspRule.getRouteSelectionDescriptor();
                                    //Log.d(TAG, "URSP" + urspRule);
                                    //Log.d(TAG, "Traffic Descriptor" + trafficDescriptor);
                                    //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);
                                //}
                            }
                            @Override
                            public void onError(
                                    @NonNull TelephonyManager.NetworkSlicingException error) {
                                OutcomeReceiver.super.onError(error);
                                Log.d(TAG, "Slice Config rejected!");
                            }
                        });
                }
            } catch (Exception e) {
                Log.d(TAG, "Network slice configuration Failed!");
            }
        }
        return flag;
    }

    /**
     * WIP: Get route selection descriptor
     * @return nothing useful jet
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean getRouteSelectionDescriptor() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context != null) {
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        List<UrspRule> urspRuleList = result.getUrspRules();
                                        for (int i = 0; i < urspRuleList.size(); i++) {
                                            UrspRule urspRule = result.getUrspRules().get(i);
                                            List<RouteSelectionDescriptor> routeSelectionDescriptorsList = urspRule.getRouteSelectionDescriptor();
                                            for (int j = 0; j < routeSelectionDescriptorsList.size(); j++) {
                                                RouteSelectionDescriptor routeSelectionDescriptor = routeSelectionDescriptorsList.get(j);
                                                if (routeSelectionDescriptor != null) {
                                                    Log.d(TAG, "Route Selection Descriptor Available");
                                                    List<String> dataNetworkNameList = routeSelectionDescriptor.getDataNetworkName();
                                                    for (int k = 0; k < dataNetworkNameList.size(); k++) {
                                                        Log.d(TAG, "Data Network Name DNN: " + dataNetworkNameList.get(i));
                                                    }
                                                    Log.d(TAG, "Route Selection Precedence: " + routeSelectionDescriptor.getPrecedence());
                                                    Log.d(TAG, "Route Selection Session Type: " + routeSelectionDescriptor.getSessionType());
                                                    Log.d(TAG, "Route Selection SSC Mode: " + routeSelectionDescriptor.getSscMode());
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onError(@NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Slice Config rejected!");
                                    }
                                });
                        flag = true;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Network slice configuration Failed!");
            }
        }
        return flag;
    }

    /**
     * WIP: get Traffic Descriptor
     * @return nothing useful jet
     */
    @SuppressLint({"MissingPermission", "ObsoleteSdkInt"})
    public boolean getTrafficDescriptor() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context != null) {
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        flag = true;
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    List<UrspRule> urspRuleList = result.getUrspRules();
                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule = result.getUrspRules().get(i);
                                        List<TrafficDescriptor> trafficDescriptorList =
                                                urspRule.getTrafficDescriptors();
                                        for (int j = 0; i < trafficDescriptorList.size(); i++) {
                                            TrafficDescriptor trafficDescriptor =
                                                    urspRule.getTrafficDescriptors().get(i);
                                            if (trafficDescriptor != null) {
                                                Log.d(TAG, "Traffic Descriptor Available");
                                                Log.d(TAG, "Traffic Descriptor DNN: " + trafficDescriptor.getDataNetworkName());
                                                Log.d(TAG, "Traffic Descriptor Os App ID: " + Arrays.toString(trafficDescriptor.getOsAppId()));
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onError(
                                        @NonNull TelephonyManager.NetworkSlicingException error) {
                                    OutcomeReceiver.super.onError(error);
                                    Log.d(TAG, "Traffic Descriptor Failed!");
                                }
                            });
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Traffic Descriptor Failed!");
            }
        }
        return flag;
    }

    /**
     * Check if IMS is supported on the current connection
     * @return Boolean if IMS is supported
     */
    public Boolean getCapabilityIMS() {
        boolean flag = false;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                flag = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_IMS);
            }
        }
        return flag;
    }

    /**
     * Get a String List of DNS servers
     * @return String list of DNS Server
     */
    public List<String> getDefaultDNS() {
        List<String> listDns = new ArrayList<>();
        listDns.add("N/A");
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            assert linkProperties != null;
            List<InetAddress> dns = linkProperties.getDnsServers();
            if (!dns.isEmpty()) {
                listDns.clear();
                for (InetAddress d : dns) {
                    listDns.add(Objects.requireNonNull(d.getHostAddress()).split("%")[0]);
                }
            }
        }
        return listDns;
    }

    /**
     * Get the network object of the current connection
     * @return Network object
     */
    public Network getCurrentNetwork() {
        return connectivityManager.getActiveNetwork();
    }

    /**
     * Get the name of the interface currently used for data
     * @return String of interface name
     */
    public String getInterfaceName() {
        String interfaceName;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            assert linkProperties != null;
            interfaceName = linkProperties.getInterfaceName();
        } else {
            interfaceName = "N/A";
        }
        return interfaceName;
    }


    /**
     * WIP: Get the PLMN from the NetworkRegistrationInfo API
     * This currently returns only the last PLMN in the List
     * @return String of PLMN
     */
    @SuppressLint("ObsoleteSdkInt")
    public String getRegisterPLMNFromNetworkRegistrationInfo() {
        String regPLMN = "N/A";
        List<NetworkRegistrationInfo> networkRegistrationInfo;
        @SuppressLint("MissingPermission")
        ServiceState serviceState = tm.getServiceState();
        if (serviceState.getState() == 1) {  //2 for DATA_CONNECTED 1 FOR DATA_CONNECTING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                networkRegistrationInfo = serviceState.getNetworkRegistrationInfoList();
                for (NetworkRegistrationInfo nri: networkRegistrationInfo) {
                    regPLMN = nri.getRegisteredPlmn();
                    Log.d(TAG, "Registered PLMN" + regPLMN);
                }
            }
        } else {
            Log.d(TAG, "Network Registration Info Unavailable! Check Network State");
        }
        return regPLMN;
    }

    /**
     * Get a String of network capabilities of the current data connection
     * @return String of capabilities
     */
    @SuppressLint("ObsoleteSdkInt")
    public String getNetworkCapabilityList() {
        // Create a StringBuilder to build the display string
        StringBuilder networkCapabilitiesBuilder = new StringBuilder();
        // Create a String that is passed on from string builder.
        String networkAllCapabilities = "N/A";
        //List of Ints for Capabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int[] capability;
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        networkCapabilitiesBuilder.append("Cellular\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_MMS)) {
                        networkCapabilitiesBuilder.append("MMS\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_SUPL)) {
                        networkCapabilitiesBuilder.append("SUPL\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_DUN)) {
                        networkCapabilitiesBuilder.append("DUN\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE)) {
                        networkCapabilitiesBuilder.append("ENTERPRISE\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        networkCapabilitiesBuilder.append("VALIDATED\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_IMS)) {
                        networkCapabilitiesBuilder.append("IMS\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CBS)) {
                        networkCapabilitiesBuilder.append("CBS\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) {
                        networkCapabilitiesBuilder.append("CAPTIVE PORTAL\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_EIMS)) {
                        networkCapabilitiesBuilder.append("EIMS\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)) {
                        networkCapabilitiesBuilder.append("FOREGROUND\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOTA)) {
                        networkCapabilitiesBuilder.append("FOTA\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        networkCapabilitiesBuilder.append("INTERNET\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_BANDWIDTH)) {
                        networkCapabilitiesBuilder.append("PRIORITIZE BANDWIDTH\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_LATENCY)) {
                        networkCapabilitiesBuilder.append("PRIORITIZE LATENCY\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)) {
                        networkCapabilitiesBuilder.append("TRUSTED\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)) {
                        networkCapabilitiesBuilder.append("NOT CONGESTED\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                        networkCapabilitiesBuilder.append("NOT METERED\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                        networkCapabilitiesBuilder.append("NOT RESTRICTED\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                        networkCapabilitiesBuilder.append("NOT VPN\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)) {
                        networkCapabilitiesBuilder.append("NOT ROAMING\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) {
                        networkCapabilitiesBuilder.append("NOT SUSPENDED\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED)) {
                        networkCapabilitiesBuilder.append("TEMPORARILY NOT METERED\n");
                    }
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        networkCapabilitiesBuilder.append("Wi-Fi\n");
                    }
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_WIFI_P2P)) {
                        networkCapabilitiesBuilder.append("Wi-Fi P2P\n");
                    }
                }
                networkAllCapabilities = networkCapabilitiesBuilder.toString();
            }
        }
        return networkAllCapabilities;
    }

    /**
     * WIP: Get Network Slice information
     * @return nothing useful jet
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean getNetworkSlicingInfo() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                    tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    List<UrspRule> urspRuleList = result.getUrspRules();
                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule = result.getUrspRules().get(i);
                                        List<RouteSelectionDescriptor> routeSelectionDescriptorsList = urspRule.getRouteSelectionDescriptor();
                                        for (int j = 0; j < routeSelectionDescriptorsList.size(); j++) {
                                            RouteSelectionDescriptor routeSelectionDescriptor = routeSelectionDescriptorsList.get(i);
                                            if (routeSelectionDescriptor != null) {
                                                List<NetworkSliceInfo> networkSliceInfoList = routeSelectionDescriptor.getSliceInfo();
                                                Log.d(TAG, "Network Slices Available: " + networkSliceInfoList.size());
                                                for (int k = 0; k < networkSliceInfoList.size(); k++) {
                                                    NetworkSliceInfo networkSliceInfo = networkSliceInfoList.get(i);
                                                    Log.d(TAG, "Slice Differentiator: " + networkSliceInfo.getSliceDifferentiator());
                                                    Log.d(TAG, "Mapped PLMN Slice Differentiator: " + networkSliceInfo.getMappedHplmnSliceDifferentiator());
                                                    Log.d(TAG, "Slice PLMN Service Type: " + networkSliceInfo.getMappedHplmnSliceServiceType());
                                                    Log.d(TAG, "Slice Service Type: " + networkSliceInfo.getSliceServiceType());
                                                }
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onError(@NonNull TelephonyManager.NetworkSlicingException error) {
                                    OutcomeReceiver.super.onError(error);
                                    Log.d(TAG, "Slice Info Failed");
                                }
                            });
                    flag = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "Slice Info Failed!");
            }
        }
        return flag;
    }

    /**
     * WIP: Get network Slicing Rules ...
     * @return nothing useful jet
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean getNetworkSlicingConfig() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context != null) {
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        List<UrspRule> urspRuleList = result.getUrspRules();
                                        for (int i = 0; i < urspRuleList.size(); i++) {
                                            UrspRule urspRule = result.getUrspRules().get(i);
                                            List<RouteSelectionDescriptor> routeSelectionDescriptorsList = urspRule.getRouteSelectionDescriptor();
                                            for (int j = 0; j < routeSelectionDescriptorsList.size(); j++) {
                                                RouteSelectionDescriptor routeSelectionDescriptor = routeSelectionDescriptorsList.get(j);
                                                Log.d(TAG, "Route Selection" + routeSelectionDescriptor);
                                                if (routeSelectionDescriptor != null) {
                                                    Log.d(TAG, "Route Selection Descriptor Available");
                                                    List<String> dataNetworkNameList = routeSelectionDescriptor.getDataNetworkName();
                                                    List<NetworkSliceInfo> networkSliceInfoList = routeSelectionDescriptor.getSliceInfo();
                                                    for (int k = 0; k < dataNetworkNameList.size(); k++) {
                                                        Log.d(TAG, "Data Network Name DNN: " + dataNetworkNameList.get(k));
                                                    }
                                                    for (int l = 0; l < networkSliceInfoList.size(); l++) {
                                                        NetworkSliceInfo sliceInfo = networkSliceInfoList.get(l);
                                                        Log.d(TAG, "Network Slice Status: " + sliceInfo.getStatus());
                                                        Log.d(TAG, "Network Slice Service Type: " + sliceInfo.getSliceServiceType());
                                                        Log.d(TAG, "Network Slice Differentiator: " + sliceInfo.getSliceDifferentiator());
                                                        Log.d(TAG, "Network HPLMN Service Type" + sliceInfo.getMappedHplmnSliceServiceType());
                                                        Log.d(TAG, "Network HPLMN Differentiator: " + sliceInfo.getMappedHplmnSliceDifferentiator());
                                                    }
                                                    Log.d(TAG, "Route Selection Precedence: " + routeSelectionDescriptor.getPrecedence());
                                                    Log.d(TAG, "Route Selection Session Type: " + routeSelectionDescriptor.getSessionType());
                                                    Log.d(TAG, "Route Selection SSC Mode: " + routeSelectionDescriptor.getSscMode());
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onError(@NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Slice Config rejected!");
                                    }
                                });
                        flag = true;
                    }
                } else {
                    Log.d(TAG, "Context returned Null!!");
                }
            } catch (Exception e) {
                Log.d(TAG, "Network slice configuration Failed!");
            }
        }
        return flag;
    }

    /**
     * Check if the current network has the capability VALIDATE
     * @return Boolean of capability VALIDATE
     */
    public Boolean getCapabilityValidity() {
        boolean flag = false;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                flag = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }
        return flag;
    }

    /**
     * Get a list of enterprise ids
     * @return int list of ids, empty list if no id available
     */
    public int[] getEnterpriseIds() {
        int[] enterpriseId = {};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    enterpriseId = networkCapabilities.getEnterpriseIds();
                }
            }
        }
        return enterpriseId;
    }

    /**
     * Check if the current connection has the capability INTERNET
     * @return Boolean of INTERNET Capability
     */
    public Boolean getCapabilityInternet() {
        boolean flag = false;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                flag = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return flag;
    }

    /**
     * WIP: Request a Network callback for network availability
     */
    public void requestNetworkCallback() {
        try {
            Network network = connectivityManager.getActiveNetwork();
            if (connectivityManager.isDefaultNetworkActive()) {
                Toast.makeText(context.getApplicationContext(), "Network:" + network, Toast.LENGTH_SHORT).show();
            }
            NetworkRequest.Builder builder = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_CBS);
            connectivityManager.requestNetwork(builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            super.onAvailable(network);
                            GlobalVars.isNetworkConnected = true;
                            Log.d(TAG, "onAvailable");
                        }
                        @Override
                        public void onUnavailable() {
                            super.onUnavailable();
                            Log.d(TAG, "onUnavailable");
                        }
                    });
        } catch (Exception e) {
            Log.d("Network Callback: Exception in requestNetworkCallback", "Catch exception RequestCallback");
            GlobalVars.isNetworkConnected = false;
        }
    }

    /**
     * WIP: register a default network callback
     */
    public void registerDefaultNetworkCallback() {
        try {
            Network network = connectivityManager.getActiveNetwork();
            Toast.makeText(context.getApplicationContext(), "Default Network: " + network, Toast.LENGTH_SHORT).show();
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            GlobalVars.isNetworkConnected = true;
                            Log.d(TAG, "onAvailable");
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            GlobalVars.isNetworkConnected = false;
                            Log.d(TAG, "onLost");
                        }

                        @Override
                        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                            super.onBlockedStatusChanged(network, blocked);
                            Log.d(TAG, "onBlockedStatusChanged");

                        }

                        @Override
                        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                            super.onCapabilitiesChanged(network, networkCapabilities);
                            Log.d(TAG, "onCapabilitiesChanged");
                        }

                        @Override
                        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                            super.onLinkPropertiesChanged(network, linkProperties);
                            Log.d(TAG, "onLinkPropertiesChanged");
                        }

                        @Override
                        public void onLosing(@NonNull Network network, int maxMsToLive) {
                            super.onLosing(network, maxMsToLive);
                            Log.d(TAG, "onLosing");
                        }

                        @Override
                        public void onUnavailable() {
                            super.onUnavailable();
                            Log.d(TAG, "onUnavailable");
                        }
                    });
        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            GlobalVars.isNetworkConnected = false;
        }
    }

    /**
     * WIP: Callback requester with parameters
     * @param capability id
     * @param transport_type id
     * @return Boolean id capability is supported
     */
    @SuppressLint("ObsoleteSdkInt")
    public boolean customNetworkCallback(int capability, int transport_type) {
        try {
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .addCapability(capability)
                    .addTransportType(transport_type);

            connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull android.net.Network network) {
                            super.onAvailable(network);
                            GlobalVars.isNetworkConnected = true; // todo this needs to be handled somewhere else
                        }

                        @Override
                        public void onLost(@NonNull android.net.Network network) {
                            super.onLost(network);
                            GlobalVars.isNetworkConnected = false;
                            Log.d(TAG, "onLost"); // todo this needs to be handled somewhere else
                        }

                        @Override
                        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                            super.onBlockedStatusChanged(network, blocked);
                        }

                        @Override
                        public void onCapabilitiesChanged(@NonNull Network network, @NonNull
                        NetworkCapabilities networkCapabilities) {
                            super.onCapabilitiesChanged(network, networkCapabilities);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                int[] enterpriseID = new int[0];
                                enterpriseID = networkCapabilities.getEnterpriseIds();
                                Log.d(TAG, " Enterprise IDs: " + Arrays.toString(enterpriseID));
                            }
                            Log.d(TAG, " does it have validated network connection internet presence : "
                                    + networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                    + " is it validated "
                                    + networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) && !GlobalVars.isNetworkConnected) {
                                    GlobalVars.isNetworkConnected = true; // todo this needs to be handled somewhere else
                                } else if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) && GlobalVars.isNetworkConnected) {
                                    // handles the scenario when the internet is blocked by ISP,
                                    // or when the dsl/fiber/cable line to the router is disconnected
                                    GlobalVars.isNetworkConnected = false; // todo this needs to be handled somewhere else
                                    Log.d(TAG, " Internet Connection is lost temporarily for network: " + network);
                                }
                            }
                        }

                        @Override
                        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                            super.onLinkPropertiesChanged(network, linkProperties);
                            Log.d(TAG, "onLinkPropertiesChanged: " + network);

                        }

                        @Override
                        public void onLosing(@NonNull Network network, int maxMsToLive) {
                            super.onLosing(network, maxMsToLive);
                            Log.d(TAG, "onLosing" + network);
                        }

                        @Override
                        public void onUnavailable() {
                            super.onUnavailable();
                            Log.d(TAG, "onUnavailable");
                        }
                    });
            return true;
        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            GlobalVars.isNetworkConnected = false; // todo this needs to be handled somewhere else
        }
        return false;
    }
}