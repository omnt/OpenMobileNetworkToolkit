package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import static com.google.android.material.progressindicator.LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Executor;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Fragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private LinearProgressIndicator progressBar;
    private Iperf3Input iperf3Input;
    private Context ct;
    private MaterialButton sendBtn;
    private View view;
    private TextInputEditText ip;
    private TextInputEditText port;
    private TextInputEditText bitrate;
    private TextInputEditText duration;
    private TextInputEditText interval;
    private TextInputEditText bytes;
    private TextInputEditText streams;
    private TextInputEditText cport;


    private MaterialButtonToggleGroup mode;
    private MaterialButtonToggleGroup protocol;
    private MaterialButtonToggleGroup direction;

    private MaterialButton modeClient;
    private MaterialButton modeServer;
    private MaterialButton protocolTCP;
    private MaterialButton protocolUDP;
    private MaterialButton directionUp;
    private MaterialButton directionDown;
    private MaterialButton directonBidir;
    private Handler handler;

    private SharedPreferencesGrouper spg;

    private FrameLayout frameLayout;
    private String TAG = "Iperf3CardFragment";
    private String uuid;
    private int[] failedColors;
    private int[] runningColors;
    private int[] succesColors;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ct = requireContext();
    }

    /**
     * Create a text watcher
     * @param consumer
     * @param name
     * @return
     */
    private TextWatcher createTextWatcher(Consumer<String> consumer, String name) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                consumer.accept(charSequence.toString());
                spg.getSharedPreference(SPType.iperf3_sp).edit().putString(name, charSequence.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    /**
     * Set up the text watchers
     */
    private void setupTextWatchers() {
        ip.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setHost(s), Iperf3Parameter.HOST));
        port.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setPort(Integer.parseInt("0"+s)), Iperf3Parameter.PORT));
        bitrate.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setBandwidth(s), Iperf3Parameter.BITRATE));
        duration.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setTime(Integer.parseInt("0"+s)), Iperf3Parameter.TIME));
        interval.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setInterval(Double.parseDouble("0"+s)), Iperf3Parameter.INTERVAL));
        bytes.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setBytes(s), Iperf3Parameter.BYTES));
        streams.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setParallel(Integer.parseInt("0"+s)), Iperf3Parameter.PARALLEL));
        cport.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setCport(Integer.parseInt("0"+s) ), Iperf3Parameter.CPORT));
    }

    /**
     * Set the text from the shared preferences
     * @param editText
     * @param key
     */
    private void setTextFromSharedPreferences(TextInputEditText editText, String key) {
        if (spg.getSharedPreference(SPType.iperf3_sp).contains(key)) {
            editText.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(key, ""));
        }
    }
    Runnable setVisibility = new Runnable() {
        @Override
        public void run() {
            progressBar.setVisibility(LinearProgressIndicator.INVISIBLE);
            progressBar.setIndicatorColor(requireContext().getColor(R.color.purple_500));
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            RemoteWorkManager remoteWorkManager = RemoteWorkManager.getInstance(ct);

            WorkManager workManager = WorkManager.getInstance(ct);


            WorkQuery workQuery = WorkQuery.Builder
                    .fromTags(Arrays.asList(uuid))
                    .build();

            workManager.getWorkInfosForUniqueWorkLiveData(uuid).observe(getViewLifecycleOwner(), new Observer<List<WorkInfo>>() {
                @Override
                public void onChanged(List<WorkInfo> workInfos) {
                    for(WorkInfo workInfo:workInfos){
                        Log.d(TAG, "onChanged: "+workInfo.getProgress());
                    }
                }
            });



            ListenableFuture<List<WorkInfo>> foobar = remoteWorkManager.getWorkInfos(workQuery);

            Futures.addCallback(
                    foobar,
                    new FutureCallback<>() {
                        public void onSuccess(List<WorkInfo> result) {
                            for (WorkInfo workInfo : result) {
                                if (workInfo.getTags().contains(Iperf3ExecutorWorker.class.getCanonicalName())) {
                                    Log.d(TAG, "" + workInfo.getState());
                                    Log.d(TAG, "onSuccess: " + workInfo.getTags());
                                    Log.d(TAG, "onSuccess: " + workInfo.getState().isFinished());

                                    switch (workInfo.getState()) {
                                        case SUCCEEDED:
                                            progressBar.setIndicatorColor(succesColors);
                                            handler.postDelayed(setVisibility, 3000);
                                            break;
                                        case CANCELLED:
                                        case FAILED:
                                            progressBar.setIndicatorColor(failedColors);
                                            handler.postDelayed(setVisibility, 3000);
                                            break;
                                        case BLOCKED:
                                        case ENQUEUED:
                                        case RUNNING:
                                            if (progressBar.getVisibility() == LinearProgressIndicator.INVISIBLE) {
                                                progressBar.setVisibility(LinearProgressIndicator.VISIBLE);
                                            }
                                            String line = workInfo.getProgress().getString("line");
                                            Log.d(TAG, "onSuccess: "+workInfo.getProgress().toString());
                                            Log.d(TAG, "onSuccess: "+line);
                                            try {
                                                if(line == null) throw new JSONException("line empty");
                                                JSONObject interval = new JSONObject(line);
                                                if(!interval.getString("event").equals("interval")){
                                                    throw new JSONException("line not an interval");
                                                }

                                                int progess = Math.toIntExact(Math.round(interval.getJSONObject("data").getJSONObject("sum").getDouble("end")));
                                                Log.d(TAG, "onSuccess: "+progess);
                                                progressBar.setProgressCompat(progess, true);
                                            } catch (JSONException e) {
                                                Log.d(TAG, "onSuccess: "+e);
                                            }



                                            ListenableFuture<List<WorkInfo>> foobar = remoteWorkManager.getWorkInfos(workQuery);
                                            handler.postDelayed(runnable, 1000);
                                            break;
                                    }
                                }
                            }
                        }

                        public void onFailure(@NonNull Throwable thrown) {
                            // handle failure
                        }
                    },
                    getContext().getMainExecutor()
            );

        }
    };

    /**
     * Set the texts from the shared preferences
     */
    private void setTextsFromSharedPreferences(){
        setTextFromSharedPreferences(ip, Iperf3Parameter.HOST);
        setTextFromSharedPreferences(port, Iperf3Parameter.PORT);
        setTextFromSharedPreferences(bitrate, Iperf3Parameter.BITRATE);
        setTextFromSharedPreferences(duration, Iperf3Parameter.TIME);
        setTextFromSharedPreferences(interval, Iperf3Parameter.INTERVAL);
        setTextFromSharedPreferences(bytes, Iperf3Parameter.BYTES);
        setTextFromSharedPreferences(streams, Iperf3Parameter.STREAMS);
        setTextFromSharedPreferences(cport, Iperf3Parameter.CPORT);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_iperf3_input, container, false);
        // Initialize the TextView
        progressBar = view.findViewById(R.id.iperf3_progress);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setIndeterminate(false);
        String iperf3UUID = UUID.randomUUID().toString();
        Iperf3Parameter iperf3Parameter = new Iperf3Parameter(iperf3UUID);
        iperf3Input = new Iperf3Input(iperf3Parameter, "");
        sendBtn = view.findViewById(R.id.iperf3_send);
        spg = SharedPreferencesGrouper.getInstance(ct);
        handler = new Handler(Looper.getMainLooper());

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uuid = UUID.randomUUID().toString();
                iperf3Input.setTestUUID(uuid);
                iperf3Input.getParameter().setTestUUID(uuid);
                iperf3Input.getParameter().updatePaths();
                Iperf3Executor iperf3Executor = new Iperf3Executor(iperf3Input, getContext());
                iperf3Executor.execute();
                progressBar.setMax(iperf3Input.getParameter().getTime());
                handler.post(runnable); // start the first execution
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.standard_bottom_sheet));
        bottomSheetBehavior.setPeekHeight(20);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);

        ip = view.findViewById(R.id.iperf3_ip);
        port = view.findViewById(R.id.iperf3_port);
        bitrate = view.findViewById(R.id.iperf3_bandwidth);
        duration = view.findViewById(R.id.iperf3_duration);
        interval = view.findViewById(R.id.iperf3_interval);
        bytes = view.findViewById(R.id.iperf3_bytes);
        streams = view.findViewById(R.id.iperf3_streams);
        cport = view.findViewById(R.id.iperf3_cport);


        mode = view.findViewById(R.id.iperf3_mode_toggle_group);
        protocol = view.findViewById(R.id.iperf3_protocol_toggle_group);
        direction = view.findViewById(R.id.iperf3_direction_toggle_group);

        modeClient = view.findViewById(R.id.iperf3_client_button);
        modeServer = view.findViewById(R.id.iperf3_server_button);

        protocolTCP = view.findViewById(R.id.iperf3_tcp_button);
        protocolUDP = view.findViewById(R.id.iperf3_udp_button);

        directionDown = view.findViewById(R.id.iperf3_download_button);
        directionUp = view.findViewById(R.id.iperf3_upload_button);
        directonBidir = view.findViewById(R.id.iperf3_bidir_button);

        failedColors = new int[] {getContext().getColor(R.color.crimson),
                getContext().getColor(R.color.crimson), getContext().getColor(R.color.crimson)};
        runningColors = new int[] {getContext().getColor(R.color.purple_500),
                getContext().getColor(R.color.crimson), getContext().getColor(R.color.forestgreen)};
        succesColors = new int[] {getContext().getColor(R.color.forestgreen),
                getContext().getColor(R.color.forestgreen), getContext().getColor(R.color.forestgreen)};
        setupTextWatchers();
        setTextsFromSharedPreferences();
        try {
            switch (Iperf3Parameter.Iperf3Mode.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Parameter.MODE, String.valueOf(Iperf3Parameter.Iperf3Mode.UNDEFINED)))){
                case CLIENT:
                    updateModeState(modeClient, modeServer, Iperf3Parameter.Iperf3Mode.CLIENT);
                    break;
                case SERVER:
                    updateModeState(modeServer, modeClient, Iperf3Parameter.Iperf3Mode.SERVER);
                    break;
                case UNDEFINED:
                default:
                    modeClient.setBackgroundColor(Color.TRANSPARENT);
                    modeServer.setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.MODE, Iperf3Parameter.Iperf3Mode.UNDEFINED.toString()).apply();
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        try {
            switch (Iperf3Parameter.Iperf3Protocol.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.UNDEFINED.toString()))){
                case TCP:
                    updateProtocolState(protocolTCP, protocolUDP, Iperf3Parameter.Iperf3Protocol.TCP);
                    break;
                case UDP:
                    updateProtocolState(protocolUDP, protocolTCP, Iperf3Parameter.Iperf3Protocol.UDP);
                    break;
                case UNDEFINED:
                default:
                    protocolTCP.setBackgroundColor(Color.TRANSPARENT);
                    protocolUDP.setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.UNDEFINED.toString()).apply();
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        try {
            switch (Iperf3Parameter.Iperf3Direction.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Parameter.DIRECTION, Iperf3Parameter.Iperf3Direction.UNDEFINED.toString()))) {
                case UP:
                    updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Parameter.Iperf3Direction.UP);
                    break;
                case DOWN:
                    updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Parameter.Iperf3Direction.DOWN);
                    break;
                case BIDIR:
                    updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Parameter.Iperf3Direction.BIDIR);
                    break;
                case UNDEFINED:
                default:
                    directionUp.setBackgroundColor(Color.TRANSPARENT);
                    directionDown.setBackgroundColor(Color.TRANSPARENT);
                    directonBidir.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }

        mode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.iperf3_client_button:
                            updateModeState(modeClient, modeServer, Iperf3Parameter.Iperf3Mode.CLIENT);
                            break;
                        case R.id.iperf3_server_button:
                            updateModeState(modeServer, modeClient, Iperf3Parameter.Iperf3Mode.SERVER);
                            break;
                    }
                }
            }
        });
        protocol.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.iperf3_tcp_button:
                            updateProtocolState(protocolTCP, protocolUDP, Iperf3Parameter.Iperf3Protocol.TCP);
                            break;
                        case R.id.iperf3_udp_button:
                            updateProtocolState(protocolUDP, protocolTCP, Iperf3Parameter.Iperf3Protocol.UDP);
                            break;
                    }
                }
            }
        });
        direction.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.iperf3_upload_button:
                            updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Parameter.Iperf3Direction.UP);
                            break;
                        case R.id.iperf3_download_button:
                            updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Parameter.Iperf3Direction.DOWN);
                            break;
                        case R.id.iperf3_bidir_button:
                            updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Parameter.Iperf3Direction.BIDIR);
                            break;
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        view.requestLayout();
    }



    private void updateModeState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Parameter.Iperf3Mode protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setMode(protocol);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.MODE, protocol.toString()).apply();
    }

    private void updateProtocolState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Parameter.Iperf3Protocol protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setProtocol(protocol);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.PROTOCOL, protocol.toString()).apply();
    }

    private void updateDirectionState(MaterialButton activeButton, MaterialButton inactiveButton1, MaterialButton inactiveButton2, Iperf3Parameter.Iperf3Direction direction) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton1.setBackgroundColor(Color.TRANSPARENT);
        inactiveButton2.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setDirection(direction);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.DIRECTION, direction.toString()).apply();
    }
}