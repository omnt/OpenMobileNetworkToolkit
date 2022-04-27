package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static android.telephony.data.NetworkSliceInfo.SLICE_SERVICE_TYPE_EMBB;
import static android.telephony.TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED;

import android.annotation.SuppressLint;
import android.app.slice.Slice;
import android.app.slice.SliceItem;
import android.app.slice.SliceManager;
import android.app.slice.SliceSpec;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.service.carrier.CarrierIdentifier;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.RoundedCorner;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.databinding.SliceFragmentBinding;

@RequiresApi(api = Build.VERSION_CODES.S)
public class SliceFragment extends Fragment {

    private static final String TAG = "NetworkSlicing";
    private String dnn;
    private SliceFragmentBinding binding;
    private boolean HasCarrierPrivilages;
    private int sdk_version;
    public ConnectivityManager connectivityManager;

    public void setHasCarrierPrivilages(boolean privilages) {
        HasCarrierPrivilages = privilages;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = SliceFragmentBinding.inflate(inflater, container, false);
        sdk_version = Build.VERSION.SDK_INT;
        return binding.getRoot();

    }

    //@SuppressLint("MissingPermission")
    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity ma = (MainActivity) getActivity();
        setHasCarrierPrivilages(ma.HasCarrierPermissions());
        TelephonyManager tm = ma.tm;
        connectivityManager = (ConnectivityManager)
                getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
        /*Network currentNetwork = connectivityManager.getBoundNetworkForProcess();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(currentNetwork);*/

        /*** Network/Network Capabilities/Link Properties ***/
        /* A Callback is needed to get the result of the mentioned values */


        byte[] osAppId = null;


       /* ConnectivityManager connectivityManager = getContext().getSystemService(ConnectivityManager.class);
        Network currentNetwork = connectivityManager.getActiveNetwork();

        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(currentNetwork);

        int up_speed = caps.getLinkDownstreamBandwidthKbps();
        int down_speed = caps.getLinkUpstreamBandwidthKbps();*/
        //boolean slicing_capability = caps.hasCapability(CAPABILITY_SLICING_CONFIG_SUPPORTED);


        PersistableBundle configForSubId = new PersistableBundle();
        //tm = defaultSubTelephonyManager.createForSubscriptionId(subId);
        PackageManager pm = getContext().getPackageManager();
        boolean feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        boolean feature_slicing = pm.hasSystemFeature(CAPABILITY_SLICING_CONFIG_SUPPORTED);
        
        /* TrafficDescriptor trafficDescriptor = new TrafficDescriptor.Builder()
                    .setOsAppId(osAppId)
                 .build();
        String osAppID = (trafficDescriptor.getOsAppId()).toString();
        String dataNetworkName = (trafficDescriptor.getDataNetworkName());*/


        ArrayList<String> props = new ArrayList<String>();
        if (HasCarrierPrivilages) {
            Toast.makeText(getActivity(), "Has Carrier Privilages", Toast.LENGTH_SHORT).show();
            if (feature_telephony) {
                Toast.makeText(getActivity(), "Has telephony", Toast.LENGTH_SHORT).show();
            }
            props.add("Carrier Permissions: " + HasCarrierPrivilages);
            props.add("Feature Telephony: " + feature_telephony);
            props.add("IMEI: " + tm.getImei());
            props.add("SimSerial: " + tm.getSimSerialNumber());
            props.add("SubscriberId: " + tm.getSubscriberId());
            props.add("Slicing Config: " +feature_slicing); //for now false
            props.add("UiCC Card Info: " + tm.getUiccCardsInfo());
            props.add("phone type: " +tm.getPhoneType());
            //props.add("OSiD: " +trafficDescriptor.getOsAppId()); //OSiD when set receive and show here
            //props.add("DNN ID: " +trafficDescriptor.getDataNetworkName()); //DNN when received show here
            //props.add("Route Selection Mode: "+ RouteSelectionDescriptor.ROUTE_SSC_MODE_2);
            props.add("Supported Modem Count: " + tm.getSupportedModemCount());
            props.add("Network Operator: "+ tm.getNetworkOperatorName());
            props.add("Sim Operator Name: "+ tm.getSimOperatorName());
            props.add("Network Specifier: " +tm.getNetworkSpecifier());
            props.add("Data State: " +tm.getDataState());
            props.add("Default Network Active:" + connectivityManager.isDefaultNetworkActive());
            props.add("");
            //props.add("UP Speed: " +up_speed);
            //props.add("Down Speed: " +down_speed);


            for (String prop : props) {
                TextView tv = new TextView(getContext());
                tv.setText(prop);
                binding.sliceInfo.addView(tv);
            }

        } else {
            TextView tv = new TextView(getContext());
            tv.setText("This app only works with Carrier Privilages. Make Sure you have the correct SHA1 fingerprint on your SIM Card.");
            binding.sliceInfo.addView(tv);
        }


        }
    @Override
    public void onDestroyView () {
        super.onDestroyView();
        binding = null;
    }
}
   /* public NetworkSlice(){
        //super();
        NetworkSliceInfo sliceURLLC = new NetworkSliceInfo.Builder()
                .setSliceServiceType(SLICE_SERVICE_TYPE_URLLC)
                .setStatus(SLICE_STATUS_DEFAULT_CONFIGURED)
                .build();

        NetworkSliceInfo sliceEMB = new NetworkSliceInfo.Builder()
                .setSliceServiceType(SLICE_SERVICE_TYPE_EMBB)
                .setStatus(SLICE_STATUS_CONFIGURED)
                .build();

        NetworkSliceInfo sliceMIOT = new NetworkSliceInfo.Builder()
                .setSliceServiceType(SLICE_SERVICE_TYPE_MIOT)
                .setStatus(SLICE_STATUS_UNKNOWN)
                .build();
        Log.d(TAG, "Network Slicing");
    }

    public PersistableBundle onSliceConfig(CarrierIdentifier id){
        int sdk_version = Build.VERSION.SDK_INT;
        Log.i(TAG, "CarrierIdentifier id " + id.toString());
        PersistableBundle configForSlicing = new PersistableBundle();

        //Slice Manager


       //Network Slice configuration
        NetworkSlicingConfig slicingConfig = new NetworkSlicingConfig();
        slicingConfig.getSliceInfo();
        slicingConfig.getUrspRules();
        slicingConfig.describeContents();

        //Route Selector
        RouteSelectionDescriptor routeSelectionDescriptor = null;
        assert false;
        routeSelectionDescriptor.getSliceInfo();
        routeSelectionDescriptor.getDataNetworkName();
        routeSelectionDescriptor.getSessionType();
        routeSelectionDescriptor.getPrecedence();
        routeSelectionDescriptor.getSscMode();

        //Traffic Descriptor
        TrafficDescriptor trafficDescriptor = new TrafficDescriptor.Builder()
                .setDataNetworkName(dnn)
                .build();
        trafficDescriptor.getDataNetworkName();

        //URSP Rules
        UrspRule urspRule = null;
        assert false;
        urspRule.getRouteSelectionDescriptor();
        urspRule.getTrafficDescriptors();

        return null; //replace with network slice info

        //Wireshark Radio observe
        //Ask for Network Slices on Core
        //Wireshark N5

    }*/
