package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.OutcomeReceiver;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.databinding.FragmentSlicingsetupBinding;
import java.util.ArrayList;
import java.util.List;


public class SlicingSetup extends Fragment {

    private static final String TAG = "SliceSetupFragment";
    private boolean HasCarrierPrivilages;
    private FragmentSlicingsetupBinding binding;
    private MaterialButton btn_enterprise1;
    private MaterialButton btn_enterprise2;
    private MaterialButton btn_enterprise3;
    private SwipeRefreshLayout swipeRefreshLayout;


    public void setHasCarrierPrivilages(boolean privilages) {
        HasCarrierPrivilages = privilages;
    }


    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup parent,
        Bundle savedInstanceState
    ) {
        binding = FragmentSlicingsetupBinding.inflate(inflater, parent, false);
        btn_enterprise1 = (MaterialButton) binding.btnENTERPRISE1;
        btn_enterprise2 = (MaterialButton) binding.btnENTERPRISE2;
        btn_enterprise3 = (MaterialButton) binding.btnENTERPRISE3;
        swipeRefreshLayout = binding.getRoot();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // todo investigate
                //getActivity().recreate();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        View view = binding.getRoot();
        return view;
        //return inflater.inflate(R.layout.fragment_slicingsetup, parent,false);
    }

    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity ma = (MainActivity) getActivity();
        setHasCarrierPrivilages(ma.cp);
        PackageManager pm = getContext().getPackageManager();
        boolean feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        TelephonyManager tm = ma.tm;
        NetworkCallback networkCallback =
            new NetworkCallback(getActivity().getApplicationContext());
        networkCallback.setHasCarrierPrivilages(tm.hasCarrierPrivileges());
        SliceCreate sliceCreate = new SliceCreate();
        ma.getOrganization(getContext());
        ArrayList<String> props = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            btn_enterprise1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG, "Enterprise 1 network request: " +
                        networkCallback.customNetworkCallback(29, 0));

                    if (networkCallback.customNetworkCallback(29, 0)) { //CAPABILITY ENTERPRISE1)
                        tm.getNetworkSlicingConfiguration(getActivity().getMainExecutor(),
                            new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                                @Override
                                public void onResult(
                                    @NonNull NetworkSlicingConfig networkSlicingConfig) {
                                    NetworkSlicingConfig networkSlicingConfig1 =
                                        networkSlicingConfig;
                                    List<UrspRule> urspRuleList =
                                        networkSlicingConfig1.getUrspRules();


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
                                                    props.add("Traffic Descriptor Available");
                                                    props.add("Traffic Descriptor DNN: " +
                                                        trafficDescriptor.getDataNetworkName());
                                                    props.add("Traffic Descriptor Os App ID: " +
                                                        trafficDescriptor.getOsAppId());

                                                }
                                            }
                                        }
                                    }

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
                                                List<NetworkSliceInfo> networkSliceInfoList =
                                                    routeSelectionDescriptor.getSliceInfo();

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
                                                    }
                                                    Log.d(TAG, "Route Selection Precedence: " +
                                                        routeSelectionDescriptor.getPrecedence());
                                                    Log.d(TAG, "Route Selection Session Type: " +
                                                        routeSelectionDescriptor.getSessionType());
                                                    Log.d(TAG, "Route Selection SSC Mode: " +
                                                        routeSelectionDescriptor.getSscMode());

                                                    if (networkSliceInfoList != null) {
                                                        for (int l = 0;
                                                             l < networkSliceInfoList.size(); l++) {
                                                            NetworkSliceInfo networkSliceInfo =
                                                                networkSliceInfoList.get(i);

                                                            int service_type =
                                                                networkSliceInfo.getSliceServiceType();
                                                            int service_status =
                                                                networkSliceInfo.getStatus();
                                                            int slice_differentior =
                                                                networkSliceInfo.getSliceDifferentiator();
                                                            int mapped_plmn_diff =
                                                                networkSliceInfo.getMappedHplmnSliceDifferentiator();
                                                            int mapped_plmn_service_type =
                                                                networkSliceInfo.getMappedHplmnSliceServiceType();

                                                            sliceCreate.sliceCreate(service_type,
                                                                slice_differentior, service_status);
                                                            Log.d(TAG, "Slice Created for :" +
                                                                service_type);
                                                        }
                                                    }


                                                }
                                            }
                                        }
                                    }
                                }
                            });
                    }
                }
            });

            btn_enterprise2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    networkCallback.customNetworkCallback(2, 0); // CAPABILITY ENTERPRISE2
                    props.add("Carrier Permissions: " + HasCarrierPrivilages);
                    props.add("Feature Telephony: " + feature_telephony);
                    props.add("Network Connection Available: " + GlobalVars.isNetworkConnected);
                    //props.add("Enterprise Capability: " + nc.getEnterpriseCapability(getContext()));
                    //props.add("TM Slice: " + nc.getConfigurationTM(getContext()));
                    tm.getNetworkSlicingConfiguration(getActivity().getMainExecutor(),
                        new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                            @Override
                            public void onResult(
                                @NonNull NetworkSlicingConfig networkSlicingConfig) {
                                NetworkSlicingConfig networkSlicingConfig1 = networkSlicingConfig;
                                List<UrspRule> urspRuleList = networkSlicingConfig1.getUrspRules();


                                for (int i = 0; i < urspRuleList.size(); i++) {
                                    UrspRule urspRule = networkSlicingConfig.getUrspRules().get(i);
                                    List<TrafficDescriptor> trafficDescriptorList =
                                        urspRule.getTrafficDescriptors();
                                    List<RouteSelectionDescriptor> routeSelectionDescriptorsList =
                                        urspRule.getRouteSelectionDescriptor();
                                    if (trafficDescriptorList != null) {
                                        for (int j = 0; i < trafficDescriptorList.size(); i++) {
                                            TrafficDescriptor trafficDescriptor =
                                                urspRule.getTrafficDescriptors().get(i);
                                            //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);

                                            if (trafficDescriptor != null) {
                                                props.add("Traffic Descriptor Available");
                                                props.add("Traffic Descriptor DNN: " +
                                                    trafficDescriptor.getDataNetworkName());
                                                props.add("Traffic Descriptor Os App ID: " +
                                                    trafficDescriptor.getOsAppId());

                                            }
                                        }
                                    }
                                }

                                for (int i = 0; i < urspRuleList.size(); i++) {
                                    UrspRule urspRule = networkSlicingConfig.getUrspRules().get(i);
                                    List<TrafficDescriptor> trafficDescriptorList =
                                        urspRule.getTrafficDescriptors();
                                    List<RouteSelectionDescriptor> routeSelectionDescriptorsList =
                                        urspRule.getRouteSelectionDescriptor();
                                    //Log.d(TAG, "URSP" + urspRule);
                                    //Log.d(TAG, "Traffic Descriptor" + trafficDescriptor);
                                    if (routeSelectionDescriptorsList != null) {
                                        for (int j = 0; i < routeSelectionDescriptorsList.size();
                                             i++) {
                                            RouteSelectionDescriptor routeSelectionDescriptor =
                                                routeSelectionDescriptorsList.get(i);
                                            //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);
                                            List<NetworkSliceInfo> networkSliceInfoList =
                                                routeSelectionDescriptor.getSliceInfo();

                                            if (routeSelectionDescriptor != null) {
                                                Log.d(TAG, "Route Selection Descriptor Available");
                                                List<String> dataNetworkNameList =
                                                    routeSelectionDescriptor.getDataNetworkName();

                                                if (dataNetworkNameList != null) {
                                                    for (int k = 0; k < dataNetworkNameList.size();
                                                         k++) {
                                                        Log.d(TAG, "Data Network Name DNN: " +
                                                            dataNetworkNameList.get(i));
                                                    }
                                                }
                                                Log.d(TAG, "Route Selection Precedence: " +
                                                    routeSelectionDescriptor.getPrecedence());
                                                Log.d(TAG, "Route Selection Session Type: " +
                                                    routeSelectionDescriptor.getSessionType());
                                                Log.d(TAG, "Route Selection SSC Mode: " +
                                                    routeSelectionDescriptor.getSscMode());

                                                if (networkSliceInfoList != null) {
                                                    for (int l = 0; l < networkSliceInfoList.size();
                                                         l++) {
                                                        NetworkSliceInfo networkSliceInfo =
                                                            networkSliceInfoList.get(i);

                                                        int service_type =
                                                            networkSliceInfo.getSliceServiceType();
                                                        int service_status =
                                                            networkSliceInfo.getStatus();
                                                        int slice_differentior =
                                                            networkSliceInfo.getSliceDifferentiator();
                                                        int mapped_plmn_diff =
                                                            networkSliceInfo.getMappedHplmnSliceDifferentiator();
                                                        int mapped_plmn_service_type =
                                                            networkSliceInfo.getMappedHplmnSliceServiceType();

                                                        sliceCreate.sliceCreate(service_type,
                                                            slice_differentior, service_status,
                                                            mapped_plmn_service_type,
                                                            mapped_plmn_diff);
                                                        Log.d(TAG,
                                                            "Slice Created for :" + service_type);
                                                    }
                                                }


                                            }
                                        }
                                    }
                                }
                            }
                        });
                }
            });

            btn_enterprise3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    networkCallback.customNetworkCallback(3, 0); //CAPABILITY ENTERPRISE 3
                    props.add("Carrier Permissions: " + HasCarrierPrivilages);
                    props.add("Feature Telephony: " + feature_telephony);
                    props.add("Network Connection Available: " + GlobalVars.isNetworkConnected);
                    //props.add("Enterprise Capability: " + NetworkCallback.getEnterpriseCapability(getContext()));
                    //props.add("TM Slice: " + NetworkCallback.getConfigurationTM(getContext()));
                    tm.getNetworkSlicingConfiguration(getActivity().getMainExecutor(),
                        new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                            @Override
                            public void onResult(
                                @NonNull NetworkSlicingConfig networkSlicingConfig) {
                                NetworkSlicingConfig networkSlicingConfig1 = networkSlicingConfig;
                                List<UrspRule> urspRuleList = networkSlicingConfig1.getUrspRules();


                                for (int i = 0; i < urspRuleList.size(); i++) {
                                    UrspRule urspRule = networkSlicingConfig.getUrspRules().get(i);
                                    List<TrafficDescriptor> trafficDescriptorList =
                                        urspRule.getTrafficDescriptors();
                                    List<RouteSelectionDescriptor> routeSelectionDescriptorsList =
                                        urspRule.getRouteSelectionDescriptor();
                                    if (trafficDescriptorList != null) {
                                        for (int j = 0; i < trafficDescriptorList.size(); i++) {
                                            TrafficDescriptor trafficDescriptor =
                                                urspRule.getTrafficDescriptors().get(i);
                                            //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);

                                            if (trafficDescriptor != null) {
                                                props.add("Traffic Descriptor Available");
                                                props.add("Traffic Descriptor DNN: " +
                                                    trafficDescriptor.getDataNetworkName());
                                                props.add("Traffic Descriptor Os App ID: " +
                                                    trafficDescriptor.getOsAppId());

                                            }
                                        }
                                    }
                                }

                                for (int i = 0; i < urspRuleList.size(); i++) {
                                    UrspRule urspRule = networkSlicingConfig.getUrspRules().get(i);
                                    List<TrafficDescriptor> trafficDescriptorList =
                                        urspRule.getTrafficDescriptors();
                                    List<RouteSelectionDescriptor> routeSelectionDescriptorsList =
                                        urspRule.getRouteSelectionDescriptor();
                                    //Log.d(TAG, "URSP" + urspRule);
                                    //Log.d(TAG, "Traffic Descriptor" + trafficDescriptor);
                                    if (routeSelectionDescriptorsList != null) {
                                        for (int j = 0; i < routeSelectionDescriptorsList.size();
                                             i++) {
                                            RouteSelectionDescriptor routeSelectionDescriptor =
                                                routeSelectionDescriptorsList.get(i);
                                            //Log.d(TAG, "Route Selection" + routeSelectionDescriptor);
                                            List<NetworkSliceInfo> networkSliceInfoList =
                                                routeSelectionDescriptor.getSliceInfo();

                                            if (routeSelectionDescriptor != null) {
                                                Log.d(TAG, "Route Selection Descriptor Available");
                                                List<String> dataNetworkNameList =
                                                    routeSelectionDescriptor.getDataNetworkName();

                                                if (dataNetworkNameList != null) {
                                                    for (int k = 0; k < dataNetworkNameList.size();
                                                         k++) {
                                                        Log.d(TAG, "Data Network Name DNN: " +
                                                            dataNetworkNameList.get(i));
                                                    }
                                                }
                                                Log.d(TAG, "Route Selection Precedence: " +
                                                    routeSelectionDescriptor.getPrecedence());
                                                Log.d(TAG, "Route Selection Session Type: " +
                                                    routeSelectionDescriptor.getSessionType());
                                                Log.d(TAG, "Route Selection SSC Mode: " +
                                                    routeSelectionDescriptor.getSscMode());

                                                if (networkSliceInfoList != null) {
                                                    for (int l = 0; l < networkSliceInfoList.size();
                                                         l++) {
                                                        NetworkSliceInfo networkSliceInfo =
                                                            networkSliceInfoList.get(i);

                                                        int service_type =
                                                            networkSliceInfo.getSliceServiceType();
                                                        int service_status =
                                                            networkSliceInfo.getStatus();
                                                        int slice_differentior =
                                                            networkSliceInfo.getSliceDifferentiator();
                                                        int mapped_plmn_diff =
                                                            networkSliceInfo.getMappedHplmnSliceDifferentiator();
                                                        int mapped_plmn_service_type =
                                                            networkSliceInfo.getMappedHplmnSliceServiceType();

                                                        sliceCreate.sliceCreate(service_type,
                                                            slice_differentior, service_status);
                                                        Log.d(TAG,
                                                            "Slice Created for :" + service_type);
                                                    }
                                                }


                                            }
                                        }
                                    }
                                }
                            }
                        });
                }
            });
        }


        if (HasCarrierPrivilages) {
            Toast.makeText(getActivity(), "Has Carrier Privilages", Toast.LENGTH_SHORT).show();
            if (feature_telephony) {
                Toast.makeText(getActivity(), "Has telephony", Toast.LENGTH_SHORT).show();
            }
            props.add("Carrier Permissions: " + HasCarrierPrivilages);
            props.add("Feature Telephony: " + feature_telephony);

            props.add("Network Connection Available: " + GlobalVars.isNetworkConnected);
            //props.add("Enterprise Capability: " +NetworkCallback.getEnterpriseCapability(getContext()));
            /*props.add("TM Slice: " +NetworkCallback.getConfigurationTM(getContext()));
            props.add("Slice Info: " +NetworkCallback.getNetworkSlicingInfo(getContext()));
            props.add("Slice Config: " +NetworkCallback.getNetworkSlicingConfig(getContext()));
            props.add("Route Descriptor: " + NetworkCallback.getRouteSelectionDescriptor(getContext()));
            props.add("Traffic Descriptor: " +NetworkCallback.getTrafficDescriptor(getContext()));*/


            for (String prop : props) {
                TextView tv = new TextView(getContext());
                tv.setText(prop);
                binding.sliceInfo.addView(tv);
            }

        } else {
            TextView tv = new TextView(getContext());
            tv.setText(
                "The slicing feature only works with Carrier Privilages. Make Sure you have the correct SHA1 fingerprint on your SIM Card.");
            binding.sliceInfo.addView(tv);

        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            TextView tv = new TextView(getContext());
            tv.setText("Slicing requires android API Level >= 11");
            binding.sliceInfo.addView(tv);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
