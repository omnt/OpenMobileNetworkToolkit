package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static android.telephony.TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.telephony.TelephonyManager;
import android.telephony.data.TrafficDescriptor;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.databinding.FragmentSliceBinding;

public class SliceFragment extends Fragment{

    private static final String TAG = "SliceFragment";
    private Context context;
    public ConnectivityManager connectivityManager;
    //public Button callback;
    private String dnn;
    private FragmentSliceBinding binding;
    private boolean HasCarrierPrivilages;
    private int sdk_version;
    private TextView textView;
    private CharSequence msg = "";
    public Spanned spanColor;
    //private RecyclerView recyclerView;


    private SwipeRefreshLayout swipeRefreshLayout;

    public void setHasCarrierPrivilages(boolean privilages) {
        HasCarrierPrivilages = privilages;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSliceBinding.inflate(inflater, container, false);
        sdk_version = Build.VERSION.SDK_INT;
        //apn = (Button) binding.button;
        swipeRefreshLayout = binding.getRoot();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActivity().recreate();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

      /*  apn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Button", Toast.LENGTH_SHORT).show();
            }
        });*/

        // Add the following lines to create RecyclerView

        // 1. get a reference to recyclerView
        //RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.debug_Recycler);

        // 2. set layoutManger
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.setHasFixedSize(true);

        // 3. set adapter
        //recyclerView.setAdapter(new RandomNumListAdapter(1234));

        return binding.getRoot();
    }


    @RequiresApi(api = 33)
    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.onViewCreated(view, savedInstanceState);
            MainActivity ma = (MainActivity) getActivity();
            setHasCarrierPrivilages(ma.HasCarrierPermissions());
            TelephonyManager tm = ma.tm;

           /*connectivityManager = (ConnectivityManager)
                    getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);*/


            /*** Network/Network Capabilities/Link Properties ***/
            /* A Callback is needed to get the result of the mentioned values */
            NetworkCallback networkCallback = new NetworkCallback(getActivity().getApplicationContext());
            networkCallback.setHasCarrierPrivilages(true);
            //networkCallback.registerDefaultNetworkCallback();
            //networkCallback.requestNetworkCallback();
            ma.getOrganization(getContext());
            networkCallback.registerNetworkCallback();

            byte[] osAppId = null;

            //NetworkSliceInfo sliceInfo =  new Build()
           /* ConnectivityManager connectivityManager = getContext().getSystemService(ConnectivityManager.class);
            Network currentNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
            LinkProperties linkProperties = connectivityManager.getLinkProperties(currentNetwork);
            int up_speed = caps.getLinkDownstreamBandwidthKbps();
            int down_speed = caps.getLinkUpstreamBandwidthKbps();*/
            //boolean slicing_capability = caps.hasCapability(CAPABILITY_SLICING_CONFIG_SUPPORTED);

            //sliceCreate emb = new sliceCreate(1,-1,0);
            //trafficDescriptor descriptor = new trafficDescriptor();


