/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;

import com.github.anastr.speedviewlib.TubeSpeedometer;
import com.github.anastr.speedviewlib.components.Style;

import java.io.FileOutputStream;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.LoggingService;
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
    private TextView pingViewer;
    private ScrollView scrollView;
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
        pingViewer.setText("");
        Intent pingStart = new Intent(ct, PingService.class);

        pingStart.putExtra("ping", true);
        ct.startService(pingStart);
    }
    private void stopPing(){
        input.setEnabled(true);
        Intent pingStart = new Intent(ct, PingService.class);
        pingStart.putExtra("ping", false);
        pingStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ct.startService(pingStart);
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
        pingViewer = horizontalLL1.findViewById(R.id.ping_viewer);
        scrollView = horizontalLL1.findViewById(R.id.ping_scrollviewer);

        saveTextInputToSharedPreferences(input, "ping_input");
        aSwitch.setChecked(sp.getBoolean("ping", false));

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: "+b);
                if(b) setupPing();
                else stopPing();
            }
        });
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double rtt = intent.getExtras().getDouble("ping_rtt");
                boolean ping_running = intent.getExtras().getBoolean("ping_running");
                handleInput(ping_running);
                String pingLine = intent.getExtras().getString("ping_line");
                pingViewer.append(pingLine+"\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        };
        requireActivity().registerReceiver(receiver, new IntentFilter("ping"), Context.RECEIVER_EXPORTED);


        pingViewer.setMovementMethod(new ScrollingMovementMethod());
        return v;
    }
}