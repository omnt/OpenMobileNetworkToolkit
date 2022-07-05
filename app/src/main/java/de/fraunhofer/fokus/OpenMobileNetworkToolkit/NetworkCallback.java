package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.slice.Slice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.params.Capability;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.OutcomeReceiver;
import android.os.Parcelable;
import android.provider.Telephony;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.telephony.ims.RegistrationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.PopUpToBuilder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NetworkCallback {
    private static final String TAG = "FLABS";
    public SliceFragment sliceFragment = new SliceFragment();
    private Context context;
    private boolean HasCarrierPrivilages;


    public NetworkCallback(Context context) {
        this.context = context;

    }

    public static List<String> getDefaultDNS(Context context) {
        List<String> listDns = new ArrayList<>();

        if (context != null) {
            Context context1 = context;

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
                List<InetAddress> dns = linkProperties.getDnsServers();
                if (dns != null)
                    for (InetAddress d : linkProperties.getDnsServers()) {
                        SRLog.d(TAG, "DNS from LinkProperties: " + d.getHostAddress());
                        listDns.add(d.getHostAddress().split("%")[0]);
                    }
            }
        } else {
            Log.d(TAG, "Context not found!");
        }

        return listDns;
    }

    public static Network getCurrentNetwork(Context context) {

        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        return network;
    }

    public static String getInterfaceName(Context context) {

        String interfaceName;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            interfaceName = linkProperties.getInterfaceName();
        } else {
            interfaceName = "null";
        }

        return interfaceName;
    }

    public static Boolean getNetworkCapabilities(Context context) {

        Boolean enterprise = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            SRLog.d(TAG,"Network Capabilities: " +networkCapabilities);
            if (networkCapabilities != null) {
                enterprise = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                if(enterprise = true) {
                    SRLog.d(TAG,"Enterprise Capabilities available for Network!");
                }
            }
        }
        return enterprise;
    }

    public static String getPLMN(Context context){
        String plmn = null;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        if (network != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            int networktype = networkInfo.getType();
            int networksubtype = networkInfo.getSubtype();
            String typename = networkInfo.getTypeName();
            String subtypename = networkInfo.getSubtypeName();


            NetworkRegistrationInfo networkRegistrationInfo = null;
            if (networkRegistrationInfo != null) {
               plmn = networkRegistrationInfo.getRegisteredPlmn();
                SRLog.d(TAG, "PLMN: " + plmn);
            } else {
                SRLog.d(TAG, "PLMN unavailable");
            }
        }

        return plmn;
    }

    public static void getAPNs(Context context){
        String apnName;
        int apnTypeBitmask;
        int authType;
        int carrierID;
        String entryName;
        int apnDatabaseID;


        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        ApnSetting apnSetting = null;

    }

    public static int getSubsID(Context context){
        int subsID = 0;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        SubscriptionInfo subscriptionInfo = (SubscriptionInfo) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        subsID = subscriptionInfo.getSubscriptionId();
        SRLog.d(TAG,"Subscription ID: " +subsID);

        return subsID;
    }

    public static void getNetworkInfo(Context context){
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        if (network != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            SRLog.d(TAG, "Network INFO RECEIVED ");
            int networktype = networkInfo.getType();
            SRLog.d(TAG, "Network Type: " + networktype);
            int networksubtype = networkInfo.getSubtype();
            SRLog.d(TAG, "Network Sub Type: " + networksubtype);
            String typename = networkInfo.getTypeName();
            SRLog.d(TAG, "Network Type Name: " + typename);
            String subtypename = networkInfo.getSubtypeName();
            SRLog.d(TAG, "Network Sub Type Name: " + subtypename);
             }
        }

    public static List<String> getAvailableServices(Context context) {
        List<String> availableServices = new ArrayList<>();
        List<Integer> availServices = new ArrayList<>();

        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkRegistrationInfo networkRegistrationInfo = null;

            if (networkRegistrationInfo != null) {
                availServices = networkRegistrationInfo.getAvailableServices();
                if(availServices != null) {
                    for (int i = 0; i < availServices.size(); i++) {
                        SRLog.d(TAG, "Available Services from Network: " + availServices);
                        String avService = availServices.toString();
                        availableServices.add(avService);
                    }
                }
            } else {
                SRLog.d(TAG, "No service Available for Registration Info" + availServices);
            }
        }
        return  availableServices;
    }

    public static boolean getEnterpriseCapability(Context context) {
        Boolean enterprise = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                enterprise = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
            }
        }
        return enterprise;
    }

    //TODO COMPLETE THIS FOR RADIO INTERFACE
    public static boolean getNetworkRegistrationInfo(Context context){
        Boolean flag = false;
        List<NetworkRegistrationInfo> networkRegistrationInfo = new ArrayList<>();
        List<String> networkRegistrationInfoList = new ArrayList<>();
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        PackageManager pm = context.getPackageManager();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        ServiceState serviceState = tm.getServiceState();
        SRLog.d(TAG,"Service State: " + serviceState.getState());
        if(serviceState.getState() == 2) {
            networkRegistrationInfo = serviceState.getNetworkRegistrationInfoList();
            if(networkRegistrationInfo != null){
                for (int i = 0; i < networkRegistrationInfoList.size(); i++) {
                    SRLog.d(TAG, "Network Registration Info " + networkRegistrationInfo);
                    String netwrokRegistrationInfoString = networkRegistrationInfo.toString();
                    networkRegistrationInfoList.add(netwrokRegistrationInfoString);
                }
            }
        } else {
            SRLog.d(TAG, "Network Registration Info Unavailable! Check Network State");
        }

        return flag;
    }
    /*public static List<String> getNetworkCapabilitylist(Context context) {
        List<String> listCapability = new ArrayList<>();
        int capability[] = null;
        Boolean enterprise = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                capability = networkCapabilities.getCapabilities();
                if (capability != null)
                    for (int i= 0; i < capability.length; i++) {
                        SRLog.d(TAG, "Capability from Network: " + capability[i]);
                        String cap = capability.toString();
                        listCapability.add(cap);
                    }
                //enterprise = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
            }
        }
        return listCapability;
    }*/

    public static List<String> getNetworkCapabilitylist(Context context) {
        List<String> listCapability = new ArrayList<>();
        int capability[] = null;
        Boolean enterprise = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                capability = networkCapabilities.getCapabilities();
                if (capability != null)
                    for (int i= 0; i < capability.length; i++) {
                        SRLog.d(TAG, "Capability from Network: " + capability[i]);
                        String cap = capability.toString();
                        listCapability.add(cap);
                    }
                //enterprise = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
            }
        }
        return listCapability;
    }

    public static boolean getNetworkSlicingInfo(Context context){
        List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
        List<UrspRule> urspRuleList = new ArrayList<>();
        boolean flag = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        /* TODO Get Network Slicing config from network */


            NetworkSliceInfo networkSliceInfo = new NetworkSliceInfo.Builder()
                    .build();


        if(networkSliceInfo != null) {
            SRLog.d(TAG, "SLICE DIFFERENTIATOR: " + networkSliceInfo.getSliceDifferentiator());
            SRLog.d(TAG, "MAPPED PLMN DIFFERENTIATOR: " + networkSliceInfo.getMappedHplmnSliceDifferentiator());
            SRLog.d(TAG, "SLICE SERVICE TYPE: " + networkSliceInfo.getSliceServiceType());
            SRLog.d(TAG, "MAPPED PLMN SERVICE TYPE: " + networkSliceInfo.getMappedHplmnSliceServiceType());
            SRLog.d(TAG, "STATUS: " + networkSliceInfo.getStatus());

            NetworkSlicingConfig networkSlicingConfig = null;
            if (networkSlicingConfig != null) {
                /* TODO: Get Slice Info lists, urspList from Network */
                sliceInfoList = networkSlicingConfig.getSliceInfo();
                urspRuleList = networkSlicingConfig.getUrspRules();
            }

            flag = true;
        } else
        {
           flag = false;
        }

        return false;
    }

   /* public NetworkSlicingConfig getNetworkSlicingConfigurationFor5G(Context context){
        NetworkSlicingConfig slicingConfig = null;
        if (context != null) {
            Context context1 = context;
        }
        PackageManager pm = context.getPackageManager();
        boolean telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm != null){
            if(telephony == true) {
                tm.getNetworkSlicingConfiguration(context.getMainExecutor(), new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                    @Override
                    public void onResult(@NonNull NetworkSlicingConfig result) {
                        SRLog.d(TAG,"Network Slicing Configuration Received");
                        slicingConfig = result;
                    }
                });
            }
        }
        return slicingConfig;
    }*/
   @RequiresPermission(value = "android.permission.READ_PRIVILEGED_PHONE_STATE")
   public static boolean getConfigurationTM(Context context) {
        PackageManager pm = context.getPackageManager();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        MainActivity ma = new MainActivity();
        boolean flag = false;
        if (context != null) {
            Context context1 = context;
        ConnectivityManager connectivityManager = (ConnectivityManager) context1.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED) == false) { /* TODO Change to true for working */
            tm.getNetworkSlicingConfiguration(context.getMainExecutor(), new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {

                @Override
                public void onResult(@NonNull NetworkSlicingConfig result) {
                    SRLog.d(TAG, "Slice config works!!");
                }

                @Override
                public void onError(@NonNull TelephonyManager.NetworkSlicingException error) {
                    OutcomeReceiver.super.onError(error);
                    SRLog.d(TAG, "Slice Config rejected!");
                }

            });

        }
            flag = true;
    }else {
            flag = false;
        }
        SRLog.d(TAG,"TM Config:" +flag);
        return flag;
    }

    public static boolean getNetworkSlicingConfig(Context context){
        List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
        List<UrspRule> urspRuleList = new ArrayList<>();
        boolean flag = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        /* TODO Get Network Slicing config from network */


        NetworkSliceInfo networkSliceInfo = new NetworkSliceInfo.Builder()
                .build();


        if(networkSliceInfo != null) {

            NetworkSlicingConfig networkSlicingConfig = new NetworkSlicingConfig();
            if (networkSlicingConfig != null) {
                /* TODO: Get Slice Info lists, urspList from Network */
                sliceInfoList = networkSlicingConfig.getSliceInfo();
                SRLog.d(TAG, "Slice Info List: " + sliceInfoList);
                urspRuleList = networkSlicingConfig.getUrspRules();
                SRLog.d(TAG, "URSP RULES: " + urspRuleList);
            }

            flag = true;
        } else
        {
            flag = false;
        }
        return false;
    }


    public static boolean getRouteSelectionDescriptor(Context context){

        List<String> dataNetworkName = new ArrayList<>();
        List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
        boolean flag = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        /* TODO Get Route Selection Descriptor from network */
        RouteSelectionDescriptor routeSelectionDescriptor = null;
        if(routeSelectionDescriptor != null){

            /* TODO: Get DNN, Slice Info lists, session type, ssc mode */

                dataNetworkName = routeSelectionDescriptor.getDataNetworkName();
                SRLog.d(TAG, "Data Network Name(Route Descriptor): " + dataNetworkName);
                sliceInfoList = routeSelectionDescriptor.getSliceInfo();
                SRLog.d(TAG, "Slice Info(Route Descriptor): " + sliceInfoList);
                int sessionType = routeSelectionDescriptor.getSessionType();
                SRLog.d(TAG, "Session Type(Route Descriptor):" + sessionType);
                int sscMode = routeSelectionDescriptor.getSscMode();
                SRLog.d(TAG, "SSC MODE(Route Descriptor): " + sscMode);

            flag = true;
        }
        else {
            flag = false;
        }
        return flag;
    }

    public static boolean getTrafficDescriptor(Context context) {
        String dataNetworkName = null;
        byte[] osAppId = null;
        boolean flag = false;


        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        /* TODO: Get TrafficDescriptor from network */
        TrafficDescriptor trafficDescriptor = null;
        if(trafficDescriptor != null) {
            /* TODO: Get DNN and osAppID from TrafficDescriptor */
            dataNetworkName = trafficDescriptor.getDataNetworkName();
            SRLog.d(TAG,"Data Network Name(Traffic Descriptor): " + dataNetworkName);
            osAppId = trafficDescriptor.getOsAppId();
            SRLog.d(TAG,"OS ID(Traffic Descriptor): " +osAppId);
            flag = true;
        } else {
            flag = false;
        }

        return flag;
    }

    public static void getURSPrules(Context context){
        List<RouteSelectionDescriptor> routeSelectionDescriptor = new ArrayList<>();
        List<TrafficDescriptor> trafficDescriptor = new ArrayList<>();

        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        /* TODO: Get URSP rules from network */
        UrspRule urspRule = null;
        if( urspRule != null) {
            /* TODO: Get routeSelection and trafficDescriptor from URSP rules */
            routeSelectionDescriptor =
                    urspRule.getRouteSelectionDescriptor();
            trafficDescriptor =
                    urspRule.getTrafficDescriptors();
        }
    }

    public static Boolean getValidity(Context context) {

        Boolean validated = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if(network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if(networkCapabilities != null) {
                validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }
        return validated;
    }

    public static Boolean getInternet(Context context) {

        Boolean internetCapability = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if(network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if(networkCapabilities != null) {
                internetCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return internetCapability;
    }

    public static Boolean getIMS(Context context) {

        Boolean imsCapability = false;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if(network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if(networkCapabilities != null) {
                imsCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_IMS);
            }
        }
        return imsCapability;
    }

    //TrafficDescriptor
    public String getDataNetworkNameTrafficDescriptor(Context context){

        String dataNetworkName = null;
        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if(network != null) {
            SRLog.d(TAG,"Network exists here!!!");
        }
        return dataNetworkName;
    }

    public void setHasCarrierPrivilages(boolean privilages) {
        HasCarrierPrivilages = privilages;
    }

    //Check Network
    public void requestNetworkCallback() {
        try {
            /*ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
             * */

            ConnectivityManager connectivityManager = sliceFragment.connectivityManager;
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

            if (connectivityManager.isDefaultNetworkActive()) {
                Toast.makeText(context.getApplicationContext(), "Network:" + network, Toast.LENGTH_SHORT).show();

            }
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);

            assert connectivityManager != null;

            connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    GlobalVars.isNetworkConnected = true;
                    SRLog.d(TAG, "onAvailable");
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    SRLog.d(TAG, "onUnavailable");

                }
            });
        } catch (Exception e) {
            SRLog.d("Network Callback: Exception in requestNetworkCallback", "Catch exception RequestCallback");
            GlobalVars.isNetworkConnected = false;
        }
    }

    public void registerDefaultNetworkCallback() {
        //private static final String TAG = "Network Callback";
        try {
            //ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            ConnectivityManager connectivityManager = sliceFragment.connectivityManager;

            Network network = connectivityManager.getActiveNetwork();
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            //NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            assert connectivityManager != null;
            Toast.makeText(context.getApplicationContext(), "Default Network: " + network, Toast.LENGTH_SHORT).show();
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
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
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    Log.d(TAG, "onCapabilitiesChanged");
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
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

    public void registerNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = sliceFragment.connectivityManager;
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                Toast.makeText(context.getApplicationContext(), "Connectivity Manager does not exist: " + connectivityManager, Toast.LENGTH_SHORT).show();
            }

            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            Network network = connectivityManager.getActiveNetwork();
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

            //Toast.makeText(context.getApplicationContext(), "INET4Address: "+ inet4Address,Toast.LENGTH_SHORT).show();

            boolean validated_capability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            boolean internet_capability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean enterprise_capability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);

            String interfaceName = linkProperties.getInterfaceName();
            //Inet4Address inet4Address = linkProperties.getDhcpServerAddress();


            SRLog.d(TAG, "Validated Capabilities:" + validated_capability);
            SRLog.d(TAG, "Internet Capabilities:" + internet_capability);
            SRLog.d(TAG, "Enterprise Capabilities:" + enterprise_capability);
            SRLog.d(TAG, "Interface Name: " + interfaceName);
            //Log.d(TAG, "INET4Address: "+ inet4Address.toString());
            SRLog.d(TAG, "LINK PROPERTIES: " + linkProperties);
            SRLog.d(TAG, "DNS LIST: " + linkProperties.getDnsServers());
            SRLog.d(TAG, "DHCP SERVER ADDRESS: " + linkProperties.getDhcpServerAddress());
            SRLog.d(TAG, "Network Type Name: " + networkInfo.getTypeName().toString());


            assert connectivityManager != null;


            connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull android.net.Network network) {
                    super.onAvailable(network);
                    GlobalVars.isNetworkConnected = true;
                    SRLog.d(TAG, "onAvailable");
                    Toast.makeText(context.getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLost(@NonNull android.net.Network network) {
                    super.onLost(network);
                    GlobalVars.isNetworkConnected = false;
                    SRLog.d(TAG, "onLost");
                    Toast.makeText(context.getApplicationContext(), "on Lost", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                    super.onBlockedStatusChanged(network, blocked);
                    SRLog.d(TAG, "onBlockedStatusChanged");
                    Toast.makeText(context.getApplicationContext(), "onBlockedStatusChanged", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    SRLog.d(TAG, "onCapabilitiesChanged");
                    Toast.makeText(context.getApplicationContext(), "onCapability Changed", Toast.LENGTH_SHORT).show();
                    SRLog.d(TAG, " capabilities for the default network is " + networkCapabilities.toString());
                    SRLog.d(TAG, " does it have validated network connection internet presence : "
                            + networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            + " is it validated "
                            + networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {

                        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                && !GlobalVars.isNetworkConnected) {
                            GlobalVars.isNetworkConnected = true;
                        } else if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                && GlobalVars.isNetworkConnected) {
                            // handles the scenario when the internet is blocked by ISP,
                            // or when the dsl/fiber/cable line to the router is disconnected
                            GlobalVars.isNetworkConnected = false;
                            SRLog.d(TAG, " Internet Connection is lost temporarily for network: " + network.toString());
                        }
                    }
                }

                @Override
                public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    SRLog.d(TAG, "onLinkPropertiesChanged: " + network.toString());
                    Toast.makeText(context.getApplicationContext(), "onLinkProperties Changed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    SRLog.d(TAG, "onLosing" + network.toString());
                    Toast.makeText(context.getApplicationContext(), "on Losing", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    SRLog.d(TAG, "onUnavailable");
                    Toast.makeText(context.getApplicationContext(), "on Unavailable", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {
            SRLog.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            Toast.makeText(context.getApplicationContext(), "registerNetworkCallback Exception", Toast.LENGTH_SHORT).show();
            GlobalVars.isNetworkConnected = false;
        }
    }
}