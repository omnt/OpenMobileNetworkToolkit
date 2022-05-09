package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;

public class NetworkCallback {
    private static final String TAG = "Network Callback";

    private Context context;
    private boolean HasCarrierPrivilages;

    public NetworkCallback(Context context){
        this.context = context;

    }

    public void setHasCarrierPrivilages(boolean privilages) {
        HasCarrierPrivilages = privilages;
    }


    //Check Network
    public void requestNetworkCallback(){
        try {
            /*ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            * */
            SliceFragment sliceFragment = new SliceFragment();
            ConnectivityManager connectivityManager = sliceFragment.connectivityManager;

            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            Network network = connectivityManager.getActiveNetwork();
            assert connectivityManager != null;
            Toast.makeText(context.getApplicationContext(), "Default Network:"+ network,Toast.LENGTH_SHORT).show();
            connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
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
            Log.d("Network Callback: Exception in requestNetworkCallback", "Catch exception RequestCallback");
            GlobalVars.isNetworkConnected = false;
        }
    }

    public void registerDefaultNetworkCallback() {
        //private static final String TAG = "Network Callback";
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);


            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            Network network = connectivityManager.getActiveNetwork();
            assert connectivityManager != null;

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

    void registerNetworkCallback(){
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            //Network network = connectivityManager.getActiveNetwork();

            assert connectivityManager != null;


            connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull android.net.Network network) {
                    super.onAvailable(network);
                    GlobalVars.isNetworkConnected = true;
                    Log.d(TAG, "onAvailable");
                }

                @Override
                public void onLost(@NonNull android.net.Network network) {
                    super.onLost(network);
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
                    Log.d(TAG," capabilities for the default network is " + networkCapabilities.toString());
                    Log.d(TAG," does it have validated network connection internet presence : "
                            + networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            + " is it validated "
                            + networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                    if(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){

                        if(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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
                    Log.d(TAG, "onLinkPropertiesChanged: "+ network.toString());
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Log.d(TAG, "onLosing"+network.toString());
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.d(TAG, "onUnavailable");

                }
            });

        } catch (Exception e){
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
            GlobalVars.isNetworkConnected = false;
        }
    }
}