/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import static android.view.View.INVISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileOutputStream;
import java.util.UUID;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricView;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker.PingWorker;
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
    private MetricView rttMetric;
    private MetricView packetLossMetric;
    private WorkManager workManager;
    private ImageButton repeatButton;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Observer<WorkInfo> observer;
    private LiveData<WorkInfo> workInfoLiveData;

    public PingFragment() {
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.HomeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

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


    private void checkLastUUID(String uuidStr) {
        if (uuidStr == null || !isAdded() || getView() == null) return;

        UUID lastUUIDUUID = UUID.fromString(uuidStr);
        Log.d(TAG, "registerObserver: lastUUID changed: " + lastUUIDUUID);
        if(workInfoLiveData != null){
            workInfoLiveData.removeObserver(observer);
        }

        workInfoLiveData = workManager.getWorkInfoByIdLiveData(lastUUIDUUID);
        observer = workInfo -> {
            if (workInfo == null) return;
            Data progress = workInfo.getProgress();
            Log.d(TAG, "registerObserver: workInfo-State: " + workInfo.getState());
            double rtt = progress.getDouble(PingWorker.RTT, -1.0);
            if(rtt != -1.0) {
                rttMetric.update(rtt);
            }

            double packetLoss = progress.getDouble(PingWorker.PACKET_LOSS, -1.0);

            if(packetLoss != -1.0) {
                packetLossMetric.setVisibility(View.VISIBLE);
                Log.d(TAG, "onChanged: Packet Loss: " + packetLoss);
                packetLossMetric.update(packetLoss);
            }

            switch (workInfo.getState()) {
                case RUNNING:
                case SUCCEEDED:
                    break;
                case FAILED:
                case CANCELLED:
                    workInfoLiveData.removeObserver(observer);  // Optionally clean up
                    break;
                default:
                    break;
            }
        };
        workInfoLiveData.observe(getViewLifecycleOwner(), observer);

    }
    private void setupRepeatButton(){
        boolean isRepeat = spg.getSharedPreference(SPType.ping_sp).getBoolean("repeat_ping", false);
        int color = ContextCompat.getColor(ct,
                isRepeat ? R.color.design_default_color_primary : R.color.material_dynamic_secondary40);
        repeatButton.setColorFilter(color);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: PingFragment resumed");
        spg.getSharedPreference(SPType.ping_sp).registerOnSharedPreferenceChangeListener(listener);
        checkLastUUID(spg.getSharedPreference(SPType.ping_sp).getString(PingService.PING_LAST_UUID, null));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: PingFragment destroyed");
        spg.getSharedPreference(SPType.ping_sp).unregisterOnSharedPreferenceChangeListener(listener);
        workInfoLiveData.removeObserver(observer);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: PingFragment view created");
        listener = (sharedPreferences, key) -> {
            Log.d(TAG, "registerObserver: key changed: " + key);
            if (PingService.PING_LAST_UUID.equals(key)) {
                String uuidStr = sharedPreferences.getString(PingService.PING_LAST_UUID, null);
                Log.d(TAG, "registerObserver: lastUUID changed "+uuidStr);
                checkLastUUID(uuidStr);

            }
        };

        spg.getSharedPreference(SPType.ping_sp).registerOnSharedPreferenceChangeListener(listener);

        checkLastUUID(spg.getSharedPreference(SPType.ping_sp).getString(PingService.PING_LAST_UUID, null));
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ping, container, false);
        ct = requireContext();
        spg = SharedPreferencesGrouper.getInstance(ct);
        workManager = WorkManager.getInstance(ct);
        verticalLL = v.findViewById(R.id.ping_vertical_ll);
        horizontalLL1 = verticalLL.findViewById(R.id.ping_horizontal1_ll);

        toggleGroup = verticalLL.findViewById(R.id.ping_toggle_group);
        input = verticalLL.findViewById(R.id.ping_input);
        input.setText(spg.getSharedPreference(SPType.ping_sp).getString("ping_input", "-w 5 8.8.8.8"));
        repeatButton = v.findViewById(R.id.ping_repeat_button);
        setupRepeatButton();
        repeatButton.setOnClickListener(view -> {
            Log.d(TAG, "onCreateView: Repeat button clicked");
            boolean isRepeat = spg.getSharedPreference(SPType.ping_sp).getBoolean("repeat_ping", false);
            spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("repeat_ping", !isRepeat).apply();
            setupRepeatButton();
        });

        saveTextInputToSharedPreferences(input, "ping_input");
        boolean pingRunning = spg.getSharedPreference(SPType.ping_sp).getBoolean("ping_running", false);
        if (pingRunning) {
            v.findViewById(R.id.ping_start).setBackgroundColor(ct.getResources().getColor(R.color.purple_500, null));
        } else {
            v.findViewById(R.id.ping_stop).setBackgroundColor(ct.getResources().getColor(R.color.purple_500, null));
        }
        spg.setListener((sharedPreferences, key) -> {
            if (key != null && key.equals("ping_running")) {
                boolean isRunning = sharedPreferences.getBoolean("ping_running", false);
                handleInput(isRunning);
                if (isRunning) {
                    v.findViewById(R.id.ping_start).setBackgroundColor(ct.getResources().getColor(R.color.purple_500, null));
                    v.findViewById(R.id.ping_stop).setBackgroundColor(Color.TRANSPARENT);
                } else {
                    v.findViewById(R.id.ping_start).setBackgroundColor(Color.TRANSPARENT);
                    v.findViewById(R.id.ping_stop).setBackgroundColor(ct.getResources().getColor(R.color.purple_500, null));
                    spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();
                    toggleGroup.check(R.id.ping_stop);
                }
            }
        }, SPType.ping_sp);
        input.setEnabled(!pingRunning);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Log.d(TAG, "onButtonChecked: " + checkedId);
            if (!isChecked) return;
            switch (checkedId) {
                case R.id.ping_start:
                    v.findViewById(R.id.ping_start).setBackgroundColor(ct.getResources().getColor(R.color.purple_500, null));
                    v.findViewById(R.id.ping_stop).setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", true).apply();
                    Intent startIntent = new Intent(ct, PingService.class);
                    startIntent.putExtra(PingService.PING_INTENT_COMMAND, input.getText().toString());
                    startIntent.putExtra(PingService.PING_INTENT_ENABLE, true);
                    ct.startService(startIntent);
                    rttMetric.getMetricCalculator().resetMetric();
                    packetLossMetric.getMetricCalculator().resetMetric();
                    break;
                case R.id.ping_stop:
                    v.findViewById(R.id.ping_start).setBackgroundColor(Color.TRANSPARENT);
                    v.findViewById(R.id.ping_stop).setBackgroundColor(ct.getResources().getColor(R.color.purple_500, null));
                    spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();
                    Intent stopIntent = new Intent(ct, PingService.class);
                    stopIntent.putExtra(PingService.PING_INTENT_ENABLE, false);
                    ct.startService(stopIntent);
                    break;
            }

        });

        rttMetric = new MetricView(new MetricCalculator(METRIC_TYPE.PING_RTT), ct);
        rttMetric.setup("RTT [ms]");
        packetLossMetric = new MetricView(new MetricCalculator(METRIC_TYPE.PACKET_LOSS), ct);
        packetLossMetric.setup("Packet Loss [%]");
        LinearLayout metricsLL = new LinearLayout(ct);
        metricsLL.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams foo1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metricsLL.setLayoutParams(foo1);
        metricsLL.addView(rttMetric);
        metricsLL.addView(packetLossMetric);
        packetLossMetric.setVisibility(INVISIBLE);
        horizontalLL1.addView(metricsLL);

        return v;
    }

}