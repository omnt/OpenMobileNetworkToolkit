/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
    TelephonyManager tm;
    boolean cp = false;
    private TextView special_code;

    public SpecialCodesFragment() {
        super(R.layout.fragment_special_codes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        tm = GlobalVars.getInstance().getTm();
        cp = tm.hasCarrierPrivileges();
        View v = inflater.inflate(R.layout.fragment_special_codes, parent, false);
        Button android_testing = v.findViewById(R.id.bt_android_testing);
        android_testing.setOnClickListener(this::buttonHandler);
        Button mediatek_ims = v.findViewById(R.id.bt_mediatek_ims);
        mediatek_ims.setOnClickListener(this::buttonHandler);
        Button sony_service = v.findViewById(R.id.bt_sony_service);
        sony_service.setOnClickListener(this::buttonHandler);
        Button nokia_enable_sa = v.findViewById(R.id.bt_nokia_enable_SA);
        nokia_enable_sa.setOnClickListener(this::buttonHandler);
        Button samsung_ims = v.findViewById(R.id.bt_samsung_ims);
        samsung_ims.setOnClickListener(this::buttonHandler);
        Button huawei_projects = v.findViewById(R.id.bt_huawei_project);
        huawei_projects.setOnClickListener(this::buttonHandler);
        Button custom_special_code = v.findViewById(R.id.bt_custom_special_code);
        custom_special_code.setOnClickListener(this::buttonHandler);
        special_code = v.findViewById(R.id.tv_special_code);
        if (!cp) {
            android_testing.setEnabled(false);
            mediatek_ims.setEnabled(false);
            sony_service.setEnabled(false);
            nokia_enable_sa.setEnabled(false);
            samsung_ims.setEnabled(false);
            huawei_projects.setEnabled(false);
            custom_special_code.setEnabled(false);
            TextView tv = v.findViewById(R.id.special_code_warning);
            tv.setText(
                "Carrier Permissions needed to dial special codes, try to dial code in system dialer");
        }

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireContext().getPackageManager()
            .hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            tm = GlobalVars.getInstance().getTm();
            cp = tm.hasCarrierPrivileges();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("NonConstantResourceId")
    private void buttonHandler(View view) {
        if (cp) {
            try {
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
            } catch (Exception e) {
                Toast.makeText(getContext(),
                                "Current subscription (SIM) is not allowed to dial", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Toast.makeText(getContext(),
                    "Carrier Permissions needed, try to dial code in system dialer", Toast.LENGTH_LONG)
                .show();
        }
    }
}