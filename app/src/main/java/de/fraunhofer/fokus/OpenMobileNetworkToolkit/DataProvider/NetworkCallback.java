/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
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
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.HomeFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;

public class NetworkCallback {
    private static final String TAG = "NETWORK_CALLBACK";
    public HomeFragment homeFragment = new HomeFragment();
    private final Context context;

    private final ConnectivityManager connectivityManager;

    public NetworkCallback(Context context) {
        this.context = context;
        connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean getEnterpriseCapability(Context context) {
        boolean enterprise = false;
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                enterprise = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
            }
        }
        return enterprise;
    }

    //TODO COMPLETE THIS FOR NETWORK REGISTRATION INFO
    public boolean getNetworkRegistrationInfo(Context context) {
        Boolean flag = false;
        List<NetworkRegistrationInfo> networkRegistrationInfo = new ArrayList<>();
        List<String> networkRegistrationInfoList = new ArrayList<>();
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        PackageManager pm = context.getPackageManager();
        TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission")
        ServiceState serviceState = tm.getServiceState(); //todo add permission check
        Log.d(TAG, "Service State: " + serviceState.getState());
        if (serviceState.getState() == 1) {  //2 for DATA_CONNECTED 1 FOR DATA_CONNECTING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                networkRegistrationInfo = serviceState.getNetworkRegistrationInfoList();
                if (networkRegistrationInfo != null) {
                    for (int i = 0; i < networkRegistrationInfoList.size(); i++) {
                        Log.d(TAG, "Network Registration Info " + networkRegistrationInfo);
                        String netwrokRegistrationInfoString = networkRegistrationInfo.toString();
                        networkRegistrationInfoList.add(netwrokRegistrationInfoString);
                    }
                }
                flag = true;
            }
        } else {
            Log.d(TAG, "Network Registration Info Unavailable! Check Network State");
            flag = false;
        }

        return flag;
    }

    public List<String> getFeatureList(Context context) {
        FeatureInfo[] feature = null;
        FeatureInfo featureInfo = null;
        List<String> featureString = new ArrayList<>();

        PackageManager pm = context.getPackageManager();
        MainActivity ma = new MainActivity();
        TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (context != null) {
            Context context1 = context;
        }

        feature = pm.getSystemAvailableFeatures();

        if (feature != null) {
            for (int i = 0; i < feature.length; i++) {
                featureInfo = feature[i];
                Log.d(TAG, "Feature: " + feature.toString());
                featureString.add(featureInfo.toString());
            }

        } else {
            Log.d(TAG, "features not exist");
        }
        return featureString;
    }

    @RequiresPermission(value = "android.permission.READ_PRIVILEGED_PHONE_STATE")
    public boolean getConfigurationTM(Context context) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PackageManager pm = context.getPackageManager();

            boolean telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            boolean capability_slicing =
                pm.hasSystemFeature(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED);

           /*List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
           List<UrspRule> urspRuleList = new ArrayList<>();*/

            MainActivity ma = new MainActivity();
            Log.d(TAG, "CAPABILITY SLICING: " + capability_slicing);


            /*Log.d(TAG, "Has carrier privilleges:" +tm.hasCarrierPrivileges());
           Log.d(TAG, "PACKAGE MANAGER" +pm.hasSystemFeature(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED));
            Log.d(TAG, "Capability: "+tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED));*/


            try {
                TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (context != null) {
                    Context context1 = context;
                    ConnectivityManager connectivityManager =
                        (ConnectivityManager) context1.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    Network network = connectivityManager.getActiveNetwork();

                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(
                        TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        //if (tm.hasCarrierPrivileges()) {/* TODO Change to true for working */

                        tm.getNetworkSlicingConfiguration(context1.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    NetworkSlicingConfig networkSlicingConfig = result;
                                    List<UrspRule> urspRuleList =
                                        networkSlicingConfig.getUrspRules();

                                    Log.d(TAG, "Slice config works!!");
                                    Log.d(TAG, "URSP List: " + urspRuleList);
                                    Log.d(TAG, "URSP received: " + urspRuleList.size());

                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule =
                                            networkSlicingConfig.getUrspRules().get(i);
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

    public boolean getRouteSelectionDescriptor(Context context) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
                List<UrspRule> urspRuleList = new ArrayList<>();
                List<RouteSelectionDescriptor> routeSelectionDescriptorsList = new ArrayList<>();
                List<TrafficDescriptor> trafficDescriptorList = new ArrayList<>();
                TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (context != null) {
                    Context context1 = context;
                    ConnectivityManager connectivityManager =
                        (ConnectivityManager) context1.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    Network network = connectivityManager.getActiveNetwork();

                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(
                        TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        //if (tm.hasCarrierPrivileges()) {/* TODO Change to true for working */

                        tm.getNetworkSlicingConfiguration(context1.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    NetworkSlicingConfig networkSlicingConfig = result;
                                    List<UrspRule> urspRuleList =
                                        networkSlicingConfig.getUrspRules();

                                    Log.d(TAG, "Slice Info config function works!!");
                                    // Log.d(TAG, "URSP List: " +urspRuleList);
                                    // Log.d(TAG,"URSP received: " +urspRuleList.size());

                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule =
                                            networkSlicingConfig.getUrspRules().get(i);
                                        List<TrafficDescriptor> trafficDescriptorList =
                                            urspRule.getTrafficDescriptors();
                                        List<RouteSelectionDescriptor>
                                            routeSelectionDescriptorsList =
                                            urspRule.getRouteSelectionDescriptor();
                                        //Log.d(TAG, "URSP" + urspRule);
                                        //Log.d(TAG, "Traffic Descriptor" + trafficDescriptor);
                                        if (routeSelectionDescriptorsList != null) {
                                            for (int j = 0;
                                                 i < routeSelectionDescriptorsList.size(); i++) {
                                                RouteSelectionDescriptor routeSelectionDescriptor =
                                                    routeSelectionDescriptorsList.get(i);
                                                //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);

                                                if (routeSelectionDescriptor != null) {
                                                    Log.d(TAG,
                                                        "Route Selection Descriptor Available");
                                                    List<String> dataNetworkNameList =
                                                        routeSelectionDescriptor.getDataNetworkName();

                                                    if (dataNetworkNameList != null) {
                                                        for (int k = 0;
                                                             k < dataNetworkNameList.size(); k++) {
                                                            Log.d(TAG, "Data Network Name DNN: " +
                                                                dataNetworkNameList.get(i));
                                                        }
                                                    } else {
                                                        Log.d(TAG, "DNN List: " +
                                                            dataNetworkNameList.size());
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
    public boolean getTrafficDescriptor(Context context) {

        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
                List<UrspRule> urspRuleList = new ArrayList<>();
                List<RouteSelectionDescriptor> routeSelectionDescriptorsList = new ArrayList<>();
                List<TrafficDescriptor> trafficDescriptorList = new ArrayList<>();
                TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (context != null) {
                    Context context1 = context;
                    ConnectivityManager connectivityManager =
                        (ConnectivityManager) context1.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    Network network = connectivityManager.getActiveNetwork();

                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(
                        TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        //if (tm.hasCarrierPrivileges()) {/* TODO Change to true for working */

                        tm.getNetworkSlicingConfiguration(context1.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    NetworkSlicingConfig networkSlicingConfig = result;
                                    List<UrspRule> urspRuleList =
                                        networkSlicingConfig.getUrspRules();
                                    Log.d(TAG, "Traffic Descriptor function works!!");

                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule =
                                            networkSlicingConfig.getUrspRules().get(i);
                                        List<TrafficDescriptor> trafficDescriptorList =
                                            urspRule.getTrafficDescriptors();
                                        List<RouteSelectionDescriptor>
                                            routeSelectionDescriptorsList =
                                            urspRule.getRouteSelectionDescriptor();
                                        if (trafficDescriptorList != null) {
                                            for (int j = 0; i < trafficDescriptorList.size(); i++) {
                                                TrafficDescriptor trafficDescriptor =
                                                    urspRule.getTrafficDescriptors().get(i);
                                                //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);

                                                if (trafficDescriptor != null) {
                                                    Log.d(TAG, "Traffic Descriptor Available");
                                                    Log.d(TAG, "Traffic Descriptor DNN: " +
                                                        trafficDescriptor.getDataNetworkName());
                                                    Log.d(TAG, "Traffic Descriptor Os App ID: " +
                                                        trafficDescriptor.getOsAppId());

                                                }
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

    public Boolean getIMS(Context context) {
        boolean imsCapability = false;
        assert context != null;
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                imsCapability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_IMS);
            }
        }
        return imsCapability;
    }

    public List<String> getDefaultDNS(Context context) {
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

    public Network getCurrentNetwork(Context context) {
        return connectivityManager.getActiveNetwork();
    }

    public String getInterfaceName(Context context) {
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

    public Boolean getNetworkCapabilities(Context context) {
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

    public String getPLMN(Context context) {
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

            TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                @SuppressLint("MissingPermission")
                ServiceState serviceState =
                    tm.getServiceState(); // todo handle this according to the privileges granted the app
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

    public String getRegisterPLMNfromNetworkRegistrationInfo(Context context) {
        String regPLMN = null;
        List<NetworkRegistrationInfo> networkRegistrationInfo = new ArrayList<>();
        List<String> networkRegistrationInfoList = new ArrayList<>();
        Network network = connectivityManager.getActiveNetwork();
        TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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

    public String getNetworkCapabilitylist(Context context) {

        // Create a StringBuilder to build the display string
        StringBuilder networkCapabilitiesBuilder = new StringBuilder();

        // Create a String that is passed on from string builder.
        String networkAllCapabilities = "";

        //List of Ints for Capabilities
        List<String> listCapability = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int[] capability;
            Boolean enterprise = false;

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
                            networkCapabilitiesBuilder.append(
                                "\nUnknown Capability Received! Check Logs for Capability value\n");
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
                    if (capability != null) {
                        for (int i = 0; i < capability.length; i++) {
                            Log.d(TAG, "Capability from Network: " + capability[i]);

                        }
                    }
                }
                networkAllCapabilities = networkCapabilitiesBuilder.toString();
            }
        }
        return networkAllCapabilities;
    }

    public boolean getNetworkSlicingInfo(Context context) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
                List<UrspRule> urspRuleList = new ArrayList<>();
                List<RouteSelectionDescriptor> routeSelectionDescriptorsList = new ArrayList<>();
                List<TrafficDescriptor> trafficDescriptorList = new ArrayList<>();
                TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (context != null) {
                    Context context1 = context;
                    ConnectivityManager connectivityManager =
                        (ConnectivityManager) context1.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    Network network = connectivityManager.getActiveNetwork();

                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(
                        TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        //if (tm.hasCarrierPrivileges()) {/* TODO Change to true for working */

                        tm.getNetworkSlicingConfiguration(context1.getMainExecutor(),
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
                                        List<TrafficDescriptor> trafficDescriptorList =
                                            urspRule.getTrafficDescriptors();
                                        List<RouteSelectionDescriptor>
                                            routeSelectionDescriptorsList =
                                            urspRule.getRouteSelectionDescriptor();

                                        if (routeSelectionDescriptorsList != null) {
                                            for (int j = 0;
                                                 i < routeSelectionDescriptorsList.size(); i++) {
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

    public boolean getNetworkSlicingConfig(Context context) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (context != null) {
                    Context context1 = context;
                    ConnectivityManager connectivityManager =
                        (ConnectivityManager) context1.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    Network network = connectivityManager.getActiveNetwork();
                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(
                        TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        //if (tm.hasCarrierPrivileges()) {/* TODO Change to true for working */

                        tm.getNetworkSlicingConfiguration(context1.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    NetworkSlicingConfig networkSlicingConfig = result;
                                    List<UrspRule> urspRuleList =
                                        networkSlicingConfig.getUrspRules();

                                    for (int i = 0; i < urspRuleList.size(); i++) {
                                        UrspRule urspRule =
                                            networkSlicingConfig.getUrspRules().get(i);
                                        List<RouteSelectionDescriptor>
                                            routeSelectionDescriptorsList =
                                            urspRule.getRouteSelectionDescriptor();

                                        if (routeSelectionDescriptorsList != null) {
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

                                                    if (dataNetworkNameList != null) {
                                                        for (int k = 0;
                                                             k < dataNetworkNameList.size(); k++) {
                                                            Log.d(TAG, "Data Network Name DNN: " +
                                                                dataNetworkNameList.get(i));
                                                        }
                                                    } else {
                                                        Log.d(TAG, "DNN List: " +
                                                            dataNetworkNameList.size());
                                                    }

                                                    if (networkSliceInfoList != null) {
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
                                                    } else {
                                                        Log.d(TAG, "NetworkSliceInfo List: " +
                                                            networkSliceInfoList.size());
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

    public void getURSPrules(Context context) {
        List<RouteSelectionDescriptor> routeSelectionDescriptor = new ArrayList<>();
        List<TrafficDescriptor> trafficDescriptor = new ArrayList<>();

        if (context != null) {
            Context context1 = context;
        }
        Network network = connectivityManager.getActiveNetwork();

        /* TODO: Get URSP rules from network */
        UrspRule urspRule = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (urspRule != null) {
                /* TODO: Get routeSelection and trafficDescriptor from URSP rules */
                routeSelectionDescriptor =
                    urspRule.getRouteSelectionDescriptor();
                trafficDescriptor =
                    urspRule.getTrafficDescriptors();
            }
        }
    }

    public Boolean getValidity(Context context) {

        Boolean validated = false;
        if (context != null) {
            Context context1 = context;
        }
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

    @RequiresApi(api = 33)
    public int getEnterpriseIds(Context context) {

        int[] enterpriseId = null;
        int enterpriseIDint = 1234;
        if (context != null) {
            Context context1 = context;
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                Log.d(TAG, "Enterprise IDs: " + networkCapabilities.getEnterpriseIds());
                enterpriseId = networkCapabilities.getEnterpriseIds();
            }
            for (int k = 0; k < enterpriseId.length; k++) {
                enterpriseIDint = enterpriseId[k];
                Log.d(TAG, "Enterprise ID: " + enterpriseIDint);
            }
        }


        return enterpriseIDint;
    }

    public Boolean getInternet(Context context) {
        boolean internetCapability = false;
        if (context != null) {
            Context context1 = context;
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                internetCapability =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return internetCapability;
    }

    public boolean getEnterpriseIdList(Context context) {
        boolean flag = false;
        List<Integer> enterpriseIDList = new ArrayList<>();
        int[] enterpriseID = null;

        try {
            if (context != null) {
                Context context1 = context;
            }
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);

            //enterpriseID = networkCapabilities.getEnterpriseIds();

            if (enterpriseID != null) {
                for (int i = 0; i < enterpriseID.length; i++) {
                    int entID;
                    entID = enterpriseID[i];
                    Log.d(TAG, "Enterprise ID: " + entID);
                    enterpriseIDList.add(entID);
                    flag = true;
                }
            } else {
                Log.d(TAG, "Enterprise ID not Found!");
                flag = false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Enterprise Function not found!! ");
            flag = false;
        }
        return flag;
    }

    @SuppressLint("MissingPermission")
    public NetworkSlicingConfig getConfig(Context context) {
        NetworkSlicingConfig networksliceConfig = null;
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
                List<UrspRule> urspRuleList = new ArrayList<>();
                List<RouteSelectionDescriptor> routeSelectionDescriptorsList = new ArrayList<>();
                List<TrafficDescriptor> trafficDescriptorList = new ArrayList<>();
                TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (context != null) {
                    Context context1 = context;
                    ConnectivityManager connectivityManager =
                        (ConnectivityManager) context1.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    Network network = connectivityManager.getActiveNetwork();

                    /* TODO Find a way to get READ_PRIVILEGED_PHONE_STATE for getNetworkSlicingConfiguration */
                    if (tm.isRadioInterfaceCapabilitySupported(
                        TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED)) {
                        //if (tm.hasCarrierPrivileges()) {/* TODO Change to true for working */

                        tm.getNetworkSlicingConfiguration(context1.getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(@NonNull NetworkSlicingConfig result) {
                                    NetworkSlicingConfig networkSlicingConfig = result;
                                    Log.d(TAG, "function works!!");
                                    //flag = true;
                                }

                                @Override
                                public void onError(
                                    @NonNull TelephonyManager.NetworkSlicingException error) {
                                    OutcomeReceiver.super.onError(error);
                                    Log.d(TAG, "Traffi Descriptor Failed!");
                                    //flag = false;
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
    public String getDataNetworkNameTrafficDescriptor(Context context) {

        String dataNetworkName = null;
        if (context != null) {
            Context context1 = context;
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            Log.d(TAG, "Network exists here!!!");
        }
        return dataNetworkName;
    }

    public void setHasCarrierPrivilages(boolean privilages) {
        Boolean HasCarrierPrivilages = privilages;
    }

    //Check Network
    public void requestNetworkCallback() {
        try {
            /*ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
             * */

            ConnectivityManager connectivityManager = homeFragment.connectivityManager;
            connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);

            if (connectivityManager.isDefaultNetworkActive()) {
                Toast.makeText(context.getApplicationContext(), "Network:" + network,
                    Toast.LENGTH_SHORT).show();

            }
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_CBS);

            assert connectivityManager != null;

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
            //ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            ConnectivityManager connectivityManager = homeFragment.connectivityManager;

            Network network = connectivityManager.getActiveNetwork();
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

            //NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            assert connectivityManager != null;
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
            ConnectivityManager connectivityManager = homeFragment.connectivityManager;
            connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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

            //Toast.makeText(context.getApplicationContext(), "INET4Address: "+ inet4Address,Toast.LENGTH_SHORT).show();

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

            assert connectivityManager != null;


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


    @RequiresApi(api = Build.VERSION_CODES.S)
    public void registerNetworkCallback() {
        try {
            /* TODO Network Request used for requesting network with Enterprise Capability */
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);

            NetworkRequest.Builder builder1 = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_CBS);

            if (builder != null) {
                Log.d(TAG, "Network Request: " +
                    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE));
            }

            if (builder1 != null) {
                Log.d(TAG, "Network Request: " +
                    builder1.addCapability(NetworkCapabilities.NET_CAPABILITY_CBS));
            }

            Network network = connectivityManager.getActiveNetwork();
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);
            assert networkCapabilities != null;
            boolean validated_capability =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            boolean internet_capability =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean enterprise_capability =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);

            assert linkProperties != null;
            String interfaceName = linkProperties.getInterfaceName();


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

            assert connectivityManager != null;


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
                        //Log.d(TAG, " Enterprise IDs: " + networkCapabilities.getEnterpriseIds().toString());
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

        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            Toast.makeText(context.getApplicationContext(), "registerNetworkCallback Exception",
                Toast.LENGTH_SHORT).show();
            GlobalVars.isNetworkConnected = false;
        }
    }
}