
/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.databinding.HomeFragmentBinding;

public class HomeFragment extends Fragment {

    private HomeFragmentBinding binding;
    private boolean HasCarrierPrivilages;
    private int sdk_version;

    public void setHasCarrierPrivileges(boolean privileges) {
        HasCarrierPrivilages = privileges;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        sdk_version = Build.VERSION.SDK_INT;
        return binding.getRoot();
    }

    //@SuppressLint({"HardwareIds"})
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity ma = (MainActivity) getActivity();
        setHasCarrierPrivileges(ma.HasCarrierPermissions());
        TelephonyManager tm = ma.tm;

        /*TrafficDescriptor trafficDescriptor = new TrafficDescriptor.Builder()
                .build();
        String osAppID = (trafficDescriptor.getOsAppId()).toString();
        String dataNetworkName = (trafficDescriptor.getDataNetworkName());
        props.add("OSiD: " +trafficDescriptor.getOsAppId());
        props.add("DNN ID: " +trafficDescriptor.getDataNetworkName());*/

        ArrayList<String> props = new ArrayList<String>();
        props.add("Carrier Permissions: " + HasCarrierPrivilages);
        props.add("Radio Version: " + Build.getRadioVersion());
        props.add("SOC Manufacturer: " + Build.SOC_MANUFACTURER);
        props.add("SOC Model: " + Build.SOC_MODEL);
        props.add("DataState: " + tm.getDataState());
        if (HasCarrierPrivilages) { // todo try root privileges or more fine granular permission
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
            if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                props.add("Cell Information: " + tm.getAllCellInfo());
            }
            props.add("SignalStrength: " + tm.getSignalStrength());
        }
        for (String prop : props) {
            TextView tv = new TextView(getContext());
            tv.setText(prop);
            binding.mainInfos.addView(tv);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