            PersistableBundle configForSubId = new PersistableBundle();
            PackageManager pm = getContext().getPackageManager();
            boolean feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            boolean feature_slicing = pm.hasSystemFeature(CAPABILITY_SLICING_CONFIG_SUPPORTED);
            boolean feature_admin = pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN);
            boolean feature_phone_state = pm.hasSystemFeature(Manifest.permission.READ_PHONE_STATE);

            //TODO Use this for the identifation of user using the profile
            //int user_id = UserHandle.getUserHandleForUid();
            //boolean feature_privilage_phone_State = pm.hasSystemFeature;

            boolean work_profile = pm.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS);

            //Network currentNetwork = connectivityManager.getActiveNetworkInfo();
            /*NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
            LinkProperties linkProperties = connectivityManager.getLinkProperties(currentNetwork);*/

            ArrayList<String> props = new ArrayList<String>();
            if (HasCarrierPrivilages) {
                Toast.makeText(getActivity(), "Has Carrier Privilages", Toast.LENGTH_SHORT).show();
                if (feature_telephony) {
                    Toast.makeText(getActivity(), "Has telephony", Toast.LENGTH_SHORT).show();
                }
                props.add("Carrier Permissions: " + HasCarrierPrivilages);
                props.add("Feature Telephony: " + feature_telephony);
                props.add("Android SDK: " + Build.VERSION.SDK_INT);
                props.add("Android Release: " + Build.VERSION.RELEASE);


                props.add("Network Connection Available: " + GlobalVars.isNetworkConnected);
                props.add("READ_PHONE_STATE: " + feature_phone_state);
                props.add("Device Software Version: " + tm.getDeviceSoftwareVersion());
                props.add("IMEI: " + tm.getImei());
                props.add("SimSerial: " + tm.getSimSerialNumber());
                props.add("SubscriberId: " + tm.getSubscriberId());
                props.add("Carrier ID from SIMmmcmnc: " + tm.getCarrierIdFromSimMccMnc());

                props.add("Feature Slicing on Package Manager: " + feature_slicing); //for now false
                //props.add("UiCC Card Info: " + tm.getUiccCardsInfo());
                props.add("phone type: " + tm.getPhoneType());
                props.add("Work Profile: " + work_profile);
                props.add("Feature Admin: " + feature_admin);

                //props.add("Service State" + tm.getServiceState()); /* Service State Received, but blocked for now */

                //props.add("OSiD: " +trafficDescriptor.getOsAppId()); //OSiD when set receive and show here
                //props.add("DNN ID: " +trafficDescriptor.getDataNetworkName()); //DNN when received show here
                //props.add("Route Selection Mode: "+ RouteSelectionDescriptor.ROUTE_SSC_MODE_2);
                props.add("Supported Modem Count: " + tm.getSupportedModemCount());
                props.add("Network Operator: " + tm.getNetworkOperatorName());
                props.add("Sim Operator Name: " + tm.getSimOperatorName());
                props.add("Network Specifier: " + tm.getNetworkSpecifier());
                props.add("Data State: " + tm.getDataState());
                props.add("Registered PLMN: " + NetworkCallback.getPLMN(getContext()));
                props.add("Network Access Identifier: " + tm.getNai());
                props.add("preferred opportunistic data subscription Id: " + tm.getPreferredOpportunisticDataSubscription());
                props.add("Radio Interface Capability Slicing Config: " + tm.isRadioInterfaceCapabilitySupported(CAPABILITY_SLICING_CONFIG_SUPPORTED));

                props.add("Default Network: " + NetworkCallback.getCurrentNetwork(getContext()));

                //TODO Identification of user
                //props.add("User ID: " + )

                props.add("Interface Name: " + NetworkCallback.getInterfaceName(getContext()));
                props.add("Network counter: " + GlobalVars.counter);
                props.add("Default DNS: " + NetworkCallback.getDefaultDNS(getContext()));
                //props.add("Capability List:" + NetworkCallback.getNetworkCapabilitylist(getContext()));
                props.add("Enterprise Capability: " + NetworkCallback.getEnterpriseCapability(getContext()));

                //TODO GetEnterpriseID having problems //

                //props.add("Enterprise ID : " + NetworkCallback.getEnterpriseIdList(getContext()));

                props.add("Validated Capability: " + NetworkCallback.getValidity(getContext()));
                props.add("Internet Capability: " + NetworkCallback.getInternet(getContext()));
                props.add("IMS Capability: " + NetworkCallback.getIMS(getContext()));
                props.add("Capabilities: " + NetworkCallback.getNetworkCapabilitylist(getContext()));
                /* TODO Fix the getConfigurationTM */
                props.add("Enterprise ID: " + NetworkCallback.getEnterpriseIds(getContext()));
                props.add("TM Slice: " + NetworkCallback.getConfigurationTM(getContext()));
                props.add("Slice Info: " + NetworkCallback.getNetworkSlicingInfo(getContext()));
                props.add("Slice Config: " + NetworkCallback.getNetworkSlicingConfig(getContext()));
                props.add("Route Descriptor: " + NetworkCallback.getRouteSelectionDescriptor(getContext()));
                props.add("Traffic Descriptor: " + NetworkCallback.getTrafficDescriptor(getContext()));
                //props.add("Features: " +NetworkCallback.getFeatureList(getContext()));
                //props.add("Service State: " +NetworkCallback.getNetworkRegistrationInfo(getContext()));


                //props.add("log" + networkCallback.sliceFragment.connectivityManager.isDefaultNetworkActive());

                //props.add("All Cell Info: \n"+tm.getAllCellInfo()) ;
                //props.add("Signal Strength: \n" +tm.getSignalStrength());
                //props.add("Down Speed: " +down_speed);

                for (String prop : props) {
                    TextView tv = new TextView(getContext());
                    tv.setText(prop);
                    binding.sliceInfo.addView(tv);
                }

            } else {
                TextView tv = new TextView(getContext());
                tv.setText("This app only works with Carrier Privileges. Make Sure you have the correct SHA1 fingerprint on your SIM Card.");
                binding.sliceInfo.addView(tv);

            }
        } else {
            TextView tv = new TextView(getContext());
                tv.setText("Slicing not supported below API leve; 33");
        }
    }



    /**
     * Don't Change the order of this method code lines.
     */
    /*private void addTextViewToLayout(String text, int textColor) {
        spanColor = setSpanColor(text, ContextCompat.getColor(getContext(), textColor));
        msg = TextUtils.concat(msg, "\n\n", spanColor);
        if (null == textView) {
            TextView debbugger_output = new TextView(getContext());
            textView = binding.debuggerOutput;
            textView.setGravity(Gravity.LEFT);
        }
        textView.setText(msg);
    }

    private static Spanned setSpanColor(String s, int color) {
        SpannableString ss = new SpannableString(s);
        ss.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        return ss;
    }

    public static void setDebugText(String msg, int textColor) {
        if (context != null)
            addTextViewToLayout(msg, textColor);
        Log.d(TAG, "NOTHING!!");
    }*/

    // TODO Commented for later use, for stopping the debug log, uncomment if required
    /*public static void stop() {
        if (null != context.getApplicationContext()) context.getApplicationContext().stopSelf();
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    public class trafficDescriptor {

        trafficDescriptor() {
            TrafficDescriptor response = new TrafficDescriptor.Builder()
                    .setDataNetworkName("")
                    .build();
        }

        trafficDescriptor(String dnn, byte[] osAppID) {
            TrafficDescriptor response = new TrafficDescriptor.Builder()
                    .setDataNetworkName(dnn)
                    .setOsAppId(osAppID)
                    .build();
        }
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
