
/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static android.telephony.TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED;
import static androidx.core.content.ContextCompat.getSystemService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import java.util.ArrayList;

import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.util.Log;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.databinding.ActivityMainBinding;

public class HomeFragment extends Fragment {
    public CarrierConfigManager ccm;
    private PersistableBundle cc;
    private ConnectivityManager cm;
    public ConnectivityManager connectivityManager;
    public TelephonyManager tm;
    public PackageManager pm;
    private boolean cp;
    private MainActivity ma;
    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState
    )
    {
        ma = (MainActivity) getActivity();
        tm = (TelephonyManager) ma.getSystemService(Context.TELEPHONY_SERVICE);
        ccm = (CarrierConfigManager) ma.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        cp = tm.hasCarrierPrivileges();
        pm = getContext().getPackageManager();

        return inflater.inflate(R.layout.fragment_home, parent,false);
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        boolean feature_admin = pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN);
        boolean feature_phone_state = pm.hasSystemFeature(Manifest.permission.READ_PHONE_STATE);
        boolean work_profile = pm.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS);

        /*TrafficDescriptor trafficDescriptor = new TrafficDescriptor.Builder()
                .build();
        String osAppID = (trafficDescriptor.getOsAppId()).toString();
        String dataNetworkName = (trafficDescriptor.getDataNetworkName());
        props.add("OSiD: " +trafficDescriptor.getOsAppId());
        props.add("DNN ID: " +trafficDescriptor.getDataNetworkName());*/

        ArrayList<String> props = new ArrayList<String>();
        props.add("Carrier Permissions: " + cp);
        props.add("Feature Telephony: " + feature_telephony);
        props.add("Android SDK: " + Build.VERSION.SDK_INT);
        props.add("Android Release: " + Build.VERSION.RELEASE);
        props.add("Phone Type: " + tm.getPhoneType());
        props.add("Work Profile: " + work_profile);
        props.add("Feature Admin: " + feature_admin);
        props.add("Network Connection Available: " + GlobalVars.isNetworkConnected);
        props.add("READ_PHONE_STATE: " + feature_phone_state);
        props.add("Radio Version: " + Build.getRadioVersion());
        props.add("SOC Manufacturer: " + Build.SOC_MANUFACTURER);
        props.add("SOC Model: " + Build.SOC_MODEL);
        props.add("Supported Modem Count: " + tm.getSupportedModemCount());
        props.add("Network Operator: " + tm.getNetworkOperatorName());
        props.add("Sim Operator Name: " + tm.getSimOperatorName());
        props.add("Network Specifier: " + tm.getNetworkSpecifier());
        props.add("DataState: " + tm.getDataState());
        props.add("Registered PLMN: " + NetworkCallback.getPLMN(getContext()));
        props.add("Network Access Identifier: " + tm.getNai());
        props.add("Preferred Opportunistic Data Subscription ID: " + tm.getPreferredOpportunisticDataSubscription());
        props.add("Radio Interface Capability Slicing Config: " + tm.isRadioInterfaceCapabilitySupported(CAPABILITY_SLICING_CONFIG_SUPPORTED));
        props.add("Default Network: " + NetworkCallback.getCurrentNetwork(getContext()));
        props.add("Interface Name: " + NetworkCallback.getInterfaceName(getContext()));
        props.add("Network counter: " + GlobalVars.counter);
        props.add("Default DNS: " + NetworkCallback.getDefaultDNS(getContext()));
        props.add("Enterprise Capability: " + NetworkCallback.getEnterpriseCapability(getContext()));
        props.add("Validated Capability: " + NetworkCallback.getValidity(getContext()));
        props.add("Internet Capability: " + NetworkCallback.getInternet(getContext()));
        props.add("IMS Capability: " + NetworkCallback.getIMS(getContext()));
        props.add("Capabilities: " + NetworkCallback.getNetworkCapabilitylist(getContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            props.add("Enterprise ID: " + NetworkCallback.getEnterpriseIds(getContext()));
        }
        props.add("TM Slice: " + NetworkCallback.getConfigurationTM(getContext()));
        props.add("Slice Info: " + NetworkCallback.getNetworkSlicingInfo(getContext()));
        props.add("Slice Config: " + NetworkCallback.getNetworkSlicingConfig(getContext()));
        props.add("Route Descriptor: " + NetworkCallback.getRouteSelectionDescriptor(getContext()));
        props.add("Traffic Descriptor: " + NetworkCallback.getTrafficDescriptor(getContext()));
        if (cp) { // todo try root privileges or more fine granular permission
            props.add("Device Software version: " + tm.getDeviceSoftwareVersion());
            props.add("Device SDK version: " + Build.VERSION.SDK_INT);
            props.add("IMEI: " + tm.getImei());
            props.add("SimSerial: " + tm.getSimSerialNumber());
            props.add("SubscriberId: " + tm.getSubscriberId());
            // todo move to config menu
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tm.setForbiddenPlmns(new ArrayList<String>());
                // todo move this to a setting menu
                //tm.setNetworkSelectionModeManual("00101", true, AccessNetworkConstants.AccessNetworkType.NGRAN);
            }
            StringBuilder tmp = new StringBuilder();
            for (String plnm : tm.getForbiddenPlmns()) {
                tmp.append(plnm).append(" ");
            }
            props.add("Forbidden PLMNs: " + tmp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                props.add("SubscriptionId: " + tm.getSubscriptionId());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PackageManager pm = ma.getPackageManager();
                boolean sc = pm.hasSystemFeature(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED);
                props.add("Slicing Config supported: " + sc);
            }
            props.add("DataNetworkType: " + tm.getDataNetworkType());
            if (ActivityCompat.checkSelfPermission(ma, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                props.add("Cell Information: " + tm.getAllCellInfo());
            }
            props.add("SignalStrength: " + tm.getSignalStrength());
        }
        TextView main_infos = getView().findViewById(R.id.main_infos);
        for (String prop : props) {
            main_infos.append(prop + "\n");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
