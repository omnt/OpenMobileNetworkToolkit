package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.slice.Slice;
import android.content.Context;
import android.hardware.camera2.params.Capability;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.telephony.ServiceState;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
            if (networkCapabilities != null) {
                enterprise = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE);
                if(enterprise = true) {
                    SRLog.d(TAG,"Enterprise Capabilities available for Network!");
                }
            }
        }
        return enterprise;
    }

    public static List<String> getEnterprise(Context context) {
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

    public static void getNetworkSlicingConfig(Context context){
        List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
        List<UrspRule> urspRuleList = new ArrayList<>();

        if (context != null) {
            Context context1 = context;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        /* TODO Get Network Slicing config from network */

        NetworkSlicingConfig networkSlicingConfig = null;

        if(networkSlicingConfig != null){
            /* TODO: Get Slice Info lists, urspList from Network */
            sliceInfoList = networkSlicingConfig.getSliceInfo();
            urspRuleList = networkSlicingConfig.getUrspRules();
        }
    }

    public static void getRouteSelectionDescriptor(Context context){

        List<String> dataNetworkName = new ArrayList<>();
        List<NetworkSliceInfo> sliceInfoList = new ArrayList<>();
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
            sliceInfoList = routeSelectionDescriptor.getSliceInfo();
            int sessionType = routeSelectionDescriptor.getSessionType();
            int sscMode = routeSelectionDescriptor.getSscMode();
        }

    }

    public static void getTrafficDescriptor(Context context) {
        String dataNetworkName = null;
        byte[] osAppId = null;

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
            osAppId = trafficDescriptor.getOsAppId();
        }
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

            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
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
                    Log.d(TAG, "onAvailable");
                    Toast.makeText(context.getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLost(@NonNull android.net.Network network) {
                    super.onLost(network);
                    GlobalVars.isNetworkConnected = false;
                    Log.d(TAG, "onLost");
                    Toast.makeText(context.getApplicationContext(), "on Lost", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                    super.onBlockedStatusChanged(network, blocked);
                    Log.d(TAG, "onBlockedStatusChanged");
                    Toast.makeText(context.getApplicationContext(), "onBlockedStatusChanged", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    Log.d(TAG, "onCapabilitiesChanged");
                    Toast.makeText(context.getApplicationContext(), "onCapability Changed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, " capabilities for the default network is " + networkCapabilities.toString());
                    Log.d(TAG, " does it have validated network connection internet presence : "
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
                            Log.d(TAG, " Internet Connection is lost temporarily for network: " + network.toString());
                        }
                    }
                }

                @Override
                public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    Log.d(TAG, "onLinkPropertiesChanged: " + network.toString());
                    Toast.makeText(context.getApplicationContext(), "onLinkProperties Changed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Log.d(TAG, "onLosing" + network.toString());
                    Toast.makeText(context.getApplicationContext(), "on Losing", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.d(TAG, "onUnavailable");
                    Toast.makeText(context.getApplicationContext(), "on Unavailable", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            Toast.makeText(context.getApplicationContext(), "registerNetworkCallback Exception", Toast.LENGTH_SHORT).show();
            GlobalVars.isNetworkConnected = false;
        }
    }
}