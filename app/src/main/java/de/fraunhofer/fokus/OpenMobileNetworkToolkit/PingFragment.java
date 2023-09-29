package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.TubeSpeedometer;
import com.github.anastr.speedviewlib.components.Style;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingWorker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingFragment extends Fragment {
    private final String TAG = "PingFragment";
    private Switch aSwitch;
    private LinearLayout verticalLL;
    private LinearLayout horizontalLL1;
    private Handler pingLogging;
    private FileOutputStream stream;
    private EditText input;
    private TubeSpeedometer pingSpeed;
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

    private void handleInput(boolean ping_running){
        input.setEnabled(!ping_running);
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ping, container, false);
        verticalLL = v.findViewById(R.id.ping_vertical_ll);
        horizontalLL1 = verticalLL.findViewById(R.id.ping_horizontal1_ll);
        aSwitch = verticalLL.findViewById(R.id.ping_switch);
        input = verticalLL.findViewById(R.id.ping_input);

        WorkManager wm = WorkManager.getInstance(requireContext());


        SharedPreferences sp = getContext().getSharedPreferences("Ping", Context.MODE_PRIVATE);


        pingSpeed = horizontalLL1.findViewById(R.id.pingSpeed);
        pingSpeed.makeSections(3, Color.CYAN, Style.BUTT);
        pingSpeed.getSections().get(0).setColor(Color.GREEN);
        pingSpeed.getSections().get(1).setColor(Color.BLUE);
        pingSpeed.getSections().get(2).setColor(Color.RED);
        pingSpeed.setSpeedTextColor(R.color.white);
        pingSpeed.setUnitTextColor(R.color.white);
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
        requireActivity().registerReceiver(receiver, new IntentFilter("ping_rtt"));

        return v;
    }



}