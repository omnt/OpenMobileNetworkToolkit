
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
import android.provider.MediaStore;
import android.telephony.AccessNetworkConstants;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.TrafficDescriptor;
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

    public void setHasCarrierPrivilages(boolean privilages) {
        HasCarrierPrivilages = privilages;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = HomeFragmentBinding.inflate(inflater, container, false);
        sdk_version = Build.VERSION.SDK_INT;
        return binding.getRoot();

    }

    @SuppressLint({"HardwareIds","MissingPermission"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity ma = (MainActivity) getActivity();
        setHasCarrierPrivilages(ma.HasCarrierPermissions());
        TelephonyManager tm = ma.tm;



        /*TrafficDescriptor trafficDescriptor = new TrafficDescriptor.Builder()
                .build();
        String osAppID = (trafficDescriptor.getOsAppId()).toString();
        String dataNetworkName = (trafficDescriptor.getDataNetworkName());
        props.add("OSiD: " +trafficDescriptor.getOsAppId());
        props.add("DNN ID: " +trafficDescriptor.getDataNetworkName());*/

        ArrayList<String> props = new ArrayList<String>();
        if (HasCarrierPrivilages) {
            props.add("Carrier Permissions: " + HasCarrierPrivilages);
            props.add("Device Software version: " + tm.getDeviceSoftwareVersion());
            props.add("Device SDK version: " + Build.VERSION.SDK_INT);
            props.add("IMEI: " + tm.getImei());
            props.add("SimSerial: " + tm.getSimSerialNumber());
            props.add("SubscriberId: " + tm.getSubscriberId());
            props.add("Radio Version: " + Build.getRadioVersion());
            props.add("SOC Manufacturer: " + Build.SOC_MANUFACTURER);
            props.add("SOC Model: " + Build.SOC_MODEL);

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
            props.add("DataState: " + tm.getDataState());
            //java.util.List<android.telephony.CellInfo> cell_info = tm.getAllCellInfo();
            if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                props.add("Cell Information: " + tm.getAllCellInfo());
            }

            props.add("SignalStrength: " + tm.getSignalStrength());
            for (String prop : props) {
                TextView tv = new TextView(getContext());
                tv.setText(prop);
                binding.mainInfos.addView(tv);
            }
        } else {
            TextView tv = new TextView(getContext());
            tv.setText("This app only works with Carrier Privilages. Make Sure you have the correct SHA1 fingerprint on your SIM Card.");
            binding.mainInfos.addView(tv);
        }
        //binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        NavHostFragment.findNavController(HomeFragment.this)
        //                .navigate(R.id.action_FirstFragment_to_SecondFragment);
        //    }
        //});
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
