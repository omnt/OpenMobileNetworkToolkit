/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileOutputStream;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.Metric;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PacketLossLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.RTTLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class PingFragment extends Fragment {
    private final String TAG = "PingFragment";
    private Switch aSwitch;
    private MaterialButtonToggleGroup toggleGroup;
    private LinearLayout verticalLL;
    private LinearLayout horizontalLL1;
    private Handler pingLogging;
    private FileOutputStream stream;
    private TextInputEditText input;
    private Context ct;
    private SharedPreferencesGrouper spg;
    private Metric rttMetric;
    private Metric packetLossMetric;

    public PingFragment() {
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void startPingService() {
        input.setEnabled(false);
        Intent pingStart = new Intent(ct, PingService.class);
        ct.startService(pingStart);
        rttMetric.resetMetric();
        packetLossMetric.resetMetric();
    }

    private void stopPingService() {
        input.setEnabled(true);
        Intent pingStart = new Intent(ct, PingService.class);
        ct.stopService(pingStart);
    }


    private void saveTextInputToSharedPreferences(EditText field, String name) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                spg.getSharedPreference(SPType.ping_sp).edit().putString(name, field.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void handleInput(boolean ping_running) {
        input.setEnabled(!ping_running);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ping, container, false);
        ct = requireContext();
        spg = SharedPreferencesGrouper.getInstance(ct);

        verticalLL = v.findViewById(R.id.ping_vertical_ll);
        horizontalLL1 = verticalLL.findViewById(R.id.ping_horizontal1_ll);

        toggleGroup = verticalLL.findViewById(R.id.ping_toggle_group);
        input = verticalLL.findViewById(R.id.ping_input);
        input.setText(spg.getSharedPreference(SPType.ping_sp).getString("ping_input", "-w 5 8.8.8.8"));
        input.setEnabled(!PingService.isRunning());
        saveTextInputToSharedPreferences(input, "ping_input");
        boolean pingRunning = spg.getSharedPreference(SPType.ping_sp).getBoolean("ping_running", false);
        if (pingRunning && PingService.isRunning()) {
            v.findViewById(R.id.ping_start).setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        } else {
            v.findViewById(R.id.ping_stop).setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        }
        spg.setListener((sharedPreferences, key) -> {
            if (key != null && key.equals("ping_running")) {
                boolean isRunning = sharedPreferences.getBoolean("ping_running", false);
                handleInput(isRunning);
                if (isRunning) {
                    v.findViewById(R.id.ping_start).setBackgroundColor(getResources().getColor(R.color.purple_500, null));
                    v.findViewById(R.id.ping_stop).setBackgroundColor(Color.TRANSPARENT);
                } else {
                    v.findViewById(R.id.ping_start).setBackgroundColor(Color.TRANSPARENT);
                    v.findViewById(R.id.ping_stop).setBackgroundColor(getResources().getColor(R.color.purple_500, null));
                }

            }
        }, SPType.ping_sp);

        input.setEnabled(!pingRunning);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Log.d(TAG, "onButtonChecked: " + checkedId);
            if (!isChecked) return;
            switch (checkedId) {
                case R.id.ping_start:
                    startPingService();
                    v.findViewById(R.id.ping_start).setBackgroundColor(getResources().getColor(R.color.purple_500, null));
                    v.findViewById(R.id.ping_stop).setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", true).apply();

                    break;
                case R.id.ping_stop:
                    v.findViewById(R.id.ping_start).setBackgroundColor(Color.TRANSPARENT);
                    v.findViewById(R.id.ping_stop).setBackgroundColor(getResources().getColor(R.color.purple_500, null));
                    stopPingService();
                    spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();
                    break;
            }

        });

        rttMetric = new Metric(METRIC_TYPE.PING_RTT, ct);
        packetLossMetric = new Metric(METRIC_TYPE.PING_PACKET_LOSS, ct);
        LinearLayout metricsLL = new LinearLayout(ct);
        metricsLL.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams foo1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metricsLL.setLayoutParams(foo1);
        metricsLL.addView(rttMetric.createMainLL("RTT [ms]"));
        metricsLL.addView(packetLossMetric.createMainLL("Packet Loss [%]"));

        horizontalLL1.addView(metricsLL);


        PingParser pingParser = PingParser.getInstance(null);
        pingParser.addPropertyChangeListener(evt -> {
            PingInformation pi = (PingInformation) evt.getNewValue();
            switch (pi.getLineType()) {
                case RTT:
                    rttMetric.update(((RTTLine) pi).getRtt());
                    break;
                case PACKET_LOSS:
                    packetLossMetric.update(((PacketLossLine) pi).getPacketLoss());
                    //packetLossMetric.setVisibility(View.VISIBLE);
                    break;
            }
        });

        //packetLossMetric.setVisibility(View.INVISIBLE);
        return v;
    }

}