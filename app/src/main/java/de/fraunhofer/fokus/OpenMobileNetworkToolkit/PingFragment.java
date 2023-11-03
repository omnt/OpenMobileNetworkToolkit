/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import com.github.anastr.speedviewlib.TubeSpeedometer;
import com.github.anastr.speedviewlib.components.Style;

import java.io.FileOutputStream;

public class PingFragment extends Fragment {
    private final String TAG = "PingFragment";
    private Switch aSwitch;
    private LinearLayout verticalLL;
    private LinearLayout horizontalLL1;
    private Handler pingLogging;
    private FileOutputStream stream;
    private EditText input;
    private TubeSpeedometer pingSpeed;


    private SharedPreferences sp;
    public PingFragment() {
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      }
    private void setupPing(){
        input.setEnabled(false);
        Intent pingStart = new Intent(getContext(), LoggingService.class);
        pingStart.putExtra("input", input.getText().toString());
        pingStart.putExtra("ping", true);
        getContext().startService(pingStart);

    }
    private void stopPing(){
        Intent pingStart = new Intent(getContext(), LoggingService.class);
        pingStart.putExtra("ping", true);
        pingStart.putExtra("ping_stop", true);
        pingStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startService(pingStart);
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
        input.setText(sp.getString("ping_input", "-w 5 8.8.8"));
        WorkManager wm = WorkManager.getInstance(requireContext());



        saveTextInputToSharedPreferences(input, "ping_input");

        pingSpeed = horizontalLL1.findViewById(R.id.pingSpeed);
        pingSpeed.makeSections(3, Color.CYAN, Style.BUTT);
        pingSpeed.getSections().get(0).setColor(Color.GREEN);
        pingSpeed.getSections().get(1).setColor(Color.BLUE);
        pingSpeed.getSections().get(2).setColor(Color.RED);
        pingSpeed.setSpeedTextColor(R.color.material_dynamic_tertiary70);
        pingSpeed.setUnitTextColor(R.color.material_dynamic_tertiary70);
        pingSpeed.setUnit("ms");
        pingSpeed.setMinSpeed(0);
        pingSpeed.setMaxSpeed(100);
        pingSpeed.setTextColor(R.color.white);
        pingSpeed.setUnitUnderSpeedText(true);
        aSwitch.setChecked(sp.getBoolean("switch", false));

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: "+b);
                if(b) setupPing();
                else stopPing();
                sp.edit().putBoolean("switch", b).apply();
            }
        });
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double rtt = intent.getExtras().getDouble("ping_rtt");
                pingSpeed.speedTo((float) rtt);
                boolean ping_running = intent.getExtras().getBoolean("ping_running");
                handleInput(ping_running);
            }
        };
        requireActivity().registerReceiver(receiver, new IntentFilter("ping_rtt"), Context.RECEIVER_EXPORTED);

        return v;
    }



}