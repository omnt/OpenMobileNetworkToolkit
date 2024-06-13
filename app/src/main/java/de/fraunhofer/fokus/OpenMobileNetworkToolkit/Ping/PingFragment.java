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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.Metric;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PacketLossLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.RTTLine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class PingFragment extends Fragment {
    private final String TAG = "PingFragment";
    private Switch aSwitch;
    private LinearLayout verticalLL;
    private LinearLayout horizontalLL1;
    private Handler pingLogging;
    private FileOutputStream stream;
    private EditText input;
    private Context ct;
    private SharedPreferences sp;
    private Metric rttMetric;
    private Metric packetLossMetric;

    public PingFragment() {
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      }

    private void startPingService(){
        input.setEnabled(false);
        Intent pingStart = new Intent(ct, PingService.class);
        ct.startService(pingStart);
        rttMetric.resetMetric();
        PingParser pingParser = PingParser.getInstance(null);
        pingParser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PingInformation pi = (PingInformation) evt.getNewValue();
                switch (pi.getLineType()){
                    case RTT:
                        rttMetric.update( ((RTTLine)pi).getRtt());
                        break;
                    case PACKET_LOSS:
                        packetLossMetric.update(((PacketLossLine)pi).getPacketLoss());
                        break;
                }
            }
        });
    }
    private void stopPingService(){
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
                sp.edit().putString(name, field.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void handleInput(boolean ping_running){
        input.setEnabled(!ping_running);
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ping, container, false);

        sp = getContext().getSharedPreferences("Ping", Context.MODE_PRIVATE);
        verticalLL = v.findViewById(R.id.ping_vertical_ll);
        horizontalLL1 = verticalLL.findViewById(R.id.ping_horizontal1_ll);

        aSwitch = verticalLL.findViewById(R.id.ping_switch);
        input = verticalLL.findViewById(R.id.ping_input);
        input.setText(sp.getString("ping_input", "-w 5 8.8.8.8"));

        ct = requireContext();

        saveTextInputToSharedPreferences(input, "ping_input");
        aSwitch.setChecked(PingService.isRunning());

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: "+b);
                if(b) startPingService();
                else stopPingService();
            }
        });
        rttMetric = new Metric(METRIC_TYPE.PING_RTT, ct);
        packetLossMetric = new Metric(METRIC_TYPE.PING_PACKET_LOSS, ct);
        horizontalLL1.addView(rttMetric.createMainLL("RTT"));
        horizontalLL1.addView(packetLossMetric.createMainLL("Packet Loss"));
        packetLossMetric.setVisibility(View.INVISIBLE);
        return v;
    }
}