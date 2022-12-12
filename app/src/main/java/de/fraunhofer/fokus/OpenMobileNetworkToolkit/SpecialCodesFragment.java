/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SpecialCodesFragment extends Fragment {
    private static final String TAG = "SPECIAL_CODES";
    private View v;
    private Button android_testing;
    private Button mediatek_ims;
    private Button sony_service;
    private Button nokia_enable_sa;
    private Button samsung_ims;
    private Button huawei_projects;
    private Button custom_special_code;
    private TextView special_code;
    TelephonyManager tm;
    boolean cp = false;
    public SpecialCodesFragment() {
        super(R.layout.fragment_special_codes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        v = inflater.inflate(R.layout.fragment_special_codes, parent, false);
        android_testing = v.findViewById(R.id.bt_android_testing);
        android_testing.setOnClickListener(this::buttonHandler);
        mediatek_ims = v.findViewById(R.id.bt_mediatek_ims);
        mediatek_ims.setOnClickListener(this::buttonHandler);
        sony_service = v.findViewById(R.id.bt_sony_service);
        sony_service.setOnClickListener(this::buttonHandler);
        nokia_enable_sa = v.findViewById(R.id.bt_nokia_enable_SA);
        nokia_enable_sa.setOnClickListener(this::buttonHandler);
        samsung_ims = v.findViewById(R.id.bt_samsung_ims);
        samsung_ims.setOnClickListener(this::buttonHandler);
        huawei_projects = v.findViewById(R.id.bt_huawei_project);
        huawei_projects.setOnClickListener(this::buttonHandler);
        custom_special_code = v.findViewById(R.id.bt_custom_special_code);
        custom_special_code.setOnClickListener(this::buttonHandler);
        special_code = v.findViewById(R.id.tv_special_code);
        return v;
    }

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            cp = tm.hasCarrierPrivileges();
        }
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

    }

    @SuppressLint("NonConstantResourceId")
    private void buttonHandler(View view){
        if (cp) {
            switch (view.getId()) {
                case R.id.bt_android_testing:
                    tm.sendDialerSpecialCode("4636");
                    break;
                case R.id.bt_mediatek_ims:
                    tm.sendDialerSpecialCode("3646633");
                    break;
                case R.id.bt_sony_service:
                    tm.sendDialerSpecialCode("7378423");
                    break;
                case R.id.bt_nokia_enable_SA:
                    tm.sendDialerSpecialCode("5555");
                    break;
                case R.id.bt_samsung_ims:
                    tm.sendDialerSpecialCode("467");
                    break;
                case R.id.bt_huawei_project:
                    tm.sendDialerSpecialCode("2846579");
                    break;
                case R.id.bt_custom_special_code:
                    tm.sendDialerSpecialCode(special_code.getText().toString());
            }
        } else {
            Toast.makeText(getContext(), "Carrier Permissions needed, try to dial code system dialer", Toast.LENGTH_LONG).show();
        }
    }
}

