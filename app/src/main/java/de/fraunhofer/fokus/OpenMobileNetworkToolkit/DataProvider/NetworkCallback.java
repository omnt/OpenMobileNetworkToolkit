/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
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
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.OutcomeReceiver;
import android.telephony.CellInfo;
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
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;

public class NetworkCallback {
    private static final String TAG = "NETWORK_CALLBACK";
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final TelephonyManager tm;

    public NetworkCallback(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        tm = GlobalVars.getInstance().getTm();
    }

    public boolean getEnterpriseCapability() {
        boolean enterprise = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    enterprise = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                }
            }
        }
        return enterprise;
    }

    //TODO COMPLETE THIS FOR NETWORK REGISTRATION INFO
    public boolean getNetworkRegistrationInfo() {
        boolean flag = false;
        List<NetworkRegistrationInfo> networkRegistrationInfo = new ArrayList<>();
        List<String> networkRegistrationInfoList = new ArrayList<>();
        @SuppressLint("MissingPermission")
        ServiceState serviceState = tm.getServiceState(); //todo add permission check
        Log.d(TAG, "Service State: " + serviceState.getState());
        if (serviceState.getState() == 1) {  //2 for DATA_CONNECTED 1 FOR DATA_CONNECTING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                networkRegistrationInfo = serviceState.getNetworkRegistrationInfoList();
                for (int i = 0; i < networkRegistrationInfoList.size(); i++) {
                    Log.d(TAG, "Network Registration Info " + networkRegistrationInfo);
                    String netwrokRegistrationInfoString = networkRegistrationInfo.toString();
                    networkRegistrationInfoList.add(netwrokRegistrationInfoString);
                }
                flag = true;
            }
        } else {
            Log.d(TAG, "Network Registration Info Unavailable! Check Network State");
            flag = false;
        }
        return flag;
    }

    public List<String> getFeatureList() {
        FeatureInfo[] feature = null;
        FeatureInfo featureInfo = null;
        List<String> featureString = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        feature = pm.getSystemAvailableFeatures();
        for (FeatureInfo info : feature) {
            featureInfo = info;
            Log.d(TAG, "Feature: " + Arrays.toString(feature));
            featureString.add(featureInfo.toString());
        }
        return featureString;
    }

    @RequiresPermission(value = "android.permission.READ_PRIVILEGED_PHONE_STATE")
    public boolean getConfigurationTM() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PackageManager pm = context.getPackageManager();
            boolean capability_slicing =
                    pm.hasSystemFeature(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED);
            MainActivity ma = new MainActivity();
            Log.d(TAG, "CAPABILITY SLICING: " + capability_slicing);
            try {
                /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                    tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    List<UrspRule> urspRuleList = result.getUrspRules();

                                    Log.d(TAG, "Slice config works!!");
                                    Log.d(TAG, "URSP List: " + urspRuleList);
                                    Log.d(TAG, "URSP received: " + urspRuleList.size());

                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule =
                                                result.getUrspRules().get(i);
                                        List<TrafficDescriptor> trafficDescriptor =
                                                urspRule.getTrafficDescriptors();
                                        List<RouteSelectionDescriptor> routeSelectionDescriptor =
                                                urspRule.getRouteSelectionDescriptor();
                                        Log.d(TAG, "URSP" + urspRule);
                                        Log.d(TAG, "Traffic Descriptor" + trafficDescriptor);
                                        Log.d(TAG, "Route Selection" + routeSelectionDescriptor);
                                    }
                                }
                                @Override
                                public void onError(
                                        @NonNull TelephonyManager.NetworkSlicingException error) {
                                    OutcomeReceiver.super.onError(error);
                                    Log.d(TAG, "Slice Config rejected!");
                                }
                            });
                    flag = true;
                }
                Log.d(TAG, "TM Config:" + flag);
            } catch (Exception e) {
                Log.d(TAG, "Network slice configuration Failed!");
                flag = false;
            }
        }
        return flag;
    }

    public boolean getRouteSelectionDescriptor() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context != null) {
                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        NetworkSlicingConfig networkSlicingConfig = result;
                                        List<UrspRule> urspRuleList =
                                                networkSlicingConfig.getUrspRules();

                                        Log.d(TAG, "Slice Info config function works!!");
                                        for (int i = 0; i < urspRuleList.size(); i++) {
                                            UrspRule urspRule =
                                                    networkSlicingConfig.getUrspRules().get(i);
                                            List<RouteSelectionDescriptor> routeSelectionDescriptorsList = urspRule.getRouteSelectionDescriptor();
                                            for (int j = 0;
                                                 i < routeSelectionDescriptorsList.size(); i++) {
                                                RouteSelectionDescriptor routeSelectionDescriptor =
                                                        routeSelectionDescriptorsList.get(i);
                                                if (routeSelectionDescriptor != null) {
                                                    Log.d(TAG,
                                                            "Route Selection Descriptor Available");
                                                    List<String> dataNetworkNameList =
                                                            routeSelectionDescriptor.getDataNetworkName();

                                                    for (int k = 0;
                                                         k < dataNetworkNameList.size(); k++) {
                                                        Log.d(TAG, "Data Network Name DNN: " +
                                                                dataNetworkNameList.get(i));
                                                    }
                                                    Log.d(TAG, "Route Selection Precedence: " +
                                                            routeSelectionDescriptor.getPrecedence());
                                                    Log.d(TAG, "Route Selection Session Type: " +
                                                            routeSelectionDescriptor.getSessionType());
                                                    Log.d(TAG, "Route Selection SSC Mode: " +
                                                            routeSelectionDescriptor.getSscMode());
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(
                                            @NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Slice Config rejected!");
                                    }
                                });
                        flag = true;
                    }
                } else {
                    flag = false;
                }
                Log.d(TAG, "TM Config:" + flag);

            } catch (Exception e) {
                Log.d(TAG, "Network slice configuration Failed!");
                flag = false;
            }
        }
        return flag;
    }

    @SuppressLint("MissingPermission")
    public boolean getTrafficDescriptor() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
                List<UrspRule> urspRuleList = new ArrayList<>();
                List<RouteSelectionDescriptor> routeSelectionDescriptorsList = new ArrayList<>();
                List<TrafficDescriptor> trafficDescriptorList = new ArrayList<>();
                if (context != null) {
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        List<UrspRule> urspRuleList =
                                                result.getUrspRules();
                                        Log.d(TAG, "Traffic Descriptor function works!!");

                                        for (int i = 0; i < urspRuleList.size(); i++) {
                                            UrspRule urspRule =
                                                    result.getUrspRules().get(i);
                                            List<TrafficDescriptor> trafficDescriptorList =
                                                    urspRule.getTrafficDescriptors();
                                            for (int j = 0; i < trafficDescriptorList.size(); i++) {
                                                TrafficDescriptor trafficDescriptor =
                                                        urspRule.getTrafficDescriptors().get(i);
                                                //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);

                                                if (trafficDescriptor != null) {
                                                    Log.d(TAG, "Traffic Descriptor Available");
                                                    Log.d(TAG, "Traffic Descriptor DNN: " +
                                                            trafficDescriptor.getDataNetworkName());
                                                    Log.d(TAG, "Traffic Descriptor Os App ID: " +
                                                            Arrays.toString(trafficDescriptor.getOsAppId()));

                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(
                                            @NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Traffi Descriptor Failed!");
                                    }
                                });
                        flag = true;
                    }
                } else {
                    flag = false;
                }
                Log.d(TAG, "TM Config:" + flag);

            } catch (Exception e) {
                Log.d(TAG, "Traffic Descriptor Failed!");
                flag = false;
            }

            return flag;
        }
        return false;
    }

    public Boolean getIMS() {
        boolean imsCapability = false;
        assert context != null;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                imsCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_IMS);
            }
        }
        return imsCapability;
    }

    public List<String> getDefaultDNS() {
        List<String> listDns = new ArrayList<>();
        if (context != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
                assert linkProperties != null;
                List<InetAddress> dns = linkProperties.getDnsServers();
                for (InetAddress d : linkProperties.getDnsServers()) {
                    Log.d(TAG, "DNS from LinkProperties: " + d.getHostAddress());
                    listDns.add(Objects.requireNonNull(d.getHostAddress()).split("%")[0]);
                }
            }
        } else {
            Log.d(TAG, "Context not found!");
        }
        return listDns;
    }

    public Network getCurrentNetwork() {
        return connectivityManager.getActiveNetwork();
    }

    public String getInterfaceName() {
        String interfaceName;
        Network network = connectivityManager.getActiveNetwork();

        if (network != null) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            assert linkProperties != null;
            interfaceName = linkProperties.getInterfaceName();
        } else {
            interfaceName = "null";
        }
        return interfaceName;
    }

    public Boolean getNetworkCapabilities() {
        boolean enterprise = false;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities =
                    connectivityManager.getNetworkCapabilities(network);
            Log.d(TAG, "Network Capabilities: " + networkCapabilities);
            if (networkCapabilities != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    enterprise = networkCapabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                }
                if (enterprise) {
                    Log.d(TAG, "Enterprise Capabilities available for Network!");
                }
            }
        }
        return enterprise;
    }

    public String getPLMN() {
        String plmn = null;
        Network network = connectivityManager.getActiveNetwork();

        if (network != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            int networktype = networkInfo.getType();
            int networksubtype = networkInfo.getSubtype();
            String typename = networkInfo.getTypeName();
            String subtypename = networkInfo.getSubtypeName();
            Log.d(TAG, "Network Type: " + networktype);
            Log.d(TAG, "Network Type Sub: " + networksubtype);
            Log.d(TAG, "Type Name " + typename);
            Log.d(TAG, "Subtype Name " + subtypename);
            if (tm != null) {
                @SuppressLint("MissingPermission")
                ServiceState serviceState = tm.getServiceState(); // todo handle this according to the privileges granted the app
                if (serviceState != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        List<NetworkRegistrationInfo> networkRegistrationInfoList =
                                serviceState.getNetworkRegistrationInfoList();
                        for (int i = 0; i < networkRegistrationInfoList.size(); i++) {
                            NetworkRegistrationInfo networkRegistrationInfo =
                                    networkRegistrationInfoList.get(i);
                            if (networkRegistrationInfo != null) {
                                plmn = networkRegistrationInfo.getRegisteredPlmn();
                                Log.d(TAG, "PLMN: " + plmn);
                            } else {
                                Log.d(TAG, "PLMN unavailable");
                            }
                        }
                    } else {
                        plmn = ""; // todo use old API here
                    }
                } else {
                    Log.d(TAG, "Missing permission to access service state");
                }
            } else {
                Log.d(TAG, "This is not a phone");
            }
        }
        return plmn;
    }

    public String getRegisterPLMNfromNetworkRegistrationInfo() {
        String regPLMN = null;
        List<NetworkRegistrationInfo> networkRegistrationInfo = new ArrayList<>();
        List<String> networkRegistrationInfoList = new ArrayList<>();
        @SuppressLint("MissingPermission")
        ServiceState serviceState =
                tm.getServiceState(); //todo this should be guarded by an permission check
        Log.d(TAG, "Service State: " + serviceState.getState());
        if (serviceState.getState() == 1) {  //2 for DATA_CONNECTED 1 FOR DATA_CONNECTING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                networkRegistrationInfo = serviceState.getNetworkRegistrationInfoList();
                for (int i = 0; i < networkRegistrationInfoList.size();
                     i++) { // todo this is always 0 as an empty array was assigned before
                    Log.d(TAG, "Size of List :" + networkRegistrationInfoList.size());
                    NetworkRegistrationInfo networkRegistrationInfo1 =
                            networkRegistrationInfo.get(i);
                    regPLMN = networkRegistrationInfo1.getRegisteredPlmn();
                    Log.d(TAG,
                            "Network Registration Info " + networkRegistrationInfoList.toString());
                    Log.d(TAG, "Registered PLMN" + regPLMN);
                }
            }
        } else {
            Log.d(TAG, "Network Registration Info Unavailable! Check Network State");
            regPLMN = "Could not recieve PLMN";
        }
        return regPLMN;
    }

    public String getNetworkCapabilitylist() {

        // Create a StringBuilder to build the display string
        StringBuilder networkCapabilitiesBuilder = new StringBuilder();

        // Create a String that is passed on from string builder.
        String networkAllCapabilities = "";

        //List of Ints for Capabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int[] capability;
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities =
                        connectivityManager.getNetworkCapabilities(network);

                // Iterate over the network capabilities and extract the information that you are interested in
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        networkCapabilitiesBuilder.append("Cellular\n");
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_MMS)) {
                            networkCapabilitiesBuilder.append("MMS\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_SUPL)) {
                            networkCapabilitiesBuilder.append("SUPL\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_DUN)) {
                            networkCapabilitiesBuilder.append("DUN\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_ENTERPRISE)) {
                            networkCapabilitiesBuilder.append("ENTERPRISE\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            networkCapabilitiesBuilder.append("VALIDATED\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_IMS)) {
                            networkCapabilitiesBuilder.append("IMS\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_CBS)) {
                            networkCapabilitiesBuilder.append("CBS\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) {
                            networkCapabilitiesBuilder.append("CAPTIVE PORTAL\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_EIMS)) {
                            networkCapabilitiesBuilder.append("EIMS\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_FOREGROUND)) {
                            networkCapabilitiesBuilder.append("FOREGROUND\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_FOTA)) {
                            networkCapabilitiesBuilder.append("FOTA\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                            networkCapabilitiesBuilder.append("INTERNET\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_BANDWIDTH)) {
                            networkCapabilitiesBuilder.append("PRIORITIZE BANDWIDTH\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_LATENCY)) {
                            networkCapabilitiesBuilder.append("PRIORITIZE LATENCY\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_TRUSTED)) {
                            networkCapabilitiesBuilder.append("TRUSTED\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)) {
                            networkCapabilitiesBuilder.append("NOT CONGESTED\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                            networkCapabilitiesBuilder.append("NOT METERED\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                            networkCapabilitiesBuilder.append("NOT RESTRICTED\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                            networkCapabilitiesBuilder.append("NOT VPN\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)) {
                            networkCapabilitiesBuilder.append("NOT ROAMING\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) {
                            networkCapabilitiesBuilder.append("NOT SUSPENDED\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED)) {
                            networkCapabilitiesBuilder.append("TEMPORARILY NOT METERED\n");
                        } else {
                            //networkCapabilitiesBuilder.append(
                            Log.d(TAG, "Unknown Capability Received!");
                        }
                        // Add more CAPABILITIES as needed
                    }
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        networkCapabilitiesBuilder.append("Wi-Fi\n");
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_WIFI_P2P)) {
                            networkCapabilitiesBuilder.append("Wi-Fi P2P\n");
                        }
                        if (networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                            networkCapabilitiesBuilder.append("WIFI INTERNET\n");
                        }
                    }
                    // Add more transports as needed
                }

                if (networkCapabilities != null) {
                    capability = networkCapabilities.getCapabilities();
                    for (int j : capability) {
                        Log.d(TAG, "Capability from Network: " + j);

                    }
                }
                networkAllCapabilities = networkCapabilitiesBuilder.toString();
            }
        }
        return networkAllCapabilities;
    }

    public boolean getNetworkSlicingInfo() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context != null) {
                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        List<UrspRule> urspRuleList = result.getUrspRules();
                                        Log.d(TAG, "Slice Info config function works!!");
                                        for (int i = 0; i < urspRuleList.size(); i++) {
                                            UrspRule urspRule =
                                                    result.getUrspRules().get(i);
                                            List<RouteSelectionDescriptor>
                                                    routeSelectionDescriptorsList =
                                                    urspRule.getRouteSelectionDescriptor();

                                            for (int j = 0; j < routeSelectionDescriptorsList.size(); j++) {
                                                RouteSelectionDescriptor routeSelectionDescriptor =
                                                        routeSelectionDescriptorsList.get(i);

                                                if (routeSelectionDescriptor != null) {
                                                    List<NetworkSliceInfo> networkSliceInfoList =
                                                            routeSelectionDescriptor.getSliceInfo();
                                                    Log.d(TAG, "Network Slices Available: " +
                                                            networkSliceInfoList.size());
                                                    for (int k = 0; k < networkSliceInfoList.size();
                                                         k++) {
                                                        NetworkSliceInfo networkSliceInfo =
                                                                networkSliceInfoList.get(i);
                                                        Log.d(TAG, "Slice Differentiator: " +
                                                                networkSliceInfo.getSliceDifferentiator());
                                                        Log.d(TAG,
                                                                "Mapped PLMN Slice Differentiator: " +
                                                                        networkSliceInfo.getMappedHplmnSliceDifferentiator());
                                                        Log.d(TAG, "Slice PLMN Service Type: " +
                                                                networkSliceInfo.getMappedHplmnSliceServiceType());
                                                        Log.d(TAG, "Slice Service Type: " +
                                                                networkSliceInfo.getSliceServiceType());

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(
                                            @NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Slice Info Failed");
                                    }
                                });
                        flag = true;
                    }
                } else {
                    flag = false;
                }


            } catch (Exception e) {
                Log.d(TAG, "Slice Info Failed!");
                flag = false;
            }
        }
        return flag;
    }

    public boolean getNetworkSlicingConfig() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context != null) {
                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        List<UrspRule> urspRuleList = result.getUrspRules();

                                        for (int i = 0; i < urspRuleList.size(); i++) {
                                            UrspRule urspRule =
                                                    result.getUrspRules().get(i);
                                            List<RouteSelectionDescriptor>
                                                    routeSelectionDescriptorsList =
                                                    urspRule.getRouteSelectionDescriptor();

                                            for (int j = 0;
                                                 i < routeSelectionDescriptorsList.size(); i++) {
                                                RouteSelectionDescriptor routeSelectionDescriptor =
                                                        routeSelectionDescriptorsList.get(i);
                                                Log.d(TAG,
                                                        "Route Selection" + routeSelectionDescriptor);

                                                if (routeSelectionDescriptor != null) {
                                                    Log.d(TAG,
                                                            "Route Selection Descriptor Available");
                                                    List<String> dataNetworkNameList =
                                                            routeSelectionDescriptor.getDataNetworkName();
                                                    List<NetworkSliceInfo> networkSliceInfoList =
                                                            routeSelectionDescriptor.getSliceInfo();

                                                    for (int k = 0;
                                                         k < dataNetworkNameList.size(); k++) {
                                                        Log.d(TAG, "Data Network Name DNN: " +
                                                                dataNetworkNameList.get(i));
                                                    }

                                                    for (int l = 0;
                                                         l < networkSliceInfoList.size(); l++) {
                                                        NetworkSliceInfo sliceInfo =
                                                                networkSliceInfoList.get(i);
                                                        Log.d(TAG, "Network Slice Status: " +
                                                                sliceInfo.getStatus());
                                                        Log.d(TAG,
                                                                "Network Slice Service Type: " +
                                                                        sliceInfo.getSliceServiceType());
                                                        Log.d(TAG,
                                                                "Network Slice Differentiator: " +
                                                                        sliceInfo.getSliceDifferentiator());
                                                        Log.d(TAG,
                                                                "Network HPLMN Service Type" +
                                                                        sliceInfo.getMappedHplmnSliceServiceType());
                                                        Log.d(TAG,
                                                                "Network HPLMN Differentiator: " +
                                                                        sliceInfo.getMappedHplmnSliceDifferentiator());

                                                    }
                                                    Log.d(TAG, "Route Selection Precedence: " +
                                                            routeSelectionDescriptor.getPrecedence());
                                                    Log.d(TAG, "Route Selection Session Type: " +
                                                            routeSelectionDescriptor.getSessionType());
                                                    Log.d(TAG, "Route Selection SSC Mode: " +
                                                            routeSelectionDescriptor.getSscMode());

                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(
                                            @NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Slice Config rejected!");
                                    }
                                });
                        flag = true;
                    }
                } else {
                    Log.d(TAG, "Context returned Null!!");
                    flag = false;
                }

            } catch (Exception e) {
                Log.d(TAG, "Network slice configuration Failed!");
                flag = false;
            }
        }
        return flag;
    }

    public void getURSPrules() {
        List<RouteSelectionDescriptor> routeSelectionDescriptor = new ArrayList<>();
        List<TrafficDescriptor> trafficDescriptor = new ArrayList<>();
        /* TODO: Get URSP rules from network */
        UrspRule urspRule = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            /* TODO: Get routeSelection and trafficDescriptor from URSP rules */
            routeSelectionDescriptor = urspRule.getRouteSelectionDescriptor();
            trafficDescriptor = urspRule.getTrafficDescriptors();
        }
    }

    public Boolean getValidity() {
        boolean validated = false;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities =
                    connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                validated =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }
        return validated;
    }

    public int getEnterpriseIds() {
        int[] enterpriseId = null;
        int enterpriseIDint = CellInfo.UNAVAILABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    enterpriseId = networkCapabilities.getEnterpriseIds();
                    Log.d(TAG, "Enterprise IDs: " + Arrays.toString(enterpriseId));
                }
                for (int i : enterpriseId) {
                    enterpriseIDint = i;
                    Log.d(TAG, "Enterprise ID: " + enterpriseIDint);
                }
            }
        }
        return enterpriseIDint;
    }

    public Boolean getInternet() {
        boolean internetCapability = false;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                internetCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return internetCapability;
    }

    public boolean getEnterpriseIdList() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            List<Integer> enterpriseIDList = new ArrayList<>();
            int[] enterpriseID = null;
            try {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                enterpriseID = networkCapabilities.getEnterpriseIds();
                for (int j : enterpriseID) {
                    int entID;
                    entID = j;
                    Log.d(TAG, "Enterprise ID: " + entID);
                    enterpriseIDList.add(entID);
                    flag = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "Enterprise Function not found!! ");
                flag = false;
            }
        }
        return flag;
    }

    @SuppressLint("MissingPermission")
    public NetworkSlicingConfig getConfig() {
        NetworkSlicingConfig networksliceConfig = null;
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
                List<UrspRule> urspRuleList = new ArrayList<>();
                List<RouteSelectionDescriptor> routeSelectionDescriptorsList = new ArrayList<>();
                List<TrafficDescriptor> trafficDescriptorList = new ArrayList<>();

                if (context != null) {
                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        tm.getNetworkSlicingConfiguration(context.getMainExecutor(),
                                new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                    @Override
                                    public void onResult(@NonNull NetworkSlicingConfig result) {
                                        NetworkSlicingConfig networkSlicingConfig = result;
                                        Log.d(TAG, "function works!!");
                                    }
                                    @Override
                                    public void onError(
                                            @NonNull TelephonyManager.NetworkSlicingException error) {
                                        OutcomeReceiver.super.onError(error);
                                        Log.d(TAG, "Traffi Descriptor Failed!");
                                    }
                                });
                    }
                } else {
                    flag = false;
                }
                Log.d(TAG, "TM Config:" + flag);
            } catch (Exception e) {
                Log.d(TAG, "Traffic Descriptor Failed!");
            }
        }
        return networksliceConfig;
    }

    //TrafficDescriptor
    public String getDataNetworkNameTrafficDescriptor() {
        String dataNetworkName = null;
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            Log.d(TAG, "Network exists here!!!");
        }
        return dataNetworkName;
    }

    //Check Network
    public void requestNetworkCallback() {
        try {
            Network network = connectivityManager.getActiveNetwork();
            if (connectivityManager.isDefaultNetworkActive()) {
                Toast.makeText(context.getApplicationContext(), "Network:" + network,
                        Toast.LENGTH_SHORT).show();
            }
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_CBS);

            connectivityManager.requestNetwork(builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
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
            Log.d("Network Callback: Exception in requestNetworkCallback",
                    "Catch exception RequestCallback");
            GlobalVars.isNetworkConnected = false;
        }
    }

    public void registerDefaultNetworkCallback() {
        //private static final String TAG = "Network Callback";
        try {
            Network network = connectivityManager.getActiveNetwork();
            Toast.makeText(context.getApplicationContext(), "Default Network: " + network,
                    Toast.LENGTH_SHORT).show();
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            GlobalVars.isNetworkConnected = true;
                            Log.d(TAG, "onAvailable");
                        }

                        @Override
                        public void onLost(Network network) {
                            GlobalVars.isNetworkConnected = false;
                            Log.d(TAG, "onLost");
                        }

                        @Override
                        public void onBlockedStatusChanged(Network network, boolean blocked) {
                            super.onBlockedStatusChanged(network, blocked);
                            Log.d(TAG, "onBlockedStatusChanged");

                        }

                        @Override
                        public void onCapabilitiesChanged(Network network,
                                                          NetworkCapabilities networkCapabilities) {
                            super.onCapabilitiesChanged(network, networkCapabilities);
                            Log.d(TAG, "onCapabilitiesChanged");
                        }

                        @Override
                        public void onLinkPropertiesChanged(Network network,
                                                            LinkProperties linkProperties) {
                            super.onLinkPropertiesChanged(network, linkProperties);
                            Log.d(TAG, "onLinkPropertiesChanged");
                        }

                        @Override
                        public void onLosing(Network network, int maxMsToLive) {
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

    public boolean customNetworkCallback(int capability, int transport_type) {
        boolean flag = false;
        try {
            if (connectivityManager == null) {
                Toast.makeText(context.getApplicationContext(),
                        "Connectivity Manager does not exist: " + connectivityManager,
                        Toast.LENGTH_SHORT).show();
            }

            /* TODO Network Request used for requesting network with Enterprise Capability */
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .addCapability(capability)
                    .addTransportType(transport_type);


            if (builder != null) {
                Log.d(TAG, "Network Request Capability: " + builder.addCapability(capability));
                Log.d(TAG,
                        "Network Request Transport Type: " + builder.addTransportType(transport_type));
            }


            Network network = connectivityManager.getActiveNetwork();
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            NetworkCapabilities networkCapabilities =
                    connectivityManager.getNetworkCapabilities(network);
            boolean validated_capability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            boolean internet_capability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean enterprise_capability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);

            String interfaceName = linkProperties.getInterfaceName();
            //Inet4Address inet4Address = linkProperties.getDhcpServerAddress();

            Log.d(TAG, "Validated Capabilities:" + validated_capability);
            Log.d(TAG, "Internet Capabilities:" + internet_capability);
            Log.d(TAG, "Enterprise Capabilities:" + enterprise_capability);
            Log.d(TAG, "Interface Name: " + interfaceName);
            //Log.d(TAG, "INET4Address: "+ inet4Address.toString());
            Log.d(TAG, "LINK PROPERTIES: " + linkProperties);
            Log.d(TAG, "DNS LIST: " + linkProperties.getDnsServers());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(TAG, "DHCP SERVER ADDRESS: " + linkProperties.getDhcpServerAddress());
            }
            Log.d(TAG, "Network Type Name: " + networkInfo.getTypeName());


            connectivityManager.registerNetworkCallback(builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull android.net.Network network) {
                            super.onAvailable(network);
                            GlobalVars.isNetworkConnected = true;
                            Log.d(TAG, "onAvailable");
                            Toast.makeText(context.getApplicationContext(), "Available",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLost(@NonNull android.net.Network network) {
                            super.onLost(network);
                            GlobalVars.isNetworkConnected = false;
                            Log.d(TAG, "onLost");
                            Toast.makeText(context.getApplicationContext(), "on Lost",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                            super.onBlockedStatusChanged(network, blocked);
                            Log.d(TAG, "onBlockedStatusChanged");
                            Toast.makeText(context.getApplicationContext(), "onBlockedStatusChanged",
                                    Toast.LENGTH_SHORT).show();

                        }

                        @RequiresApi(api = 33)
                        @Override
                        public void onCapabilitiesChanged(@NonNull Network network, @NonNull
                        NetworkCapabilities networkCapabilities) {
                            super.onCapabilitiesChanged(network, networkCapabilities);
                            Log.d(TAG, "onCapabilitiesChanged");
                            Toast.makeText(context.getApplicationContext(), "onCapability Changed",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, " capabilities for the default network is " +
                                    networkCapabilities);
                            int[] enterpriseID = networkCapabilities.getEnterpriseIds();
                            Log.d(TAG, " Enterprise IDs: " + enterpriseID);
                            Log.d(TAG, " does it have validated network connection internet presence : "
                                    + networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                    + " is it validated "
                                    + networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                            if (networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)) {

                                if (networkCapabilities.hasCapability(
                                        NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                        && !GlobalVars.isNetworkConnected) {
                                    GlobalVars.isNetworkConnected = true;
                                } else if (!networkCapabilities.hasCapability(
                                        NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                        && GlobalVars.isNetworkConnected) {
                                    // handles the scenario when the internet is blocked by ISP,
                                    // or when the dsl/fiber/cable line to the router is disconnected
                                    GlobalVars.isNetworkConnected = false;
                                    Log.d(TAG,
                                            " Internet Connection is lost temporarily for network: " +
                                                    network);
                                }
                            }
                        }

                        @Override
                        public void onLinkPropertiesChanged(@NonNull Network network,
                                                            @NonNull LinkProperties linkProperties) {
                            super.onLinkPropertiesChanged(network, linkProperties);
                            Log.d(TAG, "onLinkPropertiesChanged: " + network);
                            Toast.makeText(context.getApplicationContext(), "onLinkProperties Changed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLosing(@NonNull Network network, int maxMsToLive) {
                            super.onLosing(network, maxMsToLive);
                            Log.d(TAG, "onLosing" + network);
                            Toast.makeText(context.getApplicationContext(), "on Losing",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUnavailable() {
                            super.onUnavailable();
                            Log.d(TAG, "onUnavailable");
                            Toast.makeText(context.getApplicationContext(), "on Unavailable",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });

            flag = true;
        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            Toast.makeText(context.getApplicationContext(), "registerNetworkCallback Exception",
                    Toast.LENGTH_SHORT).show();
            GlobalVars.isNetworkConnected = false;
            flag = false;
        }
        return flag;
    }


    public void registerNetworkCallback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                if (builder != null) {
                    Log.d(TAG, "Network Request: " +
                            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE));
                }
                connectivityManager.registerNetworkCallback(builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull android.net.Network network) {
                            super.onAvailable(network);
                            GlobalVars.isNetworkConnected = true;
                            Log.d(TAG, "onAvailable");
                            Toast.makeText(context.getApplicationContext(), "Available",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLost(@NonNull android.net.Network network) {
                            super.onLost(network);
                            GlobalVars.isNetworkConnected = false;
                            Log.d(TAG, "onLost");
                            Toast.makeText(context.getApplicationContext(), "on Lost",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                            super.onBlockedStatusChanged(network, blocked);
                            Log.d(TAG, "onBlockedStatusChanged");
                            Toast.makeText(context.getApplicationContext(), "onBlockedStatusChanged",
                                    Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCapabilitiesChanged(@NonNull Network network, @NonNull
                        NetworkCapabilities networkCapabilities) {
                            super.onCapabilitiesChanged(network, networkCapabilities);
                            Log.d(TAG, "onCapabilitiesChanged");
                            Toast.makeText(context.getApplicationContext(), "onCapability Changed",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, " capabilities for the default network is " +
                                    networkCapabilities);
                            Log.d(TAG, " does it have validated network connection internet presence : "
                                    + networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                    + " is it validated "
                                    + networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                            if (networkCapabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)) {

                                if (networkCapabilities.hasCapability(
                                        NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                        && !GlobalVars.isNetworkConnected) {
                                    GlobalVars.isNetworkConnected = true;
                                } else if (!networkCapabilities.hasCapability(
                                        NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                        && GlobalVars.isNetworkConnected) {
                                    // handles the scenario when the internet is blocked by ISP,
                                    // or when the dsl/fiber/cable line to the router is disconnected
                                    GlobalVars.isNetworkConnected = false;
                                    Log.d(TAG,
                                            " Internet Connection is lost temporarily for network: " +
                                                    network);
                                }
                            }
                        }

                        @Override
                        public void onLinkPropertiesChanged(@NonNull Network network,
                                                            @NonNull LinkProperties linkProperties) {
                            super.onLinkPropertiesChanged(network, linkProperties);
                            Log.d(TAG, "onLinkPropertiesChanged: " + network);
                            Toast.makeText(context.getApplicationContext(), "onLinkProperties Changed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLosing(@NonNull Network network, int maxMsToLive) {
                            super.onLosing(network, maxMsToLive);
                            Log.d(TAG, "onLosing" + network);
                            Toast.makeText(context.getApplicationContext(), "on Losing",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUnavailable() {
                            super.onUnavailable();
                            Log.d(TAG, "onUnavailable");
                            Toast.makeText(context.getApplicationContext(), "on Unavailable",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
            }

            NetworkRequest.Builder builder1 = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_CBS);
            if (builder1 != null) {
                Log.d(TAG, "Network Request: " +
                        builder1.addCapability(NetworkCapabilities.NET_CAPABILITY_CBS));
            }

            Network network = connectivityManager.getActiveNetwork();
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            assert networkCapabilities != null;
            boolean validated_capability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            boolean internet_capability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                boolean enterprise_capability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                Log.d(TAG, "Enterprise Capabilities:" + enterprise_capability);
            }
            assert linkProperties != null;
            String interfaceName = linkProperties.getInterfaceName();

            Log.d(TAG, "Validated Capabilities:" + validated_capability);
            Log.d(TAG, "Internet Capabilities:" + internet_capability);
            Log.d(TAG, "Interface Name: " + interfaceName);
            Log.d(TAG, "LINK PROPERTIES: " + linkProperties);
            Log.d(TAG, "DNS LIST: " + linkProperties.getDnsServers());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(TAG, "DHCP SERVER ADDRESS: " + linkProperties.getDhcpServerAddress());
            }
        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            Toast.makeText(context.getApplicationContext(), "registerNetworkCallback Exception",
                    Toast.LENGTH_SHORT).show();
            GlobalVars.isNetworkConnected = false;
        }
    }
}